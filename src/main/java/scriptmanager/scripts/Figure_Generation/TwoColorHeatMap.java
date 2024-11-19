package scriptmanager.scripts.Figure_Generation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import scriptmanager.util.GZipUtilities;

/**
 * Generate a two-color heatmap PNG from a matrix of values in a given text file
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Figure_Generation.TwoColorHeatMapCLI
 * @see scriptmanager.window_interface.Figure_Generation.TwoColorHeatMapOutput
 * @see scriptmanager.window_interface.Figure_Generation.TwoColorHeatMapWindow
 */
public class TwoColorHeatMap {

	protected static File SAMPLE = null;

	protected static int startROW = 1;
	protected static int startCOL = 2;
	protected static int pixelHeight = 600;
	protected static int pixelWidth = 200;

	protected static String scaleType = "treeview";
	protected static double quantile = 0.9;
	protected static double absolute = -999;

	public static Color MINCOLOR = new Color(255, 255, 255);
	public static Color MAXCOLOR = new Color(255, 0, 0);

	protected static boolean OUTPUTSTATUS = false;
	protected static File OUTFILE = null;
	protected static String FILEID = null;

	private static ArrayList<double[]> MATRIX = null;
	public static double COLOR_RATIO = 1;

	private JLabel picLabel = null;

	/**
	 * Creates a new instance of a TwoColorHeatMap with given attributes
	 * 
	 * @param in        matrix file for heat map to represent
	 * @param c         color to represent maximum values
	 * @param startR    starting row of the CDT file (Zero indexed)
	 * @param startC    starting column of the CDT file (Zero indexed)
	 * @param pHeight   height of resulting heat map (# pixels)
	 * @param pWidth    width of resulting heat map (# pixels)
	 * @param scale     scale compression type
	 * @param abs       the difference in values for each step of the color scale
	 * @param quant     the difference in percent of values for each step of the
	 *                  color scale
	 * @param output    directory to output PNG to
	 * @param outstatus whether or not to output a PNG
	 * @param trans     specifies if min values should be transparent
	 */
	public TwoColorHeatMap(File in, Color c, int startR, int startC, int pHeight, int pWidth, String scale, double abs,
			double quant, File output, boolean outstatus, boolean trans) {

		SAMPLE = in;
		MAXCOLOR = c;
		startROW = startR;
		startCOL = startC;
		pixelHeight = pHeight;
		pixelWidth = pWidth;
		scaleType = scale;

		absolute = abs;
		quantile = quant;

		OUTFILE = output;
		OUTPUTSTATUS = outstatus;
		MINCOLOR = new Color(255, 255, 255, 255);
		if (trans) {
			MINCOLOR = new Color(MAXCOLOR.getRed(), MAXCOLOR.getGreen(), MAXCOLOR.getBlue(), 0);
		}
	}

	/**
	 * Runs the {@link TwoColorHeatMap#generateHeatMap(ArrayList) script and manages
	 * the image output}
	 * 
	 * @throws IOException Invalid file or parameters
	 */
	public void run() throws IOException {
		System.out.println("Loading Matrix file: " + OUTFILE.getName());
		MATRIX = loadMatrix(SAMPLE);
		System.out.println("Matrix file loaded.");
		System.out.println("Rows detected: " + MATRIX.size());
		System.out.println("Columns detected: " + MATRIX.get(0).length);

		if (scaleType.equalsIgnoreCase("treeview")) {
			ArrayList<double[]> newMatrix = rebinMatrix(MATRIX);
			if (absolute != -999) {
				COLOR_RATIO = absolute;
			} else {
				COLOR_RATIO = getQuantile(newMatrix, quantile);
			}

			System.out.println("Contrast threshold: " + COLOR_RATIO);
			BufferedImage treeMap = generateHeatMap(newMatrix);
			picLabel = new JLabel(new ImageIcon(treeMap));
			// Don't output PNG if OUTPUTSTATUS is false, which is the flag for not
			// outputing figures
			if (OUTPUTSTATUS) {
				ImageIO.write(treeMap, "png", OUTFILE);
			}
		} else if (!scaleType.equalsIgnoreCase("treeview")) {
			// COLOR_RATIO = 2 * getNonZeroAvg(MATRIX);
			if (absolute != -999) {
				COLOR_RATIO = absolute;
			} else {
				COLOR_RATIO = getQuantile(MATRIX, quantile);
			}
			System.out.println("Contrast threshold: " + COLOR_RATIO);

			BufferedImage rawMap = generateHeatMap(MATRIX);
			BufferedImage compressedMap = resize(rawMap, pixelWidth, pixelHeight);
			picLabel = new JLabel(new ImageIcon(compressedMap));

			// Don't output PNG if OUTPUTSTATUS is false, which is the flag for not
			// outputing figures
			if (OUTPUTSTATUS) {
				ImageIO.write(compressedMap, "png", OUTFILE);
			}
		}

	}

