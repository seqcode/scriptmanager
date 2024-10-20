package scriptmanager.scripts.Sequence_Analysis;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scriptmanager.charts.CompositePlot;
import scriptmanager.cli.Sequence_Analysis.DNAShapefromFASTACLI.ShapeType;
import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.objects.Exceptions.ScriptManagerException;
import scriptmanager.util.GZipUtilities;
import scriptmanager.util.DNAShapeReference;
import scriptmanager.util.ExtensionFileFilter;

/**
 * Calculate and score various aspects of DNA shape across a set of FASTA
 * sequences.
 * 
 * @author William KM Lai
 * @see scriptmanager.util.DNAShapeReference
 * @see scriptmanager.cli.Sequence_Analysis.DNAShapefromFASTACLI
 * @see scriptmanager.window_interface.Sequence_Analysis.DNAShapefromFASTAOutput
 * @see scriptmanager.window_interface.Sequence_Analysis.DNAShapefromFASTAWindow
 */
public class DNAShapefromFASTA {
	private File FASTA = null;
	private File OUTBASENAME = null;

	private boolean OUTPUT_COMPOSITE = false;
	private short OUTPUT_MATRIX = 0;
	private boolean GZIP_OUTPUT;

	private HashMap<Integer, PrintStream> OUT_FILES;
	private HashMap<Integer, PrintStream> COMPOSITE_FILES;
	private HashMap<Integer, ArrayList<Double>> PREDICTIONS;
	private HashMap<Integer, ArrayList<Double>> AVG_ARRS;
	private HashMap<Integer, ArrayList<Double>> TEXT_ARRS;
	private ArrayList<Integer> OUTPUT_TYPES;

	private ArrayList<PrintStream> PS = null;

	static Map<String, List<Double>> STRUCTURE = null;

	Component chart_M = null;
	Component chart_P = null;
	Component chart_H = null;
	Component chart_R = null;

	public final static short NO_MATRIX = 0;
	public final static short TAB = 1;
	public final static short CDT = 2;

	public DNAShapefromFASTA(File input, File out, ArrayList<Integer> type, boolean outputComposite, short outputMatrix, boolean gzOutput) {
		FASTA = input;
		OUTBASENAME = out;
		OUTPUT_TYPES = type;
		OUTPUT_COMPOSITE = outputComposite;
		OUTPUT_MATRIX = outputMatrix;
		GZIP_OUTPUT = gzOutput;
		PS = null;

		STRUCTURE = DNAShapeReference.InitializeStructure();
		OUT_FILES = new HashMap<>();
		COMPOSITE_FILES = new HashMap<>();
		AVG_ARRS = new HashMap<>();
		PREDICTIONS = new HashMap<>();
		TEXT_ARRS = new HashMap<>();
	}

	/**
	 * Initialize object with script inputs for generating DNA shape reports.
	 * 
	 * @param input           the FASTA-formatted sequence to calculate shape for
	 * @param out             the output file name base (to add
	 *                        _&lt;shapetype&gt;.cdt suffix to)
	 * @param type            a four-element boolean list for specifying shape type
	 *                        to output (no enforcement on size) [MGW, PropT, HelT, Roll, EP, Stretch, Buckle, Shear, Opening, Stagger, Tilt, Slide, Rise]
	 * @param outputComposite whether to output a composite average output
	 * @param outputMatrix    value encoding not to write output matrix data, write
	 *                        matrix in CDT format, and write matrix in tab format
	 * @param gzOutput        whether to output compressed file
	 * @param ps              list of four PrintStream objects corresponding to each
	 *                        shape type (for GUI)
	 * @throws IOException Invalid file or parameters
	 */
	public DNAShapefromFASTA(File input, File out, ArrayList<Integer> type, boolean outputComposite, short outputMatrix, boolean gzOutput, HashMap<Integer, CustomOutputStream> ps) {
		FASTA = input;
		OUTBASENAME = out;
		OUTPUT_TYPES = type;
		OUTPUT_COMPOSITE = outputComposite;
		OUTPUT_MATRIX = outputMatrix;
		GZIP_OUTPUT = gzOutput;
		PS = ps;

		STRUCTURE = DNAShapeReference.InitializeStructure();
		OUT_FILES = new HashMap<>();
		COMPOSITE_FILES = new HashMap<>();
		AVG_ARRS = new HashMap<>();
		PREDICTIONS = new HashMap<>();
		TEXT_ARRS = new HashMap<>();
	}

