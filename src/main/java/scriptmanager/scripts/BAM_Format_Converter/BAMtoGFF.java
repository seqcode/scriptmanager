package scriptmanager.scripts.BAM_Format_Converter;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.util.CloseableIterator;
import scriptmanager.util.GZipUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Convert BAM file to GFF file
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.BAM_Format_Converter.BAMtoGFFCLI
 * @see scriptmanager.window_interface.BAM_Format_Converter.BAMtoGFFOutput
 * @see scriptmanager.window_interface.BAM_Format_Converter.BAMtoGFFWindow
 */
public class BAMtoGFF {
	private File BAM = null;
	private File OUTFILE = null;
	private boolean OUTPUT_GZIP;
	private PrintStream OUT = null;
	private PrintStream PS = null;

	private int STRAND = 0;
	private String READ = "READ1";
	private static int PAIR = 1;
	private static int MIN_INSERT = -9999;
	private static int MAX_INSERT = -9999;

	private SamReader inputSam = null;

	private int CHROMSTOP = -999;

	/**
	 * Creates a new instance of a BAMtoGFF script with a single BAM file
	 * 
	 * @param b           BAM file
	 * @param o           output GFF file
	 * @param s           Specifies which reads to output
	 * @param pair_status Specifies if proper pairs are required (0 = not required,
	 *                    !0 = required)
	 * @param min_size    minimum acceptable insert size
	 * @param max_size    maximum acceptable insert size
	 * @param ps          PrintStream to output results
	 * @param gzOutput    whether or not to gzip output
	 */
	public BAMtoGFF(File b, File o, int s, int pair_status, int min_size, int max_size, PrintStream ps, boolean gzOutput) {
		BAM = b;
		OUTFILE = o;
		PS = ps;
		STRAND = s;
		PAIR = pair_status;
		MIN_INSERT = min_size;
		MAX_INSERT = max_size;
		if (STRAND == 0) {
			READ = "READ1";
		} else if (STRAND == 1) {
			READ = "READ2";
		} else if (STRAND == 2) {
			READ = "COMBINED";
		} else if (STRAND == 3) {
			READ = "MIDPOINT";
		} else if (STRAND == 4) {
			READ = "FRAGMENT";
		}
		OUTPUT_GZIP = gzOutput;
	}

