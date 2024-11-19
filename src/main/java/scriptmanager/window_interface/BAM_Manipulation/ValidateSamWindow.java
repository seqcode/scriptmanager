package scriptmanager.window_interface.BAM_Manipulation;

import htsjdk.samtools.SAMException;
import scriptmanager.scripts.BAM_Manipulation.BAIIndexer;
import scriptmanager.scripts.BAM_Manipulation.ValidateSamWrapper;
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
import java.util.Vector;

/**
 * @author Erik Pavloski
 * This is the window class for ValidateSamWrapper
 * @see scriptmanager.scripts.BAM_Manipulation.ValidateSamWrapper
 */

public class ValidateSamWindow  extends JFrame implements ActionListener, PropertyChangeListener {
    private JPanel contentPane;
    protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

    final DefaultListModel<String> expList;

    Vector<File> BAMFiles = new Vector<File>();
    private File OUT_DIR = null;
    private File GENOME = null;
    private JButton btnLoadBam;
    private JButton btnRemoveBam;
    private JCheckBox chckbxGenerateBaiIndex;
    private JCheckBox chckbxSummaryMode;
    private JButton btnLoadGenome;
    private JLabel lblReferenceGenome;
    private JButton btnOutput;

    private JLabel outputLabel;
    private JButton btnLoadSam;
    private JLabel maxLabel;

    private JTextField maxField;

    private JLabel lblDefaultToLocal;
    private boolean mode;

    private JButton btnValidate;
    private int maxOutput;
    private JProgressBar progressBar;
    public Task task;


    class Task extends SwingWorker<Void, Void> {

        int maxOutput;

        public Task (int maxOutput) {
            this.maxOutput = maxOutput;
        }

        @Override
        public Void doInBackground() throws IOException {

            setProgress(0);
            try {
                for(int x = 0; x < BAMFiles.size(); x++) {
                    // Build output filepath
                    String[] NAME = BAMFiles.get(x).getName().split("\\.");
                    File OUTPUT = null;
                    if (OUT_DIR != null) {
                        OUTPUT = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME[0] + "_validated.html");
                    } else {
                        OUTPUT = new File(NAME[0] + "_validated.html");
                    }
                    if (chckbxGenerateBaiIndex.isSelected()) {
                        BAIIndexer.generateIndex(BAMFiles.get(x));
                    }
                    mode = !chckbxSummaryMode.isSelected();
                    // Execute Picard wrapper
                    ValidateSamWrapper.run(BAMFiles.get(x), OUTPUT, mode, GENOME, maxOutput);
                    // Update progress
                    int percentComplete = (int)(((double)(x + 1) / BAMFiles.size()) * 100);
                    setProgress(percentComplete);
                }
                setProgress(100);
                JOptionPane.showMessageDialog(null, "Validation Complete");
            } catch (SAMException se) {
                JOptionPane.showMessageDialog(null, se.getMessage());
            }
            return null;
        }

