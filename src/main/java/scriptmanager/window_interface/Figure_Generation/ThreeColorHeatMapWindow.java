package scriptmanager.window_interface.Figure_Generation;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Figure_Generation.ThreeColorHeatMap}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Figure_Generation.ThreeColorHeatMap
 * @see scriptmanager.window_interface.Figure_Generation.ThreeColorHeatMapOutput
 */
@SuppressWarnings("serial")
public class ThreeColorHeatMapWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	final DefaultListModel<String> expList;
	ArrayList<File> txtFiles = new ArrayList<File>();

	private JButton btnLoad;
	private JButton btnRemove;
	private JButton btnGen;
	private JProgressBar progressBar;

	/**
	 * Used to run the script efficiently
	 */
	public Task task;
	private JTextField txtRow;
	private JTextField txtCol;

	private JTextField txtAbsoluteMax;
	private JTextField txtAbsoluteMid;
	private JTextField txtAbsoluteMin;

	private JTextField txtPercentMax;
	private JTextField txtPercentMid;
	private JTextField txtPercentMin;

	private JTextField txtHeight;
	private JTextField txtWidth;

	private JButton btnMaxColor;
	private JButton btnMidColor;
	private JButton btnMinColor;
	private JButton btnNanColor;
	private JCheckBox chckbxExcludeZeros;
	private JCheckBox chckbxOutStats;

	private JRadioButton rdbtnMaxAbsoluteValue;
	private JRadioButton rdbtnMidAbsoluteValue;
	private JRadioButton rdbtnMinAbsoluteValue;
	private JRadioButton rdbtnMaxPercentileValue;
	private JRadioButton rdbtnMidPercentileValue;
	private JRadioButton rdbtnMinPercentileValue;

	private JRadioButton rdbtnTreeview;
	private JRadioButton rdbtnBicubic;
	private JRadioButton rdbtnBilinear;
	private JRadioButton rdbtnNearestNeighbor;

	private JCheckBox chckbxOutputHeatmap;
	private JButton btnOutput;
	private JLabel lblOutput;
	private JLabel lblCurrentOutput;

	private File OUT_DIR = null;

	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			try {
				if (txtFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No files loaded!!!");
				} else if (Integer.parseInt(txtRow.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Starting Row!!! Must be greater than 0, 0-based indexing");
				} else if (Integer.parseInt(txtCol.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Starting Column!!! Must be greater than 0, 0-based indexing");
				} else if (Integer.parseInt(txtHeight.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Image Height!!! Must be greater than 0");
				} else if (Integer.parseInt(txtWidth.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Image Width!!! Must be greater than 0");
				} else if (rdbtnMaxAbsoluteValue.isSelected() && Double.parseDouble(txtAbsoluteMax.getText()) <= Double
						.parseDouble(txtAbsoluteMid.getText())) {
					JOptionPane.showMessageDialog(null, "Invalid absolute contrast threshold values entered!!! Max be larger than Mid");
				} else if (rdbtnMinAbsoluteValue.isSelected() && Double.parseDouble(txtAbsoluteMid.getText()) <= Double
						.parseDouble(txtAbsoluteMin.getText())) {
					JOptionPane.showMessageDialog(null, "Invalid absolute contrast threshold values entered!!! Mid be larger than Min");
				} else if (rdbtnMaxPercentileValue.isSelected() && (Double.parseDouble(txtPercentMax.getText()) <= 0
						|| Double.parseDouble(txtPercentMax.getText()) > 1)) {
					JOptionPane.showMessageDialog(null, "Invalid max quantile contrast threshold value entered!!! Must be larger than 0-1");
				} else if (rdbtnMaxPercentileValue.isSelected() && (Double.parseDouble(txtPercentMin.getText()) <= 0
						|| Double.parseDouble(txtPercentMin.getText()) > 1)) {
					JOptionPane.showMessageDialog(null, "Invalid min quantile contrast threshold value entered!!! Must be larger than 0-1");
				} else if (rdbtnMaxPercentileValue.isSelected() && (Double
						.parseDouble(txtPercentMax.getText()) <= Double.parseDouble(txtPercentMin.getText()))) {
					JOptionPane.showMessageDialog(null, "Invalid quantile contrast threshold values entered!!! Max must be larger than Min");
				}
				setProgress(0);
// parameter input checks need to be added to script portion
				Color c_min = btnMinColor.getForeground();
				Color c_mid = btnMidColor.getForeground();
				Color c_max = btnMaxColor.getForeground();
				Color c_nan = btnNanColor.getForeground();
				int startR = Integer.parseInt(txtRow.getText());
				int startC = Integer.parseInt(txtCol.getText());
				int pHeight = Integer.parseInt(txtHeight.getText());
				int pWidth = Integer.parseInt(txtWidth.getText());
				String scaletype = "treeview";
				if (rdbtnBicubic.isSelected()) {
					scaletype = "bicubic";
				} else if (rdbtnBilinear.isSelected()) {
					scaletype = "bilinear";
				} else if (rdbtnNearestNeighbor.isSelected()) {
					scaletype = "neighbor";
				}

				double q_min = Double.parseDouble(txtAbsoluteMin.getText());
				double q_mid = Double.parseDouble(txtAbsoluteMid.getText());
				double q_max = Double.parseDouble(txtAbsoluteMax.getText());
				if (rdbtnMaxPercentileValue.isSelected()) {
					q_min = Double.parseDouble(txtPercentMin.getText());
					q_mid = Double.parseDouble(txtPercentMid.getText());
					q_max = Double.parseDouble(txtPercentMax.getText());
				}
// AAA > 0
// AAP > 1
// APA > 2
// PAA > 3
// APP > 4
// PAP > 5
// PPA > 6
// PPP > 7
				// Execute script
				ThreeColorHeatMapOutput output_obj = new ThreeColorHeatMapOutput(txtFiles, c_max, c_mid, c_min, c_nan, startR,
						startC, pHeight, pWidth, scaletype, rdbtnMinPercentileValue.isSelected(),
						rdbtnMidPercentileValue.isSelected(), rdbtnMaxPercentileValue.isSelected(), q_min, q_mid, q_max,
						chckbxExcludeZeros.isSelected(), OUT_DIR, chckbxOutputHeatmap.isSelected());
				output_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
						int temp = (Integer) propertyChangeEvent.getNewValue();
						int percentComplete = (int) (((double) (temp) / (txtFiles.size())) * 100);
						setProgress(percentComplete);
					}
				});
				output_obj.addPropertyChangeListener("log", new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						firePropertyChange("log", evt.getOldValue(), evt.getNewValue());
					}
				});
				output_obj.setVisible(true);
				output_obj.run();
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!");
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
			setProgress(100);
			return null;
		}

		public void done() {
			massXable(contentPane, true);
			setCursor(null); // turn off the wait cursor
		}
	}

	/**
	 * Creates a new ThreeColorHeatMapWindow
	 */
	public ThreeColorHeatMapWindow() {
		setTitle("Hi-Lo Heatmap Generator");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 550, 720);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);

		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);

		btnLoad = new JButton("Load Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 0, SpringLayout.NORTH, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newFiles = FileSelection.getGenericFiles(fc);
				if (newFiles != null) {
					for (int x = 0; x < newFiles.length; x++) {
						txtFiles.add(newFiles[x]);
						expList.addElement(newFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemove = new JButton("Remove Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemove, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemove, -10, SpringLayout.EAST, contentPane);
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listExp.getSelectedIndex() > -1) {
					txtFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemove);

		btnGen = new JButton("Generate");
		// sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -400,
		// SpringLayout.NORTH, btnGen);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnGen, 167, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnGen, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnGen, -175, SpringLayout.EAST, contentPane);
		contentPane.add(btnGen);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnGen);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -5, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		btnGen.setActionCommand("start");

		JLabel lblInputMatrixParameters = new JLabel("Input Matrix Parameters (0-based)");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblInputMatrixParameters, 10, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblInputMatrixParameters, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblInputMatrixParameters);

		JLabel lblStartingRow = new JLabel("Starting Row:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblStartingRow, 10, SpringLayout.SOUTH,
				lblInputMatrixParameters);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblStartingRow, 25, SpringLayout.WEST, contentPane);
		contentPane.add(lblStartingRow);

		txtRow = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtRow, -2, SpringLayout.NORTH, lblStartingRow);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtRow, 6, SpringLayout.EAST, lblStartingRow);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtRow, 80, SpringLayout.EAST, lblStartingRow);
		txtRow.setHorizontalAlignment(SwingConstants.CENTER);
		txtRow.setText("1");
		contentPane.add(txtRow);
		txtRow.setColumns(10);

		JLabel lblStartingColumn = new JLabel("Starting Column:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblStartingColumn, 10, SpringLayout.SOUTH,
				lblInputMatrixParameters);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblStartingColumn, 10, SpringLayout.EAST, txtRow);
		contentPane.add(lblStartingColumn);

		txtCol = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtCol, -2, SpringLayout.NORTH, lblStartingColumn);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtCol, 6, SpringLayout.EAST, lblStartingColumn);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtCol, 80, SpringLayout.EAST, lblStartingColumn);
		txtCol.setHorizontalAlignment(SwingConstants.CENTER);
		txtCol.setText("2");
		contentPane.add(txtCol);
		txtCol.setColumns(10);

		JSeparator separator_one = new JSeparator();
		separator_one.setForeground(Color.BLACK);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, separator_one, 15, SpringLayout.SOUTH, lblStartingRow);
		sl_contentPane.putConstraint(SpringLayout.EAST, separator_one, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, separator_one, 10, SpringLayout.WEST, contentPane);
		contentPane.add(separator_one);

		JLabel lblSelectColor = new JLabel("Select Colors:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSelectColor, 15, SpringLayout.SOUTH, separator_one);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSelectColor, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblSelectColor);

		btnMinColor = new JButton("Lo Color");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnMinColor, 25, SpringLayout.SOUTH, lblSelectColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnMinColor, 0, SpringLayout.WEST, scrollPane);
		btnMinColor.setForeground(new Color(27, 183, 229, 255)); // Blue from JTree for low vals
		btnMinColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnMinColor.setForeground(
						JColorChooser.showDialog(btnMinColor, "Select a Heatmap Color", btnMinColor.getForeground()));
			}
		});
		contentPane.add(btnMinColor);

		btnMidColor = new JButton("Mid Color");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnMidColor, 30, SpringLayout.SOUTH, btnMinColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnMidColor, 0, SpringLayout.WEST, btnMinColor);
		btnMidColor.setForeground(Color.BLACK);
		btnMidColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnMidColor.setForeground(
						JColorChooser.showDialog(btnMidColor, "Select a Heatmap Color", btnMidColor.getForeground()));
			}
		});
		contentPane.add(btnMidColor);

		btnMaxColor = new JButton("Hi Color");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnMaxColor, 30, SpringLayout.SOUTH, btnMidColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnMaxColor, 0, SpringLayout.WEST, btnMinColor);
		btnMaxColor.setForeground(new Color(254, 255, 0, 255)); // Yellow from JTree for high vals
		btnMaxColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnMaxColor.setForeground(
						JColorChooser.showDialog(btnMaxColor, "Select a Heatmap Color", btnMaxColor.getForeground()));
			}
		});
		contentPane.add(btnMaxColor);

		btnNanColor = new JButton("NaN Color");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnNanColor, 15, SpringLayout.SOUTH, btnMaxColor);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnNanColor, -10, SpringLayout.EAST, contentPane);
		btnNanColor.setForeground(Color.GRAY);
		btnNanColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnNanColor.setForeground(
						JColorChooser.showDialog(btnNanColor, "Select a Heatmap Color", btnNanColor.getForeground()));
			}
		});
		contentPane.add(btnNanColor);

		// Contrast Threshold Objects (Min)
		JLabel lblContrastThreshold = new JLabel("Select Contrast Threshold:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblContrastThreshold, 0, SpringLayout.NORTH, lblSelectColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblContrastThreshold, 60, SpringLayout.EAST, lblSelectColor);
		contentPane.add(lblContrastThreshold);

		rdbtnMinAbsoluteValue = new JRadioButton("Min Absolute Value");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnMinAbsoluteValue, -15, SpringLayout.NORTH, btnMinColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnMinAbsoluteValue, 0, SpringLayout.WEST,
				lblContrastThreshold);
		contentPane.add(rdbtnMinAbsoluteValue);

		txtAbsoluteMin = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtAbsoluteMin, 0, SpringLayout.SOUTH, rdbtnMinAbsoluteValue);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtAbsoluteMin, 10, SpringLayout.WEST, rdbtnMinAbsoluteValue);
		txtAbsoluteMin.setHorizontalAlignment(SwingConstants.CENTER);
		txtAbsoluteMin.setText("-10");
		txtAbsoluteMin.setColumns(10);
		contentPane.add(txtAbsoluteMin);

		rdbtnMinPercentileValue = new JRadioButton("Min Percentile Value");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnMinPercentileValue, -15, SpringLayout.NORTH, btnMinColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnMinPercentileValue, 15, SpringLayout.EAST,
				rdbtnMinAbsoluteValue);
		contentPane.add(rdbtnMinPercentileValue);

		txtPercentMin = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtPercentMin, 0, SpringLayout.SOUTH, rdbtnMinPercentileValue);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtPercentMin, 10, SpringLayout.WEST, rdbtnMinPercentileValue);
		txtPercentMin.setHorizontalAlignment(SwingConstants.CENTER);
		txtPercentMin.setEnabled(false);
		txtPercentMin.setText("0.05");
		txtPercentMin.setColumns(10);
		contentPane.add(txtPercentMin);

		ButtonGroup MinContrast = new ButtonGroup();
		MinContrast.add(rdbtnMinAbsoluteValue);
		MinContrast.add(rdbtnMinPercentileValue);
		rdbtnMinAbsoluteValue.setSelected(true);

		// Contrast Threshold Objects (Mid)
		rdbtnMidAbsoluteValue = new JRadioButton("Mid Absolute Value");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnMidAbsoluteValue, -15, SpringLayout.NORTH, btnMidColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnMidAbsoluteValue, 0, SpringLayout.WEST,
				rdbtnMinAbsoluteValue);
		contentPane.add(rdbtnMidAbsoluteValue);

		txtAbsoluteMid = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtAbsoluteMid, 0, SpringLayout.SOUTH, rdbtnMidAbsoluteValue);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtAbsoluteMid, 10, SpringLayout.WEST, rdbtnMidAbsoluteValue);
		txtAbsoluteMid.setHorizontalAlignment(SwingConstants.CENTER);
		txtAbsoluteMid.setText("0");
		txtAbsoluteMid.setColumns(10);
		contentPane.add(txtAbsoluteMid);

		rdbtnMidPercentileValue = new JRadioButton("Mid Percentile Value");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnMidPercentileValue, -15, SpringLayout.NORTH, btnMidColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnMidPercentileValue, 0, SpringLayout.WEST,
				rdbtnMinPercentileValue);
		contentPane.add(rdbtnMidPercentileValue);

		txtPercentMid = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtPercentMid, 0, SpringLayout.SOUTH, rdbtnMidPercentileValue);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtPercentMid, 10, SpringLayout.WEST, rdbtnMidPercentileValue);
		txtPercentMid.setHorizontalAlignment(SwingConstants.CENTER);
		txtPercentMid.setEnabled(false);
		txtPercentMid.setText("0.5");
		txtPercentMid.setColumns(10);
		contentPane.add(txtPercentMid);

		ButtonGroup MidContrast = new ButtonGroup();
		MidContrast.add(rdbtnMidAbsoluteValue);
		MidContrast.add(rdbtnMidPercentileValue);
		rdbtnMidAbsoluteValue.setSelected(true);

		// Contrast Threshold Objects (Max)
		rdbtnMaxAbsoluteValue = new JRadioButton("Max Absolute Value");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnMaxAbsoluteValue, -15, SpringLayout.NORTH, btnMaxColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnMaxAbsoluteValue, 0, SpringLayout.WEST,
				rdbtnMinAbsoluteValue);
		contentPane.add(rdbtnMaxAbsoluteValue);

		txtAbsoluteMax = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtAbsoluteMax, 0, SpringLayout.SOUTH, rdbtnMaxAbsoluteValue);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtAbsoluteMax, 10, SpringLayout.WEST, rdbtnMaxAbsoluteValue);
		txtAbsoluteMax.setHorizontalAlignment(SwingConstants.CENTER);
		txtAbsoluteMax.setText("10");
		txtAbsoluteMax.setColumns(10);
		contentPane.add(txtAbsoluteMax);

		rdbtnMaxPercentileValue = new JRadioButton("Max Percentile Value");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnMaxPercentileValue, -15, SpringLayout.NORTH, btnMaxColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnMaxPercentileValue, 0, SpringLayout.WEST,
				rdbtnMinPercentileValue);
		contentPane.add(rdbtnMaxPercentileValue);

		txtPercentMax = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtPercentMax, 0, SpringLayout.SOUTH, rdbtnMaxPercentileValue);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtPercentMax, 10, SpringLayout.WEST, rdbtnMaxPercentileValue);
		txtPercentMax.setHorizontalAlignment(SwingConstants.CENTER);
		txtPercentMax.setEnabled(false);
		txtPercentMax.setText("0.95");
		txtPercentMax.setColumns(10);
		contentPane.add(txtPercentMax);

		ButtonGroup MaxContrast = new ButtonGroup();
		MaxContrast.add(rdbtnMaxAbsoluteValue);
		MaxContrast.add(rdbtnMaxPercentileValue);
		rdbtnMaxAbsoluteValue.setSelected(true);

		rdbtnMinAbsoluteValue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnMinAbsoluteValue.isSelected()) {
					activateMinPercent(false);
				} else {
					activateMinPercent(true);
				}
			}
		});
		rdbtnMinPercentileValue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnMinPercentileValue.isSelected()) {
					activateMinPercent(true);
				} else {
					activateMinPercent(false);
				}
			}
		});

		rdbtnMidAbsoluteValue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnMidAbsoluteValue.isSelected()) {
					activateMidPercent(false);
				} else {
					activateMidPercent(true);
				}
			}
		});
		rdbtnMidPercentileValue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnMidPercentileValue.isSelected()) {
					activateMidPercent(true);
				} else {
					activateMidPercent(false);
				}
			}
		});

		rdbtnMaxAbsoluteValue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnMaxAbsoluteValue.isSelected()) {
					activateMaxPercent(false);
				} else {
					activateMaxPercent(true);
				}
			}
		});
		rdbtnMaxPercentileValue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnMaxPercentileValue.isSelected()) {
					activateMaxPercent(true);
				} else {
					activateMaxPercent(false);
				}
			}
		});

		chckbxExcludeZeros = new JCheckBox("Exclude zero values (percentile threshold)");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxExcludeZeros, 0, SpringLayout.NORTH, btnNanColor);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, chckbxExcludeZeros, 0, SpringLayout.SOUTH, btnNanColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxExcludeZeros, 0, SpringLayout.WEST, scrollPane);
		contentPane.add(chckbxExcludeZeros);

		chckbxOutStats = new JCheckBox("Output Min/Max and midpoint cutoff values to text file");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxOutStats, 0, SpringLayout.SOUTH, chckbxExcludeZeros);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxOutStats, 0, SpringLayout.WEST, scrollPane);
		contentPane.add(chckbxOutStats);

		JSeparator separator_two = new JSeparator();
		separator_two.setForeground(Color.BLACK);
		sl_contentPane.putConstraint(SpringLayout.NORTH, separator_two, 10, SpringLayout.SOUTH, chckbxOutStats);
		sl_contentPane.putConstraint(SpringLayout.WEST, separator_two, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, separator_two, -10, SpringLayout.EAST, contentPane);
		contentPane.add(separator_two);

		JLabel lblImageOptions = new JLabel("Image Options");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblImageOptions, 10, SpringLayout.SOUTH, separator_two);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblImageOptions, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblImageOptions);

		JLabel lblPixelHeight = new JLabel("Height in pixels:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblPixelHeight, 10, SpringLayout.SOUTH, lblImageOptions);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblPixelHeight, 0, SpringLayout.WEST, lblStartingRow);
		contentPane.add(lblPixelHeight);

		JLabel lblPixelWidth = new JLabel("Width in pixels:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblPixelWidth, 0, SpringLayout.NORTH, lblPixelHeight);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblPixelWidth, 0, SpringLayout.WEST, lblStartingColumn);
		contentPane.add(lblPixelWidth);

		txtHeight = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtHeight, -2, SpringLayout.NORTH, lblPixelHeight);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtHeight, 10, SpringLayout.EAST, lblPixelHeight);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtHeight, 100, SpringLayout.EAST, lblSelectColor);
		txtHeight.setHorizontalAlignment(SwingConstants.CENTER);
		txtHeight.setText("600");
		contentPane.add(txtHeight);

		txtWidth = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtWidth, -2, SpringLayout.NORTH, lblPixelHeight);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtWidth, 10, SpringLayout.EAST, lblPixelWidth);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtWidth, 100, SpringLayout.EAST, lblPixelWidth);
		txtWidth.setHorizontalAlignment(SwingConstants.CENTER);
		txtWidth.setText("200");
		contentPane.add(txtWidth);

		JLabel lblImageCompression = new JLabel("Compression:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblImageCompression, 10, SpringLayout.SOUTH, lblPixelHeight);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblImageCompression, 0, SpringLayout.WEST, lblStartingRow);
		contentPane.add(lblImageCompression);

		rdbtnTreeview = new JRadioButton("Treeview");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnTreeview, 0, SpringLayout.NORTH, lblImageCompression);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnTreeview, 20, SpringLayout.EAST, lblImageCompression);
		contentPane.add(rdbtnTreeview);

		rdbtnBicubic = new JRadioButton("Bicubic");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnBicubic, 0, SpringLayout.NORTH, rdbtnTreeview);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnBicubic, 6, SpringLayout.EAST, rdbtnTreeview);
		// sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnBicubic, 0,
		// SpringLayout.WEST, txtRow);
		contentPane.add(rdbtnBicubic);

		rdbtnBilinear = new JRadioButton("Bilinear");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnBilinear, 0, SpringLayout.NORTH, rdbtnTreeview);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnBilinear, 6, SpringLayout.EAST, rdbtnBicubic);
		contentPane.add(rdbtnBilinear);

		rdbtnNearestNeighbor = new JRadioButton("Nearest Neighbor");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnNearestNeighbor, 0, SpringLayout.NORTH, rdbtnTreeview);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnNearestNeighbor, 6, SpringLayout.EAST, rdbtnBilinear);
		contentPane.add(rdbtnNearestNeighbor);

		ButtonGroup Scale = new ButtonGroup();
		Scale.add(rdbtnTreeview);
		Scale.add(rdbtnBicubic);
		Scale.add(rdbtnBilinear);
		Scale.add(rdbtnNearestNeighbor);
		rdbtnTreeview.setSelected(true);

		JSeparator separator_three = new JSeparator();
		separator_three.setForeground(Color.BLACK);
		sl_contentPane.putConstraint(SpringLayout.NORTH, separator_three, 10, SpringLayout.SOUTH, rdbtnTreeview);
		sl_contentPane.putConstraint(SpringLayout.WEST, separator_three, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, separator_three, -10, SpringLayout.EAST, contentPane);
		contentPane.add(separator_three);

		chckbxOutputHeatmap = new JCheckBox("Output Heatmap");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxOutputHeatmap, 10, SpringLayout.SOUTH, separator_three);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxOutputHeatmap, 0, SpringLayout.WEST, scrollPane);
		contentPane.add(chckbxOutputHeatmap);
		chckbxOutputHeatmap.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				activateOutput(chckbxOutputHeatmap.isSelected());
			}
		});

		btnOutput = new JButton("Output Directory");
		btnOutput.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 150, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutput, -2, SpringLayout.NORTH, chckbxOutputHeatmap);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -2, SpringLayout.SOUTH, chckbxOutputHeatmap);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -150, SpringLayout.EAST, contentPane);
		contentPane.add(btnOutput);

		lblCurrentOutput = new JLabel("Current Output:");
		lblCurrentOutput.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentOutput, 6, SpringLayout.SOUTH, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentOutput, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblCurrentOutput, 10, SpringLayout.NORTH, btnGen);
		lblCurrentOutput.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrentOutput);

		lblOutput = new JLabel("Default to Local Directory");
		lblOutput.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblOutput, 0, SpringLayout.NORTH, lblCurrentOutput);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblOutput, 0, SpringLayout.SOUTH, lblCurrentOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblOutput, 6, SpringLayout.EAST, lblCurrentOutput);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblOutput, 0, SpringLayout.EAST, contentPane);
		lblOutput.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblOutput.setBackground(Color.BLACK);
		contentPane.add(lblOutput);

		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUT_DIR = FileSelection.getOutputDir(fc);
				if (OUT_DIR != null) {
					lblOutput.setText(OUT_DIR.getAbsolutePath());
				}
			}
		});

		btnGen.addActionListener(this);
	}

	public void activateOutput(boolean activate) {
		btnOutput.setEnabled(activate);
		lblOutput.setEnabled(activate);
		lblCurrentOutput.setEnabled(activate);
	}

	public void activateMinPercent(boolean activate) {
		txtPercentMin.setEnabled(activate);
		txtAbsoluteMin.setEnabled(!activate);
	}

	public void activateMidPercent(boolean activate) {
		txtPercentMid.setEnabled(activate);
		txtAbsoluteMid.setEnabled(!activate);
	}

	public void activateMaxPercent(boolean activate) {
		txtPercentMax.setEnabled(activate);
		txtAbsoluteMax.setEnabled(!activate);
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
	 * Invoked when task's progress property changes and updates the progress bar.
	 */
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
			if (rdbtnMinPercentileValue.isSelected()) {
				activateMinPercent(true);
			}
			if (rdbtnMinAbsoluteValue.isSelected()) {
				activateMinPercent(false);
			}
			if (rdbtnMidPercentileValue.isSelected()) {
				activateMidPercent(true);
			}
			if (rdbtnMidAbsoluteValue.isSelected()) {
				activateMidPercent(false);
			}
			if (rdbtnMaxPercentileValue.isSelected()) {
				activateMaxPercent(true);
			}
			if (rdbtnMaxAbsoluteValue.isSelected()) {
				activateMaxPercent(false);
			}
			if (chckbxOutputHeatmap.isSelected()) {
				activateOutput(true);
			} else {
				activateOutput(false);
			}
		}
	}

}
