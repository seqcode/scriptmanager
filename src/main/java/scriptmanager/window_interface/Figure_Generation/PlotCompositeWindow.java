package scriptmanager.window_interface.Figure_Generation;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

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

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.util.FileSelection;

/**
 * GUI for collecting inputs to be processed by
 * {@link scriptmanager.scripts.Figure_Generation.PlotComposite}
 * 
 * @author Olivia Lang
 * @see scriptmanager.scripts.Figure_Generation.PlotComposite
 * @see scriptmanager.window_interface.Figure_Generation.PlotCompositeOutput
 */
@SuppressWarnings("serial")
public class PlotCompositeWindow extends JFrame implements ActionListener, PropertyChangeListener {
	private JPanel contentPane;
	/**
	 * FileChooser which opens to user's directory
	 */
	protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

	final DefaultListModel<String> expList;
	ArrayList<File> txtFiles = new ArrayList<File>();

	private JButton btnLoad;
	private JButton btnRemove;
	private JButton btnGen;
	private JProgressBar progressBar;

	/**
	 * default to true (include legend)
	 */
	private JCheckBox chckbxIncludeLegend;

	private JLabel lblPixelHeight;
	private JLabel lblPixelWidth;
	/**
	 * default to 270 (px)
	 */
	private JTextField txtPixelHeight;
	/**
	 * default to 500 (px)
	 */
	private JTextField txtPixelWidth;

