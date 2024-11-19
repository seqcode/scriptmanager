package scriptmanager.objects.CoordinateObjects;

import java.util.Comparator;

/**
 * Object for storing reads of a GFF file
 * 
 * @author William KM Lai
 * @see scriptmanager.objects.CoordinateObjects.GenomicCoord
 */
public class GFFCoord implements GenomicCoord {
	private String CHROM = "";
	private String SOURCE = ".";
	private String FEATURE = ".";
	private long START = 0;
	private long STOP = 0;
	private double SCORE = 0;
	private String DIR = "+";
	private String FRAME = ".";
	private String ATTRIBUTE = ".";
	private long MID = 0;
	
	public String LINE = "";

	private double[] Fstrand = null;
	private double[] Rstrand = null;
	
	/**
	 * Creates a new GFFCoord with defualt values
	 */
	public GFFCoord() {
		
	}
	
	/**
	 * Creates a new GFFCoord object with a given String
	 * @param line String to parse in order to create GFFCoord file
	 */
	public GFFCoord(String line) {
		String[] gff = line.split("\t");
		if(gff.length > -1) CHROM = gff[0];
		if(gff.length > 0) SOURCE = gff[1];
		if(gff.length > 1) FEATURE = gff[2];
		if(gff.length > 2) START = Integer.parseInt(gff[3]);
		if(gff.length > 3) STOP = Integer.parseInt(gff[4]);
		if(gff.length > 4) {
			try { SCORE = Double.parseDouble(gff[5]); }
			catch (NumberFormatException e) { SCORE = 0; }
		}
		if(gff.length > 5) DIR = gff[6];
		if(gff.length > 6) FRAME = gff[7];
		if(gff.length > 7) ATTRIBUTE = gff[8];
		LINE = line;
	}
	
	/**
	 * Creates a new GFFCoord object
	 * @param c Chromosome name
	 * @param sta Start coordinate 
	 * @param sto Ending coordinate
	 * @param di Strand direction (+/-)
	 * @param na Name of coord
	 */
	public GFFCoord(String c, long sta, long sto, String di, String na) {
		CHROM = c;
		START = sta;
		STOP = sto;
		DIR = di;
		ATTRIBUTE = na;
	}
	
	/**
	 * Creates a new GFFCoord object
	 * @param c Chromosome name
	 * @param sta Start coordinate 
	 * @param sto Ending coordinate
	 * @param di Strand direction (+/-)
	 */
	public GFFCoord(String c, long sta, long sto, String di) {
		CHROM = c;
		START = sta;
		STOP = sto;
		DIR = di;
		ATTRIBUTE = CHROM + "_" + START + "_" + STOP + "_" + DIR;
	}
	
	/**
	 * Creates a new GFFCoord object
	 * @param na Name of the coord
	 * @param sco Score
	 */
	public GFFCoord(String na, double sco) {
		ATTRIBUTE = na;
		SCORE = sco;
	}
	
	/**
	 * Returns the Pileup scores for the forward strand
	 * @return The Pileup scores for the forward strand
	 */
	public double[] getFStrand() {
		return Fstrand;
	}
	
	/**
	 * Returns the Pileup scores for the reverse strand
	 * @return The Pileup scores for the reverse strand
	 */
	public double[] getRStrand() {
		return Rstrand;
	}
	
	/**
	 * Sets the Pileup scores for the forward strand
	 * @param f The Pileup scores for the forward strand
	 */
	public void setFstrand(double[] f) {
		Fstrand = f;
	}
	
	/**
	 * Sets the Pileup scores for the reverse strand
	 * @param r The Pileup scores for the reverse strand
	 */
	public void setRstrand(double[] r) {
		Rstrand = r;
	}
	
	/**
	 * Returns the chromosome name
	 * @return the stored chr name
	 */
	public String getChrom() {
		return CHROM;
	}
	
	/**
	 * Sets the chromosome name
	 * @param chr The new chromosome name
	 */
	public void setChrom(String chr) {
		CHROM = chr;
	}
	
