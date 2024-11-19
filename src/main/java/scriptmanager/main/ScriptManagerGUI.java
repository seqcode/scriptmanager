package scriptmanager.main;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import scriptmanager.objects.ToolDescriptions;
import scriptmanager.objects.LogItem;

/**
 * Main GUI object that manages main window design for spinning out the GUI for
 * each tool and the logging manager window. Instantiated by
 * scriptmanager.main.ScriptManager.
 *
 * @author William KM Lai
 * @see scriptmanager.main.ScriptManager
 */
public class ScriptManagerGUI {
	private JFrame frmScriptManager;
	private static LogManagerWindow logs;

	private final int FRAME_WIDTH = 600;
	private final int FRAME_HEIGHT = 380;

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() throws ClassNotFoundException {
		frmScriptManager = new JFrame();
		frmScriptManager.setTitle("Script Manager v" + ToolDescriptions.VERSION);
		frmScriptManager.setBounds(100, 100, FRAME_WIDTH, FRAME_HEIGHT);
		frmScriptManager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmScriptManager.setResizable(false);
		SpringLayout springLayout = new SpringLayout();
		frmScriptManager.getContentPane().setLayout(springLayout);

		// ======== Logging ========
		JPanel pnlLogging = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, pnlLogging, -40, SpringLayout.SOUTH, frmScriptManager.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, pnlLogging, 10, SpringLayout.WEST, frmScriptManager.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, pnlLogging, -10, SpringLayout.SOUTH, frmScriptManager.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, pnlLogging, -10, SpringLayout.EAST, frmScriptManager.getContentPane());
		frmScriptManager.getContentPane().add(pnlLogging);

//		TitledBorder ttlLogging = BorderFactory.createTitledBorder("Session logs");
//		ttlLogging.setTitleFont(new Font("Lucida Grande", Font.ITALIC, 14));
//		pnlLogging.setBorder(ttlLogging);

		// Initialize Logging Manager Window
		logs = new LogManagerWindow();

