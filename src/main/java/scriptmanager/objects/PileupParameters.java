package scriptmanager.objects;

import java.io.File;
import java.io.PrintStream;

import scriptmanager.objects.Exceptions.OptionException;

/**
 * Object for storing pileup-related parameter information and constants.
 *
 * @author William KM Lai
 * @see scriptmanager.scripts.Read_Analysis.TagPileup
 * @see scriptmanager.scripts.Read_Analysis.PileupScripts.PileupExtract
 * @see scriptmanager.cli.Read_Analysis.TagPileupCLI
 * @see scriptmanager.window_interface.Read_Analysis.TagPileupWindow
 */
public class PileupParameters {
	/**
	 * Read Aspect value encoding 5 prime end
	 */
	final public static int FIVE = 0;
	/**
	 * Read Aspect value encoding 3 prime end
	 */
	final public static int THREE = 1;
	/**
	 * Read Aspect value encoding midpoint
	 */
	final public static int MIDPOINT = 2;
	/**
	 * Read Aspect value encoding = full fragment
	 */
	final public static int FRAGMENT = 3;

	/**
	 * Read Type value encoding Read 1 (R1)
	 */
	final public static int READ1 = 0;
	/**
	 * Read Type value encoding Read 2 (R2)
	 */
	final public static int READ2 = 1;
	/**
	 * Read Type value encoding all reads
	 */
	final public static int ALLREADS = 2;

	/**
	 * Strand value encoding separate strand info
	 */
	final public static int SEPARATE = 0;
	/**
	 * Strand value encoding combining strand info
	 */
	final public static int COMBINED = 1;

	/**
	 * Transformation type value encoding no smoothing
	 */
	final public static int NO_SMOOTH = 0;
	/**
	 * Transformation type value encoding sliding window smoothing
	 */
	final public static int WINDOW = 1;
	/**
	 * Transformation type value encoding gaussian smoothing
	 */
	final public static int GAUSSIAN = 2;

	/**
	 * Output matrix type value encoding no file outputs
	 */
	final public static int NO_OUTPUT = 0;
	/**
	 * Output matrix type value encoding tab-delimited matrix output
	 */
	final public static int TAB = 1;
	/**
	 * Output matrix type value encoding cdt-formatted matrix output
	 */
	final public static int CDT = 2;

	private int ASPECT = PileupParameters.FIVE;
	private int READ = PileupParameters.READ1;
	private int STRAND = PileupParameters.SEPARATE;
	private int TRANS = PileupParameters.NO_SMOOTH;

	//TRANS=1 parameters:  window size (#bins)
	private int SMOOTH = 0;
	//TRANS=2 parameters:  stdev window size (#bins) and number of standard deviations
	private int STDSIZE = 0;
	private int STDNUM = 0;

	private boolean requirePE = false;
	private int MIN_INSERT = -9999;
	private int MAX_INSERT = -9999;

	private int SHIFT = 0;

	/**
	 * Bin size (in bp). Defaults to 1.
	 */
	private int BIN = 1;
	private int TAGEXTEND = 0;

	private boolean STANDARD = false;
	private double STANDRATIO = 1; // fixed for now...
	private File BLACKLIST = null;

	// Output Matrix options
	private int OUTTYPE = PileupParameters.NO_OUTPUT;
	private boolean outputGZIP = false;
	private File OUTPUT = null;

	// Output composite values file if output
	private boolean outputCOMPOSITE = false;  //Output composite values
	private PrintStream COMPOSITE = null;

	private int CPU = 1;

	/**
	 * Print all PileupParameters attributes (mainly for debugging purposes).
	 */
	public void printAll(){
		System.out.println( "<><><><><><><><><><><><><><><><><><><><>" );
		System.out.println( "private File OUTPUT = " + OUTPUT );
		System.out.println( "private String COMPOSITE = " + COMPOSITE );
		System.out.println( "private int READ = " + READ );
		System.out.println( "private int ASPECT = " + ASPECT );
		System.out.println( "private int STRAND = " + STRAND );
		System.out.println( "private int TRANS = " + TRANS );
		System.out.println( "private int SHIFT = " + SHIFT );
		System.out.println( "private int BIN = " + BIN );
		System.out.println( "private int TAGEXTEND = " + TAGEXTEND );
		System.out.println( "private int SMOOTH = " + SMOOTH );
		System.out.println( "private int STDSIZE = " + STDSIZE );
		System.out.println( "private int STDNUM = " + STDNUM );
		System.out.println( "private int CPU = " + CPU );
		System.out.println( "private int OUTTYPE = " + OUTTYPE );
		System.out.println( "private File BLACKLIST = " + BLACKLIST );
		System.out.println( "private boolean STANDARD = " + STANDARD );
		System.out.println( "private boolean outputCOMPOSITE = " + outputCOMPOSITE );
		System.out.println( "private boolean outputGZIP = " + outputGZIP );
		System.out.println( "private boolean requirePE = " + requirePE );
		System.out.println( "private double STANDRATIO = " + STANDRATIO );
		System.out.println();
		System.out.println( "private int MIN_INSERT = " + MIN_INSERT );
		System.out.println( "private int MAX_INSERT = " + MAX_INSERT );
		System.out.println();
		System.out.println( "<><><><><><><><><><><><><><><><><><><><>" );
	}

