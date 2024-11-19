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
import java.util.ArrayList;
import java.util.Date;

/**
 * Convert BAM file to scIDX file
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.BAM_Format_Converter.BAMtoscIDXCLI
 * @see scriptmanager.window_interface.BAM_Format_Converter.BAMtoscIDXOutput
 * @see scriptmanager.window_interface.BAM_Format_Converter.BAMtoscIDXWindow
 */
public class BAMtoscIDX {
	private File BAM = null;
	private File OUTFILE = null;
	private PrintStream OUT = null;
	private boolean OUTPUT_GZIP;
	private PrintStream PS = null;

	private int STRAND = 0;
	private String READ = "READ1";
	private static int PAIR = 1;
	private int SHIFT = 0;
	private static int MIN_INSERT = -9999;
	private static int MAX_INSERT = -9999;

	private SamReader inputSam = null;

	private ArrayList<Integer> BP;
	private ArrayList<Integer> F_OCC;
	private ArrayList<Integer> R_OCC;
	private ArrayList<Integer> M_OCC;

	private int CHROMSTOP = -999;

	/**
	 * Creates a new instance of a BAMtoscIDX script with a single BAM file
	 * 
	 * @param b           BAM file
	 * @param o           output scIDX file
	 * @param s           Specifies which reads to output
	 * @param pair_status Specifies if proper pairs are required (0 = not required,
	 *                    !0 = required)
	 * @param min_size    minimum acceptable insert size
	 * @param max_size    maximum acceptable insert size
	 * @param shift       shift tags in bp
	 * @param ps          PrintStream to output results
	 * @param gzOutput    whether or not to gzip output
	 */
	public BAMtoscIDX(File b, File o, int s, int pair_status, int min_size, int max_size, int shift, PrintStream ps, boolean gzOutput) {
		BAM = b;
		OUTFILE = o;
		PS = ps;
		STRAND = s;
		PAIR = pair_status;
		MIN_INSERT = min_size;
		MAX_INSERT = max_size;
		SHIFT = shift;
		if (STRAND == 0) {
			READ = "READ1";
		} else if (STRAND == 1) {
			READ = "READ2";
		} else if (STRAND == 2) {
			READ = "COMBINED";
		} else if (STRAND == 3) {
			READ = "MIDPOINT";
		}
		OUTPUT_GZIP = gzOutput;
	}

	/**
	 * Runs the {@link BAMtoscIDX#processREADS()} and {@link BAMtoscIDX#processMIDPOINT()} method and checks that inputs are valid
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
			printPS("-----------------------------------------\nBAM to scIDX Parameters:");
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

			printPS("Tag shift: " + SHIFT);

			if (OUTPUT_GZIP){
				printPS("Output GZip: yes");
			} else {
				printPS("Output GZip: no");
			}

			// Print Header
			printOUT("#" + getTimeStamp() + ";" + BAM.getName() + ";" + READ);
			// Here add a line as CLI command "receipt" to show command to regenerate this
			// file
			if (STRAND <= 2) {
				printOUT("chrom\tindex\tforward\treverse\tvalue");
			} else {
				printOUT("chrom\tindex\tmidpoint\tnull\tvalue");
			}

			// Begin processing reads in BAM file
			if (STRAND <= 2) {
				processREADS();
			} else {
				processMIDPOINT();
			}
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
	 * Adds valid reads to scIDX output file
	 * @param sr SAMRecord to output
	 */
	public void addTag(SAMRecord sr) {
		// Get the start of the record
		int recordStart = sr.getUnclippedStart() + SHIFT;// .getAlignmentStart();
		// Accounts for reverse tag reporting 3' end of tag and converting BED to
		// IDX/GFF format
		if (sr.getReadNegativeStrandFlag()) {
			recordStart = sr.getUnclippedEnd() - SHIFT;
		} // .getAlignmentEnd(); }

		// Make sure we only add tags that have valid starts
		if (recordStart > 0 && recordStart <= CHROMSTOP) {
			if (BP.contains(Integer.valueOf(recordStart))) {
				int index = BP.indexOf(Integer.valueOf(recordStart));
				if (sr.getReadNegativeStrandFlag()) {
					R_OCC.set(index, Integer.valueOf(R_OCC.get(index).intValue() + 1));
				} else {
					F_OCC.set(index, Integer.valueOf(F_OCC.get(index).intValue() + 1));
				}
			} else {
				// Sometimes the start coordinate will be out of order due to (-) strand
				// correction
				// Need to efficiently identify where to place it relative to the other bps
				int index = BP.size() - 1;
				if (index >= 0) {
					while (index >= 0 && recordStart < BP.get(index).intValue()) {
						index--;
					}
				}
				if (index < BP.size() - 1) {
					BP.add(index + 1, Integer.valueOf(recordStart));
					if (sr.getReadNegativeStrandFlag()) {
						R_OCC.add(index + 1, Integer.valueOf(1));
						F_OCC.add(index + 1, Integer.valueOf(0));
					} else {
						F_OCC.add(index + 1, Integer.valueOf(1));
						R_OCC.add(index + 1, Integer.valueOf(0));
					}
				} else {
					BP.add(Integer.valueOf(recordStart));
					if (sr.getReadNegativeStrandFlag()) {
						R_OCC.add(Integer.valueOf(1));
						F_OCC.add(Integer.valueOf(0));
					} else {
						F_OCC.add(Integer.valueOf(1));
						R_OCC.add(Integer.valueOf(0));
					}
				}
			}
		}
	}

