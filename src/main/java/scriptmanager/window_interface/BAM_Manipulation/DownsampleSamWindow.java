package scriptmanager.window_interface.BAM_Manipulation;

import htsjdk.samtools.*;
import scriptmanager.objects.LogItem;
import scriptmanager.scripts.BAM_Manipulation.AddCommentsToBamWrapper;
import scriptmanager.scripts.BAM_Manipulation.DownsampleSamWrapper;
import scriptmanager.util.FileSelection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author Erik Pavloski
 * This is the window class for DownsampleSamWrapper
 * @see scriptmanager.scripts.BAM_Manipulation.DownsampleSamWrapper
 */
public class DownsampleSamWindow extends JFrame implements ActionListener, PropertyChangeListener {
    private JPanel contentPane;
    protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

    final DefaultListModel<String> expList;

    Vector<File> BAMFiles = new Vector<File>();
    private JButton btnLoadBam;
    private JButton btnRemoveBam;
    private JButton btnDownSample;
    private Long desiredReadCount;
    private Long totalreadCount = Long.valueOf(0);

    private JButton btnOutput;

    private JLabel outputLabel;

    private JLabel lblDefaultToLocal;

    private JCheckBox useCustomSeedBox;
    private JTextField seedField;


    private Long seed;
    private JCheckBox setReadCountBox;
    private JTextField readCountField;
    private File OUT_DIR = null;
    private JButton btnLoadSam;

    private JProgressBar progressBar;
    public Task task;



    class Task extends SwingWorker<Void, Void> {
        double probability;

        public Task (double probability) {
            this.probability = probability;
        }

        @Override
        public Void doInBackground() throws IOException{
            setProgress(0);
            LogItem old_li = null;
            try {
                for (int x = 0; x < BAMFiles.size(); x++) {
                        // Build output filepath
                        String[] NAME = BAMFiles.get(x).getName().split("\\.");
                        File OUTPUT = null;
                        if (OUT_DIR != null) {
                            OUTPUT = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME[0] + "_downsampled.bam");
                        } else {
                            OUTPUT = new File(NAME[0] + "_downsampled.bam");
                        }
                        if (probability > 1 || probability < 0) {
                            JOptionPane.showMessageDialog(null, "Must enter a value between 0 and 1");
                        }

                    // Open BAM file for reading
                    SamReader reader = SamReaderFactory.makeDefault().open(BAMFiles.get(x));

                    // Get the header and record iterator
                    SAMFileHeader header = reader.getFileHeader();
                    Iterator<SAMRecord> iterator = reader.iterator();

                    // Count the number of reads
                    while (iterator.hasNext()) {
                        iterator.next();
                        totalreadCount++;
                    }

                    // Close the reader
                    reader.close();

                    // Initialize log item
                    String command = DownsampleSamWrapper.getCLIcommand(BAMFiles.get(x), OUTPUT, probability, seed);
                    System.err.println(command);
                    LogItem new_li = new LogItem(command);
                    firePropertyChange("log", old_li, new_li);

                    // Execute picard wrapper
                    DownsampleSamWrapper.run(BAMFiles.get(x), OUTPUT, probability, seed);
                    // Update Progress
                    int percentComplete = (int) (((double) (x + 1) / BAMFiles.size()) * 100);
                    setProgress(percentComplete);
                    // Update LogItem
                    new_li.setStopTime(new Timestamp(new Date().getTime()));
					new_li.setStatus(0);
                    old_li = new_li;
                }
                firePropertyChange("log", old_li, null);
                setProgress(100);
                JOptionPane.showMessageDialog(null, "Downsampling Complete");
                return null;
            } catch (SAMException sme) {
                JOptionPane.showMessageDialog(null, sme.getMessage());
                return null;
            }
        }

