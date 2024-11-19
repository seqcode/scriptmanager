package scriptmanager.scripts.BAM_Manipulation;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.SAMFileHeader;
import picard.sam.SortSam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Picard wrapper for SortSam
 * 
 * @author Erik Pavloski
 * @see scriptmanager.window_interface.BAM_Manipulation.SortBAMWindow
 */
public class BAMFileSort {

    /**
     * Creates a new BAMFileSort object (unnecessary because class only contains static methods) 
     */
    public BAMFileSort(){}

	/**
	 * The following code uses Picard's SortSam to sort a BAM file by coordinate
	 * 
	 * @param input the BAM file to be sorted (corresponds to INPUT)
	 * @param output the file to write the sorted BAM to (corresponds to OUTPUT)
	 * @throws SAMException Invalid BAM file
	 * @throws IOException Invalid file or parameters
	 */
    public static void sort(File input, File output) throws SAMException, IOException {
        // Tells user their File is being sorted
        System.out.println("Sorting Bam File...");
		// Instatiate Picard object
            final SortSam sorter = new SortSam();
		// Build input argument string
            final ArrayList<String> args = new ArrayList<>();
            args.add("INPUT=" + input.getAbsolutePath());
            args.add("OUTPUT=" + output.getAbsolutePath());
            args.add("SORT_ORDER=" + SAMFileHeader.SortOrder.coordinate);
            // Call Picard with args
            sorter.instanceMain(args.toArray(new String[args.size()]));
    }
}