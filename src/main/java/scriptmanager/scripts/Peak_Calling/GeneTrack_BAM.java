package scriptmanager.scripts.Peak_Calling;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.CloseableIterator;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import scriptmanager.objects.GenetrackParameters;
import scriptmanager.objects.Peak;

/**
 * (Dev) GUI for running a gene track window based on a (BAM input).
 * 
 * @author William KM Lai
 */
@SuppressWarnings("serial")
public class GeneTrack_BAM extends JFrame {
	private JTextArea textArea;
	
	private File INPUT = null;
	private String OUTPUTPATH = null;
	private PrintStream OUT = null;
	private SamReader inputSam = null;
	
	private int READ = 0;
	
	private int SIGMA = 5;
	private int EXCLUSION = 20;
	private int UP_WIDTH = 10;
	private int DOWN_WIDTH = 10;
	private int FILTER = 1;
	
	//Arbitrarily set to 5 std deviations up and down for gaussian kernel smoothing
	private int NUM_STD = 5;
	private double[] gaussWeight;
	private int WIDTH;
	
	//Arbitrarily set windowSize to 10,000bp in order to chop up the genome efficiently
	private int windowSize = 200000;
	
	//Array to contain genetrack peaks separated by strand
	private ArrayList<Peak> FPEAKS = null;
	private ArrayList<Peak> RPEAKS = null;
	
	private double[] F_GOCC;
	private double[] R_GOCC;
	private double[] F_TOCC;
	private double[] R_TOCC;
	private double[] F_STD;
	private double[] R_STD;
	
	/**
	 * Creates a new instance of a GeneTrack_BAM script
	 * @param in BAM file to run Genetrack Script on
	 * @param PARAM Object containing user-specified parameters
	 */
	public GeneTrack_BAM(File in, GenetrackParameters PARAM) {
		setTitle("BAM to Genetrack Progress");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		INPUT = in;
		OUTPUTPATH = PARAM.getName();
		
		READ = PARAM.getRead();
		SIGMA = PARAM.getSigma();
		EXCLUSION = PARAM.getExclusion();
		if(PARAM.getUp() == -999) UP_WIDTH = EXCLUSION / 2;
		else UP_WIDTH = PARAM.getUp();
		if(PARAM.getDown() == -999) DOWN_WIDTH = EXCLUSION / 2;
		else DOWN_WIDTH = PARAM.getDown();
		FILTER = PARAM.getFilter();
	}
	
	/**
	 * Runs the Gene Track algorithm with file and parameters passed through constructor, outputting progress to window
	 */
	public void run() {
		String TIME = getTimeStamp();
		
		System.out.println(getTimeStamp());
		
		String PARAM = "s" + SIGMA + "e" + EXCLUSION;
		String READNAME = "READ1";
		if(READ == 1) READNAME = "READ2";
		else if(READ == 2) READNAME = "COMBINED";
		String NAME = INPUT.getName().split("\\.")[0] + "_" + READNAME + "_" + PARAM + ".gff";
		textArea.append(TIME + "\n" + NAME + "\n");
		textArea.append("Sigma: " + SIGMA + "\nExclusion: " + EXCLUSION + "\nFilter: " + FILTER + "\nUpstream width of called Peaks: " + UP_WIDTH + "\nDownstream width of called Peaks: " + DOWN_WIDTH + "\n");
		
		try { OUT = new PrintStream(new File(OUTPUTPATH + File.separator + NAME)); }
		catch (FileNotFoundException e) { e.printStackTrace(); }

		//Set genetrack parameters
		gaussWeight = gaussKernel();
		WIDTH = SIGMA * NUM_STD;
				
		File f = new File(INPUT.getAbsolutePath() + ".bai");
		if(f.exists() && !f.isDirectory()) {	
			inputSam = SamReaderFactory.makeDefault().open(INPUT);
			AbstractBAMFileIndex bai = (AbstractBAMFileIndex) inputSam.indexing().getIndex();
			
			for(int numchrom = 0; numchrom < bai.getNumberOfReferences(); numchrom++) {
				SAMSequenceRecord seq = inputSam.getFileHeader().getSequence(numchrom);
				System.out.println("Processing: " + seq.getSequenceName());
				textArea.append("Processing: " + seq.getSequenceName() + "\n");
				
				FPEAKS = new ArrayList<Peak>();
				RPEAKS = new ArrayList<Peak>();
				
				int numwindows = (int) (seq.getSequenceLength() / windowSize);
				for(int x = 0; x < numwindows; x++) {
					int start = x * windowSize;
					int stop = start + windowSize + WIDTH;
					
					F_GOCC = new double[windowSize];
					R_GOCC = new double[windowSize];
					F_TOCC = new double[windowSize];
					R_TOCC = new double[windowSize];
					F_STD = new double[windowSize];
					R_STD = new double[windowSize];
					
					CloseableIterator<SAMRecord> iter = inputSam.query(seq.getSequenceName(), start, stop, false);
					loadGenomeFragment(iter, start, stop);
					iter.close();
					//call peaks by local maxima
					filterbyLocalMaxima(seq.getSequenceName(), start);
				}
								
				int finalstart = numwindows * windowSize;
				int finalstop = seq.getSequenceLength();
				F_GOCC = new double[finalstop - finalstart];
				R_GOCC = new double[finalstop - finalstart];
				F_TOCC = new double[finalstop - finalstart];
				R_TOCC = new double[finalstop - finalstart];
				F_STD = new double[finalstop - finalstart];
				R_STD = new double[finalstop - finalstart];
				
				CloseableIterator<SAMRecord> iter = inputSam.query(seq.getSequenceName(), finalstart, finalstop, false);
				loadGenomeFragment(iter, finalstart, finalstop);
				iter.close();
				//call peaks by local maxima
				filterbyLocalMaxima(seq.getSequenceName(), finalstart);
				
				//parse peaks by exclusion zone
				parsePeaksbyExclusion(FPEAKS);
				parsePeaksbyExclusion(RPEAKS);
				
				for(int z = 0; z < FPEAKS.size(); z++) {
					OUT.println(FPEAKS.get(z).toString());		
				}
				for(int z = 0; z < RPEAKS.size(); z++) {
					OUT.println(RPEAKS.get(z).toString());		
				}
				
			}
			bai.close();
			OUT.close();
		} else {
			textArea.append("BAI Index File does not exist for: " + INPUT.getName() + "\n");
			OUT.println("BAI Index File does not exist for: " + INPUT.getName() + "\n");
		}
		
		System.out.println(getTimeStamp());
		
		dispose();
	}
	
