package cli.Peak_Analysis;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import objects.ToolDescriptions;
import util.ExtensionFileFilter;
//import scripts.Peak_Analysis.TileGenome;

/**
	Peak_AnalysisCLI/TileGenomeCLI
*/
@Command(name = "tile-genome", mixinStandardHelpOptions = true,
	description = ToolDescriptions.tile_genome_description,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class TileGenomeCLI implements Callable<Integer> {
	
	@Override
	public Integer call() throws Exception {
		System.err.println( ">TileGenomeCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		//SEStats.getSEStats( output, bamFile, null );
		
		//System.err.println("Calculations Complete");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		//validate input here
		//append messages to the user to `r`
		return(r);
	}
}