	public String getAspectString() throws OptionException {
		switch (ASPECT) {
			case PileupParameters.FIVE:
				return("Read Aspect: Five");
			case PileupParameters.THREE:
				return("Read Aspect: Three");
			case PileupParameters.MIDPOINT:
				return("Read Aspect: Midpoint");
			case PileupParameters.FRAGMENT:
				return("Read Aspect: Fragment");
		}
		throw new OptionException("Unknown PileupParameters read aspect value: " + ASPECT);
	}

	public String getReadString() throws OptionException {
		switch (READ) {
			case PileupParameters.READ1:
				return("Read Output: Read1");
			case PileupParameters.THREE:
				return("Read Output: Read2");
			case PileupParameters.MIDPOINT:
				return("Read Output: All Reads");
		}
		throw new OptionException("Unknown PileupParameters read output value: " + READ);
	}

	/**
	 * get whether to gzip matrix output or not
	 *
	 * @return true means to gzip, false means flat text
	 */
	public boolean getOutputGZIP() {
		return outputGZIP;
	}
	/**
	 * set whether to gzip matrix output or not
	 *
	 * @param status true means to gzip, false means flat text
	 */
	public void setGZIPstatus(boolean status) {
		outputGZIP = status;
	}

	/**
	 * get the file (BED-formatted for TagPileup) of regions to exclude for set tags to be equal calculation
	 *
	 * @return the stored file of blacklisted regions
	 */
	public File getBlacklist() {
		return BLACKLIST;
	}
	/**
	 * set the file (BED-formatted for TagPileup) of regions to exclude for set tags to be equal calculation
	 *
	 * @param black the file of blacklisted regions
	 */
	public void setBlacklist(File black) {
		BLACKLIST = black;
	}

	/**
	 * get stored parameter value for maximum dna fragment insert size
	 *
	 * @return the stored max bp length
	 */
	public int getMaxInsert() {
		return MAX_INSERT;
	}
	/**
	 * set stored parameter value for maximum dna fragment insert size
	 *
	 * @param max the max bp length
	 */
	public void setMaxInsert(int max) {
		MAX_INSERT = max;
	}

	/**
	 * get stored parameter value for minimum dna fragment insert size
	 *
	 * @return the stored min bp length
	 */
	public int getMinInsert() {
		return MIN_INSERT;
	}
	/**
	 * set stored parameter value for minimum dna fragment insert size
	 *
	 * @param min the min bp insert size length
	 */
	public void setMinInsert(int min) {
		MIN_INSERT = min;
	}

	/**
	 * get stored parameter value to require read to have a read mate/come from
	 * paired-end data (see <a href=
	 * "https://www.javadoc.io/static/com.github.samtools/htsjdk/1.139/htsjdk/samtools/SAMRecord.html#getReadPairedFlag--">HTSJDK
	 * SAMRecord method</a>
	 *
	 * @return the stored require paired-end value
	 */
	public boolean getPErequire() {
		return requirePE;
	}
	/**
	 * set stored parameter value to require read to have a read mate/come from
	 * paired-end data (see <a href=
	 * "https://www.javadoc.io/static/com.github.samtools/htsjdk/1.139/htsjdk/samtools/SAMRecord.html#getReadPairedFlag--">HTSJDK
	 * SAMRecord method</a>
	 *
	 * @param status true if require paired-end, false if not
	 */
	public void setPErequire(boolean status) {
		requirePE = status;
	}

	/**
	 * get composite output PrintStream object to save composite output destination
	 * (needed for bulk processing of GUI TagPileup)
	 *
	 * @return the destination to write composite information to
	 */
	public PrintStream getCompositePrintStream() {
		return COMPOSITE;
	}
	/**
	 * set composite output PrintStream object to save composite output destination
	 * (needed for bulk processing of GUI TagPileup)
	 *
	 * @param comp the stored destination to write composite information to
	 */
	public void setCompositePrintStream(PrintStream comp) {
		COMPOSITE = comp;
	}

