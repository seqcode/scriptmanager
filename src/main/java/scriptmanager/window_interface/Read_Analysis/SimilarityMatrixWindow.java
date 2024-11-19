package scriptmanager.window_interface.Read_Analysis;

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import scriptmanager.objects.LogItem;
import scriptmanager.util.FileSelection;
import scriptmanager.cli.Read_Analysis.SimilarityMatrixCLI;
import scriptmanager.scripts.Read_Analysis.SimilarityMatrix;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Read_Analysis.SimilarityMatrix}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Read_Analysis.SimilarityMatrix
 */
@SuppressWarnings("serial")
public class SimilarityMatrixWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	final DefaultListModel<String> expList;
	ArrayList<File> TABFiles = new ArrayList<File>();
	private File OUT_DIR = null;

	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnCalculate;
	private JButton btnOutput;
	private JLabel lblDefaultToLocal;
	private JLabel lblCurrent;
	private JProgressBar progressBar;

	private JRadioButton rdbtnCorrelateColumns;
	private JRadioButton rdbtnCorrelateRows;
	private JComboBox<String> comboBox;

	/**
	 * Used to run the script efficiently
	 */
	public Task task;

	/**
	 * Organizes user inputs for calling script
	 */
	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() throws IOException {
			if (TABFiles.size() < 1) {
				JOptionPane.showMessageDialog(null, "No TAB Files Loaded!!!");
			} else {
				setProgress(0);
				LogItem old_li = null;
				for (int x = 0; x < TABFiles.size(); x++) {
					// Initialize LogItem
					String command = SimilarityMatrixCLI.getCLIcommand(TABFiles.get(x), OUT_DIR,
							comboBox.getSelectedIndex(), rdbtnCorrelateColumns.isSelected());
					LogItem new_li = new LogItem(command);
					firePropertyChange("log", old_li, new_li);
					// Execute script
					SimilarityMatrix matrix = new SimilarityMatrix(TABFiles.get(x), OUT_DIR,
							comboBox.getSelectedIndex(), rdbtnCorrelateColumns.isSelected());
					matrix.run();
					// Update LogItem
					new_li.setStopTime(new Timestamp(new Date().getTime()));
					new_li.setStatus(0);
					old_li = new_li;
					// Update progress
					int percentComplete = (int) (((double) (x + 1) / TABFiles.size()) * 100);
					setProgress(percentComplete);
				}
				// final update
				firePropertyChange("log", old_li, null);
				setProgress(100);
				JOptionPane.showMessageDialog(null, "All Matrices Generated");
			}
			/* TODO: Add try-catch block */
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
	public SimilarityMatrixWindow() {
		setTitle("Generate Similarity Matrix");
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
		final JList<String> listExp = new JList<String>(expList);
		listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPane.setViewportView(listExp);

		btnLoad = new JButton("Load TAB Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 6, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newTABFiles = FileSelection.getGenericFiles(fc);
				if (newTABFiles != null) {
					for (int x = 0; x < newTABFiles.length; x++) {
						TABFiles.add(newTABFiles[x]);
						expList.addElement(newTABFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemoveBam = new JButton("Remove TAB");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -10, SpringLayout.EAST, contentPane);
		btnRemoveBam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while (listExp.getSelectedIndex() > -1) {
					TABFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemoveBam);

		btnCalculate = new JButton("Calculate");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -130, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnCalculate, 165, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnCalculate, -165, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnCalculate, -10, SpringLayout.SOUTH, contentPane);
		contentPane.add(btnCalculate);
		btnCalculate.setActionCommand("start");
		btnCalculate.addActionListener(this);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

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
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 0, SpringLayout.WEST, btnCalculate);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -10, SpringLayout.NORTH, lblDefaultToLocal);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -135, SpringLayout.EAST, contentPane);
		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUT_DIR = FileSelection.getOutputDir(fc);
				if (OUT_DIR != null) {
					lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
				}
			}
		});
		contentPane.add(btnOutput);

		comboBox = new JComboBox<>(new DefaultComboBoxModel<>(new String[] { "Standard Pearson", "Reflective Pearson",
				"Spearman Rank", "Euclidean Distance", "Manhattan Distance" }));
		;
		contentPane.add(comboBox);

		JLabel lblSimilarityMetric = new JLabel("Similarity Metric");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSimilarityMetric, 60, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, comboBox, -5, SpringLayout.NORTH, lblSimilarityMetric);
		sl_contentPane.putConstraint(SpringLayout.WEST, comboBox, 41, SpringLayout.EAST, lblSimilarityMetric);
		contentPane.add(lblSimilarityMetric);

		rdbtnCorrelateColumns = new JRadioButton("Correlate Columns");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSimilarityMetric, 10, SpringLayout.SOUTH,
				rdbtnCorrelateColumns);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnCorrelateColumns, 60, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnCorrelateColumns, 10, SpringLayout.SOUTH, scrollPane);
		contentPane.add(rdbtnCorrelateColumns);

		rdbtnCorrelateRows = new JRadioButton("Correlate Rows");
		sl_contentPane.putConstraint(SpringLayout.NORTH, rdbtnCorrelateRows, 10, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, rdbtnCorrelateRows, 40, SpringLayout.EAST,
				rdbtnCorrelateColumns);
		contentPane.add(rdbtnCorrelateRows);

		ButtonGroup CorrelateDIR = new ButtonGroup();
		CorrelateDIR.add(rdbtnCorrelateColumns);
		CorrelateDIR.add(rdbtnCorrelateRows);
		rdbtnCorrelateColumns.setSelected(true);
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