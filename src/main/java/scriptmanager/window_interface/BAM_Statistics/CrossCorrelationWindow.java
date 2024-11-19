package scriptmanager.window_interface.BAM_Statistics;

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
import java.util.Vector;

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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.ArchTEx.CorrParameter;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.BAM_Statistics.CrossCorrelation} <br>
 * Code largely sourced from ArchTEx.components.CorrelationParametersWindow in
 * <a href=
 * "https://github.com/WilliamKMLai/ArchTEx">https://github.com/WilliamKMLai/ArchTEx</a>
 * 
 * @author William KM Lai
 * @see scriptmanager.objects.ArchTEx.CorrParameter
 * @see scriptmanager.scripts.BAM_Statistics.CrossCorrelation
 * @see scriptmanager.window_interface.BAM_Statistics.CrossCorrelationOutput
 */
@SuppressWarnings("serial")
public class CrossCorrelationWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));	
	private JCheckBox chckbxOutputStatistics;
	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnOutputDirectory;
	private JButton btnCorrelate;
	private JTextField txtCPU;
	private JTextField txtWind;
	private JTextField txtSample;
//	private JLabel lblCurrentOutput;
	private JLabel lblDefaultToLocal;

	private JPanel pnlSamplingParams;

	private JRadioButton rdbtnRandom;
	private JRadioButton rdbtnGenome;

	final DefaultListModel<String> expList;
	Vector<File> BAMFiles = new Vector<File>();
	private File OUT_DIR = new File(System.getProperty("user.dir"));

	private JProgressBar progressBar;
	/**
	 * Used to run the script efficiently
	 */
	public Task task;

	/**
	 * Organize user inputs for calling script
	 */
	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			setProgress(0);
			try {
				if(Integer.parseInt(txtCPU.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid number of CPUs!!! Must be integer greater than 0");
				} else if (BAMFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "Must load at least one BAM file");
				} else if(rdbtnRandom.isSelected() && Integer.parseInt(txtWind.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid window size!!! Must be integer greater than 0");
				} else if(rdbtnRandom.isSelected() && Integer.parseInt(txtSample.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid number of samples!!! Must be greater than 0");
				} else {
					// Load Parameters
					System.out.println("Loading Parameters...");
					CorrParameter param = new CorrParameter();
					if(rdbtnRandom.isSelected()) {
						param.setCorrType(false);
						param.setWindow(Integer.parseInt(txtWind.getText()));
						param.setIterations(Integer.parseInt(txtSample.getText()));
						param.setThreads(Integer.parseInt(txtCPU.getText()));
					} else if(rdbtnGenome.isSelected()) {
						param.setCorrType(true);
					}
					System.out.println("Parameters Loaded.\n");
					// Initialize output window and run
					CrossCorrelationOutput output_obj = new CrossCorrelationOutput(BAMFiles, OUT_DIR, chckbxOutputStatistics.isSelected(), param);
					output_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
							int temp = (Integer) propertyChangeEvent.getNewValue();
							int percentComplete = (int)(((double)(temp) / BAMFiles.size()) * 100);
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
			} catch(NumberFormatException nfe){
				JOptionPane.showMessageDialog(null, "Input Fields Must Contain Integers");
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
			setCursor(null); //turn off the wait cursor
		}
	}

	/**
	 * Instantiate window with graphical interface design.
	 */
	public CrossCorrelationWindow() {
		setTitle("BAM Cross Correlation");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 490, 420);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		setMinimumSize(new Dimension(490, 380));

		JScrollPane scrollPane_BAM = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_BAM, -290, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_BAM, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_BAM, -5, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane_BAM);

		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane_BAM.setViewportView(listExp);

		btnLoad = new JButton("Load BAM Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_BAM, 6, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 5, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newBAMFiles = FileSelection.getFiles(fc,"bam");
				if(newBAMFiles != null) {
					for(int x = 0; x < newBAMFiles.length; x++) { 
						BAMFiles.add(newBAMFiles[x]);
						expList.addElement(newBAMFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemoveBam = new JButton("Remove BAM");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -5, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 0, SpringLayout.NORTH, btnRemoveBam);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while(listExp.getSelectedIndex() > -1) {
					BAMFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});		
		contentPane.add(btnRemoveBam);

		// Execution and progress bar components
		btnCorrelate = new JButton("Correlate");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnCorrelate, -5, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCorrelate, 170, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCorrelate, -170, SpringLayout.EAST, contentPane);
		contentPane.add(btnCorrelate);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -5, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnCorrelate, -3, SpringLayout.NORTH, progressBar);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCorrelate, -18, SpringLayout.WEST, progressBar);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, progressBar, -10, SpringLayout.SOUTH, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		// Correlation param components
		JPanel pnlCorrelationParams = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlCorrelationParams, 10, SpringLayout.SOUTH, scrollPane_BAM);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlCorrelationParams, 0, SpringLayout.WEST, scrollPane_BAM);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlCorrelationParams, 0, SpringLayout.EAST, scrollPane_BAM);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlCorrelationParams, 90, SpringLayout.SOUTH, scrollPane_BAM);
		contentPane.add(pnlCorrelationParams);

		SpringLayout sl_CorrelationParams = new SpringLayout();
		pnlCorrelationParams.setLayout(sl_CorrelationParams);
		TitledBorder ttlCorrelationParams = BorderFactory.createTitledBorder("Correlation Parameters");
		ttlCorrelationParams.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlCorrelationParams.setBorder(ttlCorrelationParams);

