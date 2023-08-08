package scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import scriptmanager.util.GZipUtilities;


public class GFFtoBED {
	public static void convertGFFtoBED(File out_filepath, File input) throws IOException {
		// GFF: chr22 TeleGene enhancer 10000000 10001000 500 + . touch1
		// BED: chr12 605113 605120 region_0 0 +

		PrintStream OUT = System.out;
		if (out_filepath != null)
			OUT = new PrintStream(out_filepath);

		// Checks if file is gzipped and instantiate appropriate BufferedReader
		String line;
		BufferedReader br = GZipUtilities.makeReader(input);
		while ((line = br.readLine()) != null) {
			String[] temp = line.split("\t");
			if (temp[0].toLowerCase().contains("track") || temp[0].startsWith("#")) {
				OUT.println(String.join("\t", temp));
			} else {
				if (temp.length == 9) {
					String name = temp[8];
					String score = temp[5]; // Get or make direction
					String dir = temp[6];

					// Make sure coordinate start is >= 0
					if (Integer.parseInt(temp[3]) >= 1) {
						int newstart = Integer.parseInt(temp[3]) - 1;
						OUT.println(
								temp[0] + "\t" + newstart + "\t" + temp[4] + "\t" + name + "\t" + score + "\t" + dir);
					} else {
						System.out.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
					}
				} else {
					System.out.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
				}
			}
		}
		br.close();
		OUT.close();
	}
}
