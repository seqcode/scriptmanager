package scriptmanager.scripts.BAM_Statistics;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloseableIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.jfree.chart.ChartPanel;

import scriptmanager.charts.Histogram;
import scriptmanager.charts.LineChart;

/**
 * Tally insert-size statistics by generating insert histogram and tally counts
 * (GEO requirement) along with alignment statistics and parameters from
 * paired-end BAM files.
 * 
 * @author Olivia Lang
 * @see scriptmanager.cli.BAM_Statistics.PEStatsCLI
 * @see scriptmanager.window_interface.BAM_Statistics.PEStatWindow
 * @see scriptmanager.window_interface.BAM_Statistics.PEStatOutput
 */
public class PEStats {
	
	/**
	 * Simplified wrapper for CLI call. Same as `getPEStats` with PrintStream
	 * objects in signature set to null (see CLI).
	 * 
	 * @param out_basename name of output file (without extensions)
	 * @param bamFile      BAM file to be analyzed (indexed)
	 * @param DUP_STATUS   specifies if duplication statistics and chart should be
	 *                     generated
	 * @param MIN_INSERT   maximum histogram range
	 * @param MAX_INSERT   minimum histogram range
	 * @return two-item list of charts to display (0=insert size chart,
	 *         1=duplication chart if DUP_STATUS=true)
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Vector<ChartPanel> getPEStats(File bamFile, File out_basename, boolean DUP_STATUS, int MIN_INSERT, int MAX_INSERT ) throws FileNotFoundException, IOException {
		return (getPEStats(bamFile, out_basename, DUP_STATUS, MIN_INSERT, MAX_INSERT, true, null, null));
	}

	/**
	 * Creates Insert-size Histograms and print alignment statistics to window and
	 * output file (if provided)
	 * 
	 * @param bamFile      BAM file to be analyzed (indexed
	 * @param out_basename name of output file (without extensions)
	 * @param DUP_STATUS   specifies if duplication statistics and chart should be
	 *                     generated
	 * @param MIN_INSERT   maximum histogram range
	 * @param MAX_INSERT   minimum histogram range
	 * @param OUTPUT_STATUS whether or not to write the output to files
	 * @param PS_INSERT    destination for writing insert statistics (for GUI
	 *                     display, does not write if null)
	 * @param PS_DUP       destination for writing duplication statistics (for GUI
	 *                     display, does not write if null)
	 * @return two-item list of charts to display (0=insert size chart,
	 *         1=duplication chart if DUP_STATUS=true)
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Vector<ChartPanel> getPEStats(File bamFile, File out_basename, boolean DUP_STATUS, int MIN_INSERT, int MAX_INSERT, boolean OUTPUT_STATUS, PrintStream PS_INSERT, PrintStream PS_DUP) throws IOException {
		final SamReaderFactory factory = SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS).validationStringency(ValidationStringency.SILENT);
		
		// Output Vector of Charts to be returned
		Vector<ChartPanel> charts = new Vector<ChartPanel>(2);
		//Add empty chart placeholders (prevent GUI display breaking)
		charts.add(0, new ChartPanel(null));
		charts.add(1, new ChartPanel(null));

		// Output files to be saved
		PrintStream OUT_INSERT = null;
		File OUT_INSPNG = null;
		PrintStream OUT_DUP = null;
		File OUT_DUPPNG = null;
		
		// Set output PrintStreams and PNG file objects
		if( out_basename!=null && OUTPUT_STATUS) {
			OUT_INSERT = new PrintStream(new File( out_basename.getCanonicalPath() + "_InsertHistogram.out"));
			OUT_INSPNG = new File( out_basename.getCanonicalPath() + "_PE.png");
			if( DUP_STATUS ){
				OUT_DUP = new PrintStream(new File( out_basename.getCanonicalPath() + "_DuplicationSummary.out"));
				OUT_DUPPNG = new File( out_basename.getAbsolutePath() + "_DUP.png" );
			}
		}
		
		//Print TimeStamp
		String time = getTimeStamp();
		printBoth( PS_INSERT, OUT_INSERT, "# " + time );
		printBoth( PS_DUP, OUT_DUP, "# " + time );

		//Check if BAI index file exists
		File f = new File(bamFile + ".bai");
		if(f.exists() && !f.isDirectory()) {
			
			//Print input filename to all output PrintStreams
			printBoth( PS_INSERT, OUT_INSERT, "# " + bamFile.getName() );
			printBoth( PS_DUP, OUT_DUP, "# " + bamFile.getName() );
			//Print headers to respective PrintStreams
			printBoth( PS_INSERT, OUT_INSERT, "# Chromosome_ID\tChromosome_Size\tAligned_Reads\tUnaligned_Reads" );
			printBoth( PS_DUP, OUT_DUP, "# Duplicate Rate\tNumber of Duplicate Molecules" );
			
			//Code to get individual chromosome stats
			SamReader reader = factory.open(bamFile);
			AbstractBAMFileIndex bai = (AbstractBAMFileIndex) reader.indexing().getIndex();
			System.out.println(bamFile);
			
			//Variables to keep track of insert size histogram
			double counter = 0;
			double[] HIST = new double[(MAX_INSERT - MIN_INSERT) + 1];
			
			//Variables to contain duplication rates
			HashMap<String, Integer> CHROM_COMPLEXITY = null;
			HashMap<Integer, Integer> ALL_COMPLEXITY = new HashMap<Integer, Integer>();
			
			//Variables which contain basic sequence information
			double totalAlignedRead1 = 0;
			double totalAlignedRead2 = 0;
			double totalAlignedReads = 0;
			double totalGenome = 0;
		
			for (int z = 0; z < bai.getNumberOfReferences(); z++) {
				SAMSequenceRecord seq = reader.getFileHeader().getSequence(z);
				double aligned = reader.indexing().getIndex().getMetaData(z).getAlignedRecordCount();
				double unaligned = reader.indexing().getIndex().getMetaData(z).getUnalignedRecordCount();
				
				//Basic statistic calculations
				System.out.println("Processing: " + seq.getSequenceName());
				
				printBoth( PS_INSERT, OUT_INSERT, "# " + seq.getSequenceName() + "\t" + seq.getSequenceLength() + "\t" + aligned + "\t" + unaligned );
				totalGenome += seq.getSequenceLength();
				
				//Loop through each chromosome looking at each perfect F-R PE reads
				CHROM_COMPLEXITY = new HashMap<String, Integer>();
				CloseableIterator<SAMRecord> iter = reader.query(seq.getSequenceName(), 0, seq.getSequenceLength(), false);
				while (iter.hasNext()) {
					//Create the record object 
					SAMRecord sr = iter.next();
					if(!sr.getReadUnmappedFlag()) { //Test for mapped read
						if(sr.getReadPairedFlag()) { //Test for paired-end status
							if(sr.getSecondOfPairFlag()) { totalAlignedRead2++; } //count read 2
							else if(sr.getFirstOfPairFlag()) { totalAlignedRead1++; } // count read 1
							//Insert size calculations
							if(sr.getProperPairFlag() && sr.getFirstOfPairFlag()) {
								//Insert size calculations
								int distance = Math.abs(sr.getInferredInsertSize());
								if(distance <= MAX_INSERT && distance >= MIN_INSERT) HIST[distance - MIN_INSERT]++;
								counter++;
								
								if(DUP_STATUS) {
									//Unique ID
									String tagName = sr.getAlignmentStart() + "_" + sr.getMateAlignmentStart() + "_" + sr.getInferredInsertSize();
									//Duplication rate for each chrom determined
									if(CHROM_COMPLEXITY.isEmpty()) {
										CHROM_COMPLEXITY.put(tagName, Integer.valueOf(1));
									} else if(!CHROM_COMPLEXITY.containsKey(tagName)) {
										CHROM_COMPLEXITY.put(tagName, Integer.valueOf(1));
									} else if(CHROM_COMPLEXITY.containsKey(tagName)){
										CHROM_COMPLEXITY.put(tagName, Integer.valueOf(((Integer) CHROM_COMPLEXITY.get(tagName)).intValue() + 1));
									}
								}
							}
						} else { //If the read is mapped but not paired-end, default to read 1
							totalAlignedRead1++;
						}
					}
				}
				iter.close();
				
				if(DUP_STATUS) {
					//Load each chromosome up into master duplication hashmap
					Iterator<String> keys = CHROM_COMPLEXITY.keySet().iterator();
					while(keys.hasNext()) {
						 String str = (String) keys.next();
						 if(ALL_COMPLEXITY.isEmpty()) {
							 ALL_COMPLEXITY.put(CHROM_COMPLEXITY.get(str), Integer.valueOf(1));
						 } else if(!ALL_COMPLEXITY.containsKey(CHROM_COMPLEXITY.get(str))) {
							 ALL_COMPLEXITY.put(CHROM_COMPLEXITY.get(str), Integer.valueOf(1));
						 } else if(ALL_COMPLEXITY.containsKey(CHROM_COMPLEXITY.get(str))){
								ALL_COMPLEXITY.put(CHROM_COMPLEXITY.get(str), Integer.valueOf(((Integer) ALL_COMPLEXITY.get(CHROM_COMPLEXITY.get(str))).intValue() + 1));
						 }
					}
				}
			}
			totalAlignedReads = totalAlignedRead1 + totalAlignedRead2;
			printBoth( PS_INSERT, OUT_INSERT, "# Total Genome Size: " + totalGenome + "\tTotal Aligned Tags: " + totalAlignedReads );
			
			//Output replicates used to make bam file
			for( String comment : reader.getFileHeader().getComments()) {
				printBoth( PS_INSERT, OUT_INSERT, "# " + comment );
			}
			
			//Output program used to align bam file
			for (int z = 0; z < reader.getFileHeader().getProgramRecords().size(); z++) {
				printBoth( PS_INSERT, OUT_INSERT, "# " + reader.getFileHeader().getProgramRecords().get(z).getId() + "\t# " + reader.getFileHeader().getProgramRecords().get(z).getProgramVersion() );
				printBoth( PS_INSERT, OUT_INSERT, "# " + reader.getFileHeader().getProgramRecords().get(z).getCommandLine() );
			}

			//close readers
			reader.close();
			bai.close();

			//Insert Size statistics
			printBoth( PS_INSERT, OUT_INSERT, "# Average Insert Size: " + getAverage(HIST, MIN_INSERT, MAX_INSERT));
			printBoth( PS_INSERT, OUT_INSERT, "# Median Insert Size: " + getMedian(HIST, MIN_INSERT, MAX_INSERT) );
			printBoth( PS_INSERT, OUT_INSERT, "# Mode Insert Size: " + getMode(HIST, MIN_INSERT, MAX_INSERT) );
			printBoth( PS_INSERT, OUT_INSERT, "# Std deviation of Insert Size: " + getStdDev(HIST, MIN_INSERT, MAX_INSERT) );
			printBoth( PS_INSERT, OUT_INSERT, "# Number of ReadPairs: " + counter );

			//Insert Size frequencies
			printBoth( PS_INSERT, OUT_INSERT, "# Histogram" );
			printBoth( PS_INSERT, OUT_INSERT, "# Size (bp)\tFrequency" );
			int[] DOMAIN = new int[(MAX_INSERT - MIN_INSERT) + 1];
			for(int z = 0; z < HIST.length; z++) {
				int bp = MIN_INSERT + z;
				DOMAIN[z] = bp;
				printBoth( PS_INSERT, OUT_INSERT, bp + "\t" + HIST[z] );
			}

			//Generate Insert Chart
			charts.add( 0, Histogram.createBarChart(HIST, DOMAIN, OUT_INSPNG) );

			//Duplication statistics
			if(DUP_STATUS) {
				double UNIQUE_MOLECULES = 0;
				String[] BIN_NAME = initializeBIN_Names();
				ArrayList<Double> BIN = new ArrayList<Double>();
				initializeBINS(BIN);
				
				Iterator<Integer> keys = ALL_COMPLEXITY.keySet().iterator();
				while(keys.hasNext()) {
					 Integer str = (Integer) keys.next();
					 int index = getBinIndex(str.intValue());
					 BIN.set(index, BIN.get(index) + (ALL_COMPLEXITY.get(str).doubleValue() * str.doubleValue()));		         
					 UNIQUE_MOLECULES += ALL_COMPLEXITY.get(str).doubleValue(); 
				}
				
				for(int z = 0; z < BIN.size(); z++) {
					printBoth( PS_DUP, OUT_DUP, BIN_NAME[z] + "\t" + BIN.get(z).toString() );
				}
				printBoth( PS_DUP, OUT_DUP, "# Unique Molecules:\n" + UNIQUE_MOLECULES);
				
				//Generate Duplicates Chart
				try{
					charts.add( 1, LineChart.createLineChart(BIN, BIN_NAME, OUT_DUPPNG) );
				}catch( IOException e ){ e.printStackTrace(); }
			}
		} else {
			//Add messages to output indicating missing ref files
			printBoth( PS_INSERT, OUT_INSERT, "BAI Index File missing for: " + bamFile.getName() );
			printBoth( PS_DUP, OUT_DUP, "BAI Index File missing for: " + bamFile.getName() );
		}

		//close writers
		if (OUT_INSERT != null) { OUT_INSERT.close(); }
		if (OUT_DUP != null) { OUT_DUP.close(); }

		return(charts);
	}
	
	/**
	 * Returns the median value of values a histogram within range limit
	 * 
	 * @param histogram Histogram to be analyzed
	 * @param MIN_INSERT Minimum range of histogram
	 * @param MAX_INSERT Maximum range of histogram
	 * @return the median value of values in the histogram
	 */
	public static double getMedian(double[] histogram, int MIN_INSERT, int MAX_INSERT) {
		double sum = 0;
		for(int x = 0; x < histogram.length; x++) { sum += histogram[x]; }
		if(sum % 2 == 1 && sum > 0) {
			int num = (int) ((sum + 1) / 2);
			double count = 0;
			for(int x = 0; x < histogram.length; x++) {
				count += histogram[x];
				if(count >= num) return (x + MIN_INSERT);
			}
		} else if(sum > 0) {
			double first = -999;
			double second = -999;
			int num = (int) (sum / 2);
			double count = 0;
			for(int x = 0; x < histogram.length; x++) {
				count += histogram[x];
				if(count >= num & first == -999) first = (x + MIN_INSERT);
				if(count >= num + 1) second = (x + MIN_INSERT);
				if(first != -999 && second != -999) { return (first + second) / 2; }
			}
		}
		return 0;
	}