//		JLabel lblCorrelationParameters = new JLabel("Correlation Parameters");
//		lblCorrelationParameters.setFont(new Font("Tahoma", Font.BOLD, 11));
//		lblCorrelationParameters.setBounds(185, 315, 133, 14);
//		contentPane.add(lblCorrelationParameters);

		//Radio Buttons to Control Whole-Genome vs Random Sampling
		JLabel lblCorrelationType = new JLabel("Correlation Type:");
		sl_CorrelationParams.putConstraint(SpringLayout.NORTH, lblCorrelationType, 10, SpringLayout.NORTH, pnlCorrelationParams);
		sl_CorrelationParams.putConstraint(SpringLayout.WEST, lblCorrelationType, 10, SpringLayout.WEST, pnlCorrelationParams);
		pnlCorrelationParams.add(lblCorrelationType);

		rdbtnGenome = new JRadioButton("Whole Genome");
		sl_CorrelationParams.putConstraint(SpringLayout.NORTH, rdbtnGenome, -2, SpringLayout.NORTH, lblCorrelationType);
		sl_CorrelationParams.putConstraint(SpringLayout.WEST, rdbtnGenome, 10, SpringLayout.EAST, lblCorrelationType);
		rdbtnGenome.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlSamplingParams.setEnabled(false);
				for (Component c : pnlSamplingParams.getComponents()) {
					c.setEnabled(false);
				}
			}
		});
		pnlCorrelationParams.add(rdbtnGenome);

		rdbtnRandom = new JRadioButton("Random Sampling");
		sl_CorrelationParams.putConstraint(SpringLayout.NORTH, rdbtnRandom, -2, SpringLayout.NORTH, lblCorrelationType);
		sl_CorrelationParams.putConstraint(SpringLayout.WEST, rdbtnRandom, 10, SpringLayout.EAST, rdbtnGenome);
		rdbtnRandom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pnlSamplingParams.setEnabled(true);
				for (Component c : pnlSamplingParams.getComponents()) {
					c.setEnabled(true);
				}
			}
		});
		pnlCorrelationParams.add(rdbtnRandom);

		ButtonGroup groupCorrType = new ButtonGroup();
		groupCorrType.add(rdbtnGenome);
		groupCorrType.add(rdbtnRandom);
		rdbtnGenome.setSelected(true);

		JLabel lblCpusToUse = new JLabel("CPU's to Use:");
		sl_CorrelationParams.putConstraint(SpringLayout.NORTH, lblCpusToUse, 10, SpringLayout.SOUTH, lblCorrelationType);
		sl_CorrelationParams.putConstraint(SpringLayout.WEST, lblCpusToUse, 10, SpringLayout.WEST, pnlCorrelationParams);
		pnlCorrelationParams.add(lblCpusToUse);

		txtCPU = new JTextField("1");
		sl_CorrelationParams.putConstraint(SpringLayout.NORTH, txtCPU, 0, SpringLayout.NORTH, lblCpusToUse);
		sl_CorrelationParams.putConstraint(SpringLayout.WEST, txtCPU, 10, SpringLayout.EAST, lblCpusToUse);
		txtCPU.setHorizontalAlignment(SwingConstants.CENTER);
		txtCPU.setColumns(10);
		pnlCorrelationParams.add(txtCPU);

		// Random Sampling param components  (FlowLayout)
		pnlSamplingParams = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlSamplingParams, 10, SpringLayout.SOUTH, pnlCorrelationParams);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlSamplingParams, 0, SpringLayout.WEST, scrollPane_BAM);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlSamplingParams, 0, SpringLayout.EAST, scrollPane_BAM);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlSamplingParams, 60, SpringLayout.SOUTH, pnlCorrelationParams);
		contentPane.add(pnlSamplingParams);

		TitledBorder ttlSamplingParams = BorderFactory.createTitledBorder("Random Sampling Parameters");
		ttlSamplingParams.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlSamplingParams.setBorder(ttlSamplingParams);

		JLabel lblWindowSizebp = new JLabel("Window Size (bp):");
		pnlSamplingParams.add(lblWindowSizebp);

		txtWind = new JTextField("50000");
		txtWind.setHorizontalAlignment(SwingConstants.LEFT);
		txtWind.setHorizontalAlignment(SwingConstants.CENTER);
		txtWind.setColumns(10);
		pnlSamplingParams.add(txtWind);

		JLabel lblTagSamplings = new JLabel("# of Samplings");
		pnlSamplingParams.add(lblTagSamplings);

		txtSample = new JTextField("10");
		txtSample.setHorizontalAlignment(SwingConstants.CENTER);
		txtSample.setColumns(10);
		pnlSamplingParams.add(txtSample);

		// Output Parameters
		JPanel pnlOutputOptions = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnlOutputOptions, 10, SpringLayout.SOUTH, pnlSamplingParams);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnlOutputOptions, 0, SpringLayout.WEST, scrollPane_BAM);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnlOutputOptions, 0, SpringLayout.EAST, scrollPane_BAM);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnlOutputOptions, -10, SpringLayout.NORTH, btnCorrelate);
		contentPane.add(pnlOutputOptions);

		SpringLayout sl_OutputOptions = new SpringLayout();
		pnlOutputOptions.setLayout(sl_OutputOptions);
		TitledBorder ttlOutputOptions = BorderFactory.createTitledBorder("Output Options");
		ttlOutputOptions.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 13));
		pnlOutputOptions.setBorder(ttlOutputOptions);

		chckbxOutputStatistics = new JCheckBox("Output Statistics");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, chckbxOutputStatistics, 6, SpringLayout.NORTH, pnlOutputOptions);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, chckbxOutputStatistics, 0, SpringLayout.WEST, pnlOutputOptions);
		pnlOutputOptions.add(chckbxOutputStatistics);

		chckbxOutputStatistics.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Component[] clist = {btnOutputDirectory, lblDefaultToLocal};
				if (chckbxOutputStatistics.isSelected()) {
					for (Component c : clist) {
						c.setEnabled(true);
					}
				} else {
					for (Component c : clist) {
						c.setEnabled(false);
					}
				}
			}
		});

		btnOutputDirectory = new JButton("Output Directory");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 6, SpringLayout.SOUTH, chckbxOutputStatistics);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, btnOutputDirectory, 6, SpringLayout.WEST, pnlOutputOptions);
		btnOutputDirectory.setEnabled(false);
		pnlOutputOptions.add(btnOutputDirectory);

		btnOutputDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUT_DIR = FileSelection.getOutputDir(fc);
				if (OUT_DIR != null) {
					lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
					try {
						lblDefaultToLocal.setText("..." + ExtensionFileFilter.getSubstringEnd(OUT_DIR, 43));
					} catch (IOException e1) {
						System.err.println("Output directory may not be loaded!");
						e1.printStackTrace();
					}
				} else {
					OUT_DIR = new File(System.getProperty("user.dir"));
					lblDefaultToLocal.setText("Default to Local Directory");
				}
				lblDefaultToLocal.setToolTipText(OUT_DIR.getAbsolutePath());
			}
		});

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 2, SpringLayout.NORTH, btnOutputDirectory);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, btnOutputDirectory);
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblDefaultToLocal.setBackground(Color.WHITE);
		lblDefaultToLocal.setToolTipText("Directory path");
		lblDefaultToLocal.setEnabled(false);
		pnlOutputOptions.add(lblDefaultToLocal);

		// Disable sampling parameters for initialize state
		pnlSamplingParams.setEnabled(false);
		for (Component c : pnlSamplingParams.getComponents()) {
			c.setEnabled(false);
		}
		
		btnCorrelate.addActionListener(this);
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
		for(Component c : con.getComponents()) {
			c.setEnabled(status);
			if (c instanceof Container) {
				massXable((Container) c, status);
			}
		}
	}
}
