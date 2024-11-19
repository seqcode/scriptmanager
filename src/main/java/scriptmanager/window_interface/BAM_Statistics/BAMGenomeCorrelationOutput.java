package scriptmanager.window_interface.BAM_Statistics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;

import scriptmanager.cli.BAM_Statistics.BAMGenomeCorrelationCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.scripts.BAM_Statistics.BAMGenomeCorrelation;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.BAM_Statistics.BAMGenomeCorrelation} and
 * reporting the correlation heatmap and values
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.BAMGenomeCorrelation
 * @see scriptmanager.window_interface.BAM_Statistics.BAMGenomeCorrelationWindow
 */
@SuppressWarnings("serial")
public class BAMGenomeCorrelationOutput extends JFrame {
	
	Vector<File> bamFiles = null;
	String[] fileID = null;
	private File OUT_DIR = null;
	private boolean OUTPUT_STATUS = false;
	private int SHIFT;
	private int BIN;
	private int CPU;
	private int READ;
	private short COLORSCALE;
	
	final JLayeredPane layeredPane;
	final JTabbedPane tabbedPane;

	/**
	 * Creates new BAMGenomeCorrelationOutput
	 * @param input Vector containing bam files
	 * @param o Base name for output files
	 * @param out Specifies if an output file should be generated
	 * @param s The tag shift in #of base pairs
	 * @param b The bin size in #of base pairs
	 * @param c Number of CPU's to use
	 * @param r Specifies which reads to correlate 
	 * @param cs Color scale to use when generating heatmap
	 */
	public BAMGenomeCorrelationOutput(Vector<File> input, File o, boolean out, int s, int b, int c, int r, short cs) {
		setTitle("Genome Correlation");
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
		fileID = new String[bamFiles.size()];
		OUT_DIR = o;
		OUTPUT_STATUS = out;
		SHIFT = s;
		BIN = b;
		CPU = c;
		READ = r;
		COLORSCALE = cs;
	}
	
	/**
	 * Runs the analysis and displays results
	 * @throws FileNotFoundException
	 * @throws IOException invalid file or parameters
	 * @throws OptionException
	 */
	public void run() throws FileNotFoundException, IOException, OptionException {
		// Construct output filename
		String NAME = "correlation_matrix.txt";
		File OUT_FILEPATH = new File(NAME);
		if (OUT_DIR != null) {
			OUT_FILEPATH = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME);
		}

		// Initialize LogItem
		String command = BAMGenomeCorrelationCLI.getCLIcommand(bamFiles, OUT_FILEPATH, SHIFT, BIN, CPU, READ, COLORSCALE );
		LogItem li = new LogItem(command);
		if (OUTPUT_STATUS) { firePropertyChange("log", null, li); }
		// Execute script
		BAMGenomeCorrelation script_obj = new BAMGenomeCorrelation(bamFiles, OUT_FILEPATH, SHIFT, BIN, CPU, READ, COLORSCALE );
		script_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progress" == evt.getPropertyName()) {
					firePropertyChange("progress", (Integer) evt.getOldValue(), (Integer) evt.getNewValue());
				}
			}
		});
		script_obj.getBAMGenomeCorrelation(true);

		// Update log item
		li.setStopTime(new Timestamp(new Date().getTime()));
		li.setStatus(0);
		// Add plot and data to tabs
		tabbedPane.addTab("Correlation Plot", script_obj.getHeatMap());
		tabbedPane.addTab("Correlation Data", makeTablePanel(script_obj.getMatrix()));
		
		//Make frame visible at completion of correlations
		this.setVisible(true);
		// Update log at completion
		if (OUTPUT_STATUS) { firePropertyChange("log", li, null); }
	}
		
	/**
	 * Makes the matrix panel based on given matrix of correlation data
	 * @param MATRIX Matrix of correlation data
	 * @return Matrix correlation panel
	 */
	public JScrollPane makeTablePanel(double[][] MATRIX) {
		JTable table = new JTable(MATRIX.length, MATRIX.length);
		table.setName("Correlation Matrix");
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		for(int i = 0; i < MATRIX.length; i++) {
			for(int j = 0; j < MATRIX.length; j++) {
				if(i == j) table.setValueAt(1, i, j);
				else if((i - j) >= 1) {
					table.setValueAt(MATRIX[i][j], i, j);
					table.setValueAt(MATRIX[j][i], j, i);
				}
			}
		}
		for(int i = 0; i < bamFiles.size(); i++) table.getColumnModel().getColumn(i).setHeaderValue(bamFiles.get(i).getName());
		table.setPreferredSize(table.getPreferredSize());
		JScrollPane pane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		pane.setPreferredSize(new Dimension(590, 590));
		return pane;
	}
}