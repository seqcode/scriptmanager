package scriptmanager.scripts.BAM_Statistics;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jfree.chart.ChartPanel;

import scriptmanager.charts.HeatMap;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.scripts.BAM_Statistics.CorrelationScripts.CorrelationCoord;
import scriptmanager.scripts.BAM_Statistics.CorrelationScripts.CorrelationExtract;

/**
 * Perform genome-genome correlations for replicate comparisons on multiple BAM
 * files
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.CorrelationScripts.CorrelationCoord
 * @see scriptmanager.scripts.BAM_Statistics.CorrelationScripts.CorrelationExtract
 * @see scriptmanager.cli.BAM_Statistics.BAMGenomeCorrelationCLI
 * @see scriptmanager.window_interface.BAM_Statistics.BAMGenomeCorrelationWindow
 * @see scriptmanager.window_interface.BAM_Statistics.BAMGenomeCorrelationOutput
 */
@SuppressWarnings("serial")
public class BAMGenomeCorrelation extends Component {
	
	private Vector<File> bamFiles = null;
	private String[] fileID = null;
	private double[][] MATRIX;
	private ChartPanel HEATMAP;
	private File OUT_BASENAME = null;
	private int SHIFT;
	private int BIN;
	private int CPU;
	private int READ;
	private short COLORSCALE;
	
