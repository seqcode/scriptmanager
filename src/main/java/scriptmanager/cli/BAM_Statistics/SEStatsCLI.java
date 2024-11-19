package scriptmanager.cli.BAM_Statistics;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.BAM_Statistics.SEStats;
	

/**
 * Command line interface for
 * {@link scriptmanager.scripts.BAM_Statistics.SEStats}
 * 
 * @author Olivia Lang
 */
@Command(name = "se-stat", mixinStandardHelpOptions = true,
	description = ToolDescriptions.se_stat_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class SEStatsCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "The BAM file whose statistics we want.")
	private File bamFile;
	
	@Option(names = {"-o", "--output"}, description = "Specify output file ")
	private File output;
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">SEStatsCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		SEStats.getSEStats(bamFile, output, true, null);
		
		System.err.println("Calculations Complete");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		//check inputs exist
		if(!bamFile.exists()){
			r += "(!)BAM file does not exist: " + bamFile.getName() + "\n";
			return(r);
		}
		//check BAI exists
		File f = new File(bamFile+".bai");
		if(!f.exists() || f.isDirectory()){
			r += "(!)BAI Index File does not exist for: " + bamFile.getName() + "\n";
		}
		//set default output filename
		if(output==null){
// 			output = new File("output_bam_stats.txt");		//this default name mimics the gui
			output = new File(ExtensionFileFilter.stripExtension(bamFile) + "_stats.txt");
		//check output filename is valid
		}else{
			//check directory
			if(output.getParent()==null){
// 				System.err.println("default to current directory");
			} else if(!new File(output.getParent()).exists()){
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}
	
		return(r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param bamFile the BAM file to get statistics on (from header)
	 * @param output  text file to write output to
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File bamFile, File output) {
		String command = "java -jar $SCRIPTMANAGER bam-statistics se-stats";
		command += " " + bamFile.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();
		return command;
	}
}