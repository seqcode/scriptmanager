package scriptmanager.scripts.Read_Analysis;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloseableIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.jfree.chart.ChartPanel;

import scriptmanager.charts.ScalingPlotter;
import scriptmanager.objects.CoordinateObjects.BEDCoord;

/**
 * Calculate various kinds of normalization factors from a BAM file. <br>
 * NCIS code adapted from Mahony Lab <a href=
 * "https://github.com/seqcode/seqcode-core">https://github.com/seqcode/seqcode-core</a>
 * <br>
 * NCIS algorithm from Liang &amp; Keles (BMC Bioinformatics 2012)
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Read_Analysis.ScalingFactorCLI
 * @see scriptmanager.window_interface.Read_Analysis.ScalingFactorOutput
 * @see scriptmanager.window_interface.Read_Analysis.ScalingFactorWindow
 */
public class ScalingFactor {

	private File BAMFile = null;
	private File BLACKLISTFile = null;
	private File CONTROL = null;
	private File OUT_BASENAME = null;
	private boolean OUTPUTSTATUS = false;
	private String FILEID = null;

	private int scaleType = -1;
	private int windowSize = 500;
	private double minFraction = 0.75;

	private List<String> chromName = null;
	private List<Long> chromLength = null;

	private List<Float> Sgenome = new ArrayList<Float>();
	private double STagcount = 0;
	private List<Float> Cgenome = new ArrayList<Float>();
	private double CTagcount = 0;

	private HashMap<String, ArrayList<BEDCoord>> BLACKLIST = null;
	private double SCALE = 1;

	private ChartPanel CC_plot; // Cumulative
	private ChartPanel M_plot; // Marginal Plot

	private String dialogMessage = null;

	public static final short TOTAL_TAG = 1;
	public static final short NCIS = 2;
	public static final short NCIS_W_TOTAL_TAG = 3;

	/**
	 * Initialize scaling factor parameters in this constructor
	 * 
	 * @param bamFile      the BAM file to calculate a scaling factor from
	 * @param bl           the BED formatted blacklist file for excluding specific
	 *                     regions from the calculation
	 * @param c            the control BAM file that is used by the NCIS strategy to
	 *                     determine background signal
	 * @param out_basename the filepath base name (the script will append suffixes)
	 *                     for the output files
	 * @param scale        an integer value encoding the scaling type strategy to
	 *                     use (1=Total Tag, 2=NCIS, 3=NCISwithTotal)
	 * @param win          the NCIS parameter for the window/bin size (only used if
	 *                     scale!=1)
	 * @param min          the NCIS parameter for the minimum fraction (only used if
	 *                     scale!=1)
	 * @param out          whether or not to write the output (write =true, don't
	 *                     write = false)
	 */
	public ScalingFactor(File bamFile, File bl, File c, File out_basename, int scale, int win, double min, boolean out) {
		BAMFile = bamFile;
		BLACKLISTFile = bl;
		CONTROL = c;
		OUT_BASENAME = out_basename;
		OUTPUTSTATUS = out;
		scaleType = scale;
		windowSize = win;
		minFraction = min;
	}

