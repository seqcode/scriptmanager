package scriptmanager.cli.File_Utilities;

import picocli.CommandLine.Command;

import java.io.File;
import java.util.concurrent.Callable;

import scriptmanager.objects.ToolDescriptions;

/**
 * Print a message redirecting user to the original CLI tool.
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.File_Utilities.GZipFiles
 */
@Command(name = "gzip-compress", mixinStandardHelpOptions = true,
	description = ToolDescriptions.compressFileDescription + "\n"+
		"@|bold **Please use the command line gzip this job**|@ \n"+
		"@|bold,yellow 'gzip -k infile.txt'|@",
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class CompressFileCLI implements Callable<Integer> {
	@Override
	public Integer call() throws Exception {
		System.err.println("***Please use the command line for gzip this job***\n"+
							"\t'gzip -k infile.txt'" );
		System.exit(1);
		return(1);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input the file to gzip compress
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input) {
		String command = "gzip -k";
		command += " " + input.getAbsolutePath();
		return(command);
	}
}
