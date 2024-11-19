package scriptmanager.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloseableIterator;

import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.objects.PileupParameters;
import scriptmanager.objects.CoordinateObjects.BEDCoord;

/**
 * Class containing a set of shared methods to be used across script classes.
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Read_Analysis.TagPileupCLI
 * @see scriptmanager.cli.Peak_Analysis.FRiXCalculatorCLI
 * @see scriptmanager.window_interface.Read_Analysis.TagPileupOutput
 * @see scriptmanager.window_interface.Peak_Analysis.FRiXCalculatorOutput
 */
public class BAMUtilities {

	/**
	 * Creates a new BAMUtilities object
	 */
	public BAMUtilities(){}

	/**
	 * Search the BAM header and sum the size of each chromosome/reference sequence
	 * 
	 * @param BAM input BAM-formatted file
	 * @return total bp of ref sequence aligned to
	 * @throws IOException
	 */
	public static double getGenomeSize(File BAM) throws IOException {
//		SamReaderFactory factory = SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS).validationStringency(ValidationStringency.SILENT);
		SamReader factory = SamReaderFactory.makeDefault().open(BAM);
		AbstractBAMFileIndex bai = (AbstractBAMFileIndex) factory.indexing().getIndex();
		// sum the length of each ref sequence
		double totalGenome = 0;
		for (int x = 0; x < bai.getNumberOfReferences(); x++) {
			SAMSequenceRecord seq = factory.getFileHeader().getSequence(x);
			totalGenome += seq.getSequenceLength();
		}
		bai.close();
		factory.close();
		return (totalGenome);
	}

	/**
	 * Loop through BAM index metadata and tally up total aligned read count according to PileupParameters criteria.
	 * 
	 * @param BAM input BAM-formatted file
	 * @return Number of total reads in the BAM file
	 * @throws OptionException
	 * @throws IOException
	 */
	public static double getReadCount(File BAM, PileupParameters p) throws OptionException, IOException {
		// Pre-calculate criteria
		int ASPECT = p.getAspect();
		int READ = p.getRead();
		boolean checkR1 = (READ == PileupParameters.READ1 || READ == PileupParameters.ALLREADS);
		boolean checkR2 = (READ == PileupParameters.READ2 || READ == PileupParameters.ALLREADS);
		boolean checkFiveOrThree = (ASPECT == PileupParameters.FIVE || ASPECT == PileupParameters.THREE);

		// Get total aligned
		double totalAligned = 0;

		SamReader factory = SamReaderFactory.makeDefault().open(BAM);
		CloseableIterator<SAMRecord> iter = factory.iterator();
		while (iter.hasNext()) {
			SAMRecord sr = iter.next();
			// Test for mappability
			if(!sr.getReadUnmappedFlag()) {
				// Test for paired-end status
				if(sr.getReadPairedFlag()) {
					// count read 1
					if (sr.getFirstOfPairFlag()) {
						// count reads if Read1 or All Reads
						if (checkR1) {
							// count properly paired reads if midpoint
							if (sr.getProperPairFlag()) {
								if (ASPECT == PileupParameters.MIDPOINT) {
									totalAligned++;
								} else if (ASPECT == PileupParameters.FRAGMENT) {
									throw new OptionException("PileupParameters.FRAGMENT not supported for getting read counts");
								}
							// count reads if 5' or 3'
							} else if (checkFiveOrThree) {
								totalAligned++;
							}
						}
					// count read 2
					} else {
						// count reads if Read2 or All Reads && 5' or 3'
						if (checkR2 && checkFiveOrThree) {
							totalAligned++;
						}
					}
				//If the read is mapped but not paired-end, default to read 1
				} else {
					// count reads if Read1 or All Reads && 5' or 3'
					if (checkR1 && checkFiveOrThree) {
						totalAligned++;
					}
				}
			} // ignore all unmapped reads
		}
		iter.close();
		factory.close();

		return (totalAligned);
	}