	/**
	 * Execute to calculate and write/store a scaling factor with the currently
	 * stored input values.
	 * 
	 * @throws IOException Invalid file or parameters
	 */
	public void run() throws IOException {
		// Load blacklist HashMap if blacklist file uploaded by user
		if (BLACKLISTFile != null) {
			loadBlacklist(BLACKLISTFile);
		}

		// Load up the Control File once per run
		if (scaleType != TOTAL_TAG) {
			System.err.println(getTimeStamp() + "\nLoading control genome array...");
			initalizeGenomeMetainformation(CONTROL);
			Cgenome = initializeList(CONTROL, false);
			System.err.println("Array loaded");
		}

		File f = new File(BAMFile + ".bai"); // Generate file name for BAI index file
		// Check if BAI index file exists
		if (!f.exists() || f.isDirectory()) {
			dialogMessage = "BAI Index File does not exist for: " + BAMFile.getName();
			System.err.println(dialogMessage);
		} else {
			System.err.println(getTimeStamp());
			FILEID = BAMFile.getName();
			System.err.println("Sample file:\t" + FILEID);
			if (CONTROL != null) {
				System.err.println("Control file:\t" + CONTROL.getName());
			}

			SCALE = 1;
			if (scaleType == TOTAL_TAG) {
				initalizeGenomeMetainformation(BAMFile);
				Sgenome = initializeList(BAMFile, true);
				double genomeSize = 0;
				for (int x = 0; x < chromLength.size(); x++) {
					genomeSize += chromLength.get(x);
				}
				if (genomeSize != 0) {
					SCALE = genomeSize / STagcount;
				}
				System.err.println("Sample tags: " + STagcount);
				System.err.println("Genome size: " + genomeSize);
				System.err.println("Total tag ratio: " + SCALE);
			} else {
				if (verifyFiles()) {
					System.err.println("\nLoading sample genome array...");
					Sgenome = initializeList(BAMFile, true);
					System.err.println("Array loaded");
					System.err.println("Sample tags: " + STagcount);
					System.err.println("Control tags: " + CTagcount);
					System.err.println("Bin count: " + Sgenome.size());

					if (scaleType == NCIS) {
						System.err.println("\nCalculating NCIS scaling ratio...");
						SCALE = 1 / scalingRatioByNCIS(Sgenome, Cgenome, OUT_BASENAME, FILEID, minFraction);
						System.err.println("NCIS sample scaling ratio: " + SCALE);
					} else if (scaleType == NCIS_W_TOTAL_TAG) {
						System.err.println("\nCalculating Total tag NCIS scaling ratio...");
						SCALE = 1 / scalingRatioByHitRatioAndNCIS(Sgenome, Cgenome, STagcount, CTagcount, OUT_BASENAME,
								FILEID, minFraction);
						System.err.println("NCIS with Total Tag sample scaling ratio: " + SCALE);
					}
				} else {
					SCALE = Double.NaN;
				}
			}

			// Output scaling factor is user-specified
			if (OUTPUTSTATUS) {
				PrintStream OUT = new PrintStream(OUT_BASENAME + "_ScalingFactors.out");
				OUT.println("Sample file:\t" + BAMFile.getCanonicalPath());
				if (scaleType == TOTAL_TAG) {
					OUT.println("Scaling type:\tTotalTag");
				} else {
					OUT.println("Control file:\t" + CONTROL);
					if (scaleType == NCIS) {
						OUT.println("Scaling type:\tNCIS");
					} else if (scaleType == NCIS_W_TOTAL_TAG) {
						OUT.println("Scaling type:\tTotalTag with NCIS");
					}
					OUT.println("Window size (bp):\t" + windowSize);
					OUT.println("Minimum fraction:\t" + minFraction);
				}
				OUT.println("Scaling factor:\t" + SCALE);
				OUT.close();
			}
			System.err.println(getTimeStamp());
		}
	}

	/**
	 * Creates a list of reads based on a BAM file, excluding blacklisted reads
	 * @param BAM BAM file to be used
	 * @param sample If the BAM file is a sample or a reference (true = sample, false = reference)
	 * @return A list of tags
	 */
	public List<Float> initializeList(File BAM, boolean sample) {
		List<Float> GENOME = new ArrayList<Float>();
		SamReader inputBAM = SamReaderFactory.makeDefault().open(BAM);
		double TOTAL = 0;

		for (int x = 0; x < chromName.size(); x++) {
			String seq = chromName.get(x);
			long chromSize = chromLength.get(x);
			float[] chrom = new float[(int) (chromSize / windowSize) + 1];
			// Blacklist filter each chromosome, set blacklisted windows to NaN
			if (BLACKLIST != null) {
				chrom = maskChrom(seq, chromSize, windowSize);
			}
			// Iterate through chromosome loading tags into window bin
			CloseableIterator<SAMRecord> iter = inputBAM.query(seq, 0, (int) chromSize, false);
			// SAMRecords are 1-based
			while (iter.hasNext()) {
				SAMRecord sr = iter.next();
				int FivePrime = sr.getUnclippedStart() - 1;
				if (sr.getReadNegativeStrandFlag()) {
					FivePrime = sr.getUnclippedEnd();
				}
				int INDEX = (FivePrime / windowSize);
				if (sr.getReadPairedFlag()) { // If paired-end, take only read 1
					// Read 1
					if (sr.getFirstOfPairFlag()) {
						if (INDEX < chrom.length) {
							chrom[INDEX]++;
						}
					}
				} else { // If NOT paired-end, tag is always read 1
					if (INDEX < chrom.length) {
						chrom[INDEX]++;
					}
				}
			}
			iter.close();
			for (int i = 0; i < chrom.length; i++) {
				// System.out.println(seq + "\t" + chrom[i]);
				if (!Float.isNaN(chrom[i])) {
					TOTAL += chrom[i];
					GENOME.add(chrom[i]);
				}
			}
		}
		if (sample) {
			STagcount = TOTAL;
		} else {
			CTagcount = TOTAL;
		}
		return GENOME;
	}