	/**
	 * Checks for valid input and runs the {@link BAMtoGFF#processREADS()} method
	 * @throws IOException Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the same time
	 */
	public void run() throws IOException, InterruptedException {
		// Set-up Output PrintStream
		if (OUTFILE != null) {
			try {
				OUT = GZipUtilities.makePrintStream(OUTFILE, OUTPUT_GZIP);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			printPS(OUTFILE.getCanonicalPath());
		} else {
			printPS("STDOUT");
		}
		printPS(getTimeStamp());

		// Check to Make Sure BAI-index file exists
		File f = new File(BAM.getAbsolutePath() + ".bai");
		if (f.exists() && !f.isDirectory()) {
			// Print Input Params
			printPS("-----------------------------------------\nBAM to GFF Parameters:");
			printPS("BAM file: " + BAM);
			if (OUTFILE != null) {
				printPS("Output: " + OUTFILE.getName());
			} else {
				printPS("Output: STDOUT");
			}

			printPS("Output Read: " + READ);
			if (PAIR == 0) {
				printPS("Require proper Mate-pair: no");
			} else {
				printPS("Require proper Mate-pair: yes");
			}

			if (MIN_INSERT == -9999) {
				printPS("Minimum insert size required to output: NaN");
			} else {
				printPS("Minimum insert size required to output: " + MIN_INSERT);
			}

			if (MAX_INSERT == -9999) {
				printPS("Maximum insert size required to output: NaN");
			} else {
				printPS("Maximum insert size required to output: " + MAX_INSERT);
			}
			
			if (OUTPUT_GZIP){
				printPS("Output GZip: yes");
			} else {
				printPS("Output GZip: no");
			}

			// Build&Print Header
			String header = "#" + getTimeStamp() + ";" + BAM.getName() + ";" + READ;
			if (PAIR != 0) {
				header += ";PE_Required";
			}
			if (MIN_INSERT != -9999) {
				header += ";Min_Insert-" + MIN_INSERT;
			}
			if (MAX_INSERT != -9999) {
				header += ";Max_Insert-" + MAX_INSERT;
			}
			printOUT(header);

			// Begin processing reads in BAM file
			processREADS();
		} else {
			printPS("BAI Index File does not exist for: " + BAM.getName());
			printOUT("BAI Index File does not exist for: " + BAM.getName());
		}
		if (OUTFILE != null) {
			OUT.close();
		}

		printPS(getTimeStamp());
	}

	/**
	 * Writes reads to GFF file and output stream
	 * @param read Read to be written
	 */
	public void outputRead(SAMRecord read) {
		// chr22 TeleGene enhancer 10000000 10001000 500 + . touch1
		// chr22 TeleGene promoter 10010000 10010100 900 + . touch1
		int recordStart = read.getUnclippedStart();
		int recordStop = read.getUnclippedEnd();
		String chrom = read.getReferenceName();
		String dir = "+";
		if (read.getReadNegativeStrandFlag())
			dir = "-";

		if (STRAND <= 2) {
			if (recordStart > 0 && recordStop <= CHROMSTOP) { // Make sure we only output real reads
				printOUT(chrom + "\tbam2gff\t" + read.getReadName() + "\t" + recordStart + "\t" + recordStop + "\t"
						+ read.getReadLength() + "\t" + dir + "\t.\t" + read.getReadName());
			}
		} else if (STRAND == 3) {
			recordStop = read.getMateAlignmentStart() + read.getReadLength() - 1;
			if (read.getMateAlignmentStart() - 1 < recordStart) {
				recordStart = read.getMateAlignmentStart() - 1;
				recordStop = read.getUnclippedEnd();
			}
			int midStart = (recordStart + recordStop) / 2;
			int midStop = midStart + 1;

			if (midStart > 0 && midStop <= CHROMSTOP) { // Make sure we only output real reads
				int size = Math.abs(read.getInferredInsertSize());
				printOUT(chrom + "\tbam2gff\t" + read.getReadName() + "\t" + midStart + "\t" + midStop + "\t" + size
						+ "\t" + dir + "\t.\t" + read.getReadName());
			}
		} else if (STRAND == 4) {
			if (read.getReadNegativeStrandFlag()) {
				recordStart = read.getMateAlignmentStart();
				recordStop = read.getAlignmentEnd();
			} else {
				recordStop = read.getMateAlignmentStart() + read.getReadLength();
			}
			if (recordStart > 0 && recordStop <= CHROMSTOP) { // Make sure we only output real reads
				int size = Math.abs(read.getInferredInsertSize());
				printOUT(chrom + "\tbam2gff\t" + read.getReadName() + "\t" + recordStart + "\t" + recordStop + "\t"
						+ size + "\t" + dir + "\t.\t" + read.getReadName());
			}
		}
	}

	/**
	 * Checks if individual reads are valid, sending them to {@link BAMtoGFF#outputRead(SAMRecord)}
	 */
	public void processREADS() {
		inputSam = SamReaderFactory.makeDefault().open(BAM);// factory.open(BAM);
		AbstractBAMFileIndex bai = (AbstractBAMFileIndex) inputSam.indexing().getIndex();

		for (int numchrom = 0; numchrom < bai.getNumberOfReferences(); numchrom++) {
			SAMSequenceRecord seq = inputSam.getFileHeader().getSequence(numchrom);
// 			System.out.println("Processing: " + seq.getSequenceName());
			printPS("Processing: " + seq.getSequenceName());

			CHROMSTOP = seq.getSequenceLength();

			CloseableIterator<SAMRecord> iter = inputSam.query(seq.getSequenceName(), 0, seq.getSequenceLength(),
					false);
			while (iter.hasNext()) {
				// Create the record object
				SAMRecord sr = iter.next();

				if (STRAND == 3 || STRAND == 4) { // Output Midpoint or fragment
					// Must be PAIRED-END mapped, mate must be mapped, must be read1
					if (sr.getReadPairedFlag()) {
						if (sr.getProperPairFlag() && sr.getFirstOfPairFlag()) {
							boolean flag1 = (Math.abs(sr.getInferredInsertSize()) >= MIN_INSERT && MIN_INSERT != -9999)
									|| MIN_INSERT == -9999; // check if insert size >= min if in use
							boolean flag2 = (Math.abs(sr.getInferredInsertSize()) <= MAX_INSERT && MAX_INSERT != -9999)
									|| MAX_INSERT == -9999; // check if insert size <= max if in use
							if (flag1 && flag2) {
								outputRead(sr);
							}
						}
					}
				} else if (STRAND == 2) { // Output combined READ 1 && READ 2
					if (PAIR == 0) {
						outputRead(sr);
					} // Output read if proper mate-pairing is NOT required
					else if (sr.getReadPairedFlag()) { // otherwise, check for PE flag
						if (sr.getProperPairFlag()) {
							outputRead(sr);
						} // output read if proper mate-pair is detected
					}
				} else if (STRAND == 0) { // Output READ 1
					if (sr.getReadPairedFlag()) { // Check if PAIRED-END
						if (((sr.getProperPairFlag() && PAIR == 1) || PAIR == 0) && sr.getFirstOfPairFlag()) { // mate
																												// must
																												// be
																												// mapped
																												// if
																												// PAIR
																												// requirement,
																												// must
																												// be
																												// read1
							boolean flag1 = (Math.abs(sr.getInferredInsertSize()) >= MIN_INSERT && MIN_INSERT != -9999)
									|| MIN_INSERT == -9999; // check if insert size >= min if in use
							boolean flag2 = (Math.abs(sr.getInferredInsertSize()) <= MAX_INSERT && MAX_INSERT != -9999)
									|| MAX_INSERT == -9999; // check if insert size <= max if in use
							if (flag1 && flag2) {
								outputRead(sr);
							} // add tag if both flags true
						}
					} else if (PAIR == 0) {
						outputRead(sr);
					} // Output if not paired-end, by default it is Read1, and mate-pair not required
				} else if (STRAND == 1) { // Output READ 2
					if (sr.getReadPairedFlag()) { //// Must be PAIRED-END for valid Read 2
						if (((sr.getProperPairFlag() && PAIR == 1) || PAIR == 0) && !sr.getFirstOfPairFlag()) { // mate
																												// must
																												// be
																												// mapped
																												// if
																												// PAIR
																												// requirement,
																												// must
																												// be
																												// read2
							boolean flag1 = (Math.abs(sr.getInferredInsertSize()) >= MIN_INSERT && MIN_INSERT != -9999)
									|| MIN_INSERT == -9999; // check if insert size >= min if in use
							boolean flag2 = (Math.abs(sr.getInferredInsertSize()) <= MAX_INSERT && MAX_INSERT != -9999)
									|| MAX_INSERT == -9999; // check if insert size <= max if in use
							if (flag1 && flag2) {
								outputRead(sr);
							} // add tag if both flags true
						}
					}
				}
			}
			iter.close();
		}
		bai.close();
	}

	private void printPS(String line) {
		if (PS != null) {
			PS.println(line);
		}
		System.err.println(line);
	}

	private void printOUT(String line) {
		if (OUT != null) {
			OUT.println(line);
		} else {
			System.out.println(line);
		}
	}

	private static String getTimeStamp() {
		Date date = new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}
}