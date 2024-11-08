package scriptmanager.window_interface.Sequence_Analysis;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.objects.Exceptions.ScriptManagerException;
import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.LogItem;
import scriptmanager.util.DNAShapeReference;
import scriptmanager.util.ExtensionFileFilter;

import scriptmanager.cli.Sequence_Analysis.DNAShapefromBEDCLI;
import scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED} and reporting
 * composite results
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED
 * @see scriptmanager.window_interface.Sequence_Analysis.DNAShapefromBEDWindow
 */
@SuppressWarnings("serial")
public class DNAShapefromBEDOutput extends JFrame {
	private File GENOME = null;
	private ArrayList<File> BED = null;
	private File OUT_DIR = null;
	private ArrayList<Integer> OUTPUT_TYPE = null;
	private boolean OUTPUT_COMPOSITE;
	private short OUTPUT_MATRIX;
	private boolean OUTPUT_GZIP;

	private boolean STRAND = true;

	final JLayeredPane layeredPane;
	final JTabbedPane tabbedPane;
	final JTabbedPane tabbedPane_Scatterplot;
	final JTabbedPane tabbedPane_Statistics;

	/**
	 * Initialize a tabbed window with a tab for the charts and a tab for the
	 * statistics.
	 * 
	 * @param gen     the reference genome sequence in FASTA-format
	 * @param b       the BED-formatted coordinate intervals to extract sequence from
	 * @param out_dir the output directory to save output files to
	 * @param type    the shape types to generate
	 * @param str     the force-strandedness to pass to the script
	 * @param outputComposite whether to output the composite
	 * @param outputMatrix format/whether to output matrix
	 * @param gzOutput whether to output compressed file
	 */
	public DNAShapefromBEDOutput(File gen, ArrayList<File> b, File out_dir, ArrayList<Integer> type, boolean str, boolean outputComposite, short outputMatrix, boolean gzOutput) {
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

		GENOME = gen;
		BED = b;
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
	public void run() throws OptionException, FileNotFoundException, IOException, InterruptedException, ScriptManagerException  {
			LogItem old_li = null;
			// Move through each BED File
			for (int x = 0; x < BED.size(); x++) {
				File XBED = BED.get(x);
				// Initialize TextAreas and PrintStream wrappers
                HashMap<Integer, JTextArea> TextAreas = new HashMap<>();
				HashMap<Integer, CustomOutputStream> PS = new HashMap<>();
                for (Integer shape: OUTPUT_TYPE){
                    TextAreas.put(shape, new JTextArea());
                    TextAreas.get(shape).setEditable(false);
                    PS.put(shape, new CustomOutputStream(TextAreas.get(shape)));
                }

				// Construct output filename
				String NAME = ExtensionFileFilter.stripExtension(XBED);
				File OUT_BASENAME = new File(NAME);
				if (OUT_DIR != null) {
					OUT_BASENAME = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME);
				}
				// Initialize LogItem
				String command = DNAShapefromBEDCLI.getCLIcommand(GENOME, XBED, OUT_BASENAME, OUTPUT_TYPE, STRAND, OUTPUT_COMPOSITE, OUTPUT_MATRIX, OUTPUT_GZIP);
				LogItem new_li = new LogItem(command);
				firePropertyChange("log", old_li, new_li);
				DNAShapefromBED script_obj = new DNAShapefromBED(GENOME, XBED, OUT_BASENAME, OUTPUT_TYPE, STRAND, OUTPUT_COMPOSITE, OUTPUT_MATRIX, OUTPUT_GZIP, PS);
				// Execute script
				script_obj.run();
				// Update log item 
				new_li.setStopTime(new Timestamp(new Date().getTime()));
				new_li.setStatus(0);
				old_li = new_li;
					// Convert averages to output tabs panes
					for (Integer shape: OUTPUT_TYPE){
						tabbedPane_Scatterplot.add(DNAShapeReference.HEADERS[shape], script_obj.getCharts(shape));
						JScrollPane scrollPane = new JScrollPane(TextAreas.get(shape), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						tabbedPane_Statistics.add(DNAShapeReference.HEADERS[shape], scrollPane);
					}
				// Update progress
				firePropertyChange("progress", x, x + 1);
		}
		firePropertyChange("log", old_li, null);
	}
}
