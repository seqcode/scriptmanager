package scriptmanager.objects.CoordinateObjects;

/**
 * Interface used by coordinate classes
 * 
 * @author William KM Lai
 * @see scriptmanager.objects.CoordinateObjects.BEDCoord
 * @see scriptmanager.objects.CoordinateObjects.GenericCoord
 * @see scriptmanager.objects.CoordinateObjects.GFFCoord
 */
public interface GenomicCoord {
	/**
	 * Returns the chromosome name
	 * @return the stored chromosome
	 */
	public String getChrom();	
	/**
	 * Sets the chromosome name
	 * @param chr The new chromosome
	 */
	public void setChrom(String chr);
	
	/**
	 * Returns the starting coordinate of the GenomicCoord
	 * @return the starting coordinate of the GenomicCoord
	 */
	public long getStart();
	/**
	 * Sets the starting coordinate of the GenomicCoord
	 * @param sta The starting coordinate of the GenomicCoord
	 */
	public void setStart(int sta);
	
	/**
	 * Returns the ending coordinate of the GenomicCoord
	 * @return the ending coordinate of the GenomicCoord
	 */
	public long getStop();
	/**
	 * Sets the ending coordinate of the GenomicCoord
	 * @param sto The ending coordinate of the GenomicCoord
	 */
	public void setStop(int sto);
	
	/**
	 * Returns the score of the BEDCoord
	 * @return The score of the BEDCoord
	 */
	public double getScore();	
	/**
	 * Sets the score of the BEDCoord
	 * @param sco The new score of the BEDCoord
	 */
	public void setScore(double sco);
	
	/**
	 * Returns the strand direction
	 * @return the strand direction (+/-)
	 */
	public String getDir();	
	/**
	 * Sets the strand direction
	 * @param di The strand direction (+/-)
	 */
	public void setDir(String di);
	
	/**
	 * Returns the name of the BEDCoord
	 * @return The name of the BEDCoord
	 */
	public String getName();
	/**
	 * Sets the name of the BEDCoord
	 * @param na The name of the BEDCoord
	 */
	public void setName(String na);

	public long getMid();
	public void calcMid();

	/**
	 * Default, Object toString() method
	 * @return A string representation of the object.
	 */
	public String toString();

}
