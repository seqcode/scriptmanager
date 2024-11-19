package scriptmanager.objects.CoordinateObjects;

import java.util.Comparator;

/**
 * Object for storing BED-formatted records from a file
 * 
 * @author William KM Lai
 * @see scriptmanager.util.BEDUtilities
 * @see scriptmanager.util.BAMUtilities
 * @see scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.SortBED
 * @see scriptmanager.scripts.Peak_Analysis.FilterBEDbyProximity
 * @see scriptmanager.scripts.Peak_Analysis.RandomCoordinate
 * @see scriptmanager.scripts.Peak_Analysis.TileGenome
 * @see scriptmanager.scripts.Read_Analysis.ScalingFactor
 * @see scriptmanager.scripts.Read_Analysis.TagPileup
 * @see scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED
 * @see scriptmanager.scripts.Sequence_Analysis.FASTAExtract
 */
public class BEDCoord implements GenomicCoord {
	private String CHROM = "";
	private long START = 0;
	private long STOP = 0;
	private String DIR = "+";
	
	private String NAME = ".";
	private double SCORE = 0;

	private long MID = 0;
	
	private double[] Fstrand = null;
	private double[] Rstrand = null;

	/**
	 * Creates a new BEDCoord object with default values
	 */
	public BEDCoord() {}

	/**
	 * Instantiate object from a BED-formatted string
	 * 
	 * @param line BED-formatted string to parse
	 */
	public BEDCoord(String line) {
		String[] bed = line.split("\t");
		if(bed.length > -1) CHROM = bed[0];
		if(bed.length > 0) START = Integer.parseInt(bed[1]);
		if(bed.length > 1) STOP = Integer.parseInt(bed[2]);
		if(bed.length > 2) NAME = bed[3];
		if(bed.length > 3) {
			try { SCORE = Double.parseDouble(bed[4]); }
			catch (NumberFormatException e) { SCORE = 0; }
		}
		if(bed.length > 4) DIR = bed[5];
	}
	
	/**
	 * Instantiate object from coordinate values
	 * 
	 * @param c   the chromosome name
	 * @param sta start coordinate (0-based, inclusive)
	 * @param sto stop coordinate (0-based, exclusive)
	 * @param di  direction/strand information
	 * @param na  record name
	 */
	public BEDCoord(String c, long sta, long sto, String di, String na) {
		CHROM = c;
		START = sta;
		STOP = sto;
		DIR = di;
		NAME = na;
	}
	
	/**
	 * Instantiate nameless object from coordinate values
	 *
	 * @param c   the chromosome name
	 * @param sta start coordinate (0-based, inclusive)
	 * @param sto stop coordinate (0-based, exclusive)
	 * @param di  the direction/strand information
	 */
	public BEDCoord(String c, long sta, long sto, String di) {
		CHROM = c;
		START = sta;
		STOP = sto;
		DIR = di;
		NAME = CHROM + "_" + START + "_" + STOP + "_" + DIR;
	}
	
	/**
	 * Instantiate object with just name and score
	 * 
	 * @param na  record name
	 * @param sco record score
	 */
	public BEDCoord(String na, double sco) {
		NAME = na;
		SCORE = sco;
	}
	
	/**
	 * calculates a midpoint based on the start and stop coordinates, strand agnostic (left select if even interval)
	 */
	public void calcMid() {
		MID = (START + STOP) / 2;
	}

	/**
	 * set the midpoint coordinate
	 * 
	 * @param m midpoint coordinate (overwrite)
	 */
	public void setMid(int m) {
		MID = m;
	}

	/**
	 * get the stored midpoint coordinate
	 * 
	 * @return midpoint coordinate
	 */
	public long getMid() {
		return MID;
	}

	/**
	 * get forward/combined strand pileup counts
	 * 
	 * @return the stored forward/combined strand counts
	 */
	public double[] getFStrand() {
		return Fstrand;
	}

	/**
	 * get reverse strand pileup counts
	 * 
	 * @return the stored reverse strand counts
	 */
	public double[] getRStrand() {
		return Rstrand;
	}
	
	/**
	 * set forward/combined strand pileup counts
	 * 
	 * @param f a list of forward/combined strand counts
	 */
	public void setFstrand(double[] f) {
		Fstrand = f;
	}
	
	/**
	 * set reverse strand pileup counts
	 * @param r a list of reverse strand counts
	 */
	public void setRstrand(double[] r) {
		Rstrand = r;
	}
	
	/**
	 * get chromosome name
	 * 
	 * @return chromosome name
	 */
	public String getChrom() {
		return CHROM;
	}
	
	/**
	 * set chromosome name
	 * 
	 * @param chr chromosome name
	 */
	public void setChrom(String chr) {
		CHROM = chr;
	}
	
