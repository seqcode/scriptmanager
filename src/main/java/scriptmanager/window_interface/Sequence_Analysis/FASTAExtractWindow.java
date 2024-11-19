package scriptmanager.window_interface.Sequence_Analysis;

import java.awt.Color;
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
import java.util.ArrayList;

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
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Sequence_Analysis.FASTAExtract}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Sequence_Analysis.FASTAExtract
 * @see scriptmanager.window_interface.Sequence_Analysis.FASTAExtractOutput
 */
@SuppressWarnings("serial")
public class FASTAExtractWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	final DefaultListModel<String> expList;
	ArrayList<File> BEDFiles = new ArrayList<File>();
	private File INPUT = null;
	private File OUT_DIR = null;

	private JButton btnLoad;
	private JButton btnRemoveBed;
	private JButton btnCalculate;
	private JButton btnOutput;
	private JLabel lblGenome;
	private JLabel lblDefaultToLocal;
	private JLabel lblCurrent;
	private JRadioButton rdbtnBedName;
	private JRadioButton rdbtnGenomeCoordinate;
	private JProgressBar progressBar;
	private JCheckBox chckbxStrand;
	private static JCheckBox chckbxGzipOutput;

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
				if (INPUT == null) {
					JOptionPane.showMessageDialog(null, "Genomic File Not Loaded!!!");
				} else if (BEDFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No BED Files Loaded!!!");
				} else {
					setProgress(0);
					FASTAExtractOutput output_obj = new FASTAExtractOutput(INPUT, BEDFiles, OUT_DIR,
							chckbxStrand.isSelected(), rdbtnBedName.isSelected(), chckbxGzipOutput.isSelected());

					output_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
							int temp = (Integer) propertyChangeEvent.getNewValue();
							int percentComplete = (int) (((double) (temp) / BEDFiles.size()) * 100);
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
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!");
			} catch (InterruptedException e) {
				e.printStackTrace();
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
	public FASTAExtractWindow() {
		setTitle("FASTA Extract from BED");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 380);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 140, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);

		expList = new DefaultListModel<String>();
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);

		btnLoad = new JButton("Load BED Files");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newBEDFiles = FileSelection.getFiles(fc, "bed", true);
				if (newBEDFiles != null) {
					for (int x = 0; x < newBEDFiles.length; x++) {
						BEDFiles.add(newBEDFiles[x]);
						expList.addElement(newBEDFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemoveBed = new JButton("Remove BED");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnRemoveBed, 0, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBed, -10, SpringLayout.EAST, contentPane);
		btnRemoveBed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listExp.getSelectedIndex() > -1) {
					BEDFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemoveBed);

		btnCalculate = new JButton("Calculate");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -71, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCalculate, 165, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCalculate, -165, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnCalculate, -10, SpringLayout.SOUTH, contentPane);
		contentPane.add(btnCalculate);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		btnCalculate.setActionCommand("start");

		JButton btnLoadGenome = new JButton("Load Genome FASTA");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadGenome, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadGenome, 10, SpringLayout.WEST, contentPane);
		contentPane.add(btnLoadGenome);

		lblGenome = new JLabel("No Genomic FASTA File Loaded");
		lblGenome.setFont(new Font("Dialog", Font.PLAIN, 12));
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblGenome, 10, SpringLayout.SOUTH, btnLoadGenome);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblGenome, 0, SpringLayout.WEST, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblGenome, 0, SpringLayout.EAST, contentPane);
		contentPane.add(lblGenome);

		lblCurrent = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrent, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblCurrent, -45, SpringLayout.SOUTH, contentPane);
		lblCurrent.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrent);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, lblCurrent);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrent);
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);

		btnOutput = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutput, 10, SpringLayout.SOUTH, scrollPane);
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

		chckbxGzipOutput = new JCheckBox("Output GZip");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxGzipOutput, 0, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxGzipOutput, 30, SpringLayout.WEST, contentPane);
		contentPane.add(chckbxGzipOutput);

		chckbxStrand = new JCheckBox("Force Strandedness");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxStrand, 1, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, chckbxStrand, -17, SpringLayout.WEST, btnRemoveBed);
		chckbxStrand.setSelected(true);
		contentPane.add(chckbxStrand);

		JLabel lblHeaderId = new JLabel("FASTA Header ID:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblHeaderId, 8, SpringLayout.SOUTH, lblGenome);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblHeaderId, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblHeaderId);

		rdbtnBedName = new JRadioButton("BED Name");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 6, SpringLayout.SOUTH, rdbtnBedName);
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnBedName, 6, SpringLayout.SOUTH, lblHeaderId);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnBedName, 10, SpringLayout.WEST, contentPane);
		contentPane.add(rdbtnBedName);

		rdbtnGenomeCoordinate = new JRadioButton("Genome Coordinate");
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnGenomeCoordinate, 25, SpringLayout.EAST, rdbtnBedName);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, rdbtnGenomeCoordinate, 0, SpringLayout.SOUTH, rdbtnBedName);
		contentPane.add(rdbtnGenomeCoordinate);

		ButtonGroup HeaderID = new ButtonGroup();
		HeaderID.add(rdbtnBedName);
		HeaderID.add(rdbtnGenomeCoordinate);
		rdbtnBedName.setSelected(true);

		btnLoadGenome.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File temp = FileSelection.getFile(fc, "fa");
				if (temp != null) {
					INPUT = temp;
					lblGenome.setText(INPUT.getName());
				}
			}
		});
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
		for (Component c : con.getComponents()) {
			c.setEnabled(status);
			if (c instanceof Container) {
				massXable((Container) c, status);
			}
		}
	}
}