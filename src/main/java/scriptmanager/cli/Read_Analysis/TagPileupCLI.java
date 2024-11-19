package scriptmanager.cli.Read_Analysis;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import scriptmanager.objects.PileupParameters;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.BAMUtilities;

import scriptmanager.scripts.Read_Analysis.TagPileup;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Read_Analysis.TagPileup}
 * 
 * @author Olivia Lang
 */
@Command(name = "tag-pileup", mixinStandardHelpOptions = true,
	description = ToolDescriptions.tag_pileup_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	descriptionHeading = "%nDescription:%n%n",
	parameterListHeading = "%nParameters:%n",
	optionListHeading = "%nGeneral Options:%n",
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class TagPileupCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "The BED file with reference coordinates to pileup on.")
	private File bedFile;
	@Parameters( index = "1", description = "The BAM file from which we remove duplicates. Make sure its indexed!")
	private File bamFile;
	
	@Option(names = {"-d", "--dry-run"}, description = "print all parameters without running anything")
	private boolean dryRun=false;
	
	//Output
	@ArgGroup(validate = false, heading = "%nOutput Options:%n")
	OutputOptions outputOptions = new OutputOptions();
	static class OutputOptions{
		@Option(names = {"-o", "--output-composite"}, description = "specify output file for composite values")
		private String outputComposite = "composite_average.out";
		@Option(names = {"-M", "--output-matrix"}, description = "specify output basename for matrix files (files each for sense and anti will be output)",
			arity="0..1")
		private ArrayList<String> outputMatrix = new ArrayList<String>(Arrays.asList("no","matrix","output"));
		@Option(names = {"-z", "--gzip"}, description = "output compressed output (default=false)")
		private boolean gzOutput = false;
		@Option(names = {"--cdt"}, description = "output matrix in cdt format (default)")
		private boolean cdt = false;
		@Option(names = {"--tab"}, description = "output matrix in tab format")
		private boolean tab = false;
	}
	
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
		@Option(names = {"--full-fragment"}, description = "pile full fragment length (require PE)")
		boolean fragment = false;
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
	
	//Strand
	@Option(names = {"--combined"}, description = "select output strands as combined (default separates into *_sense and *_anti)")
	boolean combStatus = false;
	
	//Smooth
	@ArgGroup(exclusive = true, multiplicity = "0..1", heading = "%nComposite Transformation/Smoothing Options:%n\t@|fg(red) (select no more than one of these options)|@%n")
	SmoothMethod smoothType = new SmoothMethod();
	static class SmoothMethod {
		@Option(names = {"-N","--no-smooth"}, description = "no smoothing applied to composite (default)")
		boolean noSmooth = false;
		@Option(names = {"-w", "--window-smooth"}, description = "sliding window smoothing applied to composite using default 3 bins for window size")
		boolean winStatus = false;
		@Option(names = {"-W", "--window-values"}, description = "sliding window smoothing applied to composite with user specified window size (in #bins)")
		int winVals = -9999;
		@Option(names = {"-g", "--gauss-smooth"}, description = "gauss smoothing applied to composite using default values: 5 bins and 3 standard deviations")
		boolean gaussStatus = false;
		@Option(names = {"-G", "--gauss-values"}, arity = "2", description = "gauss smoothing applied to composite with user specified standard deviation(SD) size (in #bins) followed by the number of SD")
		int[] gaussVals = new int[]{-9999,-9999};
	}
	
	@ArgGroup(validate = false, heading = "%nCalculation Options:%n")
	CalcOptions calcOptions = new CalcOptions();
	static class CalcOptions{	
		@Option(names = {"-s", "--shift"}, description = "set a shift in bp (default=0bp)")
		private int shift = 0;
		@Option(names = {"-b", "--bin-size"}, description = "set a bin size for the output (default=1bp)")
		private int binSize = 1;
		@Option(names = {"-e", "--tag-extend"}, description = "set a bp length to extend the tag (default=0)")
		private int tagExtend = 0;
		@Option(names = {"-t", "--standard"}, description = "set tags to be equal (default=false)")
		private boolean tagsEqual;
		@Option(names = {"--cpu"}, description = "set number of CPUs to use (default=1)")
		private int cpu = 1;
	}
	
	@ArgGroup(validate = false, heading = "%nFilter Options:%n")
	FilterOptions filterOptions = new FilterOptions();
	static class FilterOptions{
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
		System.err.println( ">TagPileupCLI.call()" );
		p = new PileupParameters();
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		if(dryRun){
			p.printAll();
			System.err.println(outputOptions.outputMatrix.get(0));
			System.exit(1);
		}
		
		TagPileup script_obj = new TagPileup(bedFile, bamFile, p, null, outputOptions.outputMatrix.get(0));
		script_obj.run();
		
		System.err.println( "Calculations complete" );
		return(0);
	}

	private String validateInput() throws IOException {
		String r = "";

		// Set ASPECT
		if(aspectType.fiveprime) { p.setAspect(PileupParameters.FIVE); }
		else if(aspectType.threeprime) { p.setAspect(PileupParameters.THREE); }
		else if(aspectType.midpoint) { p.setAspect(PileupParameters.MIDPOINT); }
		else if(aspectType.fragment) { p.setAspect(PileupParameters.FRAGMENT); }

		// Set READ
		if(readType.read1){ p.setRead(PileupParameters.READ1); }
		else if(readType.read2){ p.setRead(PileupParameters.READ2); }
		else if(readType.allreads){ p.setRead(PileupParameters.ALLREADS); }

		//check inputs exist
		if(!bedFile.exists()){
			r += "(!)BED file does not exist: " + bedFile.getCanonicalPath() +  "\n";
		}
		if(!bamFile.exists()){
			r += "(!)BAM file does not exist: " + bamFile.getCanonicalPath() +  "\n";
		}
		if(!r.equals("")){ return(r); }
		//check BAI exists
		File f = new File(bamFile+".bai");
		if(!f.exists() || f.isDirectory()){
			r += "(!)BAI Index File does not exist for: " + bamFile.getName() +  "\n";
		}

		//set default output COMPOSITE filename (done by picocli)
		//check output COMPOSITE filename is valid
		if(outputOptions.outputComposite!="composite_average.out"){
			File output = new File(outputOptions.outputComposite);
			//check directory
			if(output.getParent()==null){
// 				System.err.println("default to current directory");
			} else if(!new File(output.getParent()).exists()){
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}

		//validate smooth params
		if(smoothType.winVals!=-9999 && smoothType.winVals<1){ r += "(!)Invalid Smoothing Window Size. Must be larger than 0 bins, winSize=" + smoothType.winVals + "\n"; }
		if(smoothType.winVals!=-9999 && smoothType.winVals%2==0){ r += "(!)Invalid Smoothing Window Size. Must be odd for symmetrical smoothing (so that the window is centered properly), winSize=" + smoothType.winVals + "\n"; }
		if(smoothType.gaussVals[0]!=-9999 && smoothType.gaussVals[0]<1){ r += "(!)Invalid Standard Deviation Size. Must be larger than 0 bins, stdSize=" + smoothType.gaussVals[0] + "\n"; }
		if(smoothType.gaussVals[1]!=-9999 && smoothType.gaussVals[1]<1){ r += "(!)Invalid Number of Standard Deviations. Must be larger than 0 standard deviations, stdNum=" + smoothType.gaussVals[1] + "\n"; }
		
		//set require PE for appropriate flags
		p.setPErequire(filterOptions.requirePE);
		if( filterOptions.MIN_INSERT!=-9999 || filterOptions.MAX_INSERT!=-9999 || p.getAspect()==2) { p.setPErequire(true); }
		
		//validate shift, binSize, and CPUs
		if(calcOptions.shift<0){  r += "(!)Invalid shift! Must be non-negative, shift=" + calcOptions.shift + "\n"; }
		if(calcOptions.binSize<1){  r += "(!)Invalid Bin Size! Must use at least 1bp, binSize=" + calcOptions.binSize + "\n"; }
		if(calcOptions.cpu<1){  r += "(!)Invalid Number of CPU's! Must use at least 1, CPU=" + calcOptions.cpu + "\n"; }
		
		//validate insert sizes
		if( filterOptions.MIN_INSERT<0 && filterOptions.MIN_INSERT!=-9999 ){ r += "(!)MIN_INSERT must be a positive integer value: " + p.getMinInsert() + "\n"; }
		if( filterOptions.MAX_INSERT<0 && filterOptions.MAX_INSERT!=-9999 ){ r += "(!)MAX_INSERT must be a positive integer value: " + p.getMaxInsert() + "\n"; }
		if( filterOptions.MIN_INSERT!=-9999 && filterOptions.MAX_INSERT!=-9999 && filterOptions.MAX_INSERT<filterOptions.MIN_INSERT ){
			r += "(!)MAX_INSERT must be larger/equal to MIN_INSERT: " + filterOptions.MIN_INSERT + "," + filterOptions.MAX_INSERT + "\n";
		}
		
		// No Matrix Output
		if(outputOptions.outputMatrix.size() > 1){
			p.setOutputType(0);
			if(outputOptions.cdt || outputOptions.tab) { r += "(!)Cannot flag --cdt or --tab without -M."; }
		// Output Matrix
		} else {
			// Determine output type
			p.setOutputType(2);
			if(outputOptions.cdt && outputOptions.tab) { r += "(!)Cannot flag both --cdt and --tab. Please choose one."; }
			else if(outputOptions.tab) { p.setOutputType(1); }
			// No matrix basename specified
			if(outputOptions.outputMatrix.size() == 0) {
				outputOptions.outputMatrix.add(null);
				if(p.getOutputDirectory() == null) {
					p.setOutputDirectory(new File(System.getProperty("user.dir")));
				}
			// Validate matrix specified basename
			} else if(outputOptions.outputMatrix.size() == 1) {
				File output = new File(outputOptions.outputMatrix.get(0));
				// Check parent directory is non-null and exists
				if(output.getParent()!=null){
					if(!new File(output.getParent()).exists()) {
						r += "(!)Check output.MATRIX directory exists: " + output.getParent() + "\n";
					}
				}
			}
		}

		//Set COMPOSITE file
		p.setOutputCompositeStatus(true);
		p.setCompositePrintStream(new PrintStream(outputOptions.outputComposite));

		//Set STRAND
		p.setStrand(PileupParameters.SEPARATE);
		if(combStatus || p.getAspect() == PileupParameters.MIDPOINT) { p.setStrand(PileupParameters.COMBINED); }

		//Set smooth type and parameters
		if(smoothType.noSmooth){			//default behavior
			p.setTrans(PileupParameters.NO_SMOOTH);
		}else if(smoothType.winStatus){   //window default
			p.setTrans(PileupParameters.WINDOW);
			p.setSmooth(3);
		}else if(smoothType.winVals!=-9999){
			p.setTrans(PileupParameters.WINDOW);
			p.setSmooth(smoothType.winVals);
		}else if(smoothType.gaussStatus){   //gauss default
			p.setTrans(PileupParameters.GAUSSIAN);
			p.setStdSize(5);
			p.setStdNum(3);
		}else if(smoothType.gaussVals[0]!=-9999 && smoothType.gaussVals[1]!=-9999){
			p.setTrans(PileupParameters.GAUSSIAN);
			p.setStdSize(smoothType.gaussVals[0]);
			p.setStdNum(smoothType.gaussVals[1]);
		}else{ p.setTrans(0); }      //default behavior

		//Set SHIFT, BIN, CPU
		p.setShift(calcOptions.shift);
		p.setBin(calcOptions.binSize);
		p.setTagExtend(calcOptions.tagExtend);
		p.setCPU(calcOptions.cpu);

		//Set BLACKLIST & STANDARD
		p.setBlacklist(filterOptions.blacklistFilter);
		p.setStandard(calcOptions.tagsEqual);

		//Set output statuses
		p.setGZIPstatus(outputOptions.gzOutput);

		//Set Ratio (code to standardize tags sequenced to genome size (1 tag / 1 bp))
		if( p.getStandard() && filterOptions.blacklistFilter!=null ){
			p.setRatio(BAMUtilities.calculateStandardizationRatio(bamFile, filterOptions.blacklistFilter, p.getRead()));
		}else if( p.getStandard() ){
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
		String command = "java -jar $SCRIPTMANAGER read-analysis tag-pileup";
		command += " " + BED.getAbsolutePath();
		command += " " + BAM.getAbsolutePath();
		command += " " + PARAM.getCLIOptions();
		String NAME = PARAM.getOutputDirectory() + File.separator + PARAM.generateFileBase(BED.getName(), BAM.getName());
		command += " -M " + NAME + " -o " + NAME;
		return(command);
	}
}