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
 * {@link scriptmanager.scripts.Figure_Generation.LabelHeatMap}
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.Figure_Generation.LabelHeatMap
 * @see scriptmanager.window_interface.Figure_Generation.LabelHeatMapOutput
 */
@SuppressWarnings("serial")
public class LabelHeatMapWindow extends JFrame implements ActionListener, PropertyChangeListener {
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
	private JTextField txtBorderWidth;
	private JTextField txtXTickHeight;
	private JTextField txtLeftLabel;
	private JTextField txtMidLabel;
	private JTextField txtRightLabel;
	private JTextField txtXLabel;
	private JTextField txtYLabel;
	private JTextField txtFontSize;
	private JButton btnColor;

	private JButton btnOutput;
	private JLabel lblOutput;
	private JLabel lblCurrentOutput;

	private File OUT_DIR = new File(System.getProperty("user.dir"));

	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			try {
				setProgress(0);
				// Parse inputs from window fields
				Color color = btnColor.getForeground();
				int borderWidth = Integer.parseInt(txtBorderWidth.getText());
				int xTickHeight = Integer.parseInt(txtXTickHeight.getText());
				String llabel = txtLeftLabel.getText();
				String mlabel = txtMidLabel.getText();
				String rlabel = txtRightLabel.getText();
				String xlabel = txtXLabel.getText();
				String ylabel = txtYLabel.getText();
				int fontSize = Integer.parseInt(txtFontSize.getText());
				// Make script object and run
				LabelHeatMapOutput output_obj = new LabelHeatMapOutput(txtFiles, OUT_DIR, color,
						borderWidth, xTickHeight, fontSize,
						llabel, mlabel, rlabel,
						xlabel, ylabel);
				output_obj.run();
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
				// Update progress
				setProgress(100);
				JOptionPane.showMessageDialog(null, "Generation Complete");
			}
			catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!");
			} catch (OptionException oe) {
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
	 * Creates a new LabelHeatMapWindow
	 */
	public LabelHeatMapWindow() {
		setTitle("Labeled Heatmap Generator");
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
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -325, SpringLayout.NORTH, btnGen);
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

		JLabel lblAxisLineFormat = new JLabel("Axis Line formatting");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblAxisLineFormat, 10, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblAxisLineFormat, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblAxisLineFormat);

		JLabel lblBorderWidth = new JLabel("Border Width:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblBorderWidth, 10, SpringLayout.SOUTH,
				lblAxisLineFormat);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblBorderWidth, 25, SpringLayout.WEST, contentPane);
		contentPane.add(lblBorderWidth);

		txtBorderWidth = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtBorderWidth, -2, SpringLayout.NORTH, lblBorderWidth);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtBorderWidth, 6, SpringLayout.EAST, lblBorderWidth);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtBorderWidth, 80, SpringLayout.EAST, lblBorderWidth);
		txtBorderWidth.setHorizontalAlignment(SwingConstants.CENTER);
		txtBorderWidth.setText("2");
		contentPane.add(txtBorderWidth);
		txtBorderWidth.setColumns(10);

		JLabel lblXTickHeight = new JLabel("X-tick height:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblXTickHeight, 10, SpringLayout.SOUTH,
				lblAxisLineFormat);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblXTickHeight, 10, SpringLayout.EAST, txtBorderWidth);
		contentPane.add(lblXTickHeight);

		txtXTickHeight = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtXTickHeight, -2, SpringLayout.NORTH, lblXTickHeight);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtXTickHeight, 6, SpringLayout.EAST, lblXTickHeight);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtXTickHeight, 80, SpringLayout.EAST, lblXTickHeight);
		txtXTickHeight.setHorizontalAlignment(SwingConstants.CENTER);
		txtXTickHeight.setText("10");
		contentPane.add(txtXTickHeight);
		txtXTickHeight.setColumns(10);

		JLabel lblSelectColor = new JLabel("Select Color:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSelectColor, 20, SpringLayout.SOUTH, lblBorderWidth);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSelectColor, 0, SpringLayout.WEST, lblBorderWidth);
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

		btnColor = new JButton("Label Color");
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

		ButtonGroup LabelColor = new ButtonGroup();
		LabelColor.add(rdbtnRed);
		LabelColor.add(rdbtnBlue);
		LabelColor.add(rdbtnCustom);
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


		JSeparator separator1 = new JSeparator();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, separator1, 15, SpringLayout.SOUTH, lblSelectColor);
		sl_contentPane.putConstraint(SpringLayout.EAST, separator1, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, separator1, 10, SpringLayout.WEST, contentPane);
		separator1.setForeground(Color.BLACK);
		contentPane.add(separator1);

		JLabel lblLabelFormat = new JLabel("Label formatting");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblLabelFormat, 10, SpringLayout.SOUTH, separator1);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblLabelFormat, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblLabelFormat);

		JLabel lblFontSize = new JLabel("Font Size:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblFontSize, 30, SpringLayout.SOUTH, lblLabelFormat);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblFontSize, 0, SpringLayout.WEST, lblSelectColor);
		contentPane.add(lblFontSize);

		txtFontSize = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtFontSize, -2, SpringLayout.NORTH, lblFontSize);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtFontSize, 10, SpringLayout.EAST, lblFontSize);
