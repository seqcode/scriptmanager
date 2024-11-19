package scriptmanager.charts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Collection of static methods for plotting interactive histograms.
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.PEStats
 * @see scriptmanager.scripts.Peak_Analysis.FRiXCalculator
 */
public class Histogram {

	/**
	 * Plot a set of bar-style plots using the frequencies (y) of values (x)
	 * parallel input arrays.
	 * 
	 * @param y the list of frequencies
	 * @param x the list of values that have frequencies (same len as y)
	 * @return the bar-style histogram chart
	 * @throws IOException Invalid file or parameters
	 */
	public static ChartPanel createBarChart(double[] y, int[] x) throws IOException {
		final XYSeries series = new XYSeries("Frequency");
		for(int i = 0; i < x.length; i++) {
			series.add((double)x[i], (double)y[i]);
		}
		final XYSeriesCollection dataset = new XYSeriesCollection(series);
		
		JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		return chartPanel;
	}

	/**
	 * Plot a set of bar-style plots using the frequencies (y) of values (x)
	 * parallel input arrays and save the chart as a PNG.
	 * 
	 * @param y the list of frequencies
	 * @param x the list of values that have frequencies (same len as y)
	 * @param output the path of the PNG file to save the chart image to
	 * @return the bar-style histogram chart
	 * @throws IOException Invalid file or parameters
	 */
	public static ChartPanel createBarChart(double[] y, int[] x, File output) throws IOException {
		final XYSeries series = new XYSeries("Frequency");
		for(int i = 0; i < x.length; i++) {
			series.add((double)x[i], (double)y[i]);
		}
		final XYSeriesCollection dataset = new XYSeriesCollection(series);
		
		JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		
		if(output != null) {
			int width = 640;
			int height = 480;
			ChartUtils.saveChartAsPNG(output, chart, width, height);
		}
		return chartPanel;
	}

	/**
	 * Helper method to turn a formatted dataset into a standard chart with specific
	 * look configurations including hard coded titles, axis labels, orientation,
	 * legend, tooltips, grid colors, etc).
	 * 
	 * @param dataset the formatted dataset to plot
	 * @return the formatted and configured histogram chart
	 * @throws IOException Invalid file or parameters
	 */
	private static JFreeChart createChart(IntervalXYDataset dataset) throws IOException {
        final JFreeChart chart = ChartFactory.createXYBarChart(
        		"Paired-End Insert Size Frequency Histogram",      // chart title
                "Insert Size (bp)",               // domain axis label
                false,
                "Frequency",                  // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        //Set Histogram Color to Red
        plot.getRenderer().setSeriesPaint(0, new Color(51, 51, 51));
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinePaint(Color.black);
        plot.setDomainGridlinePaint(Color.black);
        
        //Turn off Glossy 3D on bar chart
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardXYBarPainter());

        //Code to turn off shadows if 3D
        //renderer.setShadowVisible(false);

        //Code to set y-axis range color
        //final IntervalMarker target = new IntervalMarker(400.0, 700.0);
        //target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
        //target.setLabelAnchor(RectangleAnchor.LEFT);
        //target.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
        //target.setPaint(new Color(222, 222, 255, 128));
        //plot.addRangeMarker(target, Layer.BACKGROUND);
                
        return chart;
    }

	/**
	 * Plot a set of bar-style plots using the frequencies (y) of values (x)
	 * parallel input arrays with title "FRiX", domain axis label "Tag Count
	 * (reads)", and y-axis label "Frequency".
	 * 
	 * @param y      the list of frequencies
	 * @param x      the list of values that have frequencies (same len as y)
	 * @param name   the subtitle for the plot (FRiX - \<name\>)
	 * @return the bar-style histogram chart
	 * @throws IOException
	 */
	public static ChartPanel createFRiXBarChart(double[] y, int[] x, String name) throws IOException {
		final XYSeries series = new XYSeries("Frequency");
		for(int i = 0; i < x.length; i++) {
			series.add((double)x[i], (double)y[i]);
		}
		final XYSeriesCollection dataset = new XYSeriesCollection(series);
		
		JFreeChart chart = createFRiXChart(dataset, name);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

		return chartPanel;
	}
	
	/**
	 * Helper method to turn a formatted dataset into a standard chart with specific
	 * look configurations including hard coded titles, axis labels, orientation,
	 * legend, tooltips, grid colors, etc) for the FRiX calculator tool.
	 * 
	 * @param dataset the formatted dataset to plot
	 * @param name   the subtitle for the plot (FRiX - \<name\>)
	 * @return the formatted and configured histogram chart
	 * @throws IOException
	 */
	private static JFreeChart createFRiXChart(IntervalXYDataset dataset, String name) throws IOException {
		final JFreeChart chart = ChartFactory.createXYBarChart(
				"FRiX - " + name, // chart title
				"Tag Count (reads)", // domain axis label
				false, "Frequency", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips
				false // urls
		);
		XYPlot plot = (XYPlot) chart.getPlot();
		// Set Histogram Color to Red
		plot.getRenderer().setSeriesPaint(0, new Color(51, 51, 51));
		plot.setBackgroundPaint(Color.white);
		plot.setRangeGridlinePaint(Color.black);
		plot.setDomainGridlinePaint(Color.black);
//		ValueAxis domain = plot.getDomainAxis();
//		domain.setMinorTickCount(0);

		// Turn off Glossy 3D on bar chart
		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardXYBarPainter());

		// Code to turn off shadows if 3D
		// renderer.setShadowVisible(false);

		// Code to set y-axis range color
		// final IntervalMarker target = new IntervalMarker(400.0, 700.0);
		// target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
		// target.setLabelAnchor(RectangleAnchor.LEFT);
		// target.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
		// target.setPaint(new Color(222, 222, 255, 128));
		// plot.addRangeMarker(target, Layer.BACKGROUND);

		return chart;
	}
}