	/**
	 * Marks the midpoint of a read
	 * @param sr The read to be marked
	 */
	public void addMidTag(SAMRecord sr) {
		// int recordMid = sr.getUnclippedStart() + (sr.getInferredInsertSize() / 2);
		// if(sr.getReadNegativeStrandFlag()) { recordMid = sr.getUnclippedEnd() +
		// (sr.getInferredInsertSize() / 2); }

		int recordStart = sr.getUnclippedStart() - 1;
		int recordStop = sr.getMateAlignmentStart() + sr.getReadLength() - 1;
		if (sr.getMateAlignmentStart() - 1 < recordStart) {
			recordStart = sr.getMateAlignmentStart() - 1;
			recordStop = sr.getUnclippedEnd();
		}
		// calculate midpoint
		int recordMid = (recordStart + recordStop) / 2;
		// strand-aware tag shift
		recordMid += (sr.getReadNegativeStrandFlag() ? - SHIFT : SHIFT);

		// Make sure we only add tags that have valid midpoints
		if (recordMid > 0 && recordMid <= CHROMSTOP) {
			if (BP.contains(Integer.valueOf(recordMid))) {
				int index = BP.indexOf(Integer.valueOf(recordMid));
				M_OCC.set(index, Integer.valueOf(M_OCC.get(index).intValue() + 1));
			} else {
				// Sometimes the start coordinate will be out of order due to (-) strand
				// correction
				// Need to efficiently identify where to place it relative to the other bps
				int index = BP.size() - 1;
				if (index >= 0) {
					while (index >= 0 && recordMid < BP.get(index).intValue()) {
						index--;
					}
				}
				if (index < BP.size() - 1) {
					BP.add(index + 1, Integer.valueOf(recordMid));
					M_OCC.add(index + 1, Integer.valueOf(1));
				} else {
					BP.add(Integer.valueOf(recordMid));
					M_OCC.add(Integer.valueOf(1));
				}
			}
		}

	}

	/**
	 * Removes up to 9000 bp's from the start of a chromosome string, outputting them to a file, to save memory
	 * @param chrom Chromosome to be reduced
	 */
	public void dumpExcess(String chrom) {
		int trim = 9000;
		while (trim > 0) {
			int sum = F_OCC.get(0).intValue() + R_OCC.get(0).intValue();
			printOUT(chrom + "\t" + BP.get(0).intValue() + "\t" + F_OCC.get(0).intValue() + "\t"
					+ R_OCC.get(0).intValue() + "\t" + sum);
			BP.remove(0);
			F_OCC.remove(0);
			R_OCC.remove(0);
			trim--;
		}
	}

	/**
	 * Removes at least 600 bp's from a chromsome string, outputting them to a file, to save memory
	 * @param chrom Chromosome to be reduced
	 */
	public void dumpMidExcess(String chrom) {
		int trim = (MAX_INSERT * 10) - (MAX_INSERT * 2);
		if (MAX_INSERT * 10 < 1000) {
			trim = 600;
		}
		while (trim > 0) {
			printOUT(chrom + "\t" + BP.get(0).intValue() + "\t" + M_OCC.get(0).intValue() + "\t0\t"
					+ M_OCC.get(0).intValue());
			// OUT.println(chrom + "\t" + BP.get(0).intValue() + "\t" +
			// M_OCC.get(0).intValue());

			BP.remove(0);
			M_OCC.remove(0);
			trim--;
		}
	}

