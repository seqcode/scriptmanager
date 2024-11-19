package scriptmanager.scripts.Peak_Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptmanager.objects.CoordinateObjects.BEDCoord;
import scriptmanager.util.GZipUtilities;

/**
 * Align BED peaks to a reference BED file and create a CDT file
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Peak_Analysis.BEDPeakAligntoRefCLI
 * @see scriptmanager.window_interface.Peak_Analysis.BEDPeakAligntoRefOutput
 * @see scriptmanager.window_interface.Peak_Analysis.BEDPeakAligntoRefWindow
 */
public final class BEDPeakAligntoRef {

	/**
	 * Runs the peak alignment, writing to output file and reporting progress
	 * 
	 * @throws IOException          Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the
	 *                              same time
	 */
	public static void execute(File refBED, File peakBED, File OUTPUT, boolean separate, PrintStream PS, boolean gzOutput) throws FileNotFoundException, IOException, InterruptedException {
		// Write starting message
		printPS(PS, "Mapping: " + peakBED.getName() + " to " + refBED.getName());
		printPS(PS, "Starting: " + new Timestamp(new Date().getTime()).toString());

		// Set-up PrintStreams
		PrintStream OUT1 = null;
		PrintStream OUT2 = null;
		if (separate) {
			File TEMP1 = new File(OUTPUT.getAbsolutePath() + "_sense.cdt" + (gzOutput ? ".gz": ""));
			File TEMP2 = new File(OUTPUT.getAbsolutePath() + "_anti.cdt" + (gzOutput ? ".gz": ""));
			OUT1 = GZipUtilities.makePrintStream(TEMP1, gzOutput);
			OUT2 = GZipUtilities.makePrintStream(TEMP2, gzOutput);
		} else {
			File TEMP1 = new File(OUTPUT.getAbsolutePath() + "_combined.cdt" + (gzOutput ? ".gz": ""));
			OUT1 = GZipUtilities.makePrintStream(TEMP1, gzOutput);
		}

		// =====Make peakMap=====
		Map<String, List<String>> peakMap = new HashMap<>();
		String key ;
		//Checks if BED file is compressed, creates appropriate input stream
		BufferedReader br = GZipUtilities.makeReader(peakBED);
		for (String line; (line = br.readLine()) != null; ) {
			key = new BEDCoord(line).getChrom();
			if(!peakMap.containsKey(key)) {
				peakMap.put(key, new ArrayList<String>());
			}
			peakMap.get(key).add(line);
		}
		br.close();

		// =====Overlay on Reference=====
		int counter = 0; // for tracking progress
		//Checks if BED file is compressed, creates appropriate input stream
		br = GZipUtilities.makeReader(refBED);
		for (String line; (line = br.readLine()) != null;) {
			// Parse ref BED record and chromosome
			BEDCoord refCoord = new BEDCoord(line);
			String chr = refCoord.getChrom();
			// Initialize CDT row arrays
			int cdtLength = (int)(refCoord.getStop() - refCoord.getStart());
			int cdtArr1[] = new int[cdtLength];
			int cdtArr2[] = new int[cdtLength];
			// Process one chr at a time
			if (peakMap.containsKey(chr)) {
				for (int i = 0; i < peakMap.get(chr).size(); i++) {
					// Parse peak BED record
					BEDCoord peakCoord = new BEDCoord(peakMap.get(chr).get(i));
					// Skip peak if range does not overlap ref
					if (peakCoord.getStop() < refCoord.getStart() || refCoord.getStop() <= peakCoord.getStart()) {
						continue;
					}
					// Calculate coordinate range of CDT
					int START = (int)(peakCoord.getStart() - refCoord.getStart());
					int STOP = START + (int)(peakCoord.getStop() - peakCoord.getStart());
					// Loop through CDT range (use exclusive notation but mark at least 1 in cases of START=STOP)
					for(int x = START; x < Math.max(STOP, START+1); x++) {
						// Check if valid coordinate range
						if(x >= 0 && x < cdtLength) {
							// Mark in appropriate arrays
							if (separate) {
								// Use verbose strand check to ensure non "-" chars default to positive
								if (peakCoord.getDir().equals("-")) {
									if (refCoord.getDir().equals("-")) {
										cdtArr1[x]++;
									} else {
										cdtArr2[x]++;
									}
								} else {
									if (refCoord.getDir().equals("-")) {
										cdtArr2[x]++;
									} else {
										cdtArr1[x]++;
									}
								}
							} else {
								cdtArr1[x]++;
							}
						}
					}
				}
			}
			// Print CDT header
			if (counter == 0) {
				OUT1.print("YORF" + "\t" + "NAME");
				if (separate) { OUT2.print("YORF" + "\t" + "NAME"); }
				for (int j = 0; j < cdtLength; j++) {
					OUT1.print("\t" + j);
					if (separate) { OUT2.print("\t" + j); }
				}
				OUT1.print("\n");
				if (separate) { OUT2.print("\n"); }
			}
			// Print row header
			OUT1.print(refCoord.getName() + "\t" + refCoord.getName());
			if (separate) { OUT2.print(refCoord.getName() + "\t" + refCoord.getName()); }
			// Print array contents
			if (refCoord.getDir().equals("-")) {
				for (int j = cdtLength - 1; j >= 0; j--) {
					OUT1.print("\t" + cdtArr1[j]);
					if (separate) { OUT2.print("\t" + cdtArr2[j]); }
				}
			} else {
				for (int i = 0; i < cdtLength; i++) {
					OUT1.print("\t" + cdtArr1[i]);
					if (separate) { OUT2.print("\t" + cdtArr2[i]); }
				}
			}
			OUT1.print("\n");
			if (separate) { OUT2.print("\n"); }
			// Increment and report proress counter
			counter++;
			if(counter % 1000 == 0) {
				printPS(PS, "Reference rows processed: " + counter);
			}
		}
		// Close file streams
		br.close();
		OUT1.close();
		if (separate) { OUT2.close(); }
		// Print update
		printPS(PS, "Completing: " + new Timestamp(new Date().getTime()).toString());
	}

	/**
	 * Write a message to both STDERR and the provided PrintStream if PS is non-null
	 * 
	 * @param PS      the object to attempt to write to
	 * @param message the message
	 */
	private static void printPS(PrintStream PS, String message) {
		if (PS != null) {
			PS.println(message);
		}
		System.err.println(message);
	}
}