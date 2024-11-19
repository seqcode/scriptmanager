package scriptmanager.window_interface.Peak_Calling;

import java.awt.Font;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
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

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import scriptmanager.util.FileSelection;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;

import scriptmanager.cli.Peak_Calling.PeakPairCLI;
import scriptmanager.scripts.Peak_Calling.PeakPair;

/**
 * (Dev) Unfinished GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Peak_Calling.PeakPair}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Peak_Calling.PeakPair
 */
@SuppressWarnings("serial")
public class PeakPairWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));	
	
	final DefaultListModel<String> expList;
	Vector<File> BAMFiles = new Vector<File>();
	
	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnPeak;

	private JProgressBar progressBar;
	/**
	 * Used to run the script efficiently
	 */
	public Task task;
	
	private JRadioButton rdbtnMode;
	private JRadioButton rdbtnClosest;
	private JRadioButton rdbtnLargest;
	private JRadioButton rdbtnAll;
	private JRadioButton rdbtnRelativeThreshold;
	private JRadioButton rdbtnAbsoluteThreshold;
	
	private JTextField txtUp;
	private JTextField txtDown;
	private JTextField txtBin;
	private JTextField txtRel;
	private JTextField txtAbs;
	
	private JLabel lblBinSizebp;
	private JLabel lblSort;
		
	private JComboBox<String> cmboChrom;
	private JComboBox<String> cmboScore;
	private JLabel lblScore;
	
	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
        	try {
				if(Integer.parseInt(txtUp.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Upstream Distance!!!");
				} else if(Integer.parseInt(txtDown.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Downstream Distance!!!");
				} else if(Integer.parseInt(txtBin.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Binning Size!!!");
				} else if(rdbtnAbsoluteThreshold.isSelected() && Integer.parseInt(txtAbs.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Absolute Threshold Size!!!");
				} else if(rdbtnRelativeThreshold.isSelected() && (Integer.parseInt(txtRel.getText()) < 0 || Integer.parseInt(txtRel.getText()) > 100)) {
					JOptionPane.showMessageDialog(null, "Invalid Relative Threshold Size!!!");
				} else {
					int MODE = 0;
					if(rdbtnMode.isSelected()) MODE = 0;
					else if(rdbtnClosest.isSelected()) MODE = 1;
					else if(rdbtnLargest.isSelected()) MODE = 2;
					else if(rdbtnAll.isSelected()) MODE = 3;

					int UP = Integer.parseInt(txtUp.getText());
					int DOWN = Integer.parseInt(txtDown.getText());
					int BIN = Integer.parseInt(txtBin.getText());
					
					int ABS = -999;
					int REL = -999;
					if(rdbtnAbsoluteThreshold.isSelected()) ABS = Integer.parseInt(txtAbs.getText());
					if(rdbtnRelativeThreshold.isSelected()) REL = Integer.parseInt(txtRel.getText());
					
					setProgress(0);
					LogItem old_li = null;
					for(int x = 0; x < BAMFiles.size(); x++) {
						// Initialize LogItem
						String command = PeakPairCLI.getCLIcommand(BAMFiles.get(x), MODE, UP, DOWN, BIN, ABS, REL);
						LogItem new_li = new LogItem(command);
						firePropertyChange("log", old_li, new_li);
						// Execute script
		        		PeakPair peak = new PeakPair(BAMFiles.get(x), MODE, UP, DOWN, BIN, ABS, REL);
		        		peak.setVisible(true);
		        		peak.run();
						// Update LogItem
						new_li.setStopTime(new Timestamp(new Date().getTime()));
						new_li.setStatus(0);
						old_li = new_li;
						// Update progress
		        		int percentComplete = (int)(((double)(x + 1) / BAMFiles.size()) * 100);
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
	
	public PeakPairWindow() {
		setTitle("Peak Pairing");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 500, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
	
		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -279, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);
		
      	expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);
		
		btnLoad = new JButton("Load GeneTrack Peaks");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 5, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 11, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
				File[] newGFFFiles = FileSelection.getFiles(fc,"gff");
				if(newGFFFiles != null) {
					for(int x = 0; x < newGFFFiles.length; x++) { 
						BAMFiles.add(newGFFFiles[x]);
						expList.addElement(newGFFFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);
		
		btnRemoveBam = new JButton("Remove Peak Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, 0, SpringLayout.EAST, scrollPane);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while(listExp.getSelectedIndex() > -1) {
					BAMFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});		
		contentPane.add(btnRemoveBam);
		
		btnPeak = new JButton("Peak Pair");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnPeak, 171, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnPeak, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnPeak, -171, SpringLayout.EAST, contentPane);
		contentPane.add(btnPeak);
		
		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnPeak);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, scrollPane);
        progressBar.setStringPainted(true);
		contentPane.add(progressBar);
		
        btnPeak.setActionCommand("start");
        
        rdbtnMode = new JRadioButton("Mode");
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnMode, 41, SpringLayout.WEST, contentPane);
        contentPane.add(rdbtnMode);
        
        rdbtnClosest = new JRadioButton("Closest");
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnMode, 0, SpringLayout.NORTH, rdbtnClosest);
        contentPane.add(rdbtnClosest);
        
        rdbtnLargest = new JRadioButton("Largest");
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnLargest, 28, SpringLayout.SOUTH, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnLargest, 53, SpringLayout.EAST, rdbtnClosest);
        contentPane.add(rdbtnLargest);
        
        rdbtnAll = new JRadioButton("All");
        rdbtnAll.setEnabled(false);
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnAll, 0, SpringLayout.NORTH, rdbtnMode);
        sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnAll, -33, SpringLayout.EAST, contentPane);
        contentPane.add(rdbtnAll);
        
        ButtonGroup METHOD = new ButtonGroup();
        METHOD.add(rdbtnMode);
        METHOD.add(rdbtnClosest);
        METHOD.add(rdbtnLargest);
        METHOD.add(rdbtnAll);
        rdbtnMode.setSelected(true);
        
        JLabel lblMethodOfPeak = new JLabel("Method of Peak Pairing:");
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnClosest, 6, SpringLayout.SOUTH, lblMethodOfPeak);
        lblMethodOfPeak.setToolTipText("Method of finding match");
        lblMethodOfPeak.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblMethodOfPeak, 6, SpringLayout.SOUTH, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblMethodOfPeak, 0, SpringLayout.WEST, scrollPane);
        contentPane.add(lblMethodOfPeak);
        
        JLabel lblUpstreamDistance = new JLabel("Upstream Distance (bp):");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblUpstreamDistance, 16, SpringLayout.SOUTH, rdbtnMode);
        lblUpstreamDistance.setToolTipText("Distance upstream of a Watson peak to allow a Crick pair.");
        lblUpstreamDistance.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        sl_contentPane.putConstraint(SpringLayout.WEST, lblUpstreamDistance, 10, SpringLayout.WEST, contentPane);
        contentPane.add(lblUpstreamDistance);
        
        txtUp = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnClosest, 0, SpringLayout.EAST, txtUp);
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtUp, -1, SpringLayout.NORTH, lblUpstreamDistance);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtUp, 9, SpringLayout.EAST, lblUpstreamDistance);
        txtUp.setHorizontalAlignment(SwingConstants.CENTER);
        txtUp.setText("50");
        contentPane.add(txtUp);
        txtUp.setColumns(10);
        
        JLabel lblDownstreamDistance = new JLabel("Downstream Distance (bp):");
        sl_contentPane.putConstraint(SpringLayout.EAST, txtUp, -6, SpringLayout.WEST, lblDownstreamDistance);
        sl_contentPane.putConstraint(SpringLayout.EAST, lblDownstreamDistance, -70, SpringLayout.EAST, contentPane);
        lblDownstreamDistance.setToolTipText("Distance downstream of a Watson peak to allow a Crick pair");
        lblDownstreamDistance.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblDownstreamDistance, 0, SpringLayout.NORTH, lblUpstreamDistance);
        contentPane.add(lblDownstreamDistance);
        
        txtDown = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtDown, -1, SpringLayout.NORTH, lblUpstreamDistance);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtDown, 10, SpringLayout.EAST, lblDownstreamDistance);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtDown, 0, SpringLayout.EAST, scrollPane);
        txtDown.setHorizontalAlignment(SwingConstants.CENTER);
        txtDown.setText("100");
        contentPane.add(txtDown);
        txtDown.setColumns(10);
        
        lblBinSizebp = new JLabel("Bin Size (bp):");
        sl_contentPane.putConstraint(SpringLayout.WEST, lblBinSizebp, 0, SpringLayout.WEST, scrollPane);
        lblBinSizebp.setToolTipText("Width of bins for frequency plots and mode calculations");
        lblBinSizebp.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblBinSizebp);
        
        txtBin = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtBin, -1, SpringLayout.NORTH, lblBinSizebp);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtBin, 86, SpringLayout.EAST, lblBinSizebp);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtBin, -257, SpringLayout.EAST, contentPane);
        txtBin.setHorizontalAlignment(SwingConstants.CENTER);
        txtBin.setText("1");
        contentPane.add(txtBin);
        txtBin.setColumns(10);
        
        rdbtnRelativeThreshold = new JRadioButton("Relative Threshold");
        rdbtnRelativeThreshold.setToolTipText("Percentage of the 95 percentile value to filter below.");
        contentPane.add(rdbtnRelativeThreshold);
        
        rdbtnAbsoluteThreshold = new JRadioButton("Absolute Threshold");
        rdbtnAbsoluteThreshold.setToolTipText("Absolute value to filter below");
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnRelativeThreshold, 0, SpringLayout.NORTH, rdbtnAbsoluteThreshold);
        sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnAbsoluteThreshold, 0, SpringLayout.WEST, scrollPane);
        contentPane.add(rdbtnAbsoluteThreshold);
        
        ButtonGroup FILTER = new ButtonGroup();
        FILTER.add(rdbtnRelativeThreshold);
        FILTER.add(rdbtnAbsoluteThreshold);
        rdbtnAbsoluteThreshold.setSelected(true);
        
        rdbtnRelativeThreshold.addItemListener(new ItemListener() {
		      public void itemStateChanged(ItemEvent e) {
		        if(rdbtnRelativeThreshold.isSelected()) {
		        	txtRel.setEnabled(true);
		        	txtAbs.setEnabled(false);
		        }
		      }
		    });
        rdbtnAbsoluteThreshold.addItemListener(new ItemListener() {
		      public void itemStateChanged(ItemEvent e) {
		        if(rdbtnAbsoluteThreshold.isSelected()) {
		        	txtAbs.setEnabled(true);
		        	txtRel.setEnabled(false);
		        }
		      }
		    });
        
        txtRel = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.EAST, rdbtnRelativeThreshold, -23, SpringLayout.WEST, txtRel);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtRel, -15, SpringLayout.EAST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtRel, 2, SpringLayout.NORTH, rdbtnRelativeThreshold);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtRel, 0, SpringLayout.WEST, txtDown);
        txtRel.setEnabled(false);
        txtRel.setHorizontalAlignment(SwingConstants.CENTER);
        txtRel.setText("0");
        contentPane.add(txtRel);
        txtRel.setColumns(10);
        
        txtAbs = new JTextField();
        sl_contentPane.putConstraint(SpringLayout.NORTH, txtAbs, 2, SpringLayout.NORTH, rdbtnRelativeThreshold);
        sl_contentPane.putConstraint(SpringLayout.WEST, txtAbs, 29, SpringLayout.EAST, rdbtnAbsoluteThreshold);
        sl_contentPane.putConstraint(SpringLayout.EAST, txtAbs, 0, SpringLayout.EAST, txtUp);
        txtAbs.setHorizontalAlignment(SwingConstants.CENTER);
        txtAbs.setText("0");
        contentPane.add(txtAbs);
        txtAbs.setColumns(10);
        
        lblSort = new JLabel("Sort by:");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblSort, 6, SpringLayout.SOUTH, rdbtnAbsoluteThreshold);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblSort, 0, SpringLayout.WEST, scrollPane);
        lblSort.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblSort);
        
        String[] chromSort = {"ascending", "descending"};
        cmboChrom = new JComboBox<String>(chromSort);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, cmboChrom, -6, SpringLayout.NORTH, btnPeak);
        contentPane.add(cmboChrom);
        
        String[] scoreSort = {"ascending", "descending", "none"};
        cmboScore = new JComboBox<String>(scoreSort);
        sl_contentPane.putConstraint(SpringLayout.WEST, cmboScore, 316, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.EAST, cmboScore, -44, SpringLayout.EAST, contentPane);
        cmboScore.setSelectedIndex(2);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, cmboScore, -6, SpringLayout.NORTH, btnPeak);
        contentPane.add(cmboScore);
                
        JLabel lblFilterBy = new JLabel("Filter by:");
        sl_contentPane.putConstraint(SpringLayout.SOUTH, lblBinSizebp, -18, SpringLayout.NORTH, lblFilterBy);
        sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnAbsoluteThreshold, 12, SpringLayout.SOUTH, lblFilterBy);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, lblFilterBy, -126, SpringLayout.SOUTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblFilterBy, 0, SpringLayout.WEST, scrollPane);
        lblFilterBy.setFont(new Font("Lucida Grande", Font.BOLD, 13));
        contentPane.add(lblFilterBy);
        
        JLabel lblChromosome = new JLabel("Chromosome:");
        sl_contentPane.putConstraint(SpringLayout.EAST, lblChromosome, -383, SpringLayout.EAST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, cmboChrom, 6, SpringLayout.EAST, lblChromosome);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblChromosome, 5, SpringLayout.NORTH, cmboChrom);
        lblChromosome.setToolTipText("Output files will be sorted by chromosome");
        contentPane.add(lblChromosome);
        
        lblScore = new JLabel("Score:");
        sl_contentPane.putConstraint(SpringLayout.EAST, cmboChrom, -25, SpringLayout.WEST, lblScore);
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblScore, 5, SpringLayout.NORTH, cmboChrom);
        sl_contentPane.putConstraint(SpringLayout.EAST, lblScore, -6, SpringLayout.WEST, cmboScore);
        lblScore.setToolTipText("Output files will be sorted by score");
        contentPane.add(lblScore);
        btnPeak.addActionListener(this);
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
		if(status && rdbtnRelativeThreshold.isSelected()) {
			txtRel.setEditable(true);
			txtAbs.setEnabled(false);
		}
		if(status && rdbtnAbsoluteThreshold.isSelected()) {
			txtRel.setEditable(false);
			txtAbs.setEnabled(true);
		}
	}
}