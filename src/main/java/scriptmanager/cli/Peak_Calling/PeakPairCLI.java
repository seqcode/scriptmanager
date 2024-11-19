package scriptmanager.cli.Peak_Calling;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;
import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;

/**
 * (Dev) Unfinished command line interface for
 * {@link scriptmanager.scripts.Peak_Calling.PeakPair}
 * 
 * @author Olivia Lang
 */
@Command(name = "peak-pair", mixinStandardHelpOptions = true,
	description = ToolDescriptions.peak_pairing_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class PeakPairCLI implements Callable<Integer> {
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.out.println( ">PeakPairCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		//SEStats.getSEStats( output, bamFile, null );
		
		//System.err.println("Calculations Complete");
		System.err.println("CLI Peak Pair not yet implemented");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		//validate input here
		//append messages to the user to `r`
		return(r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param in
	 * @param mode
	 * @param up
	 * @param down
	 * @param bin
	 * @param abs
	 * @param rel
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File in, int mode, int up, int down, int bin, int abs, int rel) {
		String command = "# (Not yet implemented) java -jar $SCRIPTMANAGER peak-calling peak-pair";
		/* TODO: implement CLI */
		return(command);
	}
}
