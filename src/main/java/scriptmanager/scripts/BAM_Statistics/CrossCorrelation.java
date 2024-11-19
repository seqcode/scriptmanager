package scriptmanager.scripts.BAM_Statistics;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import scriptmanager.charts.CompositePlot;
import scriptmanager.objects.ArchTEx.CorrExtract;
import scriptmanager.objects.ArchTEx.CorrNode;
import scriptmanager.objects.ArchTEx.CorrParameter;

/**
 * ArchTEx's cross-correlation analysis. <br>
 * Code largely sourced from ArchTEx.analysis.corr.CorrLoad in <a href=
 * "https://github.com/WilliamKMLai/ArchTEx">https://github.com/WilliamKMLai/ArchTEx</a>
 * 
 * @author William KM Lai
 * @see scriptmanager.objects.ArchTEx.CorrParameter
 * @see scriptmanager.cli.BAM_Statistics.CrossCorrelationCLI
 * @see scriptmanager.window_interface.BAM_Statistics.CrossCorrelationOutput
 * @see scriptmanager.window_interface.BAM_Statistics.CrossCorrelationWindow
 */
public class CrossCorrelation {

	/**
	 * Perform the cross-correlation analysis from ArchTEx of a BAM file by
	 * correlating forward and reverse strand pileups at various tag shifts to
	 * determine the tag shift with the strongest correlation between strands.
	 * 
	 * @param bamFile     the BAM file to determine the best strand correlated tag
	 *                    shift for
	 * @param output      the output file to write TagShift--&gt;Correlation score
	 *                    pair results
	 * @param param       the object for storing user-defined parameters for the
	 *                    cross-correlation
	 * @param PS_CCDATA   where progress updates and raw correlation scores are
	 *                    written as the script executes
	 * @return the JFreeChart-based line plot of corrlation (y-axis) scores for a
	 *         range of shifts (x-axis)
	 */
	public static Component correlate(File bamFile, File output, CorrParameter param, PrintStream PS_CCDATA) {
		//Check if BAI index file exists
		File f = new File(bamFile + ".bai");
		if(!f.exists() || f.isDirectory()) {
			if( PS_CCDATA!=null ){ PS_CCDATA.println("BAI Index File does not exist for: " + bamFile.getName()); }
			System.err.println("BAI Index File does not exist for: " + bamFile.getName());
			return(null);
		}
		System.out.println("Cross-Correlation: " + bamFile);

		// Output files to be saved
		PrintStream OUT_CCDATA = null;
		
		// Set output file printstream
		if( output!=null ) {
			try {
				OUT_CCDATA = new PrintStream(output);
			} catch (IOException ioe) { ioe.printStackTrace(); }
		}

		// Start timestamp
		String time = new Timestamp(new Date().getTime()).toString();
		System.out.println("Start: " + time);
		if (PS_CCDATA!=null ) { PS_CCDATA.println("Start: " + time); }

		// Print params to output
		String paramDescription = "# type=Genome;";
		if (!param.getCorrType()) {
			paramDescription = "# type=Random; window=" + param.getCorrWindow() + "; numSites=" + param.getIterations() + ";";
		}
		System.out.println(paramDescription);
		if (PS_CCDATA!=null ) { PS_CCDATA.println(paramDescription); }
		if (OUT_CCDATA!=null ) { OUT_CCDATA.println(paramDescription); }

		// Data for building C-C Plots and printing C-C Data
		double[] xData = new double[param.getCorrWindow()];
		double[] yData = new double[param.getCorrWindow()];

		//Code to get individual chromosome stats
		final SamReaderFactory factory = SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS).validationStringency(ValidationStringency.SILENT);
		SamReader reader = factory.open(bamFile);
		AbstractBAMFileIndex bai = (AbstractBAMFileIndex) reader.indexing().getIndex();
//		SAMFileReader reader = new SAMFileReader(bamFile, new File(bamFile.getAbsoluteFile()+".bai"));
//		AbstractBAMFileIndex bai = (AbstractBAMFileIndex) reader.getIndex();

		double[] Sx = new double[param.getCorrWindow()];
		double[] Sxx = new double[param.getCorrWindow()];
		double[] Sy = new double[param.getCorrWindow()];
		double[] Syy = new double[param.getCorrWindow()];
		double[] Sxy = new double[param.getCorrWindow()];
		double[] count = new double[param.getCorrWindow()];

		//Need to extract all at once for optimal efficiency
		Vector<CorrNode> ChromosomeWindows;	

