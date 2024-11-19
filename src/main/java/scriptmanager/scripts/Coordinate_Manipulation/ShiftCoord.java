package scriptmanager.scripts.Coordinate_Manipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import scriptmanager.util.GZipUtilities;

/**
 * Shift coordinate intervals (BED/GFF) by a user-defined direction and distance.
 * 
 * @author Olivia Lang
 * @see scriptmanager.cli.Coordinate_Manipulation.ShiftCoordCLI
 * @see scriptmanager.window_interface.Coordinate_Manipulation.ShiftIntervalWindow
 */
public class ShiftCoord {

	/**
	 * Shift BED-formatted intervals by a user-defined direction and distance. Includes Gzip support.
	 *
	 * @param input Filepath to starting BED-formatted coordinates we want to shift. Supports automatic detection and handling of GZipped BED-formatted files. Must have at least 3 tab-delimited columns per BED specifications.
	 * @param out_filepath Filepath to save shifted BED-formatted files. If null, outputs to STDOUT.
	 * @param SHIFT Integer value indicating number and direction of nucleotides to shift the entire intervals (negative values are upstream shifts while positive values are downstream shifts)
	 * @param stranded If this is true, then the stranded-ness of features is taken into account for the directionality of the shift. Otherwise all upstream shifts move intervals to the left and all downstream shifts move to the right with respect to the reference.
	 * @param gzOutput If this is true, the output file will be gzipped.
	 * @throws IOException Invalid file or parameters
	 */
	public static void shiftBEDInterval(File input, File out_filepath, int SHIFT, boolean stranded, boolean gzOutput) throws IOException {
		// Initialize output writer
		PrintStream OUT = System.out;
		if (out_filepath != null) {
			OUT = GZipUtilities.makePrintStream(out_filepath, gzOutput);
		}
		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(input);
		// Initialize line variable to loop through
		String line = br.readLine();
		while (line != null) {
			// Split into tokens by tab delimiter
			String[] temp = line.split("\t");
			// Ignore headers
			if (temp[0].toLowerCase().contains("track") || temp[0].startsWith("#")) {
				OUT.println(String.join("\t", temp));
			} else {
				// Skip lines with 2 or fewer columns
				if (temp.length > 2) {
					// Ensure that the coordinate is at least minimum possible value (BED is 0-indexed)
					if (Integer.parseInt(temp[1]) >= 0) {
						// Default to shift right
						int newstart = Integer.parseInt(temp[1]) + SHIFT;
						int newstop = Integer.parseInt(temp[2]) + SHIFT;
						// Shift reverse if strand negative
						if (temp.length > 5) {
							if (temp[5].equals("-") && stranded) {
								newstart = Integer.parseInt(temp[1]) - SHIFT;
								newstop = Integer.parseInt(temp[2]) - SHIFT;
							}
						}
						// Write new coordinate info
						OUT.print(temp[0] + "\t" + newstart + "\t" + newstop);
						// Copy remaining columns
						for (int x = 3; x < temp.length; x++) {
							OUT.print("\t" + temp[x]);
						}
						OUT.println();
					} else {
						System.out.println("Invalid Coordinate in File!!! (coordinate must be >= 0)\n" + Arrays.toString(temp));
					}
				} else {
					System.out.println("Invalid Coordinate in File!!! (must have at least 3 columns)\n" + Arrays.toString(temp));
				}
			}
			line = br.readLine();
		}
		// Close files
		br.close();
		OUT.close();
	}

	/**
	 * Shift GFF-formatted intervals by a user-defined direction and distance. Includes Gzip support.
	 *
	 * @param out_filepath Filepath to save shifted GFF-formatted files. If null, outputs to STDOUT.
	 * @param input Filepath to starting GFF-formatted coordinates we want to shift. Supports automatic detection and handling of GZipped GFF-formatted files. Must have exactly 9 tab-delimited columns per GFF specifications.
	 * @param SHIFT Integer value indicating number and direction of nucleotides to shift the entire intervals (negative values are upstream shifts while positive values are downstream shifts)
	 * @param stranded If this is true, then the stranded-ness of features is taken into account for the directionality of the shift. Otherwise all upstream shifts move intervals to the left and all downstream shifts move to the right with respect to the reference.
	 * @param gzOutput If this is true, the output file will be gzipped.
	 * @throws IOException Invalid file or parameters
	 */
	public static void shiftGFFInterval(File out_filepath, File input, int SHIFT, boolean stranded, boolean gzOutput) throws IOException {
		// Initialize output writer
		PrintStream OUT = System.out;
		if (out_filepath != null) {
			OUT  = GZipUtilities.makePrintStream(out_filepath, gzOutput);
		}
		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(input);
		// Initialize line variable to loop through
		String line = br.readLine();
		while (line != null) {
			// Split into tokens by tab delimiter
			String[] temp = line.split("\t");
			// Ignore headers
			if (temp[0].toLowerCase().contains("track") || temp[0].startsWith("#")) {
				OUT.println(String.join("\t", temp));
			} else {
				// Ensure there are exactly 9 columns (GFF format)
				if (temp.length == 9) {
					// Ensure that the coordinate is at least minimum possible value (GFF is 1-indexed)
					if (Integer.parseInt(temp[3]) >= 1) {
						// Default to shift right
						int newstart = Integer.parseInt(temp[3]) + SHIFT;
						int newstop = Integer.parseInt(temp[4]) + SHIFT;
						// Shift reverse if strand negative
						if (temp[6].equals("-") && stranded) {
							newstart = Integer.parseInt(temp[3]) - SHIFT;
							newstop = Integer.parseInt(temp[4]) - SHIFT;
						}
						// Write new coordinate info
						OUT.print(temp[0] + "\t" + temp[1] + "\t" + temp[2] + "\t" + newstart + "\t" + newstop);
						// Copy remaining columns
						for (int x = 5; x < temp.length; x++) {
							OUT.print("\t" + temp[x]);
						}
						OUT.println();
					} else {
						System.out.println("Invalid Coordinate in File!!! (coordinate must be >= 1)\n" + Arrays.toString(temp));
					}
				} else {
					System.out.println("Invalid Coordinate in File!!! (must have 9 columns)\n" + Arrays.toString(temp));
				}
			}
			line = br.readLine();
		}
		// Close files
		br.close();
		OUT.close();
	}
}