	/**
	 * Sets blacklisted to NaN
	 * 
	 * @param chrom      Chromosome to be processed
	 * @param chromSize  Length of the chromsome
	 * @param windowSize The window/bin size
	 * @return An array representing the chromosome, with blacklisted regions being
	 *         represented as NaN and valid regions being zero
	 */
	public float[] maskChrom(String chrom, long chromSize, int windowSize) {
		float[] chromArray = new float[(int) (chromSize / windowSize) + 1];
		if (BLACKLIST.containsKey(chrom)) {
			ArrayList<BEDCoord> blacklist = BLACKLIST.get(chrom);
			for (int x = 0; x < blacklist.size(); x++) {
				long START = blacklist.get(x).getStart();
				long STOP = blacklist.get(x).getStop();
				while (START < STOP) {
					int index = ((int) START / windowSize);
					if (index < chromArray.length) {
						chromArray[index] = Float.NaN;
					}
					START += windowSize;
				}
			}
		}
		return chromArray;
	}

	/**
	 * Loads the blacklisted coordinates from a BED file
	 * 
	 * @param BLACKFile BED file with blacklisted coordinates
	 * @throws FileNotFoundException Script could not find valid input file
	 */
	public void loadBlacklist(File BLACKFile) throws FileNotFoundException {
		BLACKLIST = new HashMap<String, ArrayList<BEDCoord>>();
		Scanner scan = new Scanner(BLACKFile);
		while (scan.hasNextLine()) {
			String[] temp = scan.nextLine().split("\t");
			if (temp.length > 2) {
				if (!temp[0].contains("track") && !temp[0].contains("#")) {
					if (Integer.parseInt(temp[1]) >= 0) {
						int start = Integer.parseInt(temp[1]);
						int stop = Integer.parseInt(temp[2]);
						BEDCoord coord = new BEDCoord(temp[0], start, stop, ".");
						if (BLACKLIST.containsKey(temp[0])) {
							BLACKLIST.get(temp[0]).add(coord);
						} else {
							ArrayList<BEDCoord> newchrom = new ArrayList<BEDCoord>();
							newchrom.add(coord);
							BLACKLIST.put(temp[0], newchrom);
						}
					} else {
						System.err.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
					}
				}
			}
		}
		scan.close();

//		Iterator it = BLACKLIST.entrySet().iterator();
//	    while (it.hasNext()) {
//	        HashMap.Entry pair = (HashMap.Entry)it.next();
//	        ArrayList<BEDCoord> temp = (ArrayList<BEDCoord>) pair.getValue();
//	        for(int x = 0; x < temp.size(); x++) {
//	        	System.out.println(pair.getKey() + " = " + temp.get(x).toString());
//	        }
//	        it.remove(); // avoids a ConcurrentModificationException
//	    }
	}