	/**
	 * get value for whether or not to print composite data
	 *
	 * @return the stored value
	 */
	public boolean getOutputCompositeStatus() {
		return outputCOMPOSITE;
	}
	/**
	 * set value for whether or not to print composite data
	 *
	 * @param out the value to set
	 */
	public void setOutputCompositeStatus(boolean out) {
		outputCOMPOSITE = out;
	}

	/**
	 * Returns the standardization ratio (default value = 1)
	 * @return The standardization ration
	 */
	public double getRatio() {
		return STANDRATIO;
	}
	
	/**
	 * Sets the standardization ratio
	 * @param rat New value of STANDRATIO
	 */
	public void setRatio(double rat) {
		STANDRATIO = rat;
	}

	/**
	 * Returns Whether to perform Tag Standardization
	 * @return Whether to perform Tag Standardization (default false)
	 */
	public boolean getStandard() {
		return STANDARD;
	}
	
	/**
	 * Sets if Tag Standardization should be performed
	 * @param stand New value of STANDARD
	 */
	public void setStandard(boolean stand) {
		STANDARD = stand;
	}

	/**
	 * get stored parameter value for what kind of matrix output to write
	 *
	 * @return the stored matrix output type encoding
	 */
	public int getOutputType() {
		return OUTTYPE;
	}
	/**
	 * set stored parameter value for what kind of matrix output to write
	 *
	 * @param newtype the matrix output type encoding
	 */
	public void setOutputType(int newtype) {
		OUTTYPE = newtype;
	}

	/**
	 * get directory to save matrix (and composite when using GUI) into
	 *
	 * @return the stored directory to save output to
	 */
	public File getOutputDirectory() {
		return OUTPUT;
	}
	/**
	 * set directory to save matrix (and composite when using GUI) into
	 * @param oUTPUT the directory to save output to
	 */
	public void setOutputDirectory(File oUTPUT) {
		OUTPUT = oUTPUT;
	}

	/**
	 * get stored parameter value for read aspect (5' end, 3' end, midpoint, or full fragment)
	 *
	 * @return the stored read aspect encoding
	 */
	public int getAspect() {
		return ASPECT;
	}
	/**
	 * set stored parameter value for read aspect (5' end, 3' end, midpoint, or full fragment)
	 *
	 * @param aSPECT the read aspect encoding
	 */
	public void setAspect(int aSPECT) {
		ASPECT = aSPECT;
	}

	/**
	 * get stored parameter value for read type. Can be one of: {@code PileupParameters.READ1}, {@code PileupParameters.READ2}, or {@code PileupParameters.ALLREADS}
	 *
	 * @return the stored read type encoding
	 */
	public int getRead() {
		return READ;
	}
	/**
	 * set stored parameter value for read type. Can be one of: {@code PileupParameters.READ1}, {@code PileupParameters.READ2}, or {@code PileupParameters.ALLREADS}
	 *
	 * @param rEAD the read type encoding
	 */
	public void setRead(int rEAD) {
		READ = rEAD;
	}

	/**
	 * get stored parameter value for strandedness. Can be one of: {@code PileupParameters.SEPARATE} or {@code PileupParameters.COMBINED}
	 *
	 * @return the strand encoding
	 */
	public int getStrand() {
		return STRAND;
	}
	/**
	 * set stored parameter value for strandedness. Can be one of: {@code PileupParameters.SEPARATE} or {@code PileupParameters.COMBINED}
	 *
	 * @param sTRAND the stored strand encoding
	 */
	public void setStrand(int sTRAND) {
		STRAND = sTRAND;
	}

	/**
	 * get stored parameter value for smoothing transformation of composite. Can be one of: {@code PileupParameters.NO_SMOOTH}, {@code PileupParameters.WINDOW}, or {@code PileupParameters.GAUSSIAN}
	 *
	 * @return the transformation type encoding
	 */
	public int getTrans() {
		return TRANS;
	}
	/**
	 * set stored parameter value for smoothing transformation of composite. Can be one of: {@code PileupParameters.NO_SMOOTH}, {@code PileupParameters.WINDOW}, or {@code PileupParameters.GAUSSIAN}
	 *
	 * @param tRANS the stored transformation type encoding
	 */
	public void setTrans(int tRANS) {
		TRANS = tRANS;
	}