		for(int numchrom = 0; numchrom < bai.getNumberOfReferences(); numchrom++) {
			//Object to keep track of the chromosomal data
			ChromosomeWindows = new Vector<CorrNode>();
			SAMSequenceRecord seq = reader.getFileHeader().getSequence(numchrom);
//			net.sf.samtools.SAMSequenceRecord seq = reader.getFileHeader().getSequence(numchrom);
			System.out.println("\nAnalyzing: " + seq.getSequenceName());
			if (PS_CCDATA!=null ) { PS_CCDATA.println("Analyzing: " + seq.getSequenceName()); }
			if(!param.getCorrType()) {
				//Randomly select #_Iteration sample from each chromosome at user-specified size
				for(int x = 0; x < param.getIterations(); x++) {
					int start = (int)(Math.random() * (double)(seq.getSequenceLength() - param.getWindow()));
					int stop = start + param.getWindow();
					ChromosomeWindows.add(new CorrNode(seq.getSequenceName(), start, stop));
				}
				
			} else {
				//Break chromosome into 100kb chunks and assign to independent BLASTNodes
				int numwindows = (int) (seq.getSequenceLength() / 100_000);
				int Resolution = param.getResolution();	// Resolution controls increment
				int windowSize = param.getCorrWindow(); // Size of theoretical sliding window
				for(int x = 0; x < numwindows; x++) {
					int start = x * 100_000;
					int stop = start + 100_000 + windowSize;
					ChromosomeWindows.add(new CorrNode(seq.getSequenceName(), start, stop));
				}
				int finalstart = numwindows * 100_000;
				int finalstop = (seq.getSequenceLength() / Resolution) * Resolution;
				ChromosomeWindows.add(new CorrNode(seq.getSequenceName(), finalstart, finalstop));
			}

			//Load Chromosome Windows with data from ALL experiments
			int numberofThreads = param.getThreads();
			int nodeSize = ChromosomeWindows.size();
			if(nodeSize < numberofThreads) {
				numberofThreads = nodeSize;
			}
			ExecutorService parseMaster = Executors.newFixedThreadPool(numberofThreads);
			int subset = 0;
			int currentindex = 0;
			for(int x = 0; x < numberofThreads; x++) {
				currentindex += subset;
				if(numberofThreads == 1) subset = nodeSize;
				else if(nodeSize % numberofThreads == 0) subset = nodeSize / numberofThreads;
				else {
					int remainder = nodeSize % numberofThreads;
					if(x < remainder ) subset = (int)(((double)nodeSize / (double)numberofThreads) + 1);
					else subset = (int)(((double)nodeSize / (double)numberofThreads));
				}
				
				CorrExtract nodeextract = new CorrExtract(bamFile, param, ChromosomeWindows, currentindex, subset);
				parseMaster.execute(nodeextract);
			}
			parseMaster.shutdown();
			while (!parseMaster.isTerminated()) {
			}

			CorrExtract.resetProgress();
			
			for(int x = 0; x < ChromosomeWindows.size(); x++) {
				for(int y = 0; y < Sx.length; y++) {
					Sx[y] += ChromosomeWindows.get(x).getSx();
					Sxx[y] += ChromosomeWindows.get(x).getSxx();
					Sy[y] += ChromosomeWindows.get(x).getSy();
					Syy[y] += ChromosomeWindows.get(x).getSyy();
					Sxy[y] += ChromosomeWindows.get(x).getSxy()[y];
					count[y] += ChromosomeWindows.get(x).getCount();
				}
			}
		}

		System.err.println("\nTagShift\tCorrelation");
		if (PS_CCDATA!=null ) { PS_CCDATA.println("\nTagShift\tCorrelation"); }
		if (OUT_CCDATA!=null ) { OUT_CCDATA.println("TagShift\tCorrelation"); }

		double[] numerator = new double[param.getCorrWindow()];
		double[] denominator = new double[param.getCorrWindow()];

		double PEAK = -999;
		int PEAK_SHIFT = -999;
		for(int x = 0; x < Sx.length; x++) {
			numerator[x] = Sxy[x] - ((Sx[x] * Sy[x]) / count[x]);
			denominator[x] = Math.sqrt((Sxx[x] - ((Sx[x] * Sx[x]) / count[x])) * (Syy[x] - ((Sy[x] * Sy[x] / count[x]))));
			yData[x] = numerator[x] / denominator[x];
			xData[x] = x;
			if(yData[x] > PEAK) {
				PEAK = yData[x];
				PEAK_SHIFT = (int)x;
			}
			//System.out.println(Sx[x] + "\t" + Sxx[x] + "\t" + Sy[x] + "\t" + Syy[x] + "\t" + Sxy[x] + "\t" + count[x]);
			if (PS_CCDATA!=null ) { PS_CCDATA.println((int)xData[x] + "\t" + yData[x]); }
			if (OUT_CCDATA!=null ) { OUT_CCDATA.println((int)xData[x] + "\t" + yData[x]); }
		}

		// Close BAM & BAI files
		try{
			reader.close();
			bai.close();
		}catch (IOException ioe) { ioe.printStackTrace(); }

		System.err.println("Analysis Complete\n");
		if (PS_CCDATA!=null ) { PS_CCDATA.println("Analysis Complete\n"); }

		System.out.println("Peak At: " + PEAK_SHIFT + "\nScore: " + PEAK);
		if (PS_CCDATA!=null ) { PS_CCDATA.println("Peak At: " + PEAK_SHIFT + "\nScore: " + PEAK); }
		if (OUT_CCDATA!=null ) { OUT_CCDATA.println("Peak At: " + PEAK_SHIFT + "\nScore: " + PEAK); }

		// Close output stream
		if(PS_CCDATA != null){ PS_CCDATA.close(); }
		if(OUT_CCDATA != null){ OUT_CCDATA.close(); }

		// Finish timestamp
		time = new Timestamp(new Date().getTime()).toString();
		System.out.println("Finish: " + time);
		if (PS_CCDATA!=null ) { PS_CCDATA.println("Finish: " + time); }

		return(CompositePlot.createCompositePlot(xData, yData, bamFile.getName()));
	}
}
