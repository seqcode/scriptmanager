package scriptmanager.cli.Figure_Generation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.lang.NullPointerException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Figure_Generation.MergeHeatMapPlot;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Figure_Generation.MergeHeatMapPlot}
 * 
 * @author Olivia Lang
 */
@Command(name = "merge-heatmap",
	mixinStandardHelpOptions = true,
	description = ToolDescriptions.merge_heatmap_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class MergeHeatMapCLI implements Callable<Integer> {

	/**
	 * Creates a new MergeHeatMapCLI object
	 */
	public MergeHeatMapCLI(){}

	@Parameters(index = "0", description = "First(sense) PNG heatmap to merge, input1")
	private File senseFile;
	@Parameters(index = "1", description = "Second(anti) PNG heatmap to merge, input2")
	private File antiFile;

	@Option(names = { "-o", "--output" }, description = "specify output filename, please use PNG extension\n(default=\"<senseFile>_merged.png\" appended to the name in working directory of ScriptManager")
	private File output = null;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">MergeHeatMapCLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		// Generate Merged HeatMap
		MergeHeatMapPlot.mergePNG(senseFile, antiFile, output);

		System.err.println("Image Generated.");
		return (0);
	}

	private String validateInput() throws IOException {
		String r = "";

		// check inputs exist
		if (!senseFile.exists()) {
			r += "(!)INPUT1 file does not exist: " + senseFile.getName() + "\n";
		}
		if (!antiFile.exists()) {
			r += "(!)INPUT2 file does not exist: " + antiFile.getName() + "\n";
		}
		if (!r.equals("")) {
			return (r);
		}
		// set default output filename
		if (output == null) {
			String NAME = ExtensionFileFilter.stripExtension(senseFile);
			output = new File(NAME + "_merged.png");
			// check output filename is valid
		} else {
			// check directory
			if (output.getParent() == null) {
// 				System.err.println("default to current directory");
			} else if (!new File(output.getParent()).exists()) {
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}

		return (r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input1 first input PNG to merge (sense)
	 * @param input2 second input PNG to merge (anti)
	 * @param output the merged output PNG file
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input1, File input2, File output) {
		String command = "java -jar $SCRIPTMANAGER figure-generation merge-heatmap";
		command += " " + input1.getAbsolutePath();
		command += " " + input2.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();
		return command;
	}

}