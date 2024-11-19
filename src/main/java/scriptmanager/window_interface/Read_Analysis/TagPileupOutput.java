package scriptmanager.window_interface.Read_Analysis;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.SpringLayout;

import scriptmanager.charts.CompositePlot;
import scriptmanager.objects.PileupParameters;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.objects.CustomOutputStream;
import scriptmanager.objects.LogItem;
import scriptmanager.util.BAMUtilities;

import scriptmanager.cli.Read_Analysis.TagPileupCLI;
import scriptmanager.scripts.Read_Analysis.TagPileup;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Read_Analysis.TagPileup} and reporting composite
 * results
 * 
 * @author William KM Lai
 * @see scriptmanager.window_interface.Read_Analysis.TagPileupWindow
 */
@SuppressWarnings("serial")
public class TagPileupOutput extends JFrame {
	Vector<File> BEDFiles = null;
	Vector<File> BAMFiles = null;

	ArrayList<Color> COLORS;
	PileupParameters PARAM = null;
	PrintStream COMPOSITE = null;

	final JLayeredPane layeredPane;
	final JTabbedPane tabbedPane;
	final JTabbedPane tabbedPane_Scatterplot;
	final JTabbedPane tabbedPane_Statistics;

	/**
	 * Store inputs and initialize a tabbed pane to display composite plot results
	 * and the composite plot values.
	 * 
	 * @param be     the list of input BED coordinate RefPT files
	 * @param ba     the list of input BAM tag alignment files
	 * @param param  the custom object to store configurations for how to perform
	 *               the TagPileup
	 * @param colors the list of colors to use for the composite plots
	 */
	public TagPileupOutput(Vector<File> be, Vector<File> ba, PileupParameters param, ArrayList<Color> colors) {
		setTitle("Tag Pileup Composite");
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
		tabbedPane.addTab("Pileup Plot", null, tabbedPane_Scatterplot, null);

		tabbedPane_Statistics = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Pileup Statistics", null, tabbedPane_Statistics, null);

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
					String command = TagPileupCLI.getCLIcommand(XBED, BAM, PARAM);
					LogItem new_li = new LogItem(command);
					firePropertyChange("log", old_li, new_li);

					// Execute script
					TagPileup script_obj = new TagPileup(XBED, BAM, PARAM, ps, null);
					script_obj.run();
					// Update log item
					new_li.setStopTime(new Timestamp(new Date().getTime()));
					new_li.setStatus(0);
					old_li = new_li;

					// Make composite plots
					if (PARAM.getStrand() == PileupParameters.SEPARATE) {
						tabbedPane_Scatterplot.add(BAM.getName(), CompositePlot.createCompositePlot(script_obj.DOMAIN, script_obj.AVG_S1, script_obj.AVG_S2, BEDFiles.get(BED_Index).getName(), COLORS));
					} else {
						tabbedPane_Scatterplot.add(BAM.getName(), CompositePlot.createCompositePlot(script_obj.DOMAIN, script_obj.AVG_S1, BEDFiles.get(BED_Index).getName(), COLORS));
					}

					// Add statistics to new tab
					STATS.setCaretPosition(0);
					JScrollPane newpane = new JScrollPane(STATS, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					tabbedPane_Statistics.add(BAM.getName(), newpane);

					// Update progress
					firePropertyChange("progress", PROGRESS, PROGRESS + 1);
					PROGRESS++;
				}
			}
		}
		firePropertyChange("log", old_li, null);
	}

}
