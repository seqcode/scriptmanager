package scriptmanager.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

//import objects.PileupParameters;
//import objects.ToolDescriptions;
import scriptmanager.util.FileSelection;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.Exceptions.ScriptManagerException;

/**
 * Store, update, and manage the set of LogItems that make up a ScriptManager
 * session log with GUI management of log properties and features.
 *
 * @author Olivia Lang
 * @see scriptmanager.main.ScriptManagerGUI
 */
@SuppressWarnings("serial")
public class LogManagerWindow extends JFrame {

	private JPanel contentPane;
	private JTabbedPane tabbedPane;

	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
	private File OUT_DIR = new File(System.getProperty("user.dir"));

	private String[] column = {"Status","Command","Start Time", "Stop Time"};;

	private JPanel pnl_Settings;
	private JButton btnOutputDirectory;
	private JLabel lblDefaultToLocal;
	private JTextField txtOutputFilename;

	private JRadioButton verbosity1;
	private JRadioButton verbosity2;
	private JRadioButton verbosity3;

	private JButton btnWriteShell;

	private JPanel pnl_ViewLog;
	private JButton btnClearAll;
	private JButton btnDeleteSelected;

	private JTable tbl_Log;

	private ArrayList<LogItem> logItems;
	private Timestamp logStart;

	private File odir = null;

	private boolean on = true;

	public final LogItem[] test = { new LogItem("Some Command 1"), new LogItem("Some Command 2")};

	/**
	 * Initialize log information and window
	 */
	public LogManagerWindow() {
		// Initilize log timestamp
		logStart = new Timestamp(new Date().getTime());
		logItems = new ArrayList<LogItem>();
		initialize();
	}

	/**
	 * Initialize window components for managing log properties and content
	 */
	public void initialize() {
		setTitle("Logging Manager");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(125, 125, 650, 350);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		// Add banner notifying user of beta-feature
		JPanel bannerPane = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, bannerPane, 5, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, bannerPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, bannerPane, 10, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, bannerPane, 30, SpringLayout.NORTH, contentPane);
		bannerPane.setBackground(new Color(102, 102, 153)); // Color from MetalTheme docs (https://docs.oracle.com/javase/8/docs/api/javax/swing/plaf/metal/DefaultMetalTheme.html#getPrimary1--)
		contentPane.add(bannerPane);

		// Text of banner pane
		JLabel bannerText = new JLabel("Logging is currently a beta feature, please report any bugs to Github.");
		bannerText.setForeground(Color.WHITE);
		bannerPane.add(bannerText);