	/**
	 * Generates the two color heat map with the matrix of values parsed with
	 * {@link TwoColorHeatMap#loadMatrix(File)}
	 * 
	 * @param matrix Matrix of values to create heat map with
	 * @return The heat map as a BufferedImage object
	 * @throws FileNotFoundException Script could not find valid input file
	 */
	public static BufferedImage generateHeatMap(ArrayList<double[]> matrix) throws FileNotFoundException {
		int width = 1;
		int height = 1;

		int pixwidth = matrix.get(0).length * width;
		int pixheight = matrix.size() * height;

		System.setProperty("java.awt.headless", "true");
		BufferedImage im = new BufferedImage(pixwidth, pixheight, BufferedImage.TYPE_INT_ARGB);
		Graphics g = im.getGraphics();
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(MINCOLOR);
		g2.fillRect(0, 0, pixwidth, pixheight);

		int count = 0;
		for (int x = 0; x < matrix.size(); x++) {
			double[] ID = matrix.get(x);

			for (int j = 0; j < ID.length; j++) {
				if (ID[j] > 0) {
					double v = ID[j] / COLOR_RATIO;
					double sVal = v > 1 ? 1 : (v < 0 ? 0 : v);
					int red = (int) (MAXCOLOR.getRed() * sVal + MINCOLOR.getRed() * (1 - sVal));
					int green = (int) (MAXCOLOR.getGreen() * sVal + MINCOLOR.getGreen() * (1 - sVal));
					int blue = (int) (MAXCOLOR.getBlue() * sVal + MINCOLOR.getBlue() * (1 - sVal));
					int alpha = (int) (MAXCOLOR.getAlpha() * sVal + MINCOLOR.getAlpha() * (1 - sVal));
					g.setColor(new Color(red, green, blue, alpha));
				} else {
					g.setColor(MINCOLOR);
				}
				g.fillRect(j * width, count * height, width, height);
			}
			count++;
		}
		return im;
	}

