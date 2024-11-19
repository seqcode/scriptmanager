package scriptmanager.scripts.Figure_Generation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import scriptmanager.objects.Exceptions.OptionException;

/**
 * Decorate an input PNG file with plot labels and save as an SVG. <br>
 * Code sourced from <a href=
 * "https://github.com/WilliamKMLai/jHeatmapLabel">https://github.com/WilliamKMLai/jHeatmapLabel</a>
 * <br>
 * (Dated March 15, 2021)
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Figure_Generation.LabelHeatMapCLI
 * @see scriptmanager.window_interface.Figure_Generation.LabelHeatMapOutput
 * @see scriptmanager.window_interface.Figure_Generation.LabelHeatMapWindow
 */
public class LabelHeatMap {

	private static File INPUT = null;
	private static File OUTPUT = new File("OutputHeatmap.svg");

	private static Color color = Color.BLACK; // Color.BLACK
	private static Integer borderWidth = 2; // Set thickness of border and tickmarks
	private static Integer XtickHeight = 10; // Height of X-axis tickmarks
	private static Integer FONTSIZE = 18; // Set font size

	private static String XleftLabel = ""; // Left X-tickmark label
	private static String XmidLabel = ""; // Mid X-tickmark label
	private static String XrightLabel = ""; // Right X-tickmark label

	private static String xLabel = ""; // X-axis label
	private static String yLabel = ""; // Y-axis label

	private static PrintStream outPS = null;

	/**
	 * Initialize labeling parameters in this constructor
	 *
	 * @param iNPUT       the PNG input to format the labels and outline around
	 * @param oUTPUT      the filepath for the SVG-formatted labeled PNG
	 * @param cOLOR       the color of all the lines and text annotating the PNG
	 * @param bORDERWIDTH the thickness of the line outlining the PNG
	 * @param xTICKHEIGHT the length (in pixels) of the tickmarks on the x-axis (no
	 *                    tickmarks on y-axis)
	 * @param fONTSIZE    the size of all text labels on the annotated SVG
	 * @param xLEFTLABEL  the text label for the far left tickmark on the x-axis
	 * @param xMIDLABEL   the text label for the midpoint tickmark on the x-axis
	 * @param xRIGHTLABEL the text label for the far right tickmark on the x-axis
	 * @param xLABEL      the text label for the x-axis
	 * @param yLABEL      the text label for the y-axis
	 * @param out_win_ps  the destination to output progress update and input
	 *                    information as the script executes
	 * @throws IOException     Invalid file or parameters
	 * @throws OptionException
	 */
	public LabelHeatMap(File iNPUT, File oUTPUT, Color cOLOR,
			Integer bORDERWIDTH, Integer xTICKHEIGHT, Integer fONTSIZE,
			String xLEFTLABEL, String xMIDLABEL, String xRIGHTLABEL,
			String xLABEL, String yLABEL, PrintStream out_win_ps) throws IOException, OptionException {

		INPUT = iNPUT;
		OUTPUT = oUTPUT;
		color = cOLOR;
		borderWidth = bORDERWIDTH;
		XtickHeight = xTICKHEIGHT;
		FONTSIZE = fONTSIZE;
		XleftLabel = xLEFTLABEL;
		XmidLabel = xMIDLABEL;
		XrightLabel = xRIGHTLABEL;

		xLabel = xLABEL;
		yLabel = yLABEL;

		outPS = out_win_ps;

		// validate input
		if(INPUT == null) {
			throw new OptionException("(!) No image file specified!!!");
		} else if(OUTPUT == null) {
			throw new OptionException("(!) No output file specified!!!");
		} else if(borderWidth < 1) {
			throw new OptionException("(!) Invalid line thickness selected!!!");
		} else if(XtickHeight < 1) {
			throw new OptionException("(!) Invalid X-tickmark height selected!!!");
		} else if(FONTSIZE < 1) {
			throw new OptionException("(!) Invalid font size selected!!!");
		}

		writePS("-----------------------------------------\nCommand Line Arguments:");
		System.out.println("Input image: " + INPUT);
		System.out.println("Output image: " + OUTPUT);
		System.out.println("-----------------------------------------");
		System.out.println("Output color:\t\t" + color.toString());
		System.out.println("Line thickness:\t\t" + borderWidth);
		System.out.println("X-tickmark height:\t" + XtickHeight);
		System.out.println("Font size:\t\t" + FONTSIZE);
		if(!XleftLabel.equals("")) { System.out.println("Left X-tick label:\t" + XleftLabel); }
		if(!XmidLabel.equals("")) { System.out.println("Mid X-tick label:\t" + XmidLabel); }
		if(!XrightLabel.equals("")) { System.out.println("Right X-tick label:\t" + XrightLabel); }
		if(!xLabel.equals("")) { System.out.println("X-axis label:\t\t" + xLabel); }
		if(!yLabel.equals("")) { System.out.println("Y-axis label:\t\t" + yLabel); }
	}

