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
 * Command line interface class for converting chromsome names of BED file by
 * calling method implemented in the scripts package.
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.File_Utilities.ConvertChrNames
 */
@Command(name = "convert-bed-genome", mixinStandardHelpOptions = true,
	description = ToolDescriptions.convertBEDChrNamesDescription,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class ConvertBEDChrNamesCLI implements Callable<Integer> {

	@Parameters( index = "0", description = "the BED coordinate file to convert")
	private File coordFile;

	@Option(names = {"-o", "--output"}, description = "specify output directory (name will be same as original with .bed ext)")
	private File output = null;

	@Option(names = {"-a", "--to-arabic"}, description = "switch converter to output arabic numeral chromsome names (default outputs roman numeral chrnames)")
	private boolean toArabic = false;
	
	@Option(names = {"-m", "--chrmt"}, description = "converter will map \"chrM\" --> \"chrmt\" (default with no flag is \"chrmt\" --> \"chrM\")")
	private boolean useChrmt = false;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;

	@Override
	public Integer call() throws Exception {
		System.err.println( ">ConvertBEDChrNamesCLI.call()" );
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
	 * Validate the input values before executing the script.
	 * 
	 * @return a multi-line string describing input validation issues
	 * @throws IOException
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
			// Set suffix format
			String SUFFIX = toArabic ? "_toRoman.gff" : "_toArabic.gff";
			SUFFIX += gzOutput ? ".gz" : "";
			// Set output filepath with name and output directory
			String OUTPUT = ExtensionFileFilter.stripExtension(coordFile);
			// Strip second extension if input has ".gz" first extension
			if (coordFile.getName().endsWith(".gff.gz")) {
				OUTPUT = ExtensionFileFilter.stripExtensionPath(new File(OUTPUT)) ;
			}
			output = new File(OUTPUT + SUFFIX);
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
}