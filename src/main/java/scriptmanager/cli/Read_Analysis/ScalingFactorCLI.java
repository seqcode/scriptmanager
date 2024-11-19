package scriptmanager.cli.Read_Analysis;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Read_Analysis.ScalingFactor;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Read_Analysis.ScalingFactor}
 * 
 * @author Olivia Lang
 */
@Command(name = "scaling-factor", mixinStandardHelpOptions = true,
	description = ToolDescriptions.scaling_factor_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class ScalingFactorCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "The BAM file from which we calculate the scaling factor.")
	private File bamFile;
	
	@Option(names = {"-o", "--output"}, description = "Specify basename for output files (default = <bamFilename>_ScalingFactors.out")
	private File outputBasename = null;
	@Option(names = {"-f", "--blacklist"}, description = "specify blacklist file to filter by")
	private File blacklistFilter = null;
	@Option(names = {"-c", "--control"}, description = "control BAM file")
	private File controlBAM = null;
	
	@ArgGroup(exclusive = true, heading = "Scale Options%n")
	ScaleType scale = new ScaleType();
	static class ScaleType{
		@Option(names = {"-t", "--total-tag"}, description = "total tag scaling (default)")
		private boolean total = false;
		@Option(names = {"-n", "--ncis"}, description = "ncis normalization with window size in bp and unitless minimum fraction (default-size=500, default-fraction=0.75)" +
		"%nAccording to the NCIS method from Liang & Keles (BMC Bioinf 2012). Also sets a background proportion estimate for the signal channel.")
		private boolean ncis = false;
		@Option(names = {"-b", "--both"}, description = "ncis with total tag (default-size=500, default-fraction=0.75)")
		private boolean both = false;
	}
	
	@Option(names = {"-w", "--window-size"}, description = "window size for NCIS-related scaling types (default=500)")
	private int window = 500;
	@Option(names = {"-m", "--min-fraction"}, description = "minimum fraction for NCIS-related scaling types (default=0.75)")
	private double minFrac = 0.75;
	
	private int scaleType = 1;
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">ScalingFactorCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		System.err.println( "OUTBASE: " + outputBasename );
		
		ScalingFactor script_obj = new ScalingFactor(bamFile, blacklistFilter, controlBAM, outputBasename, scaleType, window, minFrac, true);
		script_obj.run();
		
		System.err.println("Scaling Factor Calculated.");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		//check inputs exist
		if(!bamFile.exists() && bamFile!=null){
			r += "(!)BAM(input) file does not exist: " + bamFile.getName() + "\n";
		}
		if(blacklistFilter!=null){
			if(!blacklistFilter.exists()){ r += "(!)Blacklist file does not exist: " + blacklistFilter.getName() + "\n"; }
		}
		if(controlBAM!=null){
			if(!controlBAM.exists()){ r += "(!)BAM(control) file does not exist: " + controlBAM.getName() + "\n"; }
		}
		if(!r.equals("")){ return(r); }
		//check BAI exists
		File f = new File(bamFile+".bai");
		if(!f.exists() || f.isDirectory()){
			r += "(!)BAI Index File does not exist for: " + bamFile.getName() + "\n";
		}
		if(controlBAM!=null){
			if(!"bam".equals(ExtensionFileFilter.getExtension(controlBAM))){
				r += "(!)Is this a BAM(control) file? Check extension: " + controlBAM.getName() + "\n";
			}
			f = new File(controlBAM+".bai");
			if(!f.exists() || f.isDirectory()){
				r += "(!)BAI Index File does not exist for: " + controlBAM.getName() + "\n";
			}
		}
		//set default output filename
		if(outputBasename==null){
			outputBasename = new File(ExtensionFileFilter.stripExtension(bamFile));
		//check output filename is valid
		} else {
			//no check ext
			//check directory
			if(outputBasename.getParent()==null){
// 				System.err.println("default to current directory");
			} else if(!new File(outputBasename.getParent()).exists()){
				r += "(!)Check output directory exists: " + outputBasename.getParent() + "\n";
			}
		}
		
		//Set numeric indicator for scaling method
		if(scale.ncis) { scaleType = 2; }
		else if(scale.both) { scaleType = 3; }
		
		//NCIS-related methods must have a control file
		if(controlBAM==null && scaleType!=1){
			r += "(!)Control file must be given when using NCIS-related scaling methods. Unused for total-tag scaling.\n";
		}
		
		//validate window
		if(window<1){
			r += "(!)Window must be a positive non-zero integer\n";
		}
		//validate minimum fraction
		if(minFrac>1 || minFrac<0){
			r += "(!)Minimum fraction must be between 0 and 1\n";
		}
		
		return(r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param bamFile the BAM file to calculate a scaling factor for
	 * @param bl      BED formatted blacklist of regions to exclude from the
	 *                calculation
	 * @param c       control BAM file (used by NCIS-style scaling methods)
	 * @param obase   output basename for storing scaling factor and other reference
	 *                data
	 * @param scale   scaling method
	 * @param win     window size (used by NCIS-style scaling methods)
	 * @param min     minimum fraction (used by NCIS-style scaling methods)
	 * @return
	 * @throws OptionException
	 */
	public static String getCLIcommand(File bamFile, File bl, File c, File obase, int scale, int win, double min) throws OptionException {
		String command = "java -jar $SCRIPTMANAGER read-analysis scaling-factor";
		command += " -o " + obase.getAbsolutePath();
		command += bl==null ? "" : " -f " + bl;
		switch (scale) {
			case ScalingFactor.TOTAL_TAG:
				command += " --total-tag";
				break;
			case ScalingFactor.NCIS:
				command += " --ncis";
				command += " -c " + c;
				break;
			case ScalingFactor.NCIS_W_TOTAL_TAG:
				command += " --both";
				command += " -c " + c;
				break;
			default:
				throw new OptionException("invalid scaling type value");
		}
		command += " -w " + win;
		command += " -m " + min;
		command += " " + bamFile;
		return(command);
	}
}