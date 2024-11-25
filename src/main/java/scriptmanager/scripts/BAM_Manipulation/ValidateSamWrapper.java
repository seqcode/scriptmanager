package scriptmanager.scripts.BAM_Manipulation;
import htsjdk.samtools.SAMException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.broadinstitute.barclay.argparser.CommandLineParser;

/**
 * @author Erik Pavloski
 * @see scriptmanager.window_interface.BAM_Manipulation.ValidateSamWindow
 * This code runs the picard tool validateSAMFile
 *
 */


public class ValidateSamWrapper {
    /**
     *
     * @param input The BAM/SAM file to be Validated
     * @param output the output of the validation
     * @param mode Allows the user to select verbose or summary mode. True = verbose - false = summary
     * @param referenceGenome Allows the user to add reference sequence if needed
     * @param maxOutput Allows customization of the maximum number of outputs in verbose mode
     *
     * @throws IOException
     * @throws SAMException
     *
     */
    public static void run(File input, File output, boolean mode, File referenceGenome, int maxOutput) throws IOException, SAMException{

        System.out.println("Validating SAM/BAM file...");
            // Validates the SAM/BAM file
            final picard.sam.ValidateSamFile validateSam = new picard.sam.ValidateSamFile();
            final ArrayList<String> args = new ArrayList<>();
            args.add("INPUT=" + input.getAbsolutePath());
            args.add("OUTPUT=" + output.getAbsolutePath());
            String modeString = mode ? "VERBOSE" : "SUMMARY";
            args.add("MODE=" + modeString);
            args.add("MAX_OUTPUT=" + maxOutput);
            if (referenceGenome != null) {
                args.add("REFERENCE_SEQUENCE=" + referenceGenome.getAbsolutePath());
            }
            validateSam.instanceMain(args.toArray(new String[args.size()]));
            System.out.println("SAM/BAM file validated");
    }

    /**
	 * Reconstruct CLI command
     * 
     * @param input The BAM/SAM file to be Validated
     * @param output the output of the validation
     * @param mode Allows the user to select verbose or summary mode. True = verbose - false = summary
     * @param referenceGenome Allows the user to add reference sequence if needed
     * @param maxOutput Allows customization of the maximum number of outputs in verbose mode
     */
    public static String getCLIcommand(File input, File output, boolean mode, File referenceGenome, int maxOutput) {
        String command = "java -jar $PICARD ";
        final CommandLineParser parser = new picard.sam.ValidateSamFile().getCommandLineParser();
        final ArrayList<String> args = new ArrayList<>();
        args.add("INPUT=" + input.getAbsolutePath());
        args.add("OUTPUT=" + output.getAbsolutePath());
        String modeString = mode ? "VERBOSE" : "SUMMARY";
        args.add("MODE=" + modeString);
        args.add("MAX_OUTPUT=" + maxOutput);
        if (referenceGenome != null) {
            args.add("REFERENCE_SEQUENCE=" + referenceGenome.getAbsolutePath());
        }
        String[] argv = args.toArray(new String[args.size()]);
        parser.parseArguments(System.err, argv);
        command += parser.getCommandLine();
        return command;
    }
}