	/**
	 * Makes sure reads are valid before calling {@link BAMtoscIDX#addTag(SAMRecord sr)} 
	 */
	public void processREADS() {
		inputSam = SamReaderFactory.makeDefault().open(BAM);// factory.open(BAM);
		AbstractBAMFileIndex bai = (AbstractBAMFileIndex) inputSam.indexing().getIndex();

		for (int numchrom = 0; numchrom < bai.getNumberOfReferences(); numchrom++) {
			SAMSequenceRecord seq = inputSam.getFileHeader().getSequence(numchrom);
// 			System.out.println("Processing: " + seq.getSequenceName());
			printPS("Processing: " + seq.getSequenceName());

			CHROMSTOP = seq.getSequenceLength();
			BP = new ArrayList<Integer>();
			F_OCC = new ArrayList<Integer>();
			R_OCC = new ArrayList<Integer>();

			CloseableIterator<SAMRecord> iter = inputSam.query(seq.getSequenceName(), 0, seq.getSequenceLength(),
					false);
			while (iter.hasNext()) {
				// Create the record object
				SAMRecord sr = iter.next();

				if (STRAND == 2) { // Output combined READ 1 && READ 2
					if (PAIR == 0) {
						addTag(sr);
					} // Output read if proper mate-pairing is NOT required
					else if (sr.getReadPairedFlag()) { // otherwise, check for PE flag
						if (sr.getProperPairFlag()) {
							addTag(sr);
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
								addTag(sr);
							} // add tag if both flags true
						}
					} else if (PAIR == 0) {
						addTag(sr);
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
								addTag(sr);
							} // add tag if both flags true
						}
					}
				}

				// Dump ArrayLists to OUT if they get too big in order to save RAM and therefore
				// time
				if (BP.size() > 10000) {
					dumpExcess(seq.getSequenceName());
				}

			}
			iter.close();
			for (int z = 0; z < BP.size(); z++) {
				int sum = F_OCC.get(z).intValue() + R_OCC.get(z).intValue();
				printOUT(seq.getSequenceName() + "\t" + BP.get(z).intValue() + "\t" + F_OCC.get(z).intValue() + "\t"
						+ R_OCC.get(z).intValue() + "\t" + sum);
			}
		}
		bai.close();
	}

	/**
	 * Processes reads if 'Midpoint Record' was selected and validates them before calling {@link BAMtoscIDX#addTag(SAMRecord sr)}
	 */
	public void processMIDPOINT() {
		inputSam = SamReaderFactory.makeDefault().open(BAM);// factory.open(BAM);
		AbstractBAMFileIndex bai = (AbstractBAMFileIndex) inputSam.indexing().getIndex();

		for (int numchrom = 0; numchrom < bai.getNumberOfReferences(); numchrom++) {
			SAMSequenceRecord seq = inputSam.getFileHeader().getSequence(numchrom);
// 			System.out.println("Processing: " + seq.getSequenceName());
			printPS("Processing: " + seq.getSequenceName());

			BP = new ArrayList<Integer>();
			M_OCC = new ArrayList<Integer>();
			CHROMSTOP = seq.getSequenceLength();

			CloseableIterator<SAMRecord> iter = inputSam.query(seq.getSequenceName(), 0, seq.getSequenceLength(),
					false);
			while (iter.hasNext()) {
				// Create the record object
				SAMRecord sr = iter.next();

				// Must be PAIRED-END mapped, mate must be mapped, must be read1
				if (sr.getReadPairedFlag()) {
					if (sr.getProperPairFlag() && sr.getFirstOfPairFlag()) {
						boolean flag1 = (Math.abs(sr.getInferredInsertSize()) >= MIN_INSERT && MIN_INSERT != -9999)
								|| MIN_INSERT == -9999; // check if insert size >= min if in use
						boolean flag2 = (Math.abs(sr.getInferredInsertSize()) <= MAX_INSERT && MAX_INSERT != -9999)
								|| MAX_INSERT == -9999; // check if insert size <= max if in use
						if (flag1 && flag2) {
							addMidTag(sr);
						}
					}
				}

				// Dump ArrayLists to OUT if they get too big in order to save RAM and therefore
				// time
				if ((BP.size() > (MAX_INSERT * 10) && (MAX_INSERT * 10) > 1000)
						|| (BP.size() > 1000 && (MAX_INSERT * 10) < 1000)) {
					dumpMidExcess(seq.getSequenceName());
				}

			}
			iter.close();
			for (int z = 0; z < BP.size(); z++) {
				printOUT(seq.getSequenceName() + "\t" + BP.get(z).intValue() + "\t" + M_OCC.get(z).intValue() + "\t0\t"
						+ M_OCC.get(z).intValue());
				// OUT.println(seq.getSequenceName() + "\t" + BP.get(z).intValue() + "\t" +
				// M_OCC.get(z).intValue());
			}
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