	/**
	 * Execute script to calculate DNA shape for all types across the input
	 * sequence.
	 * 
	 * @throws ScriptManagerException thrown when FASTA parsing encounters N-containing sequence
	 * @throws FileNotFoundException
	 * @throws IOException Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the same time
	 * @throws OptionException 
	 */
	public void run() throws ScriptManagerException, FileNotFoundException, IOException, InterruptedException, OptionException {
		String NAME = ExtensionFileFilter.stripExtension(FASTA);
		String time = new Timestamp(new Date().getTime()).toString();
		for (int p = 0; p < PS.length; p++) {
			if (PS[p] != null) {
				PS[p].println(time + "\n" + NAME);
			}
		}
		openOutputFiles();

		int counter = 0;
		String line;
		int longestSequence = -1;
		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(FASTA);
		while ((line = br.readLine()) != null) { 
			if (!line.contains(">")) {
				longestSequence = Math.max(longestSequence, line.length());
			}
		}
		br.close();
		br = GZipUtilities.makeReader(FASTA);
		while ((line = br.readLine()) != null) {
			String HEADER = line;
			if (HEADER.contains(">")) {
				HEADER = HEADER.substring(1, HEADER.length());
				String seq = br.readLine();
				if (!seq.contains("N")) {
					// Populate array for each FASTA line
					for (Integer shape: OUTPUT_TYPES){
						PREDICTIONS.put(shape, DNAShapeReference.seqToShape(shape, seq));
					}
					for (Integer shape: OUTPUT_TYPES){
						if (counter == 0) {
							// Don't print matrix info if user specifies no matrix output
							if (OUTPUT_MATRIX != DNAShapefromBED.NO_MATRIX) {
								// print header
								OUT_FILES.get(shape).print("YORF");
								if (OUTPUT_MATRIX == DNAShapefromBED.CDT) {
									OUT_FILES.get(shape).print("\tNAME");
								}
								// print domain
								for (int z = 0; z < longestSequence; z++) {
									OUT_FILES.get(shape).print("\t" + z);
								}
								OUT_FILES.get(shape).println();
							}
							// Initialize AVG storage object
							ArrayList<Double> avg = new ArrayList<Double>(PREDICTIONS.get(shape).size());
							AVG_ARRS.put(shape, avg);
						}
						AVG_ARRS.put(shape, printVals(HEADER, PREDICTIONS.get(shape), AVG_ARRS.get(shape), OUT_FILES.get(shape)));
					}
					} // if seq contains 'N's
					counter++;
				} else {
					throw new ScriptManagerException("Invalid FASTA sequence (" + HEADER + ")...DNAShape does not support N-containing sequences");
				}
			}
			br.close();
			for (Integer shape: OUTPUT_TYPES){
				OUT_FILES.get(shape).close();
			}
			// Print average arrays
			if (OUTPUT_COMPOSITE){
				for (Integer shape: OUTPUT_TYPES){
					for (int z = 0; z < AVG_ARRS.get(shape).size(); z++){
						COMPOSITE_FILES.get(shape).print("\t" + z);
					}
					COMPOSITE_FILES.get(shape).println();
					COMPOSITE_FILES.get(shape).print(NAME + "-" + DNAShapeReference.HEADERS[shape] + "-Composite");
					for (int z = 0; z < AVG_ARRS.get(shape).size(); z++){
						COMPOSITE_FILES.get(shape).print("\t" + AVG_ARRS.get(shape).get(z) / counter);
					}
					COMPOSITE_FILES.get(shape);
				}
				for (Integer shape: OUTPUT_TYPES){
					COMPOSITE_FILES.get(shape).close();
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
	 * Initialize output PrintStream objects for each DNA shape as needed.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void openOutputFiles() throws FileNotFoundException, IOException {
		if (OUTBASENAME == null) {
			OUTBASENAME = new File(ExtensionFileFilter.stripExtension(FASTA));
		}
		// Open Output File
		if (OUTPUT_MATRIX > 0) {
			String SUFFIX = (OUTPUT_MATRIX==DNAShapefromBED.CDT ? ".cdt" : ".tab") + (GZIP_OUTPUT? ".gz": "");
			for (Integer shape: OUTPUT_TYPES){
				OUT_FILES.put(shape, GZipUtilities.makePrintStream(new File(OUTBASENAME + "_" + DNAShapeReference.HEADERS[shape] + SUFFIX), GZIP_OUTPUT));
			}
			if (OUTPUT_COMPOSITE){
				for (Integer shape: OUTPUT_TYPES){
					COMPOSITE_FILES.put(shape, GZipUtilities.makePrintStream(new File(OUTBASENAME + "_" + DNAShapeReference.HEADERS[shape] + "-Composite" + SUFFIX), GZIP_OUTPUT));
				}
			}
		}
	}

	/**
	 * Print a row of scores in a tab-delimited manner using the CDT format with the
	 * header string occupying the first two tokens (or "columns"). Each score is
	 * simultaneously added to the AVG array in the matching position (parallel
	 * arrays).
	 * 
	 * @param header the string to print for the first two tab-delimited tokens
	 *               preceeding the scores
	 * @param SCORES an array of scores to print to a line
	 * @param AVG    an array with the same length as SCORES (if not longer)
	 * @param O      destination to print the line to
	 * @return SCORES that have been element-wise summed with AVG
	 */
	private ArrayList<Double> printVals(String header, List<Double> SCORES, ArrayList<Double> AVG, PrintStream O) {
		// print header
		if (O != null) {
			O.print(header);
			if (OUTPUT_MATRIX == DNAShapefromBED.CDT) {
				O.print("\t" + header);
			}
		}
		for (int z = 0; z < SCORES.size(); z++) {
			// print values
			if (O != null) { O.print("\t" + SCORES.get(z)); }
			// build avg and avoid index out of bounds
			if (z < AVG.size()){
				AVG.set(z, AVG.get(z) + SCORES.get(z));
			}
			else {
				AVG.add(z, SCORES.get(z));
			}
		}
		// print new line
		if (O != null) { O.println(); }
		// return avg
		return (AVG);
	}
}