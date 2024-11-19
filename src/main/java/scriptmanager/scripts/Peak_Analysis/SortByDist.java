package scriptmanager.scripts.Peak_Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import scriptmanager.objects.CoordinateObjects.BEDCoord;
import scriptmanager.objects.CoordinateObjects.GFFCoord;
import scriptmanager.objects.CoordinateObjects.GenomicCoord;
import scriptmanager.util.GZipUtilities;

/**
 * Sort RefPT BED file by distance to closest peak within a range.
 * 
 * @author Olivia Lang
 * @see scriptmanager.cli.Peak_Analysis.SortByDistCLI
 * @see scriptmanager.window_interface.Peak_Analysis.PileupScripts.SortByDistOutput
 * @see scriptmanager.window_interface.Peak_Analysis.PileupScripts.SortByDistWindow
 */
public class SortByDist {
	private PrintStream PS = null;
	private PrintStream OUT = null;
	private File PEAK = null;
	private File REF = null;

	private boolean matchStrand = false;
	private Long maxUp = null;
	private Long maxDown = null;
	
	public SortByDist(File ref, File peak, File out, boolean gzOutput, boolean m, Long upstream, Long downstream, PrintStream ps) throws IOException {
		PS = ps;
		OUT = GZipUtilities.makePrintStream(out, gzOutput);
		PEAK = peak;
		REF = ref;
		matchStrand = m;
		if (upstream != null) {
			maxUp = upstream.longValue();
		}
		if (downstream != null) {
			maxDown = downstream.longValue();
		}
	}

	public void sortGFF() throws IOException, InterruptedException {
		printPS("Mapping: " + PEAK + " to " + REF);
		printPS("Starting: " + getTimeStamp());

		// Initialize maps to store peak and ref info
		Map<String, ArrayList<String>> peakMap = new HashMap<String, ArrayList<String>>();
		Map<String, Long> refMapDist = new HashMap<String, Long>();

		printPS("Loading Peaks...");
		String key ;
		// Load peak file, group by chrname
		BufferedReader br = GZipUtilities.makeReader(PEAK);
		for (String line; (line = br.readLine()) != null; ) {
			key = new GFFCoord(line).getChrom();
			if(!peakMap.containsKey(key)) {
				peakMap.put(key, new ArrayList<String>());
			}
			peakMap.get(key).add(line);
		}
		br.close();

		printPS("Processing Ref coordinates...");
		int counter = 0;
		// Parse ref file
		br = GZipUtilities.makeReader(REF);
		for (String line; (line = br.readLine()) != null; ) {
			// Initialize ref GFFCoord object
			GFFCoord refCoord = new GFFCoord(line);
			refCoord.calcMid();
			// Initialize minDiff update var
			long minDist = Long.MAX_VALUE;
			// Check that peakList contains peaks with ref's chromosome
			if (peakMap.containsKey(refCoord.getChrom())) {
				// Iterate through these chr-matched peaks
				for (String pLine : peakMap.get(refCoord.getChrom())) {
					// Initialize peak object
					GFFCoord peakCoord = new GFFCoord(pLine);
					peakCoord.calcMid();
					// Check if this peak is closer and update min dist score if closer
					if (validateCoord(peakCoord, refCoord, minDist)) {
						// Store directional distance
						minDist = peakCoord.getMid() - refCoord.getMid();
						if (refCoord.getDir().equals("-")) { minDist *= -1; }
					}
				}
			}
			// Add ref line with minDist
			refMapDist.put(line, minDist);

			// Update progress
			counter++;
			if (counter % 1000 == 0){
				printPS("Reference rows processed: " + counter);
			}
		}
		br.close();

		printPS("Sorting...");
		List<String> sortedRefLines = refMapDist.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		printPS("Writing...");
		for (int i = 0; i < sortedRefLines.size(); i++) {
			OUT.println(sortedRefLines.get(i));
		}
		OUT.close();

//		// TODO: rewrite to sort stream and write in same step (to reduce data held in memory)
//		printPS("Sort and write...");
//		refMapDist.entrySet().stream()
//			.sorted(Map.Entry.comparingByValue())
//			.map(Map.Entry::getKey)
//			.forEach(line -> {
//				OUT.println(line);
//			});

		printPS("Completing: " + getTimeStamp());
	}

