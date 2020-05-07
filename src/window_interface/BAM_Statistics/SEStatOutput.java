package window_interface.BAM_Statistics;

import htsjdk.samtools.AbstractBAMFileIndex;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.CustomOutputStream;
import scripts.BAM_Statistics.SEStats;

@SuppressWarnings("serial")
public class SEStatOutput extends JFrame {
	Vector<File> bamFiles = null;
	File output = null;
	//SAMFileReader reader;
	
	final SamReaderFactory factory = SamReaderFactory.makeDefault().enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS).validationStringency(ValidationStringency.SILENT);
	PrintStream OUT = null;
	
	private JTextArea textArea;
	private PrintStream jtxtPrintStream;
	
	public SEStatOutput(Vector<File> input, File o) {
		setTitle("BAM File Statistics");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);

		bamFiles = input;
		output = o;
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		jtxtPrintStream = new PrintStream( new CustomOutputStream(textArea) );
		
	}
	
	public void run() {
		
		if(output != null) {
			try {
				OUT = new PrintStream(output);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		// Execute on each BAM file in the list
		for(int x = 0; x < bamFiles.size(); x++) {
			// Use script and pass PrintStream object that sends to JTextArea
			SEStats.getSEStats( output, bamFiles.get(x), jtxtPrintStream );
		}
		if(output != null) OUT.close();
		//BAMIndexMetaData.printIndexStats(bamFiles.get(x))
	}
	
	public static void main(String[] args) {
		System.out.print("java -cp ");
		//Output full path of ScriptManager
		try { System.out.print(new File(SEStats.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath()); }
		catch (URISyntaxException e) { e.printStackTrace(); }
		System.out.println(" scripts.BAM_Statistics.SEStats");
	}
}