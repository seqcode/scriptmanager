package scriptmanager.main;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.awt.HeadlessException;
import java.util.concurrent.Callable;

import scriptmanager.objects.ToolDescriptions;

import scriptmanager.cli.BAM_Format_Converter.BAMtoBEDCLI;
import scriptmanager.cli.BAM_Format_Converter.BAMtobedGraphCLI;
import scriptmanager.cli.BAM_Format_Converter.BAMtoGFFCLI;
import scriptmanager.cli.BAM_Format_Converter.BAMtoscIDXCLI;

import scriptmanager.cli.BAM_Manipulation.BAIIndexerCLI;
import scriptmanager.cli.BAM_Manipulation.BAMRemoveDupCLI;
import scriptmanager.cli.BAM_Manipulation.FilterforPIPseqCLI;
import scriptmanager.cli.BAM_Manipulation.MergeBAMCLI;
import scriptmanager.cli.BAM_Manipulation.SortBAMCLI;

import scriptmanager.cli.BAM_Statistics.CrossCorrelationCLI;
import scriptmanager.cli.BAM_Statistics.BAMGenomeCorrelationCLI;
import scriptmanager.cli.BAM_Statistics.PEStatsCLI;
import scriptmanager.cli.BAM_Statistics.SEStatsCLI;

import scriptmanager.cli.Coordinate_Manipulation.ShiftCoordCLI;

import scriptmanager.cli.Coordinate_Manipulation.BED_Manipulation.BEDtoGFFCLI;
import scriptmanager.cli.Coordinate_Manipulation.BED_Manipulation.ExpandBEDCLI;
import scriptmanager.cli.Coordinate_Manipulation.BED_Manipulation.SortBEDCLI;

import scriptmanager.cli.Coordinate_Manipulation.GFF_Manipulation.ExpandGFFCLI;
import scriptmanager.cli.Coordinate_Manipulation.GFF_Manipulation.GFFtoBEDCLI;
import scriptmanager.cli.Coordinate_Manipulation.GFF_Manipulation.SortGFFCLI;

import scriptmanager.cli.Figure_Generation.CompositePlotCLI;
import scriptmanager.cli.Figure_Generation.FourColorSequenceCLI;
import scriptmanager.cli.Figure_Generation.TwoColorHeatMapCLI;
import scriptmanager.cli.Figure_Generation.ThreeColorHeatMapCLI;
import scriptmanager.cli.Figure_Generation.MergeHeatMapCLI;
import scriptmanager.cli.Figure_Generation.LabelHeatMapCLI;

import scriptmanager.cli.File_Utilities.MD5ChecksumCLI;
import scriptmanager.cli.File_Utilities.CompressFileCLI;
import scriptmanager.cli.File_Utilities.ConvertBEDChrNamesCLI;
import scriptmanager.cli.File_Utilities.ConvertGFFChrNamesCLI;
import scriptmanager.cli.File_Utilities.DecompressGZFileCLI;

import scriptmanager.cli.Peak_Analysis.BEDPeakAligntoRefCLI;
import scriptmanager.cli.Peak_Analysis.FilterBEDbyProximityCLI;
import scriptmanager.cli.Peak_Analysis.FRiXCalculatorCLI;
import scriptmanager.cli.Peak_Analysis.RandomCoordinateCLI;
import scriptmanager.cli.Peak_Analysis.SignalDuplicationCLI;
import scriptmanager.cli.Peak_Analysis.SortByDistCLI;
import scriptmanager.cli.Peak_Analysis.TileGenomeCLI;

import scriptmanager.cli.Peak_Calling.GeneTrackCLI;
import scriptmanager.cli.Peak_Calling.PeakPairCLI;

import scriptmanager.cli.Read_Analysis.AggregateDataCLI;
import scriptmanager.cli.Read_Analysis.ScaleMatrixCLI;
import scriptmanager.cli.Read_Analysis.ScalingFactorCLI;
//import cli.Read_Analysis.SimilarityMatrixCLI;
import scriptmanager.cli.Read_Analysis.TagPileupCLI;
import scriptmanager.cli.Read_Analysis.TransposeMatrixCLI;

