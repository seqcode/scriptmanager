package scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import scriptmanager.util.GZipUtilities;

/**
 * Expand (GFF) coordinate intervals from the center/border by a user-defined size.
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Coordinate_Manipulation.GFF_Manipulation.ExpandGFFCLI
 * @see scriptmanager.window_interface.Coordinate_Manipulation.GFF_Manipulation.ExpandGFFWindow
 */
public class ExpandGFF {
	/**
	 * Creates a new ExpandGFF object (unnecessary because only contains static methods)
	 */
	public ExpandGFF(){}

	/**
	 * Self-contained method for expanding the GFF-formatted intervals in a GFF file by user-specified distance and strategy. This method accounts for even-sized GFF interval expansion in midpoint calculations by using the strand-aware downstream nucleotide between the two center nucleotides.
	 * 
	 * @param input Filepath to starting GFF-formatted coordinates we want to shift. Supports automatic detection and handling of GZipped GFF-formatted files. Must have at least 3 tab-delimited columns per GFF specifications.
	 * @param out_filepath Filepath to save expanded GFF-formatted files. If null, outputs to STDOUT.
	 * @param SIZE Integer value indicating number of nucleotides to expand by (must be a positive integer).
	 * @param ExCenter Specifies expansion strategy: if true, size expansion will be performed from the midpoint of each GFF interval, if false, size expansion will be performed from the border/edges of the GFF intervals (default=true).
	 * @param gzOutput    whether or not to gzip output
	 * @throws IOException Invalid file or parameters
	 */
	public static void expandGFFBorders(File input, File out_filepath, int SIZE, boolean ExCenter, boolean gzOutput) throws IOException {
		// GFF: chr22 TeleGene enhancer 10000000 10001000 500 + . touch1
		// GFF: chr12 bed2gff chr12_384641_384659_+ 384642 384659 42.6 + .
		// chr12_384641_384659_+;

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
			String[] temp = line.split("\t");
			if (temp[0].toLowerCase().contains("track") || temp[0].startsWith("#")) {
				OUT.println(String.join("\t", temp));
			} else {
				if (temp.length == 9) {
					if (Integer.parseInt(temp[3]) >= 1) {
						// Default to add to border
						int newstart = Integer.parseInt(temp[3]) - SIZE;
						int newstop = Integer.parseInt(temp[4]) + SIZE;
						if (ExCenter) { // Else expand from center
							boolean EVEN = ((Integer.parseInt(temp[4]) - Integer.parseInt(temp[3])) % 2 == 0);
							int CENTER = (int) ((Integer.parseInt(temp[3]) + Integer.parseInt(temp[4])) / 2);
							if (!EVEN || !temp[6].equals("-")) {
								CENTER++;
							}
							newstart = CENTER - (SIZE / 2);
							newstop = CENTER + ((SIZE / 2) - 1);
						}

						OUT.print(temp[0] + "\t" + temp[1] + "\t" + temp[2] + "\t" + newstart + "\t" + newstop);
						for (int x = 5; x < temp.length; x++) {
							OUT.print("\t" + temp[x]);
						}
						OUT.println();
					} else {
						System.out.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
					}
				}
			}
			line = br.readLine();
		}
		br.close();
		OUT.close();
	}
}
