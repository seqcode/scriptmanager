package scriptmanager.cli.Peak_Analysis;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.util.BAMUtilities;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.objects.PileupParameters;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.scripts.Peak_Analysis.FRiXCalculator;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Peak_Analysis.FRiXCalculator}
 * 
 * @author Olivia Lang
 */
@Command(name = "frix", mixinStandardHelpOptions = true,
	description = ToolDescriptions.frix_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	descriptionHeading = "%nDescription:%n%n",
	parameterListHeading = "%nParameters:%n",
	optionListHeading = "%nGeneral Options:%n",
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class FRiXCalculatorCLI implements Callable<Integer> {

	@Parameters( index = "0", description = "The BED file with reference coordinate windows to tally from.")
	private File bedFile;
	@Parameters( index = "1", description = "The BAM file from which we remove duplicates. Make sure its indexed!")
	private File bamFile;

	@Option(names = {"-o", "--output"}, description = "specify output basename, default is the BAM input filename without extension")
	private File outputBasename = null;

	//Aspect
	@ArgGroup(exclusive = true, multiplicity = "0..1", heading = "%nSelect Aspect of Read to output:%n\t@|fg(red) (select no more than one of these options)|@%n")
	AspectType aspectType = new AspectType();
	static class AspectType {
		@Option(names = {"-5", "--five-prime"}, description = "pileup of 5' end of read(default)")
		boolean fiveprime = false;
		@Option(names = {"-3", "--three-prime"}, description = "pileup of 3' end of read")
		boolean threeprime = false;
		@Option(names = {"-m", "--midpoint"}, description = "pileup fragment midpoints (require PE, combined)")
		boolean midpoint = false;
	}

	//Read
	@ArgGroup(exclusive = true, multiplicity = "0..1", heading = "%nSelect Read to output:%n\t@|fg(red) (select no more than one of these options)|@%n")
	ReadType readType = new ReadType();
	static class ReadType {
		@Option(names = {"-1", "--read1"}, description = "pileup of read 1 (default)")
		boolean read1 = false;
		@Option(names = {"-2", "--read2"}, description = "pileup of read 2")
		boolean read2 = false;
		@Option(names = {"-a", "--all-reads"}, description = "pileup all reads")
		boolean allreads = false;
	}

	@ArgGroup(validate = false, heading = "%nCalculation Options:%n")
	CalcOptions calcOptions = new CalcOptions();
	static class CalcOptions {
		@Option(names = {"-s", "--shift"}, description = "set a shift in bp (default=0bp)")
		private int shift = 0;
		@Option(names = {"--cpu"}, description = "set number of CPUs to use (default=1)")
		private int cpu = 1;
	}

	@ArgGroup(validate = false, heading = "%nFilter Options:%n")
	FilterOptions filterOptions = new FilterOptions();
	static class FilterOptions {
		@Option(names = {"-f", "--blacklist-filter"}, description = "specify a blacklist file to filter BED by, must use with -t flag")
		private File blacklistFilter = null;
		@Option(names = {"-p", "--require-pe"}, description = "require proper paired ends (default=false)\nautomatically turned on with any of flags -mnx")
		private boolean requirePE = false;
		@Option(names = {"-n", "--min-insert"}, description = "filter by minimum insert size in bp, require PE (default=no minimum)")
		private int MIN_INSERT = -9999;
		@Option(names = {"-x", "--max-insert"}, description = "filter by maximum insert size in bp, require PE (default=no maximum)")
		private int MAX_INSERT = -9999;
	}

	PileupParameters p;

	/**
	 * Runs when this subcommand is called, running script in respective script
	 * package with user defined arguments
	 * 
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">FRiXCalculatorCLI.call()" );
		p = new PileupParameters();
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		FRiXCalculator script_obj = new FRiXCalculator(bedFile, bamFile, p, null, outputBasename.getAbsolutePath());
		script_obj.run();

		System.err.println( "Calculations complete" );
		return(0);
	}

	private String validateInput() throws IOException {
		String r = "";

		// Set ASPECT
		if (aspectType.fiveprime) { p.setAspect(PileupParameters.FIVE); }
		else if (aspectType.threeprime) { p.setAspect(PileupParameters.THREE); }
		else if  (aspectType.midpoint) { p.setAspect(PileupParameters.MIDPOINT); }

		// Set READ
		if (readType.read1) { p.setRead(PileupParameters.READ1); }
		else if (readType.read2) { p.setRead(PileupParameters.READ2); }
		else if (readType.allreads) { p.setRead(PileupParameters.ALLREADS); }

		//check inputs exist
		if (!bedFile.exists()) {
			r += "(!)BED file does not exist: " + bedFile.getCanonicalPath() +  "\n";
		}
		if (!bamFile.exists()) {
			r += "(!)BAM file does not exist: " + bamFile.getCanonicalPath() +  "\n";
		}
		if (!r.equals("")) { return(r); }
		//check BAI exists
		File f = new File(bamFile+".bai");
		if (!f.exists() || f.isDirectory()) {
			r += "(!)BAI Index File does not exist for: " + bamFile.getName() + "\n";
		}

		//set default output filename
		if (outputBasename==null) {
			outputBasename = new File(ExtensionFileFilter.stripExtension(bedFile) + "_" + ExtensionFileFilter.stripExtension(bamFile) + "_FRiXscore.txt");
		//check output filename is valid
		} else {
			//no check ext
			//check directory
			if (outputBasename.getParent()==null) {
// 				System.err.println("default to current directory");
			} else if(!new File(outputBasename.getParent()).exists()) {
				r += "(!)Check output directory exists: " + outputBasename.getParent() + "\n";
			}
		}

		//set require PE for appropriate flags
		p.setPErequire(filterOptions.requirePE);
		if (filterOptions.MIN_INSERT!=-9999 || filterOptions.MAX_INSERT!=-9999 || p.getAspect()==2) { p.setPErequire(true); }

		//validate shift, binSize, and CPUs
		if (calcOptions.shift<0){  r += "(!)Invalid shift! Must be non-negative, shift=" + calcOptions.shift + "\n"; }
		if (calcOptions.cpu<1){  r += "(!)Invalid Number of CPU's! Must use at least 1, CPU=" + calcOptions.cpu + "\n"; }

		//validate insert sizes
		if (filterOptions.MIN_INSERT<0 && filterOptions.MIN_INSERT!=-9999) { r += "(!)MIN_INSERT must be a positive integer value: " + p.getMinInsert() + "\n"; }
		if (filterOptions.MAX_INSERT<0 && filterOptions.MAX_INSERT!=-9999) { r += "(!)MAX_INSERT must be a positive integer value: " + p.getMaxInsert() + "\n"; }
		if (filterOptions.MIN_INSERT!=-9999 && filterOptions.MAX_INSERT!=-9999 && filterOptions.MAX_INSERT<filterOptions.MIN_INSERT) {
			r += "(!)MAX_INSERT must be larger/equal to MIN_INSERT: " + filterOptions.MIN_INSERT + "," + filterOptions.MAX_INSERT + "\n";
		}

		//Set SHIFT, BIN, CPU
		p.setShift(calcOptions.shift);
		p.setCPU(calcOptions.cpu);

		//Set Ratio (code to standardize tags sequenced to genome size (1 tag / 1 bp))
		if (p.getStandard() && filterOptions.blacklistFilter!=null) {
			p.setRatio(BAMUtilities.calculateStandardizationRatio(bamFile, filterOptions.blacklistFilter, p.getRead()));
		} else if (p.getStandard()) {
			p.setRatio(BAMUtilities.calculateStandardizationRatio(bamFile, p.getRead()));
		}

		//Set MIN_INSERT & MAX_INSERT
		p.setMinInsert(filterOptions.MIN_INSERT);
		p.setMaxInsert(filterOptions.MAX_INSERT);

		return(r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param BED   input BED file
	 * @param BAM   input BAM file
	 * @param PARAM parameters for tag pileup
	 * @return command line to execute with formatted inputs
	 * @throws OptionException thrown by PileupParameters if invalid param values given
	 */
	public static String getCLIcommand(File BED, File BAM, PileupParameters PARAM) throws OptionException {
		String command = "java -jar $SCRIPTMANAGER peak-analysis frix";
		command += " " + BED.getAbsolutePath();
		command += " " + BAM.getAbsolutePath();

		// Add ASPECT
		switch (PARAM.getAspect()) {
			case PileupParameters.FIVE:
				command += " -5";
				break;
			case PileupParameters.THREE:
				command += " -3";
				break;
			case PileupParameters.MIDPOINT:
				command += " -m";
				break;
			default:
				throw new OptionException("Unknown PileupParameters read aspect value: " + PARAM.getAspect());
		}

		// Add READ
		switch (PARAM.getRead()) {
			case PileupParameters.READ1:
				command += " -1";
				break;
			case PileupParameters.READ2:
				command += " -2";
				break;
			case PileupParameters.ALLREADS:
				command += " -a";
				break;
			default:
				throw new OptionException("Unknown PileupParameters read output value: " + PARAM.getRead());
		}

		// Add requirePE
		command += PARAM.getPErequire() ? " -p" : "";
		// Add MIN_INSERT
		if (PARAM.getMinInsert() != -9999) { command += " -n " + PARAM.getMinInsert(); }
		// Add MAX_INSERT
		if (PARAM.getMaxInsert() != -9999) { command += " -x " + PARAM.getMaxInsert(); }

		// Add SHIFT
		command += " -s " + PARAM.getShift();
		// Add CPU
		command += " --cpu " + PARAM.getCPU();

		// Add output
		String NAME = PARAM.getOutputDirectory() + File.separator + PARAM.generateFileBase(BED.getName(), BAM.getName());
		command += " -o " + NAME;

		return(command);
	}
}