import scriptmanager.cli.Sequence_Analysis.DNAShapefromBEDCLI;
import scriptmanager.cli.Sequence_Analysis.DNAShapefromFASTACLI;
import scriptmanager.cli.Sequence_Analysis.FASTAExtractCLI;
import scriptmanager.cli.Sequence_Analysis.RandomizeFASTACLI;
import scriptmanager.cli.Sequence_Analysis.SearchMotifCLI;


/**
 * Provides command line access to ScriptManager sub-commands
 * 
 * @author William KM Lai
 */
@Command(name = "script-manager",
		subcommands = {
			BAM_Format_ConverterCLI.class,
			BAM_ManipulationCLI.class,
			BAM_StatisticsCLI.class,
			Coordinate_ManipulationCLI.class,
			Figure_GenerationCLI.class,
			File_UtilitiesCLI.class,
			Peak_AnalysisCLI.class,
			Peak_CallingCLI.class,
			Read_AnalysisCLI.class,
			Sequence_AnalysisCLI.class
		},
		version = "ScriptManager "+ ToolDescriptions.VERSION,
		mixinStandardHelpOptions = true,
		description = "Choose a tool directory from below to see more command-line tool options.",
		exitCodeOnInvalidInput = 1,
		exitCodeOnExecutionException = 1)
public class ScriptManager implements Callable<Integer> {

	@Override
	public Integer call() {
		try {
			ScriptManagerGUI gui = new ScriptManagerGUI();
			System.out.println( "Use '-h' or '--help' for command-line usage guide" );
			gui.launchApplication();
			return(0);
		} catch (HeadlessException he) {
			he.printStackTrace();
			System.out.println("\n"
					+ "(ERROR!) Caught \"java.awt.HeadlessException\""
					+ "\n\n"
					+ "====What does this mean?====\n"
					+ "This usually means you attempted to access ScriptManager's GUI on a machine that does not have a graphical interface."
					+ "\n\n"
					+ "Please confirm you are working on a system that supports graphical interfaces and if it does not, you can...\n"
					+ "  1. set up X11 forwarding or\n"
					+ "  2. switch to using the command line interface (CLI):\n"
					+ "    - https://pughlab.mbg.cornell.edu/scriptmanager-docs/docs/Guides/Getting-Started/command-line"
					+ "\n\n"
			);
		}
		return(1);
	}

	public static void main(String[] args) {
		CommandLine cmd = new CommandLine( new ScriptManager() );
		int exitCode = cmd.execute(args);
		if(exitCode != 0) {
			System.out.println("main:Exit code = " + exitCode);
			System.exit(exitCode);
		}
	}
}

@Command(mixinStandardHelpOptions = true,
		version = "ScriptManager "+ ToolDescriptions.VERSION,
		exitCodeOnInvalidInput = 1,
		exitCodeOnExecutionException = 1)
abstract class SubcommandCLI implements Callable<Integer>{
	@Override
	public Integer call(){
		CommandLine cmd = new CommandLine( this );
		cmd.usage(System.out);
		return(1);
	}
}


@Command(name = "bam-format-converter",
		subcommands = {
			BAMtoBEDCLI.class,
			BAMtobedGraphCLI.class,
			BAMtoGFFCLI.class,
			BAMtoscIDXCLI.class
		},
		description = "Includes tools like BAMtoBEDCLI, BAMtobedGraphCLI, BAMtoGFFCLI, and BAMtoscIDXCLI.")
class BAM_Format_ConverterCLI extends SubcommandCLI {}


@Command(name = "bam-manipulation",
		subcommands = {
			BAIIndexerCLI.class,
			BAMRemoveDupCLI.class,
			FilterforPIPseqCLI.class,
			MergeBAMCLI.class,
			SortBAMCLI.class
		},
		description = "Includes tools like BAIIndexerCLI, BAMRemoveDupCLI, FilterforPIPseqCLI, MergeBAMCLI, and SortBAMCLI.")
