package scriptmanager.scripts.BAM_Manipulation;

import htsjdk.samtools.SAMException;
import picard.sam.FilterSamReads;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

import org.broadinstitute.barclay.argparser.CommandLineParser;

/**
 * @author Erik Pavloski
 * This code runs the FilterSamReads Picard tool
 * @see scriptmanager.window_interface.BAM_Manipulation.FilterSamReadsWindow
 */
public class FilterSamReadsWrapper {
    /**
     * @param input the file to be filtered
     * @param output the file to be outputted
     * @param filter which filter the user wants to use can be includeReadList or includePairIntervals
     *               true == includeReadList
     *               false == includePairIntervals
     * @param readListFile the txt file output for the read list if that is the chosen filter
     * @param intervalList the list of interval output file if that is the desired filter
     *
     * @throws IOException
     * @throws SAMException
     */
    public static void run(File input, File output, boolean filter, File readListFile, File intervalList) throws IOException, SAMException {
        System.out.println("Filtering Reads");

        final picard.sam.FilterSamReads filterSamReads = new FilterSamReads();
        final ArrayList<String> args = new ArrayList<>();
        args.add("INPUT=" + input.getAbsolutePath());
        args.add("OUTPUT=" + output.getAbsolutePath());
        if (filter) {
            args.add("READ_LIST_FILE=" + readListFile.getAbsolutePath());
            args.add("FILTER=includeReadList");
        } else {
            args.add("INTERVAL_LIST=" + intervalList.getAbsolutePath());
            args.add("FILTER=includePairedIntervals");
        }
        filterSamReads.instanceMain(args.toArray(new String[args.size()]));

        System.out.println("Filtering Complete");
    }

    /**
	 * Reconstruct CLI command
     * 
     * @param input the file to be filtered
     * @param output the file to be outputted
     * @param filter which filter the user wants to use can be includeReadList or includePairIntervals
     *               true == includeReadList
     *               false == includePairIntervals
     * @param readListFile the txt file output for the read list if that is the chosen filter
     * @param intervalList the list of interval output file if that is the desired filter
     */
    public static String getCLIcommand(File input, File output, boolean filter, File readListFile, File intervalList) {
        String command = "java -jar $PICARD ";
        final CommandLineParser parser = new picard.sam.FilterSamReads().getCommandLineParser();
        final ArrayList<String> args = new ArrayList<>();
        args.add("INPUT=" + input.getAbsolutePath());
        args.add("OUTPUT=" + output.getAbsolutePath());
        if (filter) {
            args.add("READ_LIST_FILE=" + readListFile.getAbsolutePath());
            args.add("FILTER=includeReadList");
        } else {
            args.add("INTERVAL_LIST=" + intervalList.getAbsolutePath());
            args.add("FILTER=includePairedIntervals");
        }
        String[] argv = args.toArray(new String[args.size()]);
        parser.parseArguments(System.err, argv);
        command += parser.getCommandLine();
        return command;
    }
}
