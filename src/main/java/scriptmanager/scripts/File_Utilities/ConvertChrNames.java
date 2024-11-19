package scriptmanager.scripts.File_Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.String;
import java.util.HashMap;
import scriptmanager.util.GZipUtilities;

/**
 * Convert chromosome names (roman &rarr; arabic and arabic &rarr; roman
 * numerals). Primarily a utility for renaming sacCer3 chromosomes in coordinate
 * files between the two alternative chromosome naming systems.
 * 
 * @author Olivia Lang
 * @see scriptmanager.cli.File_Utilities.ConvertBEDChrNamesCLI
 * @see scriptmanager.cli.File_Utilities.ConvertGFFChrNamesCLI
 * @see scriptmanager.window_interface.File_Utilities.ConvertBEDChrNamesWindow
 * @see scriptmanager.window_interface.File_Utilities.ConvertGFFChrNamesWindow
 */
public class ConvertChrNames {

	/**
	 * Helper method to generate the roman --&gt; arabic numeral chromosome name
	 * map.
	 * 
	 * @param useChrmt if true, include "chrM" --&gt; "chrmt" map, else include
	 *                 "chrmt" --&gt; "chrM" map
	 * @return the string for the arabic numeral chrname mapped to the analagous
	 *         roman numeral chrname with mitochondrial chr map
	 */
	public static HashMap<String, String> getR2A(boolean useChrmt) {
		HashMap<String, String> R2A = new HashMap<String, String>();
		R2A.put("chrXVI", "chr16");
		R2A.put("chrXV", "chr15");
		R2A.put("chrXIV", "chr14");
		R2A.put("chrXIII", "chr13");
		R2A.put("chrXII", "chr12");
		R2A.put("chrXI", "chr11");
		R2A.put("chrX", "chr10");
		R2A.put("chrIX", "chr9");
		R2A.put("chrVIII", "chr8");
		R2A.put("chrVII", "chr7");
		R2A.put("chrVI", "chr6");
		R2A.put("chrV", "chr5");
		R2A.put("chrIV", "chr4");
		R2A.put("chrIII", "chr3");
		R2A.put("chrII", "chr2");
		R2A.put("chrI", "chr1");
		if( useChrmt) {
			R2A.put("chrM", "chrmt");
		}else {
			R2A.put("chrmt", "chrM");
		}
		return (R2A);
	}

	/**
	 * Helper method to generate the arabic --&gt; roman numeral chromosome name
	 * map.
	 * 
	 * @param useChrmt if true, include "chrM" --&gt; "chrmt" map, else include
	 *                 "chrmt" --&gt; "chrM" map
	 * @return the string for the arabic numeral chrname mapped to the analagous
	 *         roman numeral chrname with mitochondrial chr map
	 */
	public static HashMap<String, String> getA2R(boolean useChrmt) {
		HashMap<String,String> A2R = new HashMap<String, String>();
		A2R.put("chr16", "chrXVI");
		A2R.put("chr15", "chrXV");
		A2R.put("chr14", "chrXIV");
		A2R.put("chr13", "chrXIII");
		A2R.put("chr12", "chrXII");
		A2R.put("chr11", "chrXI");
		A2R.put("chr10", "chrX");
		A2R.put("chr9", "chrIX");
		A2R.put("chr8", "chrVIII");
		A2R.put("chr7", "chrVII");
		A2R.put("chr6", "chrVI");
		A2R.put("chr5", "chrV");
		A2R.put("chr4", "chrIV");
		A2R.put("chr3", "chrIII");
		A2R.put("chr2", "chrII");
		A2R.put("chr1", "chrI");
		if( useChrmt) {
			A2R.put("chrM", "chrmt");
		}else {
			A2R.put("chrmt", "chrM");
		}
		return (A2R);
	}

	/**
	 * Wrapper for convertCoordinateFile using Roman --&gt; Arabic chromosome name
	 * map.
	 * 
	 * @param input        the input BED/GFF file to convert
	 * @param out_filepath this is the filepath to write the converted coordinate
	 *                     file to
	 * @param useChrmt     Used to generate the chromosome map for the conversion
	 *                     (see getR2A()).
	 * @param gzOutput     If this is true, the output file will be gzipped.
	 * @throws IOException Invalid file or parameters
	 */
	public static void convert_RomantoArabic(File input, File out_filepath, boolean useChrmt, boolean gzOutput) throws IOException {
		convertCoordinateFile(input, out_filepath, getR2A(useChrmt), gzOutput);
	}

	/**
	 * Wrapper for convertCoordinateFile using Arabic --&gt; Roman chromosome name
	 * map.
	 * 
	 * @param input        the input BED/GFF file to convert
	 * @param out_filepath this is the filepath to write the converted coordinate
	 *                     file to
	 * @param useChrmt     Used to generate the chromosome map for the conversion
	 *                     (see getA2R()).
	 * @param gzOutput     If this is true, the output file will be gzipped.
	 * @throws IOException Invalid file or parameters
	 */
	public static void convert_ArabictoRoman(File input, File out_filepath, boolean useChrmt, boolean gzOutput) throws IOException {
		convertCoordinateFile(input, out_filepath, getA2R(useChrmt), gzOutput);
	}
	
	/**
	 * Convert method for tab-delimited coordinate files. Note that both BED and GFF
	 * implementations are the same. If chr name not in provided HashMap (chrMap),
	 * then the coordinate is written to match the original.
	 * 
	 * @param input        the input coordinate BED/GFF file (chr info in first
	 *                     column of tab-delimited file)
	 * @param out_filepath the new output coordinate file with the exchanged chr
	 *                     names
	 * @param chrMap       the HashMap for which conversion direction to implement
	 * @param gzOutput     If this is true, the output file will be gzipped.
	 * @throws IOException Invalid file or parameters
	 */
	public static void convertCoordinateFile(File input, File out_filepath, HashMap<String, String> chrMap, boolean gzOutput) throws IOException {
		// BED Coords:
		// chr1 87116 87156 NS500168:175:H5LJJBGXY:1:11107:3505:14769 40 +
		// chr1 87124 87164 NS500168:175:H5LJJBGXY:1:23303:23713:17862 40 -
		// GFF Coords:
		// chr22 TeleGene enhancer 10000000 10001000 500 + . touch1
		// chr12 bed2gff chr12_384641_384659_+ 384642 384659 42.6 + .
		// chr12_384641_384659_+;
		
		// Initialize output writer
		PrintStream OUT = System.out;
		if (out_filepath != null) {
			OUT = GZipUtilities.makePrintStream(out_filepath, gzOutput);
		}
		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br;
		br = GZipUtilities.makeReader(input);
		// Initialize line variable to loop through
		String line = br.readLine();
		while (line != null) {
			if (line.startsWith("#")) {
				OUT.println(line);
			} else {
				// Split into tokens by tab delimiter
				String[] temp = line.split("\t");
				// Update first col with mapped key
				if (chrMap.containsKey(temp[0])) { temp[0] = chrMap.get(temp[0]); }
				// Write new row
				OUT.println(String.join("\t", temp));
			}
			line = br.readLine();
		}
		// Close files
		br.close();
		OUT.close();
	}
}
