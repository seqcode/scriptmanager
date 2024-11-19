package scriptmanager.window_interface.BAM_Statistics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
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
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.BAM_Statistics.SEStats}
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.BAM_Statistics.SEStats
 * @see scriptmanager.window_interface.BAM_Statistics.SEStatWindow
 */
@SuppressWarnings("serial")
public class SEStatWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;

	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
	private JCheckBox chckbxOutputStatistics;
	private JButton btnLoad;
	private JButton btnRemoveBam;
	private JButton btnOutputDirectory;
	private JButton btnRun;

	private JLabel lblCurrentOutput;
	private JLabel lblDefaultToLocal;

	final DefaultListModel<String> expList;
	Vector<File> BAMFiles = new Vector<File>();
	private File OUTPUT_DIR = new File(System.getProperty("user.dir"));

	JProgressBar progressBar;
	public Task task;

	class Task extends SwingWorker<Void, Void> {
		@Override
		public Void doInBackground() {
			setProgress(0);
			try {
				if (expList.size() < 1) {
					JOptionPane.showMessageDialog(null, "Must load at least one BAM file");
				} else {
					SEStatOutput output_obj = new SEStatOutput(BAMFiles, OUTPUT_DIR, chckbxOutputStatistics.isSelected());
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
				}
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null, "Input Fields Must Contain Integers");
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

	/**
	 * Instantiate window with graphical interface design.
	 */
	public SEStatWindow() {
		setTitle("BAM File Statistics");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 450, 345);
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
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 0, SpringLayout.WEST, scrollPane);
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
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 0, SpringLayout.NORTH, btnRemoveBam);
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

		chckbxOutputStatistics = new JCheckBox("Output Statistics");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxOutputStatistics, 199, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxOutputStatistics, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxOutputStatistics, 0, SpringLayout.WEST, scrollPane);
		chckbxOutputStatistics.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(chckbxOutputStatistics.isSelected()) {
					btnOutputDirectory.setEnabled(true);
					lblCurrentOutput.setEnabled(true);
					lblDefaultToLocal.setEnabled(true);
				} else {
					btnOutputDirectory.setEnabled(false);
					lblCurrentOutput.setEnabled(false);
					lblDefaultToLocal.setEnabled(false);
				}
			}
		});
		contentPane.add(chckbxOutputStatistics);

		btnOutputDirectory = new JButton("Output Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnOutputDirectory, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutputDirectory, 150, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutputDirectory, -150, SpringLayout.EAST, contentPane);
		btnOutputDirectory.setEnabled(false);
		btnOutputDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUTPUT_DIR = FileSelection.getOutputDir(fc);
				if(OUTPUT_DIR != null) {
					lblDefaultToLocal.setText(OUTPUT_DIR.getAbsolutePath());
				}
			}
		});
		contentPane.add(btnOutputDirectory);
		
		lblCurrentOutput = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentOutput, 10, SpringLayout.SOUTH, chckbxOutputStatistics);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentOutput, 0, SpringLayout.WEST, scrollPane);
		lblCurrentOutput.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblCurrentOutput.setEnabled(false);
		contentPane.add(lblCurrentOutput);

		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, lblCurrentOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, lblCurrentOutput);
		lblDefaultToLocal.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblDefaultToLocal.setBackground(Color.WHITE);
		lblDefaultToLocal.setEnabled(false);
		contentPane.add(lblDefaultToLocal);

		btnRun = new JButton("Run");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRun, 90, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnRun, 171, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRun, -171, SpringLayout.EAST, contentPane);
		contentPane.add(btnRun);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, progressBar, -8, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -5, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRun, -3, SpringLayout.NORTH, progressBar);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRun, -18, SpringLayout.WEST, progressBar);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		btnRun.addActionListener(this);
		btnRun.setActionCommand("start");
	}

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

	public void massXable(Container con, boolean status) {
		for(Component c : con.getComponents()) {
			c.setEnabled(status);
			if(c instanceof Container) { massXable((Container)c, status); }
		}
		if(status) {
			if(!chckbxOutputStatistics.isSelected()) {
				btnOutputDirectory.setEnabled(false);
				lblCurrentOutput.setEnabled(false);
				lblDefaultToLocal.setEnabled(false);
			}
		}
	}
}