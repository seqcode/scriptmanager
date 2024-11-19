package scriptmanager.cli.Sequence_Analysis;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Sequence_Analysis.RandomizeFASTA;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Sequence_Analysis.RandomizeFASTA}
 * 
 * @author Olivia Lang
 */
@Command(name = "randomize-fasta", mixinStandardHelpOptions = true,
	description = ToolDescriptions.randomize_fasta_description,
	version = "ScriptManager " + ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class RandomizeFASTACLI implements Callable<Integer> {

	@Parameters(index = "0", description = "the FASTA file ")
	private File fastaFile;

	@Option(names = { "-o",
			"--output" }, description = "Specify basename for output files (default = <fastaFilename>_RAND.fa)")
	private File output;
	@Option(names = {"-s", "--seed"}, description = "specify an integer seed for reproducible outputs")
	private Integer seed = null;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">RandomizeFASTACLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		RandomizeFASTA.randomizeFASTA(fastaFile, output, seed, gzOutput);

		System.err.println("Randomization Complete.");
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

		// check input exists
		if (!fastaFile.exists()) {
			r += "(!)FASTA file does not exist: " + fastaFile.getName() + "\n";
			return (r);
		}
		// set default output filename
		if (output == null) {
			output = new File(ExtensionFileFilter.stripExtensionIgnoreGZ(fastaFile) + "_RAND.fa"
					+ (gzOutput ? ".gz" : ""));
			// check output filename is valid
		} else {
			// check directory
			if (output.getParent() == null) {
				// System.err.println("default to current directory");
			} else if (!new File(output.getParent()).exists()) {
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}

		return (r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input  filepath to FASTA-formatted sequences to randomize
	 * @param output filepath to write randomized sequences to
	 * @param seed   set a random seed
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input, File output, Integer seed) {
		String command = "java -jar $SCRIPTMANAGER sequence-analysis randomize-fasta";
		command += " -o " + output.getAbsolutePath();
		command += " " + input.getAbsolutePath();
		return (command);
	}
}