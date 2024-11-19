package scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import scriptmanager.objects.CoordinateObjects.BEDCoord;
import scriptmanager.util.GZipUtilities;

/**
 * Sort coordinate intervals (BED) by the tag counts of a CDT matrix file.
 *
 * @author William KM Lai
 * @see scriptmanager.cli.Coordinate_Manipulation.BED_Manipulation.SortBEDCLI
 * @see scriptmanager.window_interface.Coordinate_Manipulation.BED_Manipulation.SortBEDWindow
 */
public class SortBED {
	/**
	 * Sort a BED file by the values from a CDT matrix file. Includes Gzip support.
	 * 
	 * @param bed input BED file to sort
	 * @param cdt input CDT file with values to sort by
	 * @param outbase Filepath basename (without ext) to save the sorted BED (&lt;basename&gt;.bed) and sorted CDT (&lt;basename&gt;.cdt) files.
	 * @param START_INDEX the start column to consider when summing values to sort
	 * @param STOP_INDEX
	 * @param gzOutput if true, the output files will be gzipped.
	 * @throws IOException Invalid file or parameters
	 */
	public static void sortBEDbyCDT(File bed, File cdt, File outbase, int START_INDEX, int STOP_INDEX, boolean gzOutput ) throws IOException {
		ArrayList<BEDCoord> SORT = new ArrayList<BEDCoord>();
		HashMap<String, String> CDTFile = new HashMap<String, String>();
		String CDTHeader = "";
		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(cdt);
		// Initialize line variable to loop through
		String line = br.readLine();
		while (line != null) {
			String[] ID = line.split("\t");
			if (!ID[0].contains("YORF") && !ID[0].contains("NAME")) {
				double count = 0;
				for (int x = 2 + START_INDEX; x < STOP_INDEX + 2; x++) {
					count += Double.parseDouble(ID[x]);
				}
				SORT.add(new BEDCoord(ID[0], count));
				CDTFile.put(ID[0], line);
			} else {
				CDTHeader = line;
			}
			line = br.readLine();
		}
		br.close();
		// Sort by score
		Collections.sort(SORT, BEDCoord.ScoreComparator);

		PrintStream OUT;
		// Initialize output writer
		String suffix = ".cdt" + (gzOutput? ".gz": "");
		OUT = GZipUtilities.makePrintStream(new File(outbase.getCanonicalPath() + suffix), gzOutput);
		// Output sorted CDT File
		OUT.println(CDTHeader);
		for (int x = 0; x < SORT.size(); x++) {
			OUT.println(CDTFile.get(SORT.get(x).getName()));
		}
		OUT.close();
		CDTFile = null; // Free up memory by getting CDT file out of memory

		// Match to bed file after
		HashMap<String, String> BEDFile = new HashMap<String, String>();

		// Check if file is gzipped and instantiate appropriate BufferedReader
		br = GZipUtilities.makeReader(bed);
		// Initialize line variable to loop through
		line = br.readLine();
		while (line != null) {
			String ID = line.split("\t")[3];
			if (!ID.contains("YORF") && !ID.contains("NAME")) {
				BEDFile.put(ID, line);
			}
			line = br.readLine();
		}
		br.close();

		// Initialize output writer
		suffix = ".bed" + (gzOutput? ".gz": "");
		OUT = GZipUtilities.makePrintStream(new File(outbase.getCanonicalPath() + suffix), gzOutput);
		// Output sorted BED File
		for (int x = 0; x < SORT.size(); x++) {
			OUT.println(BEDFile.get(SORT.get(x).getName()));
		}
		OUT.close();
	}
}