package scriptmanager.window_interface.Sequence_Analysis;

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
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

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
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.cli.Sequence_Analysis.RandomizeFASTACLI;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.scripts.Sequence_Analysis.RandomizeFASTA;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Sequence_Analysis.RandomizeFASTA}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Sequence_Analysis.RandomizeFASTA
 */
@SuppressWarnings("serial")
public class RandomizeFASTAWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	final DefaultListModel<String> expList;
	ArrayList<File> FASTAFiles = new ArrayList<File>();
	private File OUT_DIR = null;

	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnCalculate;
	private JButton btnOutput;
	private JCheckBox chckbxGzipOutput;
	private JCheckBox chckbxSetSeed;
	
	private JLabel lblDefaultToLocal;
	private JLabel lblCurrent;
	private JProgressBar progressBar;
	private JTextField txtSeed;

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
				if (FASTAFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No FASTA Files Loaded!!!");
				} else {
					setProgress(0);
					LogItem old_li = null;
					for (int x = 0; x < FASTAFiles.size(); x++) {

						String OUTPUT = ExtensionFileFilter.stripExtensionIgnoreGZ(FASTAFiles.get(x)) + "_RAND.fa";
						Integer SEED = null;
						if(chckbxSetSeed.isSelected()) {
							SEED = Integer.valueOf(txtSeed.getText());
							OUTPUT = ExtensionFileFilter.stripExtensionIgnoreGZ(FASTAFiles.get(x)) + "_s" + SEED + "_RAND.fa";
						}
						OUTPUT += chckbxGzipOutput.isSelected() ? ".gz" : "";
						if (OUT_DIR != null) {
							OUTPUT = OUT_DIR + File.separator + OUTPUT;
						}
						// Initialize LogItem
						String command = RandomizeFASTACLI.getCLIcommand(FASTAFiles.get(x), new File(OUTPUT), SEED);
						LogItem new_li = new LogItem(command);
						firePropertyChange("log", old_li, new_li);

						// Execute script
						RandomizeFASTA.randomizeFASTA(FASTAFiles.get(x), new File(OUTPUT), SEED, chckbxGzipOutput.isSelected());
						// Update LogItem
						new_li.setStopTime(new Timestamp(new Date().getTime()));
						new_li.setStatus(0);
						old_li = new_li;
						firePropertyChange("log", old_li, null);

						// Update progress
						int percentComplete = (int) (((double) (x + 1) / FASTAFiles.size()) * 100);
						setProgress(percentComplete);
					}
					setProgress(100);
					JOptionPane.showMessageDialog(null, "Randomization Complete");
				}
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Invalid Seed!!!");
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
	 */
	public RandomizeFASTAWindow() {
		setTitle("Randomize FASTA File");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);

		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);

		btnLoad = new JButton("Load FASTA Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 13, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 10, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newFASTAFiles = FileSelection.getFiles(fc, "fa", true);
				if (newFASTAFiles != null) {
					for (int x = 0; x < newFASTAFiles.length; x++) {
						FASTAFiles.add(newFASTAFiles[x]);
						expList.addElement(newFASTAFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemoveBam = new JButton("Remove FASTA");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -10, SpringLayout.EAST, contentPane);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listExp.getSelectedIndex() > -1) {
					FASTAFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemoveBam);

		btnCalculate = new JButton("Calculate");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCalculate, 163, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCalculate, -167, SpringLayout.EAST, contentPane);
		contentPane.add(btnCalculate);

		chckbxGzipOutput = new JCheckBox("Output GZip");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxGzipOutput, 0, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxGzipOutput, 30, SpringLayout.WEST, contentPane);
		contentPane.add(chckbxGzipOutput);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, scrollPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		btnCalculate.setActionCommand("start");

		lblCurrent = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrent, 10, SpringLayout.WEST, contentPane);
		lblCurrent.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrent);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnCalculate, 10, SpringLayout.SOUTH, lblDefaultToLocal);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, lblCurrent);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 10, SpringLayout.EAST, lblCurrent);
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);

		btnOutput = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutput, 183, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrent, 10, SpringLayout.SOUTH, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 150, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -150, SpringLayout.EAST, contentPane);
		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUT_DIR = FileSelection.getOutputDir(fc);
				if (OUT_DIR != null) {
					lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
				}
			}
		});
		contentPane.add(btnOutput);
		btnCalculate.addActionListener(this);
		
		chckbxSetSeed = new JCheckBox("Set a random seed");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxSetSeed, 183, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, chckbxSetSeed);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutput, 10,SpringLayout.SOUTH, chckbxSetSeed);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxSetSeed, 120, SpringLayout.WEST, contentPane);
		chckbxSetSeed.setSelected(false);
		chckbxSetSeed.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(chckbxSetSeed.isSelected()) {
					txtSeed.setEnabled(true);
				} else {
					txtSeed.setEnabled(false);
				}
			}
		});
		contentPane.add(chckbxSetSeed);

		txtSeed = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtSeed, 2, SpringLayout.NORTH, chckbxSetSeed);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtSeed, 0, SpringLayout.EAST, chckbxSetSeed);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtSeed, 65, SpringLayout.EAST, chckbxSetSeed);
		sl_contentPane.putConstraint(SpringLayout.EAST, txtSeed, -120, SpringLayout.EAST, contentPane);
		txtSeed.setEnabled(false);
		txtSeed.setHorizontalAlignment(SwingConstants.CENTER);
		txtSeed.setText("0");
		contentPane.add(txtSeed);
		txtSeed.setColumns(10);
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
		for (Component c : con.getComponents()) {
			c.setEnabled(status);
			if (c instanceof Container) {
				massXable((Container) c, status);
			}
		}
	}
}