	/**
	 * Calculates the standardization ratio for a given BAM file
	 * @param BAM BAM file used to calculate ratio
	 * @param read Read Type (1 = Read1, 1 = Read2, 3 = All reads)
	 * @return The standardization ratio for a given BAM file
	 * @throws IOException Invalid file or parameters
	 */
	public static double calculateStandardizationRatio(File BAM, int read) throws IOException {
		// Get Genome Size
		double totalGenome = getGenomeSize(BAM);

		// Get total aligned
		double totalAligned = 0;
		double READ1 = 0;
		double READ2 = 0;
		double MID = 0;
		SamReader factory = SamReaderFactory.makeDefault().open(BAM);
		CloseableIterator<SAMRecord> iter = factory.iterator();
		while (iter.hasNext()) {
			SAMRecord sr = iter.next();
			if(!sr.getReadUnmappedFlag()) { //Test for mappability
				if(sr.getReadPairedFlag()) { //Test for paired-end status
					if(sr.getSecondOfPairFlag()) { READ2++; } //count read 2
					else if(sr.getFirstOfPairFlag()) { READ1++; } // count read 1
					if(sr.getProperPairFlag() && sr.getFirstOfPairFlag()) { MID++; } //count properly paired reads
				} else { //If the read is mapped but not paired-end, default to read 1
					READ1++;
				}
			}
		}
		iter.close();
		factory.close();

		//System.out.println("Genome Size: " + totalGenome + "\nTotal tags: " + totalAligned + "\nDetected Read 1: " + READ1 + "\nDetected Read 2: " + READ2 + "\nDetected Midpoints: " + MID);
		if(read == 0) { totalAligned = READ1; }
		else if(read == 1) { totalAligned = READ2; }
		else if(read == 2) { totalAligned = READ1 + READ2; }
		else if(read == 3) { totalAligned = MID; }

		if(totalAligned > 0) { return (totalGenome / totalAligned); }
		else { return 1; }
	}

	/**
	 * Calculates the standardization ratio for a given BAM file, ignoring blacklisted reads
	 * @param BAM BAM file used to calculate ratio 
	 * @param BLACKFile BED file containing blacklisted regions
	 * @param read Read Type (1 = Read1, 1 = Read2, 3 = All reads)
	 * @return The standardization ratio for a given BAM file
	 * @throws IOException Invalid file or parameters
	 */
	public static double calculateStandardizationRatio(File BAM, File BLACKFile, int read) throws IOException {
		//Blacklist filter in 500bp blocks on the genome with any blacklist region overlapping negating the entire block
		int windowSize = 500;
		
		List<String> chromName = new ArrayList<String>();
		List<Long> chromLength= new ArrayList<Long>();
		double totalAligned = 0;
		double totalGenome = 0;
		
		SamReaderFactory factory = SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS).validationStringency(ValidationStringency.SILENT);
		SamReader inputBAM = factory.open(BAM);
		AbstractBAMFileIndex inputBAI = (AbstractBAMFileIndex) inputBAM.indexing().getIndex();
		for (int z = 0; z < inputBAI.getNumberOfReferences(); z++) {
			SAMSequenceRecord rec = inputBAM.getFileHeader().getSequence(z);
			chromName.add(rec.getSequenceName());
			chromLength.add(Long.valueOf(rec.getSequenceLength()));
			totalGenome += rec.getSequenceLength();
		}
		inputBAM.close();
		inputBAI.close();

		//Load Blacklist into HashMap
		HashMap<String, ArrayList<BEDCoord>> BLACKLIST = loadBlacklist(BLACKFile);
		
