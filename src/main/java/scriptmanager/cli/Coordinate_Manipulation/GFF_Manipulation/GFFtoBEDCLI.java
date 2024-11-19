package scriptmanager.cli.Coordinate_Manipulation.GFF_Manipulation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.GFFtoBED;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.GFFtoBED}
 * 
 * @author Olivia Lang
 */
@Command(name = "gff-to-bed", mixinStandardHelpOptions = true,
	description = ToolDescriptions.gff_to_bed_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class GFFtoBEDCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "the GFF file to convert")
	private File gffFile;
	
	@Option(names = {"-o", "--output"}, description = "specify output filename (name will be same as original with .bed ext)")
	private File output = null;
	@Option(names = {"-s", "--stdout"}, description = "output bed to STDOUT")
	private boolean stdout = false;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">GFFtoBEDCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		GFFtoBED.convertGFFtoBED(gffFile, output, gzOutput);
		
		System.err.println("Conversion Complete");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		//check inputs exist
		if(!gffFile.exists()){
			r += "(!)GFF file does not exist: " + gffFile.getName() + "\n";
			return(r);
		}
		//set default output filename
		if(output==null && !stdout){
			output = new File(ExtensionFileFilter.stripExtensionIgnoreGZ(gffFile) + ".bed");
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

	public static String getCLIcommand(File GFF, File output, boolean gzOutput) {
		String command = "java -jar  $SCRIPTMANAGER coordinate-manipulation gff-to-bed";
		command += " " + GFF.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();
		command += gzOutput ? " -z " : "";
		return command;
	}
}
