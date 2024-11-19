package scriptmanager.objects;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import javax.swing.JPanel;

/**
 * Component that displays a graphic of a composite plot shape to illustrate
 * separate vs combined pileups.
 *
 * @author Olivia Lang
 * @see scriptmanager.scripts.Read_Analysis.TagPileup
 */
@SuppressWarnings("serial")
public class CompositeCartoon extends JPanel {

	/**
	 * Height of cartoon
	 */
	public int cartoonHeight = 800;
	/**
	 * If cartoon represents combined or seperate composite
	 */
	public int strand = PileupParameters.SEPARATE;
	private Path2D.Double peak;
	private Path2D.Double p_rev;
	private Color forColor = Color.decode("0x0000FF");
	private Color revColor = Color.decode("0xFF0000");

	/**
	 * Scaling factor for size of cartoon.
	 */
	public int x = 16;

	/**
	 * Initialize the cartoon component with a scale factor of 16.
	 */
	public CompositeCartoon() {
		initializePeakPaths();
	}

	/**
	 * Initialize the cartoon component with a customized scale factor.
	 * 
	 * @param scale this factor will scale the cartoon larger (or smaller)
	 */
	public CompositeCartoon(int scale) {
		x = scale;
		initializePeakPaths();
	}
	/**
	 * Initialize the paths for each peak shape in the cartoon.
	 */
	public void initializePeakPaths() {
		// Forward peak path
		peak = new Path2D.Double();
		peak.moveTo(0, 0);
		peak.curveTo(  2*x,    0, 1.5*x, -3*x, 2*x, -3*x);
		peak.curveTo(2.5*x, -3*x,   2*x,    0, 4*x,    0);
		peak.closePath();

		// Reverse peak path
		p_rev = new Path2D.Double();
		p_rev.moveTo(0, 0);
		p_rev.curveTo(  2*x,    0, 1.5*x,  3*x, 2*x,  3*x);
		p_rev.curveTo(2.5*x,  3*x,   2*x,    0, 4*x,    0);
		p_rev.closePath();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		// Initialize graphic and move pen to initial position
		Graphics2D g2d = (Graphics2D) g;
		g2d.translate(0, 3*x);
		if (strand == PileupParameters.COMBINED) {
			g2d.translate(0, 2*x);
		}
		// Draw left peak
		g2d.setColor(forColor);
		g2d.fill(peak);
		g2d.translate(2*x,0);
		// Draw right peak
		if (strand == PileupParameters.SEPARATE) {
			g2d.setColor(revColor);
			g2d.fill(p_rev);
		} else {
			g2d.fill(peak);
		}
	}

	/**
	 * Set strand value as encoded by PileupParameters (SEPARATE v COMBINED).
	 *
	 * @param s strand value encoding
	 * @throws IllegalArgumentException Invalid separation value
	 */
	public void setStrand(int s) throws IllegalArgumentException {
		if(s == PileupParameters.SEPARATE) {
			strand = PileupParameters.SEPARATE;
		} else if(s == PileupParameters.COMBINED) {
			strand = PileupParameters.COMBINED;
		} else {
			throw new IllegalArgumentException("Invalid separation value. Must be " + PileupParameters.SEPARATE + " or " + PileupParameters.COMBINED + ".");
		}
	}

	/**
	 * Set a new forward ("sense") color.
	 *
	 * @param c color for forward
	 */
	public void setForwardColor(Color c) {
		forColor  = c;
	}

	/**
	 * Set a new reverse ("anti") color.
	 *
	 * @param c color for reverse
	 */
	public void setReverseColor(Color c) {
		revColor  = c;
	}

	/**
	 * Override getPreferredSize as a function of the scaling factor for the cartoon (x).
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(6*x, 6*x);
	}
}
