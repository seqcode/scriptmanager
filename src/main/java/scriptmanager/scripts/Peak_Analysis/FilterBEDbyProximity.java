package scriptmanager.scripts.Peak_Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import scriptmanager.objects.CoordinateObjects.BEDCoord;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.GZipUtilities;

/**
 * Filter coordinate peaks in a BED file by a given exclusion distance
 * 
 * @author Abeer Almutairy
 * @see scriptmanager.cli.Peak_Analysis.FilterBEDbyProximityCLI
 * @see scriptmanager.window_interface.Peak_Analysis.FilterBEDbyProximityOutput
 * @see scriptmanager.window_interface.Peak_Analysis.FilterBEDbyProximityWindow
 */
public class FilterBEDbyProximity {

	private File INPUT;
	private int CUTOFF;
	private PrintStream PS = null;

	private PrintStream OUT_Filter = null;
	private PrintStream OUT_Cluster = null;

	/**
	 * Initialize input arguments for running FilterBEDbyProximity
	 * 
	 * @param input      BED file to filter
	 * @param outputBase basename for "-FILTER.bed" and "-CLUSTER.bed" output files
	 * @param cutoff     exclusion distance (bp) to filter peaks by
	 * @param ps         if non-null, progress updates will be printed to this
	 *                   PrintStream. (i.e. GUI passes a PrintStream that wraps the
	 *                   TextArea component of the Output window to display progress
	 *                   updates to the user)
	 * @param gzOutput   whether or not to gzip output
	 * @throws IOException
	 */
	public FilterBEDbyProximity(File input, File outputBase, int cutoff, PrintStream ps, boolean gzOutput) throws IOException {
		INPUT = input;
		CUTOFF = cutoff;
		PS = ps;

		// Construct output name
		if (outputBase == null) {
			outputBase = new File(ExtensionFileFilter.stripExtensionIgnoreGZ(INPUT) + "_" + Integer.toString(CUTOFF) + "bp");
		}
		// Initialize output streams
		OUT_Filter = GZipUtilities.makePrintStream(new File(outputBase.getAbsoluteFile() + "-FILTER.bed" + (gzOutput? ".gz": "")), gzOutput); 
		OUT_Cluster = GZipUtilities.makePrintStream(new File(outputBase.getAbsoluteFile() + "-CLUSTER.bed" + (gzOutput? ".gz": "")), gzOutput);
	
	}

	/**
	 * Runs the filtering operation, outputting peaks to "-FILTER.bed" and other
	 * reads to "-CLUSTER.bed"
	 * 
	 * @throws IOException          Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the
	 *                              same time
	 */
	public void run() throws FileNotFoundException, IOException, InterruptedException {
		// Print update
		printPS("Filtering BED file with a cutoff: " + CUTOFF + " in " + INPUT.getName());
		printPS("Starting: " + new Timestamp(new Date().getTime()).toString());

		//Check if input file is compressed and assign appropriate input stream
		BufferedReader br = GZipUtilities.makeReader(INPUT);
		List<BEDCoord> bedArray = new ArrayList<BEDCoord>();
		List<Integer> failArray = new ArrayList<Integer>();
		
		//load bed coords into bedArray
		String line;
		while((line = br.readLine()) != null) {
			bedArray.add(new BEDCoord(line));
			bedArray.get(bedArray.size() - 1).calcMid();
			failArray.add(Integer.valueOf(0));
		}
		br.close();
		Collections.sort(bedArray, BEDCoord.PeakMidpointComparator);
		Collections.sort(bedArray, BEDCoord.PeakChromComparator);
		
		//Check each coord pair for proximity
		for(int i = 0; i < bedArray.size(); i++) {
			//check coords behind
			int INDEX = i - 1;
			if(INDEX >= 0) {
				while((bedArray.get(i).getChrom().equals(bedArray.get(INDEX).getChrom())) && (Math.abs(bedArray.get(i).getMid() - bedArray.get(INDEX).getMid()) <= CUTOFF)) 
				{
					if(bedArray.get(i).getScore() > bedArray.get(INDEX).getScore()) 
					{
						failArray.set(INDEX, Integer.valueOf(1));
					}
					else if((bedArray.get(i).getScore() == bedArray.get(INDEX).getScore()) && (bedArray.get(INDEX).getMid() > bedArray.get(i).getMid()))
					{
						failArray.set(INDEX, Integer.valueOf(1));
					}
					else {
						failArray.set(i, Integer.valueOf(1));
					}
					INDEX--;
					if(INDEX < 0) { break; } // To avoid redundant pair-wise checks
				}
			}
			//check coords in front
			INDEX = i + 1;
	        if(INDEX < bedArray.size()) {
	        	while((bedArray.get(i).getChrom().equals(bedArray.get(INDEX).getChrom())) && (Math.abs(bedArray.get(i).getMid() - bedArray.get(INDEX).getMid()) <= CUTOFF)) {
	        		
	        		if(bedArray.get(i).getScore() > bedArray.get(INDEX).getScore())
	        		{
	        			failArray.set(INDEX, Integer.valueOf(1));
	        		}
	        		else if((bedArray.get(i).getScore() == bedArray.get(INDEX).getScore()) && (bedArray.get(INDEX).getMid() > bedArray.get(i).getMid()))
	        		{
	        			failArray.set(INDEX, Integer.valueOf(1));
	        		}
	        		else
	        		{
	        			failArray.set(i, Integer.valueOf(1));
	        		}
	        		INDEX++;
	        		if(INDEX == bedArray.size()){ break; } // To avoid redundant pair-wise checks
	        	}
	        }
		}
		
		//print bed coords to respective files based on fail array
		for(int x = 0; x < bedArray.size(); x++) {
			if(failArray.get(x).intValue() == 0) {
				OUT_Filter.println(bedArray.get(x).toString()); 
			} else {
				OUT_Cluster.println(bedArray.get(x).toString()); 
			}
		}
		// close streams
		OUT_Filter.close();
		OUT_Cluster.close();
		// Print update
		printPS("Completing: " + new Timestamp(new Date().getTime()).toString());
	}

	/**
	 * Print logs to both STDERR and the object's PrintStream object(PS) if non-null.
	 * @param message
	 */
	private void printPS(String message){
		if(PS!=null) PS.println(message);
		System.err.println(message);
	}
}