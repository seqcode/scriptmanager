package scriptmanager.window_interface.BAM_Manipulation;

import htsjdk.samtools.SAMException;

import java.awt.Component;
import java.awt.Container;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;
import scriptmanager.cli.BAM_Manipulation.BAIIndexerCLI;
import scriptmanager.scripts.BAM_Manipulation.BAIIndexer;
import scriptmanager.cli.BAM_Manipulation.BAMRemoveDupCLI;
import scriptmanager.scripts.BAM_Manipulation.BAMMarkDuplicates;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.BAM_Manipulation.BAMMarkDuplicates}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Manipulation.BAMMarkDuplicates
 */
@SuppressWarnings("serial")
public class BAMMarkDupWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
	
	final DefaultListModel<String> expList;
	private File OUT_DIR = null;
	List<File> BAMFiles = new ArrayList<File>();

	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnSort;

	private JProgressBar progressBar;
	/**
	 * Used to run the script efficiently
	 */
	public Task task;
	private JButton btnOutput;
	private JLabel label;
	private JLabel lblDefaultToLocal;
	private JCheckBox chckbxGenerateBaiIndex;
	private JCheckBox chckbxRemoveDuplicates;

	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			try {
				setProgress(0);
				LogItem old_li = null;
			for (int x = 0; x < BAMFiles.size(); x++) {
				// Construct output filenames
				String NAME = ExtensionFileFilter.stripExtension(BAMFiles.get(x).getName());
				File OUTPUT = new File(NAME + "_dedup.bam");
				File METRICS = new File(NAME + "_dedup.metrics");
				if (OUT_DIR != null) {
					OUTPUT = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME + "_dedup.bam");
					METRICS = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME + "_dedup.metrics");
				}
				// Initialize LogItem
        	    String command = BAMRemoveDupCLI.getCLIcommand(BAMFiles.get(x), chckbxRemoveDuplicates.isSelected(), OUTPUT, METRICS);
				LogItem new_li = new LogItem(command);
				firePropertyChange("log", old_li, new_li);
				// Execute script
				BAMMarkDuplicates.mark(BAMFiles.get(x), chckbxRemoveDuplicates.isSelected(), OUTPUT, METRICS);
				// Update log item
				new_li.setStopTime(new Timestamp(new Date().getTime()));
				new_li.setStatus(0);
				old_li = new_li;
				// Generate index on output
				if (chckbxGenerateBaiIndex.isSelected()) {
					// Initialize LogItem (index BAM)
					command = BAIIndexerCLI.getCLIcommand(BAMFiles.get(x));
					old_li = new LogItem(command);
					firePropertyChange("log", old_li, new_li);
					// Execute script (index)
					BAIIndexer.generateIndex(OUTPUT);
					// Update log item
					new_li.setStopTime(new Timestamp(new Date().getTime()));
					new_li.setStatus(0);
					old_li = new_li;
				}
				// Update progress
				int percentComplete = (int) (((double)(x + 1) / BAMFiles.size()) * 100);
				setProgress(percentComplete);
				}
				
			firePropertyChange("log", old_li, null);
        	setProgress(100);
			JOptionPane.showMessageDialog(null, "Mark Duplicates Complete");

			} catch (SAMException se){
				JOptionPane.showMessageDialog(null, se.getMessage());
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
	
	/**
	 * Creates a new  BAMMarkDupWinow
	 */
	public BAMMarkDupWindow() {
		setTitle("BAM MarkDuplicates (picard)");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 360);
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
		
		btnRemoveBam = new JButton("Remove BAM");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 0, SpringLayout.NORTH, btnRemoveBam);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -5, SpringLayout.EAST, contentPane);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while(listExp.getSelectedIndex() > -1) {
					BAMFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});		
		contentPane.add(btnRemoveBam);
		
		btnSort = new JButton("Mark Duplicates");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnSort, 160, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnSort, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnSort, -160, SpringLayout.EAST, contentPane);
		contentPane.add(btnSort);
        
		btnSort.setActionCommand("start");
        btnSort.addActionListener(this);
        
		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnSort);
		sl_contentPane.putConstraint(SpringLayout.WEST, progressBar, 0, SpringLayout.WEST, btnRemoveBam);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPane);
        progressBar.setStringPainted(true);
		contentPane.add(progressBar);
		
		btnOutput = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -72, SpringLayout.NORTH, btnOutput);
		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUT_DIR = FileSelection.getOutputDir(fc);
				if(OUT_DIR != null) {
					lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
				}
			}
		});
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 146, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -50, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -146, SpringLayout.EAST, contentPane);
		contentPane.add(btnOutput);
		
		label = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.WEST, label, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, label, -30, SpringLayout.SOUTH, contentPane);
		label.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(label);
		
		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, label);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, label);
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);
		
		chckbxGenerateBaiIndex = new JCheckBox("Generate BAI Index for new BAM file");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxGenerateBaiIndex, 40, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxGenerateBaiIndex, 5, SpringLayout.WEST, contentPane);
		chckbxGenerateBaiIndex.setSelected(true);
		contentPane.add(chckbxGenerateBaiIndex);
		
		chckbxRemoveDuplicates = new JCheckBox("Remove Duplicates");
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxRemoveDuplicates, 0, SpringLayout.WEST, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, chckbxRemoveDuplicates, -6, SpringLayout.NORTH, chckbxGenerateBaiIndex);
		chckbxRemoveDuplicates.setSelected(true);
		contentPane.add(chckbxRemoveDuplicates);
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