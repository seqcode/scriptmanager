package scriptmanager.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for sequence manipulation.
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Manipulation.FilterforPIPseq
 * @see scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED
 * @see scriptmanager.scripts.Sequence_Analysis.FASTAExtract
 */
public class FASTAUtilities {

	/**
	 * Creates a new FASTAUtilities object (unnecessary because only contains static methods)
	 */
	public FASTAUtilities(){}
	
	/**
	 * Return the reverse complement DNA sequence of the input string (skipping over non-[ATCGN] char) and where rev complement of N is N.
	 * <br><br>
	 * Consider switching to <a href=https://samtools.github.io/htsjdk/javadoc/htsjdk/htsjdk/samtools/util/SequenceUtil>HTSJDK implementation</a> after considering edge case handling
	 * 
	 * @param SEQ the DNA sequence string to get the rev complement of
	 * @return the rev complement of SEQ
	 */
	public static String RevComplement(String SEQ) {
		SEQ = SEQ.toUpperCase();
		String RC = "";
		for (int x = 0; x < SEQ.length(); x++) {
			if (SEQ.charAt(x) == 'A') {
				RC = 'T' + RC;
			} else if (SEQ.charAt(x) == 'T') {
				RC = 'A' + RC;
			} else if (SEQ.charAt(x) == 'G') {
				RC = 'C' + RC;
			} else if (SEQ.charAt(x) == 'C') {
				RC = 'G' + RC;
			} else {
				RC = 'N' + RC;
			}
		}
		return RC;
	}

	/**
	 * Determine if String contains invalid DNA sequence chars other than [ATGC] (as of yet unused).
	 * 
	 * @param seq the DNA sequence to check
	 * @return true if seq only contains [ATCG], false if seq contains any other characters
	 */
	public static boolean parseStringforInvalideNuc(String seq) {
		seq = seq.toUpperCase();
		char[] check = seq.toCharArray();
		for (int x = 0; x < check.length; x++) {
			if (check[x] != 'A' && check[x] != 'T' && check[x] != 'G' && check[x] != 'C') {
				return false;
			}
		}
		return true;
	}

	/**
	 * Perform regex validation ("[ATGCRYSWKMBDHVN]+") of motif string,
	 * capitalization INsensitive, to ensure that only IUPAC DNA nucleotide
	 * characters are used in the motif search.
	 * 
	 * @param motif some IUPAC motif string to check
	 * @return false if non-IUPAC chars are in the motif String, else true
	 */
	public static boolean isValidIUPACString(String motif) {
		// check filter string is valid ATGCRYSWKMBDHVN
		Pattern seqPat = Pattern.compile("[ATGCRYSWKMBDHVN]+");
		Matcher m = seqPat.matcher(motif.toUpperCase());
		if (m.matches()) {
			return true;
		}
		return false;
	}
}
