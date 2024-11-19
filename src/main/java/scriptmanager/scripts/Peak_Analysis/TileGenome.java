package scriptmanager.scripts.Peak_Analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import scriptmanager.objects.CoordinateObjects.BEDCoord;
import scriptmanager.objects.CoordinateObjects.GFFCoord;
import scriptmanager.util.GZipUtilities;
import scriptmanager.util.GenomeSizeReference;

/**
 * Class with static method for creating a coordinate file of tiles across a
 * genome.
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Peak_Analysis.TileGenomeCLI
 * @see scriptmanager.util.GenomeSizeReference
 * @see scriptmanager.window_interface.Peak_Analysis.TileGenomeWindow
 */
	public class TileGenome {

	/**
	 * Create and write output BED/GFF file of genomic intervals tiling the genome.
	 * All entries will be "+" stranded, score="0.0", and the identifier column is a
	 * genomic coordinate string
	 * (&lt;chr&gt;_&lt;start&gt;_&lt;stop&gt;_&lt;dir&gt;). For the BED format, the
	 * identifier column will be the 4th column while the GFF format will use the
	 * 9th column for the identifier.
	 * 
	 * @param GENOME     the String encoding the genome build to tile (matches
	 *                   util.GenomeSizeReference)
	 * @param OUTPUT     the file to write the coordinate tile output to (if null, a
	 *                   default filename is determined using
	 *                   &lt;GENOME&gt;_&lt;windowSize&gt;bp.&lt;ext&gt;)
	 * @param BEDout     coordinate file format of output where BED-format is used
	 *                   if true and GFF-format used if false
	 * @param windowSize the base-pair length of the tiles
	 * @param gzOutput   whether or not to gzip output
	 * @throws IOException Invalid file or parameters
	 * @throws IllegalArgumentException
	 */
	public static void execute(String GENOME, File OUTPUT, boolean BEDout, int windowSize, boolean gzOutput) throws IOException, IllegalArgumentException {
		GenomeSizeReference coord = new GenomeSizeReference(GENOME);
		String EXTENSION = BEDout ? ".bed" : ".gff";
		String fileName = GENOME + "_" + windowSize + "bp" + EXTENSION;
		PrintStream OUT = null;
		if (OUTPUT == null) {
			OUT = GZipUtilities.makePrintStream(new File(fileName), gzOutput);
		} else {
			OUT = GZipUtilities.makePrintStream(OUTPUT, gzOutput);
		}
		// Iterate each chromosome
		for(int x = 0; x < coord.getChrom().size(); x++) {
			String CHROM = coord.getChrom().get(x);
			long SIZE = coord.getChromSize().get(x);
			// Iterate each window
			for(long y = 0; y < SIZE; y += windowSize) {
				long STOP = y + windowSize;
				//If beyond the edge of chromosome, set end to size
				if (STOP > SIZE) { STOP = SIZE; }
				if (BEDout) {
		    		BEDCoord outcoord = new BEDCoord(CHROM, y, STOP, "+");
		    		OUT.println(outcoord.toString());
		    	} else {
		    		GFFCoord outcoord = new GFFCoord(CHROM, y + 1, STOP, "+");
		    		OUT.println(outcoord.toString());
		    	}
			}
	    }
		OUT.close();
	}
}
