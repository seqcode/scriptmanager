package scriptmanager.cli.Peak_Analysis;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Peak_Analysis.FilterBEDbyProximity;
	
/**
 * Command line interface for
 * {@link scriptmanager.scripts.Peak_Analysis.FilterBEDbyProximity}
 * 
 * @author Olivia Lang
 */
@Command(name = "filter-bed", mixinStandardHelpOptions = true,
	description = ToolDescriptions.filter_bed_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class FilterBEDbyProximityCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "The BED file we are filtering on")
	private File bedFile;
	
	@Option(names = {"-o", "--output"}, description = "Specify basename for output files (default = <bedFilename>_<exclusionNum>bp)")
	private File outputBasename = null;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	@Option(names = {"-e", "--exclusion"}, description = "exclusion distance in bp (default=100)")
	private int exclusion = 100;
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">FilterBEDbyProximityCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		FilterBEDbyProximity script_obj = new FilterBEDbyProximity(bedFile, outputBasename, exclusion, null, gzOutput);
		script_obj.run();
		
		System.err.println( "Filter Complete." );
		return(0);
	}

	/**
	 * Validate the input values before executing the script.
	 * 
	 * @return a multi-line string describing input validation issues
	 * @throws IOException
	 */
	private String validateInput() throws IOException {
		String r = "";
		
		//check inputs exist
		if(!bedFile.exists()){
			r += "(!)BED file does not exist: " + bedFile.getName() + "\n";
			return(r);
		}
		//set default output filename
		if (outputBasename==null) {
			outputBasename = new File(ExtensionFileFilter.stripExtension(bedFile) + "_" + Integer.toString(exclusion) + "bp");
		//check output filename is valid
		}else{
			//check directory
			if (outputBasename.getParent()==null){
	// 			System.err.println("default to current directory");
			} else if(! new File(outputBasename.getParent()).exists()){
				r += "(!)Check output directory exists: " + outputBasename.getParent() + "\n";
			}
		}
		
		//check exclusion
		if(exclusion<0){
			r += "(!)Exclusion size needs to be a positive integer.\n";
		}
		
		return(r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input the BED file to filter using an exclusion distance
	 * @param outputBasename the basename for output BED-formatted *-FILTER.bed and *-CLUSTER.bed files
	 * @param exclusion the number of bp distance to filter by/size of exclusion zone
	 * @param gzOutput   whether or not to gzip output
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input, File output, int exclusion, boolean gzOutput) {
		String command = "java -jar $SCRIPTMANAGER peak-analysis filter-bed";
		command += " " + input.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();
		command += gzOutput ? " -z " : "";
		command += " -e " + exclusion;
		return command;
	}
}
