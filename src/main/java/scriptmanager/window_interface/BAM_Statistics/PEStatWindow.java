package scriptmanager.window_interface.BAM_Statistics;

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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.BAM_Statistics.PEStats}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.PEStats
 * @see scriptmanager.window_interface.BAM_Statistics.PEStatOutput
 */
@SuppressWarnings("serial")
public class PEStatWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));	
	private JCheckBox chckbxOutputStatistics;
	private JCheckBox chckbxDup;
	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnOutputDirectory;
	private JButton btnRun;
	private JTextField txtMin;
	private JTextField txtMax;
	private JLabel lblCurrentOutput;
	private JLabel lblDefaultToLocal;
	
	final DefaultListModel<String> expList;
	Vector<File> BAMFiles = new Vector<File>();
	private File OUT_DIR = new File(System.getProperty("user.dir"));
	
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
			setProgress(0);
			try {
				int MIN = Integer.parseInt(txtMin.getText());
				int MAX = Integer.parseInt(txtMax.getText());
				if(MIN < 0) {
					JOptionPane.showMessageDialog(null, "Invalid minimum value!!! Must be integer greater than or equal to 0");
				} else if(MAX < MIN) {
					JOptionPane.showMessageDialog(null, "Invalid maximum value!!! Must be greater than minimum");
				} else if (expList.size() < 1) {
					JOptionPane.showMessageDialog(null, "Must load at least one BAM file");
				} else {
					PEStatOutput output_obj = new PEStatOutput(BAMFiles, OUT_DIR, chckbxOutputStatistics.isSelected(), chckbxDup.isSelected(), MIN, MAX);
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
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Input Fields Must Contain Integers");
			} catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
				JOptionPane.showMessageDialog(null, "Could not find file: " + fnfe.getMessage());
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
	public PEStatWindow() {
		setTitle("Paired-End BAM File Statistics");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
	
		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -5, SpringLayout.EAST, contentPane);
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
		
		chckbxOutputStatistics = new JCheckBox("Output Statistics");
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxOutputStatistics, 0, SpringLayout.WEST, scrollPane);
		contentPane.add(chckbxOutputStatistics);
		
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
		
		btnRun = new JButton("Run");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnRun, 171, SpringLayout.WEST, contentPane);
		contentPane.add(btnRun);
		
		btnRun.addActionListener(this);
		btnRun.setActionCommand("start");
		
		JLabel lblHistogramRange = new JLabel("Histogram Range:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblHistogramRange, 201, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, lblHistogramRange);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblHistogramRange, 5, SpringLayout.WEST, contentPane);
		contentPane.add(lblHistogramRange);
		
		JLabel lblMin = new JLabel("Min:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblMin, 10, SpringLayout.SOUTH, lblHistogramRange);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblMin, 0, SpringLayout.WEST, scrollPane);
		contentPane.add(lblMin);
		
		txtMin = new JTextField();
		txtMin.setHorizontalAlignment(SwingConstants.CENTER);
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtMin, -2, SpringLayout.NORTH, lblMin);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtMin, 6, SpringLayout.EAST, lblMin);
		txtMin.setText("0");
		contentPane.add(txtMin);
		txtMin.setColumns(10);
		
		JLabel lblMax = new JLabel("Max:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblMax, 0, SpringLayout.NORTH, lblMin);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblMax, 114, SpringLayout.EAST, txtMin);
		contentPane.add(lblMax);
		
		txtMax = new JTextField();
		txtMax.setHorizontalAlignment(SwingConstants.CENTER);
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtMax, -2, SpringLayout.NORTH, lblMin);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtMax, 6, SpringLayout.EAST, lblMax);
		txtMax.setText("1000");
		contentPane.add(txtMax);
		txtMax.setColumns(10);
		
		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, progressBar, -8, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -5, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRun, -3, SpringLayout.NORTH, progressBar);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRun, -18, SpringLayout.WEST, progressBar);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);
		
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
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 35, SpringLayout.SOUTH, txtMin);
		btnOutputDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File temp = FileSelection.getOutputDir(fc);
				if(temp != null) {
					OUT_DIR = temp;
					lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
				}
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxOutputStatistics, 1, SpringLayout.NORTH, btnOutputDirectory);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutputDirectory, 150, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutputDirectory, -150, SpringLayout.EAST, contentPane);
		contentPane.add(btnOutputDirectory);
		btnOutputDirectory.setEnabled(false);

		lblCurrentOutput = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentOutput, 0, SpringLayout.WEST, scrollPane);
		lblCurrentOutput.setEnabled(false);
		lblCurrentOutput.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrentOutput);
		
		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 10, SpringLayout.SOUTH, btnOutputDirectory);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 11, SpringLayout.EAST, lblCurrentOutput);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentOutput, -1, SpringLayout.NORTH, lblDefaultToLocal);
		lblDefaultToLocal.setEnabled(false);
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);
		
		chckbxDup = new JCheckBox("Calculate duplication statistics");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, chckbxDup, -8, SpringLayout.NORTH, chckbxOutputStatistics);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxDup, 0, SpringLayout.WEST, scrollPane);
		contentPane.add(chckbxDup);
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


	
