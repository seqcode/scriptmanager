package scriptmanager.scripts.Figure_Generation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import scriptmanager.util.GZipUtilities;
/**
 * Generate a four-color sequence plot to be saved as a PNG.
 * 
 * @author William KM Lai
 * @see scriptmanager.cli.Figure_Generation.FourColorSequenceCLI
 * @see scriptmanager.window_interface.Figure_Generation.FourColorSequenceWindow
 */
public class FourColorPlot {
	/**
	 * Visualize sequences as color pixels
	 * 
	 * @param input the FASTA to make the four color plot from
	 * @param output the filepath to write the four-color PNG to
	 * @param COLOR the list of colors to use for each ATCGN encoding
	 * @param h height of each base, in pixel
	 * @param w  width of each base, in pixel
	 * @throws IOException Invalid file or parameters
	 */
	public static void generatePLOT(File input, File output, ArrayList<Color> COLOR, int h, int w) throws IOException {
		int height = h;
		int width = w;

		List<String> seq = new ArrayList<String>();
		int maxLen = -1;

		// Check if file is gzipped and instantiate appropriate BufferedReader
		BufferedReader br = GZipUtilities.makeReader(input);
		// Initialize line variable to loop through
		String line = br.readLine();
		while (line != null) {
			// Encounter seq line
			if (!line.contains(">")) {
				// Append sequence to the string in the last item
				seq.set(seq.size()-1, seq.get(seq.size()-1) + line);
			// Encounter header line
			} else {
				// Check length of last seq string and update maxLen if larger
				if (seq.size() > 0) {
					if (maxLen < seq.get(seq.size()-1).length()) {
						maxLen = seq.get(seq.size()-1).length();
					}
				}
				// Add new empty sequence string when new header encountered
				seq.add("");
			}
			line = br.readLine();
		}
		br.close();
		int pixwidth = maxLen * width;
		int pixheight = seq.size() * height;

		System.setProperty("java.awt.headless", "true");
		BufferedImage im = new BufferedImage(pixwidth, pixheight, BufferedImage.TYPE_INT_ARGB);
		Graphics g = im.getGraphics();
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, pixwidth, pixheight);

		int count = 0;
		for (int x = 0; x < seq.size(); x++) {
			String s = seq.get(x);
			char[] letters = s.toCharArray();
			for (int j = 0; j < letters.length; j++) {
				switch (letters[j]) {
				case 'A':
				case 'a':
					g.setColor(COLOR.get(0));
					break;
				case 'T':
				case 't':
					g.setColor(COLOR.get(1));
					break;
				case 'G':
				case 'g':
					g.setColor(COLOR.get(2));
					break;
				case 'C':
				case 'c':
					g.setColor(COLOR.get(3));
					break;
				case '-':
					g.setColor(Color.WHITE);
					break;
				default:
					g.setColor(COLOR.get(4));
				}
				g.fillRect(j * width, count * height, width, height);
			}
			count++;
		}
		try {
			ImageIO.write(im, "png", output);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
