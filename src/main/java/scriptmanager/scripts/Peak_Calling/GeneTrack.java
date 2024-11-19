package scriptmanager.scripts.Peak_Calling;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * (Dev) GUI for running a gene track window
 * 
 * @author William KM Lai
 */
@SuppressWarnings("serial")
public class GeneTrack extends JFrame {
	private JTextArea textArea;
	
	private File INPUT = null; // input tab file
	private String OUT_PATH = ""; // Path to output directory

	private int SIGMA;		// sigma to use when smoothing reads to call peaks
	private int EXCLUDE;	// exclusion zone around each peak that prevents others from being called
	private int UP_WIDTH;	// upstream width of called peaks
	private int DOWN_WIDTH; // downstream width of called peaks
	private int FILTER;		// absolute read filter, outputs only peaks with larger read count
	
	private int CHUNK;		// the base pair size of the data processed at once, will not affect output
	
	private int WIDTH;		// equivalent to the half the gaussian smoothing kernel distribution size
	private int frameshift;
	
	private double[] sense = null; 		// sense - raw data from the input tab file, taken from column 3 
	private double[] anti = null;		// anti - raw data from the input tab file, taken from column 4
	
	private String chromname = null;	// chromname - name of the current chromosome, changes when column 1 does not match the previous column 1
	private long chromline = 0;			// chromline - size of how many chunks have passed, changes when a value from column 2 exceeds chromline's previous value + chunk size

	private BufferedWriter write;		// read/write - buffered reader and writer to handle data input/output
	private BufferedReader read;

	private boolean empty = true;		// empty - determines if there is data to process, changes after any data has been added to either array
	private ArrayList<String[]> error = new ArrayList<String[]>(); // error - counts the amount of unreadable lines in a chunk
	
	public GeneTrack(File id, int s, int e, int f, int u, int d, String path) {
		setTitle("GeneTrack Progress");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(150, 150, 600, 800);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		INPUT = id;
		
		SIGMA = s;
		EXCLUDE = e;
		UP_WIDTH = u;
		DOWN_WIDTH = d;
		FILTER = f;
		OUT_PATH = path;
		CHUNK = 100000;
				
		WIDTH = SIGMA * 5;
		
		//frameshift parameter that is used to remove chunk overlap is the maximum of UP, DOWN and WIDTH parameters
		if (DOWN_WIDTH > WIDTH) {
			frameshift = DOWN_WIDTH;
		} else {
			frameshift = WIDTH;
		}
		
		if (frameshift < UP_WIDTH) {
			frameshift = UP_WIDTH;
		}
	}

