package cli.Coordinate_Manipulation.BED_Manipulation;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import objects.ToolDescriptions;
import util.ExtensionFileFilter;
//import scripts.Coordinate_Manipulation.BED_Manipulation.SortBED;

/**
	Coordinate_ManipulationCLI/SortBEDCLI
*/
@Command(name = "sort-bed", mixinStandardHelpOptions = true,
	description = ToolDescriptions.sort_bed_description,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class SortBEDCLI implements Callable<Integer> {
	
	@Override
	public Integer call() throws Exception {
		System.err.println( ">SortBEDCLI.call()" );
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
