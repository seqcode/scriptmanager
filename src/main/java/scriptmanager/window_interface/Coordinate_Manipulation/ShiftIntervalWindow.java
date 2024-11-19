package scriptmanager.window_interface.Coordinate_Manipulation;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.border.EmptyBorder;
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
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import scriptmanager.cli.Coordinate_Manipulation.GFF_Manipulation.SortGFFCLI;
import scriptmanager.cli.Coordinate_Manipulation.ShiftCoordCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.ToolDescriptions;
import scriptmanager.scripts.Coordinate_Manipulation.ShiftCoord;
import scriptmanager.util.ExtensionFileFilter;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Coordinate_Manipulation.ShiftCoord}
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.Coordinate_Manipulation.ShiftCoord
 */
@SuppressWarnings("serial")
public class ShiftIntervalWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	private JProgressBar progressBar;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	private File OUT_DIR = null;
	final DefaultListModel<String> expBEDList;
	final DefaultListModel<String> expGFFList;
	ArrayList<File> BEDFiles = new ArrayList<File>();
	ArrayList<File> GFFFiles = new ArrayList<File>();

	private JButton btnLoadBED;
	private JButton btnRemoveBED;
	private JButton btnLoadGFF;
	private JButton btnRemoveGFF;

	private JButton btnExecute;

	/**
	 * Used to run the script efficiently
	 */
	public Task task;
	private JLabel lblCurrent;
	private JLabel lblDefaultToLocal;
	private JButton btnOutput;
	private JTextField txtShift;

	private JRadioButton rdbtnBed;
	private JRadioButton rdbtnGff;

	private static JCheckBox chckbxGzipOutput;
	private static JCheckBox chckbxStranded;

	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			try {
				int SHIFT = Integer.parseInt(txtShift.getText());
				if (SHIFT == 0) {
					JOptionPane.showMessageDialog(null, "Shift must be non-zero (0 shift would write an identical file)");
				} else if (rdbtnBed.isSelected()) {
					setProgress(0);
					LogItem old_li = null;
					// Process each file
					for (int x = 0; x < BEDFiles.size(); x++) {
						// Save current BED to temp variable
						File XBED = BEDFiles.get(x);
						System.out.println("Input: " + XBED.getName());
						// Set suffix format
						String SUFFIX = SHIFT < 0 ? "_shift" + txtShift.getText() + "bp.bed" : "_shift+" + txtShift.getText() + "bp.bed";
						SUFFIX += chckbxGzipOutput.isSelected() ? ".gz" : "";
						// Set output filepath with name and output directory
						String OUTPUT = ExtensionFileFilter.stripExtensionIgnoreGZ(XBED);
						if (OUT_DIR != null) {
							OUTPUT = OUT_DIR + File.separator + OUTPUT;
						}
						// Initialize LogItem
						String command = ShiftCoordCLI.getCLIcommand(XBED, new File(OUTPUT + SUFFIX), SHIFT, chckbxStranded.isSelected(), chckbxGzipOutput.isSelected(), false);
						LogItem new_li = new LogItem(command);
						firePropertyChange("log", old_li, new_li);
						// Execute script
						ShiftCoord.shiftBEDInterval(XBED, new File(OUTPUT + SUFFIX), SHIFT, chckbxStranded.isSelected(), chckbxGzipOutput.isSelected());
						// Update log item
						new_li.setStopTime(new Timestamp(new Date().getTime()));
						new_li.setStatus(0);
						old_li = new_li;
						// Update progress bar
						int percentComplete = (int) (((double) (x + 1) / BEDFiles.size()) * 100);
						setProgress(percentComplete);
					}
					// final log and progress update
					firePropertyChange("log", old_li, null);
					setProgress(100);
					// Pop-up complete
					JOptionPane.showMessageDialog(null, "Shift Complete");
				} else {
					setProgress(0);
					LogItem old_li = null;
					// Process each file
					for (int x = 0; x < GFFFiles.size(); x++) {
						// Save current BED to temp variable
						File XGFF = GFFFiles.get(x);
						System.out.println("Input: " + XGFF.getName());
						// Set suffix format
						String SUFFIX = SHIFT < 0 ? "_shift" + txtShift.getText() + "bp.gff" : "_shift+" + txtShift.getText() + "bp.gff";
						SUFFIX += chckbxGzipOutput.isSelected() ? ".gz" : "";
						// Set output filepath with name and output directory
						String OUTPUT = ExtensionFileFilter.stripExtensionIgnoreGZ(XGFF);
						if (OUT_DIR != null) {
							OUTPUT = OUT_DIR + File.separator + OUTPUT;
						}
						// Initialize LogItem
						String command = ShiftCoordCLI.getCLIcommand(XGFF, new File(OUTPUT + SUFFIX), SHIFT, chckbxStranded.isSelected(), chckbxGzipOutput.isSelected(), true);
						LogItem new_li = new LogItem(command);
						firePropertyChange("log", old_li, new_li);
						// Execute script
						ShiftCoord.shiftGFFInterval(XGFF, new File(OUTPUT + SUFFIX), SHIFT, chckbxStranded.isSelected(), chckbxGzipOutput.isSelected());
						// Update log item
						new_li.setStopTime(new Timestamp(new Date().getTime()));
						new_li.setStatus(0);
						old_li = new_li;
						// Update progress bar
						int percentComplete = (int) (((double) (x + 1) / GFFFiles.size()) * 100);
						setProgress(percentComplete);
					}
					// final log and progress update
					firePropertyChange("log", old_li, null);
					setProgress(100);
					// Pop-up complete
					JOptionPane.showMessageDialog(null, "Shift Complete");
				}
			} catch (NumberFormatException nfe) {
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
			setCursor(null); // turn off the wait cursor
		}
	}

	/**
	 * Instantiate window with graphical interface design.
	 */
	public ShiftIntervalWindow() {
		setTitle("Shift Coordinate Interval");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 375);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);

		rdbtnBed = new JRadioButton("BED input");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnBed, 6, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnBed, 120, SpringLayout.WEST, contentPane);
		contentPane.add(rdbtnBed);

		rdbtnGff = new JRadioButton("GFF input");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnGff, 0, SpringLayout.NORTH, rdbtnBed);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnGff, 10, SpringLayout.EAST, rdbtnBed);
		contentPane.add(rdbtnGff);

		ButtonGroup InputType = new ButtonGroup();
		InputType.add(rdbtnBed);
		InputType.add(rdbtnGff);
		rdbtnBed.setSelected(true);

		JPanel inputCards = new JPanel(new CardLayout());
		sl_contentPane.putConstraint(SpringLayout.NORTH, inputCards, 0, SpringLayout.SOUTH, rdbtnBed);
		sl_contentPane.putConstraint(SpringLayout.WEST, inputCards, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, inputCards, 0, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, inputCards, -125, SpringLayout.SOUTH, contentPane);
		contentPane.add(inputCards);

		JPanel bedInputPane = new JPanel();
		SpringLayout sl_bedInputPane = new SpringLayout();
		bedInputPane.setLayout(sl_bedInputPane);
		inputCards.add(bedInputPane, "bed");

		JScrollPane scrollPaneBED = new JScrollPane();
		sl_bedInputPane.putConstraint(SpringLayout.NORTH, scrollPaneBED, 36, SpringLayout.NORTH, bedInputPane);
		sl_bedInputPane.putConstraint(SpringLayout.WEST, scrollPaneBED, 10, SpringLayout.WEST, bedInputPane);
		sl_bedInputPane.putConstraint(SpringLayout.EAST, scrollPaneBED, -10, SpringLayout.EAST, bedInputPane);
		sl_bedInputPane.putConstraint(SpringLayout.SOUTH, scrollPaneBED, -3, SpringLayout.SOUTH, bedInputPane);
		bedInputPane.add(scrollPaneBED);

		expBEDList = new DefaultListModel<String>();
		final JList<String> listBEDExp = new JList<String>(expBEDList);
		listBEDExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPaneBED.setViewportView(listBEDExp);

		btnLoadBED = new JButton("Load BED Files");
		sl_bedInputPane.putConstraint(SpringLayout.SOUTH, btnLoadBED, -6, SpringLayout.NORTH, scrollPaneBED);
		sl_bedInputPane.putConstraint(SpringLayout.WEST, btnLoadBED, 0, SpringLayout.WEST, scrollPaneBED);
		btnLoadBED.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newBEDFiles = FileSelection.getFiles(fc, "bed", true);
				if (newBEDFiles != null) {
					for (int x = 0; x < newBEDFiles.length; x++) {
						BEDFiles.add(newBEDFiles[x]);
						expBEDList.addElement(newBEDFiles[x].getName());
					}
				}
			}
		});
		bedInputPane.add(btnLoadBED);

		btnRemoveBED = new JButton("Remove BED");
		sl_bedInputPane.putConstraint(SpringLayout.SOUTH, btnRemoveBED, -6, SpringLayout.NORTH, scrollPaneBED);
		sl_bedInputPane.putConstraint(SpringLayout.EAST, btnRemoveBED, 0, SpringLayout.EAST, scrollPaneBED);
		btnRemoveBED.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listBEDExp.getSelectedIndex() > -1) {
					BEDFiles.remove(listBEDExp.getSelectedIndex());
					expBEDList.remove(listBEDExp.getSelectedIndex());
				}
			}
		});
		bedInputPane.add(btnRemoveBED);

		JPanel gffInputPane = new JPanel();
		SpringLayout sl_gffInputPane = new SpringLayout();
		gffInputPane.setLayout(sl_gffInputPane);
		inputCards.add(gffInputPane, "gff");

		JScrollPane scrollPaneGFF = new JScrollPane();
		sl_gffInputPane.putConstraint(SpringLayout.NORTH, scrollPaneGFF, 36, SpringLayout.NORTH, gffInputPane);
		sl_gffInputPane.putConstraint(SpringLayout.WEST, scrollPaneGFF, 10, SpringLayout.WEST, gffInputPane);
		sl_gffInputPane.putConstraint(SpringLayout.EAST, scrollPaneGFF, -10, SpringLayout.EAST, gffInputPane);
		sl_gffInputPane.putConstraint(SpringLayout.SOUTH, scrollPaneGFF, -3, SpringLayout.SOUTH, gffInputPane);
		gffInputPane.add(scrollPaneGFF);

		expGFFList = new DefaultListModel<String>();
		final JList<String> listGFFExp = new JList<String>(expGFFList);
		listGFFExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPaneGFF.setViewportView(listGFFExp);

		btnLoadGFF = new JButton("Load GFF Files");
		sl_gffInputPane.putConstraint(SpringLayout.SOUTH, btnLoadGFF, -6, SpringLayout.NORTH, scrollPaneGFF);
		sl_gffInputPane.putConstraint(SpringLayout.WEST, btnLoadGFF, 0, SpringLayout.WEST, scrollPaneGFF);
		btnLoadGFF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newGFFFiles = FileSelection.getFiles(fc, "gff", true);
				if (newGFFFiles != null) {
					for (int x = 0; x < newGFFFiles.length; x++) {
						GFFFiles.add(newGFFFiles[x]);
						expGFFList.addElement(newGFFFiles[x].getName());
					}
				}
			}
		});
		gffInputPane.add(btnLoadGFF);

		btnRemoveGFF = new JButton("Remove GFF");
		sl_gffInputPane.putConstraint(SpringLayout.SOUTH, btnRemoveGFF, -6, SpringLayout.NORTH, scrollPaneGFF);
		sl_gffInputPane.putConstraint(SpringLayout.EAST, btnRemoveGFF, 0, SpringLayout.EAST, scrollPaneGFF);
		btnRemoveGFF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listGFFExp.getSelectedIndex() > -1) {
					GFFFiles.remove(listGFFExp.getSelectedIndex());
					expGFFList.remove(listGFFExp.getSelectedIndex());
				}
			}
		});
		gffInputPane.add(btnRemoveGFF);

		btnExecute = new JButton("Execute");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnExecute, 160, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnExecute, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnExecute, -160, SpringLayout.EAST, contentPane);
		contentPane.add(btnExecute);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnExecute);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -5, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		btnExecute.setActionCommand("start");

		JLabel lblDirectionalShift = new JLabel("Directional Shift (bp):");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDirectionalShift, 6, SpringLayout.SOUTH, inputCards);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDirectionalShift, 10, SpringLayout.WEST, contentPane);
		contentPane.add(lblDirectionalShift);

		JLabel lblSizeAdmonishment = new JLabel("upstream(-) and downstream(+)");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSizeAdmonishment, 6, SpringLayout.SOUTH, lblDirectionalShift);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSizeAdmonishment, 10, SpringLayout.WEST, contentPane);
		lblSizeAdmonishment.setFont(new Font("Dialog", Font.ITALIC, 12));
		contentPane.add(lblSizeAdmonishment);

		txtShift = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtShift, -3, SpringLayout.NORTH, lblDirectionalShift);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtShift, 10, SpringLayout.EAST, lblDirectionalShift);
		txtShift.setToolTipText("Must be integer. Negative values for upstream shift, positive values for downstream shift");
		txtShift.setHorizontalAlignment(SwingConstants.CENTER);
		txtShift.setColumns(10);
		txtShift.setText("0");
		contentPane.add(txtShift);

		chckbxStranded = new JCheckBox("Use stranded info");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxStranded, 0, SpringLayout.NORTH, lblDirectionalShift);
		sl_contentPane.putConstraint(SpringLayout.EAST, chckbxStranded, -20, SpringLayout.EAST, contentPane);
		chckbxStranded.setSelected(true);
		contentPane.add(chckbxStranded);

		chckbxGzipOutput = new JCheckBox("Output GZip");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxGzipOutput, 0, SpringLayout.SOUTH, chckbxStranded);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxGzipOutput, 0, SpringLayout.WEST, chckbxStranded);
		contentPane.add(chckbxGzipOutput);

		btnOutput = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutput, 50, SpringLayout.SOUTH, inputCards);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 10, SpringLayout.WEST, contentPane);
		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUT_DIR = FileSelection.getOutputDir(fc);
				if (OUT_DIR != null) {
					lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
				}
			}
		});
		contentPane.add(btnOutput);

		lblCurrent = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrent, 0, SpringLayout.WEST, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrent, 6, SpringLayout.SOUTH, btnOutput);
		lblCurrent.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrent);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, lblCurrent);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrent);
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);

		rdbtnBed.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnBed.isSelected()) {
					CardLayout cl = (CardLayout)(inputCards.getLayout());
					cl.show(inputCards, "bed");
				}
			}
		});
		rdbtnGff.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rdbtnGff.isSelected()) {
					CardLayout cl = (CardLayout)(inputCards.getLayout());
					cl.show(inputCards, "gff");
				}
			}
		});

		btnExecute.addActionListener(this);
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
	 * Invoked when task's progress property changes and updates the progress bar
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
		for (Component c : con.getComponents()) {
			c.setEnabled(status);
			if (c instanceof Container) { massXable((Container) c, status); }
		}
	}

}
