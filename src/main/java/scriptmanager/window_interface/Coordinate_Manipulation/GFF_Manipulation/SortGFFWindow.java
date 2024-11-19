package scriptmanager.window_interface.Coordinate_Manipulation.GFF_Manipulation;

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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
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
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import scriptmanager.cli.Coordinate_Manipulation.GFF_Manipulation.SortGFFCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.CDTUtilities;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;
import scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.SortGFF;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.SortGFF}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.SortGFF
 */
@SuppressWarnings("serial")
public class SortGFFWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	private static File OUT_DIR = null;
	private File GFF_File = null;
	private File CDT_File = null;

	private boolean CDT_VALID = false;
	private int CDT_SIZE = -999;
	private static int START_INDEX = -999;
	private static int STOP_INDEX = -999;

	private JButton btnLoadGFFFile;
	private JButton btnLoadCdtFile;
	private JButton btnOutput;
	private JButton btnConvert;
	private JCheckBox chckbxGzipOutput;

	private JProgressBar progressBar;
	/**
	 * Used to run the script efficiently
	 */
	public Task task;
	private JLabel lblCurrent;
	private JLabel lblDefaultToLocal;

	private static JRadioButton rdbtnSortbyCenter;
	private static JRadioButton rdbtnSortbyIndex;
	private JTextField txtOutput;
	private JTextField txtMid;
	private JTextField txtStart;
	private JTextField txtStop;
	private JLabel lblOutputFileName;
	private JLabel lblSizeOfExpansion;
	private JLabel lblGFFFile;
	private JLabel lblCDTFile;
	private JLabel lblCdtFileStatistics;
	private JLabel lblColumnCount;
	private JLabel lblIndexStart;
	private JLabel lblIndexStop;

	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			try {
				if (rdbtnSortbyCenter.isSelected() && Integer.parseInt(txtMid.getText()) > CDT_SIZE) {
					JOptionPane.showMessageDialog(null, "Sort Size is larger than CDT File!!!");
				} else if (rdbtnSortbyIndex.isSelected() && Integer.parseInt(txtStart.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Start Index is smaller than 0!!!");
				} else if (rdbtnSortbyIndex.isSelected() && Integer.parseInt(txtStop.getText()) > CDT_SIZE) {
					JOptionPane.showMessageDialog(null, "Stop Index is larger than CDT row size!!!");
				} else {
					if (rdbtnSortbyCenter.isSelected()) {
						START_INDEX = (CDT_SIZE / 2) - (Integer.parseInt(txtMid.getText()) / 2);
						STOP_INDEX = (CDT_SIZE / 2) + (Integer.parseInt(txtMid.getText()) / 2);
					} else {
						START_INDEX = Integer.parseInt(txtStart.getText());
						STOP_INDEX = Integer.parseInt(txtStop.getText());
					}

					String OUTPUT = ExtensionFileFilter.stripExtensionIgnoreGZ(GFF_File);
					if (OUT_DIR != null) {
						OUTPUT = OUT_DIR.getCanonicalPath() + File.separator + OUTPUT;
					}

					setProgress(0);
					LogItem old_li = null;
					// Initialize LogItem
					String command = SortGFFCLI.getCLIcommand(GFF_File, CDT_File, new File(OUTPUT), START_INDEX, STOP_INDEX, chckbxGzipOutput.isSelected());
					LogItem new_li = new LogItem(command);
					firePropertyChange("log", old_li, new_li);
					// Execute script
					SortGFF.sortGFFbyCDT(GFF_File, CDT_File, new File(OUTPUT), START_INDEX, STOP_INDEX, chckbxGzipOutput.isSelected());
					// Update log item
					new_li.setStopTime(new Timestamp(new Date().getTime()));
					new_li.setStatus(0);
					old_li = new_li;
					firePropertyChange("log", old_li, null);
					setProgress(100);
					JOptionPane.showMessageDialog(null, "Sort Complete");
				}
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				JOptionPane.showMessageDialog(null, "I/O issues: " + ioe.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, ToolDescriptions.UNEXPECTED_EXCEPTION_MESSAGE + e.getMessage());
			}
			return null;
		}

		public void done() {
			massXable(contentPane, true);
			setCursor(null); // turn off the wait cursor
		}
	}

	public SortGFFWindow() {
		setTitle("Sort GFF File");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 345);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		btnConvert = new JButton("Convert");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnConvert, 167, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnConvert, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnConvert, -175, SpringLayout.EAST, contentPane);
		contentPane.add(btnConvert);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnConvert);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -5, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		btnConvert.setActionCommand("start");

		lblCurrent = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrent, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblCurrent, -33, SpringLayout.SOUTH, contentPane);
		lblCurrent.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrent);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, lblCurrent);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrent);
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);

		btnOutput = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -6, SpringLayout.NORTH, lblDefaultToLocal);
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
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxGzipOutput, 0, SpringLayout.NORTH, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.EAST, chckbxGzipOutput, -10, SpringLayout.EAST, contentPane);
		contentPane.add(chckbxGzipOutput);

		chckbxGzipOutput.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				String NAME = txtOutput.getText();
				if (chckbxGzipOutput.isSelected()) {
					if (!NAME.endsWith(".gz")) {
						txtOutput.setText(NAME + ".gz");
					}
				} else {
					if (NAME.endsWith(".gz")) {
						NAME = ExtensionFileFilter.stripExtension(NAME);
						txtOutput.setText(NAME);
					}
				}
			}
		});

		rdbtnSortbyCenter = new JRadioButton("Sort by Center");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnSortbyCenter, 129, SpringLayout.NORTH, contentPane);
		contentPane.add(rdbtnSortbyCenter);

		rdbtnSortbyIndex = new JRadioButton("Sort by Index");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnSortbyIndex, 15, SpringLayout.SOUTH, rdbtnSortbyCenter);
		contentPane.add(rdbtnSortbyIndex);

		ButtonGroup ExpansionType = new ButtonGroup();
		ExpansionType.add(rdbtnSortbyCenter);
		ExpansionType.add(rdbtnSortbyIndex);
		rdbtnSortbyCenter.setSelected(true);

		txtMid = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtMid, 2, SpringLayout.NORTH, rdbtnSortbyCenter);
		txtMid.setHorizontalAlignment(SwingConstants.CENTER);
		txtMid.setText("100");
		contentPane.add(txtMid);
		txtMid.setColumns(10);

		lblSizeOfExpansion = new JLabel("Size of Expansion (bins):");
		sl_contentPane.putConstraint(SpringLayout.WEST, txtMid, 6, SpringLayout.EAST, lblSizeOfExpansion);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtMid, 59, SpringLayout.EAST, lblSizeOfExpansion);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSizeOfExpansion, 4, SpringLayout.NORTH, rdbtnSortbyCenter);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSizeOfExpansion, 150, SpringLayout.WEST, contentPane);
		contentPane.add(lblSizeOfExpansion);

		txtOutput = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.EAST, txtOutput, -15, SpringLayout.EAST, contentPane);
		txtOutput.setEnabled(false);
		contentPane.add(txtOutput);
		txtOutput.setColumns(10);

		lblOutputFileName = new JLabel("Output File Name:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtOutput, -2, SpringLayout.NORTH, lblOutputFileName);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtOutput, 10, SpringLayout.EAST, lblOutputFileName);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblOutputFileName, 16, SpringLayout.SOUTH, rdbtnSortbyIndex);
		contentPane.add(lblOutputFileName);

		lblGFFFile = new JLabel("No GFF File Loaded");
		contentPane.add(lblGFFFile);

		lblCDTFile = new JLabel("No CDT File Loaded");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCDTFile, 0, SpringLayout.WEST, lblGFFFile);
		contentPane.add(lblCDTFile);

		lblCdtFileStatistics = new JLabel("CDT File Statistics:");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCdtFileStatistics, 0, SpringLayout.WEST, rdbtnSortbyCenter);
		contentPane.add(lblCdtFileStatistics);

		lblColumnCount = new JLabel("No CDT File Loaded");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblColumnCount, 0, SpringLayout.NORTH, lblCdtFileStatistics);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblColumnCount, 0, SpringLayout.WEST, lblGFFFile);
		contentPane.add(lblColumnCount);

		txtStart = new JTextField();
		txtStart.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtStart, 2, SpringLayout.NORTH, rdbtnSortbyIndex);
		contentPane.add(txtStart);
		txtStart.setColumns(10);

		txtStop = new JTextField();
		txtStop.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtStop, 2, SpringLayout.NORTH, rdbtnSortbyIndex);
		contentPane.add(txtStop);
		txtStop.setColumns(10);

		lblIndexStart = new JLabel("Index Start:");
		sl_contentPane.putConstraint(SpringLayout.EAST, txtStart, 59, SpringLayout.EAST, lblIndexStart);
		lblIndexStart.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtStart, 6, SpringLayout.EAST, lblIndexStart);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblIndexStart, 4, SpringLayout.NORTH, rdbtnSortbyIndex);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblIndexStart, 150, SpringLayout.WEST, contentPane);
		contentPane.add(lblIndexStart);

		lblIndexStop = new JLabel("Index Stop:");
		sl_contentPane.putConstraint(SpringLayout.EAST, txtStop, 59, SpringLayout.EAST, lblIndexStop);
		lblIndexStop.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtStop, 6, SpringLayout.EAST, lblIndexStop);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblIndexStop, 4, SpringLayout.NORTH, rdbtnSortbyIndex);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblIndexStop, 0, SpringLayout.WEST, progressBar);
		contentPane.add(lblIndexStop);
		btnConvert.addActionListener(this);

		rdbtnSortbyCenter.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnSortbyCenter.isSelected()) {
					txtMid.setEnabled(true);
					lblSizeOfExpansion.setEnabled(true);
					txtStart.setEnabled(false);
					lblIndexStart.setEnabled(false);
					txtStop.setEnabled(false);
					lblIndexStop.setEnabled(false);
				}
			}
		});

		rdbtnSortbyIndex.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnSortbyIndex.isSelected()) {
					txtMid.setEnabled(false);
					lblSizeOfExpansion.setEnabled(false);
					txtStart.setEnabled(true);
					lblIndexStart.setEnabled(true);
					txtStop.setEnabled(true);
					lblIndexStop.setEnabled(true);
				}
			}
		});

		btnLoadGFFFile = new JButton("Load GFF File");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblGFFFile, 5, SpringLayout.NORTH, btnLoadGFFFile);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblGFFFile, 14, SpringLayout.EAST, btnLoadGFFFile);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadGFFFile, 10, SpringLayout.NORTH, contentPane);
		btnLoadGFFFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File newGFFFile = FileSelection.getFile(fc, "gff", true);
				if (newGFFFile != null) {
					GFF_File = newGFFFile;
					lblGFFFile.setText(GFF_File.getName());
					txtOutput.setEnabled(true);
					// Set default output filename
					String sortName = "";
					try {
						sortName = ExtensionFileFilter.stripExtensionIgnoreGZ(GFF_File) + "_SORT.gff";
						sortName += chckbxGzipOutput.isSelected() ? ".gz" : "";
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(null, "Invalid GFF");
					}
					txtOutput.setText(sortName);
				}
			}
		});
		contentPane.add(btnLoadGFFFile);

		btnLoadCdtFile = new JButton("Load CDT File");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCdtFileStatistics, 20, SpringLayout.SOUTH, btnLoadCdtFile);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadCdtFile, 16, SpringLayout.SOUTH, btnLoadGFFFile);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCDTFile, 5, SpringLayout.NORTH, btnLoadCdtFile);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadCdtFile, 0, SpringLayout.WEST, rdbtnSortbyCenter);
		btnLoadCdtFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File newCDTFile = FileSelection.getFile(fc, "cdt", true);
				if (newCDTFile != null) {
					try {
						CDT_File = newCDTFile;
						lblCDTFile.setText(CDT_File.getName());

						CDTUtilities cdt_obj = new CDTUtilities();
						cdt_obj.parseCDT(CDT_File);
						CDT_SIZE = cdt_obj.getSize();
						CDT_VALID = cdt_obj.isValid();
						String message = cdt_obj.getInvalidMessage();
						System.err.println(CDT_File.getCanonicalPath() + ": " + message);
						if (!message.equals("")) {
							JOptionPane.showMessageDialog(null, message);
						}
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
					}

					if (CDT_VALID) {
						lblColumnCount.setText("Column Count: " + CDT_SIZE);
					} else {
						lblColumnCount.setText("CDT File does not possess equal number of columns!!!");
					}
				}
			}
		});
		contentPane.add(btnLoadCdtFile);
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
	 * Invoked when task's progress property changes and updates the progress bar
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
			if (rdbtnSortbyCenter.isSelected()) {
				txtMid.setEnabled(true);
				lblSizeOfExpansion.setEnabled(true);
				txtStart.setEnabled(false);
				lblIndexStart.setEnabled(false);
				txtStop.setEnabled(false);
				lblIndexStop.setEnabled(false);
			}
			if (rdbtnSortbyIndex.isSelected()) {
				txtMid.setEnabled(false);
				lblSizeOfExpansion.setEnabled(false);
				txtStart.setEnabled(true);
				lblIndexStart.setEnabled(true);
				txtStop.setEnabled(true);
				lblIndexStop.setEnabled(true);
			}
		}
	}
}