package scriptmanager.scripts.Peak_Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import org.jfree.chart.ChartPanel;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import scriptmanager.charts.Histogram;
import scriptmanager.objects.PileupParameters;
import scriptmanager.objects.CoordinateObjects.BEDCoord;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.BAMUtilities;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.ArrayUtilities;

import scriptmanager.scripts.Read_Analysis.PileupScripts.PileupExtract;

/**
 * Calculate a FRiX score (Fraction of Reads in Motif/Peak).
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.Read_Analysis.PileupScripts.PileupExtract
 * @see scriptmanager.cli.Peak_Analysis.PileupScripts.FRiXCalculatorCLI
 * @see scriptmanager.window_interface.Peak_Analysis.PileupScripts.FRiXCalculatorOutput
 * @see scriptmanager.window_interface.Peak_Analysis.PileupScripts.FRiXCalculatorWindows
 */
public class FRiXCalculator {
	File BED = null;
	File BAM = null;

	PileupParameters PARAM = null;
	public int[] DOMAIN = null;
	public double[] FREQ = null;

	PrintStream COMPOSITE = null;
	// Generic print stream to accept PrintStream of GZIPOutputStream
	Writer OUTPUT = null;
	Writer HOUTPUT = null;

	PrintStream PS = null;
	public ChartPanel histChart = null;

	String OBASENAME;

	public static final String[] COLUMN_NAMES = {
			"BAM Filename",
			"BED Filename",
			"Sum of Read Counts",
			"Num BED entries",
			"Total BAM Reads",
			"FRiX score",
			"Total Genome Size"
//			,"FRiX Density"
	};
	Object[] matrixRow = new Object[COLUMN_NAMES.length];

	/**
	 * Creates a new instance of the FRiXCalculator script
	 * 
	 * @param be              BED file for RefPT (X in FRiX)
	 * @param ba              BAM file of reads to tally
	 * @param param           set of parameters for pileup operation
	 * @param outputwindow_ps output to be displayed to user
	 * @param o               base name of the output statistics
	 */
	public FRiXCalculator(File be, File ba, PileupParameters param, PrintStream outputwindow_ps, String o) {
		BED = be;
		BAM = ba;
		PARAM = param;
		PS = outputwindow_ps;
		OBASENAME = o;
		
		PARAM.setStrand(PileupParameters.COMBINED);
	}

