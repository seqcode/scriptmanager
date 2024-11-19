package scriptmanager.cli.BAM_Manipulation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import htsjdk.samtools.reference.FastaSequenceIndexCreator;

import java.lang.NullPointerException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.BAM_Manipulation.FilterforPIPseq;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.BAM_Manipulation.FilterforPIPseq}
 * 
 * @author Olivia Lang
 */
@Command(name = "filter-pip-seq", mixinStandardHelpOptions = true, description = ToolDescriptions.filter_pip_seq_description
		+ "\n"
		+ "Note this program does not index the resulting BAM file and user must use appropriate samtools command to generate BAI.", version = "ScriptManager "
				+ ToolDescriptions.VERSION, sortOptions = false, exitCodeOnInvalidInput = 1, exitCodeOnExecutionException = 1)
public class FilterforPIPseqCLI implements Callable<Integer> {

	@Parameters(index = "0", description = "The reference genome FASTA file.")
	private File genomeFASTA;
	@Parameters(index = "1", description = "The BAM file from which we filter.")
	private File bamFile;

	@Option(names = { "-o", "--output" }, description = "specify output file (default=<bamFileNoExt>_PSfilter.bam)")
	private File output = null;
	@Option(names = { "-f",
			"--filter" }, description = "filter by upstream sequence, works only for single-nucleotide A,T,C, or G. (default seq ='T')")
	private String filterString = "T";

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">FilterforPIPseqCLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		FilterforPIPseq script_obj = new FilterforPIPseq(bamFile, genomeFASTA, output, filterString, null);
		script_obj.run();

		System.err.println("BAM Generated.");
		return (0);
	}

	/**
	 * Validate the input values before executing the script
	 * 
	 * @return a multi-line string describing input validation issues
	 * @throws IOException Invalid file or parameters
	 */
	private String validateInput() throws IOException {
		String r = "";

		// check inputs exist
		if (!bamFile.exists()) {
			r += "(!)BAM file does not exist: " + bamFile.getName() + "\n";
		}
		if (!genomeFASTA.exists()) {
			r += "(!)FASTA file does not exist: " + genomeFASTA.getName() + "\n";
		}
		if (!r.equals("")) {
			return (r);
		}
		// check BAI exists
		File f = new File(bamFile + ".bai");
		if (!f.exists() || f.isDirectory()) {
			r += "(!)BAI Index File does not exist for: " + bamFile.getName() + "\n";
		}
		// check FAI exists (generate if not)
		File FAI = new File(genomeFASTA + ".fai");
		if (!FAI.exists() || FAI.isDirectory()) {
			System.err.println("FASTA Index file not found.\nGenerating new one...\n");
			FastaSequenceIndexCreator.create(genomeFASTA.toPath(), true);

		}
		// set default output filename
		if (output == null) {
			output = new File(ExtensionFileFilter.stripExtension(bamFile) + "_PSfilter.bam");
			// check output filename is valid
		} else {
			// check directory
			if (output.getParent() == null) {
// 					System.err.println("default to current directory");
			} else if (!new File(output.getParent()).exists()) {
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}

		// check filter string is valid ATCG
		Pattern seqPat = Pattern.compile("[ATCG]+");
		Matcher m = seqPat.matcher(filterString);
		if (!m.matches()) {
			r += "(!)Filter string must be formatted as a nucleotide sequence.\n" + filterString
					+ " is not a valid nucleotide sequence.\nExpected input string format: \"[ATCG]\"";
		}

		return (r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param BAM    the BAM file to filter
	 * @param GENOME the genomic FASTA reference file (should match BAM header)
	 * @param OUTPUT the output BAM file
	 * @param txtSeq the IUPAC string to filter by
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File BAM, File GENOME, File OUTPUT, String txtSeq) {
		String command = "java -jar $SCRIPTMANAGER bam-manipulation filter-pip-seq";
		command += " " + GENOME.getAbsolutePath();
		command += " " + BAM.getAbsolutePath();
		command += " -o " + OUTPUT.getAbsolutePath();
		command += " -f " + txtSeq;
		return command;
	}
}