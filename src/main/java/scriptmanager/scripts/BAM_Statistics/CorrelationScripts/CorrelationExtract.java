package scriptmanager.scripts.BAM_Statistics.CorrelationScripts;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.CloseableIterator;

/**
 * Calculate correlation between two CorrelationCoords
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.BAMGenomeCorrelation
 * @see scriptmanager.scripts.BAM_Statistics.CorrelationScripts.CorrelationCoord
 */
public class CorrelationExtract implements Runnable {
	Vector<CorrelationCoord> ALLNodes;	
	
	private File EXP1 = null;
	private File EXP2 = null;
	
	private int READ = 0;
	private int SHIFT = 0;
	private int BIN = 10;
	
	private int INDEX;
	private int SUBSETSIZE;

	SamReader inputSam;

	/**
	 * Creates a new CorrelationExtract object given parameters
	 * 
	 * @param win     Set of CorrelationCoords to compare
	 * @param r       Which reads to correlate
	 * @param s       The tag shift in #of base pairs
	 * @param b       The bin size in #of base pairs
	 * @param current Specifies the starting index
	 * @param sub     ️The subset size
	 * @param e1      First BAM file
	 * @param e2      Second BAM file
	 */
	public CorrelationExtract(Vector<CorrelationCoord> win, int r, int s, int b, int current, int sub, File e1, File e2) {
		ALLNodes = win;
		READ = r;
		SHIFT = s;
		BIN = b;
		INDEX = current;
		SUBSETSIZE = sub;
		EXP1 = e1;
		EXP2 = e2;
	}
	
	/**
	 * Runs the correlation analysis between the two BAM files
	 */
	public void run() {
		for(int x = INDEX; x < INDEX + SUBSETSIZE; x++) {
			inputSam = SamReaderFactory.makeDefault().open(EXP1);
			extractRegion(ALLNodes.get(x));
			try { inputSam.close();	}
			catch (IOException e) { e.printStackTrace(); }

			inputSam = SamReaderFactory.makeDefault().open(EXP2);
			extractRegion(ALLNodes.get(x));
			try { inputSam.close(); }
			catch (IOException e) { e.printStackTrace(); }
			
			//After tag data is added, calculate out statistics for the forward and reverse tags in the node
			double[] Ftag = ALLNodes.get(x).getData().get(0);
			double[] Rtag = ALLNodes.get(x).getData().get(1);
			
			double count = 0, sx = 0, sxx = 0, sy = 0, syy = 0, sxy = 0;
			for(int winIndex = 0; winIndex < Ftag.length; winIndex++) {
				if(!Double.isNaN(Ftag[winIndex]) && !Double.isInfinite(Ftag[winIndex]) && !Double.isNaN(Rtag[winIndex]) && !Double.isInfinite(Rtag[winIndex])) {
					count++;
					sx += Ftag[winIndex];
					sxx += (Ftag[winIndex] * Ftag[winIndex]);
					sy += Rtag[winIndex];
					syy += (Rtag[winIndex] * Rtag[winIndex]);
					sxy += (Ftag[winIndex] * Rtag[winIndex]);
				}
			}
			
			ALLNodes.get(x).setCount(count);
			ALLNodes.get(x).setSx(sx);
			ALLNodes.get(x).setSxx(sxx);
			ALLNodes.get(x).setSy(sy);
			ALLNodes.get(x).setSyy(syy);
			ALLNodes.get(x).setSxy(sxy);
			
			//Remove unneeded tag data after analysis to conserve memory
			ALLNodes.get(x).setData(null);
		}
	}	
	
	/**
	 * Extracts region for correlation given a CorrelationCoord
	 * @param current CorrelationCoord to analyze
	 */
	public void extractRegion(CorrelationCoord current) {
		int WINDOW = current.getStop() - current.getStart();
		if(WINDOW < 1) {
			System.err.println(WINDOW + "\t" + current.getStop() + "\t" + current.getStart() + "\t" + BIN);
		}
		double[] retVal = new double[WINDOW + 1];
		int START = current.getStart();
		int STOP = current.getStop();
		
		try {
			CloseableIterator<SAMRecord> iter = inputSam.query(current.getChrom(), START - SHIFT - 1, STOP + SHIFT + 1, false);
			while (iter.hasNext()) {
				//Create the record object 
			    //SAMRecord is 1-based
				SAMRecord sr = iter.next();
	
				if(sr.getReadPairedFlag()) { //Must be PAIRED-END mapped
					//Read 1 and want Read 1, Read 2 and want Read 2, want any read, Read 1 and want midpoint
					if((sr.getFirstOfPairFlag() && READ == 0) || (!sr.getFirstOfPairFlag() && READ == 1) || READ == 2 || (sr.getFirstOfPairFlag() && READ == 3)) {
						int FivePrime = sr.getUnclippedStart() - 1;
						if(sr.getReadNegativeStrandFlag()) { 
							FivePrime = sr.getUnclippedEnd();
							FivePrime -= SHIFT; //SHIFT DATA HERE IF NECCESSARY
						} else { FivePrime += SHIFT; }
							
						if(sr.getProperPairFlag()) { //prevent cases where non-properly paired Read1 gets to this point
							int recordStart = sr.getUnclippedStart() - 1;
							int recordStop = sr.getMateAlignmentStart() + sr.getReadLength() - 1;
							if(sr.getMateAlignmentStart() - 1 < recordStart) {
								recordStart = sr.getMateAlignmentStart() - 1;
								recordStop = sr.getUnclippedEnd();
							}
							//Find midpoint is read flag == 3
							if(READ == 3) { FivePrime = (recordStart + recordStop) / 2; }
						} else if(READ == 3) { FivePrime = -999; } // If not properly paired, do NOT allow read to be counted
		
						//Adjust tag start to be within array reference
						FivePrime -= START;
		                //Increment Final Array keeping track of pileup
						if(FivePrime >= 0 && FivePrime < retVal.length) { retVal[FivePrime] += 1; }
					}
				} else if(READ == 0 || READ == 2) { //Also outputs if not paired-end since by default it is read-1
					int FivePrime = sr.getUnclippedStart() - 1;
					if(sr.getReadNegativeStrandFlag()) { 
						FivePrime = sr.getUnclippedEnd();
						FivePrime -= SHIFT; //SHIFT DATA HERE IF NECCESSARY
					} else { FivePrime += SHIFT; }
					FivePrime -= START;
					//Increment Final Array keeping track of pileup
					if(FivePrime >= 0 && FivePrime < retVal.length) { retVal[FivePrime] += 1;	}
				}
			}
			iter.close();
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.out.println("Exception caught");
		}
		
		//Perform Binning here
		double[] binVal = new double[retVal.length];
		for(int j = 0; j < binVal.length; j++) {
			for(int k = j - (BIN / 2); k <= j + (BIN / 2); k++) {
				if(k < 0) { k = 0; }
				if(k >= binVal.length) { k = j + (BIN / 2) + 1; }
				else { binVal[k] += retVal[j]; }
			}
		}
		double[] finalVal = new double[retVal.length / BIN];
		for(int x = (BIN / 2); x < retVal.length - (BIN / 2); x += BIN) {
			finalVal[(x - (BIN / 2)) /BIN] = binVal[x];
		}
		current.addData(finalVal);
	}
}
