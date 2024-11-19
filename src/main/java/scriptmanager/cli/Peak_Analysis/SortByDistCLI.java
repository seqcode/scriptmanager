package scriptmanager.cli.Peak_Analysis;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Peak_Analysis.SortByDist;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Peak_Analysis.SortByDist}
 * 
 * @author Ben Beer
 */
@Command(name = "sort-by-dist", mixinStandardHelpOptions = true,
	description = ToolDescriptions.sort_by_dist_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class SortByDistCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "The coordinate peak file")
	private File peak = null;
	@Parameters( index = "1", description = "The coordinate reference file")
	private File ref = null;
	
	@Option(names = {"-o", "--output"}, description = "Specify output file (default = <peak>_<ref>_Output.bed/gff)")
	private File output = null;
	@Option(names = {"-u"}, description = "Restrict search to exclude peaks upstream of this distance (default = no bound)")
	private Long upstreamBound = null;
	@Option(names = {"-d"}, description = "Restrict search to exclude peaks downstream of this distance (default = no bound)")
	private Long downstreamBound = null;
	@Option(names = {"-m", "--match-strand"}, description = "only check peaks with strand matching reference" )
	private boolean matchStrand = false;
	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)" )
	private boolean gzOutput = false;
	@Option(names = {"--gff"}, description = "input is GFF format (default=BED format)")
	private boolean isGFF = false;
	
	@Override
	public Integer call() throws Exception {
		System.err.println( ">SortByDistCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		// Execute script
		SortByDist script_obj = new SortByDist(ref, peak, output, gzOutput, matchStrand, upstreamBound, downstreamBound, null);
		if (isGFF) {
			script_obj.sortGFF();
		} else { 
			script_obj.sortBED();
		}
		
		System.err.println( "Sorting Complete." );
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";

		//check inputs exist
		if (!peak.exists()) {
			r += "(!)Coordinate-peak file does not exist: " + peak.getName() + "\n";
		}
		if (!ref.exists()) {
			r += "(!)Coordinate-ref file does not exist: " + ref.getName() + "\n";
		}
		if (!r.equals("")) { return(r); }
		//set default output filename
		if (output==null) {
			output = new File(ExtensionFileFilter.stripExtension(peak) + "_" + ExtensionFileFilter.stripExtension(ref) + "_DistSort." + ExtensionFileFilter.getExtensionIgnoreGZ(ref));
		//check output filename is valid
		} else {
			//check directory
			if (output.getParent()==null) {
// 				System.err.println("default to current directory");
			} else if (!new File(output.getParent()).exists()) {
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}

		//check bounds
		if (upstreamBound != null && downstreamBound != null) {
			r += upstreamBound > downstreamBound ? "(!)Check that upstream bound isn't greater than downstream bound" : "";
		}

		return(r);
	}

	public static String getCLIcommand(File ref, File peak, File out, boolean gff, boolean gzOutput, boolean mstrand, Long upstream, Long downstream){
		String command = "java -jar $SCRIPTMANAGER peak-analysis sort-by-dist";
		command += gff ? " --gff": "";
		command += gzOutput ? " -z": "";
		command += mstrand ? " -m": "";
		command += upstream == null ? "": " -u " + upstream;
		command += downstream == null ? "": " -d " + downstream;
		command += " -o " + out.getAbsolutePath();
		command += " " + peak.getAbsolutePath();
		command += " " + ref.getAbsolutePath();
		return command;
	}
}
