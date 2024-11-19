package scriptmanager.window_interface.Coordinate_Manipulation.GFF_Manipulation;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.SwingWorker;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import scriptmanager.cli.Coordinate_Manipulation.GFF_Manipulation.ExpandGFFCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.ExpandGFF;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.ExpandGFF}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Coordinate_Manipulation.GFF_Manipulation.ExpandGFF
 */
@SuppressWarnings("serial")
public class ExpandGFFWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	private JProgressBar progressBar;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	private File OUT_DIR = null;
	private static int SIZE = -999;
	final DefaultListModel<String> expList;
	Vector<File> GFFFiles = new Vector<File>();

	private JButton btnLoad;
	private JButton btnRemoveGFF;
	private JButton btnConvert;

	/**
	 * Used to run the script efficiently
	 */
	public Task task;
	private JLabel lblCurrent;
	private JLabel lblDefaultToLocal;
	private JButton btnOutput;
	private JTextField txtSize;

	private static JRadioButton rdbtnExpandFromCenter;
	private static JRadioButton rdbtnAddToBorder;
	private static JCheckBox chckbxGzipOutput;

	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			try {
				SIZE = Integer.parseInt(txtSize.getText());
				if (SIZE < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Expansion Size!!! Must be larger than 0 bp");
				} else {
					setProgress(0);
					LogItem old_li = new LogItem("");
					for (int x = 0; x < GFFFiles.size(); x++) {
						File XGFF = GFFFiles.get(x);

						// Set outfilepath
						String OUTPUT = ExtensionFileFilter.stripExtensionIgnoreGZ(XGFF) + "_"
								+ Integer.toString(SIZE) + "bp.gff";
						if (OUT_DIR != null) {
							OUTPUT = OUT_DIR + File.separator + OUTPUT;
						}
						// Initialize LogItem
						String command = ExpandGFFCLI.getCLIcommand(XGFF, new File(OUTPUT), SIZE, rdbtnExpandFromCenter.isSelected(), chckbxGzipOutput.isSelected());
						LogItem new_li = new LogItem(command);
						firePropertyChange("log", old_li, new_li);
						// Execute expansion and update progress
						ExpandGFF.expandGFFBorders(XGFF, new File(OUTPUT), SIZE, rdbtnExpandFromCenter.isSelected(), chckbxGzipOutput.isSelected());
						int percentComplete = (int) (((double) (x + 1) / GFFFiles.size()) * 100);
						// Update log item
						new_li.setStopTime(new Timestamp(new Date().getTime()));
						new_li.setStatus(0);
						old_li = new_li;
						setProgress(percentComplete);
					}
					firePropertyChange("log", old_li, null);
					setProgress(100);
					JOptionPane.showMessageDialog(null, "Conversion Complete");
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
			setProgress(100);
			return null;
		}

		public void done() {
			massXable(contentPane, true);
			setCursor(null); // turn off the wait cursor
		}
	}

	/**
	 * Creates a new ExpandGFFWindow
	 */
	public ExpandGFFWindow() {
		setTitle("Expand GFF File");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 345);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 36, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);

		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);

		btnLoad = new JButton("Load GFF Files");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 0, SpringLayout.WEST, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnLoad, -6, SpringLayout.NORTH, scrollPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newGFFFiles = FileSelection.getFiles(fc, "gff", true);
				if (newGFFFiles != null) {
					for (int x = 0; x < newGFFFiles.length; x++) {
						GFFFiles.add(newGFFFiles[x]);
						expList.addElement(newGFFFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemoveGFF = new JButton("Remove GFF");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnRemoveGFF, -6, SpringLayout.NORTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveGFF, 0, SpringLayout.EAST, scrollPane);
		btnRemoveGFF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listExp.getSelectedIndex() > -1) {
					GFFFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemoveGFF);

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
		lblCurrent.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrent);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, lblCurrent);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrent);
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);

		btnOutput = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 10, SpringLayout.WEST, contentPane);
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

		rdbtnExpandFromCenter = new JRadioButton("Expand from Center");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnExpandFromCenter, 6, SpringLayout.SOUTH, scrollPane);
		contentPane.add(rdbtnExpandFromCenter);

		rdbtnAddToBorder = new JRadioButton("Add to Border");
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnAddToBorder, 231, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnExpandFromCenter, -6, SpringLayout.WEST, rdbtnAddToBorder);
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnAddToBorder, 6, SpringLayout.SOUTH, scrollPane);
		contentPane.add(rdbtnAddToBorder);

		ButtonGroup ExpansionType = new ButtonGroup();
		ExpansionType.add(rdbtnExpandFromCenter);
		ExpansionType.add(rdbtnAddToBorder);
		rdbtnExpandFromCenter.setSelected(true);

		txtSize = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutput, 6, SpringLayout.SOUTH, txtSize);
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtSize, 6, SpringLayout.SOUTH, rdbtnAddToBorder);
		txtSize.setHorizontalAlignment(SwingConstants.CENTER);
		txtSize.setText("250");
		contentPane.add(txtSize);
		txtSize.setColumns(10);

		JLabel lblSizeOfExpansion = new JLabel("Size of Expansion (bp):");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSizeOfExpansion, 8, SpringLayout.SOUTH,
				rdbtnExpandFromCenter);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtSize, 10, SpringLayout.EAST, lblSizeOfExpansion);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtSize, 68, SpringLayout.EAST, lblSizeOfExpansion);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSizeOfExpansion, 100, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrent, 40, SpringLayout.SOUTH, lblSizeOfExpansion);
		contentPane.add(lblSizeOfExpansion);
		btnConvert.addActionListener(this);
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
	}
}