	/**
	 * Initialized chromName and cromLength variables, with the name and length of
	 * each chromosome respectively
	 * 
	 * @param BAM File to be used for initialization
	 * @throws IOException Invalid file or parameters
	 */
	public void initalizeGenomeMetainformation(File BAM) throws IOException {
		SamReaderFactory factory = SamReaderFactory.makeDefault()
				.enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS,
						SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS)
				.validationStringency(ValidationStringency.SILENT);
		SamReader Sreader = factory.open(BAM);
		AbstractBAMFileIndex Sbai = (AbstractBAMFileIndex) Sreader.indexing().getIndex();
		chromName = new ArrayList<String>();
		chromLength = new ArrayList<Long>();
		for (int z = 0; z < Sbai.getNumberOfReferences(); z++) {
			SAMSequenceRecord Sseq = Sreader.getFileHeader().getSequence(z);
			chromName.add(Sseq.getSequenceName());
			chromLength.add(Long.valueOf(Sseq.getSequenceLength()));
		}
		Sreader.close();
		Sbai.close();
	}

	/**
	 * Verifies that sample and control files match
	 * @return Whether sample and control files are a valid pair
	 * @throws IOException Invalid file or parameters
	 */
	public boolean verifyFiles() throws IOException {
		SamReaderFactory factory = SamReaderFactory.makeDefault()
				.enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS,
						SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS)
				.validationStringency(ValidationStringency.SILENT);
		SamReader Sreader = factory.open(BAMFile);
		SamReader Creader = factory.open(CONTROL);
		AbstractBAMFileIndex Sbai = (AbstractBAMFileIndex) Sreader.indexing().getIndex();
		AbstractBAMFileIndex Cbai = (AbstractBAMFileIndex) Creader.indexing().getIndex();
		if (Sbai.getNumberOfReferences() != Cbai.getNumberOfReferences()) {
			dialogMessage = "Unequal number of chromosomes between sample and control!!!";
			System.err.println(dialogMessage);
			return false;
		}
		for (int z = 0; z < Sbai.getNumberOfReferences(); z++) {
			SAMSequenceRecord Sseq = Sreader.getFileHeader().getSequence(z);
			SAMSequenceRecord Cseq = Creader.getFileHeader().getSequence(z);
			if (!Sseq.getSequenceName().equals(Cseq.getSequenceName())) {
				dialogMessage = "Chromosome names do not match!!!\n" + Sseq.getSequenceName() + "\n"
						+ Cseq.getSequenceName();
				System.err.println(dialogMessage);
				return false;
			}
			if (Sseq.getSequenceLength() != Cseq.getSequenceLength()) {
				dialogMessage = "Chromosome lengths do not match!!!\n" + Sseq.getSequenceName() + "\t"
						+ Sseq.getSequenceLength() + "\n" + Cseq.getSequenceName() + "\t" + Cseq.getSequenceLength();
				System.err.println(dialogMessage);
				return false;
			}
		}
		Sreader.close();
		Creader.close();
		Sbai.close();
		Cbai.close();
		return true;
	}

	/**
	 * Find the scaling ratio according to the NCIS method from Liang &amp; Keles
	 * (BMC Bioinf 2012). Also sets a background proportion estimate for the signal
	 * channel. Should be run using *all* genomic windows in the Lists. Uses ratios
	 * that are based on at least 75% of genomic regions by default.
	 * 
	 * @param setA    signal list
	 * @param setB    control list
	 * @param outpath optional file that will contain the data
	 * @param fileid
	 * @param minFrac
	 * @return
	 */
	public double scalingRatioByNCIS(List<Float> setA, List<Float> setB, File outpath, String fileid,
			double minFrac) {
		double scalingRatio = 1;
		double totalAtScaling = 0;
		if (setA.size() != setB.size()) {
			System.err.println("NCIS is trying to scale lists of two different lengths");
			System.exit(1);
		}

		float numPairs = (float) setA.size();
		List<PairedCounts> counts = new ArrayList<PairedCounts>();
		for (int x = 0; x < setA.size(); x++) {
			counts.add(new PairedCounts(setA.get(x), setB.get(x)));
		}
		// NCIS uses increasing total tag counts versus enrichment ratio
		Collections.sort(counts, new Comparator<PairedCounts>() {
			public int compare(PairedCounts o1, PairedCounts o2) {
				return o1.compareByTotal(o2);
			}
		});

		// NCIS procedure
		double cumulA = 0, cumulB = 0, currRatio = 0, lastRatio = -1;
		float i = 0;
		for (PairedCounts pc : counts) {
			cumulA += pc.x;
			cumulB += pc.y;
			totalAtScaling = pc.x + pc.y;
			i++;
			if (i / numPairs > minFrac && cumulA > 0 && cumulB > 0) { // NCIS estimates begin using the lower 3
																		// quartiles of the genome (based on total tags)
				currRatio = (cumulA / cumulB);
				if (lastRatio == -1 || currRatio < lastRatio) {
					lastRatio = currRatio;
				} else {
					break;
				}
			}
		}
		scalingRatio = currRatio;
		// Generate and output scatter plots
		plotGraphs(counts, totalAtScaling, scalingRatio, outpath, fileid, "NCIS");

		return (scalingRatio);
	}

	/**
	 * Find the scaling ratio according to the total tag normalization followed by
	 * NCIS method from Liang &amp; Keles (BMC Bioinf 2012). Also sets a background
	 * proportion estimate for the signal channel. Should be run using *all* genomic
	 * windows in the Lists. Uses ratios that are based on at least 75% of genomic
	 * regions by default.
	 * 
	 * @param setA    signal list
	 * @param setB    control list
	 * @param totalA  Total number of A reads
	 * @param totalB  Total number of B reads
	 * @param outpath optional file that will contain the data
	 * @param fileid  Name of file to be used when titling plots
	 * @param minFrac Minimum ratio of paired counts / num pairs for analysis to
	 *                start
	 * @return
	 */
	public double scalingRatioByHitRatioAndNCIS(List<Float> setA, List<Float> setB, double totalA, double totalB,
			File outpath, String fileid, double minFrac) {
		double scalingRatio = 1;
		double totalAtScaling = 0;
		if (setA.size() != setB.size()) {
			System.err.println("NCIS is trying to scale lists of two different lengths");
			System.exit(1);
		}

		// First normalize tag number between experiments using total reads
		float tRatio = (float) (totalA / totalB);
		List<Float> setnB = new ArrayList<Float>();
		for (int x = 0; x < setB.size(); x++) {
			setnB.add(setB.get(x) * tRatio);
		}

		float numPairs = (float) setA.size();
		List<PairedCounts> counts = new ArrayList<PairedCounts>();
		for (int x = 0; x < setA.size(); x++) {
			counts.add(new PairedCounts(setA.get(x), setnB.get(x)));
		}
		// NCIS uses increasing total tag counts versus enrichment ratio
		Collections.sort(counts, new Comparator<PairedCounts>() {
			public int compare(PairedCounts o1, PairedCounts o2) {
				return o1.compareByTotal(o2);
			}
		});

		// NCIS procedure
		double cumulA = 0, cumulB = 0, currRatio = 0, lastRatio = -1;
		float i = 0;
		for (PairedCounts pc : counts) {
			cumulA += pc.x;
			cumulB += pc.y;
			totalAtScaling = pc.x + pc.y;
			i++;
			if (i / numPairs > minFrac && cumulA > 0 && cumulB > 0) { // NCIS estimates begin using the lower 3
																		// quartiles of the genome (based on total tags)
				currRatio = (cumulA / cumulB);
				if (lastRatio == -1 || currRatio < lastRatio) {
					lastRatio = currRatio;
				} else {
					break;
				}
			}
		}
		scalingRatio = currRatio * tRatio; // Multiply by the total tag normalization
		// Generate and output scatter plots
		plotGraphs(counts, totalAtScaling, scalingRatio, outpath, fileid, "TotalReadsAndNCIS");

		return (scalingRatio);
	}

	/**
	 * Simple class for storing paired counts that are sortable in first dimension
	 * 
	 * @author mahony
	 *
	 */
	public static class PairedCounts implements Comparable<PairedCounts> {
		public Double x, y;

		/**
		 * Creates a new PaireCounts object
		 * @param a The value of X
		 * @param b The value of Y
		 */
		public PairedCounts(double a, double b) {
			x = a;
			y = b;
		}

		/**
		 * Sort on increasing X variables
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(PairedCounts pc) {
			if (x < pc.x) {
				return -1;
			}
			if (x > pc.x) {
				return 1;
			}
			return 0;
		}

		/**
		 * Compare based on the sum of both paired counts
		 * 
		 * @param pc
		 * @return
		 */
		public int compareByTotal(PairedCounts pc) {
			if ((x + y) < (pc.x + pc.y)) {
				return -1;
			}
			if ((x + y) > (pc.x + pc.y)) {
				return 1;
			}
			return 0;
		}

	}

	/**
	 * Generates scatter plots, assigning them to CC_plot and M_plot respectively
	 * 
	 * @param counts         List of PairedCounts
	 * @param totalAtScaling Draw a red line at the y-axis for this value
	 * @param scalingRatio   Draw a red line at the x-axis for this value
	 * @param outbase        Base name for output files
	 * @param fileid         Name of file to be used when titling chart
	 * @param scaletype      Type of scaling to use (1=Total Tag, 2=NCIS,
	 *                       3=NCISwithTotal)
	 */
	public void plotGraphs(List<PairedCounts> counts, double totalAtScaling, double scalingRatio, File outbase,
			String fileid, String scaletype) {
		// Scaling plot generation
		// Cumulative ratio vs bin total
		List<Double> bintotals = new ArrayList<Double>();
		List<Double> ratios = new ArrayList<Double>();
		double cumulA = 0, cumulB = 0;
		for (PairedCounts pc : counts) {
			cumulA += pc.x;
			cumulB += pc.y;
			if (cumulA > 0 && cumulB > 0) {
				Double ratio = (cumulA / cumulB);
				bintotals.add(pc.x + pc.y);
				ratios.add(ratio);
			}
		}

		// Marginal ratios vs bin totals
		List<Double> bintot = new ArrayList<Double>();
		List<Double> mratios = new ArrayList<Double>();
		for (int x = 0; x < counts.size(); x++) {
			PairedCounts pc = counts.get(x);
			if (pc.x > 0 && pc.y > 0) {
				double currA = pc.x, currB = pc.y;
				double currTot = pc.x + pc.y;
				while (x < counts.size() - 1 && (counts.get(x + 1).x + counts.get(x + 1).y) == currTot) {
					x++;
					pc = counts.get(x);
					currA += pc.x;
					currB += pc.y;
				}
				bintot.add(currTot);
				mratios.add(currA / currB);
			}
		}
		// Generate images
		CC_plot = ScalingPlotter.generateXYplot(bintotals, ratios, totalAtScaling, scalingRatio,
				fileid + " " + scaletype + " plot", "Binned Total Tag Count", "Cumulative Count Scaling Ratio");
		M_plot = ScalingPlotter.generateXYplot(bintot, mratios, totalAtScaling, scalingRatio,
				fileid + " " + scaletype + " plot", "Binned Total Tag Count", "Marginal Signal/Control Ratio");

		if (OUTPUTSTATUS) {
			// Print data points to files
			try {
				FileWriter Cfout = new FileWriter(outbase.getAbsolutePath() + "." + scaletype + "_scaling-ccr.count");
				for (int d = 0; d < bintotals.size(); d++) {
					Cfout.write(bintotals.get(d) + "\t" + ratios.get(d) + "\n");
				}
				Cfout.close();
				FileWriter Mfout = new FileWriter(outbase.getAbsolutePath() + "." + scaletype + "_scaling-marginal.count");
				for (int d = 0; d < bintot.size(); d++) {
					Mfout.write(bintot.get(d) + "\t" + mratios.get(d) + "\n");
				}
				Mfout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the cumulative plot
	 * 
	 * @return
	 */
	public ChartPanel getCCPlot() {
		return (CC_plot);
	}

	/**
	 * Returns the marginal plot
	 * 
	 * @return The marginal plot
	 */
	public ChartPanel getMPlot() {
		return (M_plot);
	}

	/**
	 * Returns the scaling factor
	 * 
	 * @return The scaling factor
	 */
	public double getScalingFactor() {
		return (SCALE);
	}

	/**
	 * Returns the lastest error message
	 * 
	 * @return the lastest error message
	 */
	public String getDialogMessage() {
		return (dialogMessage);
	}

	private static String getTimeStamp() {
		Date date = new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
}