		// Set-up tabbed panel
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		sl_contentPane.putConstraint(SpringLayout.NORTH, tabbedPane, 5, SpringLayout.SOUTH, bannerPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, tabbedPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, tabbedPane, -10, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, tabbedPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(tabbedPane);

		// >>>>>>>> Settings <<<<<<<<
		pnl_Settings = new JPanel();
		pnl_Settings.setBorder(new EmptyBorder(5, 5, 5, 5));
		SpringLayout sl_pnlSettings = new SpringLayout();
		pnl_Settings.setLayout(sl_pnlSettings);
		tabbedPane.addTab("Log Settings", null, pnl_Settings, null);

		JCheckBox chckbxToggleLog = new JCheckBox("Toggle Logging");
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, chckbxToggleLog, 10, SpringLayout.NORTH, pnl_Settings);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, chckbxToggleLog, 10, SpringLayout.WEST, pnl_Settings);
		chckbxToggleLog.setSelected(on);
		chckbxToggleLog.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setToggleOn(chckbxToggleLog.isSelected());
			}
		});
		pnl_Settings.add(chckbxToggleLog);

		// Define Verbosity
		JLabel lblVerbosity = new JLabel("Verbosity:");
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, lblVerbosity, 10, SpringLayout.SOUTH, chckbxToggleLog);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, lblVerbosity, 10, SpringLayout.WEST, pnl_Settings);
		pnl_Settings.add(lblVerbosity);

		verbosity1 = new JRadioButton("1");
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, verbosity1, 0, SpringLayout.NORTH, lblVerbosity);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, verbosity1, 10, SpringLayout.EAST, lblVerbosity);
		pnl_Settings.add(verbosity1);

		verbosity2 = new JRadioButton("2");
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, verbosity2, 0, SpringLayout.NORTH, lblVerbosity);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, verbosity2, 10, SpringLayout.EAST, verbosity1);
		pnl_Settings.add(verbosity2);

		verbosity3 = new JRadioButton("3");
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, verbosity3, 0, SpringLayout.NORTH, lblVerbosity);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, verbosity3, 10, SpringLayout.EAST, verbosity2);
		pnl_Settings.add(verbosity3);

		ButtonGroup verbosity = new ButtonGroup();
		verbosity.add(verbosity1);
		verbosity.add(verbosity2);
		verbosity.add(verbosity3);
		verbosity2.setSelected(true);

		// Output Settings
		btnOutputDirectory = new JButton("Output Directory");
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 10, SpringLayout.SOUTH, lblVerbosity);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, btnOutputDirectory, 10, SpringLayout.WEST, pnl_Settings);
		pnl_Settings.add(btnOutputDirectory);

		JLabel lblCurrentOutput = new JLabel("Current Output:");
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, lblCurrentOutput, 10, SpringLayout.SOUTH, btnOutputDirectory);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, lblCurrentOutput, 0, SpringLayout.WEST, btnOutputDirectory);
		lblCurrentOutput.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		pnl_Settings.add(lblCurrentOutput);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		if(getOutputDirectory() != null) {
			lblDefaultToLocal = new JLabel(getOutputDirectory().getAbsolutePath());
		}
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 0, SpringLayout.NORTH, lblCurrentOutput);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 10, SpringLayout.EAST, lblCurrentOutput);
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblDefaultToLocal.setBackground(Color.WHITE);
		pnl_Settings.add(lblDefaultToLocal);

		btnOutputDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File temp = FileSelection.getOutputDir(fc);
				if (temp != null) {
					OUT_DIR = temp;
					lblDefaultToLocal.setText(temp.getAbsolutePath());
				}
			}
		});

		JLabel lblFileSeparator = new JLabel(File.separator);
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, lblFileSeparator, 10, SpringLayout.SOUTH, lblCurrentOutput);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, lblFileSeparator, 10, SpringLayout.WEST, pnl_Settings);
		pnl_Settings.add(lblFileSeparator);

		txtOutputFilename = new JTextField();
		sl_pnlSettings.putConstraint(SpringLayout.NORTH, txtOutputFilename, 0, SpringLayout.NORTH, lblFileSeparator);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, txtOutputFilename, 10, SpringLayout.EAST, lblFileSeparator);
		sl_pnlSettings.putConstraint(SpringLayout.EAST, txtOutputFilename, -10, SpringLayout.EAST, pnl_Settings);
		txtOutputFilename.setText(logStart.toString().replace(' ', '_').replace(':', '-') + "_logfile.sh");
		pnl_Settings.add(txtOutputFilename);
		txtOutputFilename.setColumns(100);

		btnWriteShell = new JButton("Write Log to shell script");
		sl_pnlSettings.putConstraint(SpringLayout.SOUTH, btnWriteShell, -10, SpringLayout.SOUTH, pnl_Settings);
		sl_pnlSettings.putConstraint(SpringLayout.WEST, btnWriteShell, 10, SpringLayout.WEST, pnl_Settings);
		btnWriteShell.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					writeLogShell();
				} catch (ScriptManagerException sme) {
					JOptionPane.showMessageDialog(null, sme.getMessage());
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(null, ioe.getMessage());
				}
			}
		});
		pnl_Settings.add(btnWriteShell);

		// >>>>>>>> View <<<<<<<<
		pnl_ViewLog = new JPanel();
		pnl_ViewLog.setBorder(new EmptyBorder(5, 5, 5, 5));
		SpringLayout sl_pnlViewLog = new SpringLayout();
		pnl_ViewLog.setLayout(sl_pnlViewLog);
//		setContentPane(pnl_ViewLog);
		tabbedPane.addTab("View Log", null, pnl_ViewLog, null);

		// ScrollPane to hold the JTable
		tbl_Log = new JTable(new String[0][3], column);
		updateTable();