	/**
	 * get start coordinate
	 * 
	 * @return start coordinate (0-indexed, inclusive)
	 */
	public long getStart() {
		return START;
	}
	
	/**
	 * set start coordinate
	 * 
	 * @param sta start coordinate (0-indexed, inclusive)
	 */
	public void setStart(int sta) {
		START = sta;
	}
	
	/**
	 * get end coordinatec
	 * 
	 * @return end coordinate (0-indexed, inclusive)
	 */
	public long getStop() {
		return STOP;
	}
	
	/**
	 * set end coordinate
	 * 
	 * @param sto end coordinate (0-indexed, inclusive)
	 */
	public void setStop(int sto) {
		STOP = sto;
	}
	
	/**
	 * get strand direction
	 * 
	 * @return strand ("+"/"\-")
	 */
	public String getDir() {
		return DIR;
	}
	
	/**
	 * set strand direction
	 * 
	 * @param di strand ("+"/"\-")
	 */
	public void setDir(String di) {
		DIR = di;
	}
	
	/**
	 * get record name
	 * 
	 * @return record name
	 */
	public String getName() {
		return NAME;
	}
	
	/**
	 * set record name
	 * @param na record name
	 */
	public void setName(String na) {
		NAME = na;
	}
	
	/**
	 * get record score
	 * @return record score
	 */
	public double getScore() {
		return SCORE;
	}

	/**
	 * set record score
	 * @param sco record score
	 */
	public void setScore(double sco) {
		SCORE = sco;
	}
	
	/**
	 * format coordinate info in a BED-formatted string
	 * 
	 * @return coordinate info in BED-format
	 */
	public String toString() {
		String line = CHROM + "\t" + START + "\t" + STOP + "\t" + NAME + "\t" + SCORE + "\t" + DIR;
		return line;
	}

	/**
	 * Creates a new comparator which compares BEDCoords based on their chromosomes
	 */
	public static Comparator<BEDCoord> PeakChromComparator = new Comparator<BEDCoord>() {
		/**
		 * Compares two BEDCoords based on their chromosomes
		 * @param node1 First BEDCoord to comapare
		 * @param node2 Second BEDCoord to compare
		 * @return A negative value if node1 comes before node2, 0 if they have the same coordinate, and 1 if node1 comes after
		 */
		public int compare(BEDCoord node1, BEDCoord node2) {
			return node1.getChrom().compareTo(node2.getChrom());
		}
	};

	/**
	 * Creates a new comparator which compares BEDCoords based the coordinates on starting coordinates
	 */
	public static Comparator<BEDCoord> PeakPositionComparator = new Comparator<BEDCoord>() {
		/**
		 * Compares two BEDCoords based on starting coordinates
		 * @param node1 First BEDCoord to compare
		 * @param node2 Second BEDCoord to compare
		 * @return A negative value if node1 comes before node2, 0 if they have the same start, and 1 if node1 comes after
		 */
		public int compare(BEDCoord node1, BEDCoord node2) {
			long PeakStart1 = node1.getStart();
			long PeakStart2 = node2.getStart();
			if (PeakStart1 > PeakStart2) return 1;
			else if (PeakStart1 < PeakStart2) return -1;
			else return 0;
	}
	};

	/**
	 * Creates a new comparator which compares BEDCoords based the coordinates on their mid point
	 */
	public static Comparator<BEDCoord> PeakMidpointComparator = new Comparator<BEDCoord>() {
		/**
		 * Compares two BEDCoords based on the position of their mid point
		 * @param node1 First BEDCoord to compare
		 * @param node2 Second BEDCoord to compare
		 * @return A negative value if mid point of node1 comes before node2's, 0 if they have the same mid point, and 1 if node1's mid point comes after
		 */
		public int compare(BEDCoord node1, BEDCoord node2) {
			long PeakMid1 = (node1.getStart() + node1.getStop()) / 2;
			long PeakMid2 = (node2.getStart() + node2.getStop()) / 2;
			if (PeakMid1 > PeakMid2) return 1;
			else if (PeakMid1 < PeakMid2) return -1;
			else return 0;
	}
	};

	/**
	 * Creates a new comparator which compares BEDCoords based the coordinates on their score
	 */
	public static Comparator<BEDCoord> ScoreComparator = new Comparator<BEDCoord>() {
		/**
		 * Compares two BEDCoords based on the position of their score
		 * @param node1 First BEDCoord to compare
		 * @param node2 Second BEDCoord to compare
		 * @return A negative value if node1 has a higher score than node2, zero if they have equal scores, and a positive value if node1 has a lower score than node2
		 */
		public int compare(BEDCoord node1, BEDCoord node2) {
			double PeakStart1 = node1.getScore();
			double PeakStart2 = node2.getScore();
			if (PeakStart1 < PeakStart2) return 1;
			else if (PeakStart1 > PeakStart2) return -1;
			else return 0;
	}
	};
}
