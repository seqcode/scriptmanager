package scriptmanager.scripts.BAM_Statistics;
import htsjdk.samtools.SAMException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.broadinstitute.barclay.argparser.CommandLineParser;

/**
 * @author Erik Pavloski
 * @see scriptmanager.window_interface.BAM_Statistics.CollectBaseDistributionByCycleWindow
 * This code runs the Picard tool called CollectBaseDistributionByCycle
 */

public class CollectBaseDistributionByCycleWrapper {
    /**
     * @param input the bam file to be analysed
     * @param output the output text file
     * @param chartOutput the pdf chart output
     *
     * @throws IOException
     * @throws SAMException
     */
    public static void run(File input, File output, File chartOutput) throws IOException, SAMException {
        System.out.println("Analysing file...");

        // Analysis of the file
        final picard.analysis.CollectBaseDistributionByCycle collectBaseDistributionByCycle = new picard.analysis.CollectBaseDistributionByCycle();
        final ArrayList<String> args = new ArrayList<>();
        args.add("INPUT=" + input.getAbsolutePath());
        args.add("OUTPUT=" + output.getAbsolutePath());
        args.add("CHART_OUTPUT=" + chartOutput.getAbsolutePath());
        collectBaseDistributionByCycle.instanceMain(args.toArray(new String[args.size()]));
        System.out.println("Analysis complete");
    }

    /**
	 * Reconstruct CLI command
     * 
     * @param input the bam file to be analysed
     * @param output the output text file
     * @param chartOutput the pdf chart output
     */
    public static String getCLIcommand(File input, File output, File chartOutput) {
        String command = "java -jar $PICARD ";
        final CommandLineParser parser = new picard.reference.NormalizeFasta().getCommandLineParser();
        final ArrayList<String> args = new ArrayList<>();
        args.add("INPUT=" + input.getAbsolutePath());
        args.add("OUTPUT=" + output.getAbsolutePath());
        args.add("CHART_OUTPUT=" + chartOutput.getAbsolutePath());
        String[] argv = args.toArray(new String[args.size()]);
        parser.parseArguments(System.err, argv);
        command += parser.getCommandLine();
        return command;
    }
}
