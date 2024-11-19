package scriptmanager.cli.BAM_Manipulation;

import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.io.File;

import scriptmanager.objects.ToolDescriptions;

/**
 * Prints a message redirecting user to the original CLI tool (Picard
 * {@link picard.sam.MergeSamFiles})
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.BAM_Manipulation.MergeBAM
 */
@Command(name = "merge-bam", mixinStandardHelpOptions = true,
	description = ToolDescriptions.merge_bam_description + "\n"+
		"@|bold **Please run the picard tool directly:**|@ \n"+
		"@|bold,yellow 'java -jar picard.jar MergeSamFiles'|@",
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class MergeBAMCLI implements Callable<Integer> {

	/**
	 * Creates a new MergeBAMCLI object
	 */
	public MergeBAMCLI(){}
	
	/**
	 * Runs when this subcommand is called, directing user to original tool
	 * @throws Exception Please use original CLI tool
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println("***Please use the original tool for this job***\n"+
							"\t'java -jar picard.jar MergeSamFiles'");
		System.exit(1);
		return(1);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param inputs          the list of input BAM files to merge (corresponds to
	 *                        several INPUT values)
	 * @param output          the output file for the merged BAM file (corresponds
	 *                        to OUTPUT)
	 * @param useMultipleCpus whether or not to parallelize (corresponds to
	 *                        USE_THREADING)
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(ArrayList<File> inputs, File output, boolean useMultipleCpus) {
		String command = "java -jar $PICARD MergeSamFiles";
		for (File in : inputs) {
			command += " INPUT=" + in.getAbsolutePath();
		}
		command += " OUTPUT=" + output.getAbsolutePath();
		command += useMultipleCpus ? " USE_THREADING=true" : " USE_THREADING=false";
		return command;
	}
}