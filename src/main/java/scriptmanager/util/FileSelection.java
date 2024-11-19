package scriptmanager.util;

import java.io.File;

import javax.swing.JFileChooser;

/**
 * Various kinds of file selectors used by the `window_interface` objects to
 * restrict user input selection options. Used across many classes in the
 * window_interface package.
 * 
 * @author William KM Lai
 */
public class FileSelection {

	/**
	 * Generic multi-file FileSelector without extension restrictions (commonly
	 * TAB-delimited file formats like matrix/table data).
	 * 
	 * @param fc FileChooser that provides all files and displays appropriate files
	 * @return array of File objects from selector UI
	 */
	public static File[] getGenericFiles(JFileChooser fc){
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		fc.setSelectedFile(new File(""));
		fc.setDialogTitle("File Selection");
		File[] Files = null;
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Files = fc.getSelectedFiles();
		}
		return Files;
	}

	/**
	 * Generic single-file FileSelector for retrieving directories.
	 * 
	 * @param fc FileChooser that provides all files and displays appropriate files
	 * @return a directory-type file
	 */
	public static File getOutputDir(JFileChooser fc) {
		fc.setDialogTitle("Output Directory");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION) { 
			return fc.getSelectedFile();
		} else {
			return null;
		}	
	}

	/**
	 * Extension-based single-file FileSelector for retrieving a single file with a
	 * specific extension.
	 * 
	 * @param fc FileChooser that provides all files and displays appropriate files
	 * @param ext the extension to restrict file selection options by
	 * @return a single file with a specific extension (see "ext")
	 */
	public static File getFile(JFileChooser fc, String ext){
		return getFile(fc, ext, false);
	}

	/**
	 * Extension-based single-file FileSelector for retrieving a single file with a
	 * specific extension where ignoring ".gz" extensions can be toggled.
	 * 
	 * @param fc FileChooser that provides all files and displays appropriate files
	 * @param ext       the extension to restrict file selection options by
	 * @param includeGZ true value means ignore the ".gz" if present when checking
	 *                  the extension
	 * @return a single file with a specific extension (see "ext")
	 */
	public static File getFile(JFileChooser fc, String ext, boolean includeGZ){
		fc.setFileFilter(new ExtensionFileFilter(ext,includeGZ));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(false);
		fc.setSelectedFile(new File(""));
		fc.setDialogTitle("File Selection");
		File Files = null;
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Files = fc.getSelectedFile();
		}
		return Files;
	}

	/**
	 * Extension-based multi-file FileSelector for retrieving a set of files with a
	 * specific extension.
	 * 
	 * @param fc FileChooser that provides all files and displays appropriate files
	 * @param ext the extension to restrict file selection options by
	 * @return a list of files with a specific extension (see "ext")
	 */
	public static File[] getFiles(JFileChooser fc, String ext){
		return getFiles(fc, ext, false);
	}

	/**
	 * Extension-based multi-file FileSelector for retrieving a set of files with a
	 * specific extension where ignoring ".gz" extensions can be toggled.
	 * 
	 * @param fc FileChooser that provides all files and displays appropriate files
	 * @param ext       the extension to restrict file selection options by
	 * @param includeGZ true value means ignore the ".gz" if present when checking
	 *                  the extension
	 * @return a list of files with a specific extension (see "ext")
	 */
	public static File[] getFiles(JFileChooser fc, String ext, boolean includeGZ){
		fc.setFileFilter(new ExtensionFileFilter(ext,includeGZ));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		fc.setSelectedFile(new File(""));
		fc.setDialogTitle("File Selection");
		File[] Files = null;
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Files = fc.getSelectedFiles();
		}
		return Files;
	}
}