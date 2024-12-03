package scriptmanager.scripts.BAM_Manipulation;

import htsjdk.samtools.BamFileIoUtils;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMProgramRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import picard.cmdline.CommandLineSyntaxTranslater;
import picard.sam.AddCommentsToBam;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.BAMUtilities;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;

import org.broadinstitute.barclay.argparser.CommandLineParser;

/**
 * @author Erik Pavloski
 * This is the Wrapper class for the AddCommentsToBam Picard tool
 * @see scriptmanager.window_interface.BAM_Manipulation.AddCommentsToBamWindow
 */
public class AddCommentsToBamWrapper {
    /**
     * @param input the bam file to add comments to
     * @param output the output file
     *
     * @throws IOException
     * @throws SAMException
     */
    public static void run(File input, File output, ArrayList<String> comments) throws IOException, SAMException {
        System.out.println("Add Comments To Bam");

        final picard.sam.AddCommentsToBam addComments = new picard.sam.AddCommentsToBam();
        final ArrayList<String> args = new ArrayList<>();
        args.add("INPUT=" + input.getAbsolutePath());
        args.add("OUTPUT=" + output.getAbsolutePath());
        for (String c: comments){
            args.add("C=" + c);
        }
        addComments.instanceMain(args.toArray(new String[args.size()]));

        // Copy output
        File temp  = new File(output.toPath() + "copy");
        Files.copy(output.toPath(), new PrintStream(temp));
        // Create and add new record
		SamReader reader = SamReaderFactory.makeDefault().open(temp);
        String command = AddCommentsToBamWrapper.getCLIcommand(input, output, comments);
        SAMProgramRecord newRecord = BAMUtilities.getPGRecord(reader.getFileHeader(), addComments.getClass().getSimpleName(),  command, addComments.getVersion());
        reader.getFileHeader().addProgramRecord(newRecord);
		BamFileIoUtils.reheaderBamFile(reader.getFileHeader(), temp, output, false, false);
        // Delete copy
        temp.delete();
        System.out.println("SAM/BAM file downsampled");
        
        System.out.println("Comments Added");
    }

    /**
	 * Reconstruct CLI command
     * 
     * @param input the bam file to add comments to
     * @param output the output file
     */
    public static String getCLIcommand(File input, File output, ArrayList<String> comments) {
        String command = "java -jar $PICARD ";
        final CommandLineParser parser = new picard.sam.AddCommentsToBam().getCommandLineParser();
        final ArrayList<String> args = new ArrayList<>();
        args.add("INPUT=" + input.getAbsolutePath());
        args.add("OUTPUT=" + output.getAbsolutePath());
        for (String c: comments){
            args.add("C=" + c);
        }
        String[] argv = args.toArray(new String[args.size()]);
        parser.parseArguments(System.err, argv);
        command += parser.getCommandLine();
        return command;
    }
}
