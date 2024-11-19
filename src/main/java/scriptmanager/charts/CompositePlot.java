package scriptmanager.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Collection of static methods for plotting lines of sense {@literal &}
 * antisense or combined composite plots with customizable titles, colors, and
 * legends.
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED
 * @see scriptmanager.scripts.Sequence_Analysis.DNAShapefromFASTA
 * @see scriptmanager.window_interface.Read_Analysis.TagPileupOutput
 */
public class CompositePlot {

	/** 
	* Creates a new CompositePlot object
	*/
	public CompositePlot(){}

	/**
	 * Create a two-line plot (sense and antisense composites) with a title and
	 * custom colors. There are no checks on input array lengths (line plot
	 * determined by length of domain array).
	 * 
	 * @param x      the domain values array
	 * @param y1     "Sense Strand" values array
	 * @param y2     "Anti Strand" values array
	 * @param name   title of the plot
	 * @param COLORS a 2-element array of colors of the sense and antisense lines
	 *               (first color = sense, second color = anti)
	 * @return the line plot for display
	 */
	public static ChartPanel createCompositePlot(double[] x, double[] y1, double[] y2, String name, ArrayList<Color> COLORS){
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final XYSeries seriesF = new XYSeries("Sense Strand");
		final XYSeries seriesR = new XYSeries("Anti Strand");
		
		for(int i = 0; i < x.length; i++) {
			seriesF.add(x[i], y1[i]);
			seriesR.add(x[i], y2[i]);
		}
		dataset.addSeries(seriesF);
		dataset.addSeries(seriesR);

		final JFreeChart chart = createChart(dataset, name, COLORS);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		return chartPanel;
	}

	/**
	 * Create a one-line plot (combined composites) with a title and custom color.
	 * There are no checks on input array lengths (line plot determined by length of
	 * domain array).
	 * 
	 * @param x      the domain values array
	 * @param y1     "Combined" values array
	 * @param name   title of the plot
	 * @param COLORS a 1-element array of colors of the sense and antisense lines
	 *               (first color = sense, second color = anti)
	 * @return the line plot for display
	 */
	public static Component createCompositePlot(double[] x, double[] y1, String name, ArrayList<Color> COLORS){
		final XYSeriesCollection dataset = new XYSeriesCollection();
		final XYSeries seriesC = new XYSeries("Data");
		for(int i = 0; i < x.length; i++) {
			seriesC.add(x[i], y1[i]);
		}
		dataset.addSeries(seriesC);
		
		final JFreeChart chart = createChart(dataset, name, COLORS);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		return chartPanel;
	}

	/**
	 * Create a one-line plot (combined composites) with a title using a black line.
	 * There are no checks on input array lengths (line plot determined by length of
	 * domain array).
	 * 
	 * @param x    the domain values array
	 * @param y1   "Combined" values array
	 * @param name title of the plot
	 * @return the line plot for display
	 */
	public static Component createCompositePlot(double[] x, double[] y1, String name){
		ArrayList<Color> COLORS = new ArrayList<Color>();
		COLORS.add(Color.BLACK);
		return(createCompositePlot(x, y1, name, COLORS));
	}

	/**
	 * Create line chart with a legend
	 * 
	 * @param dataset the (x,y) values (agnostic to number of series)
	 * @param TITLE   title of the plot
	 * @param COLORS  colors to use for each data series (depends on dataset)
	 * @return the line plot
	 */

	public static JFreeChart createChart(final XYDataset dataset, String TITLE, ArrayList<Color> COLORS) {
		return(createChart(dataset, TITLE, COLORS, true));
	}

	/**
	 * Create line chart with an optional legend
	 * 
	 * @param dataset the (x,y) values (agnostic to number of series)
	 * @param TITLE   title of the plot
	 * @param COLORS  colors to use for each dataseries (depends on dataset)
	 * @param legend  to print legend describing series
	 * @return the line plot
	 */
	public static JFreeChart createChart(final XYDataset dataset, String TITLE, ArrayList<Color> COLORS, boolean legend) {
		//Call Chart
		final JFreeChart chart = ChartFactory.createXYLineChart(
				TITLE, // chart title
				"Distance from Feature (bp)", // x axis label
				"Score", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, legend, // include legend
				true, // tooltips
				false // urls
				);

		chart.setBackgroundPaint(Color.white);
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.black);
		plot.setRangeGridlinePaint(Color.black);

		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		for(int x = 0; x < COLORS.size(); x++) {
			renderer.setSeriesLinesVisible(x, true);	//Spline visibility
			renderer.setSeriesShapesVisible(x, false);	//Data point dot visibility
			renderer.setSeriesStroke(x, new BasicStroke(3));
			renderer.setSeriesPaint(x, COLORS.get(x));
		}
// 		renderer.setSeriesPaint(0, COLORS.get(0));
// 		if(COLORS.size() == 2) { renderer.setSeriesPaint(1, COLORS.get(1)); }
		
		plot.setRenderer(renderer);
		// change the auto tick unit selection to integer units only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		domainAxis.setAxisLineVisible(true);
		rangeAxis.setAxisLineVisible(true);
		return chart;
	}
}
