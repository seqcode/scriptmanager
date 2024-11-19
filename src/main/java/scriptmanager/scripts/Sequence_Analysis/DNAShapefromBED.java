package scriptmanager.scripts.Sequence_Analysis;

import htsjdk.samtools.SAMException;
import htsjdk.samtools.reference.FastaSequenceIndexCreator;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import scriptmanager.objects.CoordinateObjects.BEDCoord;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import scriptmanager.charts.CompositePlot;
import scriptmanager.util.FASTAUtilities;
import scriptmanager.util.GZipUtilities;
import scriptmanager.util.DNAShapeReference;
import scriptmanager.util.ExtensionFileFilter;

/**
 * Calculate and score various aspects of DNA shape across a set of BED
 * intervals.
 * 
 * @author William KM Lai
 * @see scriptmanager.util.DNAShapeReference
 * @see scriptmanager.cli.Sequence_Analysis.DNAShapefromBEDCLI
 * @see scriptmanager.window_interface.Sequence_Analysis.DNAShapefromBEDOutput
 * @see scriptmanager.window_interface.Sequence_Analysis.DNAShapefromBEDWindow
 */
public class DNAShapefromBED {
	private File GENOME = null;
	private File BED = null;
	private File OUTBASENAME = null;

	private boolean[] OUTPUT_TYPE = null;
	private boolean OUTPUT_COMPOSITE = false;
	private short OUTPUT_MATRIX = 0;
	private boolean GZIP_OUTPUT;

	private boolean STRAND = true;

	private PrintStream OUT_M = null;
	private PrintStream OUT_P = null;
	private PrintStream OUT_H = null;
	private PrintStream OUT_R = null;

	private PrintStream[] PS = null;

	static Map<String, List<Double>> STRUCTURE = null;

	double[] AVG_MGW = null;
	double[] AVG_PropT = null;
	double[] AVG_HelT = null;
	double[] AVG_Roll = null;

	Component chart_M = null;
	Component chart_P = null;
	Component chart_H = null;
	Component chart_R = null;

	public final static short NO_MATRIX = 0;
	public final static short TAB = 1;
	public final static short CDT = 2;

	public DNAShapefromBED(File gen, File b, File out, boolean[] type, boolean str, boolean outputComposite, short outputMatrix, boolean gzOutput) {
		GENOME = gen;
		BED = b;
		OUTBASENAME = out;
		OUTPUT_TYPE = type;
		STRAND = str;
		OUTPUT_COMPOSITE = outputComposite;
		OUTPUT_MATRIX = outputMatrix;
		GZIP_OUTPUT = gzOutput;
		PS = new PrintStream[] { null, null, null, null };

		STRUCTURE = DNAShapeReference.InitializeStructure();
	}

	/**
	 * Initialize object with script inputs for generating DNA shape reports.
	 * 
	 * @param gen             the reference genome sequence in FASTA-format (FAI
	 *                        will be automatically generated)
	 * @param b               the BED-formatted coordinate intervals to extract
	 *                        sequence from
	 * @param out             the output file name base (to add
	 *                        _&lt;shapetype&gt;.cdt suffix to)
	 * @param type            a four-element boolean list for specifying shape type
	 *                        to output (no enforcement on size)
	 * @param str             force strandedness (true=forced, false=not forced)
	 * @param outputComposite whether to output a composite average output
	 * @param outputMatrix    value encoding not to write output matrix data, write
	 *                        matrix in CDT format, and write matrix in tab format
	 * @param gzOutput        whether to output compressed file
	 * @param ps              list of four PrintStream objects corresponding to each
	 *                        shape type (for GUI)
	 * @throws IOException Invalid file or parameters
	 */
	public DNAShapefromBED(File gen, File b, File out, boolean[] type, boolean str, boolean outputComposite, short outputMatrix, boolean gzOutput, PrintStream[] ps) {
		GENOME = gen;
		BED = b;
		OUTBASENAME = out;
		OUTPUT_TYPE = type;
		STRAND = str;
		OUTPUT_COMPOSITE = outputComposite;
		OUTPUT_MATRIX = outputMatrix;
		GZIP_OUTPUT = gzOutput;
		PS = ps;

		STRUCTURE = DNAShapeReference.InitializeStructure();
	}

