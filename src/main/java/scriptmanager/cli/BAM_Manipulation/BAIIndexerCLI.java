package scriptmanager.cli.BAM_Manipulation;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;
import java.io.File;

import scriptmanager.objects.ToolDescriptions;

/**
 * Print a message redirecting user to the original CLI tool.
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
	@Override
	public Integer call() throws Exception {
		System.err.println("***Please use the original tool for this job***\n"+
							"\t'samtools index <bam-file>'");
		System.exit(1);
		return(1);
	}

	public static String getCLIcommand(File BAM) {
		String command = "java -jar $PICARD BuildBamIndex";
		command += "INPUT=" + BAM.getAbsolutePath();
		command += "OUTPUT=" + BAM.getAbsolutePath() + ".bai";
		return command;
	}

}