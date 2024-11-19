package scriptmanager.window_interface.Read_Analysis;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
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
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import htsjdk.samtools.SAMException;

import scriptmanager.objects.CompositeCartoon;
import scriptmanager.objects.PileupParameters;
import scriptmanager.objects.ReadFragmentCartoon;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Read_Analysis.TagPileup}
 * 
 * @author William KM Lai
 * @see scriptmanager.window_interface.Read_Analysis.TagPileupOutput
 */
@SuppressWarnings("serial")
public class TagPileupWindow extends JFrame implements ActionListener, PropertyChangeListener {
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

	private JButton btnPileup;
	private JButton btnLoadBamFiles;
	private JButton btnRemoveBam;
	private JButton btnOutputDirectory;
	private JButton btnSenseColor;
	private JButton btnAntiColor;
	private JButton btnCombinedColor;
	private JButton btnLoadBlacklistFilter;
	private JButton btnRemoveBlacklistfilter;
	private JComboBox<String> cbox_ReadAspect;
	private JComboBox<String> cbox_ReadOutput;
	private JToggleButton tglSeparate;
	private JToggleButton tglCombined;

	private JComboBox<String> cbox_Transform;
	private JToggleButton tglTab;
	private JToggleButton tglCdt;

	private JLabel lblWindowSizebin;
	private JLabel lblStdDevSize;
	private JLabel lblNumStd;
	private JLabel lblDefaultToLocal;
	private JLabel lblCurrentBlacklist;
	private JLabel lblCpusToUse;
	private JLabel lblNoBlacklistLoaded;
	private JTextField txtMin;
	private JTextField txtMax;
	private JTextField txtShift;
	private JTextField txtBin;
	private JTextField txtTagExtend;
	private JTextField txtSmooth;
	private JTextField txtStdSize;
	private JTextField txtNumStd;
	private JTextField txtCPU;
	private JTextField txtCompositeName;
	private JCheckBox chckbxOutputData;
	private JCheckBox chckbxOutputCompositeData;
	private JCheckBox chckbxTagStandard;
	private JCheckBox chckbxRequireProperPe;
	private JCheckBox chckbxFilterByMin;
	private JCheckBox chckbxFilterByMax;
	private JCheckBox chckbxOutputGzip;

	private File BLACKLIST = null;

	private ReadFragmentCartoon readCartoon;
	private CompositeCartoon compositeCartoon;

