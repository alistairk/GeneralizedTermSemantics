package GenTS.WeightMatrix;

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
public class MatrixWeighter {
	
	
	/**
	 * This static method can be called with the input for a 2x2 confusion matrix and the name of an 
	 * association measure. The matrix is represented by the arguments true-positive, false-negative
	 * false-positive and true-positive. If the resulting similarity is less than zero then it is set 
	 * to zero and returned.
	 * 
	 * @param tp
	 * @param fn
	 * @param fp
	 * @param tn
	 * @param measureType
	 * @return
	 */
	public static double getAssociation(double tp, double fn, double fp, double tn, String measureType){
		double[][] observed = formatInputs(tp, fn, fp, tn);
		double[][] expected = generateExpectedFromObserved(observed);
		double toReturn = 0;
		if(measureType.equals("Tscore")){
			toReturn = ttest(observed, expected);
		}
		else if(measureType.equals("Zscore")){
			toReturn = zscore(observed, expected);
		}
		else if(measureType.equals("F")){
			toReturn = F(observed);
		}
		else if(measureType.equals("PMI")){
			toReturn = pmi(observed, expected);
		}
		else if(measureType.equals("LL")){
			toReturn = LL(observed, expected);
		}
		else if(measureType.equals("Chi2")){
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
		double N = 0;
		double[] R = new double[observed.length];
		double[] C = new double[observed[0].length];
		double[][] expected = new double[observed.length][observed[0].length];
		
		//get values to calculate expected
		for(int i = 0; i < observed.length; i++){ //row
			for(int j = 0; j < observed[i].length; j++){ //column
				R[i] += observed[i][j];
				C[j] += observed[i][j];
				N += observed[i][j];
			}
		}
		
		//calculate expected
		for(int i = 0; i < observed.length; i++){ //row
			for(int j= 0; j < observed[i].length; j++){ //column
				expected[i][j] = R[i]*C[j]/N;
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
	 * @param x
	 * @return
	 */
	private static double log2(double x){
		if(x == 0){
			return -999;
		}
		return Math.log(x)/Math.log(2);
	}
	
	/**
	 * Gets the sum of a row.
	 * 
	 * @param observed
	 * @param index
	 * @return
	 */
	public static double getR(double[][] observed, int index){
		double R = 0;
		for(int j= 0; j < observed[index].length; j++){
			R += observed[index][j];
		}
		return R;
	}
	
	/**
	 * Gets the sum of a column
	 * 
	 * @param observed
	 * @param index
	 * @return
	 */
	public static double getC(double[][] observed, int index){
		double C = 0;
		for(int j= 0; j < observed.length; j++){ 
			C += observed[j][index];
		}
		return C;
	}
	
	/**
	 * Calculates f-measure
	 * 
	 * @param observed
	 * @return
	 */
	public static double F(double[][] observed){
		double P = observed[0][0]/getR(observed,0);
		double R = observed[0][0]/getC(observed,0);
		if(P == 0.0 || R == 0.0){
			return 0;
		}
		return 2.0 * P * R/(P + R);
	}
	
	/**
	 * Calculates Z-Score ratio.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double zscore(double[][] observed, double[][] expected){
		if(observed[0][0] == 0){
			return 0;
		}
		return (observed[0][0]-expected[0][0])/Math.sqrt(expected[0][0]);
	}
	
	/**
	 * Calculates T-Test ratio.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double ttest(double[][] observed, double[][] expected){
		if(observed[0][0] == 0){
			return 0;
		}
		return (observed[0][0]-expected[0][0])/Math.sqrt(observed[0][0]);
	}
	
	/**
	 * calculates PMI.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double pmi(double[][] observed, double[][] expected){
		if(observed[0][0] == 0){
			return 0;
		}
		return log2(observed[0][0]/expected[0][0]);
	}
	
	
	/**
	 * Calculates Chi 2.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double chi2(double[][] observed, double[][] expected){
		if(observed[0][0] == 0){
			return 0;
		}
		double chi2 = 0;
		for(int i = 0; i < observed.length; i++){ //row
			for(int j = 0; j < observed[i].length; j++){ //column
				chi2 += Math.pow(observed[i][j]-expected[i][j], 2)/expected[i][j];
			}
		}
		return chi2;
	}
	
	/**
	 * Calculates the log likelihood ratio.
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	public static double LL(double[][] observed, double[][] expected){
		double LL = 0;
		for(int i = 0; i < observed.length; i++){ //row
			for(int j = 0; j < observed[i].length; j++){ //column
				if(observed[i][j] != 0){
					LL += observed[i][j] * log2(observed[i][j]/expected[i][j]);
				}
			}
		}
		return 2*LL;
	}

}
