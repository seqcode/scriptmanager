package scriptmanager.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

/**
 * This class was created to parse and validate CDT files and counting the
 * number of columns and is based on originally tool-specific methods.
 * 
 * @author William KM Lai
 * @see scriptmanager.window_interface.Coordinate_Manipulation.BED_Manipulation.SortBEDWindow
 * @see scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.SortBED
 * @see scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.SortGFF
 * 
 */
public class CDTUtilities {

	private int SIZE;
	private boolean consistentSize;
	private String invalidMessage;

	/**
	 * Creates a new CDTUtilities object
	 */
	public CDTUtilities(){}

	/**
	 * Parse CDT-formatted file for consistent column sizes and a row count
	 * 
	 * @param CDT a CDT-formatted file to validate
	 * @throws IOException Invalid file or parameters
	 */
	public void parseCDT(File CDT) throws IOException {
		SIZE = -999;
		consistentSize = true;
		invalidMessage = "";
		
		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(CDT);
		// Initialize line variable to loop through
		String line = br.readLine();
		int currentRow = 1;
		while (line != null) {
			String[] temp = line.split("\t");
			if(!temp[0].contains("YORF") && !temp[0].contains("NAME")) {
				int tempsize = temp.length - 2;
				if(SIZE == -999) { SIZE = tempsize; }
				else if(SIZE != tempsize) {
					invalidMessage = "Invalid Row at Index: " + currentRow;
					consistentSize = false;
					break;
				}
				currentRow++;
			}
			line = br.readLine();
		}
		br.close();
	}
	
	/**
	 * Returns if the rows of a CDT all have the same number of columns
	 * @return True if all rows match, false if otherwise
	 */
	public boolean isValid(){ return consistentSize; }
	
	/**
	 * Returns the number of columns in a CDT
	 * @return The number of columns in a CDT
	 */
	public int getSize(){ return SIZE; }
	
	/**
	 * Returns an error message referencing the first invalid row
	 * @return An error message referencing the first invalid row
	 */
	public String getInvalidMessage(){ return invalidMessage; }
	
	/**
	 * Loads a given CDT file into a Vector&lt;double[]&gt;
	 * @param input File to be loaded
	 * @return A Vector&lt;double[]&gt; representing the CDT file
	 * @throws FileNotFoundException Script could not find valid input file
	 */
	public static Vector<double[]> loadCDT(File input) throws FileNotFoundException {
		Vector<double[]> matrix = new Vector<double[]>();
		Scanner scan = new Scanner(input);
		while (scan.hasNextLine()) {
			String[] temp = scan.nextLine().split("\t");
			if(!temp[0].contains("YORF")) {
				double[] ARRAY = new double[temp.length - 2];
				for(int x = 0; x < ARRAY.length; x++) {
					ARRAY[x] = Double.parseDouble(temp[x + 2]);
				}
				matrix.add(ARRAY);
			}
		}
		scan.close();
		return matrix;
	}

	/**
	 * Given a 2D-array formatted as a vector of primitive array types, return the
	 * average composite.
	 * 
	 * @param CDT a Vector of primitive arrays of primitive doubles (decimal matrix)
	 * @return an array of positional composite average values from the input matrix
	 */
	public static double[] getComposite(Vector<double[]> CDT) {
		double[] AVG = new double[CDT.get(0).length];
		double COUNT = 0;
		for(int x = 0; x < CDT.size(); x++) {
			for(int y = 0; y < CDT.get(x).length; y++) {
				AVG[y] += CDT.get(x)[y];
			}
			COUNT++;
		}
		
		for(int x = 0; x < AVG.length; x++) { AVG[x] /= COUNT; }
		return AVG;
	}

	/**
	 * Given a 2D-array formatted as a vector of primitive array types, return some
	 * basic statistics.
	 * 
	 * @param CDT a Vector of primitive arrays of primitive doubles (decimal matrix)
	 * @return an ArrayList of statistics on the input matrix
	 */
	public static ArrayList<Double> getStats(Vector<double[]> CDT) {
		ArrayList<Double> STATS = new ArrayList<Double>();
		ArrayList<Double> values = new ArrayList<Double>();
		
		double max = 0, min = 0, average = 0, median = 0, mode = 0;
		int count = 0, modecount = 0;
		Map<Double, Integer> mode_hash = new HashMap<Double, Integer>();

		for(int x = 0; x < CDT.size(); x++) {
			for(int y = 0; y < CDT.get(x).length; y++) {
				if(CDT.get(x)[y] != 0) {
					if(CDT.get(x)[y] > max) { max = CDT.get(x)[y]; }
					if(min == 0) { min = CDT.get(x)[y]; }
					else if(CDT.get(x)[y] < min) { min = CDT.get(x)[y]; }
					
					average += CDT.get(x)[y];
					count++;
					values.add(CDT.get(x)[y]);

					Integer n = mode_hash.get(CDT.get(x)[y]);
					if(n == null) { mode_hash.put(CDT.get(x)[y], 1); }
					else { mode_hash.put(CDT.get(x)[y], n + 1); }
				}
			}
		}
		Collections.sort(values);

		if(count != 0) { average /= count; }

		double pos1 = Math.floor((values.size() - 1.0) / 2.0);
		double pos2 = Math.ceil((values.size() - 1.0) / 2.0);
		if (pos1 == pos2 ) { median = values.get((int)pos1); }
		else { median = (values.get((int)pos1) + values.get((int)pos2)) / 2.0 ; }

		Set<Double> keys = mode_hash.keySet();
		for(Double key : keys) {
			if(mode_hash.get(key) > modecount) {
				modecount = mode_hash.get(key);
				mode = key;
				}
		}
		
		STATS.add(min);
		STATS.add(max);
		STATS.add(average);
		STATS.add(median);
		STATS.add(mode);
		return STATS;
	}