//		txtFontSize.setHorizontalAlignment(SwingConstants.CENTER);
		txtFontSize.setText("18");
		txtFontSize.setColumns(5);
		contentPane.add(txtFontSize);

		JLabel lblLeftLabel = new JLabel("Left label:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblLeftLabel, 5, SpringLayout.SOUTH, lblLabelFormat);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblLeftLabel, 50, SpringLayout.EAST, lblLabelFormat);
		contentPane.add(lblLeftLabel);

		txtLeftLabel = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtLeftLabel, 0, SpringLayout.NORTH, lblLeftLabel);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtLeftLabel, 10, SpringLayout.EAST, lblLeftLabel);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtLeftLabel, -25, SpringLayout.EAST, contentPane);
		txtLeftLabel.setHorizontalAlignment(SwingConstants.CENTER);
		txtLeftLabel.setText("");
		contentPane.add(txtLeftLabel);
		txtLeftLabel.setColumns(15);

		JLabel lblMidLabel = new JLabel("Mid label:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblMidLabel, 10, SpringLayout.SOUTH, lblLeftLabel);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblMidLabel, 0, SpringLayout.WEST, lblLeftLabel);
		lblMidLabel.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblMidLabel);

		txtMidLabel = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtMidLabel, 0, SpringLayout.NORTH, lblMidLabel);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtMidLabel, 10, SpringLayout.EAST, lblMidLabel);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtMidLabel, -25, SpringLayout.EAST, contentPane);
		txtMidLabel.setHorizontalAlignment(SwingConstants.CENTER);
		txtMidLabel.setText("");
		contentPane.add(txtMidLabel);
		txtMidLabel.setColumns(15);
		
		JLabel lblRightLabel = new JLabel("Right label:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblRightLabel, 10, SpringLayout.SOUTH, lblMidLabel);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblRightLabel, 0, SpringLayout.WEST, lblLeftLabel);
		contentPane.add(lblRightLabel);

		txtRightLabel = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtRightLabel, 0, SpringLayout.NORTH, lblRightLabel);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtRightLabel, 10, SpringLayout.EAST, lblRightLabel);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtRightLabel, -25, SpringLayout.EAST, contentPane);
		txtRightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		txtRightLabel.setText("");
		contentPane.add(txtRightLabel);
		txtRightLabel.setColumns(15);

		
		JLabel lblXLabel = new JLabel("X-axis Label:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblXLabel, 50, SpringLayout.SOUTH, txtFontSize);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblXLabel, 0, SpringLayout.WEST, lblSelectColor);
		contentPane.add(lblXLabel);

		txtXLabel = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtXLabel, -2, SpringLayout.NORTH, lblXLabel);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtXLabel, 10, SpringLayout.EAST, lblXLabel);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtXLabel, -25, SpringLayout.EAST, contentPane);
		txtXLabel.setHorizontalAlignment(SwingConstants.CENTER);
		txtXLabel.setText("");
		contentPane.add(txtXLabel);

		JLabel lblYLabel = new JLabel("Y-axis Label:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblYLabel, 10, SpringLayout.SOUTH, lblXLabel);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblYLabel, 0, SpringLayout.WEST, lblSelectColor);
		contentPane.add(lblYLabel);

		txtYLabel = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtYLabel, -2, SpringLayout.NORTH, lblYLabel);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtYLabel, 10, SpringLayout.EAST, lblYLabel);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtYLabel, -25, SpringLayout.EAST, contentPane);
		txtYLabel.setHorizontalAlignment(SwingConstants.CENTER);
		txtYLabel.setText("");
		contentPane.add(txtYLabel);

//		JSeparator separator2 = new JSeparator();
//		sl_contentPane.putConstraint(SpringLayout.NORTH, separator2, 10, SpringLayout.SOUTH, lblYLabel);
//		sl_contentPane.putConstraint(SpringLayout.WEST, separator2, 10, SpringLayout.WEST, contentPane);
//		sl_contentPane.putConstraint(SpringLayout.EAST, separator2, -10, SpringLayout.EAST, contentPane);
//		separator2.setForeground(Color.BLACK);
//		contentPane.add(separator2);

		btnOutput = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 150, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -30, SpringLayout.NORTH, btnGen);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -150, SpringLayout.EAST, contentPane);
		contentPane.add(btnOutput);

		lblCurrentOutput = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentOutput, 5, SpringLayout.SOUTH, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentOutput, 0, SpringLayout.WEST, lblAxisLineFormat);
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

		btnGen.addActionListener(this);
	}

	
	/**
	 * Deprecated method for displaying output
	 * @param activate 
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
	 * Invoked when task's progress property changes.
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
//		if (status) {
//			if (chckbxOutputHeatmap.isSelected()) {
//				activateOutput(true);
//			} else {
//				activateOutput(false);
//			}
//		}
	}

}