        public void done() {
            massXable(contentPane, true);
            setCursor(null);
        }
    }

    public DownsampleSamWindow() {
        setTitle("Downsample SAM/BAM File");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setBounds(125, 125, 580, 450);
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

        btnLoadBam = new JButton("Load BAM Files");
        sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadBam, 5, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, btnLoadBam);

        btnLoadBam.addActionListener(new ActionListener() {
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
        contentPane.add(btnLoadBam);

        btnLoadSam = new JButton("Load SAM Files");
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadSam, 0, SpringLayout.NORTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadSam, 5, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadSam, 30, SpringLayout.NORTH, btnLoadBam);
        sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 30, SpringLayout.NORTH, btnLoadSam);

        btnLoadSam.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File[] newBAMFiles = FileSelection.getFiles(fc,"sam");
                if(newBAMFiles != null) {
                    for(int x = 0; x < newBAMFiles.length; x++) {
                        BAMFiles.add(newBAMFiles[x]);
                        expList.addElement(newBAMFiles[x].getName());
                    }
                }
            }
        });
        contentPane.add(btnLoadSam);

        btnRemoveBam = new JButton("Remove BAM");
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadBam, 0, SpringLayout.NORTH, btnRemoveBam);
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveBam, 0, SpringLayout.NORTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveBam, -5, SpringLayout.EAST, contentPane);
        btnRemoveBam.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                while(listExp.getSelectedIndex() > -1) {
                    BAMFiles.remove(listExp.getSelectedIndex());
                    expList.remove(listExp.getSelectedIndex());
                }
            }
        });
        contentPane.add(btnRemoveBam);


        JLabel probabilityLabel = new JLabel("Probability: ");
        sl_contentPane.putConstraint(SpringLayout.NORTH, probabilityLabel, 10, SpringLayout.SOUTH, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, probabilityLabel, 0, SpringLayout.WEST, scrollPane);
        contentPane.add(probabilityLabel);

        JTextField probabilityField = new JTextField("0.5");
        probabilityField.setPreferredSize(new Dimension(50, probabilityField.getPreferredSize().height));
        sl_contentPane.putConstraint(SpringLayout.NORTH, probabilityField, 10, SpringLayout.SOUTH, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, probabilityField, 5, SpringLayout.EAST, probabilityLabel);
        contentPane.add(probabilityField);

        btnOutput = new JButton("Output Directory");
        btnOutput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File temp = FileSelection.getOutputDir(fc);
                if(temp != null) {
                    OUT_DIR = temp;
                    lblDefaultToLocal.setText(OUT_DIR.getAbsolutePath());
                }
            }
        });
        sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -3, SpringLayout.NORTH, btnOutput);
        sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 200, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -50, SpringLayout.SOUTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -200, SpringLayout.EAST, contentPane);
        contentPane.add(btnOutput);

        useCustomSeedBox = new JCheckBox("Custom Seed?");
        sl_contentPane.putConstraint(SpringLayout.EAST, useCustomSeedBox, 0, SpringLayout.EAST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, useCustomSeedBox, 50, SpringLayout.EAST, btnOutput);
        sl_contentPane.putConstraint(SpringLayout.NORTH, useCustomSeedBox, 5, SpringLayout.SOUTH, scrollPane);
        contentPane.add(useCustomSeedBox);

        useCustomSeedBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean isChecked = useCustomSeedBox.isSelected();
                seedField.setEnabled(isChecked);
                seedField.setText("");
                if (!isChecked) {
                    seedField.setText("Enter Custom Seed");
                    seed = null;
                }
            }
        });

        seedField = new JTextField("Enter Custom Seed");
        seedField.setPreferredSize(new Dimension(65, seedField.getPreferredSize().height));
        sl_contentPane.putConstraint(SpringLayout.EAST, seedField, 0, SpringLayout.EAST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, seedField, 50, SpringLayout.EAST, btnOutput);
        sl_contentPane.putConstraint(SpringLayout.NORTH, seedField, 30, SpringLayout.SOUTH, scrollPane);
        contentPane.add(seedField);

        seedField.setEnabled(false);

        setReadCountBox = new JCheckBox("Set # of reads?");
        setReadCountBox.setSize(new Dimension(120, setReadCountBox.getPreferredSize().height));
        sl_contentPane.putConstraint(SpringLayout.NORTH, setReadCountBox, 55,SpringLayout.SOUTH, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, setReadCountBox, 167, SpringLayout.WEST, contentPane);
        contentPane.add(setReadCountBox);

        setReadCountBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean isChecked = setReadCountBox.isSelected();
                readCountField.setEnabled(isChecked);
                probabilityField.setEnabled(!isChecked);
                readCountField.setText("");
                if (!isChecked) {
                    readCountField.setText("Enter # of Reads");
                }
            }
        });



        readCountField = new JTextField("Enter # of Reads");
        readCountField.setPreferredSize(new Dimension(120, readCountField.getPreferredSize().height));
        sl_contentPane.putConstraint(SpringLayout.NORTH, readCountField, 58,SpringLayout.SOUTH, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, readCountField, 5, SpringLayout.EAST, setReadCountBox);
        contentPane.add(readCountField);

        readCountField.setEnabled(false);


        outputLabel = new JLabel("Current Output:");
        sl_contentPane.putConstraint(SpringLayout.WEST, outputLabel, 0, SpringLayout.WEST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, outputLabel, -30, SpringLayout.SOUTH, contentPane);
        contentPane.add(outputLabel);

        lblDefaultToLocal = new JLabel("Default to Local Directory");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, outputLabel);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, outputLabel);
        lblDefaultToLocal.setBackground(Color.WHITE);
        contentPane.add(lblDefaultToLocal);

        btnDownSample = new JButton("Downsample");
        sl_contentPane.putConstraint(SpringLayout.WEST, btnDownSample, 5, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, btnDownSample, 0, SpringLayout.SOUTH, contentPane);
        btnDownSample.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                   massXable(contentPane, false);
                   setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                   // These lines ensure that the program will run even if the user enters a string or other invalid characters
                   // The program will just default to 0.5 
                    double probability = 0.5;
                   if (setReadCountBox.isSelected()) {
                       try {
                           desiredReadCount = Long.parseLong(readCountField.getText());
                           // Compare read count to desired read count
                               if (totalreadCount > desiredReadCount) {
                                   // Set the probability value relative to the desired read count
                                   probability = (double) desiredReadCount / totalreadCount;
                               } else {
                                   JOptionPane.showMessageDialog(null, "Invalid read count. The entered value is larger than the total number of reads. Defaulting to 0.5 probability");
                               }
                       } catch (NumberFormatException ex) {
                           JOptionPane.showMessageDialog(null, "Invalid read count. Please enter an integer value.");
                           return;
                       }
                   } else {
                       try {
                           probability = Double.parseDouble(probabilityField.getText());
                       } catch (NumberFormatException nfe) {
                           JOptionPane.showMessageDialog(null, "Must enter a double value between 0 and 1. Due to improper input program will default to 0.5 probability");
                       }
                   }
                    try {
                    seed = Long.parseLong(seedField.getText());
                    } catch (NumberFormatException nfe) {
                    seed = null;
                    }
                    if (seed != null) {
                        task = new Task(seed);
                    }
                    task = new Task(probability);
                    task.addPropertyChangeListener(DownsampleSamWindow.this);
                    task.execute();

            }
        });
        contentPane.add(btnDownSample);

        progressBar = new JProgressBar();
        sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 3, SpringLayout.NORTH, btnDownSample);
        sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, scrollPane);
        progressBar.setStringPainted(true);
        contentPane.add(progressBar);
    }
    @Override
    public void actionPerformed(ActionEvent arg0) {
        massXable(contentPane, false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        task = new DownsampleSamWindow().task;
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
    }

}
