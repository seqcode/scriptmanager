package scriptmanager.scripts.BAM_Statistics.CorrelationScripts;

import java.util.Vector;

/**
 * Stores one instance of correlation data for a specific chromosome
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.BAMGenomeCorrelation
 * @see scriptmanager.scripts.BAM_Statistics.CorrelationScripts.CorrelationExtract
 */
public class CorrelationCoord {
	private String uniqID = "";
	private String CHROM = "";
	private int BP_START = 0;
	private int BP_STOP = 0;
	private Vector<double[]> tagPopulation;
	
	//Statistics
	private double Sx = 0;
	private double Sxx = 0;
	private double Sy = 0;
	private double Syy = 0;
	private double Sxy = 0;
	private double counter = 0;
	
	/**
	 * Makes a new CorrelationCoord
	 */
	public CorrelationCoord () {
		tagPopulation = new Vector<double[]>();
	}

	/**
	 * Makes a new CorrelationCoord with a given chromosome, start, and stop 
	 * @param chr Character assigned to the chromosome
	 * @param start Index of start BP
	 * @param stop Index of stop BP
	 */
	public CorrelationCoord (String chr, int start, int stop) {
		CHROM = chr;
		BP_START = start;
		BP_STOP = stop;
		tagPopulation = new Vector<double[]>();
	}
	
	/**
	 * Sets the total number of comparisons between the two samples
	 * @param count The new total number of comparisons between the two samples
	 */
	public void setCount(double count) {
		counter = count;
	}
	
	/**
	 * Sets the sample standard deviation of x
	 * @param newsx The new sample standard deviation of x
	 */
	public void setSx(double newsx) {
		Sx = newsx;
	}
	
	/**
	 * Sets the sample variance of x
	 * @param newsxx The new sample variance of x
	 */
	public void setSxx(double newsxx) {
		Sxx = newsxx;
	}
	
	/**
	 * Sets the sample standard deviation of y
	 * @param newsy The new sample standard deviation of y
	 */
	public void setSy(double newsy) {
		Sy = newsy;
	}
	
	/**
	 * Sets the sample variance of y
	 * @param newsyy The new sample variance of y
	 */
	public void setSyy(double newsyy) {
		Syy = newsyy;
	}
	
	/**
	 * Sets the sample standard deviation of x * sample standard deviation of y
	 * @param newsxy The sample standard deviation of x * sample standard deviation of y
	 */
	public void setSxy(double newsxy) {
		Sxy = newsxy;
	}
	
	/**
	 * Returns the total number of comparisons
	 * @return The total number of comparisons
	 */
	public double getCount() {
		return counter;
	}
	
	/**
	 * Returns the sample standard deviation of x
	 * @return The sample standard deviation of x
	 */
	public double getSx() {
		return Sx;
	}
	
	/**
	 * Returns the sample variance of x
	 * @return The sample variance of x
	 */
	public double getSxx() {
		return Sxx;
	}
	
	/**
	 * Returns the sample standard deviation of y
	 * @return The sample standard deviation of y
	 */
	public double getSy() {
		return Sy;
	}
	
	/**
	 * Returns the sample variance of y
	 * @return The sample variance of y
	 */
	public double getSyy() {
		return Syy;
	}
	
	/**
	 * Returns the sample standard deviation of x * sample standard deviation of y
	 * @return The sample standard deviation of x * sample standard deviation of y
	 */
	public double getSxy() {
		return Sxy;
	}
	
	/**
	 * Adds list of compared tags
	 * @param tags The list of compared tags
	 */
	public void addData(double[] tags) {
		tagPopulation.add(tags);
	}
	
	/**
	 * Sets the list of compared tags to a given Vector of tags
	 * @param tags Vector of compared tags
	 */
	public void setData(Vector<double[]> tags) {
		tagPopulation = tags;
	}
	
	/**
	 * Returns a Vector of compared tags
	 * @return The vector of compared tags
	 */
	public Vector<double[]> getData() {
		return tagPopulation;
	}
	
	/**
	 * Returns ID of the CorrelationCoord
	 * @return ID of the CorrelationCoord
	 */
	public String getID() {
		return uniqID;		
	}
	
	/**
	 * Sets the new ID of the CoorelationCoord
	 * @param newid new ID of the CoorelationCoord
	 */
	public void setID(String newid) {
		uniqID = newid;		
	}
	
	/**
	 * Returns the string designating the chromosome
	 * @return string designating the chromosome
	 */
	public String getChrom() {
		return CHROM;		
	}
	
	/**
	 * Sets character designating the chromosome
	 * @param newchr The new character
	 */
	public void setChrom(String newchr) {
		CHROM = newchr;		
	}
	
	/**
	 * Returns the index of the ending bp
	 * @return index of the ending bp
	 */
	public int getStop() {
		return BP_STOP;		
	}
	
	/**
	 * Sets the index of the starting bp
	 * @param newbp index of the starting bp
	 */
	public void setStop(int newbp) {
		BP_STOP = newbp;		
	}
	
	/**
	 * Returns the index of the starting bp
	 * @return index of the starting bp
	 */
	public int getStart() {
		return BP_START;		
	}
	
	/**
	 * Sets the index of the starting bp
	 * @param newbp index of the starting bp
	 */
	public void setStart(int newbp) {
		BP_START = newbp;		
	}
}
