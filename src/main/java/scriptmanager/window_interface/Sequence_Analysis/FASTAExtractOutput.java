package scriptmanager.window_interface.Sequence_Analysis;

import htsjdk.samtools.SAMException;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;


import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import scriptmanager.cli.Sequence_Analysis.FASTAExtractCLI;
import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.LogItem;
import scriptmanager.scripts.Sequence_Analysis.FASTAExtract;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Sequence_Analysis.FASTAExtract} and reporting
 * progress
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Sequence_Analysis.FASTAExtract
 * @see scriptmanager.window_interface.Sequence_Analysis.FASTAExtractWindow
 */
@SuppressWarnings("serial")
public class FASTAExtractOutput extends JFrame {
	private File GENOME = null;
	private File OUT_DIR = null;
	private ArrayList<File> BED = null;
	private boolean STRAND = true;
	private boolean HEADER = true;
	private boolean gzOutput = false;

	private JTextArea textArea;

	/**
	 * Initialize a scrollable text area for printing progress log statements.
	 * 
	 * @param gen     the reference genome sequence in FASTA-format
	 * @param b       the BED-formatted coordinate intervals to extract sequence from
	 * @param out_dir the output directory to save output files to
	 * @param str     the force-strandedness to pass to the script
	 * @param head    the style of FASTA-header to use for the output
	 * @param gz      If this is true, the output file will be gzipped.
	 */
	public FASTAExtractOutput(File gen, ArrayList<File> b, File out_dir, boolean str, boolean head, boolean gz) {
		setTitle("FASTA Extraction Progress");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		GENOME = gen;
		BED = b;
		OUT_DIR = out_dir;
		STRAND = str;
		HEADER = head;
		gzOutput = gz;
	}

	/**
	 * Call script to extract genomic sequences and display progress by
	 * instantiating a scrollable JTextArea window that prints each
	 * sequence/chromosome name within the FASTA file and dispose the window after
	 * the script finishes.
	 * 
	 * @throws IOException Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the same time
	 */
	public void run() throws IOException, InterruptedException {
		LogItem old_li = null;
		PrintStream PS = new PrintStream(new CustomOutputStream(textArea));
		try {
			for (int x = 0; x < BED.size(); x++) {
				// Open Output File
				File OUTFILE;
				String NAME = BED.get(x).getName().split("\\.")[0] + ".fa";
				if (OUT_DIR != null) {
					NAME = OUT_DIR.getCanonicalPath() + File.separator + NAME;
				}
				NAME += gzOutput ? ".gz" : "";
				OUTFILE = new File(NAME);
				PS.println("Proccessing File: " + BED.get(x).getName());
				// Initialize LogItem
				String command = FASTAExtractCLI.getCLIcommand(GENOME, BED.get(x), OUTFILE, STRAND, HEADER, gzOutput);
				LogItem new_li = new LogItem(command);
				firePropertyChange("log", old_li, new_li);
				// Execute Script object
				FASTAExtract script_obj = new FASTAExtract(GENOME, BED.get(x), OUTFILE, STRAND, HEADER, PS, gzOutput);
				script_obj.run();
				// Update log item
				new_li.setStopTime(new Timestamp(new Date().getTime()));
				new_li.setStatus(0);
				old_li = new_li;
				// Update progress
				firePropertyChange("progress", x, x + 1);
			}
			firePropertyChange("log", old_li, null);
			PS.println("Extraction Complete");
		} catch (IllegalArgumentException e) {
			PS.println(e.getMessage());
		} catch (FileNotFoundException e) {
			PS.println(e.getMessage());
		} catch (SAMException e) {
			PS.println(e.getMessage());
		}
	}
}