	/**
	 * Returns the starting coordinate of the GFFCoord
	 * @return the starting coordinate of the BEDCord
	 */
	public long getStart() {
		return START;
	}
	
	/**
	 * Sets the starting coordinate of the GFFCoord
	 * @param sta The starting coordinate of the BEDCord
	 */
	public void setStart(int sta) {
		START = sta;
	}
	
	/**
	 * Returns the ending coordinate of the GFFCoord
	 * @return the ending coordinate of the GFFCoord
	 */
	public long getStop() {
		return STOP;
	}
	
	/**
	 * Sets the ending coordinate of the GFFCoord
	 * @param sto The ending coordinate of the GFFCoord
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
	 * Returns the name of the GFFCoord
	 * @return The name of the GFFCoord
	 */
	public String getName() {
		return ATTRIBUTE;
	}
	
	/**
	 * Sets the name of the GFFCoord
	 * @param na The name of the GFFCoord
	 */
	public void setName(String na) {
		ATTRIBUTE = na;
	}
	
	/**
	 * Returns the score of the GFFCoord
	 * @return The score of the GFFCoord
	 */
	public double getScore() {
		return SCORE;
	}
	
	/**
	 * Sets the score of the GFFCoord
	 * @param sco The new score of the GFFCoord
	 */
	public void setScore(double sco) {
		SCORE = sco;
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
	 * Returns the different variables of a GFFCoord represented by a string
	 * @return The different variables of a GFFCoord represented by a string
	 */
	public String toString() {
		//chr1	cwpair	.	45524	45525	3067.0	.	.	cw_distance=26
		String line = CHROM + "\t" + SOURCE + "\t" + FEATURE + "\t" + START + "\t" + STOP + "\t" + SCORE + "\t" + DIR + "\t" + FRAME + "\t" + ATTRIBUTE;
		return line;
	}
	
	/**
	 * Creates a new comparator which compares GFFCoords based on their chromosomes
	 */
	public static Comparator<GFFCoord> PeakChromComparator = new Comparator<GFFCoord>() {
		/**
		 * Compares two GFFCoords based on their chromosomes
		 * @param node1 First GFFCoord to comapare
		 * @param node2 Second GFFCoord to compare
		 * @return A negative value if node1 comes before node2, 0 if they have the same coordinate, and 1 if node1 comes after
		 */
		public int compare(GFFCoord node1, GFFCoord node2) {
			return node1.getChrom().compareTo(node2.getChrom());
	}
	};
		
	/**
	 * Creates a new comparator which compares GFFCoords based the coordinates on starting coordinates
	 */
	public static Comparator<GFFCoord> PeakPositionComparator = new Comparator<GFFCoord>() {
		/**
		 * Compares two GFFCoords based on starting coordinates
		 * @param node1 First GFFCoord to compare
		 * @param node2 Second GFFCoord to compare
		 * @return A negative value if node1 comes before node2, 0 if they have the same start, and 1 if node1 comes after
		 */
		public int compare(GFFCoord node1, GFFCoord node2) {
			long PeakStart1 = node1.getStart();
			long PeakStart2 = node2.getStart();
			if (PeakStart1 > PeakStart2) return 1;
			else if (PeakStart1 < PeakStart2) return -1;
			else return 0;
	}
	};
	
	/**
	 * Creates a new comparator which compares GFFCoords based the coordinates on their score
	 */
	public static Comparator<GFFCoord> ScoreComparator = new Comparator<GFFCoord>() {
		/**
		 * Compares two GFFCoords based on the position of their score
		 * @param node1 First GFFCoord to compare
		 * @param node2 Second GFFCoord to compare
		 * @return A negative value if node1 has a higher score than node2, zero if they have equal scores, and a positive value if node1 has a lower score than node2
		 */
		public int compare(GFFCoord node1, GFFCoord node2) {
			double PeakStart1 = node1.getScore();
			double PeakStart2 = node2.getScore();
			if (PeakStart1 < PeakStart2) return 1;
			else if (PeakStart1 > PeakStart2) return -1;
			else return 0;
	}
	};
}
