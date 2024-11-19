package scriptmanager.charts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Collection of static methods for plotting interactive line charts.
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.PEStats
 * @see scriptmanager.scripts.Peak_Analysis.SignalDuplication
 */
public class LineChart {

	/**
	 * Create a line chart with a single line labeled "Duplication Rate".
	 * 
	 * @param y the duplication rate values
	 * @param x the domain values
	 * @return the line plot chart
	 * @throws IOException Invalid file or parameters
	 */
	public static ChartPanel createLineChart(ArrayList<Double> y, String[] x) throws IOException {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for(int i = 0; i < x.length; i++) {
			dataset.addValue(y.get(i).doubleValue(), "Duplication Rate", x[i]);
		}
		
        JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		return chartPanel;
	}

	/**
	 * Create a line chart with a two lines labeled "Signal Duplication Rate" and
	 * "Genome Duplication Rate".
	 * 
	 * @param y1 the signal duplication rate values
	 * @param y2 the genome duplication rate values
	 * @param x  the domain values
	 * @return the line plot chart
	 * @throws IOException Invalid file or parameters
	 */
	public static ChartPanel createLineChart(ArrayList<Double> y1, ArrayList<Double> y2, String[] x) throws IOException {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for(int i = 0; i < x.length; i++) {
			dataset.addValue(y1.get(i).doubleValue(), "Signal Duplication Rate", x[i]);
			dataset.addValue(y2.get(i).doubleValue(), "Genome Duplication Rate", x[i]);
		}
		
        JFreeChart chart = createChart(dataset);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		return chartPanel;
	}

	/**
	 * Create a line chart with a single line labeled "Duplication Rate" and save the chart as a PNG file.
	 * 
	 * @param y the duplication rate values
	 * @param x the domain values
	 * @param output the path of the PNG file to save the chart image to
	 * @return the line plot chart
	 * @throws IOException Invalid file or parameters
	 */
	public static ChartPanel createLineChart(ArrayList<Double> y, String[] x, File output) throws IOException {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for(int i = 0; i < x.length; i++) {
			dataset.addValue(y.get(i).doubleValue(), "Duplication Rate", x[i]);
		}
		
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
	 * @return the line plot chart
	 * @throws IOException Invalid file or parameters
	 */
	private static JFreeChart createChart(CategoryDataset dataset) throws IOException {
        final JFreeChart chart = ChartFactory.createLineChart(
        		"Paired-End Duplication Rate",      // chart title
                "Level of Duplicate Molecules",               // domain axis label
                "Frequency of Duplication",                  // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );
        
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        //Set Histogram Color to Red
        plot.getRenderer().setSeriesPaint(0, Color.blue);

        CategoryAxis xAxis = (CategoryAxis)plot.getDomainAxis();
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
        
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
}
