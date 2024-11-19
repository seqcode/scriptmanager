package scriptmanager.scripts.BAM_Manipulation;

import picard.sam.BuildBamIndex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Picard wrapper for BuildBamIndex
 * 
 * @author Erik Pavloski
 * @see scriptmanager.window_interface.BAM_Manipulation.BAIIndexerWindow
 */
public class BAIIndexer {
	/**
	 * Creates a new BAIIndexer object (unnecessary becuase this class is a collection of static methods)
	 */
	public BAIIndexer(){}
	/**
	 * Index a BAM file and output said index to a file of the same name with a .bai
	 * extension
	 * 
	 * @param input the BAM file to index
	 * @return the BAM index file (.bai)
	 * @throws IOException Invalid file or parameters
	 */
	public static File generateIndex(File input) throws IOException {
		// Tells user that their file is being generated
		System.out.println("Generating Index File...");
		// Build output filepath
		String output = input.getCanonicalPath() + ".bai";
		File retVal = new File(output);
		// Instatiate Picard object
		final BuildBamIndex buildBamIndex = new BuildBamIndex();
		// Build input argument string
		final ArrayList<String> args = new ArrayList<>();
		args.add("INPUT=" + input.getAbsolutePath());
		args.add("OUTPUT=" + retVal.getAbsolutePath());
		// Call Picard with args
		buildBamIndex.instanceMain(args.toArray(new String[args.size()]));

		System.out.println("Index File Generated");
		return retVal;
	}
}
