package scriptmanager.window_interface.BAM_Manipulation;

import htsjdk.samtools.SAMException;
import picard.sam.AddCommentsToBam;
import scriptmanager.cli.BAM_Format_Converter.BAMtobedGraphCLI;
import scriptmanager.objects.LogItem;
import scriptmanager.objects.PasteableTable;
import scriptmanager.scripts.BAM_Manipulation.AddCommentsToBamWrapper;
import scriptmanager.util.FileSelection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Erik Pavloski
 * This is the window class for the AddCommentsToBam Picard tool
 * @see scriptmanager.scripts.BAM_Manipulation.AddCommentsToBamWrapper
 */
public class AddCommentsToBamWindow extends JFrame implements ActionListener, PropertyChangeListener {
    private JPanel contentPane;
    protected JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));

    final DefaultListModel<String> expList;

    Vector<File> BAMFiles = new Vector<File>();
    private File OUT_DIR = null;

    private JButton btnLoadBam;
    private JButton btnRemoveBam;
    private JButton btnAddComment;
    private JButton btnRemoveComment;
    private JLabel outputLabel;
    private JLabel lblDefaultToLocal;
    private JButton btnOutput;
    private JButton btnComment;
    private JProgressBar progressBar;
    private DefaultTableModel commentTable;
    public Task task;
    class Task extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() throws IOException {
            setProgress(0);
            LogItem old_li = null;
            try {
                ArrayList<String> comments  = new ArrayList<String>();
                for (int i = 0; i < commentTable.getRowCount(); i++){
                    comments.add(commentTable.getValueAt(i, 0).toString());
                }
                if (comments.size() == 0) {
					JOptionPane.showMessageDialog(null, "No Comments Provided!!!");
				} else {
                    for (int x = 0; x < BAMFiles.size(); x++) {
                        // Build output filepath
                        String[] NAME = BAMFiles.get(x).getName().split("\\.");
                        File OUTPUT = null;
                        if (OUT_DIR != null) {
                            OUTPUT = new File(OUT_DIR.getCanonicalPath() + File.separator + NAME[0] + "_commented.bam");
                        } else {
                            OUTPUT = new File(NAME[0] + "_commented.bam");
                        }
                        // Initialize log item
                        String command = AddCommentsToBamWrapper.getCLIcommand(BAMFiles.get(x), OUTPUT, comments);
                        System.err.println(command);
                        LogItem new_li = new LogItem(command);
                        firePropertyChange("log", old_li, new_li);
                        // Run Wrapper
                        AddCommentsToBamWrapper.run(BAMFiles.get(x), OUTPUT, comments);
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
                    JOptionPane.showMessageDialog(null, "Comments Added");
                }
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

    public AddCommentsToBamWindow() {
        setTitle("Add Comments to BAM File");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(125, 125, 580, 550);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        SpringLayout sl_contentPane = new SpringLayout();
        contentPane.setLayout(sl_contentPane);

        JScrollPane scrollPane = new JScrollPane();
        sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 5, SpringLayout.WEST, contentPane); 
        sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -5, SpringLayout.EAST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, 200, SpringLayout.NORTH, contentPane);
        contentPane.add(scrollPane);

        expList = new DefaultListModel<>();
        final JList<String> listExp = new JList<>(expList);
        listExp.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scrollPane.setViewportView(listExp);

		btnLoadBam = new JButton("Load BAM Files");
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, btnLoadBam);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnLoadBam, 0, SpringLayout.WEST, scrollPane);
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

        btnAddComment = new JButton("Add Comment");
		btnAddComment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                    commentTable.addRow(new Object[] { "" });
                }
			}
		);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnAddComment, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnAddComment, 0, SpringLayout.WEST, scrollPane);
		contentPane.add(btnAddComment);

        btnRemoveComment = new JButton("Remove Comment");
		btnRemoveComment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                    commentTable.removeRow(commentTable.getRowCount() - 1);
                }
			}
		);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnRemoveComment, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnRemoveComment, 0, SpringLayout.EAST, scrollPane);
		contentPane.add(btnRemoveComment);

        String[] TableHeader = { "Comments" };
		commentTable = new DefaultTableModel(null, TableHeader);
		JTable tableScale = new JTable(commentTable);
		// Allow for the selection of multiple OR individual cells across either rows or columns
		tableScale.setCellSelectionEnabled(true);
		tableScale.setColumnSelectionAllowed(true);
		tableScale.setRowSelectionAllowed(true);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        tableScale.setDefaultRenderer(Object.class, centerRenderer);
		@SuppressWarnings("unused")
		PasteableTable myAd = new PasteableTable(tableScale);

        scrollPane = new JScrollPane(tableScale);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, btnRemoveComment);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -5, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);

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
		sl_contentPane.putConstraint(SpringLayout.WEST, btnOutput, 146, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOutput, -50, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnOutput, -146, SpringLayout.EAST, contentPane);
		contentPane.add(btnOutput);
		
		outputLabel = new JLabel("Current Output:");
		sl_contentPane.putConstraint(SpringLayout.WEST, outputLabel, 0, SpringLayout.WEST, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, outputLabel, -30, SpringLayout.SOUTH, contentPane);
		outputLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		contentPane.add(outputLabel);
		
		lblDefaultToLocal = new JLabel("Default to Local Directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblDefaultToLocal, 1, SpringLayout.NORTH, outputLabel);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblDefaultToLocal, 6, SpringLayout.EAST, outputLabel);
		lblDefaultToLocal.setBackground(Color.WHITE);
		contentPane.add(lblDefaultToLocal);

        btnComment = new JButton("Write Comments");
        sl_contentPane.putConstraint(SpringLayout.NORTH, btnComment, 6, SpringLayout.SOUTH, lblDefaultToLocal);
        sl_contentPane.putConstraint(SpringLayout.WEST, btnComment, 200, SpringLayout.WEST, contentPane);
        sl_contentPane.putConstraint(SpringLayout.EAST, btnComment, -200, SpringLayout.EAST, contentPane);
        btnComment.addActionListener(e -> {
            task = new Task();
            task.addPropertyChangeListener(AddCommentsToBamWindow.this);
            task.execute();
        });
        contentPane.add(btnComment);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        contentPane.add(progressBar);
        sl_contentPane.putConstraint(SpringLayout.NORTH, progressBar, 6, SpringLayout.SOUTH, lblDefaultToLocal);
        sl_contentPane.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, scrollPane);
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
	 * Invoked when the task's progress changes
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
