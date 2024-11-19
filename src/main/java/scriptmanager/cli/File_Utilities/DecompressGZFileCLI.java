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
@Command(name = "gzip-decompress", mixinStandardHelpOptions = true,
	description = ToolDescriptions.decompressFileDescription + "\n"+
		"@|bold **Please use the command line gzip this job**|@ \n"+
		"@|bold,yellow 'gzip -dk infile.txt.txt'|@",
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class DecompressGZFileCLI implements Callable<Integer> {
	@Override
	public Integer call() throws Exception {
		System.err.println("***Please use the command line gzip for this job***\n"+
							"\t'gzip -dk infile.txt'" );
		System.exit(1);
		return(1);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input the file to gzip decompress
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input) {
		String command = "gzip -dk";
		command += " " + input.getAbsolutePath();
		return(command);
	}
}
