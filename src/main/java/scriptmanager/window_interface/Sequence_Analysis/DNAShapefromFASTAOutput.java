package scriptmanager.window_interface.Sequence_Analysis;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.objects.Exceptions.ScriptManagerException;
import scriptmanager.util.ExtensionFileFilter;

import scriptmanager.cli.Sequence_Analysis.DNAShapefromFASTACLI;
import scriptmanager.scripts.Sequence_Analysis.DNAShapefromFASTA;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Sequence_Analysis.DNAShapefromFASTA} and
 * reporting composite results
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Sequence_Analysis.DNAShapefromFASTA
 * @see scriptmanager.window_interface.Sequence_Analysis.DNAShapefromFASTAWindow
 */
@SuppressWarnings("serial")
public class DNAShapefromFASTAOutput extends JFrame {
	private ArrayList<File> FASTA = null;
	private File OUT_DIR = null;
	private boolean[] OUTPUT_TYPE = null;
	private boolean OUTPUT_COMPOSITE;
	private short OUTPUT_MATRIX;
	private boolean OUTPUT_GZIP;

	final JLayeredPane layeredPane;
	final JTabbedPane tabbedPane;
	final JTabbedPane tabbedPane_Scatterplot;
	final JTabbedPane tabbedPane_Statistics;

	/**
	 * Initialize a tabbed window with a tab for the charts and a tab for the
	 * statistics.
	 * 
	 * @param fa the FASTA-formatted sequences to generate the shape scores from
	 * @param out_dir the output directory to save output files to
	 * @param type    the shape types to generate
	 * @param outputComposite whether to output the composite
	 * @param outputMatrix format/whether to output matrix
	 * @param gzOutput whether to output compressed file
	 */
	public DNAShapefromFASTAOutput(ArrayList<File> fa, File out_dir, boolean[] type, boolean outputComposite, short outputMatrix, boolean gzOutput) {
		setTitle("DNA Shape Prediction Composite");
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

		tabbedPane_Scatterplot = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("DNA Shape Plot", null, tabbedPane_Scatterplot, null);

		tabbedPane_Statistics = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("DNA Shape Statistics", null, tabbedPane_Statistics, null);

		FASTA = fa;
		OUT_DIR = out_dir;
		OUTPUT_TYPE = type;
		OUTPUT_COMPOSITE = outputComposite;
		OUTPUT_MATRIX = outputMatrix;
		OUTPUT_GZIP = gzOutput;
	}

	/**
	 * Call script on each BED file to calculate shape scores and append the values
	 * for each shape type under the "DNA Shape Statistics" tab and append each
	 * chart generated under the "DNA Shape Plot" tab.
	 * 
	 * @throws ScriptManagerException when ref FASTA not indexable/unexpected formats
	 * @throws OptionException when invalid output matrix type value is set
	 * @throws FileNotFoundException Invalid file or parameters
	 * @throws IOException Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the same time
	 */
	public void run() throws ScriptManagerException, OptionException, FileNotFoundException, IOException, InterruptedException {
			LogItem old_li = null;
			// Move through each BED File
			for (int x = 0; x < FASTA.size(); x++) {
				File XFASTA = FASTA.get(x);
				// Initialize TextAreas and PrintStream wrappers
				JTextArea STATS_MGW = null;
				JTextArea STATS_PropT = null;
				JTextArea STATS_HelT = null;
				JTextArea STATS_Roll = null;
				PrintStream[] PS = { null, null, null, null };
				if (OUTPUT_TYPE[0]) {
					STATS_MGW = new JTextArea();
					STATS_MGW.setEditable(false);
					PS[0] = new PrintStream(new CustomOutputStream(STATS_MGW));
				}
				if (OUTPUT_TYPE[1]) {
					STATS_PropT = new JTextArea();
					STATS_PropT.setEditable(false);
					PS[1] = new PrintStream(new CustomOutputStream(STATS_PropT));
				}
				if (OUTPUT_TYPE[2]) {
					STATS_HelT = new JTextArea();
					STATS_HelT.setEditable(false);
					PS[2] = new PrintStream(new CustomOutputStream(STATS_HelT));
				}
				if (OUTPUT_TYPE[3]) {
					STATS_Roll = new JTextArea();
					STATS_Roll.setEditable(false);
					PS[3] = new PrintStream(new CustomOutputStream(STATS_Roll));
				}
				// Construct output filename
				String NAME = ExtensionFileFilter.stripExtension(XFASTA);
				File OUT_BASENAME = new File(NAME);
				if (OUT_DIR != null) {
					OUT_BASENAME = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME);
				}
				// Initialize LogItem
				String command = DNAShapefromFASTACLI.getCLIcommand(XFASTA, OUT_BASENAME, OUTPUT_TYPE, OUTPUT_COMPOSITE, OUTPUT_MATRIX, OUTPUT_GZIP);
				LogItem new_li = new LogItem(command);
				firePropertyChange("log", old_li, new_li);
				// Execute script
				DNAShapefromFASTA script_obj = new DNAShapefromFASTA(XFASTA, OUT_BASENAME, OUTPUT_TYPE, OUTPUT_COMPOSITE, OUTPUT_MATRIX, OUTPUT_GZIP, PS);
				script_obj.run();
				// Update log item
				new_li.setStopTime(new Timestamp(new Date().getTime()));
				new_li.setStatus(0);
				old_li = new_li;
				// Convert average and statistics to output tabs panes
				if (OUTPUT_TYPE[0]) {
					tabbedPane_Scatterplot.add("MGW", script_obj.getChartM());
					STATS_MGW.setCaretPosition(0);
					JScrollPane MGWpane = new JScrollPane(STATS_MGW, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					tabbedPane_Statistics.add("MGW", MGWpane);
				}
				if (OUTPUT_TYPE[1]) {
					tabbedPane_Scatterplot.add("Propeller Twist", script_obj.getChartP());
					STATS_PropT.setCaretPosition(0);
					JScrollPane PropTpane = new JScrollPane(STATS_PropT, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					tabbedPane_Statistics.add("PropT", PropTpane);
				}
				if (OUTPUT_TYPE[2]) {
					tabbedPane_Scatterplot.add("Helical Twist", script_obj.getChartH());
					STATS_HelT.setCaretPosition(0);
					JScrollPane HelTpane = new JScrollPane(STATS_HelT, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					tabbedPane_Statistics.add("HelT", HelTpane);
				}
				if (OUTPUT_TYPE[3]) {
					tabbedPane_Scatterplot.add("Roll", script_obj.getChartR());
					STATS_Roll.setCaretPosition(0);
					JScrollPane Rollpane = new JScrollPane(STATS_Roll, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
							JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					tabbedPane_Statistics.add("Roll", Rollpane);
				}
				// Update progress
				firePropertyChange("progress", x, x + 1);
		}
		firePropertyChange("log", old_li, null);
	}
}