	/**
	 * Execute script to calculate DNA shape for all types across the input
	 * sequence.
	 * 
	 * @throws IOException Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the same time
	 */
	public void run() throws FileNotFoundException, IOException, InterruptedException {
			File FAI = new File(GENOME + ".fai");
			// Check if FAI index file exists
			if (!FAI.exists() || FAI.isDirectory()) {
				FastaSequenceIndexCreator.create(GENOME.toPath(), true);
			}
			IndexedFastaSequenceFile QUERY = new IndexedFastaSequenceFile(GENOME);

			String NAME = ExtensionFileFilter.stripExtension(BED);
			String time = new Timestamp(new Date().getTime()).toString();
			for (int p = 0; p < PS.length; p++) {
				if (OUTPUT_TYPE[p] && PS[p] != null) {
					PS[p].println(time + "\n" + NAME);
				}
			}
			openOutputFiles();
			ArrayList<BEDCoord> BED_Coord = loadCoord(BED);

			for (int y = 0; y < BED_Coord.size(); y++) {
				try {
					String seq = new String(QUERY.getSubsequenceAt(BED_Coord.get(y).getChrom(),
							BED_Coord.get(y).getStart() + 1, BED_Coord.get(y).getStop()).getBases()).toUpperCase();
					if (!seq.contains("N")) {
						if (STRAND && BED_Coord.get(y).getDir().equals("-")) {
							seq = FASTAUtilities.RevComplement(seq);
						}
						// Populate array for each BED file
						List<Double> MGW = new ArrayList<Double>();
						List<Double> PropT = new ArrayList<Double>();
						List<Double> HelT = new ArrayList<Double>();
						List<Double> Roll = new ArrayList<Double>();
						for (int z = 0; z < seq.length() - 4; z++) {
							String key = seq.substring(z, z + 5);
							List<Double> SCORES = STRUCTURE.get(key);
							if (OUTPUT_TYPE[0]) {
								MGW.add(SCORES.get(0));
							}
							if (OUTPUT_TYPE[1]) {
								PropT.add(SCORES.get(1));
							}
							if (OUTPUT_TYPE[2]) {
								if (z == 0) {
									HelT.add(SCORES.get(2));
									HelT.add(SCORES.get(3));
								} else {
									HelT.set(HelT.size() - 1, (HelT.get(HelT.size() - 1) + SCORES.get(2)) / 2);
									HelT.add(SCORES.get(3));
								}
							}
							if (OUTPUT_TYPE[3]) {
								if (z == 0) {
									Roll.add(SCORES.get(4));
									Roll.add(SCORES.get(5));
								} else {
									Roll.set(Roll.size() - 1, (Roll.get(Roll.size() - 1) + SCORES.get(4)) / 2);
									Roll.add(SCORES.get(5));
								}
							}
						} // Move through seq by window

						if (OUTPUT_TYPE[0]) {
							if (y == 0) {
								// Don't print matrix info if user specifies no matrix output
								if (OUTPUT_MATRIX != DNAShapefromBED.NO_MATRIX) {
									// print header
									OUT_M.print("YORF");
									if (OUTPUT_MATRIX == DNAShapefromBED.CDT) {
										OUT_M.print("\tNAME");
									}
									// print domain
									for (int z = 0; z < MGW.size(); z++) {
										OUT_M.print("\t" + z);
									}
									OUT_M.println();
								}
								// Initialize AVG storage object
								AVG_MGW = new double[MGW.size()];
							}
							// print matrix data and store avg data
							AVG_MGW = printVals(BED_Coord.get(y), MGW, AVG_MGW, OUT_M);
						}
						if (OUTPUT_TYPE[1]) {
							if (y == 0) {
								// Don't print matrix info if user specifies no matrix output
								if (OUTPUT_MATRIX != DNAShapefromBED.NO_MATRIX) {
									// print header
									OUT_P.print("YORF");
									if (OUTPUT_MATRIX == DNAShapefromBED.CDT) {
										OUT_P.print("\tNAME");
									}
									// print domain
									for (int z = 0; z < PropT.size(); z++) {
										OUT_P.print("\t" + z);
									}
									OUT_P.println();
								}
								// Initialize AVG storage object
								AVG_PropT = new double[PropT.size()];
							}
							// print matrix data and store avg data
							AVG_PropT = printVals(BED_Coord.get(y), PropT, AVG_PropT, OUT_P);
						}
						if (OUTPUT_TYPE[2]) {
							if (y == 0) {
								// Don't print matrix info if user specifies no matrix output
								if (OUTPUT_MATRIX != DNAShapefromBED.NO_MATRIX) {
									// print header
									OUT_H.print("YORF");
									if (OUTPUT_MATRIX == DNAShapefromBED.CDT) {
										OUT_H.print("\tNAME");
									}
									// print domain
									for (int z = 0; z < HelT.size(); z++) {
										OUT_H.print("\t" + z);
									}
									OUT_H.println();
								}
								// Initialize AVG storage object
								AVG_HelT = new double[HelT.size()];
							}
							// print matrix data and store avg data
							AVG_HelT = printVals(BED_Coord.get(y), HelT, AVG_HelT, OUT_H);
						}
						if (OUTPUT_TYPE[3]) {
							if (y == 0) {
								// Don't print matrix info if user specifies no matrix output
								if (OUTPUT_MATRIX != DNAShapefromBED.NO_MATRIX) {
									// print header
									OUT_R.print("YORF");
									if (OUTPUT_MATRIX == DNAShapefromBED.CDT) {
										OUT_R.print("\tNAME");
									}
									// print domain
									for (int z = 0; z < Roll.size(); z++) {
										OUT_R.print("\t" + z);
									}
									OUT_R.println();
								}
								// Initialize AVG storage object
								AVG_Roll = new double[Roll.size()];
							}
							// print matrix data and store avg data
							AVG_Roll = printVals(BED_Coord.get(y), Roll, AVG_Roll, OUT_R);
						}
					} // if seq contains 'N'
				} catch (SAMException e) {
					for (int p = 0; p < PS.length; p++) {
						if (OUTPUT_TYPE[p] && PS[p] != null) {
							PS[p].println("INVALID COORDINATE: " + BED_Coord.get(y).toString());
						}
					}
				}
			}
			QUERY.close();
			if (OUT_M != null) { OUT_M.close(); }
			if (OUT_P != null) { OUT_P.close(); }
			if (OUT_H != null) { OUT_H.close(); }
			if (OUT_R != null) { OUT_R.close(); }

			// Convert average and statistics to output tabs panes
			if (OUTPUT_TYPE[0]) {
				double[] DOMAIN_MGW = new double[AVG_MGW.length];
				int temp = (int) (((double) AVG_MGW.length / 2.0) + 0.5);
				for (int z = 0; z < AVG_MGW.length; z++) {
					DOMAIN_MGW[z] = (double) (temp - (AVG_MGW.length - z));
					AVG_MGW[z] /= BED_Coord.size();
					if (PS[0] != null) {
						PS[0].println(DOMAIN_MGW[z] + "\t" + AVG_MGW[z]);
					}
				}
				chart_M = CompositePlot.createCompositePlot(DOMAIN_MGW, AVG_MGW, NAME + " MGW");
				// Write output composite file
				if (OUTPUT_COMPOSITE) {
					PrintStream COMPOSITE = new PrintStream(new File(OUTBASENAME + "_MGW-Composite.out"));
					for (int z = 0; z < AVG_MGW.length; z++) {
						COMPOSITE.print("\t" + DOMAIN_MGW[z]);
					}
					COMPOSITE.println();
					COMPOSITE.print(NAME + "MGW-Composite");
					for (int z = 0; z < AVG_MGW.length; z++) {
						COMPOSITE.print("\t" + AVG_MGW[z]);
					}
					COMPOSITE.println();
				}
			}
			if (OUTPUT_TYPE[1]) {
				double[] DOMAIN_PropT = new double[AVG_PropT.length];
				int temp = (int) (((double) AVG_PropT.length / 2.0) + 0.5);
				for (int z = 0; z < AVG_PropT.length; z++) {
					DOMAIN_PropT[z] = (double) (temp - (AVG_PropT.length - z));
					AVG_PropT[z] /= BED_Coord.size();
					if (PS[1] != null) {
						PS[1].println(DOMAIN_PropT[z] + "\t" + AVG_PropT[z]);
					}
				}
				chart_P = CompositePlot.createCompositePlot(DOMAIN_PropT, AVG_PropT, NAME + " PropT");
				// Write output composite file
				if (OUTPUT_COMPOSITE) {
					PrintStream COMPOSITE = new PrintStream(new File(OUTBASENAME + "_PropT-Composite.out"));
					for (int z = 0; z < AVG_PropT.length; z++) {
						COMPOSITE.print("\t" + DOMAIN_PropT[z]);
					}
					COMPOSITE.println();
					COMPOSITE.print(NAME + "PropT-Composite");
					for (int z = 0; z < AVG_PropT.length; z++) {
						COMPOSITE.print("\t" + AVG_PropT[z]);
					}
					COMPOSITE.println();
				}
			}
			if (OUTPUT_TYPE[2]) {
				double[] DOMAIN_HelT = new double[AVG_HelT.length];
				int temp = (int) (((double) AVG_HelT.length / 2.0) + 0.5);
				for (int z = 0; z < AVG_HelT.length; z++) {
					DOMAIN_HelT[z] = (double) (temp - (AVG_HelT.length - z));
					AVG_HelT[z] /= BED_Coord.size();
					if (PS[2] != null) {
						PS[2].println(DOMAIN_HelT[z] + "\t" + AVG_HelT[z]);
					}
				}
				chart_H = CompositePlot.createCompositePlot(DOMAIN_HelT, AVG_HelT, NAME + " HelT");
				// Write output composite file
				if (OUTPUT_COMPOSITE) {
					PrintStream COMPOSITE = new PrintStream(new File(OUTBASENAME + "_HelT-Composite.out"));
					for (int z = 0; z < AVG_HelT.length; z++) {
						COMPOSITE.print("\t" + DOMAIN_HelT[z]);
					}
					COMPOSITE.println();
					COMPOSITE.print(NAME + "HelT-Composite");
					for (int z = 0; z < AVG_HelT.length; z++) {
						COMPOSITE.print("\t" + AVG_HelT[z]);
					}
					COMPOSITE.println();
				}
			}
			if (OUTPUT_TYPE[3]) {
				double[] DOMAIN_Roll = new double[AVG_Roll.length];
				int temp = (int) (((double) AVG_Roll.length / 2.0) + 0.5);
				for (int z = 0; z < AVG_Roll.length; z++) {
					DOMAIN_Roll[z] = (double) (temp - (AVG_Roll.length - z));
					AVG_Roll[z] /= BED_Coord.size();
					if (PS[3] != null) {
						PS[3].println(DOMAIN_Roll[z] + "\t" + AVG_Roll[z]);
					}
				}
				chart_R = CompositePlot.createCompositePlot(DOMAIN_Roll, AVG_Roll, NAME + " Roll");
				// Write output composite file
				if (OUTPUT_COMPOSITE) {
					PrintStream COMPOSITE = new PrintStream(new File(OUTBASENAME + "_Roll-Composite.out"));
					for (int z = 0; z < AVG_Roll.length; z++) {
						COMPOSITE.print("\t" + DOMAIN_Roll[z]);
					}
					COMPOSITE.println();
					COMPOSITE.print(NAME + "Roll-Composite");
					for (int z = 0; z < AVG_Roll.length; z++) {
						COMPOSITE.print("\t" + AVG_Roll[z]);
					}
					COMPOSITE.println();
				}
			}
	}

