package scriptmanager.window_interface.Peak_Analysis;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import scriptmanager.cli.Peak_Analysis.BEDPeakAligntoRefCLI;
import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.LogItem;
import scriptmanager.scripts.Peak_Analysis.BEDPeakAligntoRef;
import scriptmanager.util.ExtensionFileFilter;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Peak_Analysis.BEDPeakAligntoRef} and reporting
 * progress
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Peak_Analysis.BEDPeakAligntoRef
 * @see scriptmanager.window_interface.Peak_Analysis.BEDPeakAligntoRefWindow
 */
@SuppressWarnings("serial")
public class BEDPeakAligntoRefOutput extends JFrame{
	private File PEAK = null;
	private File REF = null;
	private File OUT_DIR = null;
	private boolean SEPARATE = false;
	private boolean OUTPUT_GZIP = false;

	private JTextArea textArea;

	public BEDPeakAligntoRefOutput(File ref, File peak, File output, boolean s, boolean gzOutput) throws IOException {
	/**
	 * Creates a new BEDPeakAligntoRefOutput with two BED files and an output directory
	 * @param ref Reference BAM file
	 * @param peak BAM file to be alligned
	 * @param outpath Output directory
	 * @throws IOException Invalid file or parameters
	 */
		setTitle("BED Align to Reference Progress");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		REF = ref;
		PEAK = peak;
		OUT_DIR = output;
		SEPARATE = s;
		OUTPUT_GZIP = gzOutput;
	}

	/**
	 * Runs the BEDPeakAligntoRef script
	 * 
	 * @throws IOException          Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the
	 *                              same time
	 */
	public void run() throws IOException, InterruptedException {
		// Construct output basename
		String NAME = ExtensionFileFilter.stripExtension(PEAK.getName()) + "_" + ExtensionFileFilter.stripExtension(REF.getName()) + "_PeakAlign";
		File OUT_BASENAME = new File(NAME);
		if (OUT_DIR != null) {
			OUT_BASENAME = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME);
		}
		// Initialize LogItem
		String command = BEDPeakAligntoRefCLI.getCLIcommand(REF, PEAK, OUT_BASENAME, SEPARATE, OUTPUT_GZIP);
		LogItem li = new LogItem(command);
		firePropertyChange("log", null, li);
		// Set-up display stream
		PrintStream PS = new PrintStream(new CustomOutputStream(textArea));
		// Execute script
		BEDPeakAligntoRef.execute(REF, PEAK, OUT_BASENAME, SEPARATE, PS, OUTPUT_GZIP);
		// Update log item
		li.setStopTime(new Timestamp(new Date().getTime()));
		li.setStatus(0);
		firePropertyChange("log", li, null);
		// wait before disposing
		Thread.sleep(2000);
		dispose();
	}
}