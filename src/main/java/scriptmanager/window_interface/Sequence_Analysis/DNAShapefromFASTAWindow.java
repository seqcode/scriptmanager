package scriptmanager.window_interface.Sequence_Analysis;

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

import javax.swing.BorderFactory;
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
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.objects.Exceptions.ScriptManagerException;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;

import scriptmanager.scripts.Sequence_Analysis.DNAShapefromBED;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Sequence_Analysis.DNAShapefromFASTA}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Sequence_Analysis.DNAShapefromFASTA
 * @see scriptmanager.window_interface.Sequence_Analysis.DNAShapefromFASTAOutput
 */
@SuppressWarnings("serial")
public class DNAShapefromFASTAWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	final DefaultListModel<String> expList;
	ArrayList<File> FASTAFiles = new ArrayList<File>();
	private File OUT_DIR = null;

	private JButton btnLoad;
	private JButton btnRemoveFasta;
	private JButton btnCalculate;

	private JLabel lblDefaultToLocal;
	private JLabel lblCurrent;
	private JProgressBar progressBar;

	private JToggleButton tglAll;
	private JCheckBox chckbxMinorGrooveWidth;
	private JCheckBox chckbxRoll;
	private JCheckBox chckbxHelicalTwist;
	private JCheckBox chckbxPropellerTwist;

	private JToggleButton tglTab;
	private JToggleButton tglCdt;

	private JCheckBox chckbxOutputMatrixData;
	private JCheckBox chckbxOutputCompositeData;
	private JCheckBox chckbxOutputGzip;

	private JButton btnOutputDirectory;

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
				if (FASTAFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No FASTA Files Loaded!!!");
				} else if (!chckbxMinorGrooveWidth.isSelected() && !chckbxRoll.isSelected()
						&& !chckbxHelicalTwist.isSelected() && !chckbxPropellerTwist.isSelected()) {
					JOptionPane.showMessageDialog(null, "No Structural Predictions Selected!!!");
				} else {
					setProgress(0);
					boolean[] OUTPUT_TYPE = new boolean[4];
					OUTPUT_TYPE[0] = chckbxMinorGrooveWidth.isSelected();
					OUTPUT_TYPE[1] = chckbxPropellerTwist.isSelected();
					OUTPUT_TYPE[2] = chckbxHelicalTwist.isSelected();
					OUTPUT_TYPE[3] = chckbxRoll.isSelected();

					short outputMatrix = 0;
					if (chckbxOutputMatrixData.isSelected()) {
						if (tglTab.isSelected()) {
							outputMatrix = DNAShapefromBED.TAB;
						} else if (tglCdt.isSelected()) {
							outputMatrix = DNAShapefromBED.CDT;
						}
					}
					// Execute script
					DNAShapefromFASTAOutput output_obj = new DNAShapefromFASTAOutput(FASTAFiles,
							OUT_DIR, OUTPUT_TYPE, chckbxOutputCompositeData.isSelected(), outputMatrix, chckbxOutputGzip.isSelected());
					output_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
							int temp = (Integer) propertyChangeEvent.getNewValue();
							int percentComplete = (int) (((double) (temp) / FASTAFiles.size()) * 100);
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
				}
			} catch (ScriptManagerException sme) {
				JOptionPane.showMessageDialog(null, sme.getMessage());
			} catch (OptionException oe) {
				JOptionPane.showMessageDialog(null, oe.getMessage());
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!");
			} catch (InterruptedException ie) {
				ie.printStackTrace();
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
	public DNAShapefromFASTAWindow() {
		setTitle("DNA Shape Predictions from FASTA");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 475, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 48, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);

		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);

		btnLoad = new JButton("Load FASTA Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 10, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newFASTAFiles = FileSelection.getFiles(fc, "fa", true);
				if (newFASTAFiles != null) {
					for (int x = 0; x < newFASTAFiles.length; x++) {
						FASTAFiles.add(newFASTAFiles[x]);
						expList.addElement(newFASTAFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemoveFasta = new JButton("Remove FASTA");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveFasta, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveFasta, -10, SpringLayout.EAST, contentPane);
		btnRemoveFasta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listExp.getSelectedIndex() > -1) {
					FASTAFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemoveFasta);

		btnCalculate = new JButton("Calculate");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -200, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnCalculate, -5, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCalculate, 165, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCalculate, -165, SpringLayout.EAST, contentPane);
		contentPane.add(btnCalculate);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, progressBar, -10, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		btnCalculate.setActionCommand("start");
		btnCalculate.addActionListener(this);

		// Shape Parameters
		JPanel pnlShapeOptions = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlShapeOptions, -230, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlShapeOptions, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlShapeOptions, 0, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlShapeOptions, -160, SpringLayout.SOUTH, contentPane);
		contentPane.add(pnlShapeOptions);

		SpringLayout sl_ShapeOptions = new SpringLayout();
		pnlShapeOptions.setLayout(sl_ShapeOptions);
		TitledBorder ttlShapeOptions = BorderFactory.createTitledBorder("Shape Options");
		ttlShapeOptions.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlShapeOptions.setBorder(ttlShapeOptions);

		tglAll = new JToggleButton("Select All");
		sl_ShapeOptions.putConstraint(SpringLayout.NORTH, tglAll, 0, SpringLayout.NORTH, pnlShapeOptions);
		sl_ShapeOptions.putConstraint(SpringLayout.WEST, tglAll, 10, SpringLayout.WEST, pnlShapeOptions);
		sl_ShapeOptions.putConstraint(SpringLayout.EAST, tglAll, 130, SpringLayout.WEST, pnlShapeOptions);
		tglAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chckbxMinorGrooveWidth.setSelected(tglAll.isSelected());
				chckbxRoll.setSelected(tglAll.isSelected());
				chckbxHelicalTwist.setSelected(tglAll.isSelected());
				chckbxPropellerTwist.setSelected(tglAll.isSelected());
				tglAll.setText(tglAll.isSelected() ? "Deselect All" : "Select All");
			}
		});
		pnlShapeOptions.add(tglAll);

		chckbxMinorGrooveWidth = new JCheckBox("Minor Groove Width");
		sl_ShapeOptions.putConstraint(SpringLayout.WEST, chckbxMinorGrooveWidth, 30, SpringLayout.EAST, tglAll);
		chckbxMinorGrooveWidth.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!chckbxMinorGrooveWidth.isSelected()) {
					tglAll.setSelected(false);
					tglAll.setText("Select All");
				} else {
					if (chckbxRoll.isSelected() && chckbxHelicalTwist.isSelected() && chckbxPropellerTwist.isSelected()) {
						tglAll.setSelected(true);
						tglAll.setText("Deselect All");
					}
				}
			}
		});
		pnlShapeOptions.add(chckbxMinorGrooveWidth);

		chckbxRoll = new JCheckBox("Roll");
		sl_ShapeOptions.putConstraint(SpringLayout.NORTH, chckbxRoll, 0, SpringLayout.SOUTH, chckbxMinorGrooveWidth);
		sl_ShapeOptions.putConstraint(SpringLayout.WEST, chckbxRoll, 0, SpringLayout.WEST, chckbxMinorGrooveWidth);
		chckbxRoll.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!chckbxRoll.isSelected()) {
					tglAll.setSelected(false);
					tglAll.setText("Select All");
				} else {
					if (chckbxMinorGrooveWidth.isSelected() && chckbxHelicalTwist.isSelected() && chckbxPropellerTwist.isSelected()) {
						tglAll.setSelected(true);
						tglAll.setText("Deselect All");
					}
				}
			}
		});
		pnlShapeOptions.add(chckbxRoll);

		chckbxHelicalTwist = new JCheckBox("Helical Twist");
		sl_ShapeOptions.putConstraint(SpringLayout.NORTH, chckbxHelicalTwist, 0, SpringLayout.NORTH, chckbxMinorGrooveWidth);
		sl_ShapeOptions.putConstraint(SpringLayout.WEST, chckbxHelicalTwist, 10, SpringLayout.EAST, chckbxMinorGrooveWidth);
		chckbxHelicalTwist.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!chckbxHelicalTwist.isSelected()) {
					tglAll.setSelected(false);
					tglAll.setText("Select All");
				} else {
					if (chckbxMinorGrooveWidth.isSelected() && chckbxRoll.isSelected() && chckbxPropellerTwist.isSelected()) {
						tglAll.setSelected(true);
						tglAll.setText("Deselect All");
					}
				}
			}
		});
		pnlShapeOptions.add(chckbxHelicalTwist);

		chckbxPropellerTwist = new JCheckBox("Propeller Twist");
		sl_ShapeOptions.putConstraint(SpringLayout.NORTH, chckbxPropellerTwist, 0, SpringLayout.NORTH, chckbxRoll);
		sl_ShapeOptions.putConstraint(SpringLayout.WEST, chckbxPropellerTwist, 0, SpringLayout.WEST, chckbxHelicalTwist);
		chckbxPropellerTwist.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!chckbxPropellerTwist.isSelected()) {
					tglAll.setSelected(false);
					tglAll.setText("Select All");
				} else {
					if (chckbxMinorGrooveWidth.isSelected() && chckbxRoll.isSelected() && chckbxHelicalTwist.isSelected()) {
						tglAll.setSelected(true);
						tglAll.setText("Deselect All");
					}
				}
			}
		});
		pnlShapeOptions.add(chckbxPropellerTwist);


		// Output Parameters
		JPanel pnlOutputOptions = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlOutputOptions, -155, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlOutputOptions, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlOutputOptions, 0, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlOutputOptions, -40, SpringLayout.SOUTH, contentPane);
		contentPane.add(pnlOutputOptions);

		SpringLayout sl_OutputOptions = new SpringLayout();
		pnlOutputOptions.setLayout(sl_OutputOptions);
		TitledBorder ttlOutputOptions = BorderFactory.createTitledBorder("Output Options");
		ttlOutputOptions.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlOutputOptions.setBorder(ttlOutputOptions);

		chckbxOutputMatrixData = new JCheckBox("Output Heatmap Matrix");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, chckbxOutputMatrixData, 6, SpringLayout.NORTH, pnlOutputOptions);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, chckbxOutputMatrixData, 0, SpringLayout.WEST, pnlOutputOptions);
		pnlOutputOptions.add(chckbxOutputMatrixData);

		tglCdt = new JToggleButton("CDT");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, tglCdt, -2, SpringLayout.NORTH, chckbxOutputMatrixData);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, tglCdt, 6, SpringLayout.EAST, chckbxOutputMatrixData);
		pnlOutputOptions.add(tglCdt);

		tglTab = new JToggleButton("TAB");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, tglTab, 0, SpringLayout.NORTH, tglCdt);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, tglTab, 0, SpringLayout.EAST, tglCdt);
		pnlOutputOptions.add(tglTab);

		ButtonGroup output = new ButtonGroup();
		output.add(tglTab);
		output.add(tglCdt);
		tglCdt.setSelected(true);

		chckbxOutputGzip = new JCheckBox("Output GZIP");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, chckbxOutputGzip, 0, SpringLayout.NORTH, chckbxOutputMatrixData);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, chckbxOutputGzip, 6, SpringLayout.EAST, tglTab);
		pnlOutputOptions.add(chckbxOutputGzip);

		chckbxOutputCompositeData = new JCheckBox("Output Composite");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, chckbxOutputCompositeData, 10, SpringLayout.SOUTH, chckbxOutputMatrixData);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, chckbxOutputCompositeData, 0, SpringLayout.WEST, chckbxOutputMatrixData);
		pnlOutputOptions.add(chckbxOutputCompositeData);

		btnOutputDirectory = new JButton("Output Directory");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 10, SpringLayout.SOUTH, chckbxOutputMatrixData);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, btnOutputDirectory, 90, SpringLayout.EAST, chckbxOutputCompositeData);
		pnlOutputOptions.add(btnOutputDirectory);

		lblCurrent = new JLabel("Current Output:");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, lblCurrent, 10, SpringLayout.SOUTH, btnOutputDirectory);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, lblCurrent, 0, SpringLayout.WEST, pnlOutputOptions);
		lblCurrent.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		pnlOutputOptions.add(lblCurrent);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 2, SpringLayout.NORTH, lblCurrent);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrent);
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblDefaultToLocal.setBackground(Color.WHITE);
		lblDefaultToLocal.setToolTipText("Directory path");
		pnlOutputOptions.add(lblDefaultToLocal);

		chckbxOutputMatrixData.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				activateOutput();
			}
		});

		chckbxOutputCompositeData.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				activateOutput();
			}
		});

		btnOutputDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File temp = FileSelection.getOutputDir(fc);
				if(temp != null) {
					OUT_DIR = temp;
					lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
					lblDefaultToLocal.setToolTipText(OUT_DIR.getAbsolutePath());
				}
			}
		});
		activateOutput();
	}

	public void activateOutput() {
		boolean enableMatrixOptions = chckbxOutputMatrixData.isSelected();
		tglTab.setEnabled(enableMatrixOptions);
		tglCdt.setEnabled(enableMatrixOptions);
		chckbxOutputGzip.setEnabled(enableMatrixOptions);
		boolean enableOutputOptions = chckbxOutputMatrixData.isSelected() || chckbxOutputCompositeData.isSelected();
		btnOutputDirectory.setEnabled(enableOutputOptions);
		lblCurrent.setEnabled(enableOutputOptions);
		lblDefaultToLocal.setEnabled(enableOutputOptions);
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
	}
}