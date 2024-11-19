package scriptmanager.scripts.Read_Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import scriptmanager.objects.PileupParameters;
import scriptmanager.objects.CoordinateObjects.BEDCoord;
import scriptmanager.scripts.Read_Analysis.PileupScripts.PileupExtract;
import scriptmanager.util.ArrayUtilities;

/**
 * Pileup tags or user-defined encodings around a set of reference points
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Read_Analysis.PileupScripts.PileupExtract
 * @see scriptmanager.cli.Read_Analysis.TagPileupCLI
 * @see scriptmanager.window_interface.Read_Analysis.TagPileupOutput
 * @see scriptmanager.window_interface.Read_Analysis.TagPileupWindow
 */
public class TagPileup {
	File BED = null;
	File BAM = null;

	PileupParameters PARAM = null;
	public double[] DOMAIN = null;
	public double[] AVG_S1 = null;
	public double[] AVG_S2 = null;

	PrintStream COMPOSITE = null;
	// Generic print stream to accept PrintStream of GZIPOutputStream
	Writer OUT_S1 = null;
	Writer OUT_S2 = null;

	PrintStream PS = null;

	String outMatrixBasename;

	/**
	 * Creates a new instance of the TagPileup script
	 * 
	 * @param be              BED file to be used in Pileup
	 * @param ba              BAM file to be used in Pileup
	 * @param param           Set of parameters for Pileup analysis (specified by
	 *                        user with GUI)
	 * @param outputwindow_ps Output to be displayed to user
	 * @param outMat          Base name of the output matrix
	 */
	public TagPileup(File be, File ba, PileupParameters param, PrintStream outputwindow_ps, String outMat) {
		BED = be;
		BAM = ba;
		PARAM = param;
		PS = outputwindow_ps;
		outMatrixBasename = outMat;
	}

