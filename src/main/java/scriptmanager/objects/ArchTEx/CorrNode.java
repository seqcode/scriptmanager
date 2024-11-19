package scriptmanager.objects.ArchTEx;

import java.util.Vector;

/**
 * Object to help store ArchTEx Cross-Correlation analysis values.
 * <br>
 * Code largely sourced from ArchTEx.analysis.corr.CorrNode in <a href=
 * "https://github.com/WilliamKMLai/ArchTEx">https://github.com/WilliamKMLai/ArchTEx</a>
 * 
 * @author William KM Lai
 * @see scriptmanager.objects.ArchTEx.CorrExtract
 * @see scriptmanager.scripts.BAM_Statistics.CrossCorrelation
 */
public class CorrNode {
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
	private double[] Sxy = new double[1001];
	private double counter = 0;

	/**
	 * Creates a new CorrNode with default values
	 */
	public CorrNode () {
		
	}

	/**
	 * Creates a new CorrNode with specified values
	 * @param chr Chromosome to be assigned to CorrNode
	 * @param start Starting coordinate
	 * @param stop Ending coordinate
	 */
	public CorrNode (String chr, int start, int stop) {
		CHROM = chr;
		BP_START = start;
		BP_STOP = stop;
	}

	/**
	 * Sets the count of a given node
	 * @param count New count
	 */
	public void setCount(double count) {
		counter = count;
	}

	/**
	 * Sets the sample standard deviation of x
	 * @param newsx New sample standard deviation of x
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
	 * @param newsy New sample standard deviation of y
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
	 * Sets the sample standard deviation of x * sample standard deviation of y for a given index in the Sxy array
	 * @param index Index to update
	 * @param newsxy The new Sxy value at the given index
	 */
	public void setSxy(int index, double newsxy) {
		Sxy[index] = newsxy;
	}

	/**
	 * Sets assigns the Sxy array to a given array
	 * @param newsxy The the new array to represent the sample standard deviations of x * sample standard deviations of y
	 */
	public void setSxy(double[] newsxy) {
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
	 * Returns the sample standard deviations of x * sample standard deviations of y
	 * @return The sample standard deviations of x * sample standard deviations of y 
	 */
	public double[] getSxy() {
		return Sxy;
	}

	/**
	 * Sets the tag coordinates to a given Vector
	 * @param tags A list of tagged coordinates, represented as doubles
	 */
	public void setData(Vector<double[]> tags) {
		tagPopulation = tags;
	}

	/**
	 * Returns the Vector of compared tags
	 * @return the Vector of compared tags
	 */
	public Vector<double[]> getData() {
		return tagPopulation;
	}

	/**
	 * Returns the ID of the CoorNode
	 * @return The ID of the CoorNode
	 */
	public String getID() {
		return uniqID;		
	}

	/**
	 * Sets the id of the CorrNode
	 * @param newid The new ID of the CorrNode
	 */
	public void setID(String newid) {
		uniqID = newid;		
	}

	/**
	 * Returns the chromosome of the CorrNode
	 * @return The chromsome of the CorrNode
	 */
	public String getChrom() {
		return CHROM;		
	}

	/**
	 * Sets the chromosome of the CorrNode
	 * @param newchr The new chromosome ID of the CorrNode
	 */
	public void setChrom(String newchr) {
		CHROM = newchr;		
	}

	/**
	 * Returns the ending coordinate
	 * @return The ending coordinate
	 */
	public int getStop() {
		return BP_STOP;		
	}

	/**
	 * Sets the ending coordinate
	 * @param newbp The new ending coordinate
	 */
	public void setStop(int newbp) {
		BP_STOP = newbp;		
	}

	/**
	 * Returns the starting coordinate
	 * @return The starting coordinate
	 */
	public int getStart() {
		return BP_START;		
	}

	/**
	 * Sets the starting coordinate
	 * @param newbp The new starting coordinate
	 */
	public void setStart(int newbp) {
		BP_START = newbp;		
	}
}