package scriptmanager.objects;

import java.util.Comparator;

/**
 * The object describing how peaks are stored for the Peak Calling tools.
 *
 * @author William KM Lai
 * @see scriptmanager.scripts.Peak_Calling.GeneTrack_BAM
 * @see scriptmanager.scripts.Peak_Calling.PeakPair
 */
public class Peak {
	private String CHROM = "";
	private int BP = -1;
	private int TAG = -1;
	private double STDDEV = -1;
	private int START = -1;
	private int STOP = -1;
	private String DIR = "*";

	/**
	 * Initialize with a standard deviation value
	 *
	 * @param chr the chromosome name
	 * @param start the start coordinate of the peak (left/smaller coord index)
	 * @param stop the stop coordinate of the peak (right/larger coord index)
	 * @param dir the strand/direction
	 * @param t the tag count
	 * @param std the standard deviation value
	 */
	public Peak(String chr, int start, int stop, String dir, int t, double std) {
		CHROM = chr;
		START = start;
		STOP = stop;
		DIR = dir;
		TAG = t;
		STDDEV = std;
	}

	/**
	 * Intialize without a standard deviation value
	 *
	 * @param chr the chromosome name
	 * @param start the start coordinate of the peak (left/smaller coord index)
	 * @param stop the stop coordinate of the peak (right/larger coord index)
	 * @param dir the strand/direction
	 * @param t the tag count
	 */
	public Peak(String chr, int start, int stop, String dir, int t) {
		CHROM = chr;
		START = start;
		STOP = stop;
		DIR = dir;
		TAG = t;
	}

	/**
	 * get the strand/direction
	 *
	 * @return the stored strand/direction
	 */
	public String getDir() {
		return DIR;
	}
	/**
	 * set the strand/direction
	 *
	 * @param dir the strand/direction
	 */
	public void setDir(String dir) {
		DIR = dir;
	}

	/**
	 * get the chromosome name
	 *
	 * @return the stored chr name
	 */
	public String getChrom() {
		return CHROM;
	}

	/**
	 * Returns the BP position
	 * @return The BP position
	 */
	public int getBP() {
		return BP;
	}
	/**
	 * get the tag count
	 *
	 * @return the stored tag count
	 */
	public int getTag() {
		return TAG;
	}
	/**
	 * get the standard deviation
	 *
	 * @return the stored standard deviation
	 */
	public double getStd() {
		return STDDEV;
	}
	/**
	 * get the peak start coordinate
	 *
	 * @return the stored start coordinate
	 */
	public int getStart() {
		return START;
	}
	/**
	 * get the peak stop coordinate
	 *
	 * @return the stored stop coordinate
	 */
	public int getStop() {
		return STOP;
	}
	/**
	 * set the chromosome name
	 *
	 * @param c the chromosome name
	 */
	public void setChrom(String c) {
		CHROM = c;
	}

	/**
	 * Sets the BP position
	 * @param b The new BP position
	 */
	public void setBP(int b) {
		BP = b;
	}

	/**
	 * set the tag count
	 *
	 * @param t the tag count
	 */
	public void setTag(int t) {
		TAG = t;
	}
	/**
	 * set the standard deviation
	 *
	 * @param s the standard deviation
	 */
	public void setStd(double s) {
		STDDEV = s;
	}
	/**
	 * set the peak start coordinate
	 *
	 * @param s the start coordinate
	 */
	public void setStart(int s) {
		START = s;
	}
	/**
	 * set the peak stop coordinate
	 *
	 * @param s the stop coordinate
	 */
	public void setStop(int s) {
		STOP = s;
	}
	/**
	 * construct and return a string of the Peak object information (GFF-format)
	 *
	 * @return name a GFF-formatted string of the Peak
	 */
	public String toString() {
//		chr4    genetrack       .       51549   51559   48      +       .       stddev=1.46115797876
		String name = CHROM + "\tgenetrack\t.\t" + START + "\t" + STOP + "\t" + TAG + "\t" + DIR + "\t.\tstddev=" + STDDEV;
		return name;
	}

	/**
	 * Comparator for tag counts
	 */
	public static Comparator<Peak> PeakTagComparator = new Comparator<Peak>() {
		public int compare(Peak node1, Peak node2) {
			int PeakScore1 = node1.getTag();
			int PeakScore2 = node2.getTag();
			if (PeakScore1 < PeakScore2) return 1;
			else if (PeakScore1 > PeakScore2) return -1;
			else return 0;
		}
	};
	/**
	 * Comparator for chromosome name
	 */
	public static Comparator<Peak> PeakChromComparator = new Comparator<Peak>() {
		public int compare(Peak node1, Peak node2) {
			return node1.getChrom().compareTo(node2.getChrom());
		}
	};
	/**
	 * Comparator for peak positions defined by the start coordinate. Does not
	 * compare chr names.
	 */
	public static Comparator<Peak> PeakPositionComparator = new Comparator<Peak>() {
		public int compare(Peak node1, Peak node2) {
			int PeakStart1 = node1.getStart();
			int PeakStart2 = node2.getStart();
			if (PeakStart1 > PeakStart2) return 1;
			else if (PeakStart1 < PeakStart2) return -1;
			else return 0;
		}
	};
	/**
	 * Comparator for peak strand information:
	 * <ul>
	 * 	<li>returns 1 if both peak strands are "+"</li>
	 * 	<li>returns -1 if both peak strands are "-"</li>
	 * 	<li>returns 0 for all other cases (like non-"+"/"-" strand Strings)</li>
	 * </ul>
	 */
	public static Comparator<Peak> PeakStrandComparator = new Comparator<Peak>() {
		public int compare(Peak node1, Peak node2) {
			String PeakStrand1 = node1.getDir();
			String PeakStrand2 = node2.getDir();
			if (PeakStrand1.equals("+") && PeakStrand2.equals("-")) return 1;
			else if (PeakStrand1.equals("-") && PeakStrand2.equals("+")) return -1;
			else return 0;
		}
	};
}
