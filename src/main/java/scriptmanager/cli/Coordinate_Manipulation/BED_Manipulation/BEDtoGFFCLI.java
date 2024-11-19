package scriptmanager.cli.Coordinate_Manipulation.BED_Manipulation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.BEDtoGFF;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.BEDtoGFF}
 * 
 * @author Olivia Lang
 */
@Command(name = "bed-to-gff", mixinStandardHelpOptions = true,
	description = ToolDescriptions.bed_to_gff_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class BEDtoGFFCLI implements Callable<Integer> {
	/**
	 * Creates a new BEDtoGFFCLI object
	 */
	public BEDtoGFFCLI(){}

	@Parameters( index = "0", description = "the BED file to convert")
	private File bedFile;
	
	@Option(names = {"-o", "--output"}, description = "specify output filename (name will be same as original with .gff ext)")
	private File output = null;
	@Option(names = {"-s", "--stdout"}, description = "output gff to STDOUT")
	private boolean stdout = false;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">BEDtoGFFCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		BEDtoGFF.convertBEDtoGFF(bedFile, output, gzOutput);
		
		System.err.println("Conversion Complete");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		//check inputs exist
		if(!bedFile.exists()){
			r += "(!)BED file does not exist: " + bedFile.getName() + "\n";
			return(r);
		}
		//set default output filename
		if(output==null && !stdout){
			String NAME = ExtensionFileFilter.stripExtensionIgnoreGZ(bedFile) + ".gff";
			NAME += gzOutput ? ".gz" : "";
			output = new File(NAME);
		//check stdout and output not both selected
		}else if(stdout){
			if(output!=null){ r += "(!)Cannot use -s flag with -o.\n"; }
		//check output filename is valid
		}else{
			//check directory
			if(output.getParent()==null){
	// 			System.err.println("default to current directory");
			} else if(!new File(output.getParent()).exists()){
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}
		
		return(r);
	}

	/**
	 * Returns the CLI command for replicating results with Script Manager
	 * @param BED Bed file to be converted
	 * @param OUTPUT Output GFF file
	 * @param gzOutput   whether or not to gzip output
	 * @return The CLI command for replicating results
	 */
	public static String getCLIcommand(File BED, File OUTPUT, boolean gzOutput) {
		String command = "java -jar $SCRIPTMANAGER coordinate-manipulation bed-to-gff";
		command += " " + BED.getAbsolutePath();
		command += " -o " + OUTPUT;
		command += gzOutput ? " -z " : "";
		return(command);
	}
}
