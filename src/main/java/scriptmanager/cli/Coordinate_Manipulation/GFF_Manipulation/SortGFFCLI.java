package scriptmanager.cli.Coordinate_Manipulation.GFF_Manipulation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.CDTUtilities;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.SortGFF;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.SortBED}
 * 
 * @author Olivia Lang
 */
@Command(name = "sort-gff", mixinStandardHelpOptions = true,
	description = ToolDescriptions.sort_gff_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class SortGFFCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "the GFF file to sort")
	private File gffFile;
	@Parameters( index = "1", description = "the reference CDT file to sort the input by")
	private File cdtFile;
	
	@Option(names = {"-o", "--output"}, description = "specify output file basename with no .cdt/.gff/.jtv extension (default=<gffFile>_SORT")
	private File outputBasename = null;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	@Option(names = {"-c", "--center"}, description = "sort by center on the input size of expansion in bins (default=100)")
	private int center = -999;
	@Option(names = {"-x", "--index"}, description = "sort by index from the specified start to the specified stop (0-indexed and half-open interval)",
		arity = "2")
	private int[] index = {-999, -999};
	
	private int CDT_SIZE = -999;
	private boolean byCenter = false;
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">SortGFFCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		if( byCenter ){
			index[0] = (CDT_SIZE / 2) - (center / 2);
			index[1] = (CDT_SIZE / 2) + (center / 2);
		}
		
		SortGFF.sortGFFbyCDT(gffFile, cdtFile, outputBasename, index[0], index[1], gzOutput);
		
		System.err.println("Sort Complete");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		//check inputs exist
		if(!gffFile.exists()){
			r += "(!)GFF file does not exist: " + gffFile.getName() + "\n";
		}
		if(!cdtFile.exists()){
			r += "(!)CDT file does not exist: " + cdtFile.getName() + "\n";
		}
		if(!"".equals(r)){ return(r); }
		//check input extensions
		if(!"gff".equals(ExtensionFileFilter.getExtensionIgnoreGZ(gffFile))){
			r += "(!)Is this a GFF file? Check extension: " + gffFile.getName() + "\n";
		}
		if(!"cdt".equals(ExtensionFileFilter.getExtensionIgnoreGZ(cdtFile))){
			r += "(!)Is this a CDT file? Check extension: " + cdtFile.getName() + "\n";
		}
		// validate CDT as file, with consistent row size, and save row_size value
		try {
			CDTUtilities cdt_obj = new CDTUtilities();
			cdt_obj.parseCDT(cdtFile);
			if( cdt_obj.isValid() ){ CDT_SIZE = cdt_obj.getSize(); }
			else{ r += "(!)CDT file doesn't have consistent row sizes. " + cdt_obj.getInvalidMessage(); }
		}catch (FileNotFoundException e1){ e1.printStackTrace(); }
		
		//set default output filename
		if(outputBasename==null){
			outputBasename = new File(ExtensionFileFilter.stripExtensionIgnoreGZ(gffFile) + "_SORT");
		//check output filename is valid
		}else{
			//no extension check
			//check directory
			if(outputBasename.getParent()==null){
// 				System.err.println("default to current directory");
			} else if(!new File(outputBasename.getParent()).exists()){
				r += "(!)Check output directory exists: " + outputBasename.getParent() + "\n";
			}
		}
		
		// Set Center if Index not given
		if( index[0]==-999 && index[1]==-999 ) { byCenter = true; }
		// Center Specified
		if( byCenter ){
			if( center==-999 ){ center = 100; }
			else if( center<0 ){
				r += "(!)Invalid --center input, must be a positive integer value.";
			}
		// Index Specified
		}else{
			if( index[0]<0 || index[1]>CDT_SIZE || index[0]>index[1] ){
				r += "(!)Invalid --index value input, check that start>0, stop<CDT row size, and start<stop.";
			}
		}
		
		return(r);
	}
	public static String getCLIcommand(File BED, File CDT, File OUTPUT, int startidx, int stopidx, boolean gzOutput) {
		String command = "java -jar $SCRIPTMANAGER coordinatesort-manipulation sort-gff";
		command += " " + BED.getAbsolutePath();
		command += " " + CDT.getAbsolutePath();
		command += " -x " + startidx + " " + stopidx;
		command += " -o " + OUTPUT.getAbsolutePath();
		command += gzOutput ? " -z " : "";
		return command;
	}
}