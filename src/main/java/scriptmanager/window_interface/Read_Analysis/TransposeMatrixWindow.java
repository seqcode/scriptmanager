package scriptmanager.window_interface.Read_Analysis;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.sql.Timestamp;
import java.util.Date;

import scriptmanager.cli.Read_Analysis.TransposeMatrixCLI;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;
import scriptmanager.util.GZipUtilities;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.ScriptManagerException;
import scriptmanager.scripts.Read_Analysis.TransposeMatrix;

/**
 * GUI for collecting inputs to be processed by the TransposeMatrix script
 * @see scriptmanager.scripts.Read_Analysis.TransposeMatrix
 */
@SuppressWarnings("serial")
public class TransposeMatrixWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
	final DefaultListModel<String> expList;

	ArrayList<File> TABFiles = new ArrayList<File>();
	private File OUT_DIR = null;

	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnCalculate;
	private JButton btnOutput;
	private JCheckBox chckbxGzipOutput;
	private JLabel lblDefaultToLocal;
	private JLabel lblCurrent;
	private JProgressBar progressBar;


	private JTextField txtRow;
	private JTextField txtCol;

	public Task task;

	/**
	 * Organize user inputs for calling script.
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			try {
				if (TABFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No Files Loaded!!!");
				} else if (Integer.parseInt(txtRow.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Column Index Selected!!! Must be at least 0");
				} else if (Integer.parseInt(txtCol.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Row Index Selected!!! Must be at least 0");
				} else {
					setProgress(0);
					LogItem old_li = null;
					
					for (int x = 0; x < TABFiles.size(); x++) {
						File XTAB = TABFiles.get(x);
						// Construct output filename
						String NAME = ExtensionFileFilter.stripExtensionIgnoreGZ(XTAB) + "_TRANSPOSE." + ExtensionFileFilter.getExtensionIgnoreGZ(XTAB);
						NAME += chckbxGzipOutput.isSelected() ? ".gz" : "";
						File OUT_FILEPATH = new File(NAME);
						if (OUT_DIR != null) {
							OUT_FILEPATH = new File(OUT_DIR + File.separator + NAME);
						}
						// Initialize LogItem
						String command = TransposeMatrixCLI.getCLIcommand(XTAB, OUT_FILEPATH,
								Integer.parseInt(txtRow.getText()), Integer.parseInt(txtCol.getText()), chckbxGzipOutput.isSelected());
						LogItem new_li = new LogItem(command);
						firePropertyChange("log", old_li, new_li);
						// Execute script
						TransposeMatrix.transpose(XTAB, OUT_FILEPATH,
								Integer.parseInt(txtRow.getText()), Integer.parseInt(txtCol.getText()), chckbxGzipOutput.isSelected());
						// Update log item
						new_li.setStopTime(new Timestamp(new Date().getTime()));
						new_li.setStatus(0);
						old_li = new_li;
						// Update progress
						int percentComplete = (int) (((double) (x + 1) / TABFiles.size()) * 100);
						setProgress(percentComplete);
					}
					firePropertyChange("log", old_li, null);
					setProgress(100);
					JOptionPane.showMessageDialog(null, "All Matrices Transposed");
				}
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Invalid Scaling Factor!!! Must be number");
			} catch (ScriptManagerException sme) {
				sme.printStackTrace();
				JOptionPane.showMessageDialog(null, sme.getMessage());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				JOptionPane.showMessageDialog(null, "I/O issues: " + ioe.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, ToolDescriptions.UNEXPECTED_EXCEPTION_MESSAGE + e.getMessage());
			}
			setProgress(100);
			return null;
		}

		public void done() {
			massXable(contentPane, true);
			setCursor(null); // turn off the wait cursor
		}
	}

	/**
	 * Instantiate window with graphical interface design.
	 */
	public TransposeMatrixWindow() {
		setTitle("Transpose Matrix ");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane = new JScrollPane(listExp);

		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -150, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);

		btnLoad = new JButton("Load TAB/CDT Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 6, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newTABFiles = FileSelection.getGenericFiles(fc);
				if (newTABFiles != null) {
					for (int x = 0; x < newTABFiles.length; x++) {
						TABFiles.add(newTABFiles[x]);
						expList.addElement(newTABFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemoveBam = new JButton("Remove TAB/CDT");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -10, SpringLayout.EAST, contentPane);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listExp.getSelectedIndex() > -1) {
					TABFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemoveBam);

		btnCalculate = new JButton("Transpose");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCalculate, 165, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCalculate, -165, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnCalculate, -10, SpringLayout.SOUTH, contentPane);
		contentPane.add(btnCalculate);
		btnCalculate.setActionCommand("start");
		btnCalculate.addActionListener(this);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		lblCurrent = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrent, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblCurrent, -45, SpringLayout.SOUTH, contentPane);
		lblCurrent.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrent);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, lblCurrent);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrent);
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);

		btnOutput = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, -30, SpringLayout.WEST, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -10, SpringLayout.NORTH, lblDefaultToLocal);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -135, SpringLayout.EAST, contentPane);
		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUT_DIR = FileSelection.getOutputDir(fc);
				if (OUT_DIR != null) {
					lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
				}
			}
		});
		contentPane.add(btnOutput);


		JLabel lblRow = new JLabel("Start at Row:");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblRow, -30, SpringLayout.NORTH, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblRow, 35, SpringLayout.WEST, contentPane);
		contentPane.add(lblRow);

		JLabel lblCol = new JLabel("Start at Column:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCol, 0, SpringLayout.NORTH, lblRow);
		contentPane.add(lblCol);

		txtRow = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.WEST, txtRow, 8, SpringLayout.EAST, lblRow);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCol, 31, SpringLayout.EAST, txtRow);
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtRow, -2, SpringLayout.NORTH, lblRow);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtRow, 76, SpringLayout.EAST, lblRow);
		txtRow.setHorizontalAlignment(SwingConstants.CENTER);
		txtRow.setText("0");
		txtRow.setToolTipText("zero-indexed");
		contentPane.add(txtRow);
		txtRow.setColumns(10);

		txtCol = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtCol, -2, SpringLayout.NORTH, lblRow);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtCol, 8, SpringLayout.EAST, lblCol);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtCol, 76, SpringLayout.EAST, lblCol);
		txtCol.setHorizontalAlignment(SwingConstants.CENTER);
		txtCol.setText("0");
		txtCol.setToolTipText("zero-indexed");
		contentPane.add(txtCol);
		txtCol.setColumns(10);

		chckbxGzipOutput = new JCheckBox("Output Gzip");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxGzipOutput, 0, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.EAST, chckbxGzipOutput, -5, SpringLayout.WEST, btnOutput);
		contentPane.add(chckbxGzipOutput);
		;
	}

	/**
	 * Runs when a task is invoked, making window non-interactive and executing the task.
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		massXable(contentPane, false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}

	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		} else if ("log" == evt.getPropertyName()){
			firePropertyChange("log", evt.getOldValue(), evt.getNewValue());
		}
	}

	/**
	 * Makes the content pane non-interactive If the window should be interactive data
	 * @param con Content pane to make non-interactive
	 * @param status If the window should be interactive
	 */
	public void massXable(Container con, boolean status) {
		for (Component c : con.getComponents()) {
			c.setEnabled(status);
			if (c instanceof Container) {
				massXable((Container) c, status);
			}
		}
	}
}