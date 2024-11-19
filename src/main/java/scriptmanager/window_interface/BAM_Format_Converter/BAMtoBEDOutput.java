package scriptmanager.window_interface.BAM_Format_Converter;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import scriptmanager.cli.BAM_Format_Converter.BAMtoBEDCLI;
import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.LogItem;
import scriptmanager.scripts.BAM_Format_Converter.BAMtoBED;
import scriptmanager.util.ExtensionFileFilter;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.BAM_Format_Converter.BAMtoBED} and reporting
 * progress
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Format_Converter.BAMtoBED
 * @see scriptmanager.window_interface.BAM_Format_Converter.BAMtoBEDWindow
 */
@SuppressWarnings("serial")
public class BAMtoBEDOutput extends JFrame {
	private File BAM = null;
	private File OUT_DIR = null;
	private boolean OUTPUT_GZIP;
	private int STRAND = 0;
	private String READ = "READ1";

	private static int PAIR = 1;
	private static int MIN_INSERT = -9999;
	private static int MAX_INSERT = -9999;

	private JTextArea textArea;

	/**
	 * Creates a new BAMtoBEDOutput window
	 * @param b BAM file
	 * @param out_dir Directory to output BED file to
	 * @param s Specifies which reads to output
	 * @param pair_status Specifies if proper pairs are required (0 = not required, !0 = required)
	 * @param min_size Minimum acceptable insert size
	 * @param max_size Maximum acceptable insert size
	 * @param gzOutput   whether or not to gzip output
	 */
	public BAMtoBEDOutput(File b, File out_dir, int s, int pair_status, int min_size, int max_size, boolean gzOutput) {
		setTitle("BAM to BED Progress");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		BAM = b;
		OUT_DIR = out_dir;
		STRAND = s;
		PAIR = pair_status;
		MIN_INSERT = min_size;
		MAX_INSERT = max_size;
		if (STRAND == 0) {
			READ = "READ1";
		} else if (STRAND == 1) {
			READ = "READ2";
		} else if (STRAND == 2) {
			READ = "COMBINED";
		} else if (STRAND == 3) {
			READ = "MIDPOINT";
		} else if (STRAND == 4) {
			READ = "FRAGMENT";
		}
		OUTPUT_GZIP = gzOutput;
	}

	/**
	 * Runs the BAMtoBED script with the file passed in through the constructor
	 * @throws IOException Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the same time
	 */
	public void run() throws IOException, InterruptedException {
		// Open Output File
		String OUTPUT = ExtensionFileFilter.stripExtensionIgnoreGZ(BAM) + "_" + READ + ".bed" + (OUTPUT_GZIP ? ".gz": "");
		if (OUT_DIR != null) {
			OUTPUT = OUT_DIR.getCanonicalPath() + File.separator + OUTPUT;
		}

		// Call script here, pass in ps and OUT
		PrintStream PS = new PrintStream(new CustomOutputStream(textArea));
		PS.println(OUTPUT);

		// Initialize LogItem
		String command = BAMtoBEDCLI.getCLIcommand(BAM, new File(OUTPUT), STRAND, PAIR, MIN_INSERT, MAX_INSERT);
		LogItem new_li = new LogItem(command);
		firePropertyChange("log", null, new_li);

		// Execute script
		BAMtoBED script_obj = new BAMtoBED(BAM, new File(OUTPUT), STRAND, PAIR, MIN_INSERT, MAX_INSERT, PS, OUTPUT_GZIP);
		script_obj.run();

		// Update LogItem
		new_li.setStopTime(new Timestamp(new Date().getTime()));
		new_li.setStatus(0);
		firePropertyChange("log", new_li, null);

		Thread.sleep(2000);
		dispose();
	}
}