        public void done() {
            massXable(contentPane, true);
            setCursor(null); //turn off the wait cursor
        }
    }
    public ValidateSamWindow() {
        setTitle("Validate SAM/BAM file");
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
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadSam, 6, SpringLayout.SOUTH, btnLoadBam);
        sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadSam, 5, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadSam, 60, SpringLayout.NORTH, btnLoadBam);

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

        chckbxGenerateBaiIndex = new JCheckBox("Generate BAI Index?");
        sl_contentPane.putConstraint(SpringLayout.WEST, chckbxGenerateBaiIndex, 0, SpringLayout.WEST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxGenerateBaiIndex, 10, SpringLayout.SOUTH, scrollPane);
        chckbxGenerateBaiIndex.setSelected(false);
        contentPane.add(chckbxGenerateBaiIndex);

        chckbxSummaryMode = new JCheckBox("Use Summary Mode?");
        sl_contentPane.putConstraint(SpringLayout.WEST, chckbxSummaryMode, 0, SpringLayout.WEST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxSummaryMode, 10, SpringLayout.SOUTH, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, chckbxSummaryMode, 1, SpringLayout.SOUTH, chckbxGenerateBaiIndex);
        chckbxSummaryMode.setSelected(false);
        contentPane.add(chckbxSummaryMode);

        chckbxSummaryMode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean isChecked = chckbxSummaryMode.isSelected();
                maxField.setEnabled(!isChecked);
                maxField.setText("100");
                if (isChecked) {
                    maxField.setText("");
                    maxOutput = 100;
                }
            }
        });

        btnLoadGenome = new JButton("Load Genome");
        btnLoadGenome.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File temp = FileSelection.getFile(fc, "fa");
                if (temp != null) {
                    GENOME = temp;
                    lblReferenceGenome.setText("Reference Genome: " + GENOME.getName());
                }
            }
        });
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadGenome, 0, SpringLayout.NORTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadGenome, 5, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadGenome, 30, SpringLayout.NORTH, btnLoadBam);
        sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 60, SpringLayout.NORTH, btnLoadGenome);
        contentPane.add(btnLoadGenome);

        lblReferenceGenome = new JLabel("Reference Genome: ");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblReferenceGenome, 5, SpringLayout.NORTH, btnLoadGenome);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblReferenceGenome, 6, SpringLayout.EAST, btnLoadGenome);
        contentPane.add(lblReferenceGenome);

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

        outputLabel = new JLabel("Current Output:");
        sl_contentPane.putConstraint(SpringLayout.WEST, outputLabel, 0, SpringLayout.WEST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, outputLabel, 0, SpringLayout.SOUTH, contentPane);
        contentPane.add(outputLabel);

        lblDefaultToLocal = new JLabel("Default to Local Directory");
        sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 0, SpringLayout.NORTH, outputLabel);
        sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, outputLabel);
        lblDefaultToLocal.setBackground(Color.WHITE);
        contentPane.add(lblDefaultToLocal);

        btnValidate = new JButton("Validate");
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnValidate, 30, SpringLayout.NORTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.EAST, btnValidate, -5, SpringLayout.EAST, contentPane);
        contentPane.add(btnValidate);

        maxLabel = new JLabel("Maximum Output");
        sl_contentPane.putConstraint(SpringLayout.EAST, maxLabel, 0, SpringLayout.EAST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, maxLabel, 50, SpringLayout.EAST, btnOutput);
        sl_contentPane.putConstraint(SpringLayout.NORTH, maxLabel, 5, SpringLayout.SOUTH, scrollPane);
        contentPane.add(maxLabel);

        maxField = new JTextField("100");
        maxField.setPreferredSize(new Dimension(65, maxField.getPreferredSize().height));
        sl_contentPane.putConstraint(SpringLayout.EAST, maxField, 0, SpringLayout.EAST, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.WEST, maxField, 50, SpringLayout.EAST, btnOutput);
        sl_contentPane.putConstraint(SpringLayout.NORTH, maxField, 30, SpringLayout.SOUTH, scrollPane);
        contentPane.add(maxField);

        progressBar = new JProgressBar();
        sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, -3, SpringLayout.NORTH, lblDefaultToLocal);
        sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, scrollPane);
        progressBar.setStringPainted(true);
        contentPane.add(progressBar);

        btnValidate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    maxOutput = Integer.parseInt(maxField.getText());
                } catch (NumberFormatException nfe) {
                    maxOutput = 100;
                }

                task = new Task(maxOutput);
                task.addPropertyChangeListener(ValidateSamWindow.this);
                task.execute();
            }
        });
    }
    @Override
    public void actionPerformed(ActionEvent arg0) {
        massXable(contentPane, false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        task = new Task(maxOutput);
        task.addPropertyChangeListener(this);
        task.execute();
    }
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }
    public void massXable(Container con, boolean status) {
        for(Component c : con.getComponents()) {
            c.setEnabled(status);
            if(c instanceof Container) { massXable((Container)c, status); }
        }
    }
}