	/**
	 * Execute building SVG labels around the input PNG.
	 *
	 * @return 0 upon succesful execution
	 * @throws IOException Invalid file or parameters
	 */
	public Integer run() throws IOException {
		// Initialize empty SVG object
		SVGGraphics2D svg = new SVGGraphics2D(0,0);
		// Set font parameters
		svg.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));

		// Import PNG image
		BufferedImage image = ImageIO.read(INPUT);
		// Get initial image size
		int Height = image.getHeight();
		int Width = image.getWidth();

		// Adjust dimensions based on what we draw
		int rightPad = (borderWidth / 2);
		int leftPad = (borderWidth / 2);
		int bottomPad = XtickHeight - (borderWidth / 2);
		//System.out.println(leftPad + "\t" + rightPad + "\t" + bottomPad);

		// Account for x-label if exists
		if(!xLabel.equals("")) {
			bottomPad += (FONTSIZE * 1.5);
			// Account for x-label side-padding if exists
			int XlabelSize = svg.getFontMetrics().stringWidth(xLabel);
			if((XlabelSize - Width) / 2 > leftPad) {
				leftPad += ((XlabelSize - Width) / 2);
			}
			if((XlabelSize - Width) / 2 > rightPad) {
				rightPad += ((XlabelSize - Width) / 2);
			}
		}

		// Account for y-label if exists
		if(!yLabel.equals("")) {
			if(leftPad < (FONTSIZE * 1.5)) {
				leftPad = (int)(FONTSIZE * 1.5) + (borderWidth / 2);
			}
		}

		// Account for X-axis tickmark labels
		if(!XleftLabel.equals("") || !XmidLabel.equals("") || !XrightLabel.equals("")) {
			// Account for bottom padding
			bottomPad += (FONTSIZE * 1.5);

			// Account for X-axis tickmark labels on side-padding
			if(!XleftLabel.equals("")) {
				int XleftSize = svg.getFontMetrics().stringWidth(XleftLabel);
				if((XleftSize / 2) > leftPad) {
					leftPad += ((XleftSize / 2) - leftPad);
				}
			}
			if(!XrightLabel.equals("")) {
				int XrightSize = svg.getFontMetrics().stringWidth(XrightLabel);
				if((XrightSize / 2) > rightPad) {
					rightPad += ((XrightSize / 2) - rightPad);
				}
			}

		}

		// Re-initialize SVG object
		svg = new SVGGraphics2D(leftPad + Width + rightPad, Height + bottomPad);
		// Re-set font parameters
		svg.setFont(new Font("Arial", Font.PLAIN, FONTSIZE));

		int newHeight = svg.getHeight();
		int newWidth = svg.getWidth();

		svg.setColor(color);
		// Set thickness of border
		svg.setStroke(new BasicStroke(borderWidth));

		// Draw heatmap
		svg.drawImage(image, leftPad, (borderWidth / 2), null);
		// Draw rectangle around PNG
		svg.draw(new Rectangle(leftPad, (borderWidth / 2), Width, Height));

		// Draw left x-axis tickmark
		svg.drawLine(leftPad, Height + (borderWidth / 2), leftPad, Height + (borderWidth / 2) + XtickHeight);
		// Draw mid x-axis tickmark
		svg.drawLine(newWidth - (Width / 2) - rightPad, Height + (borderWidth / 2), newWidth - (Width / 2) - rightPad, Height + (borderWidth / 2) + XtickHeight);
		// Draw right x-axis tickmark
		svg.drawLine(newWidth - rightPad, Height + (borderWidth / 2), newWidth - rightPad, Height + (borderWidth / 2) + XtickHeight);

		// Draw X-axis tickmark labels
		if(!XleftLabel.equals("")) {
			int XleftSize = svg.getFontMetrics().stringWidth(XleftLabel);
			svg.drawString(XleftLabel, leftPad - (XleftSize / 2), Height + XtickHeight + FONTSIZE);
		}
		if(!XmidLabel.equals("")) {
			int XmidSize = svg.getFontMetrics().stringWidth(XmidLabel);
			svg.drawString(XmidLabel, (newWidth - (Width / 2)) - rightPad - (XmidSize / 2), Height + XtickHeight + FONTSIZE);
		}
		if(!XrightLabel.equals("")) {
			int XrightSize = svg.getFontMetrics().stringWidth(XrightLabel);
			svg.drawString(XrightLabel, newWidth - rightPad - (XrightSize / 2), Height + XtickHeight + FONTSIZE);
		}

		// Draw X-label
		if(!xLabel.equals("")) {
			int Xmidpoint = svg.getFontMetrics().stringWidth(xLabel);
			svg.drawString(xLabel, (newWidth - (Width / 2) - rightPad) - (Xmidpoint / 2), newHeight - (FONTSIZE / 2));
		}
		// Draw Y-label
		if(!yLabel.equals("")) {
			int Ymidpoint = svg.getFontMetrics().stringWidth(yLabel);
			// Remember original orientation
			AffineTransform orig = svg.getTransform();
			// Rotate drawing space 180 degrees
			svg.rotate(-Math.PI/2);
			svg.drawString(yLabel, -1 * ((Height / 2) + (Ymidpoint / 2)), newWidth - rightPad - Width - (FONTSIZE / 2));
			// Restore original orientation
			svg.setTransform(orig);
		}

		// Output file as SVG
		SVGUtils.writeToSVG(OUTPUT, svg.getSVGElement());
		return(0);
	}

	public void writePS(String message) {
		if(outPS!=null) {
			outPS.println(message);
		} else {
			System.err.println(message);
		}
	}
}
