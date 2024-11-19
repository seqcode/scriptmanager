package scriptmanager.util;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Fixed preset color palettes for figure generation.
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.Figure_Generation.PlotComposite
 */
public class ColorSeries {

	/**
	 * Creates a new ColorSeries object
	 */
	public ColorSeries(){}

	/**
	 * Initialize an ArrayList of Color objects using the ChIP-exo red and blue.
	 * 
	 * @return list of the two colors in the following order: {blue(0x0000FF),
	 *         red(0xFF0000)}
	 */
	public static final ArrayList<Color> InitializeXOColors() {
		ArrayList<Color> COLORS = new ArrayList<Color>();
		COLORS.add(Color.decode("0x0000FF")); // Blue
		COLORS.add(Color.decode("0xFF0000")); // Red
		return (COLORS);
	}

	/**
	 * Initialize an ArrayList of Color objects using the color palette from
	 * <a href="https://pubmed.ncbi.nlm.nih.gov/33692541/"><em>Rossi et al, 2021
	 * (Nature)</em></a>.
	 * 
	 * @return list of the ten colors in the following order: {Grey, Black, Red,
	 *         Orange, Yellow, Green, LightBlue, DarkBlue, Purple, Pink}
	 */
	public static final ArrayList<Color> InitializeYEPColors() {
		ArrayList<Color> COLORS = new ArrayList<Color>();
		COLORS.add(Color.decode("0xBFBFBF")); // Grey
		COLORS.add(Color.decode("0x000000")); // Black
		COLORS.add(Color.decode("0xEA3323")); // Red
		COLORS.add(Color.decode("0xF09637")); // Orange
		COLORS.add(Color.decode("0xD7D746")); // Yellow
		COLORS.add(Color.decode("0x68DF42")); // Green
		COLORS.add(Color.decode("0x4FAEEA")); // Light Blue
		COLORS.add(Color.decode("0x1307F5")); // Dark Blue
		COLORS.add(Color.decode("0x991EF5")); // Purple
		COLORS.add(Color.decode("0xEA33CA")); // Pink
		return (COLORS);
	}

	/**
	 * Initialize an ArrayList of Color objects using the Kelly color palette.<br>
	 * <br>
	 * Colors copied from response on StackOverflow:s
	 * https://stackoverflow.com/questions/470690/how-to-automatically-generate-n-distinct-colors
	 * 
	 * @return list of the 20 colors
	 */
	public static final ArrayList<Color> InitializeKellyColors() {
		ArrayList<Color> COLORS = new ArrayList<Color>();
		COLORS.add(Color.decode("0xFFB300")); // Vivid Yellow
		COLORS.add(Color.decode("0x803E75")); // Strong Purple
		COLORS.add(Color.decode("0xFF6800")); // Vivid Orange
		COLORS.add(Color.decode("0xA6BDD7")); // Very Light Blue
		COLORS.add(Color.decode("0xC10020")); // Vivid Red
		COLORS.add(Color.decode("0xCEA262")); // Grayish Yellow
		COLORS.add(Color.decode("0x817066")); // Medium Gray
		// The following options aren't very colorblind-friendly
		COLORS.add(Color.decode("0x007D34")); // Vivid Green
		COLORS.add(Color.decode("0xF6768E")); // Strong Purplish Pink
		COLORS.add(Color.decode("0x00538A")); // Strong Blue
		COLORS.add(Color.decode("0xFF7A5C")); // Strong Yellowish Pink
		COLORS.add(Color.decode("0x53377A")); // Strong Violet
		COLORS.add(Color.decode("0xFF8E00")); // Vivid Orange Yellow
		COLORS.add(Color.decode("0xB32851")); // Strong Purplish Red
		COLORS.add(Color.decode("0xF4C800")); // Vivid Greenish Yellow
		COLORS.add(Color.decode("0x7F180D")); // Strong Reddish Brown
		COLORS.add(Color.decode("0x93AA00")); // Vivid Yellowish Green
		COLORS.add(Color.decode("0x593315")); // Deep Yellowish Brown
		COLORS.add(Color.decode("0xF13A13")); // Vivid Reddish Orange
		COLORS.add(Color.decode("0x232C16")); // Dark Olive Green
		return (COLORS);
	}
}