	/**
	 * Runs the TagPileup script
	 * 
	 * @throws IOException Invalid file or parameters
	 */
	public void run() throws IOException {
		// Set-up Matrix output writers
		int STRAND = PARAM.getStrand();
		if (PARAM.getOutputType() != 0) {
			if (STRAND == 0) {
				// Set FileName
				String NAME0;
				String NAME1;
				if (outMatrixBasename == null) {
					NAME0 = PARAM.getOutputDirectory() + File.separator + PARAM.generateFileName(BED.getName(), BAM.getName(), 0);
					NAME1 = PARAM.getOutputDirectory() + File.separator + PARAM.generateFileName(BED.getName(), BAM.getName(), 1);
				} else {
					NAME0 = PARAM.generateFileName(outMatrixBasename, 0);
					NAME1 = PARAM.generateFileName(outMatrixBasename, 1);
				}
				// Build streams
				if (PARAM.getOutputGZIP()) {
					OUT_S1 = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(NAME0)), "UTF-8");
					OUT_S2 = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(NAME1)), "UTF-8");
				} else {
					OUT_S1 = new PrintWriter(NAME0);
					OUT_S2 = new PrintWriter(NAME1);
				}
			} else {
				// Set FileName
				String NAME2;
				if (outMatrixBasename == null) {
					NAME2 = PARAM.getOutputDirectory() + File.separator + PARAM.generateFileName(BED.getName(), BAM.getName(), 2);
				} else {
					NAME2 = PARAM.generateFileName(outMatrixBasename, 2);
				}
				if (PARAM.getOutputGZIP()) {
					OUT_S1 = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(NAME2)), "UTF-8");
				} else {
					OUT_S1 = new PrintWriter(NAME2);
				}
			}
		}

		printPS(getTimeStamp()); // Timestamp process
		printPS(BAM.getName()); // Label stat object with what BAM file is generating it

		// Validate and load BED coordinates
		System.err.println("Validating BED: " + BED.getName());
		Vector<BEDCoord> INPUT = validateBED(loadCoord(BED), BAM);

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
			// System.out.println("CPU: " + x + "\tInterval: " + currentindex + "\t" +
			// subset);
			PileupExtract extract = new PileupExtract(PARAM, BAM, INPUT, currentindex, subset);
			parseMaster.execute(extract);
		}
		parseMaster.shutdown();
		while (!parseMaster.isTerminated()) {
		}

		DOMAIN = new double[getMaxBEDSize(INPUT)];
		AVG_S1 = new double[DOMAIN.length];
		AVG_S2 = null;
		if (STRAND == 0)
			AVG_S2 = new double[DOMAIN.length];

		// Account for the shifted oversized window produced by binning and smoothing
		int OUTSTART = 0;
		if (PARAM.getTrans() == PileupParameters.WINDOW) {
			OUTSTART = PARAM.getSmooth();
		} else if (PARAM.getTrans() == PileupParameters.GAUSSIAN) {
			OUTSTART = (PARAM.getStdSize() * PARAM.getStdNum());
		}

		// Write headers
		if (PARAM.getOutputType() == 2) {
			if (OUT_S1 != null)
				OUT_S1.write("YORF\tNAME");
			if (OUT_S2 != null)
				OUT_S2.write("YORF\tNAME");
			double[] tempF = INPUT.get(0).getFStrand();

			for (int i = OUTSTART; i < tempF.length - OUTSTART; i++) {
				int index = i - OUTSTART;
				if (OUT_S1 != null)
					OUT_S1.write("\t" + index);
				if (OUT_S2 != null)
					OUT_S2.write("\t" + index);
			}
			if (OUT_S1 != null)
				OUT_S1.write("\n");
			if (OUT_S2 != null)
				OUT_S2.write("\n");
		}

		// Output individual sites
		for (int i = 0; i < INPUT.size(); i++) {
			double[] tempF = INPUT.get(i).getFStrand();
			double[] tempR = INPUT.get(i).getRStrand();
			if (OUT_S1 != null)
				OUT_S1.write(INPUT.get(i).getName());
			if (OUT_S2 != null)
				OUT_S2.write(INPUT.get(i).getName());

			if (PARAM.getOutputType() == 2) {
				if (OUT_S1 != null)
					OUT_S1.write("\t" + INPUT.get(i).getName());
				if (OUT_S2 != null)
					OUT_S2.write("\t" + INPUT.get(i).getName());
			}

			for (int j = 0; j < tempF.length; j++) {
				// Output values outside of window overhang
				if (j >= OUTSTART && j < tempF.length - OUTSTART) {
					if (OUT_S1 != null)
						OUT_S1.write("\t" + tempF[j]);
					if (OUT_S2 != null)
						OUT_S2.write("\t" + tempR[j]);
				}
				// Sum positions across BED coordinates
				AVG_S1[j] += tempF[j];
				if (AVG_S2 != null)
					AVG_S2[j] += tempR[j];
			}
			if (OUT_S1 != null) OUT_S1.write("\n");
			if (OUT_S2 != null) OUT_S2.write("\n");
		}

		if (OUT_S1 != null) OUT_S1.close();
		if (OUT_S2 != null) OUT_S2.close();

		// Calculate average and domain here
		int temp = (int) (((double) AVG_S1.length / 2.0) + 0.5);
		for (int i = 0; i < AVG_S1.length; i++) {
			DOMAIN[i] = (double) ((temp - (AVG_S1.length - i)) * PARAM.getBin()) + 1;
			AVG_S1[i] /= INPUT.size();
			if (AVG_S2 != null)
				AVG_S2[i] /= INPUT.size();
		}

		// Transform average given transformation parameters
		if (PARAM.getTrans() == PileupParameters.WINDOW) {
			AVG_S1 = ArrayUtilities.windowSmooth(AVG_S1, PARAM.getSmooth());
			if (AVG_S2 != null)
				AVG_S2 = ArrayUtilities.windowSmooth(AVG_S2, PARAM.getSmooth());
		} else if (PARAM.getTrans() == PileupParameters.GAUSSIAN) {
			AVG_S1 = ArrayUtilities.gaussSmooth(AVG_S1, PARAM.getStdSize(), PARAM.getStdNum());
			if (AVG_S2 != null)
				AVG_S2 = ArrayUtilities.gaussSmooth(AVG_S2, PARAM.getStdSize(), PARAM.getStdNum());
		}

		// Trim average here and output to statistics pane
		double[] AVG_S1_trim = new double[AVG_S1.length - (OUTSTART * 2)];
		double[] AVG_S2_trim = null;
		if (STRAND == 0)
			AVG_S2_trim = new double[AVG_S1_trim.length];
		double[] DOMAIN_trim = new double[AVG_S1_trim.length];
		for (int i = OUTSTART; i < AVG_S1.length - OUTSTART; i++) {
			if (AVG_S2 != null) {
				printPS(DOMAIN[i] + "\t" + AVG_S1[i] + "\t" + AVG_S2[i]);
				AVG_S2_trim[i - OUTSTART] = AVG_S2[i];
			} else {
				printPS(DOMAIN[i] + "\t" + AVG_S1[i]);
			}
			AVG_S1_trim[i - OUTSTART] = AVG_S1[i];
			DOMAIN_trim[i - OUTSTART] = DOMAIN[i];
		}
		AVG_S1 = AVG_S1_trim;
		AVG_S2 = AVG_S2_trim;
		DOMAIN = DOMAIN_trim;

		// Output composite data file setup
		COMPOSITE = PARAM.getCompositePrintStream();

		// Output composite data to tab-delimited file
		if (COMPOSITE != null) {
			for (int a = 0; a < DOMAIN.length; a++) {
				COMPOSITE.print("\t" + DOMAIN[a]);
			}
			COMPOSITE.println();
			if (STRAND == 0) {
				COMPOSITE.print(PARAM.generateFileName(BED.getName(), BAM.getName(), 0));
				for (int a = 0; a < AVG_S1.length; a++) {
					COMPOSITE.print("\t" + AVG_S1[a]);
				}
				COMPOSITE.println();
				COMPOSITE.print(PARAM.generateFileName(BED.getName(), BAM.getName(), 1));
				for (int a = 0; a < AVG_S2.length; a++) {
					COMPOSITE.print("\t" + AVG_S2[a]);
				}
				COMPOSITE.println();
			} else {
				COMPOSITE.print(PARAM.generateFileName(BED.getName(), BAM.getName(), 2));
				for (int a = 0; a < AVG_S1.length; a++) {
					COMPOSITE.print("\t" + AVG_S1[a]);
				}
				COMPOSITE.println();
			}
		}
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

	private void printPS(String line) {
		if (PS != null) {
			PS.println(line);
		} else {
			System.err.println(line);
		}
	}

}
