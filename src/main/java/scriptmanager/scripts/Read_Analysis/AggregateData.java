package scriptmanager.scripts.Read_Analysis;

import java.io.BufferedReader;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import scriptmanager.cli.Read_Analysis.AggregateDataCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.objects.Exceptions.ScriptManagerException;
import scriptmanager.util.ArrayUtilities;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.GZipUtilities;

/**
 * Combine multiple TAB/CDT files into site-specific scores using a given
 * operation
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Read_Analysis.AggregateDataCLI
 * @see scriptmanager.window_interface.Read_Analysis.AggregateDataWindow
 */
public class AggregateData extends Component {

	private static final long serialVersionUID = 1L;
	public static final short SUM = 0;
	public static final short AVERAGE = 1;
	public static final short MEDIAN = 2;
	public static final short MODE = 3;
	public static final short MINIMUM = 4;
	public static final short MAXIMUM = 5;
	public static final short POSITIONAL_VARIANCE = 6;

	private ArrayList<File> INPUT = null;
	private File OUT_PATH = null;
	private boolean MERGE = true;
	private int ROWSTART = 1;
	private int COLSTART = 1;
	private int METRIC = 0;
	private boolean OUTPUT_GZIP;

	/**
	 * Creates a new instance of the AggregateData script
	 * 
	 * @param in    ArrayList of TAB files to be processed
	 * @param out   Output directory
	 * @param m     Whether results should be merged into one file
	 * @param r     Starting row (1-indexed)
	 * @param c     Starting column (1-indexed)
	 * @param index Operation to be performed (0 = sum, 1 = average, 2 = median, 3 =
	 *              mode, 4 = min, 5 = max, 6 = positional variance)
	 * @param gzOutput   whether or not to gzip output
	 */
	public AggregateData(ArrayList<File> in, File out, boolean m, int r, int c, int index, boolean gzOutput) {
		INPUT = in;
		OUT_PATH = out;
		MERGE = m;
		ROWSTART = r;
		COLSTART = c;
		METRIC = index;
		OUTPUT_GZIP = gzOutput;
	}

	/**
	 * Runs the aggregation with the specified parameters
	 * 
	 * @throws IOException Invalid file or parameters
	 */
	public void run() throws IOException, OptionException, ScriptManagerException {
		// One-to-one style aggregate data
		if (!MERGE) {
			// If output is null or directory
			if (OUT_PATH == null || OUT_PATH.isDirectory()) {
				for (int x = 0; x < INPUT.size(); x++) {
					outputFileScore(INPUT.get(x));
 					firePropertyChange("progress", x, x + 1);
				}
			} else if (INPUT.size() == 1) {
				PrintStream OUT = GZipUtilities.makePrintStream(OUT_PATH, OUTPUT_GZIP);
				outputFileScore(INPUT.get(0), OUT);
			} else {
				throw new OptionException("Cannot accept non-directory filename with multi-file input when merge is not flagged.");
			}
		// Merge-style aggregate data
		} else {
			ArrayList<ArrayList<Double>> MATRIX = new ArrayList<ArrayList<Double>>();
			ArrayList<ArrayList<String>> MATRIXID = new ArrayList<ArrayList<String>>();
			for (int x = 0; x < INPUT.size(); x++) {
				// Check if file is gzipped and instantiate appropriate BufferedReader
				BufferedReader br = GZipUtilities.makeReader(INPUT.get(x));
				ArrayList<Double> scorearray = new ArrayList<Double>();
				ArrayList<String> idarray = new ArrayList<String>();
				int count = 0;
				String line;
				while ((line = br.readLine()) != null) {
					// Skip lines until desired row start
					while (count < ROWSTART) {
						line = br.readLine();
						count++;
					}

					// Split array, assume first element is ID, irrespective of column start
					String[] ID = line.split("\t");
					idarray.add(ID[0]);

					double[] numarray = new double[ID.length];
					for (int y = COLSTART - 1; y < ID.length; y++) {
						try {
							numarray[y] = Double.parseDouble(ID[y]);
						} catch (NumberFormatException nfe) {
							numarray[y] = Double.NaN;
						}
					}
					if (METRIC == SUM) {
						scorearray.add(ArrayUtilities.getSum(numarray));
					} else if (METRIC == AVERAGE) {
						scorearray.add(ArrayUtilities.getAverage(numarray));
					} else if (METRIC == MEDIAN) {
						scorearray.add(ArrayUtilities.getMedian(numarray));
					} else if (METRIC == MODE) {
						scorearray.add(ArrayUtilities.getMode(numarray));
					} else if (METRIC == MINIMUM) {
						scorearray.add(ArrayUtilities.getMin(numarray));
					} else if (METRIC == MAXIMUM) {
						scorearray.add(ArrayUtilities.getMax(numarray));
					} else if (METRIC == POSITIONAL_VARIANCE) {
						scorearray.add(ArrayUtilities.getPositionalVariance(numarray));
					}
					count++;
				}
				br.close();
				MATRIX.add(scorearray);
				MATRIXID.add(idarray);
			}

			// Check all arrays are the same size
			int ARRAYLENGTH = MATRIX.get(0).size();
			boolean ALLSAME = true;
			for (int x = 0; x < MATRIX.size(); x++) {
				if (MATRIX.get(x).size() != ARRAYLENGTH || MATRIXID.get(x).size() != ARRAYLENGTH) {
					throw new ScriptManagerException("Different number of rows between:\n" + INPUT.get(0).getName() + "\n" + INPUT.get(x).getName());
				}
			}
			// (will not proceed if exception thrown above)
			// Construct output stream
			PrintStream OUT;
			String name = "ALL_SCORES.out" + (OUTPUT_GZIP? ".gz": "");
			if (OUT_PATH == null) {
				OUT = GZipUtilities.makePrintStream(new File(name), OUTPUT_GZIP);
			} else if (!OUT_PATH.isDirectory()) {
				OUT = GZipUtilities.makePrintStream(OUT_PATH, OUTPUT_GZIP);
			} else {
				OUT = GZipUtilities.makePrintStream(new File(OUT_PATH.getCanonicalPath() + File.separator + name), OUTPUT_GZIP);
			}
			// Print matrix for elements that are the same size
			if (ALLSAME) {
				for (int x = 0; x < INPUT.size(); x++) {
					OUT.print("\t" + INPUT.get(x).getName());
				}
				OUT.println();
				for (int x = 0; x < MATRIX.get(0).size(); x++) {
					OUT.print(MATRIXID.get(0).get(x));
					for (int y = 0; y < MATRIX.size(); y++) {
						OUT.print("\t" + MATRIX.get(y).get(x));
					}
					OUT.println();
				}
			}
			OUT.close();
		}
	}

