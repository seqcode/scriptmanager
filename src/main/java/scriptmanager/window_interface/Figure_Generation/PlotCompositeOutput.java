package scriptmanager.window_interface.Figure_Generation;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartPanel;

import scriptmanager.cli.Figure_Generation.CompositePlotCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.scripts.Figure_Generation.PlotComposite;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Figure_Generation.PlotComposite} and
 * reporting composite results
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.Figure_Generation.PlotComposite
 * @see scriptmanager.window_interface.Figure_Generation.PlotCompositeWindow
 */
@SuppressWarnings("serial")
public class PlotCompositeOutput extends JFrame {

	protected static ArrayList<File> SAMPLE = null;
	protected static File OUT_DIR;
	protected static boolean OUTPUT_STATUS;
	
	/**
	 * Whether or not to include a legend
	 */
	protected static boolean includeLegend = true;
	protected static int pxHeight;
	protected static int pxWidth;

	JTabbedPane newpane;

	/**
	 * Store inputs and initialize a tabbed pane to display the results.
	 * 
	 * @param in list of files to make composites from (one chart per file)
	 * @param o_dir output directory save composites to
	 * @param o whether to save composites or not (save = true, don't save = false)
	 * @param l whether or not to include a legend (include legend = true, don't include = false)
	 * @param ph height of image to save (in pixels)
	 * @param pw width of image to save (in pixels)
	 */
	public PlotCompositeOutput(ArrayList<File> in, File o_dir, boolean o, boolean l, int ph, int pw) {
		setTitle("Composite");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 800, 600);

		newpane = new JTabbedPane(JTabbedPane.TOP);
		this.getContentPane().add(newpane);

		SAMPLE = in;
		OUT_DIR = o_dir;
		OUTPUT_STATUS = o;
		includeLegend = l;
		pxHeight = ph;
		pxWidth = pw;
	}

	/**
	 * Loop through and call the script on each input file to generate the line
	 * charts and add them to the tabbed pane to be displayed.
	 * <br>
	 * Each chart's title will be the filename. Legend will be included if
	 * indicated by input. Default colors will be used for the chart (see
	 * scripts.Figure_Generation.PlotComposite for details). If specified by the
	 * input parameter values, a PNG image is saved to the indicated output
	 * directory with the passed pixel values for the saved file dimensions.
	 * 
	 * @throws IOException Invalid file or parameters
	 */
	public void run() throws IOException {
		LogItem old_li = null;
		for (int x = 0; x < SAMPLE.size(); x++) {
			// Initialize LogItem
			String command = CompositePlotCLI.getCLIcommand(SAMPLE.get(x), OUT_DIR, SAMPLE.get(x).getName(), null, includeLegend, pxHeight, pxWidth);
			LogItem new_li = new LogItem(command);
			if (OUTPUT_STATUS) { firePropertyChange("log", old_li, new_li); }
			// Execute script
			ChartPanel chart = new ChartPanel(PlotComposite.plotCompositeFile(SAMPLE.get(x), OUT_DIR, OUTPUT_STATUS, SAMPLE.get(x).getName(), null, includeLegend, pxHeight, pxWidth));
			// Update log item
			new_li.setStopTime(new Timestamp(new Date().getTime()));
			new_li.setStatus(0);
			old_li = new_li;
			// Output image to display
			chart.setPreferredSize(new java.awt.Dimension(500, 270));
			newpane.addTab(SAMPLE.get(x).getName(), chart);
			// Update progress
			firePropertyChange("progress", x, x + 1);
		}
		// Update log at completion
		if (OUTPUT_STATUS) { firePropertyChange("log", old_li, null); }
	}
}
