package scriptmanager.scripts.Figure_Generation;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import java.io.File;
import java.io.IOException;


/**
 * Merge two PNG files (typically sense &amp; anti heatmaps output by
 * {@link scriptmanager.scripts.Figure_Generation.TwoColorHeatMap}.
 *
 * @author William KM Lai
 * @see scriptmanager.cli.Figure_Generation.MergeHeatMapCLI
 * @see scriptmanager.window_interface.Figure_Generation.MergeHeatMapOutput
 * @see scriptmanager.window_interface.Figure_Generation.MergeHeatMapWindow
 */
public class MergeHeatMapPlot {

	/**
	 * Method for merging two same-sized PNG files into a new PNG using a
	 * pixel-by-pixel color averaging strategy.
	 *
	 * @param INPUT1 first of two same-sized PNGs
	 * @param INPUT2 second of two same-sized PNGs
	 * @param OUTPUT filepath to save the averaged PNG (same pixel dimensions as
	 *               input PNG files)
	 * @return the combined heatmap
	 * @throws IOException Invalid file or parameters
	 */
	public static JLabel mergePNG(File INPUT1, File INPUT2, File OUTPUT) throws IOException {
		JLabel picLabel = null;
		if (INPUT2 == null) {
			picLabel = new JLabel("No match for file: " + INPUT1.getName());
		} else {
			BufferedImage image = ImageIO.read(INPUT1);
			BufferedImage overlay = ImageIO.read(INPUT2);
			if (image.getWidth() != overlay.getWidth()) {
				picLabel = new JLabel("Unequal Pixel Width!!!\n" + INPUT1.getName() + ":\t" + image.getWidth() + "\n"
						+ INPUT2.getName() + ":\t" + overlay.getWidth());
			} else if (image.getHeight() != overlay.getHeight()) {
				picLabel = new JLabel("Unequal Pixel Height!!!\n" + INPUT1.getName() + ":\t" + image.getHeight() + "\n"
						+ INPUT2.getName() + ":\t" + overlay.getHeight());
			} else {
				int w = Math.max(image.getWidth(), overlay.getWidth());
				int h = Math.max(image.getHeight(), overlay.getHeight());
				BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				for (int x = 0; x < combined.getWidth(); x++) {
					for (int y = 0; y < combined.getHeight(); y++) {
						int image_rgb = image.getRGB(x, y);
						int overlay_rgb = overlay.getRGB(x, y);

						int image_alpha = (image_rgb & 0xFF000000) >>> 24;
						int image_red = (image_rgb & 0x00FF0000) >>> 16;
						int image_green = (image_rgb & 0x0000FF00) >>> 8;
						int image_blue = (image_rgb & 0x000000FF) >>> 0;

						int overlay_alpha = (overlay_rgb & 0xFF000000) >>> 24;
						int overlay_red = (overlay_rgb & 0x00FF0000) >>> 16;
						int overlay_green = (overlay_rgb & 0x0000FF00) >>> 8;
						int overlay_blue = (overlay_rgb & 0x000000FF) >>> 0;

						int new_rgb;
						if (image_green >= 240 && image_blue >= 240 && image_red >= 240) {
							new_rgb = (overlay_alpha << 24) | (overlay_red << 16) | (overlay_green << 8) | overlay_blue;
						} else if (overlay_green >= 240 && overlay_blue >= 240 && overlay_red >= 240) {
							new_rgb = (image_alpha << 24) | (image_red << 16) | (image_green << 8) | image_blue;
						} else {
							int new_alpha = (image_alpha + overlay_alpha) / 2;
							int new_red = (image_red + overlay_red) / 2;
							int new_green = (image_green + overlay_green) / 2;
							int new_blue = (image_blue + overlay_blue) / 2;

							new_rgb = (new_alpha << 24) | (new_red << 16) | (new_green << 8) | new_blue;
						}
						combined.setRGB(x, y, new_rgb);
					}
				}
				// Output new image
				if (OUTPUT != null) {
					ImageIO.write(combined, "PNG", OUTPUT);
				}
				picLabel = new JLabel(new ImageIcon(combined));
			}
		}
		return (picLabel);
	}
}