		JButton btnLoggingManager = new JButton("Open Log Manager");
		btnLoggingManager.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							logs.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
		pnlLogging.add(btnLoggingManager);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 10, SpringLayout.NORTH, frmScriptManager.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 10, SpringLayout.WEST, frmScriptManager.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -10, SpringLayout.NORTH, pnlLogging);
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, -10, SpringLayout.EAST, frmScriptManager.getContentPane());
		frmScriptManager.getContentPane().add(tabbedPane);

		/* Template for new tool
		// COMMENT
		PANEL.add(initializeToolPanel("NAME", ToolDescriptions.DESCRIPTION,
				Class.forName("scriptmanager.window_interface.PACKAGE.CLASS")));
		*/

		// ======== BAM_Statistics ========
		JPanel pnlStat = new JPanel();
		BoxLayout bl_pnlStat = new BoxLayout(pnlStat, BoxLayout.PAGE_AXIS);
		pnlStat.setLayout(bl_pnlStat);
		JScrollPane sp_pnlStat = new JScrollPane(pnlStat, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("BAM Statistics", null, sp_pnlStat, null);

		// SEStats
		pnlStat.add(initializeToolPanel("BAM Statistics", ToolDescriptions.se_stat_description,
				Class.forName("scriptmanager.window_interface.BAM_Statistics.SEStatWindow")));
		// PEStats
		pnlStat.add(initializeToolPanel("Paired-End Statistics", ToolDescriptions.pe_stat_description,
				Class.forName("scriptmanager.window_interface.BAM_Statistics.PEStatWindow")));
		// SearchMotif
		pnlStat.add(initializeToolPanel("BAM Genome Correlation", ToolDescriptions.bam_correlation_description,
				Class.forName("scriptmanager.window_interface.BAM_Statistics.BAMGenomeCorrelationWindow")));
		// CrossCorrelation
		pnlStat.add(initializeToolPanel("Cross Correlation", ToolDescriptions.archtex_crosscorrelation_description,
				Class.forName("scriptmanager.window_interface.BAM_Statistics.CrossCorrelationWindow")));

		// ======== BAM_Manipulation ========
		JPanel pnlBamManip = new JPanel();
		BoxLayout bl_pnlBamManip = new BoxLayout(pnlBamManip, BoxLayout.PAGE_AXIS);
		pnlBamManip.setLayout(bl_pnlBamManip);
		JScrollPane sp_pnlBamManip = new JScrollPane(pnlBamManip, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("BAM Manipulation", null, sp_pnlBamManip, null);

		// BAMIndexer
		pnlBamManip.add(initializeToolPanel("BAM-BAI Indexer", ToolDescriptions.bam_indexer_description,
				Class.forName("scriptmanager.window_interface.BAM_Manipulation.BAIIndexerWindow")));
		// BAMFileSorter
		pnlBamManip.add(initializeToolPanel("BAM File Sorter", ToolDescriptions.sort_bam_description,
				Class.forName("scriptmanager.window_interface.BAM_Manipulation.SortBAMWindow")));
		// BAMRemoveDup
		pnlBamManip.add(initializeToolPanel("BAM MarkDuplicates", ToolDescriptions.remove_duplicates_description,
				Class.forName("scriptmanager.window_interface.BAM_Manipulation.BAMMarkDupWindow")));
		// BAMReplicateMerge
		pnlBamManip.add(initializeToolPanel("BAM Replicate Merge", ToolDescriptions.merge_bam_description,
				Class.forName("scriptmanager.window_interface.BAM_Manipulation.MergeBAMWindow")));
		// FilterPIPseq
		pnlBamManip.add(initializeToolPanel("Filter for PIP-seq", ToolDescriptions.filter_pip_seq_description,
				Class.forName("scriptmanager.window_interface.BAM_Manipulation.FilterforPIPseqWindow")));

		// ======== BAM_Format_Converter ========
		JPanel pnlBamConvert = new JPanel();
		BoxLayout bl_pnlBamConvert = new BoxLayout(pnlBamConvert, BoxLayout.PAGE_AXIS);
		pnlBamConvert.setLayout(bl_pnlBamConvert);
		JScrollPane sp_pnlBamConvert = new JScrollPane(pnlBamConvert, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("BAM Format Converter", null, sp_pnlBamConvert, null);

		// BAMtoscIdx
		pnlBamConvert.add(initializeToolPanel("BAM to scIDX", ToolDescriptions.bam_to_scidx_description,
				Class.forName("scriptmanager.window_interface.BAM_Format_Converter.BAMtoscIDXWindow")));
//		pnlBamConvert.add(Box.createVerticalGlue());
		// BAMtoGFF
		pnlBamConvert.add(initializeToolPanel("BAM to GFF", ToolDescriptions.bam_to_gff_description,
				Class.forName("scriptmanager.window_interface.BAM_Format_Converter.BAMtoGFFWindow")));
		// BAMtoBED
		pnlBamConvert.add(initializeToolPanel("BAM to BED", ToolDescriptions.bam_to_bed_description,
				Class.forName("scriptmanager.window_interface.BAM_Format_Converter.BAMtoBEDWindow")));
		// BAMtobedGraph
		pnlBamConvert.add(initializeToolPanel("BAM to bedGraph", ToolDescriptions.bam_to_bedgraph_description,
				Class.forName("scriptmanager.window_interface.BAM_Format_Converter.BAMtobedGraphWindow")));

		// ======== File_Utilities ========
		JPanel pnlFileUtility = new JPanel();
		BoxLayout bl_pnlFileUtility = new BoxLayout(pnlFileUtility, BoxLayout.PAGE_AXIS);
		pnlFileUtility.setLayout(bl_pnlFileUtility);
		JScrollPane sp_pnlFileUtility = new JScrollPane(pnlFileUtility, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("File Utilities", null, sp_pnlFileUtility, null);

		// MD5checksum
		pnlFileUtility.add(initializeToolPanel("MD5 Checksum", ToolDescriptions.md5checksum_description,
				Class.forName("scriptmanager.window_interface.File_Utilities.MD5ChecksumWindow")));
		// ConvertBEDChrNames
		pnlFileUtility.add(initializeToolPanel("Convert BED Chr Names", ToolDescriptions.convertBEDChrNamesDescription,
				Class.forName("scriptmanager.window_interface.File_Utilities.ConvertBEDChrNamesWindow")));
		// ConvertGFFChrNames
		pnlFileUtility.add(initializeToolPanel("Convert GFF Chr Names", ToolDescriptions.convertGFFChrNamesDescription,
				Class.forName("scriptmanager.window_interface.File_Utilities.ConvertGFFChrNamesWindow")));
		// CompressFileWindow
		pnlFileUtility.add(initializeToolPanel("Compress Files", ToolDescriptions.compressFileDescription,
				Class.forName("scriptmanager.window_interface.File_Utilities.CompressFileWindow")));
		// DecompressGZFileWindow
		pnlFileUtility.add(initializeToolPanel("Decompress Files", ToolDescriptions.decompressFileDescription,
				Class.forName("scriptmanager.window_interface.File_Utilities.DecompressGZFileWindow")));

		// ======== Peak_Calling ========
		JPanel pnlPeakCalling = new JPanel();
		BoxLayout bl_pnlPeakCalling = new BoxLayout(pnlPeakCalling, BoxLayout.PAGE_AXIS);
		pnlPeakCalling.setLayout(bl_pnlPeakCalling);
		JScrollPane sp_pnlPeakCalling = new JScrollPane(pnlPeakCalling, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Peak Calling", null, sp_pnlPeakCalling, null);

		// GeneTrack
		pnlPeakCalling.add(initializeToolPanel("GeneTrack", ToolDescriptions.gene_track_description,
				Class.forName("scriptmanager.window_interface.Peak_Calling.GeneTrackWindow")));
		// PeakPairing
		pnlPeakCalling.add(initializeToolPanel("Peak-Pairing", ToolDescriptions.peak_pairing_description,
				Class.forName("scriptmanager.window_interface.Peak_Calling.PeakPairWindow")));
//		// ReplicateMatching
//		pnlPeakCalling.add(initializeToolPanel("Replicate Match", ToolDescriptions.replicate_match_description,
//				Class.forName("scriptmanager.window_interface.Peak_Calling.CLASS")));

		// ======== Peak_Analysis ========
		JPanel pnlPeakAnalysis = new JPanel();
		BoxLayout bl_pnlPeakAnalysis = new BoxLayout(pnlPeakAnalysis, BoxLayout.PAGE_AXIS);
		pnlPeakAnalysis.setLayout(bl_pnlPeakAnalysis);
		JScrollPane sp_pnlPeakAnalysis = new JScrollPane(pnlPeakAnalysis, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Peak Analysis", null, sp_pnlPeakAnalysis, null);

		// PeakAlign
		pnlPeakAnalysis.add(initializeToolPanel("Align BED to Reference", ToolDescriptions.peak_align_ref_description,
				Class.forName("scriptmanager.window_interface.Peak_Analysis.BEDPeakAligntoRefWindow")));
		// FilterBED
		pnlPeakAnalysis.add(initializeToolPanel("Filter BED by Proximity", ToolDescriptions.filter_bed_description,
				Class.forName("scriptmanager.window_interface.Peak_Analysis.FilterBEDbyProximityWindow")));
		// TileGenome
		pnlPeakAnalysis.add(initializeToolPanel("Genomic Coordinate Tile", ToolDescriptions.tile_genome_description,
				Class.forName("scriptmanager.window_interface.Peak_Analysis.TileGenomeWindow")));
		// RandCoord
		pnlPeakAnalysis.add(initializeToolPanel("Generate Random Coordinate", ToolDescriptions.rand_coord_description,
				"Generate random BED coordinates based on reference genome.",
				Class.forName("scriptmanager.window_interface.Peak_Analysis.RandomCoordinateWindow")));
		// Signal_Duplication
		pnlPeakAnalysis.add(initializeToolPanel("Signal Duplication", ToolDescriptions.signal_dup_description,
				"Output signal duplication statistics",
				Class.forName("scriptmanager.window_interface.Peak_Analysis.SignalDuplicationWindow")));
		// SortByRef
		pnlPeakAnalysis.add(initializeToolPanel("Sort Coordinates By Distance", ToolDescriptions.sort_by_dist_description,
				"Sort BED/GFF files by distance to peak BED/GFF",
				Class.forName("scriptmanager.window_interface.Peak_Analysis.SortByDistWindow")));
		// FRiXCalculator
		pnlPeakAnalysis.add(initializeToolPanel("FRiX Score Calculator", ToolDescriptions.frix_description,
				Class.forName("scriptmanager.window_interface.Peak_Analysis.FRiXCalculatorWindow")));

		// ======== Coordinate_Manipulation ========
		JPanel pnlCoordManip = new JPanel();
		pnlCoordManip.setLayout(new FlowLayout(FlowLayout.CENTER, 100, 5));
		tabbedPane.addTab("Coordinate File Manipulation", null, pnlCoordManip, null);

		JSplitPane splitPaneExpand = new JSplitPane();
		pnlCoordManip.add(splitPaneExpand);
		// ExpandBED
		splitPaneExpand.setLeftComponent(initializeToolPanel("Expand BED File", null, ToolDescriptions.expand_bed_description,
				Class.forName("scriptmanager.window_interface.Coordinate_Manipulation.BED_Manipulation.ExpandBEDWindow")));
		// ExpandGFF
		splitPaneExpand.setRightComponent(initializeToolPanel("Expand GFF File", null, ToolDescriptions.expand_gff_description,
				Class.forName("scriptmanager.window_interface.Coordinate_Manipulation.GFF_Manipulation.ExpandGFFWindow")));

		JSplitPane splitPaneConvert = new JSplitPane();
		pnlCoordManip.add(splitPaneConvert);
		// BEDtoGFF
		splitPaneConvert.setLeftComponent(initializeToolPanel("Convert BED to GFF", null, ToolDescriptions.bed_to_gff_description,
				Class.forName("scriptmanager.window_interface.Coordinate_Manipulation.BED_Manipulation.BEDtoGFFWindow")));
		// GFFtoBED
		splitPaneConvert.setRightComponent(initializeToolPanel("Convert GFF to BED", null, ToolDescriptions.gff_to_bed_description,
				Class.forName("scriptmanager.window_interface.Coordinate_Manipulation.GFF_Manipulation.GFFtoBEDWindow")));

		JSplitPane splitPaneSort = new JSplitPane();
		pnlCoordManip.add(splitPaneSort);
		// SortBED
		splitPaneSort.setLeftComponent(initializeToolPanel("Sort BED by CDT", null, ToolDescriptions.sort_bed_description,
				Class.forName("scriptmanager.window_interface.Coordinate_Manipulation.BED_Manipulation.SortBEDWindow")));
		// SortGFF
		splitPaneSort.setRightComponent(initializeToolPanel("Sort GFF by CDT", null, ToolDescriptions.sort_gff_description,
				Class.forName("scriptmanager.window_interface.Coordinate_Manipulation.GFF_Manipulation.SortGFFWindow")));

		// ShiftInterval
		pnlCoordManip.add(initializeToolPanel("Shift Coordinate Interval", null, ToolDescriptions.shift_coordinate_description,
				Class.forName("scriptmanager.window_interface.Coordinate_Manipulation.ShiftIntervalWindow")));

		// ======== Read_Analysis ========
		JPanel pnlReadAnalysis = new JPanel();
		BoxLayout bl_pnlReadAnalysis = new BoxLayout(pnlReadAnalysis, BoxLayout.PAGE_AXIS);
		pnlReadAnalysis.setLayout(bl_pnlReadAnalysis);
		JScrollPane sp_pnlReadAnalysis = new JScrollPane(pnlReadAnalysis, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Sequence Read Analysis", null, sp_pnlReadAnalysis, null);

		// TagPileup
		pnlReadAnalysis.add(initializeToolPanel("Tag Pileup", ToolDescriptions.tag_pileup_description,
				Class.forName("scriptmanager.window_interface.Read_Analysis.TagPileupWindow")));
		// ScalingFactor
		pnlReadAnalysis.add(initializeToolPanel("Calculate Scaling Factor", ToolDescriptions.scaling_factor_description,
				Class.forName("scriptmanager.window_interface.Read_Analysis.ScalingFactorWindow")));
		// ScaleMatrix
		pnlReadAnalysis.add(initializeToolPanel("Scale Matrix Data", ToolDescriptions.scale_matrix_description,
				Class.forName("scriptmanager.window_interface.Read_Analysis.ScaleMatrixWindow")));
		// AggregateData
		pnlReadAnalysis.add(initializeToolPanel("Aggregate Data", ToolDescriptions.aggregate_data_description,
				Class.forName("scriptmanager.window_interface.Read_Analysis.AggregateDataWindow")));
		// TransposeMatrix
		pnlReadAnalysis.add(initializeToolPanel("Transpose Matrix", ToolDescriptions.transpose_matrix_description,
				Class.forName("scriptmanager.window_interface.Read_Analysis.TransposeMatrixWindow")));

		// ========= Sequence_Analysis ==========
		JPanel pnlSeqAnalysis = new JPanel();
		BoxLayout bl_pnlSeqAnalysis = new BoxLayout(pnlSeqAnalysis, BoxLayout.PAGE_AXIS);
		pnlSeqAnalysis.setLayout(bl_pnlSeqAnalysis);
		JScrollPane sp_pnlSeqAnalysis = new JScrollPane(pnlSeqAnalysis, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("DNA Sequence Analysis", null, sp_pnlSeqAnalysis, null);

		// FASTAExtract
		pnlSeqAnalysis.add(initializeToolPanel("FASTA from BED", ToolDescriptions.fasta_extract_description,
				Class.forName("scriptmanager.window_interface.Sequence_Analysis.FASTAExtractWindow")));
		// RandomizeFASTA
		pnlSeqAnalysis.add(initializeToolPanel("Randomize FASTA", ToolDescriptions.randomize_fasta_description,
				Class.forName("scriptmanager.window_interface.Sequence_Analysis.RandomizeFASTAWindow")));
		// SearchMotif
		pnlSeqAnalysis.add(initializeToolPanel("Search Motif in FASTA", ToolDescriptions.search_motif_description,
				Class.forName("scriptmanager.window_interface.Sequence_Analysis.SearchMotifWindow")));
		// DNAShapeFromBED
		pnlSeqAnalysis.add(initializeToolPanel("DNA Shape from BED", ToolDescriptions.dna_shape_from_bed_description,
				Class.forName("scriptmanager.window_interface.Sequence_Analysis.DNAShapefromBEDWindow")));
		// DNAShapeFromFASTA
		pnlSeqAnalysis.add(initializeToolPanel("DNA Shape from FASTA", ToolDescriptions.dna_shape_from_fasta_description,
				"Calculate intrinsic DNA shape given input FASTA file",
				Class.forName("scriptmanager.window_interface.Sequence_Analysis.DNAShapefromFASTAWindow")));

		// ======== Figure_Generation ========
		JPanel pnlFigure = new JPanel();
		BoxLayout bl_pnlFigure = new BoxLayout(pnlFigure, BoxLayout.PAGE_AXIS);
		pnlFigure.setLayout(bl_pnlFigure);
		JScrollPane sp_pnlFigure = new JScrollPane(pnlFigure, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Figure Generation", null, sp_pnlFigure, null);

		// TwoColorHeatMap
		pnlFigure.add(initializeToolPanel("Two-Color Heat Map", ToolDescriptions.heatmap_description,
				Class.forName("scriptmanager.window_interface.Figure_Generation.TwoColorHeatMapWindow")));
		// ThreeColorHeatMap
		pnlFigure.add(initializeToolPanel("Three Color Heat Map", ToolDescriptions.threecolorheatmap_description,
				Class.forName("scriptmanager.window_interface.Figure_Generation.ThreeColorHeatMapWindow")));
		// MergeHeatMap
		pnlFigure.add(initializeToolPanel("Merge Heatmaps", ToolDescriptions.merge_heatmap_description,
				Class.forName("scriptmanager.window_interface.Figure_Generation.MergeHeatMapWindow")));
		// LabelHeatMap
		pnlFigure.add(initializeToolPanel("Label HeatMap", ToolDescriptions.label_heatmap_description,
				Class.forName("scriptmanager.window_interface.Figure_Generation.LabelHeatMapWindow")));
		// FourColorPlot
		pnlFigure.add(initializeToolPanel("4Color Sequence Plot", ToolDescriptions.four_color_description,
				Class.forName("scriptmanager.window_interface.Figure_Generation.FourColorSequenceWindow")));
		// Composite Plot
		pnlFigure.add(initializeToolPanel("Composite Plot", ToolDescriptions.composite_description,
				Class.forName("scriptmanager.window_interface.Figure_Generation.PlotCompositeWindow")));

		// Set default tab to open to...
		// 0=BAM_Statistics 5=Peak_Analysis
		// 1=BAM_Manipulation 6=Coordinate_Manipulation
		// 2=BAM_Format_Converter 7=Sequence_Analysis
		// 3=File_Utilities 8=DNA_Sequence_Analysis
		// 4=Peak_Calling 9=Figure_Generation
		tabbedPane.setSelectedIndex(0);
	}

	/**
	 * Call initializeToolPanel without the tooltip in the signature (call
	 * initializeToolPanel with tooltip set to null).
	 * 
	 * @param btnName         the name of the tool to display in the JButton
	 * @param toolDescription the text describing the tool in the JTextArea
	 * @param c               the full package name of the class that the JButton
	 *                        instantiates an instance of
	 * @return
	 */
	private JPanel initializeToolPanel(String btnName, String toolDescription, Class<?> c) {
		return (initializeToolPanel(btnName, toolDescription, null, c));
	}

	/**
	 * Use reflections to generalize the ActionListener definition to different
	 * window_interface classes. If toolDescription is set to null, then the
	 * JTextArea is not instantiated and the JPanel returned only contains the
	 * JButton component.
	 * 
	 * @param btnName         the name of the tool to display in the JButton
	 * @param toolDescription the text describing the tool in the JTextArea
	 * @param toolTip         the text describing the JButton component when the
	 *                        user hovers over it
	 * @param c               the full package name of the class that the JButton
	 *                        instantiates an instance of
	 * @return
	 */
	private JPanel initializeToolPanel(String btnName, String toolDescription, String toolTip, Class<?> c) {
		// Instantiate and set layout of tool pane
		JPanel toolPane = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		toolPane.setLayout(gbl);
		// Configure GridBag constraints
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		gbc.insets = new Insets(5, 5, 5, 5);
		// Add black border (for debugging purposes)
//		Border blackline = BorderFactory.createLineBorder(Color.black);
//		toolPane.setBorder(blackline);
		// Format tool button w/ name and action
		JButton btnTool = new JButton(btnName);
		btnTool.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							// Use reflections to instantiate objects and logs from a Class input
							JFrame frame = (JFrame) c.getDeclaredConstructor().newInstance();
							frame.addPropertyChangeListener("log", new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent evt) {
									// Add log item if logging is turned on
									if ("log" == evt.getPropertyName() && logs.getToggleOn()) {
										if (evt.getNewValue() != null) {
											logs.addLogItem((LogItem) evt.getNewValue());
										}
										logs.updateTable();
									}
								}
							});
							frame.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
		// Add tool tip if specified
		if (toolTip != null) {
			btnTool.setToolTipText(toolTip);
		}
		// Add JButton to spin up window instance to the tool pane
		toolPane.add(btnTool, gbc);
		// Make & add description component if specified
		if (toolDescription != null) {
			// Instantiate and configure text area for tool description
			JTextArea txtToolDescription = new JTextArea(toolDescription);
			txtToolDescription.setWrapStyleWord(true);
			txtToolDescription.setEditable(false);
			txtToolDescription.setLineWrap(true);
			// Add JTextArea description to the tool pane
			toolPane.add(txtToolDescription, gbc);
		}
		return (toolPane);
	}

	/**
	 * Create the application.
	 */
	public ScriptManagerGUI() {
		try {
			initialize();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}

	/**
	 * Launch the application.
	 */
	public void launchApplication() {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ScriptManagerGUI window = new ScriptManagerGUI();
					window.frmScriptManager.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
