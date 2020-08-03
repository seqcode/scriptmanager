package cli.Coordinate_Manipulation.GFF_Manipulation;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import util.ExtensionFileFilter;
import scripts.Coordinate_Manipulation.GFF_Manipulation.SortGFF;

/**
	Coordinate_ManipulationCLI/SortGFFCLI
*/
@Command(name = "sort-gff", mixinStandardHelpOptions = true,
	description = "Sort a CDT file and its corresponding GFF file by the total score in the CDT file across the specified interval",
	sortOptions = false)
public class SortGFFCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "the GFF file to sort")
	private File gffFile;
	@Parameters( index = "1", description = "the reference CDT file to sort the input by")
	private File cdtReference;
	
	@Option(names = {"-o", "--output"}, description = "specify output file basename (no .cdt/.gff extension, script will add that)")
	private String outputBasename = null;
	@Option(names = {"-c", "--center"}, description = "sort by center on the input size of expansion in bins (default=100)")
	private int center = -999;
	@Option(names = {"-x", "--index"}, description = "sort by index from the specified start to the specified stop (0-indexed and half-open interval)",
		arity = "2")
	private int[] index = {-999, -999};
	
	private int CDT_SIZE;
	private boolean byCenter;
	
	@Override
	public Integer call() throws Exception {
		System.err.println( ">SortGFFCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			return(1);
		}
		
		if( byCenter ){
			index[0] = (CDT_SIZE / 2) - (center / 2);
			index[1] = (CDT_SIZE / 2) + (center / 2);
		}
		
		try{
			SortGFF.sortGFFbyCDT(outputBasename, gffFile, cdtReference, index[0], index[1]);
			System.err.println("Sort Complete");
		} catch (FileNotFoundException e) {
			System.err.println("Check your filenames!");
			e.printStackTrace();
		}
		
		System.err.println("Expansion Complete");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		//check inputs exist
		if(!gffFile.exists()){
			r += "(!)GFF file does not exist: " + gffFile.getName() + "\n";
		}
		if(!cdtReference.exists()){
			r += "(!)CDT file does not exist: " + cdtReference.getName() + "\n";
		}
		if(!"".equals(r)){ return(r); }
		//check input extensions
		if(!"gff".equals(ExtensionFileFilter.getExtension(gffFile))){
			r += "(!)Is this a GFF file? Check extension: " + gffFile.getName() + "\n";
		}
		if(!"cdt".equals(ExtensionFileFilter.getExtension(cdtReference))){
			r += "(!)Is this a CDT file? Check extension: " + cdtReference.getName() + "\n";
		}
		// validate CDT as file, with consistent row size, and save row_size value
		try {
			CDT_SIZE = SortGFF.parseCDTFile(cdtReference);
		}catch (FileNotFoundException e1){ e1.printStackTrace(); }
		if(CDT_SIZE==-999){
			r += "(!)CDT file doesn't have consistent row sizes.";
		}
		//set default output filename
		if(outputBasename==null){
			outputBasename = ExtensionFileFilter.stripExtension(gffFile) + "_SORT";
		//check output filename is valid
		}else{
			//no extension check
			//check directory
			File BASEFILE = new File(outputBasename);
			if(BASEFILE.getParent()==null){
// 				System.err.println("default to current directory");
			} else if(!new File(BASEFILE.getParent()).exists()){
				r += "(!)Check output directory exists: " + BASEFILE.getParent() + "\n";
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
}