	SamReader reader;
	final SamReaderFactory factory = SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS).validationStringency(ValidationStringency.SILENT);
	
	/**
	 * Creates a new BAMGenomeCorrelation object given parameters
	 * 
	 * @param input list of indexed BAM files to correlate
	 * @param o     Base name for output files
	 * @param s     The tag shift in #of base pairs
	 * @param b     The bin size in #of base pairs
	 * @param c     Number of CPU's to use
	 * @param r     Specifies which reads to correlate
	 * @param cs    Color scale to use when generating heatmap
	 */
	public BAMGenomeCorrelation(Vector<File> input, File o, int s, int b, int c, int r, short cs ){
		//Load in bamFiles
		bamFiles = input;
		fileID = new String[bamFiles.size()];
		//Initialize correlation matrix
		MATRIX = new double[bamFiles.size()][bamFiles.size()];
		//Store the rest of the variables
		OUT_BASENAME = o;
		SHIFT = s;
		BIN = b;
		CPU = c;
		READ = r;
		COLORSCALE = cs;
	}

	/**
	 * Creates correlation matrix 
	 * @param GUI Specifies if called by window interface or by cli
	 * @throws IOException Invalid file or parameters
	 */
		
	public void getBAMGenomeCorrelation(boolean GUI) throws IOException, OptionException {
		System.err.println(getTimeStamp());
		// Check BAMs first
		if(!validateBAM()) {
			return;
		}

		//Open Output File
		PrintStream OUT = null;
		File OUT_PNG = null;
		if(OUT_BASENAME!=null) {
			try {
				OUT = new PrintStream(new File( OUT_BASENAME + "_Correlation.out"));
				OUT_PNG = new File( OUT_BASENAME + "_Correlation.png" );
			} catch (FileNotFoundException e) { e.printStackTrace(); }
		}
	
		//Iterate through all BAM files in Vector
		int counter = 0;
		for (int x = 0; x < bamFiles.size(); x++) {
			for(int y = 0; y < bamFiles.size(); y++) {
				if(x != y && (x - y) >= 1) {
					MATRIX[x][y] = correlate(bamFiles.get(x), bamFiles.get(y));
					MATRIX[y][x] = MATRIX[x][y];
					firePropertyChange("progress", counter, counter + 1);
					counter++;
				} else if(x == y) { MATRIX[x][y] = 1; }
			}
		}
		
		//Output correlation matrix
		for(int x = 0; x < bamFiles.size(); x++) {
			fileID[x] = bamFiles.get(x).getName();
			printBoth( OUT, bamFiles.get(x).getName() + "\t" );
		}
		printBoth( OUT, "\n" );
		for(int x = 0; x < MATRIX.length; x++) {
			for(int y = 0; y < MATRIX.length; y++) {
				printBoth( OUT, MATRIX[x][y] + "\t");
			}
			printBoth( OUT, "\n" );
		}
		if(OUT != null) OUT.close();
		
		// call HeatMap script to create Heatmap with specific colorscale
		HEATMAP = HeatMap.createCorrelationHeatmap(fileID, MATRIX, OUT_PNG, HeatMap.getPresetPaintscale(COLORSCALE));
		
		System.err.println(getTimeStamp());
	}
	
	/**
	 * Performs reflexive pearson correlation calculations between two given genomes
	 * @param exp1 First genome
	 * @param exp2 Second genome
	 * @return correlation value
	 */
	public double correlate(File exp1, File exp2) {
		System.err.println("Comparing: " + exp1.getName() + "\t-\t" + exp2.getName());

		//Reflexive pearson correlation requiring only a single pass through each genome
		double Sx = 0;
		double Sxx = 0;
		double Sy = 0;
		double Syy = 0;
		double Sxy = 0;
		double count = 0;
		
		//Hyperthread extraction of genomic windows one chromosome at a time
		Vector<CorrelationCoord> ChromosomeWindows = null;
		//Open BAI index to go chromosome by chromosome
		reader = factory.open(exp1);
		AbstractBAMFileIndex bai = (AbstractBAMFileIndex) reader.indexing().getIndex();
		for(int numchrom = 0; numchrom < bai.getNumberOfReferences(); numchrom++) {
			//Object to keep track of the chromosomal data
			ChromosomeWindows = new Vector<CorrelationCoord>();
			SAMSequenceRecord seq = reader.getFileHeader().getSequence(numchrom);
			//System.err.println("Analyzing: " + seq.getSequenceName());
			
			//Break chromosome into 100kb chunks and assign to independent nodes
			int numwindows = (int) (seq.getSequenceLength() / 100_000);
			for(int x = 0; x < numwindows; x++) {
				int start = x * 100_000;
				int stop = start + 100_000;
				ChromosomeWindows.add(new CorrelationCoord(seq.getSequenceName(), start, stop));
			}
			int finalstart = numwindows * 100_000;
			int finalstop = (seq.getSequenceLength() / BIN) * BIN;
			ChromosomeWindows.add(new CorrelationCoord(seq.getSequenceName(), finalstart, finalstop));
			
			//Reduce CPU requirement is less windows
			int windowSize = ChromosomeWindows.size();
			if(windowSize < CPU) {
				CPU = ChromosomeWindows.size();;
			}
			ExecutorService parseMaster = Executors.newFixedThreadPool(CPU);
			int subset = 0;
			int currentindex = 0;
			for(int x = 0; x < CPU; x++) {
				currentindex += subset;
				if(CPU == 1) subset = windowSize;
				else if(windowSize % CPU == 0) subset = windowSize / CPU;
				else {
					int remainder = windowSize % CPU;
					if(x < remainder ) subset = (int)(((double)windowSize / (double)CPU) + 1);
					else subset = (int)(((double)windowSize / (double)CPU));
				}
				CorrelationExtract nodeextract = new CorrelationExtract(ChromosomeWindows, READ, SHIFT, BIN, currentindex, subset, exp1, exp2);
				parseMaster.execute(nodeextract);
			}
			parseMaster.shutdown();
			while (!parseMaster.isTerminated()) {
			}
						
			for(int x = 0; x < ChromosomeWindows.size(); x++) {
				Sx += ChromosomeWindows.get(x).getSx();
				Sxx += ChromosomeWindows.get(x).getSxx();
				Sy += ChromosomeWindows.get(x).getSy();
				Syy += ChromosomeWindows.get(x).getSyy();
				Sxy += ChromosomeWindows.get(x).getSxy();
				count += ChromosomeWindows.get(x).getCount();
			}
		}
				
		double numerator = 0;
		double denominator = 0;
				
		numerator = Sxy - ((Sx * Sy) / count);
		denominator = Math.sqrt((Sxx - ((Sx * Sx) / count)) * (Syy - ((Sy * Sy / count))));
		
		double correlation = numerator / denominator;
		System.err.println("Correlation: " + correlation);
		return correlation;
	}
	
	/**
	 * Checks if BAI files (index files for BAM files) exist for all BAM files
	 * @return if all the input BAM files have corresponding BAI files
	 * @throws IOException Invalid file or parameters 
	 */
	private boolean validateBAM() throws IOException, OptionException {
		//Check if BAI index file exists for all BAM files before we process any of them
		ArrayList<String> chrName = new ArrayList<String>();
		ArrayList<Integer> chrSize = new ArrayList<Integer>();
		for(int x = 0; x < bamFiles.size(); x++) {	
			File XBAM = bamFiles.get(x);			
			File f = new File(XBAM + ".bai");
			if(!f.exists() || f.isDirectory()) {
				throw new OptionException("BAI Index File does not exist for: " + XBAM.getName());
			} else {
				reader = factory.open(XBAM);
				AbstractBAMFileIndex bai = (AbstractBAMFileIndex) reader.indexing().getIndex();
				if(x == 0) {
					for (int z = 0; z < bai.getNumberOfReferences(); z++) {
						chrName.add(reader.getFileHeader().getSequence(z).getSequenceName());
						chrSize.add(reader.getFileHeader().getSequence(z).getSequenceLength());
					}
				} else if(bai.getNumberOfReferences() != chrName.size()) {
					reader.close();
					bai.close();
					throw new OptionException("Unequal number of chromosomes from previous: " + XBAM.getName());
				} else {
					boolean MATCH = true;
					for (int z = 0; z < bai.getNumberOfReferences(); z++) {
						if(!chrName.get(z).equals(reader.getFileHeader().getSequence(z).getSequenceName())) { MATCH = false; }
						if(!chrSize.get(z).equals(reader.getFileHeader().getSequence(z).getSequenceLength())) { MATCH = false; }
					}
					if(!MATCH) {
						reader.close();
						bai.close();
						throw new OptionException("File contains chromosome size/name which does not match previous: " + XBAM.getName());
					}
				}
				bai.close();
			}
		}
		reader.close();
		return true;
	}
	
	private static void printBoth( PrintStream p, String line ){
		if(p!=null) p.print(line);
		System.err.print(line);
	}
	
	private static String getTimeStamp() {
		Date date= new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
	
	/**
	 * Returns the correlation matrix
	 * @return The correlation matrix
	 */
	public double[][] getMatrix(){ return(MATRIX); }
	
	/**
	 * Returns the generated correlation heatmap
	 * @return The generated correlation heatmap
	 */
	public ChartPanel getHeatMap(){ return(HEATMAP); }
	
}
