package scriptmanager.cli.File_Utilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;
import java.io.File;
import java.io.PrintStream;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.scripts.File_Utilities.MD5Checksum;

/**
 * Prints a message redirecting user to the original CLI tool.
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.File_Utilities.MD5Checksum
 * @see scriptmanager.window_interface.File_Utilities.MD5ChecksumWindow
 */
@Command(name = "md5checksum", mixinStandardHelpOptions = true,
	description = ToolDescriptions.md5checksum_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class MD5ChecksumCLI implements Callable<Integer> {

	/**
	 * Creates a new MD5ChecksumCLI object
	 */
	public MD5ChecksumCLI(){}
	
	@Parameters( index = "0", description = "The file we want to calculate the MD5checksum for. Alternatively use md5 <file> or md5checksum <file>")
	private File input;

	@Option(names = {"-o", "--output"}, description = "specify output filepath")
	private File output = null;
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws Exception Invalid input
	 */
	@Override
	public Integer call() throws Exception {
		String md5hash = MD5Checksum.calculateMD5(input.getAbsolutePath());
		PrintStream OUT = System.out;
		if (output != null) {
			OUT = new PrintStream( output );
		}
		OUT.println("MD5 (" + input.getName() + ") = " + md5hash);
		OUT.close();
		return(0);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input the file to get checksums from
	 * @param output the file checksums are written to
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input, File output) {
		String command = "java -jar $SCRIPTMANAGER file-utilities md5checksum";
		command += " " + input.getAbsolutePath();
		command += " >> " + output;
		return command;
	}
}