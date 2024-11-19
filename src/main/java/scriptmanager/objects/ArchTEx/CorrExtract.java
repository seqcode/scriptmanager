package scriptmanager.objects.ArchTEx;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloseableIterator;

//import scriptmanager.objects.ArchTEx.CorrParameter;
//import net.sf.samtools.SAMFileReader;
//import net.sf.samtools.SAMRecord;
//import net.sf.samtools.util.CloseableIterator;

/**
 * Helper object for ArchTEx Cross-Correlation analysis to parse BAM files and
 * extract tag counts to correlate into a CorrNode object. This is <br>
 * Code largely sourced from ArchTEx.analysis.corr.CorrExtract in <a href=
 * "https://github.com/WilliamKMLai/ArchTEx">https://github.com/WilliamKMLai/ArchTEx</a>
 * Primarily modified to use ScriptManager's version of htsjdk.
 * 
 * @author William KM Lai
 * @see scriptmanager.objects.ArchTEx.CorrParameter
 * @see scriptmanager.scripts.BAM_Statistics.CrossCorrelation
 */
public class CorrExtract implements Runnable {
	final Lock lock = new ReentrantLock();
	final Condition inUse  = lock.newCondition(); 

	File input;
	CorrParameter Parameters;
	Vector<CorrNode> ALLNodes;	

	int INDEX;
	int SUBSETSIZE;

	static int progressCounter = 0;
	static int currentProgress = 0;

	final SamReaderFactory factory = SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS).validationStringency(ValidationStringency.SILENT);
	SamReader reader;
//	SAMFileReader inputSam;

	/**
	 * Pass necessary parameters to perform extraction (multi-threaded)
	 * 
	 * @param i       the BAM file to extract pileups from
	 * @param param   the stored Cross-Correlation parameters for how to perform
	 *                extraction
	 * @param nodes   the set of CorrNodes storing the extracted values
	 * @param current the start of the subset nodes in the list to extract for
	 * @param size    the length or how many nodes from "current" in the node list to extract for
	 */
	public CorrExtract(File i, CorrParameter param, Vector<CorrNode> nodes, int current, int size) {
		input = i;
		Parameters = param;
		ALLNodes = nodes;
		INDEX = current;
		SUBSETSIZE = size;
	}

	@Override
	/**
	 * Checks for valid input and runs analysis on CorrNodes
	 */
	public void run() {
		for(int x = INDEX; x < INDEX + SUBSETSIZE; x++) {
			try {
				reader = factory.open(input);
//				AbstractBAMFileIndex bai = (AbstractBAMFileIndex) reader.indexing().getIndex();
//				inputSam = new SAMFileReader(input, new File(input.getAbsoluteFile() + ".bai"));
				ALLNodes.get(x).setData(extractRegion(ALLNodes.get(x), Parameters));
//				inputSam.close();
				reader.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			//After tag data is added, calculate out statistics for the forward and reverse tags in the node
			double[] Ftag = ALLNodes.get(x).getData().get(0);
			double[] Rtag = ALLNodes.get(x).getData().get(1);
			
			//System.out.println(Ftag.length + "\t" + Rtag.length);
			double count = 0, sx = 0, sxx = 0, sy = 0, syy = 0;
			double[] sxy = new double[Parameters.getCorrWindow()];
			for(int winIndex = 0; winIndex < Ftag.length - Parameters.getCorrWindow(); winIndex++) {
				count++;
				sx += Ftag[winIndex];
				sxx += (Ftag[winIndex] * Ftag[winIndex]);
				sy += Rtag[winIndex];
				syy += (Rtag[winIndex] * Rtag[winIndex]);
				for(int shift = 0; shift < Parameters.getCorrWindow(); shift++) {
					sxy[shift] += (Ftag[winIndex] * Rtag[winIndex + shift]);
				}
			}
			
			ALLNodes.get(x).setCount(count);
			ALLNodes.get(x).setSx(sx);
			ALLNodes.get(x).setSxx(sxx);
			ALLNodes.get(x).setSy(sy);
			ALLNodes.get(x).setSyy(syy);
			ALLNodes.get(x).setSxy(sxy);
			
			//Remove residual tag data after analysis to conserve memory
			ALLNodes.get(x).setData(null);
			
			progressCounter++;
			int current = ((int)(0.5 + ((double)progressCounter / (double)ALLNodes.size() * 100)));
			if(current > currentProgress) {
				currentProgress = current;
				System.out.print(current + "%" + "\t0 {");
				for(int bar = 1; bar <= 20; bar++) {
					if(current >= bar * 5) { System.out.print("="); }
					else { System.out.print(" "); }
				}
				System.out.print("} 100\r");
				System.out.flush();
			}
		}
	}

	/**
	 * Extract from BAM file for a specific node.
	 * 
	 * @param current the specific node to extract read counts for
	 * @param param   the parameter options used to configure extraction
	 * @return the list of forward and reverse tag count primitive arrays
	 */
	public Vector<double[]> extractRegion(CorrNode current, CorrParameter param) {
		int WINDOW = current.getStop() - current.getStart();
		if(WINDOW < 1) {
			System.out.println(WINDOW + "\t" + current.getStop() + "\t" + current.getStart() + "\t" + param.getResolution());
		}
		double[] Ftags = new double[(WINDOW / param.getResolution()) + 1];
		double[] Rtags = new double[(WINDOW / param.getResolution()) + 1];
		String CHROM = current.getChrom();
		int START = current.getStart();
		int STOP = current.getStop();
		int RESOLUTION = param.getResolution();

		//Create an iterator for every tag overlapping this region.
		try {
//			CloseableIterator<SAMRecord> iter = inputSam.query(CHROM, START - 1, STOP + 1, false);
			CloseableIterator<SAMRecord> iter = reader.query(CHROM, START - 1, STOP + 1, false);
//			/* Iterate through the records that overlap this region. */
			while (iter.hasNext()) {
				/* Create the record object */
				SAMRecord sr = iter.next();
				/* Get the start of the record */
				int recordStart = sr.getAlignmentStart();
				int windowMin = START;
				int windowMax = STOP;

				if (sr.getReadNegativeStrandFlag()) {
					recordStart = recordStart + sr.getReadLength() - 1;
					recordStart += 10;
					for(int i = recordStart; i >= recordStart - 21; i--){
						if(i - windowMin >= 0 && i <= windowMax && (i - START) % RESOLUTION == 0) {
							Rtags[(i - windowMin) / RESOLUTION]++;
						}
					}
					/*if(recordStart - windowMin >= 0 && recordStart <= windowMax && (recordStart - START) % RESOLUTION == 0) { 
						Rtags[(recordStart - windowMin) / RESOLUTION]++;
					}*/
				}
				else {
					recordStart -= 10;
					for(int i = recordStart; i <= recordStart + 21; i++){
						if(i - windowMin >= 0 && i <= windowMax && (i - START) % RESOLUTION == 0) {
							Ftags[(i - windowMin) / RESOLUTION]++;
						}
					}
					/*if(recordStart - windowMin >= 0 && recordStart <= windowMax && (recordStart - START) % RESOLUTION == 0) {
						Ftags[(recordStart - windowMin) / RESOLUTION]++;
					}*/
				}
			}
			iter.close();
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.out.println("Exception caught");
		}
		Vector<double[]> retVal = new Vector<double[]>();
		retVal.add(Ftags);
		retVal.add(Rtags);
		return retVal;
	}

	/**
	 * Resest the progressCounter and currentProgress to 0.
	 */
	public static void resetProgress() {
		progressCounter = 0;
		currentProgress = 0;
	}
}