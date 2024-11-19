package scriptmanager.cli.BAM_Format_Converter;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.BAM_Format_Converter.BAMtobedGraph;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.BAM_Format_Converter.BAMtobedGraph}
 * 
 * @author Olivia Lang
 */
@Command(name = "bam-to-bedgraph", mixinStandardHelpOptions = true,
	description = ToolDescriptions.bam_to_bedgraph_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class BAMtobedGraphCLI implements Callable<Integer> {

	/**
	 * Creates a new BAMtobedGraphCLI object
	 */
	public BAMtobedGraphCLI(){}
	
	@Parameters( index = "0", description = "The BAM file from which we generate a new file.")
	private File bamFile;
	
	@Option(names = {"-o", "--output"}, description = "specify output directory (name will be same as original with _<strand>.bedgraph ext)" )
	private String outputBasename = null;

	@Option(names = {"-z", "--gzip"}, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	
	//Read
	@ArgGroup(exclusive = true, multiplicity = "0..1", heading = "%nSelect Read to output:%n\t@|fg(red) (select no more than one of these options)|@%n")
	ReadType readType = new ReadType();
	static class ReadType {
		@Option(names = {"-1", "--read1"}, description = "output read 1 (default)")
		boolean read1 = false;
		@Option(names = {"-2", "--read2"}, description = "output read 2")
		boolean read2 = false;
		@Option(names = {"-a", "--all-reads"}, description = "output combined")
		boolean combined = false;
		@Option(names = {"-m", "--midpoint"}, description = "output midpoint (require PE)")
		boolean midpoint = false;
	}
	
	@Option(names = {"-p", "--mate-pair"}, description = "require proper mate pair (default not required)")
	private boolean matePair = false;
	@Option(names = {"-n", "--min-insert"}, description = "filter by min insert size in bp")
	private int MIN_INSERT = -9999;
	@Option(names = {"-x", "--max-insert"}, description = "filter by max insert size in bp")
	private int MAX_INSERT = -9999;	
	
	private int STRAND = -9999;
	private int PAIR;
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println( ">BAMtobedGraphCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		
		BAMtobedGraph script_obj = new BAMtobedGraph(bamFile, outputBasename, STRAND, PAIR, MIN_INSERT, MAX_INSERT, null, gzOutput);
		script_obj.run();
		
		System.err.println("Conversion Complete");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		// set strand method
		if(readType.read1)			{ STRAND=0; }
		else if(readType.read2)		{ STRAND=1; }
		else if(readType.combined)	{ STRAND=2; }
		else if(readType.midpoint)	{ STRAND=3; }
		else						{ STRAND=0; }
		// set PE defaults
		if(STRAND==3) { matePair=true; }
		
		//check inputs exist
		if(!bamFile.exists()){
			r += "(!)BAM file does not exist: " + bamFile.getName() + "\n";
			return(r);
		}
		//check BAI exists
		File f = new File(bamFile+".bai");
		if(!f.exists() || f.isDirectory()){
			r += "(!)BAI Index File does not exist for: " + bamFile.getName() + "\n";
		}
		//set default output filename
		if(outputBasename==null){
			if(STRAND==0){ outputBasename = bamFile.getName().split("\\.")[0] + "_READ1"; }
			else if(STRAND==1){ outputBasename = bamFile.getName().split("\\.")[0] + "_READ2"; }
			else if(STRAND==2){ outputBasename = bamFile.getName().split("\\.")[0] + "_COMBINED"; }
			else if(STRAND==3){ outputBasename = bamFile.getName().split("\\.")[0] + "_MIDPOINT"; }
			else if(STRAND==4){ outputBasename = bamFile.getName().split("\\.")[0] + "_FRAGMENT"; }
			else { r += "(!)Somehow invalid STRAND!This error should never print. Check code if it does.\n"; }
		//check output filename is valid
		}else{
			//no check ext
			//check directory
			File tmpOut = new File(outputBasename);
			if(tmpOut.getParent()==null){
	// 			System.err.println("default to current directory");
			} else if(!tmpOut.getParentFile().exists()){
				r += "(!)Check output directory exists: " + tmpOut.getParent() + "\n";
			}
		}
		
		// validate insert sizes
		if( MIN_INSERT<0 && MIN_INSERT!=-9999 ){ r += "MIN_INSERT must be a positive integer value: " + MIN_INSERT + "\n"; }
		if( MAX_INSERT<0 && MAX_INSERT!=-9999 ){ r += "MAX_INSERT must be a positive integer value: " + MAX_INSERT + "\n"; }
		if( MAX_INSERT<MIN_INSERT && MIN_INSERT!=-9999 && MAX_INSERT!=-9999){ r += "MAX_INSERT must be larger/equal to MIN_INSERT: " + MIN_INSERT + "," + MAX_INSERT + "\n"; }
		// turn pair status boolean into int
		PAIR = matePair ? 1 : 0;
		
		return(r);
	}
	public static String getCLIcommand(File BAM, File output, int strand, int pair, int min, int max){
		String command = "java -jar $SCRIPTMANAGER bam-format-converter bam-to-bedgraph";
		command += " " + BAM.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();
		if (strand == 0) {
			command += " -1 ";
		} else if (strand == 1) {
			command += " -2 ";
		} else if (strand == 2) {
			command += " -a ";
		} else if (strand == 3 )  {
			command += " -m ";
		}
		command += pair != 0 ? " -p" : "";
		if (min != -9999) {
			command += " -n " + min;
		} else if (max != -9999) {
			command += " -x " + max;
		}
		return command;
	}
}