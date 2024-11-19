package scriptmanager.window_interface.Figure_Generation;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import scriptmanager.cli.Figure_Generation.MergeHeatMapCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.scripts.Figure_Generation.MergeHeatMapPlot;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.Figure_Generation.MergeHeatMapPlot} and
 * reporting progress
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Figure_Generation.MergeHeatMapPlot
 * @see scriptmanager.window_interface.Figure_Generation.MergeHeatMapWindow
 */
@SuppressWarnings("serial")
public class MergeHeatMapOutput extends JFrame {
	private ArrayList<File> pngFiles = null;
	private ArrayList<File> senseFile = null;
	private ArrayList<File> antiFile = null;
	private File OUT_DIR = null;

	JTabbedPane newpane;

	/**
	 * Creates a new MergeHeatMapOutput window
	 * @param in An ArrayList&lt;File&gt; containing two heat maps of the same size
	 * @param out_dir Output directory for the combned heat map
	 */
	public MergeHeatMapOutput(ArrayList<File> in, File out_dir) {
		setTitle("Merged Heatmap");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);

		newpane = new JTabbedPane(JTabbedPane.TOP);
		this.getContentPane().add(newpane);

		pngFiles = in;
		OUT_DIR = out_dir;
	}

	/**
	 * Runs the MergeHeatMapPlot script
	 * @throws IOException Invalid file or parameters
	 */
	public void run() throws IOException {
		// Group files into "sense"-containing and "anti"-containing lists
		senseFile = new ArrayList<File>();
		antiFile = new ArrayList<File>();
		for (int x = 0; x < pngFiles.size(); x++) {
			if (pngFiles.get(x).getName().toLowerCase().contains("sense")) {
				senseFile.add(pngFiles.get(x));
			} else if (pngFiles.get(x).getName().toLowerCase().contains("anti")) {
				antiFile.add(pngFiles.get(x));
			}
		}
		LogItem old_li = new LogItem("");
		for (int x = 0; x < senseFile.size(); x++) {
			// Search antiFile list for files containing the same base as the current sense PNG file and save the index
			String name = senseFile.get(x).getName();
			String BASE = name.substring(0, name.lastIndexOf("sense"));
			int matchIndex = -999;
			for (int y = 0; y < antiFile.size(); y++) {
				if (antiFile.get(y).getName().contains(BASE)) {
					matchIndex = y;
				}
			}
			// Construct output filename
			File OUT_FILEPATH = new File(BASE + "merge.png");
			if (OUT_DIR != null) {
				OUT_FILEPATH = new File(OUT_DIR.getCanonicalPath() + File.separator + OUT_FILEPATH.getName());
			}
			// Store results in JFrame window
			if (matchIndex != -999) {
				// Initialize LogItem
				String command = MergeHeatMapCLI.getCLIcommand(senseFile.get(x), antiFile.get(matchIndex), OUT_FILEPATH);
				LogItem new_li = new LogItem(command);
				firePropertyChange("log", old_li, new_li);
				// Execute script
				JLabel pic = MergeHeatMapPlot.mergePNG(senseFile.get(x), antiFile.get(matchIndex), OUT_FILEPATH);
				newpane.addTab(OUT_FILEPATH.getName(), new JScrollPane(pic, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
				// Update log item
				new_li.setStopTime(new Timestamp(new Date().getTime()));
				new_li.setStatus(0);
				old_li = new_li;
			} else {
				// no logging if no match
				// display sense without merge if no match
				JLabel pic = MergeHeatMapPlot.mergePNG(senseFile.get(x), null, OUT_FILEPATH);
				newpane.addTab(OUT_FILEPATH.getName(), new JScrollPane(pic, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
			}
			// Update progress
			firePropertyChange("progress", x, x + 1);
		}
		// Update log at completion
		firePropertyChange("log", old_li, null);
	}
}