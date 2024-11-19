package scriptmanager.window_interface.BAM_Statistics;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.SwingWorker;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import scriptmanager.charts.HeatMap;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.BAM_Statistics.BAMGenomeCorrelation}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.BAMGenomeCorrelation
 * @see scriptmanager.window_interface.BAM_Statistics.BAMGenomeCorrelationOutput
 */
@SuppressWarnings("serial")
public class BAMGenomeCorrelationWindow extends JFrame implements ActionListener, PropertyChangeListener {
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
	private JTextField txtShift;
	private JTextField txtBin;
	private JTextField txtCPU;
	private JLabel lblCurrentOutput;
	private JLabel lblDefaultToLocal;
	
	private JRadioButton rdbtnRead1;
	private JRadioButton rdbtnRead2;
	private JRadioButton rdbtnAllReads;
	private JRadioButton rdbtnMidpoint;
	
	private JRadioButton rdbtnClassicCS;
	private JRadioButton rdbtnJetLikeCS;
	
	final DefaultListModel<String> expList;
	Vector<File> BAMFiles = new Vector<File>();
	private File OUTPUT_PATH = null;
	
	private int SHIFT = 0;
	private int BIN = 10;
	private int CPU = 1;
	
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
        		if(Integer.parseInt(txtBin.getText()) < 1) {
        			JOptionPane.showMessageDialog(null, "Invalid Bin Size!!! Must be greater than 0 bp");
        		} else if(Integer.parseInt(txtCPU.getText()) < 1) {
        			JOptionPane.showMessageDialog(null, "Invalid CPU's!!! Must be greater than 0");
        		} else {
                	setProgress(0);
        			SHIFT = Integer.parseInt(txtShift.getText());
        			BIN = Integer.parseInt(txtBin.getText());
        			CPU = Integer.parseInt(txtCPU.getText());
        			
        			int READ = 0;
        			if(rdbtnRead1.isSelected()) { READ = 0; }
		        	else if(rdbtnRead2.isSelected()) { READ = 1; }
		        	else if(rdbtnAllReads.isSelected()) { READ = 2; }
		        	else if(rdbtnMidpoint.isSelected()) { READ = 3; }

					short COLORSCALE = 0;
					if (rdbtnClassicCS.isSelected()) { COLORSCALE = HeatMap.BLUEWHITERED; }
					else if (rdbtnJetLikeCS.isSelected()) { COLORSCALE = HeatMap.JETLIKE; }

					BAMGenomeCorrelationOutput output_obj = new BAMGenomeCorrelationOutput(BAMFiles, OUTPUT_PATH, chckbxOutputStatistics.isSelected(), SHIFT, BIN, CPU, READ, COLORSCALE);
					output_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
							int temp = (Integer) propertyChangeEvent.getNewValue();
							int percentComplete = (int) (((double)(temp) / (((BAMFiles.size() * BAMFiles.size()) - BAMFiles.size()) / 2)) * 100);
							setProgress(percentComplete);
						}
					});
					output_obj.addPropertyChangeListener("log", new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							firePropertyChange("log", evt.getOldValue(), evt.getNewValue());
						}
					});

					output_obj.run();
				}
			} catch(NumberFormatException nfe){
				JOptionPane.showMessageDialog(null, "Input Fields Must Contain Integers");
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
			setCursor(null); //turn off the wait cursor
		}
	}
	
	/**
	 * Creates a new instance of a BAMGenomeCorrelationWindow
	 */
	public BAMGenomeCorrelationWindow() {
		setTitle("BAM Genome Correlation");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 500);
		setMinimumSize(new Dimension(450, 450));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
	
		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -5, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -340, SpringLayout.SOUTH, contentPane);
		contentPane.add(scrollPane);
		
		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);
		
		btnLoad = new JButton("Load BAM Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, btnLoad);
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
		
		// Add Execute button
		btnCorrelate = new JButton("Correlate");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCorrelate, 171, SpringLayout.WEST, contentPane);
		contentPane.add(btnCorrelate);
		
		
		// Add Progress bar and general output objects
		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -5, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnCorrelate, -3, SpringLayout.NORTH, progressBar);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCorrelate, -18, SpringLayout.WEST, progressBar);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, progressBar, -10, SpringLayout.SOUTH, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);
		
		
		// Select Read Encoding
		JPanel pnl_ReadEncoding = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnl_ReadEncoding, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnl_ReadEncoding, 0, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnl_ReadEncoding, 0, SpringLayout.WEST, contentPane);
		contentPane.add(pnl_ReadEncoding);
		
		FlowLayout fl_ReadEncoding = new FlowLayout();
		pnl_ReadEncoding.setLayout(fl_ReadEncoding);
		pnl_ReadEncoding.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		TitledBorder ttlReadEncoding = BorderFactory.createTitledBorder("Please Select Which Read to Correlate:");
		ttlReadEncoding.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 12));
		pnl_ReadEncoding.setBorder(ttlReadEncoding);

		rdbtnRead1 = new JRadioButton("Read 1");
		pnl_ReadEncoding.add(rdbtnRead1);
		
		rdbtnRead2 = new JRadioButton("Read 2");
		pnl_ReadEncoding.add(rdbtnRead2);
		
		rdbtnAllReads = new JRadioButton("All Reads");
		pnl_ReadEncoding.add(rdbtnAllReads);
		
		rdbtnMidpoint = new JRadioButton("Midpoint (Requires PE)");
		pnl_ReadEncoding.add(rdbtnMidpoint);
		
		ButtonGroup CorrelateRead = new ButtonGroup();
		CorrelateRead.add(rdbtnRead1);
		CorrelateRead.add(rdbtnRead2);
		CorrelateRead.add(rdbtnAllReads);
		CorrelateRead.add(rdbtnMidpoint);
		rdbtnRead1.setSelected(true);

		// Add Genome Correlation Parameters
		JPanel pnl_GenomeCorrParams = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnl_GenomeCorrParams, 6, SpringLayout.SOUTH, pnl_ReadEncoding);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnl_GenomeCorrParams, 60, SpringLayout.NORTH, pnl_GenomeCorrParams);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnl_GenomeCorrParams, 0, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnl_GenomeCorrParams, 0, SpringLayout.WEST, contentPane);
		contentPane.add(pnl_GenomeCorrParams);
		
		SpringLayout sl_GenomeCorrParams = new SpringLayout();
		pnl_GenomeCorrParams.setLayout(sl_GenomeCorrParams);
		pnl_GenomeCorrParams.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		TitledBorder ttlGenomeCorrParams = BorderFactory.createTitledBorder("Genome Correlation Parameters:");
		ttlGenomeCorrParams.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 12));
		pnl_GenomeCorrParams.setBorder(ttlGenomeCorrParams);

		JLabel lblShift = new JLabel("Tag Shift (bp):");
		sl_GenomeCorrParams.putConstraint(SpringLayout.NORTH, lblShift, 10, SpringLayout.NORTH, pnl_GenomeCorrParams);
		sl_GenomeCorrParams.putConstraint(SpringLayout.WEST, lblShift, 6, SpringLayout.WEST, pnl_GenomeCorrParams);
		pnl_GenomeCorrParams.add(lblShift);

		txtShift = new JTextField();
		sl_GenomeCorrParams.putConstraint(SpringLayout.NORTH, txtShift, -2, SpringLayout.NORTH, lblShift);
		sl_GenomeCorrParams.putConstraint(SpringLayout.WEST, txtShift, 8, SpringLayout.EAST, lblShift);
		sl_GenomeCorrParams.putConstraint(SpringLayout.EAST, txtShift, 80, SpringLayout.EAST, lblShift);
		txtShift.setHorizontalAlignment(SwingConstants.CENTER);
		txtShift.setText("0");
		txtShift.setColumns(10);
		pnl_GenomeCorrParams.add(txtShift);

		JLabel lblBin = new JLabel("Bin Size (bp):");
		sl_GenomeCorrParams.putConstraint(SpringLayout.NORTH, lblBin, 0, SpringLayout.NORTH, lblShift);
		sl_GenomeCorrParams.putConstraint(SpringLayout.WEST, lblBin, 40, SpringLayout.EAST, txtShift);
		pnl_GenomeCorrParams.add(lblBin);

		txtBin = new JTextField();
		sl_GenomeCorrParams.putConstraint(SpringLayout.NORTH, txtBin, -2, SpringLayout.NORTH, lblBin);
		sl_GenomeCorrParams.putConstraint(SpringLayout.WEST, txtBin, 8, SpringLayout.EAST, lblBin);
		sl_GenomeCorrParams.putConstraint(SpringLayout.EAST, txtBin, 80, SpringLayout.EAST, lblBin);
		txtBin.setHorizontalAlignment(SwingConstants.CENTER);
		txtBin.setText("10");
		txtBin.setColumns(10);
		pnl_GenomeCorrParams.add(txtBin);

		// Select Color Scheme
		JPanel pnl_ColorScheme = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnl_ColorScheme, 6, SpringLayout.SOUTH, pnl_GenomeCorrParams);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnl_ColorScheme, 0, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnl_ColorScheme, 0, SpringLayout.WEST, contentPane);
		contentPane.add(pnl_ColorScheme);
		
		FlowLayout fl_ColorScheme = new FlowLayout();
		pnl_ColorScheme.setLayout(fl_ColorScheme);
		pnl_ColorScheme.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		TitledBorder ttlColorScheme = BorderFactory.createTitledBorder("Select Color scheme:");
		ttlColorScheme.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 12));
		pnl_ColorScheme.setBorder(ttlColorScheme);

		rdbtnClassicCS = new JRadioButton("Classic (BWR)");
		pnl_ColorScheme.add(rdbtnClassicCS);
		
		rdbtnJetLikeCS = new JRadioButton("Jet-like");
		pnl_ColorScheme.add(rdbtnJetLikeCS);
		
		ButtonGroup colorSchemeGroup = new ButtonGroup();
		colorSchemeGroup.add(rdbtnClassicCS);
		colorSchemeGroup.add(rdbtnJetLikeCS);
		rdbtnClassicCS.setSelected(true);
		
		btnCorrelate.setActionCommand("start");
		
		// Output options
		JPanel pnl_OutputOptions = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, pnl_OutputOptions, 6, SpringLayout.SOUTH, pnl_ColorScheme);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, pnl_OutputOptions, -6, SpringLayout.NORTH, btnCorrelate);
		sl_contentPane.putConstraint(SpringLayout.EAST, pnl_OutputOptions, 0, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, pnl_OutputOptions, 0, SpringLayout.WEST, contentPane);
		contentPane.add(pnl_OutputOptions);
		
		SpringLayout sl_OutputOptions = new SpringLayout();
		pnl_OutputOptions.setLayout(sl_OutputOptions);
		pnl_OutputOptions.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		TitledBorder ttlOutputOptions = BorderFactory.createTitledBorder("Output options:");
		ttlOutputOptions.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 12));
		pnl_OutputOptions.setBorder(ttlOutputOptions);
		
		// Select CPUs
		JLabel lblCpu = new JLabel("CPU's to Use:");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, lblCpu, 0, SpringLayout.NORTH, pnl_OutputOptions);
		sl_OutputOptions.putConstraint(SpringLayout.EAST, lblCpu, -100, SpringLayout.EAST, pnl_OutputOptions);
		pnl_OutputOptions.add(lblCpu);
		
		txtCPU = new JTextField();
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, txtCPU, -2, SpringLayout.NORTH, lblCpu);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, txtCPU, 8, SpringLayout.EAST, lblCpu);
		sl_OutputOptions.putConstraint(SpringLayout.EAST, txtCPU, 80, SpringLayout.EAST, lblCpu);
		txtCPU.setHorizontalAlignment(SwingConstants.CENTER);
		txtCPU.setText("1");
		pnl_OutputOptions.add(txtCPU);
		txtCPU.setColumns(10);
		
		// Output Statistics
		chckbxOutputStatistics = new JCheckBox("Output Statistics");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, chckbxOutputStatistics, 0, SpringLayout.NORTH, pnl_OutputOptions);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, chckbxOutputStatistics, 10, SpringLayout.WEST, pnl_OutputOptions);
		pnl_OutputOptions.add(chckbxOutputStatistics);
		
		chckbxOutputStatistics.addItemListener(new ItemListener() {
		      public void itemStateChanged(ItemEvent e) {
		        if(chckbxOutputStatistics.isSelected()) {
		        	btnOutputDirectory.setEnabled(true);
		        	lblCurrentOutput.setEnabled(true);
		        	lblDefaultToLocal.setEnabled(true);
		        } else {
		        	btnOutputDirectory.setEnabled(false);
		        	lblCurrentOutput.setEnabled(false);	
		        	lblDefaultToLocal.setEnabled(false);
		        }
		      }
		    });
		
		btnOutputDirectory = new JButton("Output Directory");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 6, SpringLayout.SOUTH, chckbxOutputStatistics);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, btnOutputDirectory, 120, SpringLayout.WEST, pnl_OutputOptions);
		sl_OutputOptions.putConstraint(SpringLayout.EAST, btnOutputDirectory, -120, SpringLayout.EAST, pnl_OutputOptions);
		btnOutputDirectory.setEnabled(false);
		pnl_OutputOptions.add(btnOutputDirectory);
		
		btnOutputDirectory.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
    			OUTPUT_PATH = FileSelection.getOutputDir(fc);
    			if(OUTPUT_PATH != null) {
    				lblDefaultToLocal.setText(OUTPUT_PATH.getAbsolutePath());
    			}
        	}
        });

		lblCurrentOutput = new JLabel("Current Output:");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, lblCurrentOutput, 6, SpringLayout.SOUTH, pnl_OutputOptions);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, lblCurrentOutput, 10, SpringLayout.WEST, pnl_OutputOptions);
		lblCurrentOutput.setEnabled(false);
		lblCurrentOutput.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		pnl_OutputOptions.add(lblCurrentOutput);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 6, SpringLayout.SOUTH, btnOutputDirectory);
		sl_OutputOptions.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 11, SpringLayout.EAST, lblCurrentOutput);
		sl_OutputOptions.putConstraint(SpringLayout.NORTH, lblCurrentOutput, -1, SpringLayout.NORTH, lblDefaultToLocal);
		lblDefaultToLocal.setEnabled(false);
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblDefaultToLocal.setBackground(Color.WHITE);
		pnl_OutputOptions.add(lblDefaultToLocal);
		
		
		btnCorrelate.addActionListener(this);
	}
	
	/**
	 * Checks if inputs are valid
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
	 * Invoked when task's progress changes, updating the progress bar.
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
			if(c instanceof Container) { massXable((Container)c, status); }
		}
		if(status) {
			if(!chckbxOutputStatistics.isSelected()) {
				btnOutputDirectory.setEnabled(false);
				lblCurrentOutput.setEnabled(false);
				lblDefaultToLocal.setEnabled(false);
			}
		}
	}
}