	/**
	 * default to false (don't write image to file)
	 */
	private JCheckBox chckbxOutputDir;
	private JButton btnOutput;
	private JLabel lblOutput;
	private JLabel lblCurrentOutput;

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
				if (txtFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "No files loaded!!!");
				} else if (Integer.parseInt(txtPixelHeight.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid pixel height!!! Must be greater than 1");
				} else if (Integer.parseInt(txtPixelWidth.getText()) < 1) {
					JOptionPane.showMessageDialog(null, "Invalid pixel width!!! Must be greater than 1");
				} else {
					setProgress(0);
					if (OUT_DIR==null) {
						OUT_DIR = new File(System.getProperty("user.dir"));
					}
					PlotCompositeOutput output_obj = new PlotCompositeOutput(txtFiles, OUT_DIR, chckbxOutputDir.isSelected(), chckbxIncludeLegend.isSelected(), Integer.parseInt(txtPixelHeight.getText()), Integer.parseInt(txtPixelWidth.getText()));
					output_obj.addPropertyChangeListener("progress", new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
							int temp = (Integer) propertyChangeEvent.getNewValue();
							int percentComplete = (int)(((double)(temp) / (txtFiles.size())) * 100);
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
			} catch (NumberFormatException nfe){
				JOptionPane.showMessageDialog(null, "Invalid Input in Fields!!!");
			} catch (NoSuchElementException nsee){
				nsee.printStackTrace();
				JOptionPane.showMessageDialog(null, "Check that your input files are properly formatted!");
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
	public PlotCompositeWindow() {
		setTitle("Composite Plot Generator");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setBounds(125, 125, 500, 300);
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

		btnLoad = new JButton("Load Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.SOUTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoad, 10, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoad, 0, SpringLayout.NORTH, contentPane);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] newFiles = FileSelection.getGenericFiles(fc);
				if(newFiles != null) {
					for(int x = 0; x < newFiles.length; x++) {
						txtFiles.add(newFiles[x]);
						expList.addElement(newFiles[x].getName());
					}
				}
			}
		});
		contentPane.add(btnLoad);

		btnRemove = new JButton("Remove Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemove, 0, SpringLayout.NORTH, btnLoad);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemove, -10, SpringLayout.EAST, contentPane);
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				while(listExp.getSelectedIndex() > -1) {
					txtFiles.remove(listExp.getSelectedIndex());
					expList.remove(listExp.getSelectedIndex());
				}
			}
		});
		contentPane.add(btnRemove);

		btnGen = new JButton("Generate");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -100, SpringLayout.NORTH, btnGen);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnGen, 167, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnGen, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnGen, -175, SpringLayout.EAST, contentPane);
		contentPane.add(btnGen);

		progressBar = new JProgressBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnGen);
		sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, -5, SpringLayout.EAST, contentPane);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);

		btnGen.setActionCommand("start");

		chckbxIncludeLegend = new JCheckBox("Include Legend");
		sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxIncludeLegend, 10, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, chckbxIncludeLegend, -10, SpringLayout.EAST, scrollPane);
		chckbxIncludeLegend.setSelected(true);
		contentPane.add(chckbxIncludeLegend);

		btnOutput = new JButton("Output Directory");
		btnOutput.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 150, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -65, SpringLayout.NORTH, btnGen);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -150, SpringLayout.EAST, contentPane);
		contentPane.add(btnOutput);

		lblCurrentOutput = new JLabel("Current Output:");
		lblCurrentOutput.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblCurrentOutput, 15, SpringLayout.SOUTH, btnOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblCurrentOutput, 10, SpringLayout.WEST, contentPane);
		lblCurrentOutput.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(lblCurrentOutput);

		lblOutput = new JLabel("Default to Local Directory");
		lblOutput.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblOutput, 0, SpringLayout.NORTH, lblCurrentOutput);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblOutput, 6, SpringLayout.EAST, lblCurrentOutput);
		sl_contentPane.putConstraint(SpringLayout.EAST, lblOutput, 0, SpringLayout.EAST, contentPane);
		lblOutput.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblOutput.setBackground(Color.WHITE);
		contentPane.add(lblOutput);

		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OUT_DIR = FileSelection.getOutputDir(fc);
				if(OUT_DIR != null) {
					lblOutput.setText(OUT_DIR.getAbsolutePath());
				}
			}
		});

		chckbxOutputDir = new JCheckBox("Output Image");
		sl_contentPane.putConstraint(SpringLayout.WEST, chckbxOutputDir, 0, SpringLayout.WEST, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, chckbxOutputDir, 0, SpringLayout.SOUTH, btnOutput);
		contentPane.add(chckbxOutputDir);
		chckbxOutputDir.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				activateOutput(chckbxOutputDir.isSelected());
			}
		});

		lblPixelHeight = new JLabel("Image Height (px):");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, lblPixelHeight, -10, SpringLayout.NORTH, btnGen);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblPixelHeight, 10, SpringLayout.WEST, scrollPane);
		lblPixelHeight.setEnabled(false);
		contentPane.add(lblPixelHeight);

		lblPixelWidth = new JLabel("Image Width (px):");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblPixelWidth, 0, SpringLayout.NORTH, lblPixelHeight);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblPixelWidth, 80, SpringLayout.EAST, lblPixelHeight);
		lblPixelWidth.setEnabled(false);
		contentPane.add(lblPixelWidth);

		txtPixelHeight = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtPixelHeight, -2, SpringLayout.NORTH, lblPixelHeight);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtPixelHeight, 10, SpringLayout.EAST, lblPixelHeight);
		txtPixelHeight.setHorizontalAlignment(SwingConstants.CENTER);
		txtPixelHeight.setEnabled(false);
		txtPixelHeight.setColumns(5);
		txtPixelHeight.setText("270");
		contentPane.add(txtPixelHeight);

		txtPixelWidth = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, txtPixelWidth, -2, SpringLayout.NORTH, lblPixelWidth);
		sl_contentPane.putConstraint(SpringLayout.WEST, txtPixelWidth, 10, SpringLayout.EAST, lblPixelWidth);
		txtPixelWidth.setHorizontalAlignment(SwingConstants.CENTER);
		txtPixelWidth.setEnabled(false);
		txtPixelWidth.setColumns(5);
		txtPixelWidth.setText("500");
		contentPane.add(txtPixelWidth);

		btnGen.addActionListener(this);
	}

	public void activateOutput(boolean activate) {
		btnOutput.setEnabled(activate);
		lblOutput.setEnabled(activate);
		lblCurrentOutput.setEnabled(activate);
		lblPixelHeight.setEnabled(activate);
		lblPixelWidth.setEnabled(activate);
		txtPixelHeight.setEnabled(activate);
		txtPixelWidth.setEnabled(activate);
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
		if (status) {
			if (chckbxOutputDir.isSelected()) {
				activateOutput(true);
			} else {
				activateOutput(false);
			}
		}
	}
}
