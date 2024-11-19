package scriptmanager.window_interface.Peak_Analysis;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
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
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.border.TitledBorder;

import htsjdk.samtools.SAMException;
import scriptmanager.objects.PileupParameters;
import scriptmanager.objects.ReadFragmentCartoon;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Peak_Analysis.FRiXCalculator}
 * 
 * @author William KM Lai
 * @see scriptmanager.window_interface.Peak_Analysis.FRiXCalculatorOutput
 */
@SuppressWarnings("serial")
public class FRiXCalculatorWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	final DefaultListModel<String> expList;
	Vector<File> BAMFiles = new Vector<File>();
	final DefaultListModel<String> bedList;
	Vector<File> BEDFiles = new Vector<File>();
	private File OUT_DIR = new File(System.getProperty("user.dir"));

	private JButton btnCalculate;
	private JButton btnLoadBamFiles;
	private JButton btnRemoveBam;
	private JButton btnOutputDirectory;
	private JComboBox<String> cbox_ReadAspect;
	private JComboBox<String> cbox_ReadOutput;

	private JLabel lblDefaultToLocal;
	private JLabel lblCpusToUse;
	private JTextField txtMin;
	private JTextField txtMax;
	private JTextField txtShift;
	private JTextField txtCPU;
	private JCheckBox chckbxOutputData;
	private JCheckBox chckbxRequireProperPe;
	private JCheckBox chckbxFilterByMin;
	private JCheckBox chckbxFilterByMax;
	
	private ReadFragmentCartoon readCartoon;

	// Names of fields indexed by PileupParameters constants
	private String[] readAspectOptions = {"5' End", "3' End", "Midpoint"};
	private String[] readOutputOptions = {"Read 1", "Read 2", "All Reads"};

	JProgressBar progressBar;
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
				if (Integer.parseInt(txtCPU.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Number of CPU's!!! Must use at least 1", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else if (chckbxFilterByMin.isSelected() && Integer.parseInt(txtMin.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Minimum Insert Size!!! Must be greater than or equal to 0 bp", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else if (chckbxFilterByMax.isSelected() && Integer.parseInt(txtMax.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Maximum Insert Size!!! Must be greater than or equal to 0 bp", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else if (chckbxFilterByMin.isSelected() && chckbxFilterByMax.isSelected() && Integer.parseInt(txtMax.getText()) < Integer.parseInt(txtMin.getText())) {
					JOptionPane.showMessageDialog(null, "Invalid Maximum & Minimum Insert Sizes!!! Maximum must be larger/equal to Minimum!", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else if (BEDFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No BED Files Loaded!!!", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else if (BAMFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No BAM Files Loaded!!!", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else {
					setProgress(0);
					// Load up parameters for the pileup into single object
					PileupParameters param = new PileupParameters();
					ArrayList<Color> colors = new ArrayList<Color>();

					// Set Read encoding values from combo box selections
					param.setAspect(cbox_ReadAspect.getSelectedIndex());
					param.setRead(cbox_ReadOutput.getSelectedIndex());

					// Set Read Filter requirements
					if (chckbxRequireProperPe.isSelected()) { param.setPErequire(true); }
					else { param.setPErequire(false); }

					if (chckbxFilterByMin.isSelected()) { param.setMinInsert(Integer.parseInt(txtMin.getText())); }
					if (chckbxFilterByMax.isSelected()) { param.setMaxInsert(Integer.parseInt(txtMax.getText())); }

					// Set output options according to selections
					if (!chckbxOutputData.isSelected()) { param.setOutputDirectory(null); }
					else { param.setOutputDirectory(OUT_DIR); }

					param.setOutputCompositeStatus(chckbxOutputData.isSelected()); //Outputs composite plots if check box is selected

					// SHIFT can be negative
					param.setShift(Integer.parseInt(txtShift.getText()));
					param.setCPU(Integer.parseInt(txtCPU.getText()));

					// Execute script
					FRiXCalculatorOutput output_obj = new FRiXCalculatorOutput(BEDFiles, BAMFiles, param, colors);
					output_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
							int temp = (Integer) propertyChangeEvent.getNewValue();
							int percentComplete = (int) (((double) (temp) / (BAMFiles.size() * BEDFiles.size())) * 100);
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
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!", "Validate Input", JOptionPane.ERROR_MESSAGE);
			} catch (SAMException se) {
				JOptionPane.showMessageDialog(null, se.getMessage(), "Validate Input", JOptionPane.ERROR_MESSAGE);
			} catch (OptionException oe) {
				oe.printStackTrace();
				JOptionPane.showMessageDialog(null, oe.getMessage(), "Validate Input", JOptionPane.ERROR_MESSAGE);
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
	 * 
	 * @throws IOException Invalid file or parameters
	 */
	public FRiXCalculatorWindow() throws IOException {
		setTitle("FRiX Calculator");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 850, 480);
		setMinimumSize(new Dimension(750, 480));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		JButton btnLoadBedFile = new JButton("Load BED Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadBedFile, 4, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadBedFile, 10, SpringLayout.WEST, contentPane);
		btnLoadBedFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newBEDFiles = FileSelection.getFiles(fc, "bed", true);
				if (newBEDFiles != null) {
					for (int x = 0; x < newBEDFiles.length; x++) {
						BEDFiles.add(newBEDFiles[x]);
						bedList.addElement(newBEDFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoadBedFile);

		JScrollPane scrollPane_BED = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_BED, 10, SpringLayout.SOUTH, btnLoadBedFile);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_BED, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_BED, 185, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_BED, -425, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane_BED);

		bedList = new DefaultListModel<String>();
		final JList<String> listBed = new JList<>(bedList);
		listBed.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane_BED.setViewportView(listBed);

		JButton btnRemoveBed = new JButton("Remove BED");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBed, 4, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBed, 0, SpringLayout.EAST, scrollPane_BED);
		btnRemoveBed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				while (listBed.getSelectedIndex() > -1) {
					BEDFiles.remove(listBed.getSelectedIndex());
					bedList.remove(listBed.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemoveBed);

		btnLoadBamFiles = new JButton("Load BAM Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadBamFiles, 10, SpringLayout.SOUTH, scrollPane_BED);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadBamFiles, 10, SpringLayout.WEST, contentPane);
		btnLoadBamFiles.addActionListener(new ActionListener() {
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
		contentPane.add(btnLoadBamFiles);

		JScrollPane scrollPane_BAM = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_BAM, 10, SpringLayout.SOUTH, btnLoadBamFiles);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_BAM, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_BAM, -30, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_BAM, 0, SpringLayout.EAST, scrollPane_BED);
		contentPane.add(scrollPane_BAM);

		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane_BAM.setViewportView(listExp);

		btnRemoveBam = new JButton("Remove BAM");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, btnLoadBamFiles);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, 0, SpringLayout.EAST, scrollPane_BAM);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listExp.getSelectedIndex() > -1) {
					BAMFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemoveBam);

		JPanel pnlReadEncoding = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlReadEncoding, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlReadEncoding, 10, SpringLayout.EAST, scrollPane_BED);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlReadEncoding, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlReadEncoding, 60, SpringLayout.NORTH, contentPane);
		contentPane.add(pnlReadEncoding);

		SpringLayout sl_ReadEncoding = new SpringLayout();
		pnlReadEncoding.setLayout(sl_ReadEncoding);
		TitledBorder ttlReadEncoding = BorderFactory.createTitledBorder("Select Read Encoding");
		ttlReadEncoding.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlReadEncoding.setBorder(ttlReadEncoding);

		JLabel lblAspectRead = new JLabel("Read aspect:");
		sl_ReadEncoding.putConstraint(SpringLayout.NORTH, lblAspectRead, 10, SpringLayout.NORTH, pnlReadEncoding);
		sl_ReadEncoding.putConstraint(SpringLayout.WEST, lblAspectRead, 10, SpringLayout.WEST, pnlReadEncoding);
		lblAspectRead.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		pnlReadEncoding.add(lblAspectRead);

		cbox_ReadAspect = new JComboBox<String>(readAspectOptions);
		sl_ReadEncoding.putConstraint(SpringLayout.NORTH, cbox_ReadAspect, -3, SpringLayout.NORTH, lblAspectRead);
		sl_ReadEncoding.putConstraint(SpringLayout.WEST, cbox_ReadAspect, 10, SpringLayout.EAST, lblAspectRead);
		cbox_ReadAspect.setSelectedIndex(0);
		pnlReadEncoding.add(cbox_ReadAspect);
		cbox_ReadAspect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switch (cbox_ReadAspect.getSelectedIndex()) {
					case PileupParameters.FIVE:
						allowReadChoice(true);
						break;
					case PileupParameters.THREE:
						allowReadChoice(true);
						break;
					case PileupParameters.MIDPOINT:
						chckbxRequireProperPe.setSelected(true);
						allowReadChoice(false);
						break;
					case PileupParameters.FRAGMENT:
						chckbxRequireProperPe.setSelected(true);
						allowReadChoice(false);
						break;
				}
				updateCartoon();
			}
		});

		JLabel lblOutputRead = new JLabel("Output read:");
		sl_ReadEncoding.putConstraint(SpringLayout.NORTH, lblOutputRead, 0, SpringLayout.NORTH, lblAspectRead);
		sl_ReadEncoding.putConstraint(SpringLayout.WEST, lblOutputRead, 20, SpringLayout.EAST, cbox_ReadAspect);
		lblOutputRead.setFont(new Font("Lucida Grande", Font.BOLD, 12));
		pnlReadEncoding.add(lblOutputRead);

		cbox_ReadOutput = new JComboBox<String>(readOutputOptions);
		sl_ReadEncoding.putConstraint(SpringLayout.NORTH, cbox_ReadOutput, -3, SpringLayout.NORTH, lblOutputRead);
		sl_ReadEncoding.putConstraint(SpringLayout.WEST, cbox_ReadOutput, 10, SpringLayout.EAST, lblOutputRead);
		sl_ReadEncoding.putConstraint(SpringLayout.EAST, cbox_ReadOutput, -10, SpringLayout.EAST, pnlReadEncoding);
		cbox_ReadOutput.setSelectedIndex(0);
		pnlReadEncoding.add(cbox_ReadOutput);
		cbox_ReadOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateCartoon();
			}
		});

		// Include Graphics
		readCartoon = new ReadFragmentCartoon();
		sl_contentPane.putConstraint(SpringLayout.NORTH, readCartoon, 10, SpringLayout.SOUTH, pnlReadEncoding);
		sl_contentPane.putConstraint(SpringLayout.WEST, readCartoon, 10, SpringLayout.WEST, pnlReadEncoding);
		sl_contentPane.putConstraint(SpringLayout.EAST, readCartoon, -10, SpringLayout.EAST, pnlReadEncoding);
		contentPane.add(readCartoon);

		// Filter Reads
		JPanel pnlFilterReads = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlFilterReads, 0, SpringLayout.SOUTH, readCartoon);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlFilterReads, 10, SpringLayout.EAST, scrollPane_BED);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlFilterReads, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlFilterReads, 100, SpringLayout.SOUTH, readCartoon);
		contentPane.add(pnlFilterReads);

		SpringLayout sl_FilterReads = new SpringLayout();
		pnlFilterReads.setLayout(sl_FilterReads);
		TitledBorder ttlFilterReads = BorderFactory.createTitledBorder("Filter Reads");
		ttlFilterReads.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlFilterReads.setBorder(ttlFilterReads);

		chckbxRequireProperPe = new JCheckBox("Require Proper Paired-End");
		sl_FilterReads.putConstraint(SpringLayout.NORTH, chckbxRequireProperPe, 3, SpringLayout.NORTH, pnlFilterReads);
		sl_FilterReads.putConstraint(SpringLayout.WEST, chckbxRequireProperPe, 10, SpringLayout.WEST, pnlFilterReads);
		pnlFilterReads.add(chckbxRequireProperPe);

		chckbxFilterByMin = new JCheckBox("Filter Min Insert Size (bp)");
		sl_FilterReads.putConstraint(SpringLayout.NORTH, chckbxFilterByMin, 3, SpringLayout.SOUTH, chckbxRequireProperPe);
		sl_FilterReads.putConstraint(SpringLayout.WEST, chckbxFilterByMin, 0, SpringLayout.WEST, chckbxRequireProperPe);
		chckbxFilterByMin.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chckbxFilterByMin.isSelected()) {
					txtMin.setEnabled(true);
					chckbxRequireProperPe.setSelected(true);
					chckbxRequireProperPe.setEnabled(false);
				} else {
					txtMin.setEnabled(false);
					updateCartoon();
				}
			}
		});
		pnlFilterReads.add(chckbxFilterByMin);

		txtMin = new JTextField();
		sl_FilterReads.putConstraint(SpringLayout.NORTH, txtMin, 2, SpringLayout.NORTH, chckbxFilterByMin);
		sl_FilterReads.putConstraint(SpringLayout.WEST, txtMin, 190, SpringLayout.WEST, chckbxFilterByMin);
		sl_FilterReads.putConstraint(SpringLayout.EAST, txtMin, 120, SpringLayout.EAST, chckbxFilterByMin);
		txtMin.setEnabled(false);
		txtMin.setHorizontalAlignment(SwingConstants.CENTER);
		txtMin.setText("0");
		txtMin.setColumns(10);
		pnlFilterReads.add(txtMin);

		chckbxFilterByMax = new JCheckBox("Filter Max Insert Size (bp)");
		sl_FilterReads.putConstraint(SpringLayout.NORTH, chckbxFilterByMax, 3, SpringLayout.SOUTH, chckbxFilterByMin);
		sl_FilterReads.putConstraint(SpringLayout.WEST, chckbxFilterByMax, 0, SpringLayout.WEST, chckbxRequireProperPe);
		chckbxFilterByMax.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chckbxFilterByMax.isSelected()) {
					txtMax.setEnabled(true);
					chckbxRequireProperPe.setSelected(true);
					chckbxRequireProperPe.setEnabled(false);
				} else {
					txtMax.setEnabled(false);
					updateCartoon();
				}
			}
		});
		pnlFilterReads.add(chckbxFilterByMax);

		txtMax = new JTextField();
		sl_FilterReads.putConstraint(SpringLayout.NORTH, txtMax, 2, SpringLayout.NORTH, chckbxFilterByMax);
		sl_FilterReads.putConstraint(SpringLayout.WEST, txtMax, 190, SpringLayout.WEST, chckbxFilterByMax);
		sl_FilterReads.putConstraint(SpringLayout.EAST, txtMax, 120, SpringLayout.EAST, chckbxFilterByMax);
		txtMax.setEnabled(false);
		txtMax.setHorizontalAlignment(SwingConstants.CENTER);
		txtMax.setText("1000");
		txtMax.setColumns(10);
		pnlFilterReads.add(txtMax);

		// Read Manipulation
		JPanel pnlReadManipulation = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlReadManipulation, 6, SpringLayout.SOUTH, pnlFilterReads);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlReadManipulation, 10, SpringLayout.EAST, scrollPane_BED);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlReadManipulation, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlReadManipulation, 55, SpringLayout.SOUTH, pnlFilterReads);
		contentPane.add(pnlReadManipulation);

		SpringLayout sl_ReadManipulation = new SpringLayout();
		pnlReadManipulation.setLayout(sl_ReadManipulation);
		TitledBorder ttlReadManipulation = BorderFactory.createTitledBorder("Read Manipulation");
		ttlReadManipulation.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlReadManipulation.setBorder(ttlReadManipulation);

		JLabel lblTagShift = new JLabel("Tag Shift (bp):");
		sl_ReadManipulation.putConstraint(SpringLayout.NORTH, lblTagShift, 7, SpringLayout.NORTH, pnlReadManipulation);
		sl_ReadManipulation.putConstraint(SpringLayout.WEST, lblTagShift, 10, SpringLayout.WEST, pnlReadManipulation);
		pnlReadManipulation.add(lblTagShift);

		txtShift = new JTextField();
		sl_ReadManipulation.putConstraint(SpringLayout.NORTH, txtShift, -1, SpringLayout.NORTH, lblTagShift);
		sl_ReadManipulation.putConstraint(SpringLayout.WEST, txtShift, 120, SpringLayout.WEST, lblTagShift);
		sl_ReadManipulation.putConstraint(SpringLayout.EAST, txtShift, 120, SpringLayout.EAST, lblTagShift);
		txtShift.setText("0");
		txtShift.setHorizontalAlignment(SwingConstants.CENTER);
		txtShift.setColumns(10);
		pnlReadManipulation.add(txtShift);

		// Output Parameters
		JPanel pnlOutputOptions = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlOutputOptions, 6, SpringLayout.SOUTH, pnlReadManipulation);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlOutputOptions, 10, SpringLayout.EAST, scrollPane_BED);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlOutputOptions, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlOutputOptions, 0, SpringLayout.SOUTH, scrollPane_BAM);
		contentPane.add(pnlOutputOptions);

		SpringLayout sl_OutputOptions = new SpringLayout();
		pnlOutputOptions.setLayout(sl_OutputOptions);
		TitledBorder ttlOutputOptions = BorderFactory.createTitledBorder("Output Options");
		ttlOutputOptions.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlOutputOptions.setBorder(ttlOutputOptions);

		chckbxOutputData = new JCheckBox("Output scores");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, chckbxOutputData, 10, SpringLayout.NORTH, pnlOutputOptions);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, chckbxOutputData, 0, SpringLayout.WEST, pnlOutputOptions);
		chckbxOutputData.setSelected(true);
		pnlOutputOptions.add(chckbxOutputData);

		btnOutputDirectory = new JButton("Output Directory");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 10, SpringLayout.SOUTH, chckbxOutputData);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, btnOutputDirectory, 6, SpringLayout.WEST, pnlOutputOptions);
		pnlOutputOptions.add(btnOutputDirectory);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 2, SpringLayout.NORTH, btnOutputDirectory);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, btnOutputDirectory);
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblDefaultToLocal.setBackground(Color.WHITE);
		lblDefaultToLocal.setToolTipText("Directory path");
		pnlOutputOptions.add(lblDefaultToLocal);

		chckbxOutputData.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				activateOutput();
			}
		});

		btnOutputDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File temp = FileSelection.getOutputDir(fc);
				if(temp != null) {
					OUT_DIR = temp;
					lblDefaultToLocal.setToolTipText(OUT_DIR.getAbsolutePath());
					try {
						lblDefaultToLocal.setText("..." + ExtensionFileFilter.getSubstringEnd(OUT_DIR, 35));
					} catch (IOException ioe) {
						System.err.println("Output directory may not be loaded!");
						ioe.printStackTrace();
					}
				}
			}
		});

		lblCpusToUse = new JLabel("CPU's to Use:");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblCpusToUse, -10, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCpusToUse, 10, SpringLayout.WEST, contentPane);
		lblCpusToUse.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCpusToUse);

		txtCPU = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtCPU, -1, SpringLayout.NORTH, lblCpusToUse);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtCPU, 100, SpringLayout.WEST, lblCpusToUse);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtCPU, 120, SpringLayout.EAST, lblCpusToUse);
		txtCPU.setHorizontalAlignment(SwingConstants.CENTER);
		txtCPU.setText("1");
		txtCPU.setColumns(10);
		contentPane.add(txtCPU);

		btnCalculate = new JButton("Calculate");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCalculate, 350, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnCalculate, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCalculate, -350, SpringLayout.EAST, contentPane);
		contentPane.add(btnCalculate);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.WEST, progressBar, -200, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);
		btnCalculate.setActionCommand("start");

		updateCartoon();
		btnCalculate.addActionListener(this);
	}

	/**
	 * Update components to allow for read choice or not--Disable or enable BOTH
	 * Read Output choices and and "Combined" toggle.
	 * 
	 * @param activate to enable (true) or disable (false) the two components
	 */
	public void allowReadChoice(boolean activate) {
		cbox_ReadOutput.setEnabled(activate);
	}

	/**
	 * Activate/deactivate output directory on whether or not BOTH output heatmap
	 * and output composite are unchecked.
	 */
	public void activateOutput() {
		if (!chckbxOutputData.isSelected()) {
			btnOutputDirectory.setEnabled(false);
			lblDefaultToLocal.setEnabled(false);
		} else {
			btnOutputDirectory.setEnabled(true);
			lblDefaultToLocal.setEnabled(true);
		}
	}

	/**
	 * Extract read aspect and read output to upddate the cartoon accordingly.
	 */
	public void updateCartoon() {
		int aspect = cbox_ReadAspect.getSelectedIndex();
		int read = cbox_ReadOutput.getSelectedIndex();

		if (aspect > 1) {
			chckbxRequireProperPe.setEnabled(false);
		} else {
			if (chckbxFilterByMin.isSelected() || chckbxFilterByMax.isSelected()) {
				chckbxRequireProperPe.setEnabled(false);
			} else {
				chckbxRequireProperPe.setEnabled(true);
			}
		}

		readCartoon.redrawArrows(aspect, read);
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
	 * Makes the content pane non-interactive If the window should be interactive data
	 * @param con Content pane to make non-interactive
	 * @param status If the window should be interactive
	 */
	public void massXable(Container con, boolean status) {
		Component[] components = con.getComponents();
		for (Component component : components) {
			component.setEnabled(status);
			if (component instanceof Container) {
				massXable((Container) component, status);
			}
		}
		if (status) {
			if (cbox_ReadAspect.getSelectedIndex() == 2) {
				chckbxRequireProperPe.setEnabled(false);
				allowReadChoice(false);
			}
			if (chckbxFilterByMin.isSelected() || chckbxFilterByMax.isSelected()) {
				chckbxRequireProperPe.setEnabled(false);
			}
			if (!chckbxFilterByMin.isSelected()) {
				txtMin.setEnabled(false);
			}
			if (!chckbxFilterByMax.isSelected()) {
				txtMax.setEnabled(false);
			}
			activateOutput();
		}
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
}
