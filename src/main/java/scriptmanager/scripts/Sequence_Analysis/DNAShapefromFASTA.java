package scriptmanager.scripts.Sequence_Analysis;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;

import scriptmanager.charts.CompositePlot;
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
	private HashMap<Integer, double[][]> AVG_ARRS;
	private ArrayList<Integer> OUTPUT_TYPES;

	private HashMap<Integer, CustomOutputStream> PS = null;
	static Map<String, List<Double>> STRUCTURE = null;
	private HashMap<Integer, Component> CHARTS = null;
	private DecimalFormat FORMAT = new DecimalFormat("##.00");

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
		CHARTS = new HashMap<>();
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
		
		// Print time to ScriptManager gui
		String NAME = ExtensionFileFilter.stripExtension(FASTA);
		String time = new Timestamp(new Date().getTime()).toString();
		if (PS != null){
			for (Integer shape: OUTPUT_TYPES){
				PS.get(shape).write((time + "\n" + NAME + "\n").getBytes(Charset.forName("UTF-8")));
			}
		}
		openOutputFiles();

		// Find longest sequence in file
		String line;
		int longestSequence = -1;
		BufferedReader br = GZipUtilities.makeReader(FASTA);
		while ((line = br.readLine()) != null) { 
			if (!line.contains(">")) {
				longestSequence = Math.max(longestSequence, line.length());
			}
		}
		br.close();
		// Create coordinate domain
		double[] domain = new double[0];
		int numPredictions = (1 + (int)(longestSequence - 4));
		domain = new double[numPredictions];
		int temp = (int) (((double) (numPredictions) / 2.0) + 0.5);
		for (int z = 0; z < numPredictions; z++) {
			domain[z] = (temp - (numPredictions - z));
		}

		int counter = 0;
		br = GZipUtilities.makeReader(FASTA);
		while ((line = br.readLine()) != null) {
			String HEADER = line;
			if (HEADER.contains(">")) {
				HEADER = HEADER.substring(1, HEADER.length());
				String seq = br.readLine();
				if (!seq.contains("N")) {
					// Populate array for each shape type
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
								// Adjust domain to fit number of predictions
								int start;
								if (new ArrayList<>(Arrays.asList( DNAShapeReference.HELT, DNAShapeReference.ROLL, DNAShapeReference.RISE,
								DNAShapeReference.SHIFT, DNAShapeReference.TILT, DNAShapeReference.SLIDE)).contains(shape)){
									start = 0;
								} else {
									start = 1;
								}
								for (int z = start; z < numPredictions; z++) {
									OUT_FILES.get(shape).print("\t" + domain[z]);
								}
								OUT_FILES.get(shape).println();
							}
							// Initialize AVG storage object
							double[][] avg = new double[2][numPredictions + 1];
							AVG_ARRS.put(shape, avg);
						}
						// Adjust domain to fit number of predictions
						if (new ArrayList<>(Arrays.asList( DNAShapeReference.HELT, DNAShapeReference.ROLL, DNAShapeReference.RISE,
							DNAShapeReference.SHIFT, DNAShapeReference.TILT, DNAShapeReference.SLIDE)).contains(shape)){
							temp = numPredictions + 1;
						} else {
							temp = numPredictions;
						}
						//Print values and save output as ArrayList
						AVG_ARRS.put(shape, printVals(HEADER, PREDICTIONS.get(shape), AVG_ARRS.get(shape), OUT_FILES.get(shape), temp));
						}
					} // if seq contains 'N's
					counter++;
				} else {
					throw new ScriptManagerException("Invalid FASTA sequence (" + HEADER + ")...DNAShape does not support N-containing sequences");
				}
			}
			br.close();
			if (OUT_FILES.keySet().size() > 0){
				for (Integer shape: OUTPUT_TYPES){
					OUT_FILES.get(shape).close();
				}
			}
			// Calculate averages based on number of scores at each coordinate
			for (int shape : OUTPUT_TYPES) {
				double[] totals = AVG_ARRS.get(shape)[0];
				double[] counts = AVG_ARRS.get(shape)[1];
				double[][] averages;
				if (counts[counts.length - 2] == 0){
					averages = new double[1][numPredictions - 1];
				} else {
					averages = new double[1][numPredictions];
				}
				for (int i = 0; i < totals.length; i++){
					if (counts[i] != 0){
						averages[0][i] = totals[i] / counts[i];
					}
				}
				AVG_ARRS.put(shape, averages);
			}

			// Output averages to composite files
			if (OUTPUT_COMPOSITE){
				for (Integer shape: OUTPUT_TYPES){
					double[] scores = AVG_ARRS.get(shape)[0];
					// Adjust domain to fit number of predictions
					int start;
					if (new ArrayList<>(Arrays.asList( DNAShapeReference.HELT, DNAShapeReference.ROLL, DNAShapeReference.RISE,
					DNAShapeReference.SHIFT, DNAShapeReference.TILT, DNAShapeReference.SLIDE)).contains(shape)){
						start = 0;
					} else {
						start = 1;
					}
					for (int z = start; z < numPredictions; z++) {
						COMPOSITE_FILES.get(shape).print("\t" + domain[z]);
					}
					COMPOSITE_FILES.get(shape).println();
					COMPOSITE_FILES.get(shape).print(NAME + "-" + DNAShapeReference.HEADERS[shape] + "-Composite");
					for (int z = 0; z < scores.length; z++){
						COMPOSITE_FILES.get(shape).print("\t" + FORMAT.format(scores[z]));
					}
				}
				for (Integer shape: OUTPUT_TYPES){
					COMPOSITE_FILES.get(shape).close();
				}
			}
			// Output averages to GUI
			if (PS != null){
				for (Integer shape: OUTPUT_TYPES){
					// Convert arraylist to array
					double[] scores = AVG_ARRS.get(shape)[0];
					System.out.println(Arrays.toString(scores));
					// Make create domain for composite plot
					double[] truncatedDomain = new double[scores.length];
					for (int z = 1; z <= scores.length; z++){
						truncatedDomain[truncatedDomain.length - z] = domain[domain.length - z];
					}
					for (int z = 0; z < scores.length; z++){
						PS.get(shape).write((truncatedDomain[z] + "\t" + FORMAT.format(scores[z]) + "\n").getBytes(Charset.forName("UTF-8")));
					}
					// Create composite plot
					CHARTS.put(shape, CompositePlot.createCompositePlot(truncatedDomain, scores, NAME + " " + DNAShapeReference.HEADERS[shape]));
				}
			}
		}

	/**
	 * Getter for specific chart based on shape ID
	 * @param shape shape to retrieve chart for
	 * @return the specific chart based on shape ID
	 */
	public Component getCharts(Integer shape){
		return CHARTS.get(shape);
	}

	/**
	 * Returns HashMap of all charts for all shapes
	 * @return HashMap of all charts for all shapes
	 */
	public HashMap<Integer, Component> getCharts(){
		return CHARTS;
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
	 * @param numPredictions Greatest number of predictions in output file
	 * @return SCORES that have been element-wise summed with AVG
	 */
	private double[][] printVals(String header, List<Double> SCORES, double[][] AVG, PrintStream O, int numPredictions) {
		// print header
		if (O != null) {
			O.print(header);
			if (OUTPUT_MATRIX == DNAShapefromBED.CDT) {
				O.print("\t" + header);
			}
		}
		// Calculate how many cells to skip on each side if the sequence is shorter than the longest sequence
		int buffer = Math.round((numPredictions - SCORES.size()) / 2);
		for (int z = 0; z < numPredictions - 1; z++) {
			// Stopping and starting point for outputs. If number of predictions is even, the median of coordinate will be -0.5.  If odd the median coordinate will be 0.
			int stop = (SCORES.size() % 2 == 0)? numPredictions - buffer: numPredictions - buffer - 1;
			int start = buffer;
			int index = z - buffer;
			// Print score if available or 'Nan'.  Save output to AVG array
			if (z >= start && z < stop && index < SCORES.size()){
				if (O != null) { O.print("\t" + FORMAT.format(SCORES.get(index))); }
				AVG[0][z] += SCORES.get(index);
				AVG[1][z] += 1;
			} else {
				if (O != null) { O.print("\tNan"); };
			}
		}
		// print new line
		if (O != null) { O.println(); }
		// return avg
		return (AVG);
	}
}