	// Names of fields indexed by PileupParameters constants
	private String[] readAspectOptions = {"5' End", "3' End", "Midpoint", "Full Fragment"};
	private String[] readOutputOptions = {"Read 1", "Read 2", "All Reads"};
	private String[] transformationOptions = {"None", "Sliding Window", "Gaussian Smooth"};

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
				if (Integer.parseInt(txtBin.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Bin Size!!! Must be larger than 0 bp");
				} else if (Integer.parseInt(txtTagExtend.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Tag Extend length!!! Must be non-negative bp length" + "\n\n" + "Consider adjusting the shift bp value if you are interested in negative extensions.", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else if (cbox_Transform.getSelectedIndex()==PileupParameters.WINDOW && Integer.parseInt(txtSmooth.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Smoothing Window Size!!! Must be larger than 0 bp", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else if (cbox_Transform.getSelectedIndex()==PileupParameters.WINDOW && Integer.parseInt(txtSmooth.getText()) % 2 == 0) {
					JOptionPane.showMessageDialog(null, "Invalid Smoothing Window Size!!! Must be odd for symmetrical smoothing (so that the window is centered properly).", "Validate Options", JOptionPane.ERROR_MESSAGE);
				} else if (cbox_Transform.getSelectedIndex()==PileupParameters.GAUSSIAN && Integer.parseInt(txtStdSize.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Standard Deviation Size!!! Must be larger than 0 bp", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else if (cbox_Transform.getSelectedIndex()==PileupParameters.GAUSSIAN && Integer.parseInt(txtNumStd.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Number of Standard Deviations!!! Must be larger than 0", "Validate Input", JOptionPane.ERROR_MESSAGE);
				} else if (Integer.parseInt(txtCPU.getText()) < 1) {
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
					if (tglSeparate.isSelected()) {
						param.setStrand(0);
						colors.add(btnSenseColor.getForeground());
						colors.add(btnAntiColor.getForeground());
					} else if (tglCombined.isSelected()) {
						param.setStrand(1);
						colors.add(btnCombinedColor.getForeground());
					}

					// Set Read encoding values from combo box selections
					param.setAspect(cbox_ReadAspect.getSelectedIndex());
					param.setRead(cbox_ReadOutput.getSelectedIndex());

					// Set Read Filter requirements
					if (chckbxRequireProperPe.isSelected()) { param.setPErequire(true); }
					else { param.setPErequire(false); }

					if (chckbxFilterByMin.isSelected()) { param.setMinInsert(Integer.parseInt(txtMin.getText())); }
					if (chckbxFilterByMax.isSelected()) { param.setMaxInsert(Integer.parseInt(txtMax.getText())); }

					// Set composite transformation encoding from combo box selections
					param.setTrans(cbox_Transform.getSelectedIndex());

					// Set output options according to selections
					if (!chckbxOutputData.isSelected() && !chckbxOutputCompositeData.isSelected()) { param.setOutputDirectory(null); }
					else { param.setOutputDirectory(OUT_DIR); }

					param.setOutputCompositeStatus(chckbxOutputCompositeData.isSelected()); //Outputs composite plots if check box is selected
					if(chckbxOutputCompositeData.isSelected()) {
						try {
							param.setCompositePrintStream(new PrintStream(OUT_DIR + File.separator + txtCompositeName.getText()));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}

					if (chckbxOutputData.isSelected()) {
						if (tglTab.isSelected()) { param.setOutputType(1); }
						else if (tglCdt.isSelected()) { param.setOutputType(2); }
						if (chckbxOutputGzip.isSelected()) { param.setGZIPstatus(true); }
					} else { param.setOutputType(0); }

					if (chckbxTagStandard.isSelected()) {
						if (BLACKLIST != null) { param.setBlacklist(BLACKLIST); }
						param.setStandard(true);
					} else {
						param.setStandard(false);
					}

					// SHIFT can be negative
					param.setShift(Integer.parseInt(txtShift.getText()));
					param.setBin(Integer.parseInt(txtBin.getText()));
					param.setSmooth(Integer.parseInt(txtSmooth.getText()));
					param.setStdSize(Integer.parseInt(txtStdSize.getText()));
					param.setStdNum(Integer.parseInt(txtNumStd.getText()));
					param.setTagExtend(Integer.parseInt(txtTagExtend.getText()));
					param.setCPU(Integer.parseInt(txtCPU.getText()));

					// Execute script
					TagPileupOutput output_obj = new TagPileupOutput(BEDFiles, BAMFiles, param, colors);
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
	public TagPileupWindow() throws IOException {
		setTitle("Tag Pileup");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 950, 585);
		setMinimumSize(new Dimension(900, 585));
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
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_BED, -525, SpringLayout.EAST, contentPane);
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
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_BAM, 380, SpringLayout.NORTH, contentPane);
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
						tglCombined.setSelected(true);
						allowReadChoice(false);
						break;
					case PileupParameters.FRAGMENT:
						chckbxRequireProperPe.setSelected(true);
						tglCombined.setSelected(true);
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
		sl_contentPane.putConstraint(SpringLayout.WEST, readCartoon, 0, SpringLayout.WEST, pnlReadEncoding);
		contentPane.add(readCartoon);

		compositeCartoon = new CompositeCartoon(16);
		sl_contentPane.putConstraint(SpringLayout.NORTH, compositeCartoon, -5, SpringLayout.NORTH, readCartoon);
		sl_contentPane.putConstraint(SpringLayout.EAST, compositeCartoon, -20, SpringLayout.EAST, contentPane);
		contentPane.add(compositeCartoon);


		// Filter Reads
		JPanel pnlFilterReads = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlFilterReads, 20, SpringLayout.SOUTH, readCartoon);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlFilterReads, 10, SpringLayout.EAST, scrollPane_BED);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlFilterReads, 290, SpringLayout.EAST, scrollPane_BED);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlFilterReads, 120, SpringLayout.SOUTH, readCartoon);
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
		sl_FilterReads.putConstraint(SpringLayout.EAST, txtMin, -6, SpringLayout.EAST, pnlFilterReads);
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
		sl_FilterReads.putConstraint(SpringLayout.EAST, txtMax, -6, SpringLayout.EAST, pnlFilterReads);
		txtMax.setEnabled(false);
		txtMax.setHorizontalAlignment(SwingConstants.CENTER);
		txtMax.setText("1000");
		txtMax.setColumns(10);
		pnlFilterReads.add(txtMax);

		// Read Manipulation
		JPanel pnlReadManipulation = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlReadManipulation, 0, SpringLayout.NORTH, pnlFilterReads);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlReadManipulation, 10, SpringLayout.EAST, pnlFilterReads);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlReadManipulation, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlReadManipulation, 0, SpringLayout.SOUTH, pnlFilterReads);
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
		sl_ReadManipulation.putConstraint(SpringLayout.EAST, txtShift, -6, SpringLayout.EAST, pnlReadManipulation);
		txtShift.setText("0");
		txtShift.setHorizontalAlignment(SwingConstants.CENTER);
		txtShift.setColumns(10);
		pnlReadManipulation.add(txtShift);

		JLabel lblBinSizebp = new JLabel("Bin Size (bp):");
		sl_ReadManipulation.putConstraint(SpringLayout.NORTH, lblBinSizebp, 10, SpringLayout.SOUTH, lblTagShift);
		sl_ReadManipulation.putConstraint(SpringLayout.WEST, lblBinSizebp, 0, SpringLayout.WEST, lblTagShift);
		pnlReadManipulation.add(lblBinSizebp);

		txtBin = new JTextField();
		sl_ReadManipulation.putConstraint(SpringLayout.NORTH, txtBin, -1, SpringLayout.NORTH, lblBinSizebp);
		sl_ReadManipulation.putConstraint(SpringLayout.WEST, txtBin, 120, SpringLayout.WEST, lblBinSizebp);
		sl_ReadManipulation.putConstraint(SpringLayout.EAST, txtBin, -6, SpringLayout.EAST, pnlReadManipulation);
		txtBin.setText("1");
		txtBin.setHorizontalAlignment(SwingConstants.CENTER);
		txtBin.setColumns(10);
		pnlReadManipulation.add(txtBin);

		JLabel lblTagExtend = new JLabel("Tag Extend (bp):");
		sl_ReadManipulation.putConstraint(SpringLayout.NORTH, lblTagExtend, 10, SpringLayout.SOUTH, lblBinSizebp);
		sl_ReadManipulation.putConstraint(SpringLayout.WEST, lblTagExtend, 0, SpringLayout.WEST, lblBinSizebp);
		pnlReadManipulation.add(lblTagExtend);

		txtTagExtend = new JTextField();
		sl_ReadManipulation.putConstraint(SpringLayout.NORTH, txtTagExtend, -1, SpringLayout.NORTH, lblTagExtend);
		sl_ReadManipulation.putConstraint(SpringLayout.WEST, txtTagExtend, 120, SpringLayout.WEST, lblTagExtend);
		sl_ReadManipulation.putConstraint(SpringLayout.EAST, txtTagExtend, -6, SpringLayout.EAST, pnlReadManipulation);
		txtTagExtend.setText("0");
		txtTagExtend.setHorizontalAlignment(SwingConstants.CENTER);
		txtTagExtend.setColumns(10);
		pnlReadManipulation.add(txtTagExtend);


		// Stand Options
		int SCALE = 170;
		tglSeparate = new JToggleButton("Separate");
		sl_contentPane.putConstraint(SpringLayout.NORTH, tglSeparate, 10, SpringLayout.SOUTH, pnlFilterReads);
		sl_contentPane.putConstraint(SpringLayout.WEST, tglSeparate, 10, SpringLayout.EAST, scrollPane_BED);
		sl_contentPane.putConstraint(SpringLayout.EAST, tglSeparate, 10 + SCALE*2, SpringLayout.EAST, scrollPane_BED);
		tglSeparate.setSelected(true);
		tglSeparate.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (tglSeparate.isSelected()) {
					btnSenseColor.setEnabled(true);
					btnAntiColor.setEnabled(true);
					btnCombinedColor.setEnabled(false);

					compositeCartoon.setStrand(PileupParameters.SEPARATE);
					compositeCartoon.setForwardColor(btnSenseColor.getForeground());
					compositeCartoon.setReverseColor(btnAntiColor.getForeground());
					compositeCartoon.repaint();
				}
			}
		});
		contentPane.add(tglSeparate);

		tglCombined = new JToggleButton("Combined");
		sl_contentPane.putConstraint(SpringLayout.NORTH, tglCombined, 0, SpringLayout.NORTH, tglSeparate);
		sl_contentPane.putConstraint(SpringLayout.WEST, tglCombined, 0, SpringLayout.EAST, tglSeparate);
		sl_contentPane.putConstraint(SpringLayout.EAST, tglCombined, SCALE, SpringLayout.EAST, tglSeparate);
		tglCombined.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (tglCombined.isSelected()) {
					btnSenseColor.setEnabled(false);
					btnAntiColor.setEnabled(false);
					btnCombinedColor.setEnabled(true);

					compositeCartoon.setStrand(PileupParameters.COMBINED);
					compositeCartoon.setForwardColor(btnCombinedColor.getForeground());
					compositeCartoon.repaint();
				}
			}
		});
		contentPane.add(tglCombined);

		ButtonGroup toggleStrand = new ButtonGroup();
		toggleStrand.add(tglSeparate);
		toggleStrand.add(tglCombined);

		btnSenseColor = new JButton("Sense Color");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnSenseColor, 0, SpringLayout.SOUTH, tglSeparate);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnSenseColor, 0, SpringLayout.WEST, tglSeparate);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnSenseColor, SCALE, SpringLayout.WEST, tglSeparate);
		btnSenseColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Color newColor = JColorChooser.showDialog(btnSenseColor, "Select an Output Color", btnSenseColor.getForeground());
				if (newColor != null) {
					btnSenseColor.setForeground(newColor);
					compositeCartoon.setForwardColor(btnSenseColor.getForeground());
				}
				compositeCartoon.repaint();
			}
		});
		btnSenseColor.setForeground(new Color(0, 0, 255));
		contentPane.add(btnSenseColor);

		btnAntiColor = new JButton("Anti Color");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnAntiColor, 0, SpringLayout.NORTH, btnSenseColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnAntiColor, 0, SpringLayout.EAST, btnSenseColor);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnAntiColor, 0, SpringLayout.EAST, tglSeparate);
		btnAntiColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(btnAntiColor, "Select an Output Color", btnAntiColor.getForeground());
				if (newColor != null) {
					btnAntiColor.setForeground(newColor);
					compositeCartoon.setReverseColor(btnAntiColor.getForeground());
				}
				compositeCartoon.repaint();
			}
		});
		btnAntiColor.setForeground(new Color(255, 0, 0));
		contentPane.add(btnAntiColor);

		btnCombinedColor = new JButton("Combined Color");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnCombinedColor, 0, SpringLayout.NORTH, btnAntiColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCombinedColor, 0, SpringLayout.WEST, tglCombined);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCombinedColor, 0, SpringLayout.EAST, tglCombined);
		btnCombinedColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(btnCombinedColor, "Select an Output Color", btnCombinedColor.getForeground());
				if (newColor != null) {
					btnCombinedColor.setForeground(newColor);
					compositeCartoon.setForwardColor(btnCombinedColor.getForeground());
				}
				compositeCartoon.repaint();
			}
		});
		btnCombinedColor.setForeground(new Color(0, 0, 0));
		btnCombinedColor.setEnabled(false);
		contentPane.add(btnCombinedColor);


		// Scaling Options
		JPanel pnlEqualStrands = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlEqualStrands, 10, SpringLayout.SOUTH, btnCombinedColor);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlEqualStrands, 0, SpringLayout.WEST, pnlFilterReads);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlEqualStrands, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlEqualStrands, 90, SpringLayout.SOUTH, btnCombinedColor);
		contentPane.add(pnlEqualStrands);

		SpringLayout sl_EqualStrands = new SpringLayout();
		pnlEqualStrands.setLayout(sl_EqualStrands);
		TitledBorder ttlStrandOutput = BorderFactory.createTitledBorder("Scaling Options");
		ttlStrandOutput.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlEqualStrands.setBorder(ttlStrandOutput);

		chckbxTagStandard = new JCheckBox("Set Tags to Be Equal");
		sl_EqualStrands.putConstraint(SpringLayout.NORTH, chckbxTagStandard, 6, SpringLayout.NORTH, pnlEqualStrands);
		sl_EqualStrands.putConstraint(SpringLayout.WEST, chckbxTagStandard, 10, SpringLayout.WEST, pnlEqualStrands);
		pnlEqualStrands.add(chckbxTagStandard);

		btnLoadBlacklistFilter = new JButton("Load Blacklist Filter");
		sl_EqualStrands.putConstraint(SpringLayout.NORTH, btnLoadBlacklistFilter, -1, SpringLayout.NORTH, chckbxTagStandard);
		sl_EqualStrands.putConstraint(SpringLayout.EAST, btnLoadBlacklistFilter, -10, SpringLayout.EAST, pnlEqualStrands);
		btnLoadBlacklistFilter.setEnabled(false);
		pnlEqualStrands.add(btnLoadBlacklistFilter);

		btnRemoveBlacklistfilter = new JButton();
		btnRemoveBlacklistfilter.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/miniRedX.png")).getScaledInstance(20, 20, Image.SCALE_DEFAULT)));
		sl_EqualStrands.putConstraint(SpringLayout.NORTH, btnRemoveBlacklistfilter, 3, SpringLayout.SOUTH, chckbxTagStandard);
		sl_EqualStrands.putConstraint(SpringLayout.WEST, btnRemoveBlacklistfilter, 0, SpringLayout.WEST, chckbxTagStandard);
		btnRemoveBlacklistfilter.setPreferredSize(new Dimension(20, 20));
		btnRemoveBlacklistfilter.setEnabled(false);
		btnRemoveBlacklistfilter.setToolTipText("remove blacklist");
		pnlEqualStrands.add(btnRemoveBlacklistfilter);

		lblCurrentBlacklist = new JLabel("Current Blacklist:");
		sl_EqualStrands.putConstraint(SpringLayout.NORTH, lblCurrentBlacklist, 2, SpringLayout.NORTH, btnRemoveBlacklistfilter);
		sl_EqualStrands.putConstraint(SpringLayout.WEST, lblCurrentBlacklist, 10, SpringLayout.EAST, btnRemoveBlacklistfilter);
		lblCurrentBlacklist.setEnabled(false);
		lblCurrentBlacklist.setFont(new Font("Dialog", Font.BOLD, 13));
		pnlEqualStrands.add(lblCurrentBlacklist);

		lblNoBlacklistLoaded = new JLabel("No Blacklist Loaded");
		sl_EqualStrands.putConstraint(SpringLayout.NORTH, lblNoBlacklistLoaded, 0, SpringLayout.NORTH, lblCurrentBlacklist);
		sl_EqualStrands.putConstraint(SpringLayout.WEST, lblNoBlacklistLoaded, 6, SpringLayout.EAST, lblCurrentBlacklist);
		lblNoBlacklistLoaded.setToolTipText("No Blacklist Loaded");
		lblNoBlacklistLoaded.setEnabled(false);
		lblNoBlacklistLoaded.setFont(new Font("Dialog", Font.PLAIN, 12));
		pnlEqualStrands.add(lblNoBlacklistLoaded);


		// Transformation Options
		JPanel pnlTransformation = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlTransformation, 10, SpringLayout.SOUTH, pnlEqualStrands);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlTransformation, 0, SpringLayout.WEST, pnlFilterReads);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlTransformation, -10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlTransformation, 90, SpringLayout.SOUTH, pnlEqualStrands);
		contentPane.add(pnlTransformation);

		SpringLayout sl_Transformation = new SpringLayout();
		pnlTransformation.setLayout(sl_Transformation);
		TitledBorder ttlTransformation = BorderFactory.createTitledBorder("Composite Transformation");
		ttlTransformation.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlTransformation.setBorder(ttlTransformation);

		cbox_Transform = new JComboBox<String>(transformationOptions);
		sl_Transformation.putConstraint(SpringLayout.NORTH, cbox_Transform, 15, SpringLayout.NORTH, pnlTransformation);
		sl_Transformation.putConstraint(SpringLayout.WEST, cbox_Transform, 10, SpringLayout.WEST, pnlTransformation);
		pnlTransformation.add(cbox_Transform);
		cbox_Transform.setSelectedIndex(0);
		cbox_Transform.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switch(cbox_Transform.getSelectedIndex()) {
					case PileupParameters.NO_SMOOTH:
						lblStdDevSize.setEnabled(false);
						lblNumStd.setEnabled(false);
						txtSmooth.setEnabled(false);
						txtStdSize.setEnabled(false);
						txtNumStd.setEnabled(false);
						break;
					case PileupParameters.WINDOW:
						lblWindowSizebin.setEnabled(true);
						lblStdDevSize.setEnabled(false);
						lblNumStd.setEnabled(false);
						txtSmooth.setEnabled(true);
						txtStdSize.setEnabled(false);
						txtNumStd.setEnabled(false);
						break;
					case PileupParameters.GAUSSIAN:
						lblWindowSizebin.setEnabled(false);
						lblStdDevSize.setEnabled(true);
						lblNumStd.setEnabled(true);
						txtSmooth.setEnabled(false);
						txtStdSize.setEnabled(true);
						txtNumStd.setEnabled(true);
						break;
				}
			}
		});

		lblWindowSizebin = new JLabel("Window Size (Bin #):");
		sl_Transformation.putConstraint(SpringLayout.NORTH, lblWindowSizebin, -6, SpringLayout.NORTH, cbox_Transform);
		sl_Transformation.putConstraint(SpringLayout.WEST, lblWindowSizebin, 10, SpringLayout.EAST, cbox_Transform);
		lblWindowSizebin.setEnabled(false);
		pnlTransformation.add(lblWindowSizebin);

		txtSmooth = new JTextField();
		sl_Transformation.putConstraint(SpringLayout.NORTH, txtSmooth, 0, SpringLayout.NORTH, lblWindowSizebin);
		sl_Transformation.putConstraint(SpringLayout.WEST, txtSmooth, 10, SpringLayout.EAST, lblWindowSizebin);
		sl_Transformation.putConstraint(SpringLayout.EAST, txtSmooth, 80, SpringLayout.EAST, lblWindowSizebin);
		txtSmooth.setHorizontalAlignment(SwingConstants.CENTER);
		txtSmooth.setEnabled(false);
		txtSmooth.setText("3");
		txtSmooth.setColumns(10);
		pnlTransformation.add(txtSmooth);

		lblStdDevSize = new JLabel("Std Dev Size (Bin #):");
		sl_Transformation.putConstraint(SpringLayout.NORTH, lblStdDevSize, 8, SpringLayout.SOUTH, lblWindowSizebin);
		sl_Transformation.putConstraint(SpringLayout.WEST, lblStdDevSize, 0, SpringLayout.WEST, lblWindowSizebin);
		lblStdDevSize.setEnabled(false);
		pnlTransformation.add(lblStdDevSize);

		lblNumStd = new JLabel("# of Std Dev:");
		sl_Transformation.putConstraint(SpringLayout.NORTH, lblNumStd, 0, SpringLayout.NORTH, lblStdDevSize);
		lblNumStd.setEnabled(false);
		pnlTransformation.add(lblNumStd);

		txtStdSize = new JTextField();
		sl_Transformation.putConstraint(SpringLayout.EAST, txtStdSize, 50, SpringLayout.EAST, lblStdDevSize);
		sl_Transformation.putConstraint(SpringLayout.WEST, lblNumStd, 8, SpringLayout.EAST, txtStdSize);
		sl_Transformation.putConstraint(SpringLayout.WEST, txtStdSize, 0, SpringLayout.WEST, txtSmooth);
		sl_Transformation.putConstraint(SpringLayout.NORTH, txtStdSize, -1, SpringLayout.NORTH, lblStdDevSize);
		txtStdSize.setEnabled(false);
		txtStdSize.setHorizontalAlignment(SwingConstants.CENTER);
		txtStdSize.setText("5");
		txtStdSize.setColumns(10);
		pnlTransformation.add(txtStdSize);

		txtNumStd = new JTextField();
		sl_Transformation.putConstraint(SpringLayout.NORTH, txtNumStd, -1, SpringLayout.NORTH, lblStdDevSize);
		sl_Transformation.putConstraint(SpringLayout.WEST, txtNumStd, 8, SpringLayout.EAST, lblNumStd);
		sl_Transformation.putConstraint(SpringLayout.EAST, txtNumStd, 50, SpringLayout.EAST, lblNumStd);
		txtNumStd.setEnabled(false);
		txtNumStd.setHorizontalAlignment(SwingConstants.CENTER);
		txtNumStd.setText("3");
		txtNumStd.setColumns(10);
		pnlTransformation.add(txtNumStd);


		// Output Parameters
		JPanel pnlOutputOptions = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlOutputOptions, 6, SpringLayout.SOUTH, scrollPane_BAM);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlOutputOptions, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlOutputOptions, -10, SpringLayout.WEST, pnlTransformation);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlOutputOptions, -35, SpringLayout.SOUTH, contentPane);
		contentPane.add(pnlOutputOptions);

		SpringLayout sl_OutputOptions = new SpringLayout();
		pnlOutputOptions.setLayout(sl_OutputOptions);
		TitledBorder ttlOutputOptions = BorderFactory.createTitledBorder("Output Options");
		ttlOutputOptions.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlOutputOptions.setBorder(ttlOutputOptions);

		chckbxOutputData = new JCheckBox("Output Heatmap Matrix");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, chckbxOutputData, 6, SpringLayout.NORTH, pnlOutputOptions);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, chckbxOutputData, 0, SpringLayout.WEST, pnlOutputOptions);
		chckbxOutputData.setSelected(true);
		pnlOutputOptions.add(chckbxOutputData);

		tglCdt = new JToggleButton("CDT");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, tglCdt, -2, SpringLayout.NORTH, chckbxOutputData);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, tglCdt, 6, SpringLayout.EAST, chckbxOutputData);
		pnlOutputOptions.add(tglCdt);

		tglTab = new JToggleButton("TAB");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, tglTab, 0, SpringLayout.NORTH, tglCdt);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, tglTab, 0, SpringLayout.EAST, tglCdt);
		pnlOutputOptions.add(tglTab);

		ButtonGroup output = new ButtonGroup();
		output.add(tglTab);
		output.add(tglCdt);
		tglCdt.setSelected(true);

		chckbxOutputGzip = new JCheckBox("Output GZip");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, chckbxOutputGzip, 0, SpringLayout.NORTH, chckbxOutputData);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, chckbxOutputGzip, 6, SpringLayout.EAST, tglTab);
		pnlOutputOptions.add(chckbxOutputGzip);

		chckbxOutputCompositeData = new JCheckBox("Output Composite");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, chckbxOutputCompositeData, 10, SpringLayout.SOUTH, chckbxOutputData);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, chckbxOutputCompositeData, 0, SpringLayout.WEST, pnlOutputOptions);
		chckbxOutputCompositeData.setSelected(true);
		pnlOutputOptions.add(chckbxOutputCompositeData);

		txtCompositeName = new JTextField();
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, txtCompositeName, 2, SpringLayout.NORTH, chckbxOutputCompositeData);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, txtCompositeName, 10, SpringLayout.EAST, chckbxOutputCompositeData);
		sl_OutputOptions.putConstraint(SpringLayout.EAST, txtCompositeName, -10, SpringLayout.EAST, pnlOutputOptions);
		txtCompositeName.setText("composite_average.out");
		txtCompositeName.setColumns(10);
		pnlOutputOptions.add(txtCompositeName);

		btnOutputDirectory = new JButton("Output Directory");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 10, SpringLayout.SOUTH, chckbxOutputCompositeData);
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
				if (chckbxOutputData.isSelected()) {
					tglTab.setEnabled(true);
					tglCdt.setEnabled(true);
					chckbxOutputGzip.setEnabled(true);
				} else {
					tglTab.setEnabled(false);
					tglCdt.setEnabled(false);
					chckbxOutputGzip.setEnabled(false);
				}
				activateOutput();
			}
		});

		chckbxOutputCompositeData.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chckbxOutputCompositeData.isSelected()) {
					txtCompositeName.setEnabled(true);
				} else {
					txtCompositeName.setEnabled(false);
					if (!chckbxOutputData.isSelected()) {
					}
				}
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

		chckbxTagStandard.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chckbxTagStandard.isSelected()) {
					activateBlacklist(true);
				} else {
					activateBlacklist(false);
				}
			}
		});

		btnLoadBlacklistFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File newBlack = FileSelection.getFile(fc, "bed");
				if (newBlack != null) {
					BLACKLIST = newBlack.getAbsoluteFile();
					try {
						lblNoBlacklistLoaded.setText("..." + ExtensionFileFilter.getSubstringEnd(newBlack, 45));
					} catch (IOException e1) {
						System.err.println("Blacklist may not be loaded!");
						e1.printStackTrace();
					}
				}
				lblNoBlacklistLoaded.setToolTipText(newBlack.getAbsolutePath());
			}
		});

		btnRemoveBlacklistfilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BLACKLIST = null;
				lblNoBlacklistLoaded.setText("No Blacklist Loaded");
				lblNoBlacklistLoaded.setToolTipText("No Blacklist Loaded");
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

		btnPileup = new JButton("Pile Tags");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnPileup, 400, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnPileup, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnPileup, -400, SpringLayout.EAST, contentPane);
		contentPane.add(btnPileup);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnPileup);
		sl_contentPane.putConstraint(SpringLayout.WEST, progressBar, -200, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);
		btnPileup.setActionCommand("start");

		updateCartoon();
		btnPileup.addActionListener(this);
	}

	/**
	 * Update components to allow for read choice or not--Disable or enable BOTH
	 * Read Output choices and and "Combined" toggle.
	 * 
	 * @param activate to enable (true) or disable (false) the two components
	 */
	public void allowReadChoice(boolean activate) {
		cbox_ReadOutput.setEnabled(activate);
		tglSeparate.setEnabled(activate);
	}

	/**
	 * Activate/deactivate output directory on whether or not BOTH output heatmap
	 * and output composite are unchecked.
	 */
	public void activateOutput() {
		if (!chckbxOutputCompositeData.isSelected() && !chckbxOutputData.isSelected()) {
			btnOutputDirectory.setEnabled(false);
			lblDefaultToLocal.setEnabled(false);
		} else {
			btnOutputDirectory.setEnabled(true);
			lblDefaultToLocal.setEnabled(true);
		}
	}

	public void activateBlacklist(boolean activate) {
		btnLoadBlacklistFilter.setEnabled(activate);
		btnRemoveBlacklistfilter.setEnabled(activate);
		lblCurrentBlacklist.setEnabled(activate);
		lblNoBlacklistLoaded.setEnabled(activate);
		if (activate) {
			btnRemoveBlacklistfilter.setBackground(Color.BLACK);
		} else {
			btnRemoveBlacklistfilter.setBackground(Color.GRAY);
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
			if (tglSeparate.isSelected()) {
				btnSenseColor.setEnabled(true);
				btnAntiColor.setEnabled(true);
				btnCombinedColor.setEnabled(false);
			} else {
				btnSenseColor.setEnabled(false);
				btnAntiColor.setEnabled(false);
				btnCombinedColor.setEnabled(true);
			}
			if (cbox_Transform.getSelectedIndex()==0) {
				lblWindowSizebin.setEnabled(false);
				lblStdDevSize.setEnabled(false);
				lblNumStd.setEnabled(false);
				txtSmooth.setEnabled(false);
				txtStdSize.setEnabled(false);
				txtNumStd.setEnabled(false);
			}
			if (cbox_Transform.getSelectedIndex()==2) {
				lblWindowSizebin.setEnabled(false);
				lblStdDevSize.setEnabled(true);
				lblNumStd.setEnabled(true);
				txtSmooth.setEnabled(false);
				txtStdSize.setEnabled(true);
				txtNumStd.setEnabled(true);
			}
			if (cbox_Transform.getSelectedIndex()==1) {
				lblWindowSizebin.setEnabled(true);
				lblStdDevSize.setEnabled(false);
				lblNumStd.setEnabled(false);
				txtSmooth.setEnabled(true);
				txtStdSize.setEnabled(false);
				txtNumStd.setEnabled(false);
			}
			if (!chckbxOutputData.isSelected()) {
				tglTab.setEnabled(false);
				tglCdt.setEnabled(false);
				chckbxOutputGzip.setEnabled(false);
			}
			if (!chckbxOutputCompositeData.isSelected()) {
				txtCompositeName.setEnabled(false);
			}
			activateOutput();
			if (chckbxTagStandard.isSelected()) {
				activateBlacklist(true);
			} else {
				activateBlacklist(false);
			}
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
