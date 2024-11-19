package scriptmanager.cli.Figure_Generation;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.scripts.Figure_Generation.FourColorPlot;

/**
 * Command line interface for
 * {@link scriptmanager.scripts.Figure_Generation.FourColorPlot}
 * 
 * @author Olivia Lang
 */
@Command(name = "four-color", mixinStandardHelpOptions = true,
	description = ToolDescriptions.four_color_description,
	version = "ScriptManager "+ ToolDescriptions.VERSION,
	sortOptions = false,
	exitCodeOnInvalidInput = 1,
	exitCodeOnExecutionException = 1)
public class FourColorSequenceCLI implements Callable<Integer> {

	@Parameters(index = "0", description = "input FASTA file of sequences to plot")
	private File fastaFile;

	@Option(names = { "-o", "--output" }, description = "specify output filename, please use PNG extension\n(default=FASTA filename with \"_4color.png\" appended to the name in working directory of ScriptManager")
	private File output = null;
	@Option(names = { "-c", "--colors" }, arity = "4..5", description = "For custom colors: List colors to use for ATGC[N], in that order. Type hexadecimal string to represent colors, e.g. FF0000 is hexadecimal for red.\n(default=A-red,T-green,G-yellow,C-blue,N-gray, if only 4 colors specified, N will be set to gray)\n See <http://www.javascripter.net/faq/rgbtohex.htm> for some color options with their corresponding hex strings.")
	private ArrayList<String> colors = null;
	@Option(names = { "-x", "--pixel-width" }, description = "pixel width (default=1)")
	private int pixelWidth = 1;
	@Option(names = { "-y", "--pixel-height" }, description = "pixel height (default=1)")
	private int pixelHeight = 1;

	/**
	 * Runs when this subcommand is called, running script in respective script package with user defined arguments
	 * @throws IOException Invalid file or parameters
	 */
	@Override
	public Integer call() throws Exception {
		System.err.println(">FourColorSequenceCLI.call()");
		String validate = validateInput();
		if (!validate.equals("")) {
			System.err.println(validate);
			System.err.println("Invalid input. Check usage using '-h' or '--help'");
			System.exit(1);
		}
		// Add colors into ArrayList
		ArrayList<Color> ATCG_COLORS = new ArrayList<Color>(5);
		if (colors != null) {
			System.err.println("size:" + Integer.toString(ATCG_COLORS.size()));
			for (int c = 0; c < colors.size(); c++) {
				ATCG_COLORS.add(c, Color.decode("#" + colors.get(c)));
			}
		} else {
			ATCG_COLORS.add(0, new Color(208, 0, 0)); // default A-color #D00000
			ATCG_COLORS.add(1, new Color(0, 208, 0)); // default T-color #00D000
			ATCG_COLORS.add(2, new Color(255, 180, 0)); // default G-color #FFB400
			ATCG_COLORS.add(3, new Color(0, 0, 208)); // default C-color #0000D0
		}
		// Set N-color to gray if only 4 colors provided as input
		if (colors == null || colors.size() == 4) {
			ATCG_COLORS.add(4, Color.GRAY); // default N-color
		}

		// Generate Heatmap
		FourColorPlot.generatePLOT(fastaFile, output, ATCG_COLORS, pixelHeight, pixelWidth);

		System.err.println("Image Generated.");
		return (0);
	}

	private String validateInput() throws IOException {
		String r = "";

		// check inputs exist
		if (!fastaFile.exists()) {
			r += "(!)FASTA file does not exist: " + fastaFile.getName() + "\n";
			return (r);
		}
		// set default output filename
		if (output == null) {
			String NAME = ExtensionFileFilter.stripExtensionIgnoreGZ(fastaFile);
			output = new File(NAME + ".png");
			// check output filename is valid
		} else {
			// check directory
			if (output.getParent() == null) {
// 				System.err.println("default to current directory");
			} else if (!new File(output.getParent()).exists()) {
				r += "(!)Check output directory exists: " + output.getParent() + "\n";
			}
		}

		// check colors if custom
		if (colors != null) {
			// check that 4-5 provided
			if (colors.size() != 4 && colors.size() != 5) {
				r += "(!)There must be 4 or 5 colors specified if using custom colors.";
			}
			// check that hex string is formatted properly
			Pattern hexColorPat = Pattern.compile("#?[0-9A-Fa-f]{6}");
			for (int c = 0; c < colors.size(); c++) {
				String hexStr = colors.get(c);
				Matcher m = hexColorPat.matcher(hexStr);
				if (!m.matches()) {
					r += "(!)Color must be formatted as a hexidecimal String.\n" + hexStr
							+ " is not a valid hex string.\nExpected input string format: \"#?[0-9A-F]{6}\"";
				}
			}
		}
		// check pixel ranges are valid
		if (pixelHeight < 1) {
			r += "(!)Image Height must be a positive integer value! check \"-y\" flag.\"";
		}
		if (pixelWidth < 1) {
			r += "(!)Image Width must be a positive integer value! check \"-x\" flag.\"";
		}

		return (r);
	}

	/**
	 * Reconstruct CLI command
	 * 
	 * @param input  the FASTA file to generate sequence plot from
	 * @param output the PNG image of the sequence plot
	 * @param COLORS the list of colors encoding the various nucleotides (A,T,G,C,N)
	 * @param h      the height of each pixel
	 * @param w      the width of each pixel
	 * @return command line to execute with formatted inputs
	 */
	public static String getCLIcommand(File input, File output, ArrayList<Color> COLORS, int h, int w) throws IndexOutOfBoundsException {
		String command = "java -jar $SCRIPTMANAGER figure-generation four-color";
		command += " -o " + output.getAbsolutePath();
		command += " -c";
		for (int x=0; x<5; x++) {
			command += " " + Integer.toHexString(COLORS.get(x).getRGB()).substring(2);
		}
		command += " -y " + h;
		command += " -x " + w;
		command += " " + input.getAbsolutePath();
		return (command);
	}
}