	/**
	 * Returns the average value of values a histogram within range limit
	 * 
	 * @param histogram Histogram to be analyzed
	 * @param MIN_INSERT Minimum range of histogram
	 * @param MAX_INSERT Maximum range of histogram
	 * @return the average value of values in the histogram
	 */
	public static double getAverage(double[] histogram, int MIN_INSERT, int MAX_INSERT) {
		double sum = 0;
		int counter = 0;
		for (int x = 0; x < histogram.length; x++) {
			sum += histogram[x] * (x + MIN_INSERT);
			counter += histogram[x];
		}
		// Return zero if empty no insert sizes caught
		return(counter > 0 ? sum/counter : 0);
	}

	/**
	 * Returns the mode value of values a histogram within range limit
	 * 
	 * @param histogram Histogram to be analyzed
	 * @param MIN_INSERT Minimum range of histogram
	 * @param MAX_INSERT Maximum range of histogram
	 * @return the mode value of values in the histogram
	 */
	public static double getMode(double[] histogram, int MIN_INSERT, int MAX_INSERT) {
		double maxNumReads = 0;
		int value = 0;
		for (int x = 0; x < histogram.length; x++) {
			if (histogram[x] > maxNumReads) {
				maxNumReads = histogram[x];
				value = x + MIN_INSERT;
			}
		}
		return(value);
	}

