package scriptmanager.scripts.BAM_Manipulation;

import htsjdk.samtools.SAMException;
import picard.reference.NormalizeFasta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.broadinstitute.barclay.argparser.CommandLineParser;

/**
 * @author Erik Pavloski
 * This is the Wrapper class for the NormalizeFasta Picard tool
 * @see scriptmanager.window_interface.BAM_Manipulation.NormalizeFastaWindow
 */
public class NormalizeFastaWrapper {
    /**
     * @param input the file to be filtered
     * @param output the file to be outputted
     *
     * @throws IOException
     * @throws SAMException
     */
    public static void run(File input, File output) throws IOException, SAMException {
        System.out.println("Normalizing FASTA file");

        final picard.reference.NormalizeFasta normalizeFasta = new picard.reference.NormalizeFasta();
        final ArrayList<String> args = new ArrayList<>();
        args.add("INPUT=" + input.getAbsolutePath());
        args.add("OUTPUT=" + output.getAbsolutePath());
        normalizeFasta.instanceMain(args.toArray(new String[args.size()]));

        System.out.println("File Normalized");
    }

    /**
	 * Reconstruct CLI command
     * 
     * @param input the file to be filtered
     * @param output the file to be outputted
     */
    public static String getCLIcommand(File input, File output) {
        String command = "java -jar $PICARD ";
        final CommandLineParser parser = new picard.reference.NormalizeFasta().getCommandLineParser();
        final ArrayList<String> args = new ArrayList<>();
        args.add("INPUT=" + input.getAbsolutePath());
        args.add("OUTPUT=" + output.getAbsolutePath());
        String[] argv = args.toArray(new String[args.size()]);
        parser.parseArguments(System.err, argv);
        command += parser.getCommandLine();
        return command;
    }
}
