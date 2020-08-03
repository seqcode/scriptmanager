package scripts.Peak_Analysis;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloseableIterator;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import org.jfree.chart.ChartPanel;
// import org.jfree.chart.JFreeChart;

import charts.LineChart;
import objects.CoordinateObjects.GFFCoord;

@SuppressWarnings("serial")
public class SignalDuplication {
	File bamFile = null;
	ArrayList<GFFCoord> COORD = null;
	ArrayList<GFFCoord> GENOME = null;
	
	File input = null;
	private double WINDOW = 0;
	
	SamReader reader;
	final SamReaderFactory factory = SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS).validationStringency(ValidationStringency.SILENT);
	
	File output;
	PrintStream OUT = null;
	
	ChartPanel lineChart;
	
	public SignalDuplication(File in, File bam, double w, PrintStream out) {
		bamFile = bam;
		input = in;
		WINDOW = w;
		OUT = out;
	}
	
	public void run() throws IOException {
		
		//Print TimeStamp & Input
		String time = getTimeStamp();
		printOUT(time);
		printOUT(bamFile.getName());
		
		//Load up GFF file into COORD
		loadCoord();
		Collections.sort(COORD, GFFCoord.PeakPositionComparator);
		Collections.sort(COORD, GFFCoord.PeakChromComparator);
		
		//Code to get individual chromosome stats
		reader = factory.open(bamFile);
		AbstractBAMFileIndex bai = (AbstractBAMFileIndex) reader.indexing().getIndex();
		
		//Variables to contain duplication rates
		HashMap<String, Integer> CHROM_COMPLEXITY = null;
		HashMap<Integer, Integer> SIG_COMPLEXITY = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> GEN_COMPLEXITY = new HashMap<Integer, Integer>();
		
		for (int z = 0; z < bai.getNumberOfReferences(); z++) {
			SAMSequenceRecord seq = reader.getFileHeader().getSequence(z);
							
			//Loop through each chromosome looking at each perfect F-R PE read
			CHROM_COMPLEXITY = new HashMap<String, Integer>();
			CloseableIterator<SAMRecord> iter = reader.query(seq.getSequenceName(), 0, seq.getSequenceLength(), false);
			while (iter.hasNext()) {
				//Create the record object 
				SAMRecord sr = iter.next();
								
				if(sr.getReadPairedFlag()) {
					if(sr.getProperPairFlag() && sr.getFirstOfPairFlag()) {
						//Unique ID
						String tagName = sr.getAlignmentStart() + "_" + sr.getMateAlignmentStart() + "_" + sr.getInferredInsertSize();
						//Duplication rate for each chrom determined
						if(CHROM_COMPLEXITY.isEmpty()) {
							CHROM_COMPLEXITY.put(tagName, new Integer(1));
						} else if(!CHROM_COMPLEXITY.containsKey(tagName)) {
							CHROM_COMPLEXITY.put(tagName, new Integer(1));
						} else if(CHROM_COMPLEXITY.containsKey(tagName)){
							CHROM_COMPLEXITY.put(tagName, new Integer(((Integer) CHROM_COMPLEXITY.get(tagName)).intValue() + 1));
						}
						
					}
				}
			}
			iter.close();
			
			//Load each chromosome up into master duplication hashmap
			Iterator<String> keys = CHROM_COMPLEXITY.keySet().iterator();
			while(keys.hasNext()) {
				 String str = (String) keys.next();
				//Determine if molecule has ANY overlap with determined signal region
				if(checkOverlap(seq.getSequenceName(), str, COORD)) { //if so, push into SIGNAL hash
					 if(SIG_COMPLEXITY.isEmpty()) {
						 SIG_COMPLEXITY.put(CHROM_COMPLEXITY.get(str), new Integer(1));
					 } else if(!SIG_COMPLEXITY.containsKey(CHROM_COMPLEXITY.get(str))) {
						 SIG_COMPLEXITY.put(CHROM_COMPLEXITY.get(str), new Integer(1));
					 } else if(SIG_COMPLEXITY.containsKey(CHROM_COMPLEXITY.get(str))){
						 SIG_COMPLEXITY.put(CHROM_COMPLEXITY.get(str), new Integer(((Integer) SIG_COMPLEXITY.get(CHROM_COMPLEXITY.get(str))).intValue() + 1));
					 }
				} else { 	//else, push into BACKGROUND hash
					 if(GEN_COMPLEXITY.isEmpty()) {
						 GEN_COMPLEXITY.put(CHROM_COMPLEXITY.get(str), new Integer(1));
					 } else if(!GEN_COMPLEXITY.containsKey(CHROM_COMPLEXITY.get(str))) {
						 GEN_COMPLEXITY.put(CHROM_COMPLEXITY.get(str), new Integer(1));
					 } else if(GEN_COMPLEXITY.containsKey(CHROM_COMPLEXITY.get(str))){
						 GEN_COMPLEXITY.put(CHROM_COMPLEXITY.get(str), new Integer(((Integer) GEN_COMPLEXITY.get(CHROM_COMPLEXITY.get(str))).intValue() + 1));
					 }
				}
			}			
		}
		
		reader.close();
		bai.close();

		//Duplication statistics
		double SIG_UNIQUE_MOLECULES = 0;
		double GEN_UNIQUE_MOLECULES = 0;
		String[] BIN_NAME = initializeBIN_Names();
		ArrayList<Double> S_BIN = new ArrayList<Double>();
		ArrayList<Double> G_BIN = new ArrayList<Double>();
		initializeBINS(S_BIN);
		initializeBINS(G_BIN);
		
		Iterator<Integer> Ckeys = SIG_COMPLEXITY.keySet().iterator();
		while(Ckeys.hasNext()) {
			 Integer str = (Integer) Ckeys.next();
			 int index = getBinIndex(str.intValue());
			 S_BIN.set(index, S_BIN.get(index) + (SIG_COMPLEXITY.get(str).doubleValue() * str.doubleValue()));
			 SIG_UNIQUE_MOLECULES += SIG_COMPLEXITY.get(str).doubleValue(); 
		}
		
		Iterator<Integer> Gkeys = GEN_COMPLEXITY.keySet().iterator();
		while(Gkeys.hasNext()) {
			 Integer str = (Integer) Gkeys.next();
			 int index = getBinIndex(str.intValue());
			 G_BIN.set(index, G_BIN.get(index) + (GEN_COMPLEXITY.get(str).doubleValue() * str.doubleValue()));
			 GEN_UNIQUE_MOLECULES += GEN_COMPLEXITY.get(str).doubleValue(); 
		}
		
		printOUT("Duplicate Rate\tNumber of Duplicate Molecules");
		for(int z = 0; z < BIN_NAME.length; z++) {
			printOUT(BIN_NAME[z] + "\t" + S_BIN.get(z).toString());
		}
		printOUT("Signal Unique Molecules:\n" + SIG_UNIQUE_MOLECULES);
		printOUT("");

		for(int z = 0; z < BIN_NAME.length; z++) {
			printOUT(BIN_NAME[z] + "\t" + G_BIN.get(z).toString());
		}
		printOUT("Genomic Unique Molecules:\n" + GEN_UNIQUE_MOLECULES);
		
		lineChart = LineChart.createLineChart(S_BIN, G_BIN, BIN_NAME);
		
		if(OUT != null) OUT.close();
	}
	
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
	
	public static void initializeBINS(ArrayList<Double> BIN) {
		BIN.add(new Double(0)); // Bin 1
		BIN.add(new Double(0)); // Bin 2-10
		BIN.add(new Double(0)); // Bin 11-25
		BIN.add(new Double(0)); // Bin 26-50
		BIN.add(new Double(0)); // Bin 51-75
		BIN.add(new Double(0)); // Bin 76-100
		BIN.add(new Double(0)); // Bin 101-125
		BIN.add(new Double(0)); // Bin 126-150
		BIN.add(new Double(0)); // Bin 151-250
		BIN.add(new Double(0)); // Bin 251-500
		BIN.add(new Double(0)); // Bin 501-1,000
		BIN.add(new Double(0)); // Bin 1,001-5,000
		BIN.add(new Double(0)); // Bin 5,001-10,000
		BIN.add(new Double(0)); // Bin 10,000+
	}
	
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
	
	public boolean checkOverlap(String chrom, String ID, ArrayList<GFFCoord> coord) {
		String[] temp = ID.split("_");
		int start = Integer.parseInt(temp[0]); 
		int stop = start + Integer.parseInt(temp[2]);
		if(start > stop) {
			int x = stop;
			stop = start;
			start = x;
		}

		for(int z = 0; z < coord.size(); z++) {
			if(coord.get(z).getChrom().equals(chrom)) {
				if(start <= coord.get(z).getStop() + (WINDOW / 2) && start >= coord.get(z).getStart() - (WINDOW / 2)) { return true; }
				if(stop <= coord.get(z).getStop() +  (WINDOW / 2) && stop >= coord.get(z).getStart() -  (WINDOW / 2)) { return true; }
				if(start <= coord.get(z).getStart() -  (WINDOW / 2) && stop >= coord.get(z).getStop() +  (WINDOW / 2)) { return true; }
			}
		}
		return false;
	}
	
    public void loadCoord() throws FileNotFoundException {
    	//chr1	cwpair	.	45524	45525	3067.0	.	.	cw_distance=26

		Scanner scan = new Scanner(input);
		COORD = new ArrayList<GFFCoord>();
		while (scan.hasNextLine()) {
			String[] temp = scan.nextLine().split("\t");
			if(temp.length > 3) { 
				if(!temp[0].contains("track") && !temp[0].contains("#")) {
					String name = "";
					if(temp.length > 7) { name = temp[8]; }
					else { name = temp[0] + "_" + temp[3] + "_" + temp[4]; }
					if(Integer.parseInt(temp[3]) >= 0) {
						if(temp[6].equals("+") || temp[6].equals(".")) { COORD.add(new GFFCoord(temp[0], Integer.parseInt(temp[3]), Integer.parseInt(temp[4]), "+", name)); }
						else { COORD.add(new GFFCoord(temp[0], Integer.parseInt(temp[3]), Integer.parseInt(temp[4]), "-", name)); }
					} else {
						System.err.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
					}
				}
			}
		}
		scan.close();
    }
	
	private void printOUT(String message){
		if(OUT!=null) OUT.println(message);
	}
	
	private static String getTimeStamp() {
		Date date= new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
	
	public Component getLineChart(){ return(lineChart); }
}