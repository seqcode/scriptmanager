package scriptmanager.window_interface.BAM_Manipulation;

import htsjdk.samtools.SAMException;
import scriptmanager.objects.LogItem;
import scriptmanager.scripts.BAM_Manipulation.AddCommentsToBamWrapper;
import scriptmanager.scripts.BAM_Manipulation.NormalizeFastaWrapper;
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
import java.util.Vector;

/**
 * @author Erik Pavloski
 * This is the window class for the NormalizeFasta Picard tool
 * @see scriptmanager.scripts.BAM_Manipulation.NormalizeFastaWrapper
 */
public class NormalizeFastaWindow extends JFrame implements ActionListener, PropertyChangeListener {
    private JPanel contentPane;
    protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

    final DefaultListModel<String> expList;

    Vector<File> BAMFiles = new Vector<File>();
    private File OUT_DIR = null;

    private JButton btnLoadFasta;
    private JButton btnRemoveFasta;
    private JLabel outputLabel;
    private JLabel lblDefaultToLocal;
    private JButton btnOutput;
    private JButton btnNormalize;
    private JProgressBar progressBar;
    public Task task;
    class Task extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() throws IOException {
            setProgress(0);
            LogItem old_li = null;
            try {
                for (int x = 0; x < BAMFiles.size(); x++) {
                    // Build output filepath
                    String[] NAME = BAMFiles.get(x).getName().split("\\.");
                    File OUTPUT = null;
                    if (OUT_DIR != null) {
                        OUTPUT = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME[0] + "_normalized.fasta");
                    } else {
                        OUTPUT = new File(NAME[0] + "_normalized.fasta");
                    }
                    // Initialize log item
                    String command = NormalizeFastaWrapper.getCLIcommand(BAMFiles.get(x), OUTPUT);
                    System.err.println(command);
                    LogItem new_li = new LogItem(command);
                    // Run Wrapper
                    NormalizeFastaWrapper.run(BAMFiles.get(x), OUTPUT);
                    // Update progress
                    int percentComplete = (int)(((double)(x + 1) / BAMFiles.size()) * 100);
                    setProgress(percentComplete);
                    // Update LogItem
                    new_li.setStopTime(new Timestamp(new Date().getTime()));
					new_li.setStatus(0);
                    old_li = new_li;
                }
                firePropertyChange("log", old_li, null);
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

    public NormalizeFastaWindow() {
        setTitle("Normalize FASTA File");
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

        btnLoadFasta = new JButton("Load FASTA Files");
        sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadFasta, 5, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, btnLoadFasta);

        btnLoadFasta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File[] newBAMFiles = FileSelection.getFiles(fc,"fasta");
                if(newBAMFiles != null) {
                    for(int x = 0; x < newBAMFiles.length; x++) {
                        BAMFiles.add(newBAMFiles[x]);
                        expList.addElement(newBAMFiles[x].getName());
                    }
                }
            }
        });
        contentPane.add(btnLoadFasta);

        btnRemoveFasta = new JButton("Remove FASTA");
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnLoadFasta, 0, SpringLayout.NORTH, btnRemoveFasta);
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveFasta, 0, SpringLayout.NORTH, contentPane);
        sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveFasta, -5, SpringLayout.EAST, contentPane);
        btnRemoveFasta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                while(listExp.getSelectedIndex() > -1) {
                    BAMFiles.remove(listExp.getSelectedIndex());
                    expList.remove(listExp.getSelectedIndex());
                }
            }
        });
        contentPane.add(btnRemoveFasta);

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

        btnNormalize = new JButton("Normalize");
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnNormalize, 2, SpringLayout.SOUTH, scrollPane);
        sl_contentPane.putConstraint(SpringLayout.EAST, btnNormalize, -5, SpringLayout.EAST, contentPane);
        contentPane.add(btnNormalize);

        btnNormalize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                task = new Task();
                task.addPropertyChangeListener(NormalizeFastaWindow.this);
                task.execute();
            }
        });

        progressBar = new JProgressBar();
        sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, -3, SpringLayout.NORTH, lblDefaultToLocal);
        sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, scrollPane);
        progressBar.setStringPainted(true);
        contentPane.add(progressBar);
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
    }
}