	/**
	 * Returns the standard deviation for a histogram within range limit
	 * 
	 * @param histogram  Histogram to be analyzed
	 * @param avg        Average of values in the histogram
	 * @param MIN_INSERT Minimum range of histogram
	 * @param MAX_INSERT Maximum range of histogram
	 * @return The standard deviation for the histogram
	 */
	public static double getStdDev(double[] histogram, int MIN_INSERT, int MAX_INSERT) {
		double avg = getAverage(histogram, MIN_INSERT, MAX_INSERT);
		double stddev = 0;
		double sum = 0;
		for(int x = 0; x < histogram.length; x++) {
			stddev += (Math.pow(((x + MIN_INSERT) - avg), 2) * histogram[x]);
			sum += histogram[x];
		}
		if(sum > 0) return Math.sqrt(stddev / sum);
		else return 0;
	}
	
	/**
	 * Returns the correct X-value for the Pair-End Duplication Rate plot given a
	 * number of duplications
	 * 
	 * @param COUNT Frequency/number of duplications
	 * @return The index/X-value for a given number of duplications
	 */
	public static int getBinIndex(int COUNT) {
		if(COUNT == 1) return 0;
        else if(COUNT >= 2 && COUNT <= 10) return 1;
        else if(COUNT >= 11 && COUNT <= 25) return 2;
        else if(COUNT >= 26 && COUNT <= 50) return 3;
        else if(COUNT >= 51 && COUNT <= 75) return 4;
        else if(COUNT >= 76 && COUNT <= 100) return 5;
        else if(COUNT >= 101 && COUNT <= 125) return 6;
        else if(COUNT >= 126 && COUNT <= 150) return 7;
        else if(COUNT >= 151 && COUNT <= 250) return 8;
        else if(COUNT >= 251 && COUNT <= 500) return 9;
        else if(COUNT >= 501 && COUNT <= 1000) return 10;
        else if(COUNT >= 1001 && COUNT <= 5000) return 11;
        else if(COUNT >= 5001 && COUNT <= 10000) return 12;
        else if(COUNT >= 10001) return 13;
		
		return -999;
	}
	
