package scriptmanager.window_interface.Peak_Calling;

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
import java.sql.Timestamp;
import java.util.Date;
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

import scriptmanager.util.FileSelection;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;

import scriptmanager.cli.Peak_Calling.GeneTrackCLI;
import scriptmanager.scripts.Peak_Calling.GeneTrack;

/**
 * (Dev) Unfinished GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Peak_Calling.GeneTrack}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Peak_Calling.GeneTrack
 * @see scriptmanager.scripts.Peak_Calling.GeneTrack_BAM
 */
@SuppressWarnings("serial")
public class GeneTrackWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));	
	
	final DefaultListModel<String> expList;
	Vector<File> IDXFiles = new Vector<File>();
	private File OUTPUT_PATH = null;
	
	private JButton btnLoad;
	private JButton btnRemoveFile;
	private JButton btnGene;

	private JProgressBar progressBar;
	/**
	 * Used to run the script efficiently
	 */
	public Task task;
	private JTextField txtSigma;
	private JTextField txtExclusion;
	private JTextField txtUp;
	private JTextField txtDown;
	private JTextField txtFilter;
	private JLabel lblUpWidth;
	private JLabel lblDownWidth;
	private JLabel lblMinimumTagsPer;
	private JCheckBox chckbxPeakWidth;
	private JButton btnOutputDirectory;
	private JLabel lblDefaultToLocal;
	
	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
        	try {
				if(Integer.parseInt(txtSigma.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Sigma!!!");
				} else if(Integer.parseInt(txtExclusion.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Exclusion!!!");
				} else if(chckbxPeakWidth.isSelected() && Integer.parseInt(txtUp.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Upstream Window Width!!!");
				} else if(chckbxPeakWidth.isSelected() && Integer.parseInt(txtDown.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Downstream Window Width!!!");
				} else if(Integer.parseInt(txtFilter.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Peak Filtering Criteria!!!");
				} else {
					int SIGMA = Integer.parseInt(txtSigma.getText());
					int EXCLUSION = Integer.parseInt(txtExclusion.getText());
					int FILTER = Integer.parseInt(txtFilter.getText());
					int UP = EXCLUSION / 2;
					int DOWN = EXCLUSION / 2;			
					if(chckbxPeakWidth.isSelected()) {
						UP = Integer.parseInt(txtUp.getText());
						DOWN = Integer.parseInt(txtDown.getText());
					}
					
					String DIRECTORY = "genetrack_s" + SIGMA + "e" + EXCLUSION;				
					if(UP != EXCLUSION / 2) DIRECTORY += "u" + UP;
					if(DOWN != EXCLUSION / 2) DIRECTORY += "d" + DOWN;
					DIRECTORY += "F" + FILTER;
					
					if(OUTPUT_PATH != null) { DIRECTORY = OUTPUT_PATH.getCanonicalPath() + File.separator + DIRECTORY; }
					new File(DIRECTORY).mkdirs();
					
					setProgress(0);
					LogItem old_li = null;
					for(int x = 0; x < IDXFiles.size(); x++) {
						// Initialize LogItem
						String command = GeneTrackCLI.getCLIcommand(IDXFiles.get(x), SIGMA, EXCLUSION, FILTER, UP, DOWN, DIRECTORY);
						LogItem new_li = new LogItem(command);
						firePropertyChange("log", old_li, new_li);
						// Execute script
		        		GeneTrack track = new GeneTrack(IDXFiles.get(x), SIGMA, EXCLUSION, FILTER, UP, DOWN, DIRECTORY);
		        		track.setVisible(true);
						track.run();
						// Update LogItem
						new_li.setStopTime(new Timestamp(new Date().getTime()));
						new_li.setStatus(0);
						old_li = new_li;
						// Update progress
						int percentComplete = (int)(((double)(x + 1) / IDXFiles.size()) * 100);
		        		setProgress(percentComplete);
		        	}
					// final update
					firePropertyChange("log", old_li, null);
					setProgress(100);
				}
			} catch(NumberFormatException nfe){
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				JOptionPane.showMessageDialog(null, "I/O issues: " + ioe.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, ToolDescriptions.UNEXPECTED_EXCEPTION_MESSAGE + e.getMessage());
			}
        	return null;
        }
        
        public void done() {
    		massXable(contentPane, true);
            setCursor(null); //turn off the wait cursor
        }
	}
	
	public GeneTrackWindow() {
		setTitle("GeneTrack");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 500, 410);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
	
		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -193, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);
		
      	expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);
		
		btnLoad = new JButton("Load scIDX Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				File[] newIDXFiles = FileSelection.getGenericFiles(fc);
				if(newIDXFiles != null) {
					for(int x = 0; x < newIDXFiles.length; x++) { 
						IDXFiles.add(newIDXFiles[x]);
						expList.addElement(newIDXFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);
		
		btnRemoveFile = new JButton("Remove File");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveFile, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveFile, -10, SpringLayout.EAST, contentPane);
		btnRemoveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while(listExp.getSelectedIndex() > -1) {
					IDXFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});		
		contentPane.add(btnRemoveFile);
		
		btnGene = new JButton("GeneTrack");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnGene, 170, SpringLayout.WEST, contentPane);
		contentPane.add(btnGene);
		
		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.EAST, btnGene, -14, SpringLayout.WEST, progressBar);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, progressBar, -5, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnGene, 0, SpringLayout.SOUTH, progressBar);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPane);
        progressBar.setStringPainted(true);
		contentPane.add(progressBar);
		
        btnGene.setActionCommand("start");
        
        txtSigma = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtSigma, 6, SpringLayout.SOUTH, scrollPane);
        txtSigma.setHorizontalAlignment(SwingConstants.CENTER);
        txtSigma.setText("5");
        contentPane.add(txtSigma);
        txtSigma.setColumns(10);
        
        txtExclusion = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.WEST, txtSigma, 0, SpringLayout.WEST, txtExclusion);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtSigma, 0, SpringLayout.EAST, txtExclusion);
        txtExclusion.setHorizontalAlignment(SwingConstants.CENTER);
        txtExclusion.setText("20");
        contentPane.add(txtExclusion);
        txtExclusion.setColumns(10);
        
        txtUp = new JTextField();
        txtUp.setEnabled(false);
        txtUp.setHorizontalAlignment(SwingConstants.CENTER);
        txtUp.setText("10");
        contentPane.add(txtUp);
        txtUp.setColumns(10);
        
        txtDown = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtDown, 0, SpringLayout.NORTH, txtUp);
        txtDown.setEnabled(false);
        txtDown.setHorizontalAlignment(SwingConstants.CENTER);
        txtDown.setText("10");
        contentPane.add(txtDown);
        txtDown.setColumns(10);
        
        txtFilter = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtFilter, 0, SpringLayout.NORTH, txtSigma);
        txtFilter.setHorizontalAlignment(SwingConstants.CENTER);
        txtFilter.setText("1");
        contentPane.add(txtFilter);
        txtFilter.setColumns(10);
        
        JLabel lblSigma = new JLabel("Sigma (-s):");
        sl_contentPane.putConstraint(SpringLayout.WEST, lblSigma, 20, SpringLayout.WEST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblSigma, 2, SpringLayout.NORTH, txtSigma);
        lblSigma.setToolTipText("Sigma to use when smoothing reads to call peaks");
        contentPane.add(lblSigma);
        
        JLabel lblExclusion = new JLabel("Exclusion Zone (-e):");
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtExclusion, -2, SpringLayout.NORTH, lblExclusion);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblExclusion, 15, SpringLayout.SOUTH, lblSigma);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtExclusion, 20, SpringLayout.EAST, lblExclusion);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtExclusion, 80, SpringLayout.EAST, lblExclusion);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblExclusion, 0, SpringLayout.WEST, lblSigma);
        lblExclusion.setToolTipText("Exclusion zone around each peak that prevents others from being called");
        contentPane.add(lblExclusion);
        
        lblUpWidth = new JLabel("Up Width:");
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtUp, -2, SpringLayout.NORTH, lblUpWidth);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtUp, 20, SpringLayout.EAST, lblUpWidth);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtUp, 80, SpringLayout.EAST, lblUpWidth);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblUpWidth, 0, SpringLayout.WEST, btnGene);
        lblUpWidth.setEnabled(false);
        lblUpWidth.setToolTipText("Upstream width of called peaks (Default uses half exclusion)");
        contentPane.add(lblUpWidth);
        
        lblDownWidth = new JLabel("Down Width:");
        sl_contentPane.putConstraint(SpringLayout.WEST, txtDown, 20, SpringLayout.EAST, lblDownWidth);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtDown, 80, SpringLayout.EAST, lblDownWidth);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblDownWidth, 2, SpringLayout.NORTH, txtUp);
        lblDownWidth.setEnabled(false);
        lblDownWidth.setToolTipText("Downstream width of called peaks (Default uses half exclusion)");
        contentPane.add(lblDownWidth);
        
        lblMinimumTagsPer = new JLabel("Min Tags per Peak (-F):");
        sl_contentPane.putConstraint(SpringLayout.EAST, lblDownWidth, 0, SpringLayout.EAST, lblMinimumTagsPer);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtFilter, 20, SpringLayout.EAST, lblMinimumTagsPer);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtFilter, 80, SpringLayout.EAST, lblMinimumTagsPer);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblMinimumTagsPer, 2, SpringLayout.NORTH, txtSigma);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblMinimumTagsPer, 23, SpringLayout.EAST, txtSigma);
        lblMinimumTagsPer.setToolTipText("Absolute read filter; outputs only peaks with larger read count");
        contentPane.add(lblMinimumTagsPer);
        
        chckbxPeakWidth = new JCheckBox("Set Peak Width");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblUpWidth, 4, SpringLayout.NORTH, chckbxPeakWidth);
        sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxPeakWidth, 15, SpringLayout.SOUTH, lblExclusion);
        sl_contentPane.putConstraint(SpringLayout.WEST, chckbxPeakWidth, 0, SpringLayout.WEST, lblSigma);
        chckbxPeakWidth.setToolTipText("Default Peak Width is Half the Exclusion Zone");
        contentPane.add(chckbxPeakWidth);
               
        btnOutputDirectory = new JButton("Output Directory");
        sl_contentPane.putConstraint(SpringLayout.WEST, btnOutputDirectory, 175, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutputDirectory, -65, SpringLayout.SOUTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.EAST, btnOutputDirectory, -175, SpringLayout.EAST, contentPane);
        contentPane.add(btnOutputDirectory);
        
        JLabel lblCurrentOutput = new JLabel("Current Output:");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentOutput, 46, SpringLayout.SOUTH, chckbxPeakWidth);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentOutput, 0, SpringLayout.WEST, scrollPane);
        contentPane.add(lblCurrentOutput);
        
        lblDefaultToLocal = new JLabel("Default to Local Directory");
        sl_contentPane.putConstraint(SpringLayout.EAST, lblDefaultToLocal, -10, SpringLayout.EAST, contentPane);
        lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
        sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrentOutput);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, lblDefaultToLocal, 0, SpringLayout.SOUTH, lblCurrentOutput);
        contentPane.add(lblDefaultToLocal);
        
        btnOutputDirectory.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	OUTPUT_PATH = FileSelection.getOutputDir(fc);
				if(OUTPUT_PATH != null) {
					lblDefaultToLocal.setText(OUTPUT_PATH.getAbsolutePath());
				}
			}
		});
        
        btnGene.addActionListener(this);
        chckbxPeakWidth.addItemListener(new ItemListener() {
		      public void itemStateChanged(ItemEvent e) {
		        if(chckbxPeakWidth.isSelected()) {
		        	txtUp.setEnabled(true);
		        	txtDown.setEnabled(true);
		        	lblUpWidth.setEnabled(true);
		        	lblDownWidth.setEnabled(true);

		        } else {
		        	txtUp.setEnabled(false);
		        	txtDown.setEnabled(false);
		        	lblUpWidth.setEnabled(false);
		        	lblDownWidth.setEnabled(false);
		        }
		      }
		    }); 
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
			if(c instanceof Container) { massXable((Container)c, status); }
		}
	}
}


	
