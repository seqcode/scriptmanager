package scriptmanager.cli.Sequence_Analysis;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.DNAShapeReference;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED}
 * 
 * @author Olivia Lang
 */
@Command(name = "dna-shape-bed", mixinStandardHelpOptions = true,
	description = ToolDescriptions.dna_shape_from_bed_description,
	version = "ScriptManager " + ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class DNAShapefromBEDCLI implements Callable<Integer> {

	/**
	 * Creates a new DNAShapefromBEDCLI object
	 */
	public DNAShapefromBEDCLI(){}

	@Parameters(index = "0", description = "reference genome FASTA file")
	private File genomeFASTA;
	@Parameters(index = "1", description = "the BED file of sequences to extract")
	private File bedFile;

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
	@Option(names = { "-n", "--no-force" }, description = "don't force-strandedness (default is to force strandedness)")
	private boolean forceStrand = true;

	@ArgGroup(validate = false, heading = "Shape Options%n")
	ShapeType shape = new ShapeType();
	ArrayList<Integer> OUTPUT_TYPES;

	public static class ShapeType {
		@Option(names = { "-g", "--groove" }, description = "output minor groove width")
		public boolean groove = false;
		@Option(names = { "-r", "--roll" }, description = "output roll")
		public boolean roll = false;
		@Option(names = { "-p", "--propeller" }, description = "output propeller twist")
		public boolean propeller = false;
		@Option(names = { "-l", "--helical" }, description = "output helical twist")
		public boolean helical = false;
		@Option(names = { "--electrostatic-potential" }, description = "output electrostatic potential")
		public boolean ep = false;
		@Option(names = { "--stretch" }, description = "output stretch")
		public boolean stretch = false;
		@Option(names = { "--buckle" }, description = "output buckle")
		public boolean buckle = false;
		@Option(names = { "--shear" }, description = "output shear")
		public boolean shear = false;
		@Option(names = { "--opening" }, description = "output opening")
		public boolean opening = false;
		@Option(names = { "--stagger" }, description = "output stagger")
		public boolean stagger = false;
		@Option(names = { "--tilt" }, description = "output tilt")
		public boolean tilt = false;
		@Option(names = { "--slide" }, description = "output slide")
		public boolean slide = false;
		@Option(names = { "--rise" }, description = "output rise")
		public boolean rise = false;
		@Option(names = { "--shift" }, description = "output shift")
		public boolean shift = false;
		@Option(names = { "--2013" }, description = "output groove, roll, propeller twist, and helical twist (equivalent to -grpl).")
		public boolean originalAll = false;
		@Option(names = { "-a", "--2021" }, description = "output all 14 shapes")
		public boolean all = false;
	}

	private short outputMatrix = DNAShapefromBED.NO_MATRIX;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">DNAShapefromBEDCLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		// Generate Composite Plot
		DNAShapefromBED script_obj = new DNAShapefromBED(genomeFASTA, bedFile, outputBasename, OUTPUT_TYPES,
				forceStrand, composite, outputMatrix, gzOutput);
		script_obj.run();
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

		// validate genome

		if (!genomeFASTA.exists()) {
			r += "(!)FASTA file does not exist: " + genomeFASTA.getName() + "\n";
		}
		if (!bedFile.exists()) {
			r += "(!)BED file does not exist: " + bedFile.getName() + "\n";
		}
		if (!r.equals("")) {
			return (r);
		}
		// set default output filename
		if (outputBasename == null) {
			outputBasename = new File(ExtensionFileFilter.stripExtension(bedFile));
			// check output filename is valid
		} else {
			String outParent = outputBasename.getParent();
			// no extension check
			// check directory
			if (outParent == null) {
				System.err.println("default to current directory");
			} else if (!new File(outParent).exists()) {
				r += "(!)Check output directory exists: " + outParent + "\n";
			}
		}

		OUTPUT_TYPES = new ArrayList<>();
		if (shape.groove) { OUTPUT_TYPES.add(DNAShapeReference.MGW); }
		if (shape.propeller) { OUTPUT_TYPES.add(DNAShapeReference.PROPT); }
		if (shape.helical) { OUTPUT_TYPES.add(DNAShapeReference.HELT); }
		if (shape.roll) { OUTPUT_TYPES.add(DNAShapeReference.ROLL); }
		if (shape.ep) { OUTPUT_TYPES.add(DNAShapeReference.EP); }
		if (shape.stretch) { OUTPUT_TYPES.add(DNAShapeReference.STRETCH); }
		if (shape.buckle) { OUTPUT_TYPES.add(DNAShapeReference.BUCKLE); }
		if (shape.shear) { OUTPUT_TYPES.add(DNAShapeReference.SHEAR); }
		if (shape.opening) { OUTPUT_TYPES.add(DNAShapeReference.OPENING); }
		if (shape.stagger) { OUTPUT_TYPES.add(DNAShapeReference.STAGGER); }
		if (shape.tilt) { OUTPUT_TYPES.add(DNAShapeReference.TILT); }
		if (shape.slide) { OUTPUT_TYPES.add(DNAShapeReference.SLIDE); }
		if (shape.rise) { OUTPUT_TYPES.add(DNAShapeReference.RISE); }
		if (shape.shift) { OUTPUT_TYPES.add(DNAShapeReference.SHIFT); }
		if (shape.all){
			for (int i = 0; i < 14; i++){
				if (OUTPUT_TYPES.contains(i)){
					r += "(!)Please avoid mixing the \"-a\" flag with the other shape flags.\n";
				}
				else {
					OUTPUT_TYPES.add(i);
				}
			}
		}
		else if (shape.originalAll){
			for (int i = 0; i < 5; i++){
				if (OUTPUT_TYPES.contains(i)){
					r += "(!)Please avoid mixing the \"--2013\" flag with the other shape flags.\n";
				}
				else {
					OUTPUT_TYPES.add(i);
				}
			}
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
	 * @param gen             the reference genome sequence in FASTA-format (FAI
	 *                        will be automatically generated)
	 * @param input           the BED-formatted coordinate intervals to extract
	 *                        sequence from
	 * @param out             the output file name base (to add
	 *                        _&lt;shapetype&gt;.cdt suffix to)
	 * @param type            An ArrayList specifying the shape type of the output with integers
	 * @param str             force strandedness (true=forced, false=not forced)
	 * @param outputComposite whether to output a composite average output
	 * @param outputMatrix    value encoding not to write output matrix data, write
	 *                        matrix in CDT format, and write matrix in tab format
	 * @param gzOutput        whether or not to gzip output
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File gen, File input, File out, ArrayList<Integer> type, boolean str, boolean outputComposite, short outputMatrix, boolean gzOutput) throws OptionException {
		String command = "java -jar $SCRIPTMANAGER sequence-analysis dna-shape-bed";
		command += " -o " + out.getAbsolutePath();
		command += gzOutput ? " -z " : "";
		if (type.size() == 14){
			command += type.size() == 14? "-a" : "";
		} else {
			command += type.contains(0)? "--groove" : "";
			command += type.contains(1)? "--propeller" : "";
			command += type.contains(2)? "--helical" : "";
			command += type.contains(3)? "--roll" : "";
			command += type.contains(4)? "--electrostatic-potential" : "";
			command += type.contains(5)? "--stretch" : "";
			command += type.contains(6)? "--buckle" : "";
			command += type.contains(7)? "--shear" : "";
			command += type.contains(8)? "--opening" : "";
			command += type.contains(9)? "--stagger" : "";
			command += type.contains(10)? "--tilt" : "";
			command += type.contains(11)? "--slide" : "";
			command += type.contains(12)? "--rise" : "";
			command += type.contains(13)? "--shift" : "";
		}
		command += str ? "" : "--no-force";
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
		command += " " + gen.getAbsolutePath();
		command += " " + input.getAbsolutePath();
		return (command);
	}
}