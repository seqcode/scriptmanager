package scriptmanager.util;

import java.text.DecimalFormat;

/**
 * Calculates the correlation coefficient for two matrices based on different
 * methods
 * 
 * @author William KM Lai
 * @see scriptmanager.scripts.Read_Analysis.SimilarityMatrix
 */
public class SimilarityMetric {
	String metrictype = "";
	
	/**
	 * Creates a new SimilarityMetric wich uses the Pearson method
	 */
	public SimilarityMetric() {
		metrictype = "pearson";
	}
	
	/**
	 * Creates a new SimilarityMetric with a given method
	 * @param type Method to use (pearson, reflective, spearman, euclidean, or manhattan)
	 */
	public SimilarityMetric(String type) {
		metrictype = type;
	}
	
	/**
	 * Sets the method used to calcualte the correlation coefficient
	 * @param type The method used to be used for calculating the correlation coefficient
	 */
	public void setType(String type) {
		metrictype = type;
	}
	
	/**
	 * Returns the method used to calculate the correlation coefficient
	 * @return type The method used to calculate the correlation coefficient
	 */
	public String getType() {
		return metrictype;
	}
		
	/**
	 * Returns the value for two cell of the similarity matrix given two rows
	 * @param x Row one
	 * @param y Row two
	 * @return The value for two cells of the similarity matrix
	 */
	public double getScore(double[] x, double[] y) {
		double[] arrayx = x;
		double[] arrayy = y;
		
		if(metrictype.equals("pearson")) {
			return Pearson(arrayx, arrayy);
		} else if(metrictype.equals("reflective")) {
			return Euclidean(arrayx, arrayy);
		} else if(metrictype.equals("spearman")) {
			return Spearman(arrayx, arrayy);
		} else if(metrictype.equals("euclidean")) {
			return Euclidean(arrayx, arrayy);
		} else if(metrictype.equals("manhattan")) {
			return Manhattan(arrayx, arrayy);
		}
		return 0;
	}
	
	/**
	 * Calculates a value using the Pearson method given two rows of a matrix
	 * @param arrayx Row one
	 * @param arrayy Row two
	 * @return The value for two cells of a similarity matrix
	 */
	public double Pearson(double[] arrayx, double[] arrayy) {
		double xmean = 0, ymean = 0;
		int arraycount = 0;
		for(int a = 0; a < arrayx.length; a++) {
			if(!Double.isNaN(arrayx[a]) && !Double.isNaN(arrayy[a])) {
				xmean += arrayx[a];
				ymean += arrayy[a];
				arraycount++;
			}
		}
		xmean /= arraycount;
		ymean /= arraycount;
		
		double t1 = 0, t2 = 0, xv = 0, yv = 0, s = 0;
		double correlation;
		for(int a = 0; a < arrayx.length; a++) {
			if(!Double.isNaN(arrayx[a]) && !Double.isNaN(arrayy[a])) {
				t1 = arrayx[a] - xmean;
				t2 = arrayy[a] - ymean;
				xv += Math.pow(t1, 2);
				yv += Math.pow(t2, 2);
				s += (t1 * t2);
			}
		}
		if (xv == 0 || yv == 0) { correlation = 0; }
		else { correlation = (s / (Math.sqrt(xv) * Math.sqrt(yv))); } 
		DecimalFormat df = new DecimalFormat("#.############");
		correlation = Double.parseDouble(df.format(correlation));
		return correlation;
	}
	
	/**
	 * Calculates a value using the Euclidean method given two rows of a matrix
	 * @param arrayx Row one
	 * @param arrayy Row two
	 * @return The value for two cells of a similarity matrix
	 */
	private double Euclidean(double[] arrayx, double[] arrayy) {
		double distance = 0;
		for(int i = 0; i < arrayx.length; i++) {
			if(!Double.isNaN(arrayx[i]) && !Double.isNaN(arrayy[i])) { distance += Math.pow(arrayx[i] - arrayy[i], 2); }
		}
		return Math.sqrt(distance);
	}
	
	/**
	 * Calculates a value using the Manhattan method given two rows of a matrix
	 * @param arrayx Row one
	 * @param arrayy Row two
	 * @return The value for two cells of a similarity matrix
	 */
	private double Manhattan(double[] arrayx, double[] arrayy) {
		double distance = 0;
		for(int i = 0; i < arrayx.length; i++) {
			if(!Double.isNaN(arrayx[i]) && !Double.isNaN(arrayy[i])) { distance += Math.abs(arrayx[i] - arrayy[i]); }
		}
		return distance;
	}
	
	/**
	 * Calculates a value using the Spearman method given two rows of a matrix
	 * @param arrayx Row one
	 * @param arrayy Row two
	 * @return The value for two cells of a similarity matrix
	 */
	private double Spearman(double[] arrayx, double[] arrayy) {
		double[] rankX = SpearRank(arrayx);
		double[] rankY = SpearRank(arrayy);
		return Pearson(rankX, rankY);
	}
	
	/**
	 * Calculates the SpearRans[] for a given row
	 * @param x Row to analyze
	 * @return The SpearRank values for the given row
	 */
	private double[] SpearRank(double[] x) {
		double[] newx = new double[x.length];
		int j, k, t, tmpi;
        double tmp;

        double[] r = new double[x.length];
        int[] c = new int[x.length];

        if(x.length <= 1) { newx[0] = 1; return newx; }
        for(int i = 0; i < x.length; i++) {
        	r[i] = x[i];
            c[i] = i;
        }
        for(int i = 2; i < x.length; i++) {
            t = i;
            while (t != 1) {
            	k = t / 2;
                if (r[k - 1] >= r[t - 1]) {
                	t = 1;
                } else {
                	tmp = r[k - 1];
                    r[k - 1] = r[t - 1];
                    r[t - 1] = tmp;
                    tmpi = c[k - 1];
                    c[k - 1] = c[t - 1];
                    c[t - 1] = tmpi;
                    t = k;
                	}
            }
        }
        for(int i = x.length - 1; i > 1; i--) {
        	tmp = r[i];
            r[i] = r[0];
            r[0] = tmp;
            tmpi = c[i];
            c[i] = c[0];
            c[0] = tmpi;
            t = 1;
            while (t != 0) {
            	k = 2 * t;
            	if (k > i) { t = 0; }
                else {
                	if (k < i) {
                		if (r[k] > r[k - 1]) { k++; }
                    }
                	if (r[t - 1] >= r[k - 1]) { t = 0; }
                    else {
                    	tmp = r[k - 1];
                        r[k - 1] = r[t - 1];
                        r[t - 1] = tmp;
                        tmpi = c[k - 1];
                        c[k - 1] = c[t - 1];
                        c[t - 1] = tmpi;
                        t = k;
                    }
                }
            }
        }
        int i = 0;
        while (i < x.length) {
        	j = i + 1;
        	while (j < x.length) {
        		if (r[j] != r[i]) { break; }
        		j++;
        	}
        	for (k = i; k <= j - 1; k++) {
        		r[k] = 1 + (double)(i + j - 1) / (double)(2);
        	}
        	i = j;
        }

		for (i = 0; i < x.length; i++) {
			newx[c[i]] = r[i];
		}
		return newx;
	}
	
	/**
	 * Returns the type of metric used for correlation calculations
	 */
	public String toString() {
		return metrictype;
	}
}