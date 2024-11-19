package scriptmanager.objects;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 * Adapted from:
 * https://www.javaworld.com/article/2077579/learn-java/java-tip-77--enable-copy-and-paste-functionality-between-swing-s-jtables-and-excel.html
 * ExcelAdapter enables Copy-Paste Clipboard functionality on JTables. The
 * clipboard data format used by the adapter is compatible with the clipboard
 * format used by Excel. This provides for clipboard interoperability between
 * enabled JTables and Excel.
 * <br><br>
 * Create and update your JTable like normal but just add the reference to the
 * constructor to attach the copy/paste features. For example, you can add the
 * paste keystrokes to some {@code myTable} with the following:
 * 
 * <pre>
 * JTable myTable = new JTable(...);
 * &#64;SuppressWarnings("unused")
 * PasteableTable myAd = new PasteableTable(myTable);
 * </pre>
 * 
 * @author William KM Lai
 * @see scriptmanager.window_interface.Read_Analysis.ScaleMatrixWindows
 */
public class PasteableTable implements ActionListener {
	private String rowstring, value;
	private Clipboard system;
	private StringSelection stsel;
	private JTable myTable;

	/**
	 * The Excel Adapter is constructed with a JTable on which it enables Copy-Paste
	 * and acts as a Clipboard listener.
	 */
	public PasteableTable(JTable jt) {
		myTable = jt;
		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		// Identifying the copy KeyStroke user can modify this to copy on some other Key
		// combination.
		KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
		// Identifying the Paste KeyStroke user can modify this to copy on some other
		// Key combination.
		myTable.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
		myTable.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);
		system = Toolkit.getDefaultToolkit().getSystemClipboard();
	}

	/**
	 * Public Accessor methods for the Table on which this adapter acts.
	 */
	public JTable getJTable() {
		return myTable;
	}

	public void setJTable(JTable jTable1) {
		this.myTable = jTable1;
	}

	/**
	 * This method is activated on the Keystrokes we are listening to in this
	 * implementation. Here it listens for Copy and Paste ActionCommands. Selections
	 * comprising non-adjacent cells result in invalid selection and then copy
	 * action cannot be performed. Paste is done by aligning the upper left corner
	 * of the selection with the 1st element in the current selection of the JTable.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().compareTo("Copy") == 0) {
			StringBuffer sbf = new StringBuffer();
			// Check to ensure we have selected only a contiguous block of cells
			int numcols = myTable.getSelectedColumnCount();
			int numrows = myTable.getSelectedRowCount();
			int[] rowsselected = myTable.getSelectedRows();
			int[] colsselected = myTable.getSelectedColumns();
			if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0]
					&& numrows == rowsselected.length)
					&& (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0]
							&& numcols == colsselected.length))) {
				JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			for (int i = 0; i < numrows; i++) {
				for (int j = 0; j < numcols; j++) {
					sbf.append(myTable.getValueAt(rowsselected[i], colsselected[j]));
					if (j < numcols - 1) {
						sbf.append("\t");
					}
				}
				sbf.append("\n");
			}
			stsel = new StringSelection(sbf.toString());
			system = Toolkit.getDefaultToolkit().getSystemClipboard();
			system.setContents(stsel, stsel);
		}

		if (e.getActionCommand().compareTo("Paste") == 0) {
			// System.out.println("Trying to Paste");
			int startRow = (myTable.getSelectedRows())[0];
			int startCol = (myTable.getSelectedColumns())[0];
			try {
				String trstring = (String) (system.getContents(this).getTransferData(DataFlavor.stringFlavor));
				// System.out.println("String is:"+trstring);
				StringTokenizer st1 = new StringTokenizer(trstring, "\n");
				for (int i = 0; st1.hasMoreTokens(); i++) {
					rowstring = st1.nextToken();
					StringTokenizer st2 = new StringTokenizer(rowstring, "\t");
					for (int j = 0; st2.hasMoreTokens(); j++) {
						value = (String) st2.nextToken();
						if (startRow + i < myTable.getRowCount() && startCol + j < myTable.getColumnCount()
								&& myTable.isCellEditable(startRow + i, startCol + j)) {
							myTable.setValueAt(value, startRow + i, startCol + j);
						}
						// System.out.println("Putting "+ value+"at
						// row="+startRow+i+"column="+startCol+j);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}