package scriptmanager.window_interface.BAM_Statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import scriptmanager.cli.BAM_Statistics.CrossCorrelationCLI;
import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.ArchTEx.CorrParameter;
import scriptmanager.objects.LogItem;
import scriptmanager.scripts.BAM_Statistics.CrossCorrelation;
import scriptmanager.util.ExtensionFileFilter;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.BAM_Statistics.CrossCorrelation} and reporting
 * the Tag Shift-to-Correlation plots, the raw Tag Shift-to-Correlation values,
 * and progress.
 * 
 * @author William KM Lai
 * @see scriptmanager.objects.ArchTEx.CorrParameter
 * @see scriptmanager.scripts.BAM_Statistics.CrossCorrelation
 * @see scriptmanager.window_interface.BAM_Statistics.CrossCorrelationWindow
 */
@SuppressWarnings("serial")
public class CrossCorrelationOutput extends JFrame {

	Vector<File> bamFiles = null;
	private File OUT_DIR = null;
	private boolean OUTPUT_STATUS = false;
	private CorrParameter PARAM;
	
	final JLayeredPane layeredPane;
	final JTabbedPane tabbedPane;
	final JTabbedPane tabbedPane_CCPlots;
	final JTabbedPane tabbedPane_CCData;

	/**
	 * Initialize tab frames for scrollable JTextArea and charts as well as save
	 * inputs for calling the script
	 * 
	 * @param input the list of input BAM files to process
	 * @param o     the output directory to write output to
	 * @param out   whether to output results to a file or not
	 * @param param the custom parameter storing object for running the ArchTEx
	 *              script
	 */
	public CrossCorrelationOutput(Vector<File> input, File o, boolean out, CorrParameter param) {
		setTitle("BAM File Cross Correlation Plots and Statistics");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 800, 600);
		
		layeredPane = new JLayeredPane();
		getContentPane().add(layeredPane, BorderLayout.CENTER);
		SpringLayout sl_layeredPane = new SpringLayout();
		layeredPane.setLayout(sl_layeredPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		sl_layeredPane.putConstraint(SpringLayout.NORTH, tabbedPane, 6, SpringLayout.NORTH, layeredPane);
		sl_layeredPane.putConstraint(SpringLayout.WEST, tabbedPane, 6, SpringLayout.WEST, layeredPane);
		sl_layeredPane.putConstraint(SpringLayout.SOUTH, tabbedPane, -6, SpringLayout.SOUTH, layeredPane);
		sl_layeredPane.putConstraint(SpringLayout.EAST, tabbedPane, -6, SpringLayout.EAST, layeredPane);
		layeredPane.add(tabbedPane);
		
		bamFiles = input;
		OUT_DIR = o;
		OUTPUT_STATUS = out;
		PARAM = param;
		
		tabbedPane_CCPlots = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_CCData = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("C-C Plots", null, tabbedPane_CCPlots, null);
		tabbedPane.addTab("C-C Data", null, tabbedPane_CCData, null);
	}

	/**
	 * Call script, validate BAI files, build output basename, and display progress
	 * with results by instantiating results tabs for each input BAM file.
	 * 
	 * @throws IOException Invalid file or parameters
	 */
	public void run() throws IOException {
		// Check if BAI index file exists for all BAM files
		boolean[] BAMvalid = new boolean[bamFiles.size()];
		for (int z = 0; z < bamFiles.size(); z++) {
			File BAM = bamFiles.get(z); // Pull current BAM file
			File f = new File(BAM + ".bai"); // Generate file name for BAI index file
			if (!f.exists() || f.isDirectory()) {
				BAMvalid[z] = false;
				System.err.println("BAI Index File does not exist for: " + BAM.getName());
				JOptionPane.showMessageDialog(null, "BAI Index File does not exist for: " + BAM.getName());
			} else {
				BAMvalid[z] = true;
			}
		}
		LogItem old_li = null;
		//Iterate through all BAM files in Vector
		for(int x = 0; x < bamFiles.size(); x++) {
			if (BAMvalid[x]) {
				// Construct output filename
				String NAME = ExtensionFileFilter.stripExtension(bamFiles.get(x).getName()) + "_CrossCorrelation.txt";
				File OUT_FILEPATH = new File(NAME);
				if (OUT_DIR != null) {
					OUT_FILEPATH = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME);
				}
				// Initialize PrintStream and TextArea for C-C Data
				PrintStream ps_ccdata = null;
				JTextArea CC_DATA = new JTextArea();
				CC_DATA.setCaretPosition(0);
				CC_DATA.setLineWrap(false);
				CC_DATA.setEditable(false);
				ps_ccdata = new PrintStream(new CustomOutputStream( CC_DATA ));
				tabbedPane_CCData.add(bamFiles.get(x).getName(), new JScrollPane(CC_DATA, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
				// Initialize LogItem
				String command = CrossCorrelationCLI.getCLIcommand(bamFiles.get(x), OUT_FILEPATH, PARAM);
				LogItem new_li = new LogItem(command);
				if (OUTPUT_STATUS) { firePropertyChange("log", old_li, new_li); }
				//Call public static method from scripts
				Component chart = CrossCorrelation.correlate(bamFiles.get(x), OUT_FILEPATH, PARAM, ps_ccdata);
				tabbedPane_CCPlots.add(bamFiles.get(x).getName(), chart);
				// Update log item
				new_li.setStopTime(new Timestamp(new Date().getTime()));
				new_li.setStatus(0);
				old_li = new_li;
				// Close PrintStream
				ps_ccdata.close();
				// Update progress
				firePropertyChange("progress",x, x + 1);
			}
		}
		// Update log after final input
		if (OUTPUT_STATUS) { firePropertyChange("log", old_li, null); }
	}
}