	/**
	 * Resizes the heat map to fit within specified dimensions
	 * 
	 * @param img  BufferedImage to be resized
	 * @param newW The width of the new buffered image (# of pixels)
	 * @param newH The height of the new buffered image (# of pixels)
	 * @return
	 */
	public static BufferedImage resize(BufferedImage img, int newW, int newH) {
		BufferedImage resized_image = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) resized_image.createGraphics();
		if (scaleType.equalsIgnoreCase("bicubic")) {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		} else if (scaleType.equalsIgnoreCase("bilinear")) {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		} else if (scaleType.equalsIgnoreCase("neighbor")) {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		}
		g2.drawImage(img, 0, 0, newW, newH, null);
		g2.dispose();
		return resized_image;
	}

	/**
	 * Expands or compresses the given matrix by duplicating rows/columns or
	 * averaging values respectively
	 * 
	 * @param oldmatrix Matrix to be compressed or expanded
	 * @return A new matrix with the appropriate values and dimensions
	 */
	public static ArrayList<double[]> rebinMatrix(ArrayList<double[]> oldmatrix) {
		ArrayList<double[]> newmatrix = new ArrayList<double[]>();
		int R = oldmatrix.size();
		int C = oldmatrix.get(0).length;
		int r = pixelHeight;
		int c = pixelWidth;

		// expand the original array to be larger is expanding beyond pixel size
		if (r > R) {
			int rowAdd = (r / R) + 1;
			// System.out.println("Row duplication events: " + rowAdd);
			ArrayList<double[]> temp = new ArrayList<double[]>();
			for (int x = 0; x < R; x++) {
				for (int y = 0; y <= rowAdd; y++) {
					temp.add(oldmatrix.get(x).clone());
				}
			}
			// Set pointers to new row values
			oldmatrix = temp;
			R = oldmatrix.size();
		}
		if (c > C) {
			int colAdd = (c / C) + 1;
			// System.out.println("Column duplication events: " + colAdd);
			ArrayList<double[]> temp = new ArrayList<double[]>();
			for (int x = 0; x < R; x++) {
				double[] oldarray = oldmatrix.get(x);
				double[] newarray = new double[oldarray.length * colAdd];
				for (int y = 0; y < newarray.length; y++) {
					// System.out.print(y + "\t");
					// System.out.println(y / colAdd);
					newarray[y] = oldarray[y / colAdd];
				}
				temp.add(newarray);
			}
			// Set pointers to new row values
			oldmatrix = temp;
			C = oldmatrix.get(0).length;
		}

		int rowDelete = R % r;
		int colDelete = C % c;
		// if(rowDelete != 0) { System.out.println("Rows to delete: " + rowDelete); }
		// if(colDelete != 0) { System.out.println("Cols to delete: " + colDelete); }

		// Matrix resize requires perfect division % == 0; So remove and average in
		// rows/columns to make it happen
		if (rowDelete > 0) {
			// Get indexes of matrix rows evenly spread across the matrix
			// [0 ... 100] down to 20 rows produces [0,5,10,15,20]
			int[] row_delete = linspace(0, R - 1, rowDelete);
			// Correction to remove the last row if linspace output results in out of bounds
			for (int x = 0; x < row_delete.length; x++) {
				if (row_delete[x] == oldmatrix.size() - 1) {
					row_delete[x]--;
				}
			}
			// Get the indexes of matrix rows +1
			int[] row_delete_plus1 = frameshift(row_delete);
			if (row_delete.length != row_delete_plus1.length) {
				System.err.println("Row delete frameshift/merge failure!!!");
				System.exit(1);
			}
			// Merge rows to be deleted into adjacent row by averaging
			for (int x = 0; x < row_delete.length; x++) {
				double[] oldrow = oldmatrix.get(row_delete[x]);
				double[] updatedrow = oldmatrix.get(row_delete_plus1[x]);
				for (int y = 0; y < oldrow.length; y++) {
					updatedrow[y] = (oldrow[y] + updatedrow[y]) / 2.0;
				}
			}
			// Delete redundant rows - go in reverse order to maintain index order
			for (int x = row_delete.length - 1; x >= 0; x--) {
				oldmatrix.remove(row_delete[x]);
			}
			// Set R to new value
			R = oldmatrix.size();
		}

		if (colDelete > 0) {
			int[] col_delete = linspace(0, C - 1, colDelete);
			// Correction to remove the last column if linspace output results in out of
			// bounds
			for (int x = 0; x < col_delete.length; x++) {
				if (col_delete[x] == oldmatrix.get(0).length - 1) {
					col_delete[x]--;
				}
			}
			int[] col_delete_plus1 = frameshift(col_delete);
			if (col_delete.length != col_delete_plus1.length) {
				System.err.println("Column delete frameshift/merge failure!!!");
				System.exit(1);
			}

			// Merge cols to be deleted into adjacent col by averaging
			for (int x = 0; x < oldmatrix.size(); x++) {
				double[] oldcol = oldmatrix.get(x);
				double[] avgcol = oldcol.clone();
				for (int y = 0; y < col_delete.length; y++) {
					avgcol[col_delete_plus1[y]] = (oldcol[col_delete[y]] + oldcol[col_delete_plus1[y]]) / 2.0;
				}

				double[] updatedcol = new double[oldcol.length - col_delete.length];
				int currentIndex = 0;
				for (int y = 0; y < oldcol.length; y++) {
					boolean remove = false;
					for (int z = 0; z < col_delete.length; z++) {
						if (col_delete[z] == y) {
							remove = true;
						}
					}
					if (!remove) {
						updatedcol[currentIndex] = avgcol[y];
						currentIndex++;
					}
				}
				oldmatrix.set(x, updatedcol);
			}
			// Set C to new value
			C = oldmatrix.get(0).length;
		}

		if (R % r != 0) {
			System.err.println("Failure to remove rows modularly!!!");
			System.exit(1);
		}
		if (C % c != 0) {
			System.err.println("Failure to remove columns modularly!!!");
			System.exit(1);
		}

		// Resize original matrix into new matrix size; average values to make new
		// values
		for (int i = 0; i < R; i += (R / r)) {
			double[] newRow = new double[c];
			for (int j = 0; j < C; j += (C / c)) {
				double AVG = 0, count = 0;
				for (int x = i; x < i + (R / r); x++) {
					for (int y = j; y < j + (C / c); y++) {
						AVG += oldmatrix.get(x)[y];
						count++;
					}
				}
				newRow[j / (C / c)] = AVG / count;
			}
			newmatrix.add(newRow);
		}
		return newmatrix;
	}

	/**
	 * Returns an array of n values from min to max that are spaced evenly apart.
	 * 
	 * @param min lower bound value of array
	 * @param max upper bound value of array
	 * @param n   the number of points in the array
	 * @return list of n points with value evenly spaced from min to max
	 */
	public static int[] linspace(int min, int max, int n) {
		int[] d = new int[n];
		if (n < 0) {
			System.err.println("Invalid number of points to parse!!! " + n + "\n");
			System.exit(1);
		}
		for (int i = 1; i < n; i++) {
			d[i] = min + i * (max - min) / (n - 1);
		}
		return d;
	}

	/**
	 * Returns a copy of the original array with all values incremented by 1
	 * 
	 * @param orig array to increment
	 * @return incremented array
	 */
	public static int[] frameshift(int[] orig) {
		int[] newarray = new int[orig.length];
		for (int x = 0; x < orig.length; x++) {
			newarray[x] = orig[x] + 1;
		}
		return newarray;
	}

	/**
	 * Returns the average of arrays in a matrix, ignoring zero-values
	 * 
	 * @param matrix Matrix to be averaged
	 * @return Average of the matrix, ignoring zero-values
	 */
	public static double getNonZeroAvg(ArrayList<double[]> matrix) {
		double average = 0;
		int count = 0;

		for (int x = 0; x < matrix.size(); x++) {
			for (int y = 0; y < matrix.get(x).length; y++) {
				if (matrix.get(x)[y] != 0) {
					average += matrix.get(x)[y];
					count++;
				}
			}
		}
		if (count != 0) {
			average /= count;
		}
		return average;
	}

	/**
	 * Returns the highest value in a certain percent of the matrix values
	 * (excluding zeros)
	 * 
	 * @param matrix  Matrix with values
	 * @param percent Percent of matrix values to analyze
	 * @return The highest value in a percent of matrix values
	 */
	public static double getQuantile(ArrayList<double[]> matrix, double percent) {
		ArrayList<Double> nonZero = new ArrayList<Double>();
		for (int x = 0; x < matrix.size(); x++) {
			for (int y = 0; y < matrix.get(x).length; y++) {
				if (matrix.get(x)[y] != 0 && !Double.isNaN(matrix.get(x)[y])) {
					nonZero.add(Double.valueOf(matrix.get(x)[y]));
				}
			}
		}
		Collections.sort(nonZero);
		// Collections.reverse(nonZero);
		int index = (int) (percent * (double) (nonZero.size() - 1));
		return nonZero.get(index);
	}

	/**
	 * Takes an input file and returns an ArrayList &lt;double[]&gt; with the values
	 * from the input file
	 * 
	 * @param input Input file
	 * @return An ArrayList &lt;double[]&gt; with the values from the input file
	 * @throws UnsupportedEncodingException
	 * @throws IOException                  Invalid file or parameters
	 */
	public static ArrayList<double[]> loadMatrix(File input) throws UnsupportedEncodingException, IOException {
		ArrayList<double[]> matrix = new ArrayList<double[]>();
		int currentRow = 0;
		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(input);
		// Initialize line variable to loop through
		String line = br.readLine();
		while (line != null) {
			// Split into tokens by tab delimiter
			String[] temp = line.split("\t");
			if (!temp[0].contains("YORF") && currentRow >= startROW) {
				double[] ARRAY = new double[temp.length - startCOL];
				for (int x = startCOL; x < temp.length; x++) {
					try {
						ARRAY[x - startCOL] = Double.parseDouble(temp[x]);
					} catch (NumberFormatException nfe) {
						ARRAY[x - startCOL] = Double.NaN;
					}
				}
				matrix.add(ARRAY);
			}
			currentRow++;
			line = br.readLine();
		}
		// Close files
		br.close();
		return matrix;
	}

	/**
	 * Returns the two color heat map as a JLabel object
	 * 
	 * @return the two color heat map
	 */
	public JLabel getImg() {
		return (picLabel);
	}
}