	/**
	 * Initializes ArrayList of values (the X-axis) for Pair-End Duplication Rate
	 * plot
	 * 
	 * @param BIN ArrayList to be initialized
	 */
	public static void initializeBINS(ArrayList<Double> BIN) {
		BIN.add(Double.valueOf(0)); // Bin 1
		BIN.add(Double.valueOf(0)); // Bin 2-10
		BIN.add(Double.valueOf(0)); // Bin 11-25
		BIN.add(Double.valueOf(0)); // Bin 26-50
		BIN.add(Double.valueOf(0)); // Bin 51-75
		BIN.add(Double.valueOf(0)); // Bin 76-100
		BIN.add(Double.valueOf(0)); // Bin 101-125
		BIN.add(Double.valueOf(0)); // Bin 126-150
		BIN.add(Double.valueOf(0)); // Bin 151-250
		BIN.add(Double.valueOf(0)); // Bin 251-500
		BIN.add(Double.valueOf(0)); // Bin 501-1,000
		BIN.add(Double.valueOf(0)); // Bin 1,001-5,000
		BIN.add(Double.valueOf(0)); // Bin 5,001-10,000
		BIN.add(Double.valueOf(0)); // Bin 10,000+
	}
	
	/**
	 * Initializes ArrayList of x-axis labels for Pair-End Duplication Rate plot
	 * 
	 * @return ArrayList of x-axis labels for Pair-End Duplication Rate plot
	 */
	public static String[] initializeBIN_Names() {
		String[] NAME = new String[14];
		NAME[0] = "1";
		NAME[1] = "2-10";
		NAME[2] = "11-25";
		NAME[3] = "26-50";
		NAME[4] = "51-75";
		NAME[5] = "76-100";
		NAME[6] = "101-125";
		NAME[7] = "126-150";
		NAME[8] = "151-250";
		NAME[9] = "251-500";
		NAME[10] = "501-1,000";
		NAME[11] = "1,001-5,000";
		NAME[12] = "5,001-10,000";
		NAME[13] = "10,000+";
		return NAME;
	}
	
	private static String getTimeStamp() {
		Date date= new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}

	/**
	 * Helper methods to de-clutter print statements. `println` string to each of
	 * two printstream objects if they are non-null
	 * 
	 * @param p    one of two streams to print to if non-null
	 * @param out  one of two streams to print to if non-null
	 * @param line the string to attempt to print to each stream
	 */
	private static void printBoth( PrintStream p, PrintStream out, String line ){
		if( p != null ){ p.println(line); }
		if( out!=null ){ out.println(line); }
	}
}