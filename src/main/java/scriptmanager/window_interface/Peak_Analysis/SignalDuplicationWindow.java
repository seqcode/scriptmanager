package scriptmanager.window_interface.Peak_Analysis;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
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

import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.FileSelection;

import scriptmanager.cli.Peak_Analysis.SignalDuplicationCLI;
import scriptmanager.scripts.Peak_Analysis.SignalDuplication;

/**
 * (Dev) Unfinished GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Peak_Analysis.SignalDuplication}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Peak_Analysis.SignalDuplication
 */
@SuppressWarnings("serial")
public class SignalDuplicationWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));	
	
	final DefaultListModel<String> expList;
	Vector<File> BAMFiles = new Vector<File>();
	private File INPUT = null;
	
	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnCalculate;
	private JLabel lblGFFFile;

	private JProgressBar progressBar;
	/**
	 * Used to run the script efficiently
	 */
	public Task task;
	private JTextField txtWindow;
	
	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
        	try {
        		if(Double.parseDouble(txtWindow.getText()) < 1) {
        			JOptionPane.showMessageDialog(null, "Invalid Bin Size!!! Must be larger than 0 bp");
        		} else if(INPUT == null) {
        			JOptionPane.showMessageDialog(null, "GFF File Not Loaded!!!");
        		} else if(BAMFiles.size() < 1) {
        			JOptionPane.showMessageDialog(null, "No BAM Files Loaded!!!");
        		} else {
					setProgress(0);
					/* Output class object needs to be created
					   LogItem creation and updates should be moved to Output class along with
					   non-script code in the current script class
					*/
					// Initialize LogItem
					String command = SignalDuplicationCLI.getCLIcommand(INPUT, BAMFiles.get(0), Double.parseDouble(txtWindow.getText()));
					LogItem li = new LogItem(command);
					// Execute script
					SignalDuplication output_obj = new SignalDuplication(INPUT, BAMFiles, Double.parseDouble(txtWindow.getText()));
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
					// Update log item
					li.setStopTime(new Timestamp(new Date().getTime()));
					li.setStatus(0);
					firePropertyChange("log", li, null);
					// Pop-up completion
					setProgress(100);
					JOptionPane.showMessageDialog(null, "Random Coordinate Generation Complete");
					// Update progress
					/* TODO */
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
			setProgress(100);
			return null;
        }
        
        public void done() {
        	massXable(contentPane, true);
            setCursor(null); //turn off the wait cursor
        }
	}
	
	public SignalDuplicationWindow() {
		setTitle("Signal Duplication");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 293);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
	
		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);
		
      	expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);
		
		btnLoad = new JButton("Load BAM Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 9, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, btnLoad);
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
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -10, SpringLayout.EAST, contentPane);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while(listExp.getSelectedIndex() > -1) {
					BAMFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});		
		contentPane.add(btnRemoveBam);
		
		btnCalculate = new JButton("Calculate");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -6, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCalculate, 166, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnCalculate, -10, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCalculate, -176, SpringLayout.EAST, contentPane);
		contentPane.add(btnCalculate);
		
		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPane);
        progressBar.setStringPainted(true);
		contentPane.add(progressBar);
		
        btnCalculate.setActionCommand("start");
        
        JButton btnLoadGFF = new JButton("Load GFF");
        sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 0, SpringLayout.WEST, btnLoadGFF);
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadGFF, 0, SpringLayout.NORTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadGFF, 5, SpringLayout.WEST, contentPane);
        contentPane.add(btnLoadGFF);
        
        lblGFFFile = new JLabel("No GFF File Loaded");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblGFFFile, 5, SpringLayout.NORTH, btnLoadGFF);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblGFFFile, 6, SpringLayout.EAST, btnLoadGFF);
        sl_contentPane.putConstraint(SpringLayout.EAST, lblGFFFile, -10, SpringLayout.EAST, contentPane);
        contentPane.add(lblGFFFile);

        btnLoadGFF.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		File temp = FileSelection.getFile(fc,"gff");
				if(temp != null) {
					INPUT = temp;
					lblGFFFile.setText(INPUT.getName());
				}
        	}
        });
        
        txtWindow = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.EAST, txtWindow, -105, SpringLayout.EAST, contentPane);
        txtWindow.setHorizontalAlignment(SwingConstants.CENTER);
        txtWindow.setText("100");
        contentPane.add(txtWindow);
        txtWindow.setColumns(10);
        
        JLabel lblSizeOfSignal = new JLabel("Size of Signal Window around Center (bp):");
        sl_contentPane.putConstraint(SpringLayout.WEST, lblSizeOfSignal, 5, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 6, SpringLayout.SOUTH, lblSizeOfSignal);
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtWindow, -2, SpringLayout.NORTH, lblSizeOfSignal);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtWindow, 6, SpringLayout.EAST, lblSizeOfSignal);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblSizeOfSignal, 6, SpringLayout.SOUTH, btnLoadGFF);
        contentPane.add(lblSizeOfSignal);
        btnCalculate.addActionListener(this);
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