	/**
	 * Filters given chromosome for peaks using options from PARAM
	 * @param chrom Chromosome to find peaks in
	 * @param start Last position to be 
	 */
	public void filterbyLocalMaxima(String chrom, int start) {
		for(int z = 0; z < F_GOCC.length; z++) {	
			int fiveprime = z + start - UP_WIDTH;
			int threeprime = z + start + DOWN_WIDTH;
			if(fiveprime < 1) { fiveprime = 1; }
			if(threeprime < 1) { threeprime = 1; }
			
			if(z == 0) {
				if(F_GOCC[z] >= F_GOCC[z + 1] && F_TOCC[z] > FILTER) { 
					FPEAKS.add(new Peak(chrom, fiveprime, threeprime, "+", (int)F_TOCC[z], F_STD[z]));
				}
				if(R_GOCC[z] >= R_GOCC[z + 1] && R_TOCC[z] > FILTER) {
					RPEAKS.add(new Peak(chrom, fiveprime, threeprime, "-", (int)R_TOCC[z], R_STD[z]));
				}
			} else if(z + 1 == F_GOCC.length) {
				if(F_GOCC[z] >= F_GOCC[z - 1] && F_TOCC[z] > FILTER) {
					FPEAKS.add(new Peak(chrom, fiveprime, threeprime, "+", (int)F_TOCC[z], F_STD[z]));
				}
				if(R_GOCC[z] >= R_GOCC[z - 1] && R_TOCC[z] > FILTER) {
					RPEAKS.add(new Peak(chrom, fiveprime, threeprime, "-", (int)R_TOCC[z], R_STD[z]));
				}
			} else {
				if(F_GOCC[z] >= F_GOCC[z + 1] && F_GOCC[z] > F_GOCC[z - 1] && F_TOCC[z] > FILTER) {
					FPEAKS.add(new Peak(chrom, fiveprime, threeprime, "+", (int)F_TOCC[z], F_STD[z]));
				}
				if(R_GOCC[z] >= R_GOCC[z + 1] && R_GOCC[z] > R_GOCC[z - 1] && R_TOCC[z] > FILTER) {
					RPEAKS.add(new Peak(chrom, fiveprime, threeprime, "-", (int)R_TOCC[z], R_STD[z]));
				}
			}
			//int bp = z + start + 1;
			//OUT.println(seq.getSequenceName() + "\t" + bp + "\t" + F_GOCC[z] + "\t" + R_GOCC[z] + "\t" + F_TOCC[z] + "\t" + R_TOCC[z]);		
		}
	}
	
