package scriptmanager.cli.Sequence_Analysis;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FASTAUtilities;
import scriptmanager.scripts.Sequence_Analysis.SearchMotif;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Sequence_Analysis.SearchMotif}
 * 
 * @author Olivia Lang
 */
@Command(name = "search-motif", mixinStandardHelpOptions = true,
	description = ToolDescriptions.search_motif_description,
	version = "ScriptManager " + ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class SearchMotifCLI implements Callable<Integer> {

	@Parameters(index = "0", description = "The FASTA file in which to search for the motif.")
	private File fastaFile;

	@Option(names = { "-o", "--output" }, description = "Specify output filename (default = <motif>_<num>Mismatch_<fastaFilename>.bed)")
	private File output = null;
	@Option(names = { "-z", "--gzip" }, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	@Option(names = { "-m", "--motif" }, required = true, description = "the IUPAC motif to search for")
	private String motif;
	@Option(names = { "-n", "--mismatches" }, description = "the number of mismatches allowed (default=0)")
	private int ALLOWED_MISMATCH = 0;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">SearchMotifCLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		SearchMotif script_obj = new SearchMotif(fastaFile, motif, ALLOWED_MISMATCH, output, System.err, gzOutput);
		script_obj.run();

		System.err.println("Search Complete.");
		return (0);
	}

	/**
	 * Validate the input values before executing the script
	 * 
	 * @return a multi-line string describing input validation issues
	 * @throws IOException Invalid file or parameters
	 */
	private String validateInput() throws IOException {
		String r = "";

		// check inputs exist
		if (!fastaFile.exists()) {
			r += "(!)FASTA file does not exist: " + fastaFile.getName() + "\n";
			return (r);
		}
		// set default output filename
		if (output == null) {
			String NAME = motif + "_" + Integer.toString(ALLOWED_MISMATCH) + "Mismatch_"
					+ ExtensionFileFilter.stripExtension(fastaFile) + ".bed"
					+ (gzOutput ? ".gz" : "");
			output = new File(NAME);
		// check output filename is valid
		} else {
			// check directory
			if (output.getParent() == null) {
// 				System.err.println("default to current directory");
			} else if (!new File(output.getParent()).exists()) {
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}

		// check filter string is valid IUPAC
		if (!FASTAUtilities.isValidIUPACString(motif)) {
			r += "(!)Motif string must be formatted as an IUPAC sequence.\n" + motif
					+ " is not a valid nucleotide sequence.\nExpected input string format: \"[ATGCRYSWKMBDHVN]+\"";
		}

		// check mismatch value
		if (ALLOWED_MISMATCH < 0) {
			r += "(!)Please use a non-negative integer for allowed mismatches.";
		}

		return (r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input    the FASTA sequence (often genomic) to look for motifs in
	 * @param output   the coordinates of motif instances found
	 * @param motif    the IUPAC formated motif to search for
	 * @param mismatch the number of allowed mismatches in motif search
	 * @param gzOutput whether or not to output the coordinate file gzip compressed
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input, File output, String motif, int mismatch, boolean gzOutput) {
		String command = "java -jar $SCRIPTMANAGER sequence-analysis search-motif";
		command += " " + input.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();
		command += " -m " + motif;
		command += " -n " + mismatch;
		command += gzOutput ? " -z" : "";
		return command;
	}
}