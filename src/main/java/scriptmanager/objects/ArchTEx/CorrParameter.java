package scriptmanager.objects.ArchTEx;

//import java.io.File;
//import java.util.Vector;

/**
 * Object to help store ArchTEx Cross-Correlation parameter values.
 * <br>
 * Code largely sourced from ArchTEx.analysis.corr.CorrParameter in <a href=
 * "https://github.com/WilliamKMLai/ArchTEx">https://github.com/WilliamKMLai/ArchTEx</a>
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.CrossCorrelation
 * @see scriptmanager.window_interface.BAM_Statistics.CrossCorrelationOutput
 * @see scriptmanager.window_interface.BAM_Statistics.CrossCorrelationWindow
 */
public class CorrParameter {
//	//BAM Files and Accompanying indexes
//	private Vector<File> input;	// Stores the names of input files
//	private Vector<File> index;		// Stores the name of the index file

	//Parameter to determine between random sampling and complete sampling
	private boolean corrType = false; //Default randomly sample genome

	//Random Sampling Parameters
	private int windowSize = 50_000;	//Default set Window frame for each extraction to 50kb
	private int iterations = 10;	//Default set number of random iterations per chromosome

	//TODO if time add resolution and window frame variables
	private int resolution = 1;	//Default set correlation resolution
	private int corrWindow = 1001; //Default set window size of correlation

	//Misc Parameters
	private int threads = 1;	//Default set number of cpu's to use to 1
//	private int currentExp = 0;	//Default current experiment under analysis
//
//	public CorrParameter() {
//		input = new Vector<File>();
//		index = new Vector<File>();
//	}
//
//	public void setExp(int newexp) {
//		currentExp = newexp;
//	}
//	
//	public int getExp() {
//		return currentExp;
//	}

	/**
	 * Creates a new CorrParameter object
	 */
	public CorrParameter(){}
	
	/**
	 * Returns whether coordinates are randomly or completely sampled
	 * @return False = randomly sampled, True = completely sampled
	 */
	public boolean getCorrType() {
		return corrType;
	}
	
	/**
	 * Sets whether coordinates are randomly or completely sampled
	 * @param newtype False = randomly sampled, True = completely sampled
	 */
	public void setCorrType(boolean newtype) {
		corrType = newtype;
	}
	
//	public Vector<File> getInput() {
//		return input;
//	}
//
//	public void setInput(Vector<File> newinput) {
//		input = newinput;
//	}
//
//	public File getInputIndex(int index) {
//		return input.get(index);
//	}
//
//	public void addInput(File newinput) {
//		input.add(newinput);
//	}
//
//	public Vector<File> getIndex() {
//		return index;
//	}
//
//	public void setIndex(Vector<File> newindex) {
//		index = newindex;		
//	}
//
//	public File getIndexIndex(int in) {
//		return index.get(in);		
//	}
//
//	public void addIndex(File newinput) {
//		index.add(newinput);		
//	}

	/**
	 * Returns the size of the window frame
	 * @return The size of the window frame in # of bp
	 */
	public int getWindow() {
		return windowSize;		
	}

	/**
	 * Sets the size of the window frame
	 * @param newwindow The new size of the window frame
	 */
	public void setWindow(int newwindow) {
		windowSize = newwindow;		
	}

	/**
	 * Returns the number of random iterations per chromosome
	 * @return The number of random iterations per chromosome 
	 */
	public int getIterations() {
		return iterations;		
	}

	/**
	 * Sets the number of random iterations per chromosome
	 * @param newiter The new number of random iterations per chrosmosome
	 */
	public void setIterations(int newiter) {
		iterations = newiter;		
	}

	/**
	 * Returns the correlation resolution
	 * @return The correlation resolution
	 */
	public int getResolution() {
		return resolution;		
	}

	/**
	 * Sets the correlation resolution
	 * @param newresolution The new correlation resolution
	 */
	public void setResolution(int newresolution) {
		resolution = newresolution;		
	}

	/**
	 * Returns the size of the theoretical sliding window
	 * @return The size of the theoretical sliding window
	 */
	public int getCorrWindow() {
		return corrWindow;		
	}

	/**
	 * Sets the size of the theoretical sliding window
	 * @param newcorrWindow The new size of the theroretical sliding window
	 */
	public void setCorrWindow(int newcorrWindow) {
		corrWindow = newcorrWindow;		
	}

	/**
	 * Returns the number of CPU's to use
	 * @return The number of CPU's to use
	 */
	public int getThreads() {
		return threads;		
	}

	/**
	 * Sets the number of CPU's to use
	 * @param newthreads The new number of CPU's to use
	 */
	public void setThreads(int newthreads) {
		threads = newthreads;		
	}

	/**
	 * Returns the different variables of a CorrParameter represented by a string
	 * @return The different variables of a CorrParameter represented by a string
	 */
	public String printStats() {
		String OUTPUT = "";
//		if(input == null)	OUTPUT += "Input file: NONE\n";
//		else {
//			for(int x = 0; x < input.size(); x++) {
//				OUTPUT += "Input file: " + input.get(x) + "\n";
//			}
//		}
//		if(index == null)	OUTPUT += "Index file: NONE\n";
//		else {
//			for(int x = 0; x < index.size(); x++) {
//				OUTPUT += "Index file: " + input.get(x) + "\n";
//			}
//		}
		OUTPUT += "Window Size: " + windowSize + "bp" + "\n";
		OUTPUT += "Iterations: " + iterations + "\n";
		OUTPUT += "Resolution: " + resolution + "bp" + "\n";
		OUTPUT += "CPU Cores: " + threads;
		return OUTPUT;
	}

//	public String[] getInputNames() {
//		String[] names = new String[input.size()];
//		for (int x = 0; x < names.length; x++) {
//			names[x] = input.get(x).getName();
//		}
//		return names;
//	}
//
//	public String[] getIndexNames() {
//		String[] names = new String[input.size()];
//		for (int x = 0; x < names.length; x++) {
//			names[x] = input.get(x).getName();
//		}
//		return names;
//	}
}