	/**
	 * get the bp shift direction and distance for a read alignment. For TagPileup,
	 * upstream(+) and downstream(-)...note this is different from the ShiftInterval
	 * tool's upstream(-) and downstream(+)
	 *
	 * @return the stored bp shift distance
	 */
	public int getShift() {
		return SHIFT;
	}
	/**
	 * set the bp shift direction and distance for a read alignment. For TagPileup,
	 * upstream(+) and downstream(-)...note this is different from the ShiftInterval
	 * tool's upstream(-) and downstream(+)
	 *
	 * @param sHIFT the bp shift distance
	 */
	public void setShift(int sHIFT) {
		SHIFT = sHIFT;
	}

	/**
	 * get the bin size in bp
	 *
	 * @return the stored bin size (bp)
	 */
	public int getBin() {
		return BIN;
	}
	/**
	 * set the bin size in bp
	 *
	 * @param bIN the bin size (bp)
	 */
	public void setBin(int bIN) {
		BIN = bIN;
	}

	/**
	 * get the distance to extend the tag by
	 * @return distance (bp)
	 */
	public int getTagExtend() {
		return TAGEXTEND;
	}
	/**
	 * set the distance to extend the tag by
	 *
	 * @param tAGEXTEND distance (bp)
	 */
	public void setTagExtend(int tAGEXTEND) {
		TAGEXTEND = tAGEXTEND;
	}

	/**
	 * get the smoothing window size. Used when transformation is set to sliding window mode ({@code PileupParameters.WINDOW})
	 *
	 * @return window size (bins)
	 */
	public int getSmooth() {
		return SMOOTH;
	}
	/**
	 * set the smoothing window size. Used when transformation is set to sliding window mode ({@code PileupParameters.WINDOW})
	 *
	 * @param sMOOTH window size (bins)
	 */
	public void setSmooth(int sMOOTH) {
		SMOOTH = sMOOTH;
	}

	/**
	 * get standard deviation size for smoothing. Used when transformation is set to gaussian mode ({@code PileupParameters.GAUSSIAN})
	 *
	 * @return standard deviations size (bins)
	 */
	public int getStdSize() {
		return STDSIZE;
	}
	/**
	 * set standard deviation size for smoothing. Used when transformation is set to gaussian mode ({@code PileupParameters.GAUSSIAN})
	 *
	 * @param sTDSIZE standard deviations size (bins)
	 */
	public void setStdSize(int sTDSIZE) {
		STDSIZE = sTDSIZE;
	}

	/**
	 * get number of standard deviations for smoothing. Used when transformation is set to gaussian mode ({@code PileupParameters.GAUSSIAN})
	 *
	 * @return number of standard deviations (bins)s
	 */
	public int getStdNum() {
		return STDNUM;
	}
	/**
	 * set number of standard deviations for smoothing. Used when transformation is set to gaussian mode ({@code PileupParameters.GAUSSIAN})
	 *
	 * @param sTDNUM number of standard deviations (bins)
	 */
	public void setStdNum(int sTDNUM) {
		STDNUM = sTDNUM;
	}

	/**
	 * get stored parameter value for number of CPUs to use
	 *
	 * @return the number of CPUs to use
	 */
	public int getCPU() {
		return CPU;
	}
	/**
	 * set stored parameter value for number of CPUs to use
	 *
	 * @param cPU the stored number of CPUs to use
	 */
	public void setCPU(int cPU) {
		CPU = cPU;
	}

	/**
	 * Returns the file name and path for an output file (without the extension or strand #)
	 * @param bed file path to the BED file (name included)
	 * @param bam file bath to the BAM file  (name included)
	 * @return The file name, with the path, of an output file (without the extension or strand #)
	 */
	public String generateFileBase(String bed, String bam) {
		String[] bedname = bed.split("\\.");
		String[] bamname = bam.split("\\.");

		String read = "5read1";
		if (getAspect() == PileupParameters.FIVE && getRead() == PileupParameters.READ2) {
			read = "5read2";
		} else if (getAspect() == PileupParameters.FIVE && getRead() == PileupParameters.ALLREADS) {
			read = "5readc";
		} else if (getAspect() == PileupParameters.THREE && getRead() == PileupParameters.READ1) {
			read = "3read1";
		} else if (getAspect() == PileupParameters.THREE && getRead() == PileupParameters.READ2) {
			read = "3read2";
		} else if (getAspect() == PileupParameters.THREE && getRead() == PileupParameters.ALLREADS) {
			read = "3readc";
		} else if (getAspect() == PileupParameters.MIDPOINT) {
			read = "midpoint";
		} else if (getAspect() == PileupParameters.FRAGMENT) {
			read = "fragment";
		}

		return (bedname[0] + "_" + bamname[0] + "_" + read);
	}

