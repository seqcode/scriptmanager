package scriptmanager.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import scriptmanager.objects.CoordinateObjects.BEDCoord;

/**
 * Class containing a set of shared methods to be used across script classes.
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.Sequence_Analysis.FASTAExtract
 */
public class BEDUtilities {

	/**
	 * Creates a new BEDUtilities object
	 */
	public BEDUtilities(){}
	
	/**
	 * Load a list of BEDCoord objects from a file.
	 * 
	 * @param input  the BED-formatted input file to load
	 * @param HEADER the style of FASTA-header to use for the output (true = BED
	 *               coord name, false = use Genomic Coordinate)
	 * @return Returns a ArraList&lt;BEDCoord&gt; representing the input BED file
	 * @throws IOException Invalid file or parameters 
	 * @throws UnsupportedEncodingException File has unsupported encoding format (see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html">Supported Encodings</a> )
	 */
	public static ArrayList<BEDCoord> loadCoord(File input, boolean HEADER) throws UnsupportedEncodingException, IOException {
		ArrayList<BEDCoord> COORD = new ArrayList<BEDCoord>();
		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(input);
		// Initialize line variable to loop through
		String line = br.readLine();
		while (line != null) {
			String[] temp = line.split("\t");
			if (temp.length > 2) {
				if (!temp[0].contains("track") && !temp[0].contains("#")) {
					String name = "";

					if (!HEADER) { // create genomic coordinate name if requested
						if (temp.length > 5) {
							name = temp[0] + ":" + temp[1] + "-" + temp[2] + "(" + temp[5] + ")";
						} else {
							name = temp[0] + ":" + temp[1] + "-" + temp[2] + "(.)";
						}
					} else { // else create name based on BED file name or create one if non-existent
						if (temp.length > 3) {
							name = temp[3];
						} else {
							name = temp[0] + ":" + temp[1] + "-" + temp[2] + "(" + temp[5] + ")";
						}
					}

					if (Integer.parseInt(temp[1]) >= 0) {
						if (temp[5].equals("+")) {
							COORD.add(new BEDCoord(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), "+",
									name));
						} else {
							COORD.add(new BEDCoord(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), "-",
									name));
						}
					} else {
						System.out.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
					}
				}
			}
			line = br.readLine();
		}
		br.close();
		return COORD;
	}
}