//		tbl_Log.setBounds(30,40,200,300);

		JScrollPane scrollPane = new JScrollPane(tbl_Log);
		sl_pnlViewLog.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, pnl_ViewLog);
		sl_pnlViewLog.putConstraint(SpringLayout.NORTH, scrollPane, -50, SpringLayout.NORTH, pnl_ViewLog);
		sl_pnlViewLog.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, pnl_ViewLog);
		sl_pnlViewLog.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, pnl_ViewLog);
		pnl_ViewLog.add(scrollPane);


		btnClearAll = new JButton("Clear All");
		sl_pnlViewLog.putConstraint(SpringLayout.NORTH, btnClearAll, 5, SpringLayout.NORTH, pnl_ViewLog);
		sl_pnlViewLog.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, btnClearAll);
		sl_pnlViewLog.putConstraint(SpringLayout.EAST, btnClearAll, -10, SpringLayout.EAST, pnl_ViewLog);
		btnClearAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearLog();
				updateTable();
			}
		});
		pnl_ViewLog.add(btnClearAll);

		btnDeleteSelected = new JButton("Delete Selection");
		sl_pnlViewLog.putConstraint(SpringLayout.NORTH, btnDeleteSelected, 0, SpringLayout.NORTH, btnClearAll);
		sl_pnlViewLog.putConstraint(SpringLayout.EAST, btnDeleteSelected, -10, SpringLayout.WEST, btnClearAll);
		btnDeleteSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selection = tbl_Log.getSelectedRows();
				removeLogItems(selection);
				updateTable();
			}
		});
		pnl_ViewLog.add(btnDeleteSelected);
	}

	/**
	 * Update the JTable from the logItems ArrayList
	 */
	public void updateTable() {
		int n = logItems.size();
		String[][] d = new String[n][3];
		for(int i = 0; i<n; i++) {
			d[i] = logItems.get(i).toStringArray();
		}

		DefaultTableModel dtm = new DefaultTableModel(d, column);
		tbl_Log.setModel(dtm);
	}

	/**
	 * Regenerates the JTable from the LogInfo object
	 */
	public void drawTable() {
	}

	// Getters
	public boolean getToggleOn() { return on; }
	public ArrayList<LogItem> getLogItems() { return logItems; }
	public LogItem getLogItem(int i) { return logItems.get(i); }
	public Timestamp getLogStart() { return logStart; }
	public File getOutputDirectory() { return odir; }

	// Setters
	public void setToggleOn(boolean toggle) { on = toggle; }
	public void setLogStart(Timestamp time) { logStart = time; }
	public void setOutputDirectory(File o) { odir = o; }

	// Status - getters, setters, and Comparators
	// Comparator (timestamp)

	/**
	 * Clear empty the logItems ArrayList
	 */
	public void clearLog() {
		logItems = new ArrayList<LogItem>(0);
		logStart = new Timestamp(new Date().getTime());
	}

	/**
	 * Append LogItem to the end of the logItems ArrayList and update the JTable
	 *
	 * @param li the entry to append to logItems
	 */
	public void addLogItem(LogItem li) {
		logItems.add(li);
	}

	/**
	 * Remove LogItems from the Log indicated by an array of indices
	 *
	 * @param r_idxs index list corresponding to LogItem indices in the `logItems`
	 *               ArrayList
	 */
	public void removeLogItems(int[] r_idxs) {
		Arrays.sort(r_idxs);
		for (int i = r_idxs.length; i>0; i--) {
			logItems.remove(r_idxs[i-1]);
		}
	}

	/**
	 * Get the verbosity value from the radio buttons
	 * 
	 * @return verbosity level (1, 2, or 3)
	 */
	public short getLogVerbosity() throws ScriptManagerException {
		if (verbosity1.isSelected()) {
			return 1;
		} else if (verbosity2.isSelected()) {
			return 2;
		} else if (verbosity3.isSelected()) {
			return 3;
		}
		throw new ScriptManagerException("Unrecognized verbosity level. This should never throw");
	}

	/**
	 * Write the contents of the log as a shell script
	 *
	 * @throws IOException Invalid file or parameters
	 */
	public void writeLogShell() throws IOException, ScriptManagerException {
		// Create File & initialize stream
		PrintStream OUT = new PrintStream(new File(OUT_DIR + File.separator + txtOutputFilename.getText()));

		// Determine verbosity of output
		short verbosity = getLogVerbosity();

		// Write Header (V=1+)
		OUT.println("# VERSION\t" + ToolDescriptions.VERSION + "\tVerbosity Level:" + verbosity);
		OUT.println("SCRIPTMANAGER=/path/to/ScriptManager.jar");
		OUT.println("PICARD=/path/to/picard.jar");
		OUT.println();

		// Write LogInfo start time (V=1+)
		OUT.println("# Log start time: " + logStart.toString());

		// Add later: Sort LogItems for writing--not needed unless fireproperty adding out of order

		// Record each LogItem
		for (int i = 0; i<logItems.size(); i++) {
			LogItem item = logItems.get(i);
			OUT.println();

			if(verbosity>=2) {
				// Write LogItem start time (V=2+)
				OUT.println("# " + item.getStartTime().toString());
			}

			// Write LogItem command string (V=1+)
			if (item.getStopTime()==null) {
				OUT.print("# ");
			}
			OUT.println(item.getCommand());

			if(verbosity>=2) {
				// Write LogItem status (V=2+)
				OUT.println("# Exit status: " + item.getStatus());
				// Write LogItem end time (V=2+)
				OUT.println(item.getStopTime()==null ? "#Incomplete" : "# " + item.getStopTime().toString());
				// Write LogItem runtime (V=3+)
				if(verbosity>=3) {
					OUT.println(item.getStopTime()==null ? "# Runtime: -" : "# Runtime: " + (item.getStopTime().getTime() - item.getStartTime().getTime()));
				}
			}
			// TODO: Write Toggle Log info
		}

		// Write LogInfo write/save/end time (V=1+)
		OUT.println();
		OUT.println("# Written at " + new Timestamp(new Date().getTime()).toString());

		OUT.close();
	}
}
