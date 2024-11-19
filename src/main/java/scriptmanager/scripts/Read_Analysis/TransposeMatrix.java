package scriptmanager.scripts.Read_Analysis;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import scriptmanager.objects.Exceptions.ScriptManagerException;
import scriptmanager.util.GZipUtilities;

public class TransposeMatrix {

	/**
	 * Creates a new instance of the TransposeMatrix script with a given input file
	 * 
	 * @param MATRIX TAB file to be scaled
	 * @param OUTFILE Output directory/file
	 * @param ROWINDEX Starting row (zero-indexed)
	 * @param COLINDEX Starting column (zero-indexed)
	 * @param OUTPUT_GZIP Output Gzip
	 * @throws IOException Invalid file or parameters
	 */
	public static void transpose(File MATRIX, File OUTFILE, int ROWINDEX, int COLINDEX, boolean OUTPUT_GZIP) throws IOException, ScriptManagerException {

		System.err.println(getTimeStamp());
		System.err.println("Processing file:\t" + MATRIX.getName());
		System.err.println("Starting row index:\t" + ROWINDEX);
		System.err.println("Starting column index:\t" + COLINDEX);

		try {
			// Parse, transpose, and output tab-delimited matrix on the fly
			BufferedReader br = makeReader(MATRIX);
			PrintStream OUT = makePrintStream(OUTFILE, OUTPUT_GZIP);
			//Determine number of rows and columns
			int numCols = br.readLine().split("\t").length;
			int numRows = 1;
			while (br.readLine() != null){
				numRows++;
			}
			//Skip rows less than ROWINDEX
			br = makeReader(MATRIX);
			int row;
			for (row = 0; row < ROWINDEX; row++){
				br.readLine();
			}
			//Load original matrix into array
			String[][] originalMatrix = new String[numRows - ROWINDEX][numCols - COLINDEX];
			String[][] newMatrix = new String[numCols - COLINDEX][numRows - ROWINDEX];
			String line;
			row = 0;
			while ((line = br.readLine()) != null){
				//Skip columns less than COLINDEX
				String[] originalLine = line.split("\t");
				String[] croppedLine = new String[numCols - COLINDEX];
				for (int i = originalLine.length - 1; i >= COLINDEX; i--){
					croppedLine[i - COLINDEX] = originalLine[i];
				}
				originalMatrix[row++] = croppedLine;
			}
			br.close();
			//Transfer rows of original matrix to new matrix and vice-versa
			for (row = 0; row < originalMatrix.length; row++){
				for (int col = 0; col < originalMatrix[0].length; col++){
					newMatrix[col][row] = originalMatrix[row][col];
				}
			}
			//Write new matrix to output file
			for (row = 0; row < newMatrix.length; row++){
				OUT.println(String.join("\t", newMatrix[row]));
			}
			OUT.close();
		} catch (ArrayIndexOutOfBoundsException nfe) {
			throw new ScriptManagerException(MATRIX.getName() + " contains inconsistent columns per row!!!");
		}
	}

	private static String getTimeStamp() {
		Date date = new Date();
		String time = new Timestamp(date.getTime()).toString();
		return time;
	}

	private static BufferedReader makeReader(File MATRIX) throws IOException{
		if(GZipUtilities.isGZipped(MATRIX)) {
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(MATRIX)), "UTF-8"));
		} else {
			return new BufferedReader(new InputStreamReader(new FileInputStream(MATRIX), "UTF-8"));
		}
	}

	private static PrintStream makePrintStream(File o, boolean gzip) throws IOException{
		if(gzip){
			return new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(o)))); 
		} else {
			return new PrintStream(new BufferedOutputStream(new FileOutputStream(o)));
		}
	}
}
