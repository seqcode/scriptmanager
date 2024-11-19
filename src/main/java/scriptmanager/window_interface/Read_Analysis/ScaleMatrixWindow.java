package scriptmanager.window_interface.Read_Analysis;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import scriptmanager.objects.LogItem;
import scriptmanager.objects.PasteableTable;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;

import scriptmanager.cli.Read_Analysis.ScaleMatrixCLI;
import scriptmanager.scripts.Read_Analysis.ScaleMatrix;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Read_Analysis.ScaleMatrix}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Read_Analysis.ScalingFactor
 * @see scriptmanager.window_interface.Read_Analysis.ScalingFactorOutput
 */
@SuppressWarnings("serial")
public class ScaleMatrixWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

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

	private JLabel lblUniformScalingFactor;
	private JTextField txtUniform;
	private JTextField txtRow;
	private JTextField txtCol;

	private JRadioButton rdbtnFilespecifcScaling;
	private JRadioButton rdbtnUniformScaling;

	private DefaultTableModel expTable;

	/**
	 * Used to run the script efficiently
	 */
	public Task task;

	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() throws IOException {
			try {
				if (TABFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No Files Loaded!!!");
				} else if (Integer.parseInt(txtRow.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Column Index Selected!!! Must be at least 0");
				} else if (Integer.parseInt(txtCol.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Row Index Selected!!! Must be at least 0");
				} else {
					// Check that all scaling numbers are valid
					boolean ALLNUM = true;
					if (rdbtnUniformScaling.isSelected()) {
						System.out.println("Uniform scaling factor check...");
						try {
							Double.parseDouble(txtUniform.getText());
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null, "Uniform scaling value an invalid scaling factor (" + txtUniform.getText() + ") !!!");
							ALLNUM = false;
						}
					} else {
						System.out.println("File-sspecific scaling factor check...");
						for (int x = 0; x < TABFiles.size(); x++) {
							System.out.println(x);
							try {
								Double.parseDouble(expTable.getValueAt(x, 1).toString());
							} catch (NumberFormatException e) {
								JOptionPane.showMessageDialog(null, TABFiles.get(x).getName() + " possesses an invalid scaling factor (" + expTable.getValueAt(x, 1) + ") !!!");
								ALLNUM = false;
							}
						}
					}
					// Loop through matrix files with valid scaling factors
					if (ALLNUM) {
						setProgress(0);
						double SCALE = 0;
						if (rdbtnUniformScaling.isSelected()) {
							SCALE = Double.parseDouble(txtUniform.getText());
						}
						LogItem old_li = null;
						for (int x = 0; x < TABFiles.size(); x++) {
							// Pull input file
							File XTAB = TABFiles.get(x);
							// Construct output filename
							String NAME = ExtensionFileFilter.stripExtensionIgnoreGZ(XTAB) + "_SCALE."
									+ ExtensionFileFilter.getExtensionIgnoreGZ(XTAB)
									+ (chckbxGzipOutput.isSelected() ? ".gz" : "");
							File OUT_FILEPATH = new File(NAME);
							if (OUT_DIR != null) {
								OUT_FILEPATH = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME);
							}
							// Determine out scaling factor
							if (rdbtnFilespecifcScaling.isSelected()) {
								SCALE = Double.parseDouble(expTable.getValueAt(x, 1).toString());
							}
							// Initialize LogItem
							String command = ScaleMatrixCLI.getCLIcommand(XTAB, OUT_FILEPATH, SCALE, Integer.parseInt(txtRow.getText()), Integer.parseInt(txtCol.getText()), chckbxGzipOutput.isSelected());
							LogItem new_li = new LogItem(command);
							firePropertyChange("log", old_li, new_li);
							// Execute script
							ScaleMatrix script_obj = new ScaleMatrix(XTAB, OUT_FILEPATH, SCALE, Integer.parseInt(txtRow.getText()), Integer.parseInt(txtCol.getText()), chckbxGzipOutput.isSelected());
							script_obj.run();
							// Update log item
							new_li.setStopTime(new Timestamp(new Date().getTime()));
							new_li.setStatus(0);
							old_li = new_li;
							// Update progress
							int percentComplete = (int) (((double) (x + 1) / TABFiles.size()) * 100);
							setProgress(percentComplete);
						}
						// Update log after final input
						firePropertyChange("log", old_li, null);
						// Update progress
						setProgress(100);
						JOptionPane.showMessageDialog(null, "All Matrices Scaled");
					}
				}
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Invalid Scaling Factor!!! Must be number");
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
	public ScaleMatrixWindow() {
		setTitle("Apply Scaling Factor");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		String[] TableHeader = { "Experiment", "Scaling Factor" };
		expTable = new DefaultTableModel(null, TableHeader) {
			@Override
			public Class<?> getColumnClass(int col) {
				return getValueAt(0, col).getClass();
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				if (column == 0) {
					return false;
				} else {
					return true;
				}
			}
		};
		JTable tableScale = new JTable(expTable);
		// Allow for the selection of multiple OR individual cells across either rows or columns
		tableScale.setCellSelectionEnabled(true);
		tableScale.setColumnSelectionAllowed(true);
		tableScale.setRowSelectionAllowed(true);
		@SuppressWarnings("unused")
		PasteableTable myAd = new PasteableTable(tableScale);

		JScrollPane scrollPane = new JScrollPane(tableScale);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -190, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);

		btnLoad = new JButton("Load TAB Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 6, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newTABFiles = FileSelection.getGenericFiles(fc);
				if (newTABFiles != null) {
					for (int x = 0; x < newTABFiles.length; x++) {
						TABFiles.add(newTABFiles[x]);
						expTable.addRow(new Object[] { newTABFiles[x].getName(), "0" });
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemoveBam = new JButton("Remove TAB");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -10, SpringLayout.EAST, contentPane);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int[] index = tableScale.getSelectedRows();
				for (int x = index.length - 1; x >= 0; x--) {
					TABFiles.remove(index[x]);
					expTable.removeRow(index[x]);
				}
			}
		});
		contentPane.add(btnRemoveBam);

		btnCalculate = new JButton("Scale");
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
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 0, SpringLayout.WEST, btnCalculate);
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

		chckbxGzipOutput = new JCheckBox("Output GZip");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxGzipOutput, 0, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxGzipOutput, 30, SpringLayout.WEST, contentPane);
		contentPane.add(chckbxGzipOutput);

		rdbtnFilespecifcScaling = new JRadioButton("File-specifc Scaling");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnFilespecifcScaling, 8, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnFilespecifcScaling, -66, SpringLayout.EAST, contentPane);
		contentPane.add(rdbtnFilespecifcScaling);

		rdbtnUniformScaling = new JRadioButton("Uniform Scaling");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnUniformScaling, 0, SpringLayout.NORTH,
				rdbtnFilespecifcScaling);
		sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnUniformScaling, -32, SpringLayout.WEST,
				rdbtnFilespecifcScaling);
		contentPane.add(rdbtnUniformScaling);

		ButtonGroup SCALETYPE = new ButtonGroup();
		SCALETYPE.add(rdbtnFilespecifcScaling);
		SCALETYPE.add(rdbtnUniformScaling);
		rdbtnFilespecifcScaling.setSelected(true);

		lblUniformScalingFactor = new JLabel("Uniform Scaling Factor:");
		lblUniformScalingFactor.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblUniformScalingFactor, 0, SpringLayout.WEST,
				rdbtnUniformScaling);
		contentPane.add(lblUniformScalingFactor);

		txtUniform = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtUniform, 8, SpringLayout.SOUTH, rdbtnFilespecifcScaling);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblUniformScalingFactor, 2, SpringLayout.NORTH, txtUniform);
		txtUniform.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtUniform, 22, SpringLayout.EAST, lblUniformScalingFactor);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtUniform, -142, SpringLayout.EAST, contentPane);
		txtUniform.setHorizontalAlignment(SwingConstants.CENTER);
		txtUniform.setText("1");
		contentPane.add(txtUniform);
		txtUniform.setColumns(10);
		txtUniform.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int x = 0; x < expTable.getRowCount(); x++) {
					expTable.setValueAt(txtUniform.getText(), x, 1);
				}
        	}
		});

		JLabel lblRow = new JLabel("Start at Row:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblRow, 10, SpringLayout.SOUTH, lblUniformScalingFactor);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblRow, 10, SpringLayout.WEST, contentPane);
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
		txtRow.setText("1");
		contentPane.add(txtRow);
		txtRow.setColumns(10);

		txtCol = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtCol, -2, SpringLayout.NORTH, lblRow);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtCol, 8, SpringLayout.EAST, lblCol);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtCol, 76, SpringLayout.EAST, lblCol);
		txtCol.setHorizontalAlignment(SwingConstants.CENTER);
		txtCol.setText("2");
		contentPane.add(txtCol);
		txtCol.setColumns(10);
		;

		rdbtnUniformScaling.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnUniformScaling.isSelected()) {
					lblUniformScalingFactor.setEnabled(true);
					txtUniform.setEnabled(true);
				}
			}
		});
		rdbtnFilespecifcScaling.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnFilespecifcScaling.isSelected()) {
					lblUniformScalingFactor.setEnabled(false);
					txtUniform.setEnabled(false);
				}
			}
		});

	}

	public void activateOutput(boolean activate) {
		btnOutput.setEnabled(activate);
		lblDefaultToLocal.setEnabled(activate);
		lblCurrent.setEnabled(activate);
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
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		} else if ("log" == evt.getPropertyName()) {
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
		if (status) {
			if (rdbtnUniformScaling.isSelected()) {
				lblUniformScalingFactor.setEnabled(true);
				txtUniform.setEnabled(true);
			} else {
				lblUniformScalingFactor.setEnabled(false);
				txtUniform.setEnabled(false);
			}
		}
	}
}