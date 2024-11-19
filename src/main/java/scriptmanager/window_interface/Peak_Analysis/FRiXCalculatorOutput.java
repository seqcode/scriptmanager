package scriptmanager.window_interface.Peak_Analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLayeredPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.table.DefaultTableModel;

import scriptmanager.objects.PasteableTable;
import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.PileupParameters;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.BAMUtilities;

import scriptmanager.cli.Peak_Analysis.FRiXCalculatorCLI;
import scriptmanager.scripts.Peak_Analysis.FRiXCalculator;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Peak_Analysis.FRiXCalculator} and displaying
 * table of FRiX statistics with histogram.
 * 
 * @author Olivia Lang
 * @see scriptmanager.window_interface.Peak_Analysis.FRiXCalculatorWindow
 */
@SuppressWarnings("serial")
public class FRiXCalculatorOutput extends JFrame {
	Vector<File> BEDFiles = null;
	Vector<File> BAMFiles = null;

	ArrayList<Color> COLORS;
	PileupParameters PARAM = null;
	PrintStream COMPOSITE = null;

	DefaultTableModel expTable;
	JTable table_AllStats;
	Object[][] allStatsMatrix;

	final JLayeredPane layeredPane;
	final JTabbedPane tabbedPane;
	final JTabbedPane tabbedPane_Histogram;
	final JScrollPane tablePane_Statistics;

	/**
	 * Store inputs and initialize a tabbed pane to display tag frequency
	 * distribution across reference sites and FRiX scores, density, and other
	 * metrics of enrichment around reference points.
	 * 
	 * @param be     the list of input BED coordinate RefPT files
	 * @param ba     the list of input BAM tag alignment files
	 * @param param  the custom object to store configurations for how to perform
	 *               the TagPileup
	 * @param colors the list of colors to use for the composite plots
	 */
	public FRiXCalculatorOutput(Vector<File> be, Vector<File> ba, PileupParameters param, ArrayList<Color> colors) {
		setTitle("FRiX Calculator Output");
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

		// Build histogram pane
		tabbedPane_Histogram = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Histogram Plot", null, tabbedPane_Histogram, null);

		// Initialize data matrix
		int nm = be.size() * ba.size();
		allStatsMatrix = new Object[nm][10];
		
		// Initialize table object and add to pane
		table_AllStats = new JTable(allStatsMatrix, FRiXCalculator.COLUMN_NAMES);
		table_AllStats.setName("FRiX Statistics");
		table_AllStats.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table_AllStats.setPreferredSize(table_AllStats.getPreferredSize());
		table_AllStats.setFillsViewportHeight(true);
		@SuppressWarnings("unused")
		PasteableTable myAd = new PasteableTable(table_AllStats);

		// Build statistics pane
		tablePane_Statistics = new JScrollPane(table_AllStats);
		tablePane_Statistics.setPreferredSize(new Dimension(590, 590));
		tabbedPane.addTab("FRiX Statistics", null, tablePane_Statistics, null);

		// Store inputs
		BEDFiles = be;
		BAMFiles = ba;
		PARAM = param;
		COLORS = colors;
	}