	public void run() {	
		//finds the input file name and adds parameters to the label
		String NAME = INPUT.getName().split("\\.")[0] + "_s" + SIGMA + "e" + EXCLUDE;
		if(UP_WIDTH != EXCLUDE / 2) NAME += "u" + UP_WIDTH;
		if(DOWN_WIDTH != EXCLUDE / 2) NAME += "d" + DOWN_WIDTH;
		NAME += "F" + FILTER + ".gff";
		System.out.println("Processing: " + NAME);
		
		//creates output gff file in the output folder
		try {
			write = new BufferedWriter(new FileWriter(OUT_PATH + File.separator + NAME));
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		//holds the new line before it is broken by delimiter
		String passedline = null;
		
		sense = new double[(int) CHUNK];
		anti = new double[(int) CHUNK];
		Arrays.fill(sense, (double) 0);					
		Arrays.fill(anti, (double) 0);

		try {
			read = new BufferedReader(new FileReader(INPUT));	
			try {
				while((passedline = read.readLine()) != null) {	  
					arrayFill(passedline.split("\\s+"));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} 

		//forces the output of the last chromosome as processing is triggered when a new chunk is found
		chromBreak("DONE", true, true);
		try {
			read.close(); 
			write.close();
		} catch (IOException e1) { e1.printStackTrace(); }
		
	}

	private void arrayFill(String[] line) {
		 //checks for errors of line length and line composition (of numbers)
		 if (line.length > 4 && line.length <= 5 && line[1].matches("[0-9]+") && line[2].matches("[0-9]+") && line[3].matches("[0-9]+")) {
			 if (!empty && !line[0].equals(chromname) && chromname != null) { // processes the data held if a new chromosome is found
			 	chromBreak(line[0], true, false);
				chromline = 0;
			 } else if (chromname == null) { // if this is the first line, load the chromosome name and do not process as no data is held
				chromname = line[0];
			 }

			 //processes the data held as the chunk is filled
			 if (!empty && Long.parseLong(line[1]) >= CHUNK + chromline) {				 
				 //exclude first value as it cannot be a peak
				 if (chromline == 0) {	chromBreak(line[0], false, true); }
				 //do not exclude first value, it can be a valid peak
				 else { chromBreak(line[0], false, false); }
			 }	

			//increase chunk count after processing until it can hold the new indexes
			while ((Long.parseLong(line[1])) >= CHUNK + chromline) { chromline += CHUNK; }

			//save sense line
			if (line[2].matches("[0-9]+") && Integer.parseInt(line[2]) > 0) {
				sense[(int) (Integer.parseInt(line[1]) - chromline)] = Double.parseDouble(line[2]);
			}		
			
			//save anti line
			if (line[3].matches("[0-9]+") && Integer.parseInt(line[3]) > 0) {
				anti[(int) (Integer.parseInt(line[1]) - chromline)] = Double.parseDouble(line[3]);
			}				

			//used to skip over empty chunks
			empty = false;
		 } else {
			//if an error is found, print it later to the user
			 error.add(line); 
		 }	 
	}

	private void chromBreak(String nextcname, boolean getLast, boolean getFirst) {		

		//sense processing + file writing
		smoothPeaks(sense, false, getLastValue(getLast), getFirstValue(getFirst));
		fileWriter(getPeaks(), "+");
	
		//anti processing + file writing
		smoothPeaks(anti, true, getLastValue(getLast), getFirstValue(getFirst));	
		fileWriter(getPeaks(), "-");
		
		//resets both raw data arrays
		for (int z = 0; z < CHUNK; z++) {
			sense[z] = 0;
			anti[z] = 0;
		}
	
		//resets empty state and increments the chomosome name
		empty = true;
	    chromname = nextcname;
		    
	}

 	private int getLastValue(boolean getLast) {
 		//finds the last value of a chromosome that is not a valid peak
	 	if (getLast) {
 			int lastSenseValue = 0;
 			int lastAntiValue = 0;
			int ii = 1;
			while (lastSenseValue == 0 && lastAntiValue == 0) {
				lastSenseValue = (int) sense[(int) (CHUNK - ii)];
				lastAntiValue = (int) anti[(int) (CHUNK - ii)];
				ii ++;
			}
			return (int) (CHUNK - ii + 1 + WIDTH);
	 	}
		
		else {
			return -1;
		}
 	}
 	
 	private int getFirstValue(boolean getFirst) {
 		//finds the first value of a chromosome that is not a valid peak
	 	if (getFirst) {
 			int firstSenseValue = 0;
 			int firstAntiValue = 0;
			int ii = 0;
			while (firstSenseValue == 0 && firstAntiValue == 0) {
				firstSenseValue = (int) sense[ii];
				firstAntiValue = (int) anti[ii];
				ii ++;
			}
			return (int) (ii + WIDTH);
	 	}
		
		else {
			return -1;
		}
 	}
	
	private void fileWriter(float[][] peaks, String s) {
		int first = UP_WIDTH;
		int second = DOWN_WIDTH;
		
		//switches up and down parameters for anti strand
		if (s == "-") {
			first = DOWN_WIDTH;
			second = UP_WIDTH;
		}

		int front = 0;

			for (int i = 0; i < CHUNK; i++) {

				if (peaks[i][0] > FILTER) {
				
					//prevents negative indexes 
					if (i + chromline > first + frameshift) {
						front = (int) (chromline + i) - first - frameshift;
					}
					
					else {
						front = 1;
					}															

					try {
						write.write(	
						//chromosome name	
						chromname +		
						//source(genetrack), placeholder(.)
						"\tgenetrack\t.\t" +	
						//starting position
						front +								
						"\t" +
						//ending position
						(i + second + chromline - frameshift) + 		
						"\t" +
						//score sum of the peak
						(int) peaks[i][0] +		
						"\t" +
						//strand (+ or -)
						s +	
						//placeholder(.)
						"\t.\t"	
						//standard deviation
						+ "stddev=" + peaks[i][1] +	
						"\n");

						write.flush();
					}

					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		System.out.println(chromname + ", chunk " + chromline + ", strand " + s + "\n");
		System.out.println("ERRORS: ");
		if (!error.isEmpty()) {
			for (String[] errorline : error) {
				for (String erroritem : errorline) {
					System.out.print(erroritem + " ");
				}
			}
		}
		else {
			System.out.print("none");
		}
		System.out.println("\n\n");
		error.clear();
	}

	private float[][] peaks;
	private double[] data;
	private double[] NORM;
	
	private int firstvalue;

	/**
	 * Holds the end overlapping region of the anti strand
	 */
	private double[] antidata;
	/**
	 * Holds the end overlapping region of the sense strand
	 */
	private double[] sensedata;
	/**
	 * Holds the end overlapping sum values of the sense strand
	 */
	private ArrayList<Integer> sensehold = new ArrayList<Integer>();
	/**
	 * Holds the end overlapping sum values of the anti strand
	 */	
	private ArrayList<Integer> antihold = new ArrayList<Integer>();
	/**
	 * Holds the moving sum value over the end overlap for the sense strand
	 */
	private int sensesum = 0;
	/**
	 * Holds the moving sum value over the end overlap for the anti strand
	 */
	private int antisum = 0;
	/**
	 * Holds the standard deviation sum over the end overlap for the sense strand
	 */
	private double senseavg = 0;
	/**
	 * Holds the standard deviation sum over the end overlap for the anti strand
	 */
	private double antiavg = 0;
	private ArrayList<Integer> antistand = new ArrayList<Integer>();	
	private ArrayList<Integer> sensestand = new ArrayList<Integer>();
	
	public void smoothPeaks(double[] raw, boolean reverse, int excludeLast, int excludeFirst) {
		
		boolean cut = true;
		
		if (excludeFirst > -1) {
			firstvalue = excludeFirst;
			cut = false;
		}
				
		//held data is loaded into these placeholders to be used
		ArrayList<Integer> hold = new ArrayList<Integer>();
		ArrayList<Integer> standdev = new ArrayList<Integer>();		
		int sum = 0;
		double avg = 0;
		int lastadd = 0;
		
		//if any data holder if empty, fill it
		if (antidata == null || antidata.length <= 0) {
			antidata = new double[WIDTH + frameshift];
			Arrays.fill(antidata, 0);
		}
		
		if (sensedata == null || sensedata.length <= 0) {
			sensedata = new double[WIDTH + frameshift];
			Arrays.fill(sensedata, 0);
		}
		
		int first = UP_WIDTH;
		int second = DOWN_WIDTH;
		
		//load shift data and corresponding values
		if (reverse) {
			for (int shift = 0; shift < WIDTH + frameshift; shift ++) {
				data[shift] = antidata[shift];
			}
			hold = antihold;
			sum = antisum;
			avg = antiavg;
			standdev = antistand;
			first = DOWN_WIDTH;
			second = UP_WIDTH;
		}
		
		else {
			for (int shift = 0; shift < WIDTH + frameshift; shift ++) {
				data[shift] = sensedata[shift];
			}
			hold = sensehold;
			sum = sensesum;
			avg = senseavg;
			standdev = sensestand;
		}
			
		for (int reset = WIDTH + frameshift; reset < CHUNK; reset++) {
			data[reset] = 0;
			peaks[reset][0] = 0;
		}
		
		//reset only beginning peak data as we just shifted the smoothed data
		for (int reset = 0; reset < WIDTH + frameshift; reset ++) {
			peaks[reset][0] = 0;
		}
		
		//adds zeroes for the overhang to the left of the 0 bp
		if (hold.isEmpty()) {
			while (hold.size() < first + second + 1) {
				hold.add(0);
			}
		}

		int b = 0;
		//preloads values to the right of the 0 bp
		while (hold.size() < first + second + 1) {
			hold.add((int) raw[b]);
			sum += raw[b];
			b ++;
		}
		
		//preloads additional values skipped by the parser
		while (hold.size() < first + frameshift + 1) {
			hold.add((int) raw[b]);
			b++;
		}
				
		//iterates through one chunk and therefore can only call one chunk of peaks
		for (int line = 0; line < CHUNK; line ++) {	
			
			//superimposes the spread data over the previous distribution
			if (raw[line] != 0) {
				for (int i = line - WIDTH; i < line + WIDTH; i ++) {	
					data[i + frameshift] = data[i + frameshift] + raw[line] * NORM[i - line + WIDTH];	
				}
			}
					
			//if a nonzero read is found, add it to the hold sum array and standdard deviation hold array 
			if (line > 0 && raw[line - 1] > 0) {
								
				for (int c = 0; c < raw[line - 1]; c++) {
					standdev.add(line - 1);
				}
				
				hold.add((int) raw[line - 1]);	
			}
			
			//else add a zero placeholder
			else {
				hold.add(0);
			}
			
			//add the read value to correctly size the sum range
			sum += hold.get(first + second + 1);
			
			//add the standard deviation value if it is within range
			while (lastadd < standdev.size() && standdev.get(lastadd) <= line + second - frameshift - 1) {
				avg += standdev.get(lastadd);
				lastadd ++;
			}
			
			//remove the hold array value that is now out of range
			sum -= hold.get(0);		
			hold.remove(0);
			
			//remove the standard deviation that is now out of range
			while (!standdev.isEmpty() && standdev.get(0) < line - frameshift - first - 1) {
				avg -= standdev.get(0);
				standdev.remove(0);
				lastadd --;
			}
			
			//call the peaks
			if (line < CHUNK - 1 && line >= 2) {
				if (data[line - 2] < data[line - 1] && data[line - 1] > data[line] && sum > FILTER) {
						peaks[line - 1][0] = origChange(line - 1, sum, hold, second, first, cut);  //sum;
						peaks[line - 1][1] = standDev(standdev, avg, lastadd);		
				}
			}
		}

		//save shift data and corresponding values
		if (reverse) {
			for (int shift = 0; shift < WIDTH + frameshift; shift ++) {
				antidata[shift] = data[shift + CHUNK];
				data[shift + CHUNK] = 0;
			}
			antihold = hold;
			antisum = sum;
			antiavg = avg;
			antistand = standdev;
		}
		
		else {
			for (int shift = 0; shift < WIDTH + frameshift; shift ++) {
				sensedata[shift] = data[shift + CHUNK];
				data[shift + CHUNK] = 0;
			}
			sensehold = hold;
			sensesum = sum;
			senseavg = avg;
			sensestand = standdev;
		}
		
		avg = 0;
		standdev.clear();
		
		//call peak exclusion
		peakExclude(excludeLast, excludeFirst, reverse);
	}
 
	private float origChange(int line, int sum, ArrayList<Integer> hold, int second, int first, boolean cut) {
		if (cut) {
			if (line + second > (firstvalue + frameshift - WIDTH) && line < (firstvalue + frameshift - WIDTH)) {
				for (int ii = (firstvalue + frameshift - WIDTH) - (line - first) + WIDTH; ii < hold.size(); ii ++) {
					sum -= hold.get(ii);
				}
			}
			

			if (line - first < (firstvalue + frameshift - WIDTH) && line > (firstvalue + frameshift - WIDTH)) {
				for (int ii = 0; ii < (firstvalue + frameshift - WIDTH) - (line - first) - WIDTH; ii ++) {
					sum -= hold.get(ii);
				}
			}
		}
		return sum;
	}
	
	private void peakExclude(int excludeLast, int excludeFirst, boolean reverse) {
				
		int size = 0;
		
		//MAJOR PROBLEM WHEN USING DIFFERENT U AND D PARAMETERS, WILL BE INCLUDED IN A BUG FIX LATER, SHOULD BE size = up when up is greater
//		if (first > second) {size = second;}
//		else {size = first;}
		
		size = EXCLUDE / 2;
				
		ArrayList<Integer> excluded = new ArrayList<Integer>();	
		
		for (int index = 0; index < CHUNK; index++) {
			if (peaks[index][0] > 0) {
				
				//if a nonzero index is found, check for adjacent peaks
				for (int n = 1; n <= size; n++) {		
					if (index + n < peaks.length && peaks[index + n][0] > 0) {
							
						//add the lower peak to be removed later
						if (peaks[index][0] < peaks[index + n][0]) {
							excluded.add(index);
						}
						
						else {
							excluded.add(index + n);
						}
					}
				}
			}
		}

		//removes the last index if at the end of a chromosome and resets the hold arrays
		if (excludeLast > -1) {
			excluded.add(excludeLast);
			
			if (reverse) {
				antihold.clear();
				antisum = 0;
				antiavg = 0;
				antistand.clear();
			}
			
			else {
				sensehold.clear();
				sensesum = 0;
				senseavg = 0;
				sensestand.clear();
			}
		}
		
		if (excludeFirst > -1) {
			excluded.add(excludeFirst);
		}
		
		//removes excluded peaks
		for (int remove : excluded) {
			peaks[remove][0] = 0;
		}	
		
		excluded.clear();					
	}

	private float standDev(ArrayList<Integer> standdev, double avg, int lastadd) {
		
		double total = 0;
					
		for (int i = 0; i < lastadd; i++) {
			total += Math.pow(Math.abs((avg / lastadd) - standdev.get(i)), 2);
		}
		
		total = total / lastadd;
		return (float) Math.pow(total, 0.5);
	}

	public float[][] getPeaks() {
		return peaks;
	}
	
	public void emptyPeaks() {
		Arrays.fill(peaks, 0);
	}	
}