	/**
	 * Runs the FRiXCalculator script
	 * 
	 * @throws OptionException
	 * @throws IOException
	 */
	public void run() throws OptionException, IOException {
		if (OBASENAME == null) {
			// Set output file
			String bedname = ExtensionFileFilter.stripExtensionIgnoreGZ(BED);
			String bamname = ExtensionFileFilter.stripExtension(BAM);
			OBASENAME = bedname + "_" + bamname;
			if (PARAM.getOutputDirectory() != null) {
				OBASENAME = PARAM.getOutputDirectory() + File.separator + OBASENAME;
			}
		}

		// Build streams
		OUTPUT = new PrintWriter(OBASENAME + "_FRiX-statistics.txt");
		HOUTPUT = new PrintWriter(OBASENAME + "_FRiX-frequencies.txt");

		// Timestamp start
		String tempTimeStamp = "# " + getTimeStamp();
		OUTPUT.write(tempTimeStamp + "\n");
		HOUTPUT.write(tempTimeStamp + "\n");

		// Validate and load BED coordinates
		System.err.println("Validating BED: " + BED.getName());
		Vector<BEDCoord> LOADED = loadCoord(BED);
		System.err.println("Loaded " + LOADED.size() + " coords");
		Vector<BEDCoord> INPUT = validateBED(LOADED, BAM);
		System.err.println("Validated " + INPUT.size() + " coords");
		LOADED = null;

		// Split up job and send out to threads to process
		int CPU = PARAM.getCPU();
		ExecutorService parseMaster = Executors.newFixedThreadPool(CPU);
		if (INPUT.size() < CPU)
			CPU = INPUT.size();
		int subset = 0;
		int currentindex = 0;
		for (int x = 0; x < CPU; x++) {
			currentindex += subset;
			if (CPU == 1) {
				subset = INPUT.size();
			} else if (INPUT.size() % CPU == 0) {
				subset = INPUT.size() / CPU;
			} else {
				int remainder = INPUT.size() % CPU;
				if (x < remainder)
					subset = (int) (((double) INPUT.size() / (double) CPU) + 1);
				else
					subset = (int) (((double) INPUT.size() / (double) CPU));
			}
			// System.out.println("CPU: " + x + "\tInterval: " + currentindex + "\t" + subset);
			PileupExtract extract = new PileupExtract(PARAM, BAM, INPUT, currentindex, subset);
			parseMaster.execute(extract);
		}
		parseMaster.shutdown();
		while (!parseMaster.isTerminated()) {
		}

		// Initialize tally storage variable
		double[] TALLY = new double[INPUT.size()];
		// Output individual sites
		for (int i = 0; i < INPUT.size(); i++) {
			TALLY[i] = ArrayUtilities.getSum(INPUT.get(i).getFStrand());
		}

		// Get frequency counts
		FREQ = new double[(int) Arrays.stream(TALLY).max().orElse(0) + 1];
		Arrays.stream(TALLY).forEach(num -> FREQ[(int) num]++);
		// double[] COUNTS = new double[(int)(TALLY[TALLY.length - 1])];
//		int[] COUNTS = Arrays.stream(TALLY).map(e -> (int)e).count();
		// forEach(e -> COUNTS[(int)e]++);
		DOMAIN = IntStream.range(0, FREQ.length).toArray();
		histChart = Histogram.createFRiXBarChart(FREQ, DOMAIN, ExtensionFileFilter.stripExtension(BED));

		// Write frequency values to output
		HOUTPUT.write("TagCount\tFrequency\n");
		for (int i = 0; i < FREQ.length; i++) {
			HOUTPUT.write(i + "\t" + FREQ[i] + "\n");
		}

		// Get/calculate statistics
		double totalSum = Arrays.stream(TALLY).sum();
		double totalReads = BAMUtilities.getReadCount(BAM, PARAM);
		double totalGenomeSize = BAMUtilities.getGenomeSize(BAM);
		double frix = (double) (totalSum / totalReads);
		double density = frix / totalGenomeSize;

		// Write values to report
		OUTPUT.write("BAM filename: " + BAM.getAbsolutePath() + "\n");
		OUTPUT.write("RefPT filename: " + BED.getAbsolutePath() + "\n");
		OUTPUT.write(PARAM.getAspectString() + "\n");
		OUTPUT.write(PARAM.getReadString() + "\n");
		OUTPUT.write("Require PE: " + PARAM.getPErequire() + "\n");
		OUTPUT.write("Insert size minimum (bp): " + PARAM.getMinInsert() + "\n");
		OUTPUT.write("Insert size maximum (bp): " + PARAM.getMaxInsert() + "\n");
		OUTPUT.write("Tag Shift (bp): " + PARAM.getShift() + "\n");
		OUTPUT.write("=================\n");
		OUTPUT.write("Summed tags at all sites: " + totalSum + "\n");
		OUTPUT.write("Number of Sites: " + INPUT.size() + "\n");
		OUTPUT.write("Total aligned read count: " + totalReads + "\n");
		OUTPUT.write("Total genome size: " + totalGenomeSize + "\n");
		OUTPUT.write("FRiX score: " + frix + "\n");
		OUTPUT.write("FRiX density: " + density + "\n");
		OUTPUT.close();

		// Timestamp finish
		tempTimeStamp = "# " + getTimeStamp();
		OUTPUT.write(tempTimeStamp + "\n");
		HOUTPUT.write(tempTimeStamp + "\n");

		// Close file handles
		OUTPUT.close();
		HOUTPUT.close();

		// Input data row for GUI display
		matrixRow[0] = BAM.getName();
		matrixRow[1] = BED.getName();
		matrixRow[2] = totalSum;
		matrixRow[3] = INPUT.size();
		matrixRow[4] = totalReads;
		matrixRow[5] = frix;
		matrixRow[6] = totalGenomeSize;
//		matrixRow[7] = density;

	}