	/**
	 * Generates the fine name and path for an output
	 * @param bed file path to the BED file (name included)
	 * @param bam file bath to the BAM file  (name included)
	 * @param strandnum strand # to be used in the file name
	 * @return The file name, with the path, of an output file (including the file extension and strand #)
	 */
	public String generateFileName(String bed, String bam, int strandnum) {
		return (generateFileName(generateFileBase(bed, bam), strandnum));
	}

	/**
	 * Generates the file name and path for an output
	 * @param basename Base name of the file (created with {@link PileupParameters#generateFileBase(String, String)})
	 * @param strandnum strand # to be used in the file name
	 * @return The file name, with the path, of an output file (including the file extension and strand #)
	 */
	public String generateFileName(String basename, int strandnum) {
		String strand = "sense";
		if (strandnum == 1) {
			strand = "anti";
		} else if (strandnum == 2) {
			strand = "combined";
		}

		String filename = basename + "_" + strand;
		if (getOutputType() == 1) {
			filename += ".tab";
		} else {
			filename += ".cdt";
		}

		if (getOutputGZIP()) {
			filename += ".gz";
		}

		return filename;
	}

	/**
	 * Get the flag options for a cli execution with this object's parameter
	 * settings.
	 *
	 * @return the string of flags and options
	 * @throws OptionException when PileupParameters value is unrecognized
	 */
	public String getCLIOptions() throws OptionException {
		String cliCommand = "";

		// Add ASPECT
		switch (ASPECT) {
			case PileupParameters.FIVE:
				cliCommand += " -5";
				break;
			case PileupParameters.THREE:
				cliCommand += " -3";
				break;
			case PileupParameters.MIDPOINT:
				cliCommand += " -m";
				break;
			case PileupParameters.FRAGMENT:
				cliCommand += " --full-fragment";
				break;
			default:
				throw new OptionException("Unknown PileupParameters read aspect value: " + ASPECT);
		}

		// Add READ
		switch (READ) {
			case PileupParameters.READ1:
				cliCommand += " -1";
				break;
			case PileupParameters.READ2:
				cliCommand += " -2";
				break;
			case PileupParameters.ALLREADS:
				cliCommand += " -a";
				break;
			default:
				throw new OptionException("Unknown PileupParameters read output value: " + READ);
		}

		// Add STRAND
		switch (STRAND) {
			case PileupParameters.SEPARATE:
				cliCommand += " --separate";
				break;
			case PileupParameters.COMBINED:
				cliCommand += " --combined";
				break;
			default:
				throw new OptionException("Unknown PileupParameters strand value: " + STRAND);
		}

		// Add TRANS
		if(TRANS==0){
			cliCommand += " --no-smooth";
		}else if(TRANS==1){
			if(SMOOTH==3){ cliCommand += " -w"; }
			else{ cliCommand += " -W " + SMOOTH; }
		}else if(TRANS==2){
			if(STDSIZE==5 && STDNUM==3){ cliCommand += " -g"; }
			else{ cliCommand += " -G " + STDSIZE + " " + STDNUM; }
		}else{ System.err.println("This should not print."); }

		// Add SHIFT
		if (SHIFT != 0) { cliCommand += " -s " + SHIFT; }
		// Add BIN
		if (BIN != 1) { cliCommand += " -b " + BIN; }
		// Add TagExtend
		if (TAGEXTEND != 0) { cliCommand += " -e " + TAGEXTEND; }

		//Add STANDARD
		if(STANDARD==true){ cliCommand += " -t"; }
		//Add BLACKLIST
		if(BLACKLIST!=null){ cliCommand += " -f " + BLACKLIST.getAbsolutePath(); }

		//Add requirePE
		if(requirePE==true){ cliCommand += " -p"; }
		//Add MIN_INSERT
		if(MIN_INSERT!=-9999){ cliCommand += " -n " + MIN_INSERT; }
		//Add MAX_INSERT
		if(MAX_INSERT!=-9999){ cliCommand += " -x " + MAX_INSERT; }

		if (OUTTYPE == PileupParameters.TAB) { cliCommand += " --tab"; }
		//Add outputGZIP
		if(outputGZIP==true){ cliCommand += " -z"; }

		//Add CPU
		if(CPU!=1){ cliCommand += " --cpu " + CPU; }

		return(cliCommand);
	}
}
