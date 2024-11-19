package scriptmanager.scripts.Sequence_Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import scriptmanager.util.GZipUtilities;

/**
 * Randomize a FASTA sequence file by shuffling the nucleotides within each
 * FASTA sequence.
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Sequence_Analysis.RandomizeFASTACLI
 * @see scriptmanager.window_interface.Sequence_Analysis.RandomizeFASTAWindow
 */
public class RandomizeFASTA {

	/**
	 * Static method to call for randomizing FASTA sequences.
	 * 
	 * @param input  filepath to FASTA-formatted sequences to randomize
	 * @param output filepath to write randomized sequences to
	 * @param seed   set a random seed
	 * @param gzOutput Whether to output a compressed file
	 * @return name of output filename
	 * @throws IOException Invalid file or parameters
	 */
	public static File randomizeFASTA(File input, File output, Integer seed, boolean gzOutput) throws IOException {
		// Set seed if specified
		Random randnum = new Random();
		if (seed != null) {
			System.err.println("Set Seed=" + seed);
			randnum.setSeed(seed);
		}
		// Initialize output stream
		PrintStream OUT = GZipUtilities.makePrintStream(output, gzOutput);

		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(input);
		String line;
		while ((line = br.readLine()) != null) {
			String HEADER = line;
			OUT.println(HEADER);
			if (HEADER.contains(">")) {
				String[] SEQ = br.readLine().split("");
				ArrayList<String> SEQ_ARRAY = new ArrayList<String>();
				for (int x = 0; x < SEQ.length; x++) {
					SEQ_ARRAY.add(SEQ[x]);
				}
				for (int x = 0; x < SEQ.length; x++) {
					int randIndex = randnum.nextInt(SEQ_ARRAY.size());
					OUT.print(SEQ_ARRAY.get(randIndex));
					SEQ_ARRAY.remove(randIndex);
				}
				OUT.println();
			} else {
				OUT.println("ERROR - NOT FASTA FORMAT");
				System.err.println("ERROR - NOT FASTA FORMAT");
			}
		}

		OUT.close();
		br.close();
		return output;
	}
	
}