package scriptmanager.window_interface.BAM_Statistics;

import java.awt.BorderLayout;
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

import org.jfree.chart.ChartPanel;

import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.LogItem;
import scriptmanager.util.ExtensionFileFilter;

import scriptmanager.cli.BAM_Statistics.PEStatsCLI;
import scriptmanager.scripts.BAM_Statistics.PEStats;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.BAM_Statistics.PEStats} and reporting the
 * histograms and statistics
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.PEStats
 * @see scriptmanager.window_interface.BAM_Statistics.PEStatOutput
 */
@SuppressWarnings("serial")
public class PEStatOutput extends JFrame {
	
	Vector<File> bamFiles = null;
	private File OUT_DIR = null;
	private boolean OUTPUT_STATUS = false;
	private boolean DUP_STATUS = false;
	private static int MIN_INSERT = 0;
	private static int MAX_INSERT = 1000;
	
	final JLayeredPane layeredPane;
	final JTabbedPane tabbedPane;
	final JTabbedPane tabbedPane_Histogram;
	final JTabbedPane tabbedPane_InsertStats;
	final JTabbedPane tabbedPane_Duplication;
	final JTabbedPane tabbedPane_DupStats;
		
	public PEStatOutput(Vector<File> input, File o, boolean out, boolean dup, int min, int max) {
		setTitle("BAM File Paired-end Statistics");
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
		DUP_STATUS = dup;
		MIN_INSERT = min;
		MAX_INSERT = max;
		
		tabbedPane_Histogram = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_InsertStats = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Insert Histogram", null, tabbedPane_Histogram, null);
		tabbedPane.addTab("PE Insert Stats", null, tabbedPane_InsertStats, null);
		
		tabbedPane_Duplication = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_DupStats = new JTabbedPane(JTabbedPane.TOP);
		if(DUP_STATUS) {
			tabbedPane.addTab("Duplication Rate", null, tabbedPane_Duplication, null);
			tabbedPane.addTab("Duplication Stats", null, tabbedPane_DupStats, null);
		}
	}
	
	public void run() throws FileNotFoundException, IOException {
		// Check if BAI index file exists for all BAM files
		boolean[] BAMvalid = new boolean[bamFiles.size()];
		for (int z = 0; z < bamFiles.size(); z++) {
			File BAM = bamFiles.get(z); // Pull current BAM file
			File f = new File(BAM + ".bai"); // Generate file name for BAI index file
			if (!f.exists() || f.isDirectory()) {
				BAMvalid[z] = false;
				JOptionPane.showMessageDialog(null, "BAI Index File does not exist for: " + BAM.getName());
				System.err.println("BAI Index File does not exist for: " + BAM.getName());
			} else {
				BAMvalid[z] = true;
			}
		}
		LogItem old_li = null;
		//Iterate through all BAM files in Vector
		for(int x = 0; x < bamFiles.size(); x++) {
			if (BAMvalid[x]) {
				// Construct output basename
				String NAME = ExtensionFileFilter.stripExtension(bamFiles.get(x).getName());
				File OUT_BASENAME = new File(NAME);
				if (OUT_DIR != null) {
					OUT_BASENAME = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME);
				}
				// Initialize PrintStream and TextArea for PE stats (insert sizes)
				PrintStream ps_insert = null;
				JTextArea PE_STATS = new JTextArea();
				PE_STATS.setEditable(false);
				ps_insert = new PrintStream(new CustomOutputStream( PE_STATS ));
				// Initialize PrintStream and TextArea for DUP stats
				PrintStream ps_dup = null;
				JTextArea DUP_STATS = new JTextArea();
				if (DUP_STATUS) {
					DUP_STATS.setEditable(false);
					ps_dup = new PrintStream(new CustomOutputStream( DUP_STATS ));
				}
				// Initialize LogItem
				String command = PEStatsCLI.getCLIcommand(bamFiles.get(x), OUT_BASENAME, DUP_STATUS, MIN_INSERT, MAX_INSERT);
				LogItem new_li = new LogItem(command);
				if (OUTPUT_STATUS) { firePropertyChange("log", old_li, new_li); }
				// Execute script
				Vector<ChartPanel> charts = PEStats.getPEStats(bamFiles.get(x), OUT_BASENAME, DUP_STATUS, MIN_INSERT, MAX_INSERT, OUTPUT_STATUS, ps_insert, ps_dup );
				// Update log item
				new_li.setStopTime(new Timestamp(new Date().getTime()));
				new_li.setStatus(0);
				old_li = new_li;
				// Add pe stats to tabbed pane
				PE_STATS.setCaretPosition(0);
				JScrollPane pe_pane = new JScrollPane(PE_STATS, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				tabbedPane_InsertStats.add(bamFiles.get(x).getName(), pe_pane);
				tabbedPane_Histogram.add(bamFiles.get(x).getName(), charts.get(0));
				// Set-up duplication statistics if flagged
				if(DUP_STATUS) {
					//Add duplication stats to tabbed pane
					DUP_STATS.setCaretPosition(0);
					JScrollPane dup_pane = new JScrollPane(DUP_STATS, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					tabbedPane_DupStats.add(bamFiles.get(x).getName(), dup_pane);
					tabbedPane_Duplication.add(bamFiles.get(x).getName(), charts.get(1));
				}
				// Close streams
				ps_insert.close();
				if (ps_dup!=null) { ps_dup.close(); }
				// Update progress
				firePropertyChange("progress",x, x + 1);
			}
		}
		// Update log after final input
		if (OUTPUT_STATUS) { firePropertyChange("log", old_li, null); }
	}
}