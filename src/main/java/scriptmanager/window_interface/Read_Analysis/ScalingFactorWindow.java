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
import java.util.ArrayList;

import javax.swing.ButtonGroup;
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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.FileSelection;

import scriptmanager.scripts.Read_Analysis.ScalingFactor;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Read_Analysis.ScalingFactor}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Read_Analysis.ScalingFactor
 * @see scriptmanager.window_interface.Read_Analysis.ScalingFactorOutput
 */
@SuppressWarnings("serial")
public class ScalingFactorWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	final DefaultListModel<String> expList;
	private ArrayList<File> BAMFiles = new ArrayList<File>();
	private File BLACKLIST = null;
	private File CONTROL = null;
	private File OUT_DIR = null;

	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnCalculate;
	private JButton btnLoadControlBam;
	private JButton btnRemoveBlacklistFilter;
	private JButton btnOutput;
	private JCheckBox chckbxOutputStatistics;
	private JLabel lblNoControlLoaded;
	private JLabel lblCurrentControl;
	private JLabel lblWindow;
	private JLabel lblMinFraction;
	private JLabel lblDefaultToLocal;
	private JLabel lblCurrent;
	private JLabel lblNoBlacklistLoaded;
	private JProgressBar progressBar;
	private JRadioButton rdbtnTotalTag;
	private JRadioButton rdbtnNCIS;
	private JRadioButton rdbtnNcisWithTotal;
	private JTextField txtWindow;
	private JTextField txtFraction;

	/**
	 * Used to run the script efficiently
	 */
	public Task task;

	
	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			try {
				if (BAMFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No BAM Files Loaded!!!");
				} else if (!rdbtnTotalTag.isSelected() && lblNoControlLoaded.getText().equals("No Control Loaded")) {
					JOptionPane.showMessageDialog(null, "No Control File Loaded!!!");
				} else if (!rdbtnTotalTag.isSelected() && Integer.parseInt(txtWindow.getText()) <= 0) {
					JOptionPane.showMessageDialog(null, "Invalid Window Size Selected!!!");
				} else if (!rdbtnTotalTag.isSelected() && (Double.parseDouble(txtFraction.getText()) >= 1
						|| Double.parseDouble(txtFraction.getText()) <= 0)) {
					JOptionPane.showMessageDialog(null, "Invalid Minimum Fraction Selected!!! Must be between 0 & 1");
				} else {
					setProgress(0);

					int scaleType = 0;
					if (rdbtnTotalTag.isSelected()) {
						scaleType = ScalingFactor.TOTAL_TAG;
						CONTROL = null;
					} else if (rdbtnNCIS.isSelected()) {
						scaleType = ScalingFactor.NCIS;
					} else if (rdbtnNcisWithTotal.isSelected()) {
						scaleType = ScalingFactor.NCIS_W_TOTAL_TAG;
					}
					// Execute script
					ScalingFactorOutput output_obj = new ScalingFactorOutput(BAMFiles, BLACKLIST, CONTROL,
							OUT_DIR, scaleType, Integer.parseInt(txtWindow.getText()),
							Double.parseDouble(txtFraction.getText()), chckbxOutputStatistics.isSelected());
					output_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
							int temp = (Integer) propertyChangeEvent.getNewValue();
							int percentComplete = (int) (((double) (temp) / BAMFiles.size()) * 100);
							setProgress(percentComplete);
						}
					});
					output_obj.addPropertyChangeListener("log", new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							firePropertyChange("log", evt.getOldValue(), evt.getNewValue());
						}
					});
					output_obj.run();
					output_obj.setVisible(true);
					JOptionPane.showMessageDialog(null, "All Scaling Factors Calculated");
				}
			} catch (OptionException oe) {
				oe.printStackTrace();
				JOptionPane.showMessageDialog(null, oe.getMessage());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				JOptionPane.showMessageDialog(null, "I/O issues: " + ioe.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, ToolDescriptions.UNEXPECTED_EXCEPTION_MESSAGE + e.getMessage());
			}
			// Update progress
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
	public ScalingFactorWindow() {
		setTitle("Calculate Scaling Factor");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 515);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -295, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);

		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);

		btnLoad = new JButton("Load BAM Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 6, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newBAMFiles = FileSelection.getFiles(fc, "bam");
				if (newBAMFiles != null) {
					for (int x = 0; x < newBAMFiles.length; x++) {
						BAMFiles.add(newBAMFiles[x]);
						expList.addElement(newBAMFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemoveBam = new JButton("Remove BAM");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -10, SpringLayout.EAST, contentPane);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listExp.getSelectedIndex() > -1) {
					BAMFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemoveBam);

		btnCalculate = new JButton("Calculate");
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
		lblCurrent.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrent, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblCurrent, -45, SpringLayout.SOUTH, contentPane);
		lblCurrent.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrent);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		lblDefaultToLocal.setEnabled(false);
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, lblCurrent);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrent);
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);

		btnOutput = new JButton("Output Directory");
		btnOutput.setEnabled(false);
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
		;

		lblCurrentControl = new JLabel("Current Control:");
		lblCurrentControl.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentControl, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblCurrentControl);

		lblNoControlLoaded = new JLabel("No Control Loaded");
		lblNoControlLoaded.setEnabled(false);
		lblNoControlLoaded.setFont(new Font("Dialog", Font.PLAIN, 12));
		sl_contentPane.putConstraint(SpringLayout.WEST, lblNoControlLoaded, 0, SpringLayout.WEST, lblDefaultToLocal);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblNoControlLoaded, 0, SpringLayout.SOUTH, lblCurrentControl);
		contentPane.add(lblNoControlLoaded);

		btnLoadControlBam = new JButton("Load Control BAM File");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnLoadControlBam, -200, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentControl, 10, SpringLayout.SOUTH, btnLoadControlBam);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadControlBam, 10, SpringLayout.WEST, contentPane);
		btnLoadControlBam.setEnabled(false);
		btnLoadControlBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File newControl = FileSelection.getFile(fc, "bam");
				if (newControl != null) {
					CONTROL = newControl.getAbsoluteFile();
					lblNoControlLoaded.setText(newControl.getName());
				}
			}
		});
		contentPane.add(btnLoadControlBam);

		rdbtnTotalTag = new JRadioButton("Total Tag");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnTotalTag, 10, SpringLayout.SOUTH, lblCurrentControl);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnTotalTag, 10, SpringLayout.WEST, contentPane);
		contentPane.add(rdbtnTotalTag);

		rdbtnNCIS = new JRadioButton("NCIS");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnNCIS, 0, SpringLayout.NORTH, rdbtnTotalTag);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnNCIS, 30, SpringLayout.EAST, rdbtnTotalTag);
		contentPane.add(rdbtnNCIS);

		rdbtnNcisWithTotal = new JRadioButton("NCIS with Total Tag");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnNcisWithTotal, 0, SpringLayout.NORTH, rdbtnTotalTag);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnNcisWithTotal, 30, SpringLayout.EAST, rdbtnNCIS);
		contentPane.add(rdbtnNcisWithTotal);

		ButtonGroup SCALETYPE = new ButtonGroup();
		SCALETYPE.add(rdbtnTotalTag);
		SCALETYPE.add(rdbtnNCIS);
		SCALETYPE.add(rdbtnNcisWithTotal);
		rdbtnTotalTag.setSelected(true);

		lblWindow = new JLabel("Window Size (bp):");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblWindow, 10, SpringLayout.WEST, contentPane);
		lblWindow.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblWindow, 12, SpringLayout.SOUTH, rdbtnTotalTag);
		contentPane.add(lblWindow);

		txtWindow = new JTextField();
		txtWindow.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtWindow, -2, SpringLayout.NORTH, lblWindow);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtWindow, 4, SpringLayout.EAST, lblWindow);
		txtWindow.setHorizontalAlignment(SwingConstants.CENTER);
		txtWindow.setText("500");
		contentPane.add(txtWindow);
		txtWindow.setColumns(10);

		lblMinFraction = new JLabel("Minimum Fraction:");
		lblMinFraction.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtWindow, -10, SpringLayout.WEST, lblMinFraction);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblMinFraction, 0, SpringLayout.NORTH, lblWindow);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblMinFraction, 0, SpringLayout.WEST, rdbtnNcisWithTotal);
		contentPane.add(lblMinFraction);

		txtFraction = new JTextField();
		txtFraction.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtFraction, -2, SpringLayout.NORTH, lblWindow);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtFraction, 6, SpringLayout.EAST, lblMinFraction);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtFraction, 100, SpringLayout.EAST, btnOutput);
		txtFraction.setHorizontalAlignment(SwingConstants.CENTER);
		txtFraction.setText("0.75");
		contentPane.add(txtFraction);
		txtFraction.setColumns(10);

		chckbxOutputStatistics = new JCheckBox("Output Statistics");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxOutputStatistics, 1, SpringLayout.NORTH, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.EAST, chckbxOutputStatistics, 0, SpringLayout.EAST, btnLoad);
		contentPane.add(chckbxOutputStatistics);

		JButton btnLoadBlacklistFilter = new JButton("Load Blacklist Filter");
		btnLoadBlacklistFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File newBlack = FileSelection.getFile(fc, "bed");
				if (newBlack != null) {
					BLACKLIST = newBlack.getAbsoluteFile();
					lblNoBlacklistLoaded.setText(newBlack.getName());
				}
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadBlacklistFilter, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadBlacklistFilter, 0, SpringLayout.WEST, scrollPane);
		contentPane.add(btnLoadBlacklistFilter);

		JLabel lblCurrentBlacklist = new JLabel("Current Blacklist:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentBlacklist, 10, SpringLayout.SOUTH,
				btnLoadBlacklistFilter);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentBlacklist, 0, SpringLayout.WEST, scrollPane);
		contentPane.add(lblCurrentBlacklist);

		lblNoBlacklistLoaded = new JLabel("No Blacklist Loaded");
		lblNoBlacklistLoaded.setFont(new Font("Dialog", Font.PLAIN, 12));
		sl_contentPane.putConstraint(SpringLayout.WEST, lblNoBlacklistLoaded, 0, SpringLayout.WEST, lblDefaultToLocal);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblNoBlacklistLoaded, 0, SpringLayout.SOUTH,
				lblCurrentBlacklist);
		contentPane.add(lblNoBlacklistLoaded);

		btnRemoveBlacklistFilter = new JButton("Remove Blacklist Filter");
		btnRemoveBlacklistFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BLACKLIST = null;
				lblNoBlacklistLoaded.setText("No Blacklist Loaded");
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBlacklistFilter, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBlacklistFilter, 0, SpringLayout.EAST, scrollPane);
		contentPane.add(btnRemoveBlacklistFilter);

		chckbxOutputStatistics.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				activateOutput(chckbxOutputStatistics.isSelected());
			}
		});

		rdbtnTotalTag.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnTotalTag.isSelected()) {
					activateNCISfeatures(false);
				}
			}
		});
		rdbtnNCIS.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnNCIS.isSelected()) {
					activateNCISfeatures(true);
				}
			}
		});
		rdbtnNcisWithTotal.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnNcisWithTotal.isSelected()) {
					activateNCISfeatures(true);
				}
			}
		});
	}

	public void activateNCISfeatures(boolean activate) {
		btnLoadControlBam.setEnabled(activate);
		lblCurrentControl.setEnabled(activate);
		lblNoControlLoaded.setEnabled(activate);
		lblWindow.setEnabled(activate);
		txtWindow.setEnabled(activate);
		lblMinFraction.setEnabled(activate);
		txtFraction.setEnabled(activate);
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
			if (rdbtnTotalTag.isSelected()) {
				activateNCISfeatures(false);
			} else {
				activateNCISfeatures(true);
			}
			if (chckbxOutputStatistics.isSelected()) {
				activateOutput(true);
			} else {
				activateOutput(false);
			}
		}
	}
}