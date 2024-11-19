package scriptmanager.scripts.File_Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.String;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import scriptmanager.util.ExtensionFileFilter;

/**
 * Gzip compressing and decompressing methods with customizable buffer sizes.
 * 
 * @author Olivia Lang
 * @see scriptmanager.window_interface.File_Utilities.CompressFileWindow
 * @see scriptmanager.window_interface.File_Utilities.DecompressGZFileWindow
 */
public class GZipFiles {

	/**
	 * Compresses an individual file (gzip) to the same filepath with a ".gz"
	 * extension added.
	 * 
	 * @param input      file to compress
	 * @param bufferSize number of bytes in buffer
	 * @throws IOException Invalid file or parameters
	 */
	public static void compressFile(File input, int bufferSize) throws IOException {
		String path = input.getCanonicalPath()+".gz";
		OutputStream target = new GZIPOutputStream(new FileOutputStream(new File(path)));
		InputStream source = new FileInputStream(input);
		byte[] buf = new byte[bufferSize];
		int length;
		while ((length = source.read(buf)) != -1) {
			target.write(buf, 0, length);
		}
		source.close();
		target.close();
	}

	/**
	 * Decompresses an individual file (gzip) to the same filepath without the ".gz"
	 * extension.
	 * 
	 * @param input      file to decompress
	 * @param bufferSize number of bytes in buffer
	 * @throws IOException Invalid file or parameters when the extension does not match ".gz"
	 */
	public static void decompressFile(File input, int bufferSize) throws IOException {
		if (input.getAbsoluteFile().toString().endsWith(".gz")) {
			String name = ExtensionFileFilter.stripExtensionPath(input);
			OutputStream target = new FileOutputStream(new File(name));
			InputStream source = new GZIPInputStream(new FileInputStream(input));
			byte[] buf = new byte[bufferSize];
			int length;
			while ((length = source.read(buf)) != -1) {
				target.write(buf, 0, length);
			}
			source.close();
			target.close();
			return;
		}
		throw new IOException("Does not have .gz extension");
	}
}
