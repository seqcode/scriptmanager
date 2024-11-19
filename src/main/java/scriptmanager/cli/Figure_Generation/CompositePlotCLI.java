package scriptmanager.cli.Figure_Generation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.scripts.Figure_Generation.PlotComposite;

/**
 * Command line interface for {@link scriptmanager.scripts.Figure_Generation.PlotComposite}
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.Read_Analysis.TagPileup
 */
@Command(name = "composite-plot", mixinStandardHelpOptions = true,
	description = ToolDescriptions.composite_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class CompositePlotCLI implements Callable<Integer> {

	/**
	 * Creates a CompositePlotCLI object
	 */
	public CompositePlotCLI(){}

	@Parameters(index = "0", description = "Composite data to plot. (formatted like TagPileup composite output)")
	private File inputComposite;

	@Option(names = { "-o", "--output" }, description = "specify output filename, please use PNG extension\n(default=Input filename with \"_compositePlot.png\" appended to the name in working directory of ScriptManager")
	private File output = null;
	@Option(names = { "-t", "--title" }, description = "set title (default=<composite-file-name>)")
	private String title = null;
	@Option(names = { "-l", "--legend" }, description = "add a legend (default=no legend)")
	private boolean legend = false;
	@Option(names = { "-x", "--width" }, description = "indicate a pixel width for the plot (default=500)")
	private int pixelWidth = 500;
	@Option(names = { "-y", "--height" }, description = "indicate a pixel height for the plot (default=270)")
	private int pixelHeight = 270;
	@Option(names = { "-c", "--custom-colors" }, description = "indicate colors to use for each series. Must indicate a number of colors that matches number of dataseries\n"
					+ "default behavior:\n" + "if one series input, use black\n"
					+ "if two series input, use blue(sense) and red(anti)\n"
					+ "if greater than two series, cycle through a set of 10 preset colors based on Rossi et al, 2021 (PMID:33692541).", arity = "1..")
	private String[] colors = null;

	private ArrayList<Color> COLORS = new ArrayList<Color>();

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">CompositePlotCLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}

		// Generate Composite Plot
		PlotComposite.plotCompositeFile(inputComposite, output, true, title, COLORS, legend, pixelHeight, pixelWidth);

		System.err.println("Image Generated.");
		return (0);
	}

	/**
	 * Validate input values and create user-readable error messages.
	 * @return
	 * @throws IOException Invalid file or parameters
	 */
	private String validateInput() throws IOException {
		String r = "";

		// Input file check
		if (!inputComposite.exists()) {
			r += "(!)Composite Data file does not exist: " + inputComposite.getName() + "\n";
		} else if (inputComposite.isDirectory()) {
			r += "(!)Composite Data file is a directory: " + inputComposite.getName() + "\n";
		}
		
		// Output file check
		if (output == null) {
			output = new File(System.getProperty("user.dir"));
		} else {
			if (output.getParent() == null) {
// 				System.err.println("default to current directory");
			} else if (!new File(output.getParent()).exists()) {
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}

		// Pixel ranges must be valid
		if (pixelHeight <= 0) {
			r += "(!)Cell height must be a positive integer value! check \"-y\" flag.\"";
		}
		if (pixelWidth <= 0) {
			r += "(!)Cell width must be a positive integer value! check \"-x\" flag.\"";
		}

		// Set Colors (customized)
		if (colors != null) {
			// check color input format and decode
			Pattern hexColorPat = Pattern.compile("[0-9A-Fa-f]{6}");
			for (int i = 0; i < colors.length; i++) {
				Matcher m = hexColorPat.matcher(colors[i]);
				if (!m.matches()) {
					r += "(!)Color(" + colors[i] + ") must be formatted as a hexidecimal String!\n\tExpected input string format: \"[0-9A-Fa-f]{6}\"\n";
				} else {
					System.err.println("Decoding color: 0x" + colors[i]);
					COLORS.add(Color.decode("0x" + colors[i]));
				}
			}
		}
		return (r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input       a tab-delimited file containing the composite information
	 *                    in the format of scripts.Figure_Generation.TagPileup's
	 *                    output
	 * @param output      filepath to save composite image to. if null, defaults to
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
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input, File output, String title, ArrayList<Color> COLORS, boolean legend, int pxHeight, int pxWidth) {
		String command = "java -jar $SCRIPTMANAGER figure-generation composite-plot";
		command += " -o " + output.getAbsolutePath();
		command += title==null ? "" : " -t " + title;
		if (COLORS!=null) {
			command += " -c";
			for (int x=0; x<COLORS.size(); x++) {
				command += " " + Integer.toHexString(COLORS.get(x).getRGB()).substring(2);
			}
		}
		command += legend ? " -l " : "";
		command += " -y " + pxHeight;
		command += " -x " + pxWidth;
		command += " " + input.getAbsolutePath();
		return (command);
	}
}