	/**
	 * Loop through and call the script on each combination of BED, BAM pair
	 * inputs, build a chart for each composite result, and display that chart
	 * by adding it to the "Pileup Plot" tabbed pane. Raw results will also be
	 * added under the "Pileup Statistics" tabbed pane. <br>
	 * Each chart's BED filename will be the title of the chart under a tab
	 * labeled with the BAM filename. These tabs are "subtabs" in the "Pileup
	 * Plot" tab.
	 * 
	 * @throws IOException Invalid file or parameters
	 * @throws OptionException invalid input values for read, aspect, or strand
	 */
	public void run() throws OptionException, IOException {
		// Check if BAI index file exists for all BAM files
		boolean[] BAMvalid = new boolean[BAMFiles.size()];
		for (int z = 0; z < BAMFiles.size(); z++) {
			File BAM = BAMFiles.get(z); // Pull current BAM file
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
		int PROGRESS = 0;
		for (int z = 0; z < BAMFiles.size(); z++) {
			File BAM = BAMFiles.get(z); // Pull current BAM file
			if (BAMvalid[z]) {
				// Code to standardize tags sequenced to genome size (1 tag / 1 bp)
				if (PARAM.getStandard() && PARAM.getBlacklist() != null) {
					PARAM.setRatio(BAMUtilities.calculateStandardizationRatio(BAM, PARAM.getBlacklist(), PARAM.getRead()));
				} else if (PARAM.getStandard()) {
					PARAM.setRatio(BAMUtilities.calculateStandardizationRatio(BAM, PARAM.getRead()));
				}
				// Loop through each BED file
				for (int BED_Index = 0; BED_Index < BEDFiles.size(); BED_Index++) {
					File XBED  = BEDFiles.get(BED_Index);
					System.err.println( "Processing BAM: " + BAM.getName() + "\tCoordinate: " + XBED.getName());
					// Generate statistics object for printing composite results
					JTextArea STATS = new JTextArea();
					STATS.setEditable(false); // Make it un-editable
					PrintStream ps = new PrintStream(new CustomOutputStream(STATS));
					// Initialize LogItem
					String command = FRiXCalculatorCLI.getCLIcommand(XBED, BAM, PARAM);
					LogItem new_li = new LogItem(command);
					firePropertyChange("log", old_li, new_li);
					// Execute script
					FRiXCalculator script_obj = new FRiXCalculator(BEDFiles.get(BED_Index), BAM, PARAM, ps, null);
					script_obj.run();
					// Update log item
					new_li.setStopTime(new Timestamp(new Date().getTime()));
					new_li.setStatus(0);
					old_li = new_li;

					// Update MATRIX with data row from script_obj
					Object[] mRow = script_obj.getMatrixRow();
					for (int i = 0; i < mRow.length; i++) {
						allStatsMatrix[PROGRESS][i] = mRow[i];
					}
					updateTable();

					// Make histogram plots
//					double[] data = {1.0, 2.0, 3.0};
//					int[] vals = {1, 2, 3};
//					Component chart = Histogram.createBarChart(data, vals);
					tabbedPane_Histogram.add(BAM.getName(), script_obj.histChart);
					// Update progress
					firePropertyChange("progress", PROGRESS, PROGRESS + 1);
					PROGRESS++;
				}
			}
		}
		firePropertyChange("log", old_li, null);
	}


	/*
	public void run() throws IOException {
		//Open Output File
		if(OUTPUT_STATUS) {
				String NAME = "correlation_matrix";
				if(OUTPUT_PATH != null) {
					try { OUT = new File(OUTPUT_PATH.getCanonicalPath() + File.separator + NAME); }
					catch (FileNotFoundException e) { e.printStackTrace(); }
					catch (IOException e) { e.printStackTrace(); }
				} else {
					OUT = new File(NAME);
				}
			} else {
				OUTPUT_PATH = null;
			}
			BAMGenomeCorrelation script_obj = new BAMGenomeCorrelation( bamFiles, OUT, SHIFT, BIN, CPU, READ, COLORSCALE );
			script_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress" == evt.getPropertyName()) {
						firePropertyChange("progress", (Integer) evt.getOldValue(), (Integer) evt.getNewValue());
					}
				}
			});
			script_obj.getBAMGenomeCorrelation(true);

			tabbedPane.addTab("Correlation Plot", script_obj.getHeatMap());
			tabbedPane.addTab("Correlation Data", makeTablePanel(script_obj.getMatrix()));
			
			//Make frame visible at completion of correlations
			this.setVisible(true);
		}
		
	public JScrollPane makeTablePanel() {
		JTable table = new JTable(MATRIX.length, MATRIX.length);
		table.setName("FRiX Statistics");
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
		for (int i = 0; i < BEDFiles.size(); i++) {
			for (int j = 0; j < BAMFiles.size(); j++) {
				table.getColumnModel().getColumn(i).setHeaderValue(BAMFiles.get(j).getName());
			}
		}
		table.setPreferredSize(table.getPreferredSize());
		JScrollPane pane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		pane.setPreferredSize(new Dimension(590, 590));
		return pane;
	}
	*/

	/**
	 * Update the JTable from the logItems ArrayList
	 */
	public void updateTable() {
//		int n = logItems.size();
//		String[][] d = new String[n][3];
//		for(int i = 0; i<n; i++) {
//			d[i] = logItems.get(i).toStringArray();
//		}

		DefaultTableModel expTable = new DefaultTableModel(allStatsMatrix, FRiXCalculator.COLUMN_NAMES) {
			@Override
			public Class<?> getColumnClass(int col) {
				return getValueAt(0, col).getClass();
			}

//			@Override
//			public boolean isCellEditable(int row, int column) {
//				return false;
//			}
		};
		table_AllStats.setModel(expTable);
	}

	/**
	 * Regenerates the JTable from the LogInfo object
	 */
	public void drawTable() {
	}
}