		inputBAM = SamReaderFactory.makeDefault().open(BAM);
		for(int x = 0; x < chromName.size(); x++) {
			String seq = chromName.get(x);
			long chromSize = chromLength.get(x);
			//Blacklist filter each chromosome, set blacklisted windows to NaN
			float[] chrom = maskChrom(seq, chromSize, windowSize, BLACKLIST);
			//Iterate through chromosome loading tags into window bin 
			CloseableIterator<SAMRecord> iter = inputBAM.query(seq, 0, (int)chromSize, false);
			//SAMRecords are 1-based
			while (iter.hasNext()) {
				SAMRecord sr = iter.next();
				int FivePrime = sr.getUnclippedStart() - 1;
				if(sr.getReadNegativeStrandFlag()) { FivePrime = sr.getUnclippedEnd(); }
				int INDEX = (FivePrime / windowSize);
				if(!sr.getReadUnmappedFlag() && INDEX < chrom.length) { //Test for mappability
					if(sr.getReadPairedFlag()) { //Test for paired-end status
						if(sr.getSecondOfPairFlag() && read == 1) { chrom[INDEX]++; } //count read 2
						else if(sr.getFirstOfPairFlag() && (read == 0 || read == 2)) { chrom[INDEX]++; } // count read 1
						if(sr.getProperPairFlag() && sr.getFirstOfPairFlag() && read == 3) { chrom[INDEX]++; } //count properly paired reads for midpoint
					} else if(read == 0 || read == 2) { //If the read is mapped but not paired-end, default to read 1
						chrom[INDEX]++;
					}
				}
			}
			iter.close();
			for(int i = 0; i < chrom.length; i++) { if(!Float.isNaN(chrom[i])) { totalAligned += chrom[i]; } }
		}
		if(totalAligned > 0) { return (totalGenome / totalAligned); }
		else { return 1; }
	}

	/**
	 * Sets blacklisted positions to NaN
	 * @param chrom Chromosome to be processed
	 * @param chromSize Length of the chromosome
	 * @param windowSize The window/bin size 
	 * @param BLACKLIST BED file containing blacklisted regions
	 * @return An array representing the chromosome, with blacklisted regions being represented as NaN and valid regions being zero
	 */
	private static float[] maskChrom(String chrom, long chromSize, int windowSize, HashMap<String, ArrayList<BEDCoord>> BLACKLIST) {
		float[] chromArray = new float[(int) (chromSize / windowSize) + 1];
		if(BLACKLIST.containsKey(chrom)) {
			ArrayList<BEDCoord> blacklist = BLACKLIST.get(chrom);
			for(int x = 0; x < blacklist.size(); x++) {
				long START = blacklist.get(x).getStart();
				long STOP = blacklist.get(x).getStop();
				while(START < STOP) {
					int index = ((int)START / windowSize);
					if(index < chromArray.length) { chromArray[index] = Float.NaN; }
					START += windowSize;
				}
			}
		}
		return chromArray;
	}
	
	/**
	 * Loads the blacklist BED file into a Hashmap<String, ArrayList<BEDCoord>>
	 * @param BLACKFile BED file to make the blacklist Hashmap with
	 * @return A Hashmap<String, ArrayList<BEDCoord>> with the name of chromosomes as keys and ArrayLists of blacklisted coordinates as values
	 * @throws FileNotFoundException Script could not find valid input file
	 */
	private static HashMap<String, ArrayList<BEDCoord>> loadBlacklist(File BLACKFile) throws FileNotFoundException {
		HashMap<String, ArrayList<BEDCoord>>  BLACKLIST = new HashMap<String, ArrayList<BEDCoord>>();
	    Scanner scan = new Scanner(BLACKFile);
		while (scan.hasNextLine()) {
			String[] temp = scan.nextLine().split("\t");
			if(temp.length > 2) {
				if(!temp[0].contains("track") && !temp[0].contains("#")) {
					if(Integer.parseInt(temp[1]) >= 0) {
						int start = Integer.parseInt(temp[1]);
						int stop = Integer.parseInt(temp[2]);
						BEDCoord coord = new BEDCoord(temp[0], start, stop , ".");
						if(BLACKLIST.containsKey(temp[0])) { BLACKLIST.get(temp[0]).add(coord);	}
						else {
							ArrayList<BEDCoord> newchrom = new ArrayList<BEDCoord>();
							newchrom.add(coord);
							BLACKLIST.put(temp[0], newchrom);
						}			
					} else {
						System.err.println("Invalid Coordinate in File!!!\n" + Arrays.toString(temp));
					}
				}
			}
	    }
		scan.close();
		return BLACKLIST;
	}
}