	/**
	 * Calls {@link AggregateData#outputFileScore(File, PrintStream)} but outputs results to a new file if a PrintStream is not provided
	 * <br>
	 * Assumes OUTPUT_PATH is null or directory!
	 * 
	 * @param IN Input file (used to generate output file's name)
	 * @throws FileNotFoundException Script could not find valid input file
	 * @throws IOException Invalid file or parameters
	 */
	public void outputFileScore(File IN) throws FileNotFoundException, IOException {
		PrintStream OUT;
		String NAME = ExtensionFileFilter.stripExtensionIgnoreGZ(IN) + "_SCORES.out" + (OUTPUT_GZIP? ".gz": "");
		if (OUT_PATH != null) {
			OUT = GZipUtilities.makePrintStream(new File(OUT_PATH.getAbsolutePath() + File.separator + NAME), OUTPUT_GZIP);
		} else {
			OUT = GZipUtilities.makePrintStream(new File(NAME), OUTPUT_GZIP);
		}
		outputFileScore(IN, OUT);
	}

	/**
	 * Outputs the first value in a given row and the result of the selected operation for each line of a file
	 * @param IN TAB file to be scored
	 * @param OUT Print stream to output scores
	 * @throws FileNotFoundException Script could not find valid input file
	 * @throws IOException Invalid file or parameters
	 */
	public void outputFileScore(File IN, PrintStream OUT) throws FileNotFoundException, IOException {
		if (METRIC == SUM) {
			OUT.println("\tSum");
		} else if (METRIC == AVERAGE) {
			OUT.println("\tAverage");
		} else if (METRIC == MEDIAN) {
			OUT.println("\tMedian");
		} else if (METRIC == MODE) {
			OUT.println("\tMode");
		} else if (METRIC == MINIMUM) {
			OUT.println("\tMin");
		} else if (METRIC == MAXIMUM) {
			OUT.println("\tMax");
		} else if (METRIC == POSITIONAL_VARIANCE) {
			OUT.println("\tPositionalVariance");
		}

		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(IN);
		int count = 0;
		String line;
		while ((line = br.readLine()) != null) {
			// Skip lines until desired row start
			while (count < ROWSTART) {
				line = br.readLine();
				count++;
			}

			String[] ID = line.split("\t");
			double[] numarray = new double[ID.length];
			for (int x = COLSTART - 1; x < ID.length; x++) {
				try {
					numarray[x] = Double.parseDouble(ID[x]);
				} catch (NumberFormatException nfe) {
					numarray[x] = Double.NaN;
				}
			}
			if (METRIC == SUM) {
				OUT.println(ID[0] + "\t" + ArrayUtilities.getSum(numarray));
			} else if (METRIC == AVERAGE) {
				OUT.println(ID[0] + "\t" + ArrayUtilities.getAverage(numarray));
			} else if (METRIC == MEDIAN) {
				OUT.println(ID[0] + "\t" + ArrayUtilities.getMedian(numarray));
			} else if (METRIC == MODE) {
				OUT.println(ID[0] + "\t" + ArrayUtilities.getMode(numarray));
			} else if (METRIC == MINIMUM) {
				OUT.println(ID[0] + "\t" + ArrayUtilities.getMin(numarray));
			} else if (METRIC == MAXIMUM) {
				OUT.println(ID[0] + "\t" + ArrayUtilities.getMax(numarray));
			} else if (METRIC == POSITIONAL_VARIANCE) {
				OUT.println(ID[0] + "\t" + ArrayUtilities.getPositionalVariance(numarray));
			}
			count++;
		}
		br.close();
		OUT.close();
	}
}