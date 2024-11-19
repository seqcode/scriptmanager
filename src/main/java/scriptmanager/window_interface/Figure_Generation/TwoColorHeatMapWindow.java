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
 * {@link scriptmanager.scripts.Figure_Generation.TwoColorHeatMap}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Figure_Generation.TwoColorHeatMap
 * @see scriptmanager.window_interface.Figure_Generation.TwoColorHeatMapOutput
 */
@SuppressWarnings("serial")
public class TwoColorHeatMapWindow extends JFrame implements ActionListener, PropertyChangeListener {
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
	private JTextField txtAbsolute;
	private JTextField txtPercent;
	private JTextField txtHeight;
	private JTextField txtWidth;
	private JButton btnColor;
	private JCheckBox chckbxTransparentBackground;
	private JRadioButton rdbtnAbsoluteValue;
	private JRadioButton rdbtnPercentileValue;
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
					JOptionPane.showMessageDialog(null,
							"Invalid Starting Row!!! Must be greater than 0, 0-based indexing");
				} else if (Integer.parseInt(txtCol.getText()) < 0) {
					JOptionPane.showMessageDialog(null,
							"Invalid Starting Column!!! Must be greater than 0, 0-based indexing");
				} else if (Integer.parseInt(txtHeight.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Image Height!!! Must be greater than 0");
				} else if (Integer.parseInt(txtWidth.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Image Width!!! Must be greater than 0");
				} else if (rdbtnAbsoluteValue.isSelected() && Double.parseDouble(txtAbsolute.getText()) <= 0) {
					JOptionPane.showMessageDialog(null,
							"Invalid absolute contrast threshold value entered!!! Must be larger than 0");
				} else if (rdbtnPercentileValue.isSelected() && (Double.parseDouble(txtPercent.getText()) <= 0
						|| Double.parseDouble(txtPercent.getText()) > 1)) {
					JOptionPane.showMessageDialog(null,
							"Invalid quantile contrast threshold value entered!!! Must be larger than 0-1");
				}
				setProgress(0);

				Color COLOR = btnColor.getForeground();
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
				if (OUT_DIR == null) {
					OUT_DIR = new File(System.getProperty("user.dir"));
				}
				double absolute = Double.parseDouble(txtAbsolute.getText());
				if (rdbtnPercentileValue.isSelected()) {
					absolute = -999;
				}
				double quantile = Double.parseDouble(txtPercent.getText());

				TwoColorHeatMapOutput output_obj = new TwoColorHeatMapOutput(txtFiles, COLOR, startR, startC, pHeight, pWidth,
						scaletype, absolute, quantile, OUT_DIR, chckbxOutputHeatmap.isSelected(), chckbxTransparentBackground.isSelected());
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

	public TwoColorHeatMapWindow() {
		setTitle("Two Color Heatmap Generator");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 600);
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
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -340, SpringLayout.NORTH, btnGen);
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

		JSeparator separator = new JSeparator();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, separator, 15, SpringLayout.SOUTH, lblStartingRow);
		sl_contentPane.putConstraint(SpringLayout.EAST, separator, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, separator, 10, SpringLayout.WEST, contentPane);
		separator.setForeground(Color.BLACK);
		contentPane.add(separator);

		JLabel lblSelectColor = new JLabel("Select Color:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSelectColor, 15, SpringLayout.SOUTH, separator);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSelectColor, 15, SpringLayout.WEST, contentPane);
		contentPane.add(lblSelectColor);

		JRadioButton rdbtnRed = new JRadioButton("Red");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnRed, -4, SpringLayout.NORTH, lblSelectColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnRed, 8, SpringLayout.EAST, lblSelectColor);
		rdbtnRed.setForeground(new Color(255, 0, 0));

		contentPane.add(rdbtnRed);

		JRadioButton rdbtnBlue = new JRadioButton("Blue");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnBlue, 0, SpringLayout.NORTH, rdbtnRed);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnBlue, 8, SpringLayout.EAST, rdbtnRed);
		rdbtnBlue.setForeground(new Color(0, 0, 255));

		contentPane.add(rdbtnBlue);

		JRadioButton rdbtnCustom = new JRadioButton("Custom");
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnCustom, 8, SpringLayout.EAST, rdbtnBlue);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, rdbtnCustom, 0, SpringLayout.SOUTH, rdbtnRed);
		rdbtnCustom.setForeground(new Color(0, 0, 0));
		contentPane.add(rdbtnCustom);

		btnColor = new JButton("Heatmap Color");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnColor, -2, SpringLayout.NORTH, rdbtnCustom);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnColor, 8, SpringLayout.EAST, rdbtnCustom);
		btnColor.setForeground(new Color(0, 0, 0));
		btnColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnColor.setForeground(
						JColorChooser.showDialog(btnColor, "Select a Heatmap Color", btnColor.getForeground()));
				if (btnColor.getForeground().equals(new Color(255, 0, 0))) {
					rdbtnRed.setSelected(true);
				} else if (btnColor.getForeground().equals(new Color(0, 0, 255))) {
					rdbtnBlue.setSelected(true);
				} else {
					rdbtnCustom.setForeground(btnColor.getForeground());
					rdbtnCustom.setSelected(true);
				}

			}
		});
		contentPane.add(btnColor);

		ButtonGroup HeatColor = new ButtonGroup();
		HeatColor.add(rdbtnRed);
		HeatColor.add(rdbtnBlue);
		HeatColor.add(rdbtnCustom);
		rdbtnCustom.setSelected(true);
		rdbtnRed.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnRed.isSelected()) {
					btnColor.setForeground(new Color(255, 0, 0));
				}
			}
		});
		rdbtnBlue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnBlue.isSelected()) {
					btnColor.setForeground(new Color(0, 0, 255));
				}
			}
		});
		rdbtnCustom.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnCustom.isSelected()) {
					btnColor.setForeground(rdbtnCustom.getForeground());
				}
			}
		});

		chckbxTransparentBackground = new JCheckBox("Use transparent background");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxTransparentBackground, 8, SpringLayout.SOUTH, lblSelectColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxTransparentBackground, 10, SpringLayout.WEST, lblSelectColor);
		contentPane.add(chckbxTransparentBackground);
		
		JLabel lblPixelHeight = new JLabel("Image Height:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblPixelHeight, 8, SpringLayout.SOUTH, chckbxTransparentBackground);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblPixelHeight, 15, SpringLayout.WEST, contentPane);
		contentPane.add(lblPixelHeight);

		JLabel lblPixelWidth = new JLabel("Image Width:");
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

		JLabel lblContrastThreshold = new JLabel("Contrast Threshold:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblContrastThreshold, 20, SpringLayout.SOUTH, lblPixelHeight);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblContrastThreshold, 15, SpringLayout.WEST, contentPane);
		contentPane.add(lblContrastThreshold);

		rdbtnAbsoluteValue = new JRadioButton("Absolute Value");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnAbsoluteValue, -5, SpringLayout.NORTH,
				lblContrastThreshold);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnAbsoluteValue, 10, SpringLayout.EAST,
				lblContrastThreshold);
		contentPane.add(rdbtnAbsoluteValue);

		rdbtnPercentileValue = new JRadioButton("Percentile Value");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnPercentileValue, 0, SpringLayout.NORTH,
				rdbtnAbsoluteValue);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnPercentileValue, 6, SpringLayout.EAST, rdbtnAbsoluteValue);
		contentPane.add(rdbtnPercentileValue);

		ButtonGroup Contrast = new ButtonGroup();
		Contrast.add(rdbtnAbsoluteValue);
		Contrast.add(rdbtnPercentileValue);
		rdbtnAbsoluteValue.setSelected(true);

		txtAbsolute = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtAbsolute, 6, SpringLayout.SOUTH, rdbtnAbsoluteValue);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtAbsolute, 20, SpringLayout.WEST, rdbtnAbsoluteValue);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtAbsolute, -20, SpringLayout.EAST, rdbtnAbsoluteValue);
		txtAbsolute.setHorizontalAlignment(SwingConstants.CENTER);
		txtAbsolute.setText("10");
		contentPane.add(txtAbsolute);
		txtAbsolute.setColumns(10);

		txtPercent = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.WEST, txtPercent, 20, SpringLayout.WEST, rdbtnPercentileValue);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtPercent, -20, SpringLayout.EAST, rdbtnPercentileValue);
		txtPercent.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtPercent, 6, SpringLayout.SOUTH, rdbtnPercentileValue);
		txtPercent.setHorizontalAlignment(SwingConstants.CENTER);
		txtPercent.setText("0.95");
		contentPane.add(txtPercent);
		txtPercent.setColumns(10);

		rdbtnAbsoluteValue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnAbsoluteValue.isSelected()) {
					txtAbsolute.setEnabled(true);
					txtPercent.setEnabled(false);
				} else {
					txtPercent.setEnabled(true);
					txtAbsolute.setEnabled(false);
				}
			}
		});
		rdbtnPercentileValue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnPercentileValue.isSelected()) {
					txtPercent.setEnabled(true);
					txtAbsolute.setEnabled(false);
				} else {
					txtAbsolute.setEnabled(true);
					txtPercent.setEnabled(false);
				}
			}
		});

		JLabel lblImageCompression = new JLabel("Image Compression:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblImageCompression, 5, SpringLayout.SOUTH, txtAbsolute);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblImageCompression, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblImageCompression);

		rdbtnTreeview = new JRadioButton("Treeview");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnTreeview, 6, SpringLayout.SOUTH, lblImageCompression);
		contentPane.add(rdbtnTreeview);

		rdbtnBicubic = new JRadioButton("Bicubic");
		sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnTreeview, -6, SpringLayout.WEST, rdbtnBicubic);
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnBicubic, 0, SpringLayout.NORTH, rdbtnTreeview);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnBicubic, 0, SpringLayout.WEST, txtRow);
		contentPane.add(rdbtnBicubic);

		rdbtnBilinear = new JRadioButton("Bilinear");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnBilinear, 0, SpringLayout.NORTH, rdbtnTreeview);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnBilinear, 6, SpringLayout.EAST, rdbtnBicubic);
		contentPane.add(rdbtnBilinear);

		rdbtnNearestNeighbor = new JRadioButton("Nearest Neighbor");
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnNearestNeighbor, 6, SpringLayout.EAST, rdbtnBilinear);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, rdbtnNearestNeighbor, 0, SpringLayout.SOUTH, rdbtnTreeview);
		contentPane.add(rdbtnNearestNeighbor);

		ButtonGroup Scale = new ButtonGroup();
		Scale.add(rdbtnTreeview);
		Scale.add(rdbtnBicubic);
		Scale.add(rdbtnBilinear);
		Scale.add(rdbtnNearestNeighbor);
		rdbtnTreeview.setSelected(true);

		btnOutput = new JButton("Output Directory");
		btnOutput.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 150, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -45, SpringLayout.NORTH, btnGen);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -150, SpringLayout.EAST, contentPane);
		contentPane.add(btnOutput);

		JSeparator separator_1 = new JSeparator();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, separator_1, 10, SpringLayout.SOUTH, rdbtnTreeview);
		separator_1.setForeground(Color.BLACK);
		sl_contentPane.putConstraint(SpringLayout.WEST, separator_1, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, separator_1, -10, SpringLayout.EAST, contentPane);
		contentPane.add(separator_1);

		lblCurrentOutput = new JLabel("Current Output:");
		lblCurrentOutput.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentOutput, 15, SpringLayout.SOUTH, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentOutput, 10, SpringLayout.WEST, contentPane);
		lblCurrentOutput.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrentOutput);

		lblOutput = new JLabel("Default to Local Directory");
		lblOutput.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblOutput, 0, SpringLayout.NORTH, lblCurrentOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblOutput, 6, SpringLayout.EAST, lblCurrentOutput);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblOutput, 0, SpringLayout.EAST, contentPane);
		lblOutput.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblOutput.setBackground(Color.WHITE);
		contentPane.add(lblOutput);

		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUT_DIR = FileSelection.getOutputDir(fc);
				if (OUT_DIR != null) {
					lblOutput.setText(OUT_DIR.getAbsolutePath());
				}
			}
		});

		chckbxOutputHeatmap = new JCheckBox("Output Heatmap");
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxOutputHeatmap, 0, SpringLayout.WEST, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, chckbxOutputHeatmap, 0, SpringLayout.SOUTH, btnOutput);
		contentPane.add(chckbxOutputHeatmap);
		chckbxOutputHeatmap.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				activateOutput(chckbxOutputHeatmap.isSelected());
			}
		});

		btnGen.addActionListener(this);
	}

	/**
	 * Activates the appropriate settings for outputting a PNG
	 * @param activate Whether a heat map PNG should be output
	 */
	public void activateOutput(boolean activate) {
		btnOutput.setEnabled(activate);
		lblOutput.setEnabled(activate);
		lblCurrentOutput.setEnabled(activate);
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
			if (rdbtnPercentileValue.isSelected()) {
				txtPercent.setEnabled(true);
				txtAbsolute.setEnabled(false);
			}
			if (rdbtnAbsoluteValue.isSelected()) {
				txtAbsolute.setEnabled(true);
				txtPercent.setEnabled(false);
			}
			if (chckbxOutputHeatmap.isSelected()) {
				activateOutput(true);
			} else {
				activateOutput(false);
			}
		}
	}

}
