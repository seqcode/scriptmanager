package scriptmanager.cli.Peak_Analysis;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.scripts.Peak_Analysis.TileGenome;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Peak_Analysis.TileGenome}
 * 
 * @author Olivia Lang
 */
@Command(name = "tile-genome", mixinStandardHelpOptions = true,
	description = ToolDescriptions.tile_genome_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class TileGenomeCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "reference genome [sacCer3|sacCer3_cegr|hg38|hg38_contigs|hg19|hg19_contigs|mm10]")
	private String genomeName;
	
	@Option(names = {"-o", "--output"}, description = "Specify output directory (default = current working directory), file name will be genome_tiles_<genomeName>_<window>bp.<ext>")
	private File output = null;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	@Option(names = {"-f", "--gff"}, description = "file format output as GFF (default format as BED)")
	private boolean formatIsBed = true;
	@Option(names = {"-w", "--window"}, description = "window size in bp (default=200)")
	private int window = 200;
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">TileGenomeCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		TileGenome.execute(genomeName, output, formatIsBed, window, gzOutput);
		
		System.err.println( "Genomic Tiling Complete." );
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		//set default output filename
		if (output==null) {
			output = new File(genomeName + "_" + window + "bp"
					+ (formatIsBed ? ".bed" : ".gff")
					+ (gzOutput ? ".gz" : ""));
		//check output filename is valid
		} else {
			//check directory
			if(output.getParent()==null){
	// 			System.err.println("default to current directory");
			} else if(!new File(output.getParent()).exists()){
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}
		
		//check window size
		if( window<1 ){
			r += "(!)Window size needs to be a positive integer.\n";
		}
		
		return(r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param genomeName  the genome build to tile coordinates from
	 * @param output      the text file of the tiled coordinates
	 * @param formatIsBed the format of the coordinate output (BED or GFF)
	 * @param window      the size of the tiles sampled
	 * @param gzOutput    whether or not to gzip output
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(String genomeName, File output, boolean formatIsBed, int window, boolean gzOutput) {
		String command = "java -jar $SCRIPTMANAGER peak-analysis tile-genome";
		command += " " + genomeName;
		command += " -o " + output.getAbsolutePath();
		command += gzOutput ? " -z " : "";
		command += formatIsBed ? "" : " --gff";
		command += " -w " + window;
		return command;
	}
}
