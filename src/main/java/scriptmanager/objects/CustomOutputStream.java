package scriptmanager.objects;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

/**
 * This class extends from OutputStream to redirect output to a JTextArea.
 * <br>
 * Source written by Nam Ha Minh written 2019/07/06, retrieved 2020/05/05, url below
 * https://www.codejava.net/java-se/swing/redirect-standard-output-streams-to-jtextarea
 *
 * @author www.codejava.net
 * @see scriptmanager.window_interface.BAM_Format_Converter.BAMtoBEDOutput
 * @see scriptmanager.window_interface.BAM_Format_Converter.BAMtoGFFOutput
 * @see scriptmanager.window_interface.BAM_Format_Converter.BAMtobedGraphOutput
 * @see scriptmanager.window_interface.BAM_Format_Converter.BAMtoscIDXOutput
 * @see scriptmanager.window_interface.BAM_Manipulation.FilterforPIPseqOutput
 * @see scriptmanager.window_interface.BAM_Statistics.PEStatOutput
 * @see scriptmanager.window_interface.BAM_Statistics.SEStatOutput
 * @see scriptmanager.window_interface.Figure_Generation.LabelHeatMapOutput
 * @see scriptmanager.window_interface.Peak_Analysis.BEDPeakAligntoRefOutput
 * @see scriptmanager.window_interface.Peak_Analysis.FilterBEDbyProximityOutput
 * @see scriptmanager.window_interface.Read_Analysis.TagPileupOutput
 * @see scriptmanager.window_interface.Sequence_Analysis.DNAShapefromBEDOutput
 * @see scriptmanager.window_interface.Sequence_Analysis.DNAShapefromFASTAOutput
 * @see scriptmanager.window_interface.Sequence_Analysis.FASTAExtractOutput
 * @see scriptmanager.window_interface.Sequence_Analysis.SearchMotifOutput
 */
public class CustomOutputStream extends OutputStream {
	private JTextArea textArea;

	/**
	 * Creates a CustomOutputStream
	 * @param textArea Text area to place the output stream
	 */
	public CustomOutputStream(JTextArea textArea) {
		this.textArea = textArea;
	}

	/**
	 * Writes integers to the JTextArea
	 */
	@Override
	public void write(int b) throws IOException {
		// redirects data to the text area
        textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
	}
}