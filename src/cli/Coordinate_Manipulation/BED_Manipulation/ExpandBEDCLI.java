package cli.Coordinate_Manipulation.BED_Manipulation;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import objects.ToolDescriptions;
import util.ExtensionFileFilter;
//import scripts.Coordinate_Manipulation.BED_Manipulation.ExpandBED;

/**
	Coordinate_ManipulationCLI/ExpandBEDCLI
*/
@Command(name = "expand-bed", mixinStandardHelpOptions = true,
	description = ToolDescriptions.expand_bed_description,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class ExpandBEDCLI implements Callable<Integer> {
	
	@Override
	public Integer call() throws Exception {
		System.err.println( ">ExpandBEDCLI.call()" );
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
