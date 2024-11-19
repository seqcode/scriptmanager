package scriptmanager.cli.Coordinate_Manipulation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Coordinate_Manipulation.ShiftCoord;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Coordinate_Manipulation.ShiftCoord}
 * 
 * @author Olivia Lang
 */
@Command(name = "shift-coord", mixinStandardHelpOptions = true,
	description = ToolDescriptions.shift_coordinate_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class ShiftCoordCLI implements Callable<Integer> {

	@Parameters( index = "0", description = "the coordinate file (BED/GFF format) to shift")
	private File input;

	@Option(names = {"-o", "--output"}, description = "specify output filepath (default input filename with _shiftXXXbp.bed/gff)")
	private String outputFilepath = null;
	@Option(names = {"-t", "--shift"}, description = "shift distance in bp, upstream  < 0 and downstream > 0 (default=0)")
	private int shift = 0;
	@Option(names = {"-u", "--unstranded"}, description = "don't force strandedness (default=forced)")
	private boolean stranded = true;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	@Option(names = {"--gff"}, description = "input is GFF format (default=BED format)")
	private boolean isGFF = false;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">ShiftCoord.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		if(isGFF) {
			ShiftCoord.shiftGFFInterval(input, new File(outputFilepath), shift, stranded, gzOutput);
		} else {
			ShiftCoord.shiftBEDInterval(input, new File(outputFilepath), shift, stranded, gzOutput);
		}

		System.err.println("Shift Complete");
		return(0);
	}

	private String validateInput() throws IOException {
		String r = "";

		//check inputs exist
		if(!input.exists()){
			r += "(!)BED/GFF file does not exist: " + input.getName() + "\n";
		}
		if(!"".equals(r)){ return(r); }

		//set default output filename
		if(outputFilepath==null){
			outputFilepath = ExtensionFileFilter.stripExtensionIgnoreGZ(input)
					+ (shift < 0 ? "_shift" + shift + "bp." : "_shift+" + shift + "bp")
					+ (isGFF ? ".gff" : ".bed")
					+ (gzOutput ? ".gz" : "");
		//check output filename is valid
		}else{
			//no extension check
			//check directory
			File BASEFILE = new File(outputFilepath);
			if(BASEFILE.getParent()==null){
// 				System.err.println("default to current directory");
			} else if(!new File(BASEFILE.getParent()).exists()){
				r += "(!)Check output directory exists: " + BASEFILE.getParent() + "\n";
			}
		}
		return(r);
	}

	public static String getCLIcommand(File input, File output, int shift, boolean stranded, boolean gzOutput, boolean isGFF) {
		String command = "java -jar $SCRIPTMANAGER coordinate-manipulation shift-coord";
		command += " " + input.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();
		command += " -t " + shift;
		command += stranded ? " -u " : "";
		command += isGFF ? " --gff " : "";
		command += gzOutput ? " -z " : "";
		return command;
	}
}