	/**
	 * Getter method for the swing component chart of the Minor Groove Width DNA
	 * shape type.
	 * 
	 * @return the chart for Minor Groove Width
	 */
	public Component getChartM() {
		return chart_M;
	}

	/**
	 * Getter method for the swing component chart of the Propeller Twist DNA shape type.
	 * 
	 * @return the chart for Propeller Twist
	 */
	public Component getChartP() {
		return chart_P;
	}

	/**
	 * Getter method for the swing component chart of the Helical Twist DNA shape
	 * type.
	 * 
	 * @return the chart for Helical Twist
	 */
	public Component getChartH() {
		return chart_H;
	}

	/**
	 * Getter method for the swing component chart of the Roll DNA shape type.
	 * 
	 * @return the chart for Roll
	 */
	public Component getChartR() {
		return chart_R;
	}

	/**
	 * Getter method for average scores of each DNA shape type.
	 * 
	 * @param shapeType indicate shape type to return (0=MGW, 1=PropT, 2=HelT, 3=Roll).
	 * @return the array of shape scores
	 */
	public double[] getAvg(int shapeType) {
		if (shapeType == 0) {
			return AVG_MGW;
		} else if (shapeType == 1) {
			return AVG_PropT;
		} else if (shapeType == 2) {
			return AVG_HelT;
		} else if (shapeType == 3) {
			return AVG_Roll;
		} else {
			return null;
		}
	}