	/**
	 * Getter for table display of this object's row of statistics. The matrixRow variable returned includes the
	 * <ol>
	 *     <li> BAM Filepath
	 * </ol>
	 * 
	 * @return The array of statistics values to display
	 */
	public Object[] getMatrixRow() {
		return (matrixRow);
	}

	/**
	 * Gets size of largest array for composite figure generation
	 * @param sites Sites to find the largest array in
	 * @return The largest array
	 */
	public static int getMaxBEDSize(Vector<BEDCoord> sites) {
		int maxSize = 0;
		for (int x = 0; x < sites.size(); x++) {
			int SIZE = sites.get(x).getFStrand().length;
			if (SIZE > maxSize) {
				maxSize = SIZE;
			}
		}
		return maxSize;
	}

	/**
	 * Validate BED coordinates exist within BAM file and satisfy BED format
	 * @param COORD BED coordinates to validate
	 * @param BAM BAM file to reference
	 * @return Whether input files are valid
	 * @throws IOException Invalid file or parameters
	 */
	public Vector<BEDCoord> validateBED(Vector<BEDCoord> COORD, File BAM) throws IOException {
		Vector<BEDCoord> FINAL = new Vector<BEDCoord>();
		ArrayList<Integer> indexFail = new ArrayList<Integer>();

		// Get chromosome IDs
		SamReader inputSam = SamReaderFactory.makeDefault().open(BAM);
		AbstractBAMFileIndex bai = (AbstractBAMFileIndex) inputSam.indexing().getIndex();
		ArrayList<String> chrom = new ArrayList<String>();
		for (int x = 0; x < bai.getNumberOfReferences(); x++) {
			chrom.add(inputSam.getFileHeader().getSequence(x).getSequenceName());
		}
		inputSam.close();
		bai.close();

		// check each BED coordinate...
		for (int x = 0; x < COORD.size(); x++) {
			// check for (1) bed chrom that aren't in BAM file OR
			// (2) starts smaller than the stop
			if ((!chrom.contains(COORD.get(x).getChrom())) || (COORD.get(x).getStart() > COORD.get(x).getStop())) {
				if (!indexFail.contains(Integer.valueOf(x))) {
					indexFail.add(Integer.valueOf(x));
				}
			}
		}

		if (indexFail.size() == COORD.size()) {
			System.err.println("No BED Coordinates exist within BAM file!!!");
		}

		// Create new input file without failed indexes to more efficiently use CPUs
		for (int x = 0; x < COORD.size(); x++) {
			if (!indexFail.contains(Integer.valueOf(x))) {
				FINAL.add(COORD.get(x));
			}
		}

		return FINAL;
	}


	private static String getTimeStamp() {
		Date date = new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}

	/**
	 * Loads BED coordinates from an input file into a Vector&lt;BEDCoord&gt;
	 * @param INPUT Input BED file
	 * @return Vector of type BEDCoord representing input file
	 * @throws UnsupportedEncodingException
	 * @throws IOException Invalid file or parameters
	 */
	public Vector<BEDCoord> loadCoord(File INPUT) throws UnsupportedEncodingException, IOException {
		Vector<BEDCoord> COORD = new Vector<BEDCoord>();
		BufferedReader br;
		if (INPUT.getAbsoluteFile().toString().endsWith(".gz")) {
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(INPUT)), "UTF-8"));
		} else {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(INPUT), "UTF-8"));
		}

		String line = br.readLine();
		while (line != null) {
			String[] temp = line.split("\t");
			if (temp.length > 2) {
				if (!temp[0].contains("track") && !temp[0].contains("#")) {
					String name = "";
					if (temp.length > 3) {
						name = temp[3];
					} else {
						name = temp[0] + "_" + temp[1] + "_" + temp[2];
					}
					if (Integer.parseInt(temp[1]) >= 0) {
						if (temp.length > 4) {
							if (temp[5].equals("-")) {
								COORD.add(new BEDCoord(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), "-", name));
							} else {
								COORD.add(new BEDCoord(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), "+", name));
							}
						} else {
							COORD.add(new BEDCoord(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), "+", name));
						}
					} else {
						System.err.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
					}
				}
			}
			line = br.readLine();
		}
		br.close();
		return COORD;
	}
}
