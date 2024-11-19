package scriptmanager.cli.Sequence_Analysis;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED;
import scriptmanager.scripts.Sequence_Analysis.DNAShapefromFASTA;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Sequence_Analysis.DNAShapefromFASTA}
 * 
 * @author Olivia Lang
 */
@Command(name = "dna-shape-fasta", mixinStandardHelpOptions = true,
	description = ToolDescriptions.dna_shape_from_fasta_description,
	version = "ScriptManager " + ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class DNAShapefromFASTACLI implements Callable<Integer> {

	/**
	 * Creates a new DNAShapefromFASTACLI object
	 */
	public DNAShapefromFASTACLI(){}

	@Parameters(index = "0", description = "FASTA sequence file")
	private File fastaFile;

	@Option(names = { "-o", "--output" }, description = "Specify basename for output files, files for each shape indicated will share this name with a different suffix")
	private File outputBasename = null;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	@Option(names = { "--composite" }, description = "Save average composite (column-wise avg of matrix)")
	private boolean composite = false;
	@Option(names = { "--matrix" }, description = "Save tab-delimited matrix of shape scores")
	private boolean matrix = false;
	@Option(names = { "--cdt" }, description = "Save CDT-formatted matrix")
	private boolean cdt = true;

	@ArgGroup(validate = false, heading = "Shape Options%n")
	ShapeType shape = new ShapeType();

	static class ShapeType {
		@Option(names = { "-g", "--groove" }, description = "output minor groove width")
		private boolean groove = false;
		@Option(names = { "-r", "--roll" }, description = "output roll")
		private boolean roll = false;
		@Option(names = { "-p", "--propeller" }, description = "output propeller twist")
		private boolean propeller = false;
		@Option(names = { "-l", "--helical" }, description = "output helical twist")
		private boolean helical = false;
		@Option(names = { "-a", "--all" }, description = "output groove, roll, propeller twist, and helical twist (equivalent to -grpl).")
		private boolean all = false;
	}

	private boolean[] OUTPUT_TYPE = new boolean[] { false, false, false, false };
	private short outputMatrix = DNAShapefromBED.NO_MATRIX;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">DNAShapefromFASTACLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		// Generate Composite Plot
		DNAShapefromFASTA script_obj = new DNAShapefromFASTA(fastaFile, outputBasename, OUTPUT_TYPE,
				composite, outputMatrix, gzOutput);
		script_obj.run();
		// Print Composite Scoress
		if (composite) {
			String[] headers = new String[] { "AVG_MGW", "AVG_PropT", "AVG_HelT", "AVG_Roll" };
			for (int t = 0; t < OUTPUT_TYPE.length; t++) {
				if (OUTPUT_TYPE[t]) {
					PrintStream COMPOSITE = new PrintStream(new File(outputBasename + "_" + headers[t] + ".out"));
					double[] AVG = script_obj.getAvg(t);
					// position vals
					for (int z = 0; z < AVG.length; z++) {
						COMPOSITE.print("\t" + z);
					}
					COMPOSITE.print("\n" + ExtensionFileFilter.stripExtension(fastaFile) + "_" + headers[t]);
					// score vals
					for (int z = 0; z < AVG.length; z++) {
						COMPOSITE.print("\t" + AVG[z]);
					}
					COMPOSITE.println();
				}
			}
		}

		System.err.println("Shapes Calculated.");
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
		if (!fastaFile.exists()) {
			r += "(!)FASTA file does not exist: " + fastaFile.getName() + "\n";
		}
		if (!r.equals("")) {
			return (r);
		}
		// set default output filename
		if (outputBasename == null) {
			outputBasename = new File(ExtensionFileFilter.stripExtension(fastaFile));
			// check output filename is valid
		} else {
			String outParent = outputBasename.getParent();
			// no extension check
			// check directory
			if (outParent == null) {
// 				System.err.println("default to current directory");
			} else if (!new File(outParent).exists()) {
				r += "(!)Check output directory exists: " + outParent + "\n";
			}
		}

		// Check & set output_type
		if (!(shape.groove || shape.propeller || shape.helical || shape.roll || shape.all)) {
			r += "(!)Please select at least one of the shape flags.\n";
		} else if ((shape.groove || shape.propeller || shape.helical || shape.roll) && shape.all) {
			r += "(!)Please avoid mixing the \"-a\" flag with the other shape flags.\n";
		}

		if (shape.groove) {
			OUTPUT_TYPE[0] = true;
		}
		if (shape.propeller) {
			OUTPUT_TYPE[1] = true;
		}
		if (shape.helical) {
			OUTPUT_TYPE[2] = true;
		}
		if (shape.roll) {
			OUTPUT_TYPE[3] = true;
		}

		if (shape.all) {
			OUTPUT_TYPE = new boolean[] { true, true, true, true };
		}

		if (matrix && cdt) {
			r += "(!)Please select either the matrix or the cdt flag.\n";
		} else if (matrix) {
			outputMatrix = DNAShapefromBED.TAB;
		} else if (cdt) {
			outputMatrix = DNAShapefromBED.CDT;
		}

		return (r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input           the FASTA-formatted file with a fixed sequence length
	 * @param out             the output file name base (to add
	 *                        _&lt;shapetype&gt;.cdt suffix to)
	 * @param type            a four-element boolean list for specifying shape type
	 *                        to output (no enforcement on size)
	 * @param outputComposite whether to output a composite average output
	 * @param outputMatrix    value encoding not to write output matrix data, write
	 *                        matrix in CDT format, and write matrix in tab format
	 * @param gzOutput        whether or not to gzip output
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input, File out, boolean[] type, boolean outputComposite, short outputMatrix, boolean gzOutput) throws OptionException {
		String command = "java -jar $SCRIPTMANAGER sequence-analysis dna-shape-fasta";
		command += " -o " + out.getAbsolutePath();
		command += gzOutput ? " -z " : "";
		command += type[0] ? " --groove" : "";
		command += type[1] ? " --propeller" : "";
		command += type[2] ? " --helical" : "";
		command += type[3] ? " --roll" : "";
		command += outputComposite ? "--composite" : "";
		switch (outputMatrix) {
			case DNAShapefromBED.NO_MATRIX:
				break;
			case DNAShapefromBED.TAB:
				command += " --matrix";
				break;
			case DNAShapefromBED.CDT:
				command += " --cdt";
				break;
			default:
				throw new OptionException("outputMatrix type value " + outputMatrix + " not supported");
		}
		command += " " + input.getAbsolutePath();
		return (command);
	}
}