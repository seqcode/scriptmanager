package scriptmanager.cli.Figure_Generation;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.lang.NullPointerException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Figure_Generation.ThreeColorHeatMap;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Figure_Generation.ThreeColorHeatMap}
 * 
 * @author Olivia Lang
 */
@Command(name = "three-color", mixinStandardHelpOptions = true,
	description = ToolDescriptions.threecolorheatmap_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class ThreeColorHeatMapCLI implements Callable<Integer> {

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
	
	@ArgGroup(exclusive = true, multiplicity = "0..1", heading = "%nSelect minimum value:%n\t@|fg(red) (select no more than one of these options)|@%n")
	private MinGroup minGroup = new MinGroup();
	static class MinGroup {
		@Option(names = {"-pn"}, description = "use this percentile (as opposed to specific values) for contrast thresholding in determining max contrast value of the heatmap")
		private Double percentile = null;
		@Option(names = {"-an"}, description = "the minimum value for contrast thresholding in the heatmap (default,min=-10)")
		private Double absolute = null;
	}
	@ArgGroup(exclusive = true, multiplicity = "0..1", heading = "%nSelect middling value:%n\t@|fg(red) (select no more than one of these options)|@%n")
	private MidGroup midGroup = new MidGroup();
	static class MidGroup {
		@Option(names = {"-pd"}, description = "use this percentile (as opposed to specific values) for contrast thresholding in determining max contrast value of the heatmap")
		private Double percentile = null;
		@Option(names = {"-ad"}, description = "the midpoint value for contrast thresholding in the heatmap (default,mid=0)")
		private Double absolute = null;
	}
	@ArgGroup(exclusive = true, multiplicity = "0..1", heading = "%nSelect maximum value:%n%n\t@|fg(red) (select no more than one of these options)|@%n")
	private MaxGroup maxGroup = new MaxGroup();
	static class MaxGroup {
		@Option(names = {"-px"}, description = "use this percentile (as opposed to specific values) for contrast thresholding in determining max contrast value of the heatmap")
		private Double percentile = null;
		@Option(names = {"-ax"}, description = "the maximum value for contrast thresholding (default,max=10)")
		private Double absolute = null;
	}

	@Option(names = {"-0", "--include-zeros"}, description = "used with `-p` flag, indicating exclusion of zero values when calculating percentile thresholds")
	private boolean includeZeros = false;

	@ArgGroup(multiplicity = "0..1", exclusive=false, heading = "%nSelect heatmap colors:%n")
	private ColorGroup color = new ColorGroup();
	static class ColorGroup {
		@Option(names = {"-cn", "--color-min"}, description = "Color indicating minimum values (default=YELLOW) For custom color: type hexadecimal string to represent colors (e.g. \"FF0000\" is hexadecimal for red).\n See <http://www.javascripter.net/faq/rgbtohex.htm> for some color options with their corresponding hex strings.\n")
		private String min = null;
		@Option(names = {"-cd", "--color-mid"}, description = "Color indicating middle values (default=BLACK) For custom color: type hexadecimal string to represent colors (e.g. \"FF0000\" is hexadecimal for red).\n See <http://www.javascripter.net/faq/rgbtohex.htm> for some color options with their corresponding hex strings.\n")
		private String mid = null;
		@Option(names = {"-cx", "--color-max"}, description = "Color indicating maximum values (default=CYAN) For custom color: type hexadecimal string to represent colors (e.g. \"FF0000\" is hexadecimal for red).\n See <http://www.javascripter.net/faq/rgbtohex.htm> for some color options with their corresponding hex strings.\n")
		private String max = null;
		@Option(names = {"-ca", "--color-nan"}, description = "Color indicating not-a-number values (default=GRAY) For custom color: type hexadecimal string to represent colors (e.g. \"FF0000\" is hexadecimal for red).\n See <http://www.javascripter.net/faq/rgbtohex.htm> for some color options with their corresponding hex strings.\n")
		private String nan = null;
	}
	
	@ArgGroup(multiplicity = "0..1", exclusive=false, heading = "%nSelect transparency of heatmap colors (alpha channel):%n")
	private AlphaGroup alpha = new AlphaGroup();
	static class AlphaGroup {
		@Option(names = {"-tn", "--transparent-min"}, description = "Value indicating transparency of minimum values, 0 to 255 (default=255)\n")
		private int min = 255;
		@Option(names = {"-td", "--transparent-mid"}, description = "Value indicating transparency of middle values, 0 to 255  (default=255)\n")
		private int mid = 255;
		@Option(names = {"-tx", "--transparent-max"}, description = "Value indicating transparency of maximum values, 0 to 255  (default=255)\n")
		private int max = 255;
		@Option(names = {"-ta", "--transparent-nan"}, description = "Value indicating transparency of not-a-number values, 0 to 255  (default=255)\n")
		private int nan = 255;
	}
	
	String scaleType = "treeview";
	//Colors from JavaTreeview microarray software
	Color CMAX = new Color(254,255,0,255);
	Color CMID = Color.BLACK;
	Color CMIN = new Color(27,183,229,255);
	Color CNAN = Color.GRAY;
	double MIN = -10;
	double MID = 0;
	double MAX = 10;
	boolean perMin = false;
	boolean perMid = false;
	boolean perMax = false;
	
	//Consider using bitflags:
	// 000 --> 0 (Amax,Amid,Amin)
	// 001 --> 1 (Amax,Amid,Pmin)
	// 010 --> 2 (Amax,Pmid,Amin)
	// 011 --> 3 (Amax,Pmid,Pmin)
	// 100 --> 4 (Pmax,Amid,Amin)
	// 101 --> 5 (Pmax,Amid,Pmin)
	// 110 --> 6 (Pmax,Pmid,Amin)
	// 111 --> 7 (Pmax,Pmid,Pmin)
	
	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">ThreeColorHeatMapCLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		// Generate HeatMap
		try {
			ThreeColorHeatMap script_object = new ThreeColorHeatMap( CDT, CMIN, CMID, CMAX, CNAN,
					startROW, startCOL, pixelHeight, pixelWidth, scaleType,
					perMin, perMid, perMax,
					MIN, MID, MAX, !includeZeros,
					output, true );
			script_object.run();
			System.err.println("Image Generated.");
			return (0);
		} catch (OptionException e) {
			System.err.println(e.getMessage());
			return (1);
		}
	}

	private String validateInput() throws IOException {
		String r = "";

		// Assign a scaleType based on "compression" input
		if(compression==2){ scaleType = "bicubic"; }
		else if(compression==3){ scaleType = "bilinear"; }
		else if(compression==4){ scaleType = "neighbor"; }
		
		// check inputs exist
		if (!CDT.exists()) {
			r += "(!)CDT file does not exist: " + CDT.getName() + "\n";
			return (r);
		}
		// set default output filename
		if (output == null) {
			String NAME = ExtensionFileFilter.stripExtension(CDT);
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
		// check that Color hex strings are formatted properly and decode/assign colors
		Pattern hexColorPat = Pattern.compile("[0-9A-Fa-f]{6}");
		if (color.max != null) {
			Matcher m = hexColorPat.matcher(color.max);
			if (!m.matches()) {
				r += "(!)Color for higher values(max) must be formatted as a hexidecimal String!\n\tExpected input string format: \"[0-9A-Fa-f]{6}\"\n";
			}
			System.err.println("Decoding Max color: 0x" + color.max);
			CMAX = Color.decode("0x" + color.max);
		}
		if (color.mid != null) {
			Matcher m = hexColorPat.matcher(color.mid);
			if (!m.matches()) {
				r += "(!)Color for middling values must be formatted as a hexidecimal String!\n\tExpected input string format: \"[0-9A-Fa-f]{6}\"\n";
			}
			System.err.println("Decoding Mid color: 0x" + color.mid);
			CMID = Color.decode("0x" + color.mid);
		}
		if (color.min != null) {
			Matcher m = hexColorPat.matcher(color.min);
			if (!m.matches()) {
				r += "(!)Color for lower values(min) must be formatted as a hexidecimal String!\n\tExpected input string format: \"[0-9A-Fa-f]{6}\"\n";
			}
			System.err.println("Decoding Min color: 0x" + color.min);
			CMIN = Color.decode("0x" + color.min);
		}
		if (color.nan != null) {
			Matcher m = hexColorPat.matcher(color.nan);
			if (!m.matches()) {
				r += "(!)Color for invalid/non-numeric values(NaN) must be formatted as a hexidecimal String!\n\tExpected input string format: \"[0-9A-Fa-f]{6}\"\n";
			}
			System.err.println("Decoding NaN color: 0x" + color.nan);
			CNAN = Color.decode("0x" + color.nan);
		}
		// check that Alpha channel/transparency values are formatted properly and decode/assign colors
		if (alpha.max<0 || alpha.max>255) { r += "(!)Alpha/transparency value for higher values (max) must be a numeric 0 to 255\n"; }
		else { CMAX = new Color(CMAX.getRed(), CMAX.getGreen(), CMAX.getBlue(), alpha.max); }
		if (alpha.mid<0 || alpha.mid>255) { r += "(!)Alpha/transparency value for middling values (mid) must be a numeric 0 to 255\n"; }
		else { CMID = new Color(CMID.getRed(), CMID.getGreen(), CMID.getBlue(), alpha.mid); }
		if (alpha.min<0 || alpha.min>255) { r += "(!)Alpha/transparency value for lower values(min) must be a numeric 0 to 255\n"; }
		else { CMIN = new Color(CMIN.getRed(), CMIN.getGreen(), CMIN.getBlue(), alpha.min); }
		if (alpha.nan<0 || alpha.nan>255) { r += "(!)Alpha/transparency value for invalid/non-numeric values(NaN) must be a numeric 0 to 255\n"; }
		else { CNAN = new Color(CNAN.getRed(), CNAN.getGreen(), CNAN.getBlue(), alpha.nan); }
		
		// assign vals for contrast thresholds and set bools
		if(maxGroup.percentile!=null) {
			MAX = maxGroup.percentile;
			perMax = true;
		}else if(maxGroup.absolute!=null) {
			MAX = maxGroup.absolute;
		}
		if(midGroup.percentile!=null) {
			MID = midGroup.percentile;
			perMid = true;
		}else if(midGroup.absolute!=null) {
			MID = midGroup.absolute;
		}
		if(minGroup.percentile!=null) {
			MIN = minGroup.percentile;
			perMin = true;
		}else if(minGroup.absolute!=null) {
			MIN = minGroup.absolute;
		}
		
		// check pixel ranges are valid
		if (pixelHeight <= 0) { r += "(!)Cell height must be a positive integer value! check \"-y\" flag.\"\n"; }
		if (pixelWidth <= 0) { r += "(!)Cell width must be a positive integer value! check \"-x\" flag.\"\n"; }
		// check start row/col are valid
		if (startROW <= 0) { r += "(!)Row start index must be a positive integer value! check \"-rs\" flag.\"\n"; }
		if (startCOL<=0) { r += "(!)Column start index must be a positive integer value! check \"-l\" flag.\"\n"; }

		return (r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input      the input matrix/CDT to generate a heatmap from
	 * @param c_min      the min-value color to use
	 * @param c_mid      the mid-value color to use
	 * @param c_max      the max-value color to use
	 * @param c_nan      the color to use for NaN values
	 * @param startR     the index of the starting row
	 * @param startC     the index of the starting column
	 * @param pHeight    the height (in pixels) of the output image
	 * @param pWidth     the width (in pixels) of the output image
	 * @param scale      the image compression strategy (e.g. "treeview")
	 * @param minPstatus the matrix value corresponding to the minimum color value
	 *                   (absolute strategy)
	 * @param midPstatus the matrix value corresponding to the middle color value
	 *                   (absolute strategy)
	 * @param maxPstatus the matrix value corresponding to the maximum color value
	 *                   (absolute strategy)
	 * @param min_quant  the matrix percentile value corresponding to the minimum
	 *                   color value (quantile strategy)
	 * @param mid_quant  the matrix percentile value corresponding to the middle
	 *                   color value (quantile strategy)
	 * @param max_quant  the matrix percentile value corresponding to the maximum
	 *                   color value (quantile strategy)
	 * @param exZ        whether or not to exclude zero values from percentile
	 *                   calculations
	 * @param output     the output PNG file
	 * @return command line to execute with formatted inputs
	 * @throws OptionException 
	 */
	public static String getCLIcommand(File input, Color c_min, Color c_mid, Color c_max, Color c_nan, int startR, int startC,
			int pHeight, int pWidth, String scale, boolean minPstatus, boolean midPstatus, boolean maxPstatus,
			double min_quant, double mid_quant, double max_quant, boolean exZ, File output) throws OptionException {
		String command = "java -jar $SCRIPTMANAGER figure-generation three-color";
		command += " " + input.getAbsolutePath();
		command += " -o " + output.getAbsolutePath();

		command += " --color-min " + Integer.toHexString(c_min.getRGB()).substring(2);
		command += " --color-mid " + Integer.toHexString(c_mid.getRGB()).substring(2);
		command += " --color-max " + Integer.toHexString(c_max.getRGB()).substring(2);
		command += " --color-nan " + Integer.toHexString(c_nan.getRGB()).substring(2);

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

		command += minPstatus ? " -pn " + min_quant : " -an " + min_quant;
		command += midPstatus ? " -pd " + mid_quant : " -ad " + mid_quant;
		command += maxPstatus ? " -px " + max_quant : " -ax " + max_quant;

		command += c_min.getAlpha()==0 ? " --transparent-min" : "";
		command += c_mid.getAlpha()==0 ? " --transparent-mid" : "";
		command += c_max.getAlpha()==0 ? " --transparent-max" : "";
		command += c_nan.getAlpha()==0 ? " --transparent-nan" : "";

		command += exZ ? "--include-zeros" : "";

		return (command);
	}
}