	/**
	 * Given a 2D-array formatted as a vector of primitive array types, return the
	 * non-zero maximum value.
	 * 
	 * @param CDT a Vector of primitive arrays of primitive doubles (decimal matrix)
	 * @return the maximum value ignoring zeros of the input matrix.
	 */
	public static Double getMax(Vector<double[]> CDT) {
		double max = 0;
		for(int x = 0; x < CDT.size(); x++) {
			for(int y = 0; y < CDT.get(x).length; y++) {
				if(CDT.get(x)[y] != 0) {
					if(CDT.get(x)[y] > max) { max = CDT.get(x)[y]; }
				}
			}
		}
		return max;
	}

	/**
	 * Given a 2D-array formatted as a vector of primitive array types, return the
	 * non-zero minimum value.
	 * 
	 * @param CDT a Vector of primitive arrays of primitive doubles (decimal matrix)
	 * @return the minimum value ignoring zeros of the input matrix.
	 */
	public static Double getMin(Vector<double[]> CDT) {
		double min = 0;
		for(int x = 0; x < CDT.size(); x++) {
			for(int y = 0; y < CDT.get(x).length; y++) {
				if(CDT.get(x)[y] != 0) {
					if(min == 0) { min = CDT.get(x)[y]; }
					else if(CDT.get(x)[y] < min) { min = CDT.get(x)[y]; }
				}
			}
		}
		return min;
	}

	/**
	 * Given a 2D-array formatted as a vector of primitive array types, return the
	 * non-zero median value.
	 * 
	 * @param CDT a Vector of primitive arrays of primitive doubles (decimal matrix)
	 * @return the median value ignoring zeros of the input matrix
	 */
	public static Double getMedian(Vector<double[]> CDT) {
		ArrayList<Double> values = new ArrayList<Double>();
		for(int x = 0; x < CDT.size(); x++) {
			for(int y = 0; y < CDT.get(x).length; y++) {
				if(CDT.get(x)[y] != 0) {
					values.add(CDT.get(x)[y]);
				}
			}
		}
		Collections.sort(values);

		// Averaging two floor/ceil middle values accounts for even/odd list size
		double pos1 = Math.floor((values.size() - 1.0) / 2.0);
		double pos2 = Math.ceil((values.size() - 1.0) / 2.0);
		if (pos1 == pos2 ) {
			return values.get((int)pos1);
		} else {
			return (values.get((int)pos1) + values.get((int)pos2)) / 2.0 ;
		}
	}

	/**
	 * Given a 2D-array formatted as a vector of primitive array types, return the
	 * non-zero average value.
	 * 
	 * @param CDT a Vector of primitive arrays of primitive doubles (decimal matrix)
	 * @return the average value ignoring zeros of the input matrix
	 */
	public static Double getAverage(Vector<double[]> CDT) {
		double average = 0;
		int count = 0;
		for(int x = 0; x < CDT.size(); x++) {
			for(int y = 0; y < CDT.get(x).length; y++) {
				if(CDT.get(x)[y] != 0) {
					average += CDT.get(x)[y];
					count++;
				}
			}
		}
		if(count != 0) return (average / count);
		else return 0.0;
	}

	/**
	 * Given a 2D-array formatted as a vector of primitive array types, return the
	 * non-zero mode value.
	 * 
	 * @param CDT a Vector of primitive arrays of primitive doubles (decimal matrix)
	 * @return the mode value ignoring zeros of the input matrix
	 */
	public static Double getMode(Vector<double[]> CDT) {
		double mode = 0;
		int modecount = 0;
		Map<Double, Integer> count = new HashMap<Double, Integer>();
		for(int x = 0; x < CDT.size(); x++) {
			for(int y = 0; y < CDT.get(x).length; y++) {
				if(CDT.get(x)[y] != 0) {
					Integer n = count.get(CDT.get(x)[y]);
					if(n == null) {
						count.put(CDT.get(x)[y], 1);
					} else {
						count.put(CDT.get(x)[y], n + 1);
					}
				}
			}
		}
		Set<Double> keys = count.keySet();
		for(Double key : keys) {
			if(count.get(key) > modecount) {
				modecount = count.get(key);
				mode = key;
				}
		}
		return mode;
	}
}
