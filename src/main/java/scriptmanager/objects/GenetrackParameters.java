package scriptmanager.objects;

/**
 * Object for tracking accessing GeneTrack parameters
 *
 * @author William KM Lai
 * @see scriptmanager.scripts.Peak_Calling.GeneTrack_BAM
 * @see scriptmanager.scripts.Peak_Calling.GeneTrack
 */
public class GenetrackParameters {

	int SIGMA = 5;
	int EXCLUSION = 10;
	int FILTER = 1;
	int UP = -999;
	int DOWN = -999;
	int READ = 0;
	String NAME = "";

	/**
	 * Initialize object without attributes set
	 */
	public GenetrackParameters() {

	}

	/**
	 * set sigma to use when smoothing reads to call peaks
	 *
	 * @param s the sigma value
	 */
	public void setSigma(int s) {
		SIGMA = s;
	}
	/**
	 * get sigma to use when smoothing reads to call peaks
	 *
	 * @return the stored sigma value
	 */
	public int getSigma() {
		return SIGMA;
	}

	/**
	 * set exclusion zone around each peak that prevents others from being called
	 *
	 * @param e the exclusion distance in bp
	 */
	public void setExclusion(int e) {
		EXCLUSION = e;
	}
	/**
	 * get exclusion zone around each peak that prevents others from being called
	 *
	 * @return the stored exclusion distance in bp
	 */
	public int getExclusion() {
		return EXCLUSION;
	}

	/**
	 * set the absolute read filter (output only peaks with larger read count)
	 *
	 * @param f the read count filter threshold
	 */
	public void setFilter(int f) {
		FILTER = f;
	}
	/**
	 * get the absolute read filter (output only peaks with larger read count)
	 *
	 * @return the stored read count filter threshold
	 */
	public int getFilter() {
		return FILTER;
	}

	/**
	 * set upstream width of called peaks
	 *
	 * @param u the upstream width
	 */
	public void setUp(int u) {
		UP = u;
	}
	/**
	 * get upstream width of called peaks
	 *
	 * @return the stored upstream width
	 */
	public int getUp() {
		return UP;
	}

	/**
	 * set downstream width of called peaks
	 *
	 * @param d the downstream width
	 */
	public void setDown(int d) {
		DOWN = d;
	}
	/**
	 * get downstream width of called peaks
	 *
	 * @return the stored downstream width
	 */
	public int getDown() {
		return DOWN;
	}

	/**
	 * set read encoding (used by GeneTrack_BAM)
	 *
	 * @param r the read encoding (read1=0, read2=1, combined=2)
	 */
	public void setRead(int r) {
		READ = r;
	}
	/**
	 * get read encoding (used by GeneTrack_BAM)
	 *
	 * @return the stored read encoding (read1=0, read2=1, combined=2)
	 */
	public int getRead() {
		return READ;
	}

	/**
	 * set filename output path
	 *
	 * @param n the output filename
	 */
	public void setName(String n) {
		NAME = n;
	}
	/**
	 * get filename output path
	 *
	 * @return the stored output filename
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * Returns the different variables of a GenetrackParameter represented by a string
	 * @return The different variables of a GenetrackParameter represented by a string
	 */
	public String toString() {
		String temp = "genetrack_s" + SIGMA + "e" + EXCLUSION;
		if(UP != -999) temp += "u" + UP;
		if(DOWN != -999) temp += "d" + DOWN;
		temp += "F" + FILTER;
		return temp;
	}
}
