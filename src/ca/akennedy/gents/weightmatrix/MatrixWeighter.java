package ca.akennedy.gents.weightmatrix;

/**
 * This class is made up of static methods for calculating a variety of measures of association.
 * Each method is called statically. matrices of observed and expected values are calculated and
 * then used in the measure of association.
 * 
 * New measures of association can easily be written and added and called from the getAssociation method.
 *
 * @author akennedy
 *
 */
public final class MatrixWeighter {

	private MatrixWeighter(){}
	
	/**
	 * This static method can be called with the input for a 2x2 confusion matrix and the name of an 
	 * association measure. The matrix is represented by the arguments true-positive, false-negative
	 * false-positive and true-positive. If the resulting similarity is less than zero then it is set 
	 * to zero and returned.
	 * 
	 * @param truePos
	 * @param falseNeg
	 * @param falsePos
	 * @param trueNeg
	 * @param measureType
	 * @return
	 */
	public static double getAssociation(double truePos, double falseNeg, double falsePos, double trueNeg, String measureType){
		double[][] observed = formatInputs(truePos, falseNeg, falsePos, trueNeg);
		double[][] expected = generateExpectedFromObserved(observed);
		double toReturn = 0;
		if("Tscore".equals(measureType)){
			toReturn = ttest(observed, expected);
		}
		else if("Zscore".equals(measureType)){
			toReturn = zscore(observed, expected);
		}
		else if("Dice".equals(measureType)){
			toReturn = dice(observed);
		}
		else if("PMI".equals(measureType)){
			toReturn = pmi(observed, expected);
		}
		else if("LL".equals(measureType)){
			toReturn = logLik(observed, expected);
		}
		else if("Chi2".equals(measureType)){
			toReturn = chi2(observed, expected);
		}
		
		if(toReturn < 0){
			toReturn = 0;
		}
		
		return toReturn;
	}
	

	
	/**
	 * Takes as input a matrix of observed co-occurrences and produces a matrix
	 * of the expected co-occurrences. Sums of the rows and columns are used
	 * to generate the expected values.
	 * 
	 * @param observed
	 * @return
	 */
	public static double[][] generateExpectedFromObserved(double[][] observed){
		double total = 0;
		double[] row = new double[observed.length];
		double[] column = new double[observed[0].length];
		double[][] expected = new double[observed.length][observed[0].length];
		
		//get values to calculate expected
		for(int i = 0; i < observed.length; i++){ //row
			for(int j = 0; j < observed[i].length; j++){ //column
				row[i] += observed[i][j];
				column[j] += observed[i][j];
				total = total + observed[i][j];
			}
		}
		
		//calculate expected
		for(int i = 0; i < observed.length; i++){ //row
			for(int j= 0; j < observed[i].length; j++){ //column
				expected[i][j] = row[i]*column[j]/total;
			}
		}
		
		return expected;
	}
	
	
	/**
	 * Takes a set of true positives, false negatives, etc. and builds a 2x2 matrix.
	 * 
	 * @param SG2F
	 * @param SG1F
	 * @param nSG2F
	 * @param nSG1F
	 * @return
	 */
	public static double[][] formatInputs(double SG2F, double SG1F, double nSG2F, double nSG1F){
		double[][] toReturn = new double[2][2];
		toReturn[0][0] = SG2F; //tp
		toReturn[0][1] = SG1F; //fn
		toReturn[1][0] = nSG2F; //fp
		toReturn[1][1] = nSG1F; //tn
		return toReturn;
	}
	
	/**
	 * Log base 2
	 * 
	 * @param number
	 * @return
	 */
	private static double log2(double number){
		double log = Double.MIN_VALUE;
		if(number > 0){
			log = Math.log(number)/Math.log(2);
		}
		return log;
	}
	
	/**
	 * Gets the sum of a row.
	 * 
	 * @param observed
	 * @param index
	 * @return
	 */
	public static double getR(double[][] observed, int index){
		double row = 0;
		for(int j= 0; j < observed[index].length; j++){
			row += observed[index][j];
		}
		return row;
	}
	
	/**
	 * Gets the sum of a column
	 * 
	 * @param observed
	 * @param index
	 * @return
	 */
	public static double getC(double[][] observed, int index){
		double column = 0;
		for(int j= 0; j < observed.length; j++){ 
			column += observed[j][index];
		}
		return column;
	}
	
	/**
	 * Calculates Dice coefficient.
	 * 
	 * @param observed
	 * @return
	 */
	public static double dice(double[][] observed){
		double precision = observed[0][0]/getR(observed,0);
		double recall = observed[0][0]/getC(observed,0);
		double value = 0;
		if(precision != 0.0 && recall != 0.0){
			value = 2.0 * precision * recall/(precision + recall);
		}
		return value;
	}
	
	/**
	 * Calculates Z-Score ratio.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double zscore(double[][] observed, double[][] expected){
		double value = 0;
		if(observed[0][0] != 0){
			value = (observed[0][0]-expected[0][0])/Math.sqrt(expected[0][0]);
		}
		return value;
	}
	
	/**
	 * Calculates T-Test ratio.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double ttest(double[][] observed, double[][] expected){
		double value = 0;
		if(observed[0][0] != 0){
			value = (observed[0][0]-expected[0][0])/Math.sqrt(observed[0][0]);
		}
		return value;
	}
	
	/**
	 * calculates PMI.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double pmi(double[][] observed, double[][] expected){
		double value = 0;
		if(observed[0][0] != 0){
			value = log2(observed[0][0]/expected[0][0]);
		}
		return value;
	}
	
	
	/**
	 * Calculates Chi 2.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double chi2(double[][] observed, double[][] expected){
		double value = 0;
		if(observed[0][0] != 0){
			for(int i = 0; i < observed.length; i++){ //row
				for(int j = 0; j < observed[i].length; j++){ //column
					value += Math.pow(observed[i][j]-expected[i][j], 2)/expected[i][j];
				}
			}
		}
		return value;
	}
	
	/**
	 * Calculates the log likelihood ratio.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double logLik(double[][] observed, double[][] expected){
		double value = 0;
		if(observed[0][0] != 0){
			for(int i = 0; i < observed.length; i++){ //row
				for(int j = 0; j < observed[i].length; j++){ //column
					if(observed[i][j] != 0){
						value += observed[i][j] * log2(observed[i][j]/expected[i][j]);
					}
				}
			}
		}
		return 2*value;
	}

}