	/**
	 * Parse a BED-formatted file to load all coordinates into memory as a list of
	 * BEDCoord objects.
	 * 
	 * @param INPUT a BED-formatted file
	 * @return the parsed BED coordinate objects
	 * @throws FileNotFoundException Script could not find valid input file
	 * @throws IOException
	 */
	public ArrayList<BEDCoord> loadCoord(File INPUT) throws FileNotFoundException, IOException {
		String line;
		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(INPUT);
		ArrayList<BEDCoord> COORD = new ArrayList<BEDCoord>();
		while ((line = br.readLine()) != null) {
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
						if (temp[5].equals("+")) {
							COORD.add(new BEDCoord(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), "+",
									name));
						} else {
							COORD.add(new BEDCoord(temp[0], Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), "-",
									name));
						}
					} else {
						System.out.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
					}
				}
			}
		}
		br.close();
		return COORD;
	}

	/**
	 * Initialize output PrintStream objects for each DNA shape as needed.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void openOutputFiles() throws FileNotFoundException, IOException {
		if (OUTBASENAME == null) {
			OUTBASENAME = new File(ExtensionFileFilter.stripExtension(BED));
		}
		// Open Output File
		if (OUTPUT_MATRIX > 0) {
			String SUFFIX = (OUTPUT_MATRIX==DNAShapefromBED.CDT ? ".cdt" : ".tab") + (GZIP_OUTPUT? ".gz": "");
			if (OUTPUT_TYPE[0]) {
				OUT_M = GZipUtilities.makePrintStream(new File(OUTBASENAME + "_MGW" + SUFFIX), GZIP_OUTPUT);
			}
			if (OUTPUT_TYPE[1]) {
				OUT_P = GZipUtilities.makePrintStream(new File(OUTBASENAME + "_PropT" + SUFFIX), GZIP_OUTPUT);
			}
			if (OUTPUT_TYPE[2]) {
				OUT_H = GZipUtilities.makePrintStream(new File(OUTBASENAME + "_HelT" + SUFFIX), GZIP_OUTPUT);
			}
			if (OUTPUT_TYPE[3]) {
				OUT_R = GZipUtilities.makePrintStream(new File(OUTBASENAME + "_Roll" + SUFFIX), GZIP_OUTPUT);
			}
		}
	}

	/**
	 * Print a row of scores in a tab-delimited manner using the CDT format with the
	 * BEDCoord name/id string occupying the first two tokens (or "columns"). Each
	 * score is simultaneously added to the AVG array in the matching position
	 * (parallel arrays).
	 * 
	 * @param b      the coordinate object whose name/id attribute is used for the
	 *               header
	 * @param SCORES an array of scores to print to a line
	 * @param AVG    an array with the same length as SCORES (if not longer)
	 * @param O      destination to print the line to
	 * @return SCORES that have been element-wise summed with AVG
	 */
	private double[] printVals(BEDCoord b, List<Double> SCORES, double[] AVG, PrintStream O) {
		// print header
		if (O != null) {
			O.print(b.getName());
			if (OUTPUT_MATRIX == DNAShapefromBED.CDT) {
				O.print("\t" + b.getName());
			}
		}
		for (int z = 0; z < SCORES.size(); z++) {
			// print values
			if (O != null) { O.print("\t" + SCORES.get(z)); }
			// build avg
			AVG[z] += SCORES.get(z);
		}
		// print new line
		if (O != null) { O.println(); }
		// return avg
		return (AVG);
	}
}