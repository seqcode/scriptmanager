package scriptmanager.cli.BAM_Manipulation;

import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import scriptmanager.objects.ToolDescriptions;

/**
 * Prints a message redirecting user to the original CLI tool (Picard
 * {@link picard.sam.markduplicates.MarkDuplicates})
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.BAM_Manipulation.BAMMarkDuplicates
 */
@Command(name = "remove-duplicates", mixinStandardHelpOptions = true,
	description = ToolDescriptions.remove_duplicates_description + "\n"+
		"@|bold **Please run the picard/samtools tools directly:**|@ \n"+
		"@|bold,yellow 'java -jar picard.jar MarkDuplicates I=<in.bam> O=<marked.bam>\n"+
		"samtools view -F 1024 <marked.bam> > <out.bam>'|@",
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class BAMRemoveDupCLI implements Callable<Integer> {

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println("***Please use the original tool for this job***\n"+
							"\t'java -jar picard.jar MarkDuplicates I=<in.bam> O=<marked.bam>'\n"+
							"\t'samtools view -F 1024 <marked.bam> > <out.bam>'" );
		System.exit(1);
		return(1);
	}

	/**
	 * Reconstruct CLI command (Picard)
	 * 
	 * @param input            the BAM file to mark/remove duplicates for
	 * @param removeDuplicates whether to remove or just mark duplicates
	 * @param output           the marked/filtered BAM output file
	 * @param metrics          the output metrics file with information about the
	 *                         deduplicates
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input, boolean removeDuplicates, File output, File metrics) {
		String command = "java -jar $PICARD MarkDuplicates";
		command += " INPUT=" + input.getAbsolutePath();
		command += " OUTPUT=" + output.getAbsolutePath();
		command += " METRICS_FILE=" + metrics.getAbsolutePath();
		command += removeDuplicates ? " REMOVE_DUPLICATES=true" : " REMOVE_DUPLICATES=false";
		return(command);
	}
}