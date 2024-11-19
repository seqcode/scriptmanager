package scriptmanager.scripts.Read_Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.JOptionPane;

import scriptmanager.util.GZipUtilities;

/**
 * Performs scalar multiplication on given matrix
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Read_Analysis.ScaleMatrixCLI
 * @see scriptmanager.window_interface.Read_Analysis.ScaleMatrixWindow
 */
public class ScaleMatrix {

	private File MATRIX = null;
	private File OUTFILE = null;
	private boolean OUTPUT_GZIP = false;
	private double SCALE = 0;

	private int ROWINDEX = 0;
	private int COLINDEX = 0;

	/**
	 * Creates a new instance of the ScaleMatrix script with a given input file
	 * @param m TAB file to be scaled
	 * @param o Output directory
	 * @param s Scaling factor
	 * @param r Starting row (1-indexed)
	 * @param c Starting column (1-indexed)
	 * @param gzOutput   whether or not to gzip output
	 */
	public ScaleMatrix(File m, File o, double s, int r, int c, boolean gzOutput) {
		MATRIX = m;
		OUTFILE = o;
		OUTPUT_GZIP = gzOutput;
		SCALE = s;

		ROWINDEX = r;
		COLINDEX = c;
	}

	/**
	 * Performs the scalar multiplication
	 * @throws IOException Invalid file or parameters
	 */
	public void run() throws IOException {
		// Open output file
		PrintStream OUT = GZipUtilities.makePrintStream(OUTFILE, OUTPUT_GZIP);

		System.err.println(getTimeStamp());
		System.err.println("Processing file:\t" + MATRIX.getName());
		System.err.println("Scaling factor:\t" + SCALE);
		System.err.println("Starting row index:\t" + ROWINDEX);
		System.err.println("Starting column index:\t" + COLINDEX);

		// Parse, scale, and output tab-delimited matrix on the fly
		try {
			// Check if file is gzipped and instantiate appropriate BufferedReader
			BufferedReader br = GZipUtilities.makeReader(MATRIX);
			int counter = 0;
			String line;
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\t");
				if (counter < ROWINDEX) {
					OUT.println(String.join("\t", temp));
				} else {
					for (int x = 0; x < temp.length; x++) {
						if (x < COLINDEX) {
							OUT.print(temp[x]);
						} else {
							OUT.print(Double.parseDouble(temp[x]) * SCALE);
						}
						if (x < temp.length - 1) {
							OUT.print("\t");
						}
					}
					OUT.println();
				}
				counter++;
				if (counter % 1000 == 0) {
					System.err.println("Rows processed: " + counter);
				}
			}
			br.close();
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, MATRIX.getName() + " contains non-numbers in indexes selected!!!");
		}
		OUT.close();
	}

	private static String getTimeStamp() {
		Date date = new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
}
