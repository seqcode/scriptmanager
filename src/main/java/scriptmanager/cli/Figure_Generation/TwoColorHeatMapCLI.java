package scriptmanager.cli.Figure_Generation;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Figure_Generation.TwoColorHeatMap;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Figure_Generation.TwoColorHeatMap}
 * 
 * @author Olivia Lang
 */
@Command(name = "heatmap",
	mixinStandardHelpOptions = true,
	description = ToolDescriptions.heatmap_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class TwoColorHeatMapCLI implements Callable<Integer> {

	@Parameters(index = "0", description = "")
	private File CDT;

	@Option(names = { "-o", "--output" }, description = "specify output filename, please use PNG extension\n(default=CDT filename with \"_<compression-type>.png\" appended to the name in working directory of ScriptManager")
	private File output = null;
	@Option(names = { "-r", "--start-row" }, description = "")
	private int startROW = 1;
	@Option(names = { "-l", "--start-col" }, description = "")
	private int startCOL = 2;
	@Option(names = { "-x", "--width" }, description = "indicate a pixel width for the heatmap (default=200)")
	private int pixelWidth = 200;
	@Option(names = { "-y", "--height" }, description = "indicate a pixel height for the heatmap (default=600)")
	private int pixelHeight = 600;
	@Option(names = { "-z", "--compression" }, description = "choose an image compression type: 1=Treeview, 2=Bicubic, 3=Bilinear, 4=Nearest Neighbor (default=1Treeview)")
	private int compression = 1;
	@Option(names = { "-a",
			"--absolute-threshold" }, description = "use the specified value for contrast thresholding in the heatmap (default=10)")
	private double absolute = -999;
	@Option(names = { "-p",
			"--percentile-threshold" }, description = "use the specified percentile value for contrast thresholding in the heatmap (try .95 if unsure)")
	private double percentile = -999;

	@ArgGroup(exclusive = true, multiplicity = "0..1", heading = "%nSelect heatmap color:%n\t@|fg(red) (select no more than one of these options)|@%n")
	private ColorGroup color = new ColorGroup();
	static class ColorGroup {
		@Option(names = { "--black" }, description = "Use the color black for generating the heatmap (default)")
		private boolean black = false;
		@Option(names = { "--red" }, description = "Use the color red for generating the heatmap")
		private boolean red = false;
		@Option(names = { "--blue" }, description = "Use the color blue for generating the heatmap")
		private boolean blue = false;
		@Option(names = { "-c", "--color" }, description = "For custom color: type hexadecimal string to represent colors (e.g. \"FF0000\" is hexadecimal for red).\n See <http://www.javascripter.net/faq/rgbtohex.htm> for some color options with their corresponding hex strings.\n")
		private String custom = null;
	}
	@Option(names = { "-t", "--transparent" }, description = "Value indicating transparency of heatmap, 0 to 255  (default=255)\n")
	private int alpha = 255;

	@Option(names = { "-b", "--background" }, description = "Set a transparent background for the heatmap minimum values (default=white)\n")
	private boolean transparentBackground = false;

	String scaleType = "treeview";
	Color MAXCOLOR = Color.BLACK;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">TwoColorHeatMapCLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		// Generate HeatMap
		TwoColorHeatMap script_object = new TwoColorHeatMap(CDT, MAXCOLOR, startROW, startCOL, pixelHeight, pixelWidth,
				scaleType, absolute, percentile, output, true, transparentBackground);
		script_object.run();

		System.err.println("Image Generated.");
		return (0);
	}

	private String validateInput() throws IOException {
		String r = "";

		// Assign a scaleType based on "compression" input
		if (compression == 2) {
			scaleType = "bicubic";
		} else if (compression == 3) {
			scaleType = "bilinear";
		} else if (compression == 4) {
			scaleType = "neighbor";
		}

		// check inputs exist
		if (!CDT.exists()) {
			r += "(!)CDT file does not exist: " + CDT.getName() + "\n";
			return (r);
		}
		// set default output filename
		if (output == null) {
			String NAME = ExtensionFileFilter.stripExtensionIgnoreGZ(CDT);
			output = new File(NAME + "_" + scaleType + ".png");
			// check output filename is valid
		} else {
			// check directory
			if (output.getParent() == null) {
// 				System.err.println("default to current directory");
			} else if (!new File(output.getParent()).exists()) {
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}

		// check compression is a valid input value
		if (compression < 1 || compression > 4) {
			r += "(!)Compression must be integer 1-4. Please select from the available compression types.";
		}

		// check scaling is valid input
		if (absolute == -999 && percentile == -999) {
			absolute = 10;
		}
		// check pixel ranges are valid
		if (pixelHeight <= 0) {
			r += "(!)Cell height must be a positive integer value! check \"-y\" flag.\"";
		}
		if (pixelWidth <= 0) {
			r += "(!)Cell width must be a positive integer value! check \"-x\" flag.\"";
		}
		// check start row/col are valid
		/* <ADD CODE HERE> */

		// Assign a color from the -c, --red, --blue, and --black inputs
		if (color.red) {
			MAXCOLOR = Color.RED;
		} else if (color.blue) {
			MAXCOLOR = Color.BLUE;
		} else if (color.custom != null) {
			System.err.println("Decoding color: 0x" + color.custom);
			// check that hex string is formatted properly
			Pattern hexColorPat = Pattern.compile("[0-9A-Fa-f]{6}");
			Matcher m = hexColorPat.matcher(color.custom);
			if (!m.matches()) {
				r += "(!)Color must be formatted as a hexidecimal String!\n\tExpected input string format: \"[0-9A-Fa-f]{6}\"";
			} else { MAXCOLOR = Color.decode("0x" + color.custom); }
		}
		// check that Alpha channel/transparency values are formatted properly and decode/assign colors
		if (alpha<0 || alpha>255) { r += "(!)Alpha/transparency value for higher values (max) must be a numeric 0 to 255\n"; }
		else { MAXCOLOR = new Color(MAXCOLOR.getRed(), MAXCOLOR.getGreen(), MAXCOLOR.getBlue(), alpha); }

		return (r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input   the input matrix/CDT to generate a heatmap from
	 * @param color   the max-value color to use
	 * @param startR  the index of the starting row
	 * @param startC  the index of the starting column
	 * @param pHeight the height (in pixels) of the output image
	 * @param pWidth  the width (in pixels) of the output image
	 * @param scale   the image compression strategy (e.g. "treeview")
	 * @param abs     the matrix value corresponding to the maximum color value
	 *                (absolute strategy)
	 * @param quant   the matrix percentile value corresponding to the maximum color
	 *                value (quantile strategy)
	 * @param output  the output PNG file
	 * @param trans   whether or not to use transparent (vs white) min-value color
	 * @return command line to execute with formatted inputs
	 * @throws OptionException
	 */
	public static String getCLIcommand(File input, Color color, int startR, int startC, int pHeight, int pWidth, String scale,
			double abs, double quant, File output, boolean trans) throws OptionException {
		String command = "java -jar $SCRIPTMANAGER figure-generation heatmap";
		command += " " + input.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();
		if (color == Color.BLACK) {
			command += " --black ";
		} else if (color == Color.RED) {
			command += " --red ";
		} else if (color == Color.BLUE) {
			command += " --blue ";
		} else {
			String hex = Integer.toHexString(color.getRGB()).substring(2);
			command += " -c " + hex;
		}
		command += " -r " + startR;
		command += " -l " + startC;
		command += " -y " + pHeight;
		command += " -x " + pWidth;
		switch (scale) {
			case "treeview":
				command += " -z 1";
				break;
			case "bicubic":
				command += " -z 2";
				break;
			case "bilinear":
				command += " -z 3";
				break;
			case "neighbor":
				command += " -z 4";
				break;
			default:
				throw new OptionException("invalid compression string");
		}
		if (abs == -999) {
			command += " -a " + abs;
		} else {
			command += " -p " + quant;
		}
		command += trans ? " -t " : "";
		return command;
	}
}