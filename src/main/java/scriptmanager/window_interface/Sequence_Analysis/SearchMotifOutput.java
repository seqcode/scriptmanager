package scriptmanager.window_interface.Sequence_Analysis;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import scriptmanager.cli.Sequence_Analysis.SearchMotifCLI;
import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.LogItem;
import scriptmanager.scripts.Sequence_Analysis.SearchMotif;
import scriptmanager.util.ExtensionFileFilter;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Sequence_Analysis.SearchMotif} and reporting
 * progress
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Sequence_Analysis.SearchMotif
 * @see scriptmanager.window_interface.Sequence_Analysis.SearchMotifWindow
 */
@SuppressWarnings("serial")
public class SearchMotifOutput extends JFrame {

	private int ALLOWED_MISMATCH;
	private String motif;
	private File INPUTFILE = null;
	private File OUT_DIR;
	private boolean gzOutput = false;

	private JTextArea textArea;

	/**
	 * Initialize a scrollable JTextArea window to display progress and save inputs
	 * for calling the script.
	 * 
	 * @param input
	 * @param mot
	 * @param num
	 * @param out_dir
	 * @param gz If this is true, the output file will be gzipped.
	 * @throws IOException Invalid file or parameters
	 */
	public SearchMotifOutput(File input, String mot, int num, File out_dir, boolean gz) throws IOException {
		setTitle("Motif Search Progress");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		ALLOWED_MISMATCH = num;
		motif = mot;
		INPUTFILE = input;
		OUT_DIR = out_dir;
		gzOutput = gz;
	}

	/**
	 * Call script to search for motif instances and display progress by
	 * instantiating a scrollable JTextArea window that prints each
	 * sequence/chromosome name within the FASTA file and dispose the window after
	 * the script finishes.
	 * 
	 * @throws IOException Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the same time
	 */
	public void run() throws IOException, InterruptedException {
		PrintStream PS = new PrintStream(new CustomOutputStream(textArea));
		// Construct output filename
		String BASENAME = motif + "_" + Integer.toString(ALLOWED_MISMATCH) + "Mismatch_"
				+ ExtensionFileFilter.stripExtensionIgnoreGZ(INPUTFILE) + ".bed";
		BASENAME += gzOutput ? ".gz" : "";
		if (OUT_DIR != null) {
			BASENAME = OUT_DIR.getCanonicalPath() + File.separator + BASENAME;
		}
		// Initialize LogItem
		String command = SearchMotifCLI.getCLIcommand(INPUTFILE, new File(BASENAME), motif, ALLOWED_MISMATCH, gzOutput);
		LogItem li = new LogItem(command);
		firePropertyChange("log", null, li);
		// Execute script
		SearchMotif script_obj = new SearchMotif(INPUTFILE, motif, ALLOWED_MISMATCH, new File(BASENAME), PS, gzOutput);
		script_obj.run();
		// Update log item
		li.setStopTime(new Timestamp(new Date().getTime()));
		li.setStatus(0);
		firePropertyChange("log", li, null);
		// Sleep and dispose
		Thread.sleep(2000);
		dispose();
	}
}