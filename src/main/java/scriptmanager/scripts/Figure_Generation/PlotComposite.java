package scriptmanager.scripts.Figure_Generation;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import scriptmanager.charts.CompositePlot;
import scriptmanager.util.ColorSeries;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.GZipUtilities;

/**
 * Create/display line plot images based on the composite output files from
 * {@link scriptmanager.scripts.Read_Analysis.TagPileup}).
 * 
 * @author Olivia Lang
 * @see scriptmanager.util.ColorSeries
 * @see scriptmanager.charts.CompositePlot
 * @see scriptmanager.cli.Figure_Generation.CompositePlotCLI
 * @see scriptmanager.window_interface.Figure_Generation.PlotCompositeOutput
 * @see scriptmanager.window_interface.Figure_Generation.PlotCompositeWindow
 */
public class PlotComposite {

	/**
	 * Parse the input composite data (formatted like output of
	 * {@link scriptmanager.scripts.Read_Analysis.TagPileup}), determine the
	 * default color palette if none specified, and plot the line chart. Then save
	 * the image file to the appropriate output if indicated.
	 * 
	 * @param input       a tab-delimited file containing the composite information
	 *                    in the format of scripts.Figure_Generation.TagPileup's
	 *                    output
	 * @param OUT_PATH    filepath to save composite image to. if null, defaults to
	 *                    &lt;InputWithoutExtension&gt;_plot.png.
	 * @param outputImage to save image (true) or not (false)
	 * @param title       the string to include at the top of the line chart
	 * @param COLORS      the color palette list of colors to plot. If null, then a
	 *                    different color palette is chosen based on the number of
	 *                    lines parsed from the composite input file: if n=1, then
	 *                    black is used, if n=2, then the first plot is blue and the
	 *                    second is red, if n&gt;2, then the YEP color pallete is
	 *                    used.
	 * @param legend      to include the legend in the chart (true) or not (false)
	 * @param pxHeight    height of image to save
	 * @param pxWidth     width of image to save
	 * @return The composite plot
	 * @throws IOException              Invalid file or parameters
	 * @throws IllegalArgumentException File is not formatted properly
	 * @throws FileNotFoundException    Script could not find valid input file
	 */
	public static JFreeChart plotCompositeFile(File input, File OUT_PATH, boolean outputImage, String title, ArrayList<Color> COLORS, boolean legend, int pxHeight, int pxWidth) throws IOException, IllegalArgumentException, FileNotFoundException {
		BufferedReader br = GZipUtilities.makeReader(input);
		// parse x values
		String line;
		String[] tokens = {""};
		if ((line = br.readLine()) != null){ tokens = line.split("\t"); }
		if (!tokens[0].equals("")) {
			br.close();
			throw new IllegalArgumentException("(!) First row of input file must have an empty first column (as x-values)");
		}
		double[] x = new double[tokens.length - 1];
		for (int i = 1; i < tokens.length; i++) {
			x[i - 1] = Double.parseDouble(tokens[i]);
		}

		// parse input into XYDataset obj
		XYSeriesCollection dataset = new XYSeriesCollection();

		XYSeries s;
		// line-by-line through file
		while ((line = br.readLine()) != null) {
			tokens = line.split("\t");
			// check for format consistency: number of x-values matches y-values
			if (tokens.length - 1 != x.length) {
				br.close();
				throw new IllegalArgumentException("(!) Check number of x-values matches number of y-values");
			}
			// skip any rows with blank labels
			if (tokens[0].equals("")) {
				for (int i = 1; i < tokens.length; i++) {
					if (x[i - 1] != Double.parseDouble(tokens[i])) {
						System.err.println(x[i - 1]);
						System.err.println(tokens[i]);
						br.close();
						throw new IllegalArgumentException("(!) Check dataseries based on same x-scale file");
					}
				}
				continue;
			}
			// initialize series based on rowname column of input file
			s = new XYSeries(tokens[0]);
			// parse y-values of current row
			for (int i = 1; i < tokens.length; i++) {
				s.add(x[i - 1], Double.parseDouble(tokens[i]));
			}
			dataset.addSeries(s);
		}
		br.close();

		// Set-up colors
		if (COLORS==null) {
			COLORS = new ArrayList<Color>(2);
			if (dataset.getSeriesCount() == 1) { // n=1, default to black
				COLORS.add(Color.BLACK);
			} else if (dataset.getSeriesCount() == 2) { // n=2, default to red & blue  XO colors
				COLORS = ColorSeries.InitializeXOColors();
			} else if (dataset.getSeriesCount() > 2) { // n>2, default to yep colors
				COLORS = ColorSeries.InitializeYEPColors();
			}
		// assert custom colors have been assigned and length matches number of series
		} else if (COLORS.size() != dataset.getSeriesCount()) {
			System.err.println("(Caution!) Number of colors specified(" + COLORS.size() + ") doesnt match number of dataseries(" + dataset.getSeriesCount() + ")\n");
		}

		// Make chart
		JFreeChart chart = CompositePlot.createChart(dataset, title, COLORS, legend);

		// Save Composite Plot
		if (outputImage) {
			// set default output filename
			if (OUT_PATH == null) {
				OUT_PATH = new File(ExtensionFileFilter.stripExtension(input) + "_plot.png");
			} else if (OUT_PATH.isDirectory()) {
				OUT_PATH = new File(OUT_PATH.getCanonicalFile() + File.separator + ExtensionFileFilter.stripExtension(input) + "_plot.png");

			}
			ChartUtils.writeChartAsPNG(new FileOutputStream(OUT_PATH), chart, pxWidth, pxHeight);
		}

		return (chart);
	}
}
