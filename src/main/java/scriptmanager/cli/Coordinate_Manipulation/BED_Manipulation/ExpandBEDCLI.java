package scriptmanager.cli.Coordinate_Manipulation.BED_Manipulation;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.ExpandBED;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.ExpandBED}
 * 
 * @author Olivia Lang
 */
@Command(name = "expand-bed", mixinStandardHelpOptions = true,
	description = ToolDescriptions.expand_bed_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class ExpandBEDCLI implements Callable<Integer> {
	/**
	 * Creates a new ExpandBEDCLI object
	 */
	public ExpandBEDCLI(){}
	
	@Parameters( index = "0", description = "the BED file to expand on")
	private File bedFile;

	@Option(names = {"-o", "--output"}, description = "specify output filename (name will be same as original with coordinate info appended)")
	private File output = null;
	@Option(names = {"-s", "--stdout"}, description = "output bed to STDOUT")
	private boolean stdout = false;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;

	@ArgGroup(validate = false, heading = "%nType of Expansion%n")
	ExpandType expandType = new ExpandType();
	static class ExpandType {
		@Option(names = {"-c", "--center"}, description = "expand from center (default, at 250bp)")
		private int center = -999;
		@Option(names = {"-b", "--border"}, description = "add to border")
		private int border = -999;
	}

	private boolean byCenter = true;
	private int SIZE = 250;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">ExpandBEDCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		ExpandBED.expandBEDBorders(bedFile, output, SIZE, byCenter, gzOutput);

		System.err.println("Expansion Complete");
		return(0);
	}

	private String validateInput() throws IOException {
		String r = "";

		//check inputs exist
		if(!bedFile.exists()){
			r += "(!)BED file does not exist: " + bedFile.getName() + "\n";
			return(r);
		}
		if(!"".equals(r)){ return(r); }

		// Define default behavior
		if(expandType.center==-999 && expandType.border==-999){
			SIZE = 250;
			byCenter = true;
		}else if(expandType.border==-999){
			SIZE = expandType.center;
			byCenter = true;
		}else if(expandType.center==-999){
			SIZE = expandType.border;
			byCenter = false;
		}else{
			r += "(!) Both center and border are flagged. This should have been caught by Picocli.";
		}
		//check size of expansion is valid
		if(SIZE<=0){
			r += "(!) Invalid size input. Must be a positive integer greater than 0.";
		}

		//check stdout and gzip not both selected
		if (stdout && gzOutput) {
			r += "(!) Cannot use -s flag with -z.\n";
		}
		//set default output filename
		if (output == null) {
			if (!stdout) {
				String NAME = ExtensionFileFilter.stripExtension(bedFile);
				NAME += byCenter ? "_" + Integer.toString(SIZE) + "bp.bed" : "_border_" + Integer.toString(SIZE) + "bp.bed";
				output = new File(NAME);
			//check stdout and output not both selected
			} else {
				r += "(!) Cannot use -s flag with -o.\n";
			}
		} else {
			//check directory
			if (output.getParent() != null) {
				if (!new File(output.getParent()).exists()) {
					r += "(!) Check output directory exists: " + output.getParent() + "\n";
				}
			}
		}

		return(r);
	}
	public static String getCLIcommand(File input, File output, int size, boolean byCenter, boolean gzOutput) {
		String command = "java -jar $SCRIPTMANAGER coordinate-manipulation expand-bed";
		command += " " + input.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();
		command += gzOutput ? " -z " : "";
		command += byCenter ? " -c " + size : " -b " + size;
		return command;
	}
}
