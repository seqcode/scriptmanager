package scriptmanager.window_interface.Figure_Generation;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import scriptmanager.cli.Figure_Generation.TwoColorHeatMapCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.scripts.Figure_Generation.TwoColorHeatMap;
import scriptmanager.util.ExtensionFileFilter;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Figure_Generation.TwoColorHeatMap} and reporting
 * heatmap results
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Figure_Generation.TwoColorHeatMap
 * @see scriptmanager.window_interface.Figure_Generation.TwoColorHeatMapWindow
 */
@SuppressWarnings("serial")
public class TwoColorHeatMapOutput extends JFrame {

	protected static ArrayList<File> SAMPLE = null;

	protected static int startROW = 1;
	protected static int startCOL = 2;
	protected static int pixelHeight = 600;
	protected static int pixelWidth = 200;

	protected static String scaleType = "treeview";
	protected static double quantile = 0.9;
	protected static double absolute = -999;

	public static Color MAXCOLOR = new Color(255, 0, 0);
	public boolean transparentBackground = false;

	protected static boolean OUTPUT_STATUS = false;
	protected static File OUT_DIR = null;

	public static double COLOR_RATIO = 1;

	JTabbedPane newpane;

	/**
	 * Creates a new instance of a TwoColorHeatMapOutput with given attributes
	 * @param in Matrix file for heat map to represent
	 * @param c Color to represent maximum values
	 * @param startR Starting row of the CDT file (Zero indexed)
	 * @param startC Starting column of the CDT file (Zero indexed)
	 * @param pHeight Height of resulting heat map (# pixels)
	 * @param pWidth Width of resulting heat map (# pixels)
	 * @param scale Scale compression type
	 * @param abs The difference in values for each step of the color scale
	 * @param quant The difference in percent of values for each step of the color scale
	 * @param out_dir Directory to output PNG to
	 * @param outstatus Whether or not to output a PNG
	 * @param trans If min values should be transparent
	 */
	public TwoColorHeatMapOutput(ArrayList<File> in, Color c, int startR, int startC, int pHeight, int pWidth,
			String scale, double abs, double quant, File out_dir, boolean outstatus, boolean trans) {
		setTitle("Heatmap");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);

		newpane = new JTabbedPane(JTabbedPane.TOP);
		this.getContentPane().add(newpane);

		SAMPLE = in;
		MAXCOLOR = c;
		transparentBackground = trans;
		startROW = startR;
		startCOL = startC;
		pixelHeight = pHeight;
		pixelWidth = pWidth;
		scaleType = scale;

		absolute = abs;
		quantile = quant;

		OUT_DIR = out_dir;
		OUTPUT_STATUS = outstatus;
	}

	public void run() throws IOException, OptionException {
		LogItem old_li = null;
		for (int x = 0; x < SAMPLE.size(); x++) {
			// Construct output filename
			String NAME = ExtensionFileFilter.stripExtensionIgnoreGZ(SAMPLE.get(x)) + "_" + scaleType + ".png";
			File OUT_FILEPATH = new File(NAME);
			if (OUT_DIR != null) {
				OUT_FILEPATH = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME);
			}
			// Initialize LogItem
			String command = TwoColorHeatMapCLI.getCLIcommand(SAMPLE.get(x), MAXCOLOR, startROW, startCOL,
					pixelHeight, pixelWidth, scaleType, absolute, quantile, OUT_FILEPATH, transparentBackground);
			LogItem new_li = new LogItem(command);
			if (OUTPUT_STATUS) { firePropertyChange("log", old_li, new_li); }
			// Execute script
			TwoColorHeatMap script_object = new TwoColorHeatMap(SAMPLE.get(x), MAXCOLOR, startROW, startCOL,
					pixelHeight, pixelWidth, scaleType, absolute, quantile, OUT_FILEPATH, OUTPUT_STATUS, transparentBackground);
			script_object.run();
			JLabel picLabel = script_object.getImg();
			// Update log item
			new_li.setStopTime(new Timestamp(new Date().getTime()));
			new_li.setStatus(0);
			old_li = new_li;
			// Output image/error to GUI
			newpane.addTab(OUT_FILEPATH.getName(), new JScrollPane(picLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
			// Update progress
			firePropertyChange("progress", x, x + 1);
		}
		// Update log at completion
		if (OUTPUT_STATUS) { firePropertyChange("log", old_li, null); }
	}
}
