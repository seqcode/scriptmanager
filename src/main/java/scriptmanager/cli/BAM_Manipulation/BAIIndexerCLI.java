package scriptmanager.cli.BAM_Manipulation;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;
import java.io.File;

import scriptmanager.objects.ToolDescriptions;

/**
 * Prints a message redirecting user to the original CLI tool (Picard
 * {@link picard.sam.BuildBamIndex})
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.BAM_Manipulation.BAIIndexer
 */
@Command(name = "bam-indexer", mixinStandardHelpOptions = true,
	description = ToolDescriptions.bam_indexer_description + "\n"+
		"@|bold **Please run the samtools tool directly:**|@ \n"+
		"@|bold,yellow 'samtools index <bam-file>'|@",
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class BAIIndexerCLI implements Callable<Integer> {
	/**
	 * Creates a new BAIIndexerCLI object
	 */
	public BAIIndexerCLI(){}

	/**
	 * Runs when this subcommand is called, directing user to original tool
	 * @throws Exception Please use original CLI tool
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println("***Please use the original tool for this job***\n"+
							"\t'samtools index <bam-file>'");
		System.exit(1);
		return(1);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input the BAM file to index
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input) {
		String command = "java -jar $PICARD BuildBamIndex";
		command += " INPUT=" + input.getAbsolutePath();
		command += " OUTPUT=" + input.getAbsolutePath() + ".bai";
		return command;
	}

}