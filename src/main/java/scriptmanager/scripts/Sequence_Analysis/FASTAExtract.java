package scriptmanager.scripts.Sequence_Analysis;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.reference.FastaSequenceIndexCreator;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import scriptmanager.objects.CoordinateObjects.BEDCoord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import scriptmanager.util.FASTAUtilities;
import scriptmanager.util.GZipUtilities;
import scriptmanager.util.BEDUtilities;

/**
 * Retrieve the genomic sequences from the coordinates in a BED file.
 * 
 * @author William KM Lai
 * @see scriptmanager.util.FASTAUtilities
 * @see scriptmanager.cli.Sequence_Analysis.FASTAExtractCLI
 * @see scriptmanager.window_interface.Sequence_Analysis.FASTAExtractOutput
 * @see scriptmanager.window_interface.Sequence_Analysis.FASTAExtractWindow
 */
public class FASTAExtract {
	private File GENOME = null;
	private File OUTFILE = null;
	private File BED = null;
	private PrintStream PS = null;
	private boolean STRAND = true;
	private boolean HEADER = true;
	private boolean gzOutput = false;

	/**
	 * Initialize object with script inputs for extracting genomic sequences.
	 * 
	 * @param gen  the reference genome sequence in FASTA-format (FAI will be
	 *             automatically generated)
	 * @param b    the BED-formatted coordinate intervals to extract sequence from
	 * @param out  the FASTA-formatted subsequences that were extracted from the
	 *             genomic sequence
	 * @param str  force strandedness (true = force, false = don't force)
	 * @param head the style of FASTA-header to use for the output (true = BED coord
	 *             name, false = use Genomic Coordinate)
	 * @param ps   a PrintStream object for printing progress updates (for GUI)
	 * @param gz   If this is true, the output file will be gzipped.
	 * @throws IOException Invalid file or parameters
	 */
	public FASTAExtract(File gen, File b, File out, boolean str, boolean head, PrintStream ps, boolean gz)
			throws IOException {
		GENOME = gen;
		BED = b;
		OUTFILE = out;
		STRAND = str;
		HEADER = head;
		PS = ps;
		gzOutput = gz;

		File FAI = new File(GENOME + ".fai");
		// Check if FAI index file exists
		if (!FAI.exists() || FAI.isDirectory()) {
			FastaSequenceIndexCreator.create(GENOME.toPath(), true);
		}
	}

	/**
	 * Execute script to extract the genomic sequences.
	 * 
	 * @throws IOException Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the same time
	 */
	public void run() throws IOException, InterruptedException {
		PS.println("STRAND:" + STRAND);
		PS.println("COORD:" + HEADER);

		try {
			IndexedFastaSequenceFile QUERY = new IndexedFastaSequenceFile(GENOME);
			PS.println("Proccessing File: " + BED.getName());

			// Initialize output writer
			PrintStream OUT = System.out;
			if (OUTFILE != null) {
				OUT = GZipUtilities.makePrintStream(OUTFILE, gzOutput);
			}

			ArrayList<BEDCoord> BED_Coord = BEDUtilities.loadCoord(BED, HEADER);

			for (int y = 0; y < BED_Coord.size(); y++) {
				try {
					String seq = new String(QUERY.getSubsequenceAt(BED_Coord.get(y).getChrom(),
							BED_Coord.get(y).getStart() + 1, BED_Coord.get(y).getStop()).getBases());
					if (STRAND && BED_Coord.get(y).getDir().equals("-")) {
						seq = FASTAUtilities.RevComplement(seq);
					}
					OUT.println(">" + BED_Coord.get(y).getName() + "\n" + seq);
				} catch (SAMException e) {
					PS.println("INVALID COORDINATE: " + BED_Coord.get(y).toString());
				}
			}
			OUT.close();
			QUERY.close();
		} catch (IllegalArgumentException e) {
			PS.println(e.getMessage());
		} catch (FileNotFoundException e) {
			PS.println(e.getMessage());
		} catch (SAMException e) {
			PS.println(e.getMessage());
		}
	}
}