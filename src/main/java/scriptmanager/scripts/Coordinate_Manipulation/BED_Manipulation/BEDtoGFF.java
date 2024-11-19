package scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import scriptmanager.util.GZipUtilities;

/**
 * Convert a BED-formatted coordinate file to the GFF-format
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Coordinate_Manipulation.BED_Manipulation.BEDtoGFFCLI
 * @see scriptmanager.window_interface.Coordinate_Manipulation.BED_Manipulation.BEDtoGFFWindow
 */
public class BEDtoGFF {

	/**
	 * Read the BED-formatted input file and write it as a GFF-formatted output file.
	 * 
	 * @param input the BED-formatted file to convert
	 * @param outpath the filepath destination of the GFF-formatted output
	 * @param gzOutput If this is true, the output file will be gzipped.
	 * @throws IOException Invalid file or parameters
	 */
	public static void convertBEDtoGFF(File input, File outpath, boolean gzOutput) throws IOException {
		// chr22 TeleGene enhancer 10000000 10001000 500 + . touch1
		// Initialize output writer
		PrintStream OUT = System.out;
		if (outpath != null) {
			OUT = GZipUtilities.makePrintStream(outpath, gzOutput);
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
				if (temp.length > 2) {
					String name = temp[0] + "_" + temp[1] + "_" + temp[2]; // Get or make name from BED file
					if (temp.length > 3) {
						name = temp[3];
					}
					String score = "0"; // Get or make direction
					if (temp.length > 4) {
						score = temp[4];
					}
					String dir = "+";
					if (temp.length > 5) {
						dir = temp[5];
					}

					// Make sure coordinate start is >= 0
					if (Integer.parseInt(temp[1]) >= 0) {
						int newstart = Integer.parseInt(temp[1]) + 1;
						OUT.println(temp[0] + "\tbed2gff\t" + name + "\t" + newstart + "\t" + temp[2] + "\t" + score
								+ "\t" + dir + "\t.\t" + name + ";");
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
