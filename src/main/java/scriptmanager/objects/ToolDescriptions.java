package scriptmanager.objects;

/**
 * This class acts as a central and shared storage location for each tool's
 * description for consistentcy betweeen CLI help message and the GUI-displayed
 * text.
 * 
 * @author Olivia Lang
 * @see scriptmanager.main.ScriptManager
 * @see scriptmanager.main.ScriptManagerGUI
 * @see scriptmanager.cli.BAM_Format_Converter
 * @see scriptmanager.cli.BAM_Statistics
 * @see scriptmanager.cli.Coordinate_Manipulation
 * @see scriptmanager.cli.Figure_Generation
 * @see scriptmanager.cli.File_Utilities
 * @see scriptmanager.cli.Peak_Analysis
 * @see scriptmanager.cli.Peak_Calling
 * @see scriptmanager.cli.Read_Analysis
 * @see scriptmanager.cli.Sequence_Analysis
 */
public class ToolDescriptions {
	/**
	 * The version string for the whole tool
	 */
	public static final String VERSION = "0.15-dev";
	
	/**
	 * Message to user to direct user to open an issue ticket in case of unexpected exception. Print alongside exception's stack trace/message information
	 */
	public static final String UNEXPECTED_EXCEPTION_MESSAGE = "Unexpected exception encountered. Please copy the stack trace and open a Github issue ticket if a ticket does not already exist for your error.";

	// BAM Statistics
	public static final String se_stat_description = "Output BAM Header including alignment statistics and parameters given any indexed (BAI) BAM File.";
	public static final String pe_stat_description = "Generates Insert-size Histogram statistics (GEO requirement) and outputs BAM Header including alignment statistics and parameters given a sorted and indexed (BAI) paired-end BAM File.";
	public static final String bam_correlation_description = "Genome-Genome correlations for replicate comparisons given multiple sorted and indexed (BAI) BAM files.";
	public static final String archtex_crosscorrelation_description = ("Calculate optimal tag shift based on ArchTEx implementation (PMID:22302569)");

	// BAM Manipulation
	public static final String bam_indexer_description = "Generates BAI Index for input BAM files. Output BAI is in the same directory as input BAM file."; //*
	public static final String sort_bam_description = "Sort BAM files in order to efficiently extract and manipulate.\nRAM intensive process. If program freezes, increase JAVA heap size."; //*
	public static final String remove_duplicates_description = "Removes duplicate reads in Paired-End sequencing given identical 5' read locations. RAM intensive process. If program freezes, increase JAVA heap size."; //*
	public static final String merge_bam_description = "Merges Multiple BAM files into single BAM file. Sorting is performed automatically. RAM intensive process. If program freezes, increase JAVA heap size."; //*
	public static final String filter_pip_seq_description = "Filter BAM file by -1 nucleotide. Requires genome FASTA file.";
	public static final String downsample_sam_description = "Downsample a BAM file to a specified percentage or read count of deterministically random reads.";
	public static final String validate_sam_file_description = "Validate SAM/BAM files comprehensively, ensuring adherence to the SAM format specification and detecting errors or inconsistencies in alignment data.";
	public static final String normalize_fasta_description = "Reduce redundant sequences and normalize read coverage in a FASTA file, improving downstream analysis accuracy";
	public static final String filter_sam_reads_description = "Filter BAM files by applying user-defined filters to include or exclude specific reads based on read lists or genomic intervals";
	// BAM Format Converter
	public static final String bam_to_scidx_description = "Convert BAM file to scIDX file.";
	public static final String bam_to_gff_description = "Convert BAM file to GFF file.";
	public static final String bam_to_bed_description = "Convert BAM file to BED file.";
	public static final String bam_to_bedgraph_description = "Convert BAM file to bedGraph file.";
	public static final String sam_to_fastq_description = "Convert BAM file to FASTQ file";
	public static final String sam_format_converter_description = "Convert SAM file to Bam file or Bam file to Sam file";

