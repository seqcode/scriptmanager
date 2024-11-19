package scriptmanager.cli.File_Utilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.File_Utilities.ConvertChrNames;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.File_Utilities.ConvertChrNames}
 * 
 * @author Olivia Lang
 */
@Command(name = "convert-gff-genome", mixinStandardHelpOptions = true,
	description = ToolDescriptions.convertGFFChrNamesDescription,
	version = "ScriptManager " + ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class ConvertGFFChrNamesCLI implements Callable<Integer> {

	/**
	 * Creates a new ConvertGFFChrNamesCLI object
	 */
	public ConvertGFFChrNamesCLI(){}

	@Parameters( index = "0", description = "the GFF coordinate file to convert")
	private File coordFile;

	@Option(names = {"-o", "--output"}, description = "specify output directory (name will be same as original with .gff ext)")
	private File output = null;

	@Option(names = {"-a", "--to-arabic"}, description = "switch converter to output arabic numeral chromsome names (default outputs roman numeral chrnames)")
	private boolean toArabic = false;
	
	@Option(names = {"-m", "--chrmt"}, description = "converter will map \"chrM\" --> \"chrmt\" (default with no flag is \"chrmt\" --> \"chrM\")")
	private boolean useChrmt = false;
	
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">ConvertGFFChrNamesCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		// call method according to conversion direction
		if (toArabic) {
			ConvertChrNames.convert_RomantoArabic(coordFile, output, useChrmt, gzOutput);
		} else {
			ConvertChrNames.convert_ArabictoRoman(coordFile, output, useChrmt, gzOutput);
		}

		System.err.println("Conversion Complete");
		return(0);
	}

	/**
	 * Validate the input values before executing the script
	 * 
	 * @return a multi-line string describing input validation issues
	 * @throws IOException Invalid file or parameters
	 */
	private String validateInput() throws IOException {
		String r = "";

		//check inputs exist
		if(!coordFile.exists()){
			r += "(!)Coordinate file does not exist: " + coordFile.getName() + "\n";
			return(r);
		}
		//set default output filename
		if (output == null) {
			// Set output filepath with name and output directory
			output = new File(ExtensionFileFilter.stripExtensionIgnoreGZ(coordFile)
					+ (toArabic ? "_toArabic.gff" : "_toRoman.gff")
					+ (gzOutput ? ".gz" : ""));
		} else {
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
	 * Reconstruct CLI command
	 * 
	 * @param RtoA whether to do a roman to arabic numeral conversion (vs arabic to roman numeral)
	 * @param input the GFF file to convert chr names of
	 * @param output the output BED file of converted coords
	 * @param useChrmt whether or not to use "chrmt"
	 * @param gzOutput gzip output
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(boolean RtoA, File input, File output, boolean useChrmt, boolean gzOutput) {
		String command = "java -jar $SCRIPTMANAGER file-utilities convert-gff-genome";
		command += RtoA ? " --to-arabic" : "";
		command += " -o " + output.getAbsolutePath();
		command += useChrmt ? " --chrmt" : "";
		command += gzOutput ? " --gzip" : "";
		command += " " + input.getAbsolutePath();
		return command;
	}
}
