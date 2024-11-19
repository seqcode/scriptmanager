package scriptmanager.scripts.BAM_Manipulation;

import htsjdk.samtools.SAMException;
import picard.sam.MergeSamFiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Picard wrapper for MergeSamFiles
 * 
 * @author Erik Pavloski
 * @see scriptmanager.window_interface.BAM_Manipulation
 */
public class MergeBAM {

	/**
	 * Creates a new MergeBAM object
	 */
	public MergeBAM(){}

	/**
	 * Excecute the Picard MergeSamFiles command line tool after checking ever input file has been indexed.
	 * 
	 * @param inputs the list of input files to merge (corresponds to several INPUT values)
	 * @param output the output file for the merged BAM file (corresponds to OUTPUT)
	 * @param useMultipleCpus whether or not to parallelize (corresponds to USE_THREADING)
	 * @throws SAMException BAM files aren't formatted properly
	 * @throws IOException Invalid file or parameters
	 */
	public static void run(ArrayList<File> inputs, File output, boolean useMultipleCpus) throws SAMException, IOException {
		// Check all BAM files have an index
		for (int x = 0; x<inputs.size(); x++) {
			File f = new File(inputs.get(x) + ".bai");
			if(!f.exists() || f.isDirectory()) {
				throw new SAMException("BAI Index File does not exist for: " + inputs.get(x).getName() + "\n");
			}
		}
		// Tells the user their files are being merged
		System.out.println("Merging BAM Files...");
		// Merges the files
		final MergeSamFiles merger = new MergeSamFiles();
		final ArrayList<String> args = new ArrayList<>();
		for (File input : inputs) {
			args.add("INPUT=" + input.getAbsolutePath());
		}
		args.add("OUTPUT=" + output.getAbsolutePath());
		if (useMultipleCpus) {
			args.add("USE_THREADING=true");
		}
		merger.instanceMain(args.toArray(new String[args.size()]));
		System.out.println("BAM Files Merged.");
	}
}