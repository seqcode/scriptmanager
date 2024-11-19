package scriptmanager.window_interface.BAM_Manipulation;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import scriptmanager.objects.CustomOutputStream;
import scriptmanager.scripts.BAM_Manipulation.FilterforPIPseq;

/**
 * Output wrapper for running
 * {@link scriptmanager.scripts.BAM_Manipulation.FilterforPIPseq} and reporting
 * progress
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Manipulation.FilterforPIPseq
 * @see scriptmanager.window_interface.BAM_Manipulation.FilterforPIPseqWindow
 */
@SuppressWarnings("serial")
public class FilterforPIPseqOutput extends JFrame {
	File bamFile = null;
	File genome = null;
	File output = null;
	String SEQ = "";

	private JTextArea textArea;

	public FilterforPIPseqOutput(File in, File gen, File out, String s) {
		setTitle("Permanganate-Seq Filtering Progress");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		bamFile = in;
		genome = gen;
		output = out;
		SEQ = s.toUpperCase();
	}

	/**
	 * Runs the FilterforPIPseq script and displays results
	 * @throws IOException Invalid file or parameters
	 * @throws InterruptedException Thrown when more than one script is run at the same time 
	 */
	public void run() throws IOException, InterruptedException {

		PrintStream PS = new PrintStream(new CustomOutputStream(textArea));

		// Check if BAI index file exists
		File f = new File(bamFile + ".bai");
		if (f.exists() && !f.isDirectory()) {

			FilterforPIPseq script_obj = new FilterforPIPseq(bamFile, genome, output, SEQ, PS);
			script_obj.run();

			Thread.sleep(2000);
			dispose();
		} else {
			JOptionPane.showMessageDialog(null, "BAI Index File does not exist for: " + bamFile.getName() + "\n");
		}
	}
}