	/**
	 * 
	 * @param iter
	 * @param start
	 * @param stop
	 */
	public void loadGenomeFragment(CloseableIterator<SAMRecord> iter, int start, int stop) {
		double[] tempF = new double[F_TOCC.length];
		double[] tempR = new double[R_TOCC.length];
		
		while (iter.hasNext()) {
			//Create the record object 
			SAMRecord sr = iter.next();
			
			int recordStart = -999;
			
			//Check for paired-end
			if(sr.getReadPairedFlag()) {
				//Must be PAIRED-END mapped, mate must be mapped, must be read 1
				if(sr.getProperPairFlag() && sr.getFirstOfPairFlag() && (READ == 0 || READ == 2)) {
					//Get the start of the record 
					recordStart = sr.getUnclippedStart();
					//Accounts for reverse tag reporting 3' end of tag and converting BAM to IDX/GFF format
					if(sr.getReadNegativeStrandFlag()) { recordStart = sr.getUnclippedEnd(); }
				} else if(sr.getProperPairFlag() && !sr.getFirstOfPairFlag() && (READ == 1 || READ == 2)) {
					//Get the start of the record 
					recordStart = sr.getUnclippedStart();
					//Accounts for reverse tag reporting 3' end of tag and converting BAM to IDX/GFF format
					if(sr.getReadNegativeStrandFlag()) { recordStart = sr.getUnclippedEnd(); }
				} 
			} else if(READ == 0 || READ == 2) {
				//Get the start of the record 
				recordStart = sr.getUnclippedStart();
				//Accounts for reverse tag reporting 3' end of tag and converting BAM to IDX/GFF format
				if(sr.getReadNegativeStrandFlag()) { recordStart = sr.getUnclippedEnd(); }				
			}
	
			if(recordStart > 0) {
				for(int POS = recordStart - WIDTH; POS <= recordStart + WIDTH; POS++) {
					if(POS - start >= 0 && POS - start < F_GOCC.length) {
						if(sr.getReadNegativeStrandFlag()) {
							R_GOCC[POS - start] += gaussWeight[POS - (recordStart - WIDTH)];
							if(POS == recordStart) tempR[POS - start]++;
						} else {
							F_GOCC[POS - start] += gaussWeight[POS - (recordStart - WIDTH)];
							if(POS == recordStart) tempF[POS - start]++;
						}
					}
				}
			}
		}
		
		//Variance code adapted from Donald Knuth's implementation of Welford method
		for(int x = 0; x < tempF.length; x++) {
			double meanF = 0, meanR = 0, varF = 0, varR = 0, Fcount = 0, Rcount = 0;
			for(int y = x - UP_WIDTH; y <= x + DOWN_WIDTH; y++) {
				if(y < 0) y = 0;
				if(y < tempF.length) {
					if(tempF[y] != 0) {
						F_TOCC[x] += tempF[y];
						if(Fcount == 0) { meanF = y; }
						else {
							 double tempMean = ((meanF * Fcount) + (tempF[y] * y)) / (Fcount + tempF[y]);
							 double tempVar = varF + ((y - meanF) * (y - tempMean) * tempF[y]);
							 meanF = tempMean;
							 varF = tempVar;
						}
						Fcount += tempF[y];
					}
					if(tempR[y] != 0) {
						R_TOCC[x] += tempR[y];
						if(Rcount == 0) { meanR = y; }
						else {
							double tempMean = ((meanR * Rcount) + (tempR[y] * y)) / (Rcount + tempR[y]);
							double tempVar = varR + ((y - meanR) * (y - tempMean) * tempR[y]);
							meanR = tempMean;
							varR = tempVar;
						}
						Rcount += tempR[y];
					}
				}
			}
			if(Fcount != 0) varF /= Fcount;
			if(Rcount != 0) varR /= Rcount;
			F_STD[x] = Math.sqrt(varF);
			R_STD[x] = Math.sqrt(varR);
		}
		
	}
	
	public void parsePeaksbyExclusion(ArrayList<Peak> peaks) {
		//Sort by Peak Score
		Collections.sort(peaks, Peak.PeakTagComparator);
		
		for(int x = 0; x < peaks.size(); x++) {
			for(int y = 0; y < peaks.size(); y++) {
				if(x != y) {
					if(peaks.get(x).getStop() > peaks.get(y).getStart() && peaks.get(x).getStart() < peaks.get(y).getStop()) {
						peaks.remove(y);
						if(x > y) x--;
						y--;
					} else if(peaks.get(x).getStart() < peaks.get(y).getStop() && peaks.get(x).getStop() > peaks.get(y).getStart()) {
						peaks.remove(y);
						if(x > y) x--;
						y--;
					}
				}
			}
		}
		//Sort by position
		Collections.sort(peaks, Peak.PeakPositionComparator);
	}
	
	/**
	 * Creates an 
	 * @return
	 */
	private double[] gaussKernel() {
		double[] Garray = new double[(int) (SIGMA * NUM_STD * 2) + 1];
		for(int x = 0; x < Garray.length; x++) {
             double HEIGHT = Math.exp(-1 * Math.pow((x - (Garray.length / 2)), 2) / (2 * Math.pow(SIGMA, 2)));
             HEIGHT /= (SIGMA * Math.sqrt(2 * Math.PI));
             //Garray[x] = Double.parseDouble(String.format("%.6g%n", HEIGHT));
             Garray[x] = HEIGHT;
		}
		return Garray;
     }
	
	private static String getTimeStamp() {
		Date date= new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
}