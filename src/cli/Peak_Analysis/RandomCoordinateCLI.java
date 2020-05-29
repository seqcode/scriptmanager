package cli.Peak_Analysis;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import util.ExtensionFileFilter;
import scripts.Peak_Analysis.RandomCoordinate;

/**
	Peak_AnalysisCLI/RandomCoordinateCLI
*/
@Command(name = "rand-coord", mixinStandardHelpOptions = true,
		description = "Generate a coordinate file that tiles (non-overlapping) across an entire genome.",
		sortOptions = false)
public class RandomCoordinateCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "reference genome [sacCer3_cegr|hg19|hg19_contigs|mm10]")
	private String genome;
	
	@Option(names = {"-o", "--output"}, description = "specify output directory (name will be random_coordinates_<genome>_<window>bp.<ext>)")
	private File output = null;
	@Option(names = {"-f", "--gff"}, description = "file format output as GFF (default format as BED)")
	private boolean formatIsBed = true;
	@Option(names = {"-n", "--num-sites"}, description = "number of sites (default=1000)")
	private int numSites = 1000;
	@Option(names = {"-w", "--window"}, description = "window size in bp (default=200)")
	private int window = 200;
	
	@Override
	public Integer call() throws Exception {
		System.err.println( ">RandomCoordinateCLI.call()" );
		String validate = validateInput();
		if( validate.compareTo("")!=0 ){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			return(1);
		}
		
		RandomCoordinate script_obj = new RandomCoordinate(genome, numSites, window, formatIsBed, output); 
		script_obj.execute();
		
		System.err.println( "Random Coordinate Generation Complete." );
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		//check input genomes are valid
		if(genome.compareTo("sacCer3_cegr")==0 || genome.compareTo("hg19")==0 || genome.compareTo("hg19_contigs")==0 || genome.compareTo("mm10")==0 ){
// 			System.err.println("Input genome is valid");
		}else{
			r += "(!)Invalid genome selected(" +genome+ "), please select from one of the provided genomes: sacCer3_cegr, hg19, hg19_contigs, and mm10\n";
		}
		//set default output filename
		if(output==null){
			String NAME = "random_coordinates_" + genome + "_" + numSites + "sites_" + window + "bp";
			if(formatIsBed){ output = new File(NAME + ".bed"); }
			else{ output = new File(NAME + ".gff"); }
		//check output filename is valid
		}else{
			String ext = "gff";
			if(formatIsBed){ ext = "bed"; }
			//check ext
			try{
				if(ext.compareTo(ExtensionFileFilter.getExtension(output))!=0){
					r += "(!)Use \"." + ext + "\" extension for output filename. Try: " + ExtensionFileFilter.stripExtension(output) + "." + ext + "\n";
				}
			} catch( NullPointerException e){ r += "(!)Output filename must have extension: use \"." + ext + "\" extension for output filename. Try: " + output + "." + ext + "\n"; }
			//check directory
			if(output.getParent()==null){
	// 			System.err.println("default to current directory");
			} else if(!new File(output.getParent()).exists()){
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}
		
		//check number of sites
		if( numSites<1 ){
			r += "(!)Number of sites needs to be a positive integer.\n";
		}
		//check window size
		if( window<1 ){
			r += "(!)Window size needs to be a positive integer.\n";
		}
		
		return(r);
	}
}