	// File Utilities
	public static final String md5checksum_description = "Calculate MD5 checksum for files.";
	public static final String convertBEDChrNamesDescription = "Convert BED coordinate files between the standard SGD roman numeral chromosome names to the legacy SacCer3_cegr arabic numeral chromosome names.";
	public static final String convertGFFChrNamesDescription = "Convert GFF coordinate files between the standard SGD roman numeral chromosome names to the legacy SacCer3_cegr arabic numeral chromosome names.";
	public static final String compressFileDescription = "Compress (gzip) files.";
	public static final String decompressFileDescription = "Decompress (gzip) files.";

	// Peak Calling
	public static final String gene_track_description = "Genetrack peak-calling algorithm.";
	public static final String peak_pairing_description = "Peak-pairing algorithm.";
	public static final String replicate_match_description = "Peak-pair replicate analysis.";

	// Peak Analysis
	public static final String peak_align_ref_description = "Align BED peaks to Reference BED file creating CDT files for heatmap generation.";
	public static final String filter_bed_description = "Filter BED file using user-specified exclusion zone using the score column to determine which peak to retain.";
	public static final String tile_genome_description = "Generate a coordinate file that tiles (non-overlapping) across an entire genome.";
	public static final String rand_coord_description = "Generate random BED coordinates based on reference genome.";
	public static final String signal_dup_description = "Calculate duplication statistics at user-specified regions.";
	public static final String sort_by_dist_description = "Sort BED/GFF by distance to another BED/GFF (e.g. peak annotations).";
	public static final String frix_description = "Quantify library enrichment around a set of Reference points (Fraction of Reads in Peak/Motif, aka FRiP/FRiM).";

	// Coordinate Manipulation
	public static final String expand_bed_description = "Expand BED file given user-defined criteria."; //"Expands input BED file by adding positions to the border or around the center"
	public static final String expand_gff_description = "Expand GFF file given user-defined criteria."; //"Expands input GFF file by adding positions to the border or around the center"
	public static final String bed_to_gff_description = "Convert BED file to GFF file.";
	public static final String gff_to_bed_description = "Convert GFF file to BED file.";
	public static final String sort_bed_description = "Sort BED file by CDT file statistics."; //"Sort a CDT file and its corresponding BED file by the total score in the CDT file across the specified interval"
	public static final String sort_gff_description = "Sort GFF file by CDT file statistics."; //"Sort a CDT file and its corresponding GFF file by the total score in the CDT file across the specified interval"
	public static final String shift_coordinate_description = "Shift coordinate intervals up/downstream.";

	// Read Analysis
	public static final String tag_pileup_description = "Pileup 5' ends of aligned tags given BED and BAM files according to user-defined parameters.";
	public static final String scaling_factor_description = "Calculate scaling factor as either total tag normalization or normalization of ChIP-seq data with control. (PMID: 22883957)";
	public static final String scale_matrix_description = "Apply a user-specified scaling factor to tab-delimited matrix data.";
	public static final String aggregate_data_description = "Compile data from tab-delimited file into matrix according to user-specified metric.";
	public static final String transpose_matrix_description = "Interchange the rows and columns of tab-delimited matrix data.";


	// Sequence Analysis
	public static final String fasta_extract_description = "Generate FASTA file from indexed Genome FASTA file and BED file. Script will generate FAI index if not present in Genome FASTA folder.";
	public static final String randomize_fasta_description = "Randomize FASTA sequence for each input entry.";
	public static final String search_motif_description = "Search for an IUPAC DNA sequence motif in FASTA files with mismatches allowed.";
	public static final String dna_shape_from_bed_description = "Calculate intrinsic DNA shape parameters given BED file and Genome FASTA file. Based on Roh's lab DNAshape server data."; //%nNotes: Sequences with Ns are thrown out.
	public static final String dna_shape_from_fasta_description = "Calculate intrinsic DNA shape parameters given input FASTA files. Based on Roh's lab DNAshape server data.";

	// Figure Generation
	public static final String heatmap_description = "Generate heat map using CDT files.";
	public static final String threecolorheatmap_description = "Generate heat map with middling values.";
	public static final String merge_heatmap_description = "Merge Sense and Antisense png heatmaps.";
	public static final String four_color_description = "Generate 4Color sequence plot given FASTA file and user-defined RGB colors.";
	public static final String composite_description = "Generate a Composite Plot PNG from composite data like the output in TagPileup";
	public static final String label_heatmap_description = "Create an SVG label for heatmap inputs";

}
