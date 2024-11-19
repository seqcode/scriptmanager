package scriptmanager.objects.CoordinateObjects;

import java.util.Comparator;

/**
 * Object for storing generic reads
 * 
 * @author William KM Lais
 * @see scriptmanager.objects.CoordinateObjects.GenomicCoord
 */
public class GenericCoord implements GenomicCoord {
	private String CHROM = "";
	private long START = 0;
	private long STOP = 0;
	private String DIR = "+";
	private long MID = 0;
	
	private String NAME = ".";
	private double SCORE = 0;

	/**
	 * Creates a new GenericCoord with default values
	 */
	public GenericCoord() {
		
	}
	
	/**
	 * Creates a new GenericCoord 
	 * @param c Chromosome name
	 * @param sta Start coordinate 
	 * @param sto Ending coordinate
	 * @param di Strand direction (+/-)
	 */
	public GenericCoord(String c, long sta, long sto, String di) {
		CHROM = c;
		START = (int)sta;
		STOP = (int)sto;
		DIR = di;
		NAME = CHROM + "_" + START + "_" + STOP + "_" + DIR;
	}
	
	/**
	 * Creates a new GenericCoord
	 * @param c Chromosome name
	 * @param sta Start coordinate 
	 * @param sto Ending coordinate
	 * @param di Strand direction (+/-)
	 * @param na Name of coord
	 */
	public GenericCoord(String c, int sta, int sto, String di, String na) {
		CHROM = c;
		START = sta;
		STOP = sto;
		DIR = di;
		NAME = na;
	}

	public void calcMid() {
		MID = (START + STOP) / 2;
	}
	
	public void setMid(int m) {
		MID = m;
	}
	
	public long getMid() {
		return MID;
	}
	
	/**
	 * Returns the chromsome of the Coord
	 * @return The chromosome of the Coord
	 */
	public String getChrom() {
		return CHROM;
	}
	
	/**
	 * Sets the chromosome of the Coord
	 * @param chr The new chromosome of the Coord
	 */
	public void setChrom(String chr) {
		CHROM = chr;
	}
	
	/**
	 * Returns the starting point of the Coord
	 * @return the starting point of the Coord
	 */
	public long getStart() {
		return START;
	}
	
	/**
	 * Sets the starting coordinate of the Coord
	 * @param sta The starting coordinate of the Coord
	 */
	public void setStart(int sta) {
		START = sta;
	}
	
	/**
	 * Returns the ending coordinate of the Coord
	 * @return the ending coordinate of the Coord
	 */
	public long getStop() {
		return STOP;
	}
	
	/**
	 * Sets the ending coordinate of the Coord
	 * @param sto The ending coordinate of the Coord
	 */
	public void setStop(int sto) {
		STOP = sto;
	}
	
	/**
	 * Returns the strand direction
	 * @return the strand direction (+/-)
	 */
	public String getDir() {
		return DIR;
	}
	
	/**
	 * Sets the strand direction
	 * @param di The strand direction (+/-)
	 */
	public void setDir(String di) {
		DIR = di;
	}
	
	/**
	 * Returns the name of the Coord
	 * @return The name of the Coord
	 */
	public String getName() {
		return NAME;
	}
	
	/**
	 * Sets the name of the Coord
	 * @param na The name of the Coord
	 */
	public void setName(String na) {
		NAME = na;
	}
	
	/**
	 * Returns the score of the Coord
	 * @return The score of the Coord
	 */
	public double getScore() {
		return SCORE;
	}
	
	/**
	 * Sets the score of the Coord
	 * @param sco The new score of the Coord
	 */
	public void setScore(double sco) {
		SCORE = sco;
	}
	
	/**
	 * Returns the different variables of a Coord represented by a string
	 * @return The different variables of a Coord represented by a string
	 */
	public String toString() {
		String line = CHROM + "\t" + START + "\t" + STOP + "\t" + NAME + "\t" + SCORE + "\t" + DIR;
		return line;
	}
	
	/**
	 * Creates a new comparator which compares GenericCoords based on their chromosomes
	 */
	public static Comparator<GenericCoord> PeakChromComparator = new Comparator<GenericCoord>() {
		/**
		 * Compares two GenericCoords based on their chromosomes
		 * @param node1 First GenericCoord to comapare
		 * @param node2 Second GenericCoord to compare
		 * @return A negative value if node1 comes before node2, 0 if they have the same coordinate, and 1 if node1 comes after
		 */
		public int compare(GenericCoord node1, GenericCoord node2) {
			return node1.getChrom().compareTo(node2.getChrom());
	}
	};
	
	/**
	 * Creates a new comparator which compares GenericCoords based the coordinates on starting coordinates
	 */
	public static Comparator<GenericCoord> PeakPositionComparator = new Comparator<GenericCoord>() {
		/**
		 * Compares two GenericCoords based on starting coordinates
		 * @param node1 First GenericCoord to compare
		 * @param node2 Second GenericCoord to compare
		 * @return A negative value if node1 comes before node2, 0 if they have the same start, and 1 if node1 comes after
		 */
		public int compare(GenericCoord node1, GenericCoord node2) {
			long PeakStart1 = node1.getStart();
			long PeakStart2 = node2.getStart();
			if (PeakStart1 > PeakStart2) return 1;
			else if (PeakStart1 < PeakStart2) return -1;
			else return 0;
	}
	};

	/**
	 * Creates a new comparator which compares GenericCoords based the coordinates on their score
	 */
	public static Comparator<GenericCoord> ScoreComparator = new Comparator<GenericCoord>() {
		/**
		 * Compares two GenericCoords based on the position of their score
		 * @param node1 First GenericCoord to compare
		 * @param node2 Second GenericCoord to compare
		 * @return A negative value if node1 has a higher score than node2, zero if they have equal scores, and a positive value if node1 has a lower score than node2
		 */
		public int compare(GenericCoord node1, GenericCoord node2) {
			double PeakStart1 = node1.getScore();
			double PeakStart2 = node2.getScore();
			if (PeakStart1 < PeakStart2) return 1;
			else if (PeakStart1 > PeakStart2) return -1;
			else return 0;
	}
	};
}
