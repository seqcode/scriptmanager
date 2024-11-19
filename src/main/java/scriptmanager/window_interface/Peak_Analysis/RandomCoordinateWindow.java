package scriptmanager.window_interface.Peak_Analysis;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;


import scriptmanager.cli.Peak_Analysis.RandomCoordinateCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.OptionException;
import scriptmanager.util.FileSelection;
import scriptmanager.scripts.Peak_Analysis.RandomCoordinate;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Peak_Analysis.RandomCoordinate}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Peak_Analysis.RandomCoordinate
 */
@SuppressWarnings("serial")
public class RandomCoordinateWindow extends JFrame implements ActionListener, PropertyChangeListener {

	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
	private JCheckBox chckbxGzipOutput;
	private JPanel contentPane;
	private JTextField txtSites;
	private JTextField txtSize;
	private JRadioButton rdbtnGff;
	private JRadioButton rdbtnBed;
	private String[] genomeBuilds = { "sacCer3", "sacCer3_cegr", "hg38", "hg38_contigs", "hg19", "hg19_contigs", "mm10" };
	private JComboBox<String> cmbGenome;

	private File OUT_DIR = null;

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
				if(txtSites.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "No Sites Entered!!!");
				} else if(Integer.parseInt(txtSites.getText()) < 0) {
					JOptionPane.showMessageDialog(null, "Invalid Number of Sites Entered!!!");
				} else if(txtSize.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "No Window Size Entered!!!");
				} else if(Integer.parseInt(txtSize.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid Window Size Entered!!!");
				} else {
					// Construct output filename
					String NAME = (String)cmbGenome.getSelectedItem() + "_" + Integer.parseInt(txtSites.getText()) + "SITES_" + Integer.parseInt(txtSize.getText()) + "bp"
							+ (rdbtnBed.isSelected() ? ".bed" : ".gff")
							+ (chckbxGzipOutput.isSelected() ? ".gz" : "");
					File OUT_FILEPATH = new File(NAME);
					if (OUT_DIR != null) {
						OUT_FILEPATH = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME);
					}
					// Initialize LogItem
					String command = RandomCoordinateCLI.getCLIcommand((String)cmbGenome.getSelectedItem(), OUT_FILEPATH, rdbtnBed.isSelected(), Integer.parseInt(txtSites.getText()), Integer.parseInt(txtSize.getText()), chckbxGzipOutput.isSelected());
					LogItem li = new LogItem(command);
					firePropertyChange("log", null, li);
					// Execute script
					RandomCoordinate.execute((String)cmbGenome.getSelectedItem(), OUT_FILEPATH, rdbtnBed.isSelected(), Integer.parseInt(txtSites.getText()), Integer.parseInt(txtSize.getText()), chckbxGzipOutput.isSelected());
					// Update log item
					li.setStopTime(new Timestamp(new Date().getTime()));
					li.setStatus(0);
					firePropertyChange("log", li, null);
					// Pop-up completion
					JOptionPane.showMessageDialog(null, "Random Coordinate Generation Complete");
				}
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!");
			} catch (IllegalArgumentException iae) {
				JOptionPane.showMessageDialog(null, iae.getMessage());
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
			return null;
        }
        
        public void done() {
			massXable(contentPane, true);
			setCursor(null); //turn off the wait cursor
		}
	}
	
	public RandomCoordinateWindow() {
		setTitle("Random Coordinate Generator");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 315, 340);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);	
		
		JLabel lblSiteNum = new JLabel("Enter Number of Sites:");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSiteNum, 10, SpringLayout.WEST, contentPane);
		lblSiteNum.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblSiteNum);
		
		txtSites = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtSites, -1, SpringLayout.NORTH, lblSiteNum);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtSites, 10, SpringLayout.EAST, lblSiteNum);
		txtSites.setText("1000");
		txtSites.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtSites);
		txtSites.setColumns(10);
		
		JLabel lblWindowSizebp = new JLabel("Window Size (bp):");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblWindowSizebp, 20, SpringLayout.SOUTH, lblSiteNum);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblWindowSizebp, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblWindowSizebp);
		
		txtSize = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtSize, -1, SpringLayout.NORTH, lblWindowSizebp);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtSize, 51, SpringLayout.EAST, lblWindowSizebp);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtSize, 0, SpringLayout.EAST, txtSites);
		txtSize.setHorizontalAlignment(SwingConstants.CENTER);
		txtSize.setText("200");
		contentPane.add(txtSize);
		txtSize.setColumns(10);
		
		final JLabel lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 5, SpringLayout.WEST, contentPane);
		lblDefaultToLocal.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		contentPane.add(lblDefaultToLocal);
		
		JButton btnOutputDirectory = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutputDirectory, 80, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutputDirectory, -80, SpringLayout.EAST, contentPane);
		btnOutputDirectory.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		OUT_DIR = FileSelection.getOutputDir(fc);
    			if(OUT_DIR != null) {
    				lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
    			}
        	}
        });
		contentPane.add(btnOutputDirectory);

		chckbxGzipOutput = new JCheckBox("Output GZip");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxGzipOutput, 10, SpringLayout.SOUTH, btnOutputDirectory);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxGzipOutput, 105, SpringLayout.WEST, contentPane);
		contentPane.add(chckbxGzipOutput);

		JLabel lblCurrentOutputDirectory = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentOutputDirectory, 10, SpringLayout.SOUTH, chckbxGzipOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentOutputDirectory, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 6, SpringLayout.SOUTH, lblCurrentOutputDirectory);
		lblCurrentOutputDirectory.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrentOutputDirectory);
		
		JButton btnRandom = new JButton("Randomize");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnRandom, 100, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnRandom, -5, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRandom, -100, SpringLayout.EAST, contentPane);
		contentPane.add(btnRandom);
		btnRandom.setActionCommand("start");
		btnRandom.addActionListener(this);
		
		cmbGenome = new JComboBox<String>();
		for(int x = 0; x < genomeBuilds.length; x++) { cmbGenome.addItem(genomeBuilds[x]); }
		sl_contentPane.putConstraint(SpringLayout.EAST, txtSites, 0, SpringLayout.EAST, cmbGenome);
		contentPane.add(cmbGenome);
		cmbGenome.setSelectedIndex(0);
		
		JLabel lblSelectGenome = new JLabel("Select Genome:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSiteNum, 20, SpringLayout.SOUTH, lblSelectGenome);
		sl_contentPane.putConstraint(SpringLayout.WEST, cmbGenome, 10, SpringLayout.EAST, lblSelectGenome);
		sl_contentPane.putConstraint(SpringLayout.NORTH, cmbGenome, -5, SpringLayout.NORTH, lblSelectGenome);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSelectGenome, 10, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSelectGenome, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblSelectGenome);
		
		JLabel lblFileFormatOutput = new JLabel("File format output:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblFileFormatOutput, 20, SpringLayout.SOUTH, lblWindowSizebp);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblFileFormatOutput, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblFileFormatOutput);
		
		rdbtnBed = new JRadioButton("BED");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 15, SpringLayout.SOUTH, rdbtnBed);
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnBed, -4, SpringLayout.NORTH, lblFileFormatOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnBed, 20, SpringLayout.EAST, lblFileFormatOutput);
		contentPane.add(rdbtnBed);
		
		rdbtnGff = new JRadioButton("GFF");
		sl_contentPane.putConstraint(SpringLayout.EAST, cmbGenome, 0, SpringLayout.EAST, rdbtnGff);
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnGff, -4, SpringLayout.NORTH, lblFileFormatOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnGff, 30, SpringLayout.EAST, rdbtnBed);
		contentPane.add(rdbtnGff);
		
		ButtonGroup OutputFormat = new ButtonGroup();
		OutputFormat.add(rdbtnBed);
		OutputFormat.add(rdbtnGff);
		rdbtnBed.setSelected(true);
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
		if ("log" == evt.getPropertyName()) {
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