class BAM_ManipulationCLI extends SubcommandCLI {}


@Command(name = "bam-statistics",
		subcommands = {
			CrossCorrelationCLI.class,
			BAMGenomeCorrelationCLI.class,
			PEStatsCLI.class,
			SEStatsCLI.class
		},
		description = "Includes tools like SEStatsCLI, PEStatsCLI, BAMGenomeCorrelationCLI, and CrossCorrelationCLI.")
class BAM_StatisticsCLI extends SubcommandCLI {}


@Command(name = "coordinate-manipulation",
		subcommands = {
			ShiftCoordCLI.class,
			BEDtoGFFCLI.class,
			ExpandBEDCLI.class,
			SortBEDCLI.class,
			ExpandGFFCLI.class,
			GFFtoBEDCLI.class,
			SortGFFCLI.class
		},
		description = "Includes tools like BEDtoGFFCLI, ExpandBEDCLI, SortBEDCLI, ExpandGFFCLI, GFFtoBEDCLI, and SortGFFCLI.")
class Coordinate_ManipulationCLI extends SubcommandCLI {}


@Command(name = "figure-generation",
		subcommands = {
			CompositePlotCLI.class,
			FourColorSequenceCLI.class,
			TwoColorHeatMapCLI.class,
			ThreeColorHeatMapCLI.class,
			MergeHeatMapCLI.class,
			LabelHeatMapCLI.class
		},
		description = "Includes tools like FourColorSequenceCLI, HeatMapCLI, and MergeHeatMapCLI.")
class Figure_GenerationCLI extends SubcommandCLI {}


@Command(name = "file-utilities",
		subcommands = {
			MD5ChecksumCLI.class,
			ConvertBEDChrNamesCLI.class,
			ConvertGFFChrNamesCLI.class,
			CompressFileCLI.class,
			DecompressGZFileCLI.class
		},
		description = "Includes the tool MD5Checksum.")
class File_UtilitiesCLI extends SubcommandCLI {}


@Command(name = "peak-analysis",
		subcommands = {
			BEDPeakAligntoRefCLI.class,
			FilterBEDbyProximityCLI.class,
			RandomCoordinateCLI.class,
			SignalDuplicationCLI.class,
			TileGenomeCLI.class,
			SortByDistCLI.class,
			FRiXCalculatorCLI.class
		},
		description = "Includes tools like BEDPeakAligntoRefCLI, FilterBEDbyProximityCLI, RandomCoordinateCLI, SignalDuplicationCLI, and TileGenomeCLI.")
class Peak_AnalysisCLI extends SubcommandCLI {}


@Command(name = "peak-calling",
		subcommands = {
			GeneTrackCLI.class,
			PeakPairCLI.class
		},
		description = "Includes tools like GeneTrackCLI and PeakPairCLI.")
class Peak_CallingCLI extends SubcommandCLI {}


@Command(name = "read-analysis",
		subcommands = {
			AggregateDataCLI.class,
			ScaleMatrixCLI.class,
			ScalingFactorCLI.class,
// 			SimilarityMatrixCLI.class,
			TagPileupCLI.class,
			TransposeMatrixCLI.class
		},
		description = "Includes tools like AggregateDataCLI, ScaleMatrixCLI, ScalingFactorCLI, SimilarityMatrixCLI, TagPileupCLI and TransposeMatrix.")
class Read_AnalysisCLI extends SubcommandCLI {}


@Command(name = "sequence-analysis",
		subcommands = {
			DNAShapefromBEDCLI.class,
			DNAShapefromFASTACLI.class,
			FASTAExtractCLI.class,
			RandomizeFASTACLI.class,
			SearchMotifCLI.class
		},
		description = "Includes tools like DNAShapefromBEDCLI, DNAShapefromFASTACLI, FASTAExtractCLI, RandomizeFASTACLI, and SearchMotifCLI.")
class Sequence_AnalysisCLI extends SubcommandCLI {}
