package scriptmanager.cli.Read_Analysis;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

import java.io.File;
import java.io.IOException;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.ScriptManagerException;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Read_Analysis.TransposeMatrix;

/**
	Read_AnalysisCLI/TransposeMatrixCLI
*/
@Command(name = "transpose-matrix", mixinStandardHelpOptions = true,
	description = ToolDescriptions.transpose_matrix_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class TransposeMatrixCLI implements Callable<Integer> {
	
	@Parameters( index = "0", description = "Input matrix file")
	private File matrixFile;
	
	@Option(names = {"-o", "--output"}, description = "Specify output file (default = <matrixFilename>_TRANPOSE.tab)")
	private File output = null;
	@Option(names = {"-r", "--start-row"}, description = "Row to start transposing the matrix (zero indexed)")
	private int startROW = 0;
	@Option(names = {"-l", "--start-col"}, description = "Column to start transposing the matrix (zero indexed)")
	private int startCOL = 0;
	@Option(names = { "-z", "--gzip" }, description = "gzip output (default=false)")
	private boolean gzOutput = false;
	
	
	@Override
	public Integer call() throws IOException, ScriptManagerException {
		System.err.println( ">TransposeMatrixCLI.call()" );
		String validate = validateInput();
		if(!validate.equals("")){
			System.err.println( validate );
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		// Execute script
		TransposeMatrix.transpose(matrixFile, output, startROW, startCOL, gzOutput);

		System.err.println("All Matrices Transposed.");
		return(0);
	}
	
	private String validateInput() throws IOException {
		String r = "";
		
		//check inputs exist
		if(!matrixFile.exists()){
			r += "(!)MATRIX file does not exist: " + matrixFile.getName() + "\n";
			return(r);
		}
		//no check ext
		//set default output filename
		if(output==null){
			output = new File(ExtensionFileFilter.stripExtension(matrixFile) + "_TRANPOSE."
					+ ExtensionFileFilter.getExtension(matrixFile)
					+ (gzOutput ? ".gz" : ""));
		//check output filename is valid
		}else if( output.isDirectory() ){
			r += "(!)Must indicate file (not a directory) for your output.";
		} else {
			//no check ext
			//check directory
			if(output.getParent()==null){
			//System.err.println("default to current directory");
			} else if(!new File(output.getParent()).exists()){
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}
		//validate row&column start indexes
		if(startROW<0){ r += "(!)Row start must not be less than zero\n"; }
		if(startCOL<0){ r += "(!)Column start must not be less than zero\n"; }

		return(r);
	}

	public static String getCLIcommand(File matrix, File output, int startROW, int startCOL, boolean gzOutput){
		String command = "java -jar $SCRIPTMANAGER read-analysis transpose-matrix";
		command += gzOutput? " -z": "";
		command += startCOL != 0? " -l " + startCOL: "";
		command += " -o " + output.getAbsolutePath();
		command += startROW != 0? " -r " + startROW: "";
		command += " " + matrix.getAbsolutePath();
		return command;
	}
}