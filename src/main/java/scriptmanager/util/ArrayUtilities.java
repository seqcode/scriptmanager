package scriptmanager.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Generally useful array manipulations.
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Read_Analysis.TagPileup
 * @see scriptmanager.scripts.Read_Analysis.PileupScripts.PileupExtract
 * @see scriptmanager.scripts.Read_Analysis.AggregateData
 */
public class ArrayUtilities {

	/**
	 * Creates a new ArrayUtilities object (unnecessary becuase this class is a collection of static methods)
	 */
	public ArrayUtilities(){}
	/**
	 * Scale every element of the input primitive array by an element-wise division
	 * operation with the scalar input.<br>
	 * i.e. for each element i of orig, divide by ratio (i/ratio). <em>Note: No
	 * other classes use this method yet</em>
	 * 
	 * @param orig  primitive array to element-wise divide
	 * @param ratio denominator for each array value
	 * @return the new scalar array of each orig divided by ratio
	 */
	public static double[] scaleArray(double[] orig, double ratio) {
		double[] Sarray = new double[orig.length];
		for (int x = 0; x < orig.length; x++) {
			Sarray[x] = orig[x] / ratio;
		}
		return Sarray;
	}

	/**
	 * Reverse the order of an input primitive array.
	 * 
	 * @see scriptmanager.scripts.Read_Analysis.PileupScripts.PileupExtract
	 * @param orig the array to reverse
	 * @return reversed array, null if orig array is null
	 */
	public static double[] reverseArray(double[] orig) {
		if (orig != null) {
			double[] reverse = new double[orig.length];
			for (int x = orig.length - 1; x >= 0; x--) {
				reverse[orig.length - 1 - x] = orig[x];
			}
			return reverse;
		}
		return null;
	}

	/**
	 * Apply a smoothing window to an input primitive array.
	 * 
	 * @see scriptmanager.scripts.Read_Analysis.TagPileup
	 * @param orig the array to smooth
	 * @param win  the size of the smoothing window (recommend odd values for
	 *             symmetrical smoothing but not required)
	 * @return the smoothed window
	 */
	public static double[] windowSmooth(double[] orig, int win) {
		int window = win / 2;
		double[] Sarray = new double[orig.length];
		for (int x = 0; x < orig.length; x++) {
			double score = 0;
			double weight = 0;
			for (int y = x - window; y <= x + window; y++) {
				if (y < 0)
					y = -1;
				else if (y < orig.length) {
					score += orig[y];
					weight++;
				} else
					y = x + window + 1;
			}
			if (weight != 0)
				Sarray[x] = score / weight;
		}
		return Sarray;
	}

	/**
	 * Apply a gaussian smoothing to an input primitive array.
	 * 
	 * @param orig the array to smooth
	 * @param size set the size of the standard deviation
	 * @param num  set the number of standard deviations
	 * @return the smoothed window
	 */
	public static double[] gaussSmooth(double[] orig, int size, int num) {
		double[] Garray = new double[orig.length];
		int window = size * num;
		double SDSize = (double) size;
		for (int x = 0; x < orig.length; x++) {
			double score = 0;
			double weight = 0;
			for (int y = x - window; y <= x + window; y++) {
				if (y < 0)
					y = -1;
				else if (y < orig.length) {
					double HEIGHT = Math.exp(-1 * Math.pow((y - x), 2) / (2 * Math.pow(SDSize, 2)));
					score += (HEIGHT * orig[y]);
					weight += HEIGHT;
				} else
					y = x + window + 1;
			}
			if (weight != 0)
				Garray[x] = score / weight;
		}
		return Garray;
	}

	/**
	 * Return the largest value in an input primitive array.
	 * 
	 * @see scriptmanager.scripts.Read_Analysis.AggregateData
	 * @param array the input array to search
	 * @return largest value in array
	 */
	public static double getMax(double[] array) {
		double max = 0;
		for (int x = 0; x < array.length; x++) {
			if (!Double.isNaN(array[x])) {
				if (array[x] > max) {
					max = array[x];
				}
			}
		}
		return max;
	}

	/**
	 * Return the smallest value in an input primitive array.
	 * 
	 * @see scriptmanager.scripts.Read_Analysis.AggregateData
	 * @param array the input array to search
	 * @return smallest value in array
	 */
	public static double getMin(double[] array) {
		double min = Double.NaN;
		for (int x = 0; x < array.length; x++) {
			if (!Double.isNaN(array[x])) {
				if (Double.isNaN(min)) {
					min = array[x];
				} else if (array[x] < min) {
					min = array[x];
				}
			}
		}
		return min;
	}