	public void sortBED() throws IOException, InterruptedException {
		printPS("Mapping: " + PEAK + " to " + REF);
		printPS("Starting: " + getTimeStamp());

		// Initialize maps to store peak and ref info
		Map<String, ArrayList<String>> peakMap = new HashMap<String, ArrayList<String>>();
		Map<String, Long> refMapDist = new HashMap<String, Long>();

		printPS("Loading Peaks...");
		String key ;
		// Load peak file, group by chrname
		BufferedReader br = GZipUtilities.makeReader(PEAK);
		for (String line; (line = br.readLine()) != null; ) {
			key = new BEDCoord(line).getChrom();
			if(!peakMap.containsKey(key)) {
				peakMap.put(key, new ArrayList<String>());
			}
			peakMap.get(key).add(line);
		}
		br.close();

		printPS("Processing Ref coordinates...");
		int counter = 0;
		// Parse ref file
		br = GZipUtilities.makeReader(REF);
		for (String line; (line = br.readLine()) != null; ) {
			// Initialize ref BEDCoord object
			BEDCoord refCoord = new BEDCoord(line);
			refCoord.calcMid();
			// Initialize minDiff update var
			long minDist = Long.MAX_VALUE;
			// Check that peakList contains peaks with ref's chromosome
			if (peakMap.containsKey(refCoord.getChrom())) {
				// Iterate through these chr-matched peaks
				for (String pLine : peakMap.get(refCoord.getChrom())) {
					// Initialize peak object
					BEDCoord peakCoord = new BEDCoord(pLine);
					peakCoord.calcMid();
					// Check if this peak is closer and update min dist score if closer
					if (validateCoord(peakCoord, refCoord, minDist)) {
						// Store directional distance
						minDist = peakCoord.getMid() - refCoord.getMid();
						if (refCoord.getDir().equals("-")) { minDist *= -1; }
					}
				}
			}
			// Add ref line with minDist
			refMapDist.put(line, minDist);

			// Update progress
			counter++;
			if (counter % 1000 == 0){
				printPS("Reference rows processed: " + counter);
			}
		}
		br.close();

		printPS("Sorting...");
		List<String> sortedRefLines = refMapDist.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		printPS("Writing...");
		for (String line : sortedRefLines) {
			OUT.println(line + "\t" + (refMapDist.get(line) == Long.MAX_VALUE ? "NaN" : refMapDist.get(line)));
		}
		OUT.close();

//		// TODO: rewrite to sort stream and write in same step (to reduce data held in memory)
//		printPS("Sort and write...");
//		refMapDist.entrySet().stream()
//			.sorted(Map.Entry.comparingByValue())
//			.map(Map.Entry::getKey)
//			.forEach(line -> {
//				OUT.println(line);
//			});

		printPS("Completing: " + getTimeStamp());
	}

	/**
	 * Check if the distance (abs value) between the two input coordinates (peak -
	 * ref) is smaller than the (abs value) of the provided minDistance.
	 * 
	 * @param peak        peak midpoint
	 * @param ref         reference point peak midpoint
	 * @param minDistance min distance to compare against
	 * @return true if closer, false if not
	 */
	private boolean validateCoord(GenomicCoord peak, GenomicCoord ref, long minDistance) {
		// Calculate strand-adjusted directional distance
		long distPeakRef = (peak.getMid() - ref.getMid()) * (ref.getDir().equals("-") ? -1 : 1);
		// Determine if this distance is closer than min
		boolean closer = Math.abs(minDistance) >= Math.abs(distPeakRef);
		// Check if peak matches strand if user option is set
		if (matchStrand && (peak.getDir().equals("-") != ref.getDir().equals("-"))) { return false; }
		// Check if peak is within bounds and return false if not
		if (closer) {
			// Check bounds
			if (maxDown != null && distPeakRef > maxDown) { return false; }
			if (maxUp != null && distPeakRef < maxUp) { return false; }
		}
		return closer;
	}

	private static String getTimeStamp() {
		Date date= new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
	
	private void printPS(String message){
		if (PS!=null) { PS.println(message); }
		System.err.println(message);
	}
}