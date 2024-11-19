package scriptmanager.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class contains static methods for parsing files and input streams to
 * determine GZip-format information. Used across many tools in the scripts
 * package.
 * 
 * Found on a stackoverflow thread
 * (https://stackoverflow.com/questions/30507653/how-to-check-whether-file-is-gzip-or-not-in-java)
 *
 * @author Olivia Lang
 * @see scriptmanager.scripts.Coordinate_Manipulation.ShiftCoord
 * @see scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.BEDtoGFF
 * @see scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.ExpandBED
 * @see scriptmanager.scripts.Coordinate_Manipulation.BED_Manipulation.SortBED
 * @see scriptmanager.scripts.Figure_Generation.TwoColorHeatMap
 * @see scriptmanager.scripts.File_Utilities.ConvertChrNames
 * @see scriptmanager.scripts.Peak_Analysis.FilterBEDbyProximity
 * @see scriptmanager.scripts.Read_Analysis.TagPileup
 * @see scriptmanager.scripts.Sequence_Analysis.SearchMotif
 * @see scriptmanager.util.BEDUtilities
 * @see scriptmanager.util.CDTUtilities
 */
public final class GZipUtilities {

	/**
	 * Checks if InputStream is gzipped by reading the next two bytes for the GZip
	 * MAGIC value and then resets the file marker to the original start position.
	 *
	 * @param in the InputStream to check if Gzipped (assumes stream starts at the
	 *           beginning of the file).
	 * @return returns true if bytes match GZip MAGIC and false if not.
	 */
	public static boolean isGZipped(InputStream in) {
		if (!in.markSupported()) {
			in = new BufferedInputStream(in);
		}
		in.mark(2); //mark current position in stream
		int magic = 0;
		try {
			magic = in.read() & 0xff | ((in.read() << 8) & 0xff00);
			in.reset(); //return to marked position in stream
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return false;
		}
		return magic == GZIPInputStream.GZIP_MAGIC;
	}
	
	/**
	 * Checks if File is gzipped by opening and reading the first two bytes for the
	 * GZip MAGIC value and then closing the file.
	 * 
	 * @param f the File to check if Gzipped.
	 * @return true if bytes match GZip MAGIC and false if not.
	 */
	public static boolean isGZipped(File f) {
		int magic = 0;
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
			raf.close();
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		}
		return magic == GZIPInputStream.GZIP_MAGIC;
	}

	/**
	 * Creates a new BufferedReader for Gzipped or non-Gzipped files
	 * @param f File to read
	 * @return BufferedReader made for given file
	 * @throws IOException Invalid file
	 */
	public static BufferedReader makeReader(File f) throws IOException{
		if(GZipUtilities.isGZipped(f)) {
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
		} else {
			return new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		}
	}

	/**
	 * Creates a new PrintStream for writing compressed or uncompressed data to a specified file
	 * @param f File to write
	 * @param gzip whether or not to compress the stream
	 * @return PrintStream which outputs compressed or uncompressed data to given file
	 * @throws IOException Invalid file
	 */
	public static PrintStream makePrintStream(File f, boolean gzip) throws IOException{
		if(gzip){
			return new PrintStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(f)))); 
		} else {
			return new PrintStream(new BufferedOutputStream(new FileOutputStream(f)));
		}
	}
}