	/**
	 * Return the median value in an input primitive array.
	 * 
	 * @see scriptmanager.scripts.Read_Analysis.AggregateData
	 * @param array the input array to search
	 * @return median of the array elements
	 */
	public static double getMedian(double[] array) {
		ArrayList<Double> values = new ArrayList<Double>();
		for (int x = 0; x < array.length; x++) {
			if (!Double.isNaN(array[x])) {
				values.add(array[x]);
			}
		}
		if (values.size() > 0) {
			Collections.sort(values);
			double pos1 = Math.floor((values.size() - 1.0) / 2.0);
			double pos2 = Math.ceil((values.size() - 1.0) / 2.0);
			if (pos1 == pos2) {
				return values.get((int) pos1);
			} else {
				return (values.get((int) pos1) + values.get((int) pos2)) / 2.0;
			}
		} else {
			return Double.NaN;
		}
	}

	/**
	 * Return the average value in an input primitive array.
	 * 
	 * @see scriptmanager.scripts.Read_Analysis.AggregateData
	 * @param array the input array to average
	 * @return average of the array elements
	 */
	public static double getAverage(double[] array) {
		double average = 0;
		int count = 0;
		for (int x = 0; x < array.length; x++) {
			if (!Double.isNaN(array[x])) {
				average += array[x];
				count++;
			}
		}
		if (count != 0)
			return (average / count);
		else
			return Double.NaN;
	}

	/**
	 * Return the sum of an input primitive array.
	 * 
	 * @see scriptmanager.scripts.Read_Analysis.AggregateData
	 * @param array the input array to sum
	 * @return sum of the array elements
	 */
	public static double getSum(double[] array) {
		double sum = 0;
		int count = 0;
		for (int x = 0; x < array.length; x++) {
			if (!Double.isNaN(array[x])) {
				sum += array[x];
				count++;
			}
		}
		if (count != 0)
			return sum;
		else
			return Double.NaN;
	}

	/**
	 * Return the mode of an input primitive array.
	 * 
	 * @see scriptmanager.scripts.Read_Analysis.AggregateData
	 * @param array the input array to search
	 * @return mode of the array elements
	 */
	public static double getMode(double[] array) {
		double mode = 0;
		int modecount = 0;
		Map<Double, Integer> count = new HashMap<Double, Integer>();
		for (int x = 0; x < array.length; x++) {
			if (!Double.isNaN(array[x])) {
				Integer n = count.get(array[x]);
				if (n == null) {
					count.put(array[x], 1);
				} else {
					count.put(array[x], n + 1);
				}
			}
		}
		if (count.isEmpty()) {
			return Double.NaN;
		}
		Set<Double> keys = count.keySet();
		for (Double key : keys) {
			if (count.get(key) > modecount) {
				modecount = count.get(key);
				mode = key;
			}
		}
		return mode;
	}

	/**
	 * Return the positional variance of an input primitive array.
	 * 
	 * @see scriptmanager.scripts.Read_Analysis.AggregateData
	 * @param array the input array to calculate from
	 * @return positional variance of array (NaN if array is empty and 0 if array
	 *         has only 1 element)
	 */
	public static double getPositionalVariance(double[] array) {
		// Calculate AVG, count N, and store a copy of array into ref as an ArrayList
		ArrayList<Double> ref = new ArrayList<Double>();
		double AVG = 0;
		double N = 0;
		for (int x = 0; x < array.length; x++) {
			if (!Double.isNaN(array[x])) {
				for (int z = 0; z < array[x]; z++) {
					ref.add(Double.valueOf(x));
					AVG += x;
					N++;
				}
			}
		}
		AVG /= N;

		// VAR = NaN if array is empty, VAR = 0 if array has one element,...
		double VAR = Double.NaN;
		// VAR = calculate variance from AVG
		if (N > 1) {
			double SS = 0;
			for (int x = 0; x < ref.size(); x++) {
				SS += Math.pow(Math.abs(ref.get(x).doubleValue() - AVG), 2); // Is Math.abs necessary here?
			}
			VAR = Math.sqrt(SS / N) * Math.sqrt(N / (N - 1));
		} else if (N > 0) {
			VAR = 0;
		}
		return VAR;
	}
}