package scriptmanager.cli.Read_Analysis;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import scriptmanager.objects.ToolDescriptions;

import java.io.File;
import java.io.IOException;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Read_Analysis.SimilarityMatrix}
 * 
 * @author Olivia Lang
 */
@Command(name = "similarity-matrix", mixinStandardHelpOptions = true,
	description = "",
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class SimilarityMatrixCLI implements Callable<Integer> {
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">SimilarityMatrixCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		//SEStats.getSEStats( output, bamFile, null );
		
		//System.err.println("Calculations Complete");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		//validate input here
		//append messages to the user to `r`
		r += "(!)This tool is deactivated to reflect the GUI.";
		
		return(r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input
	 * @param output
	 * @param index
	 * @param correlateColumns
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input, File output, int index, boolean correlateColumns) {
		String command = "# (Not yet implemented) java -jar $SCRIPTMANAGER read-analysis similarity-matrix";
		/* TODO: implement CLI */
		return(command);
	}
}
