package MatrixFormat.WeightMatrix;

/**
 * This class is made up of static methods for calculating a variety of measures of association.
 *
 * @author akennedy
 *
 */
public class MatrixWeighter {
	
	/**
	 * Contains some test cases for this class.
	 * @param args
	 */
	public static void main(String[] args){
		double[][] observed = formatInputs(8, 4667, 15820,14287173);
		double[][] expected = generateExpectedFromObserved(observed);
		for(int i = 0; i < observed.length; i++){ //row
			for(int j= 0; j < observed[0].length; j++){ //column
				System.out.print(observed[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		for(int i = 0; i < expected.length; i++){ //row
			for(int j= 0; j < expected[0].length; j++){ //column
				System.out.print(expected[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
		int a = 10, b = 23, c = 880,d = 33233;
		System.out.println(getTest(a,b,c,d, "F"));
		System.out.println(getTest(a,c,b,d, "F"));
		System.out.println();
		System.out.println(getTest(a,b,c,d, "Tscore"));
		System.out.println(getTest(a,c,b,d, "Tscore"));
		System.out.println();
		System.out.println(getTest(a,b,c,d, "Zscore"));
		System.out.println(getTest(a,c,b,d, "Zscore"));
		System.out.println();
		System.out.println(getTest(a,b,c,d, "PMI"));
		System.out.println(getTest(a,c,b,d, "PMI"));
		System.out.println("LL");
		System.out.println(getTest(a,b,c,d, "LL"));
		System.out.println(getTest(a,c,b,d, "LL"));
		System.out.println("chi2");
		System.out.println(getTest(a,b,c,d, "Chi2"));
		System.out.println(getTest(a,c,b,d, "Chi2"));
		System.out.println();
		System.out.println(getTest(a,b,c,d, "InfoGain"));
		System.out.println(getTest(a,c,b,d, "InfoGain"));
		System.out.println();
		System.out.println();
		
		System.out.println("a");
		System.out.println(getTest(4,20,20,50000, "PMI"));
		System.out.println("b");
		System.out.println(getTest(400,2000,2000,5000000, "PMI"));
	}
	
	/**
	 * This static method can be called with the input for a 2x2 confusion matrix and the name of a test.
	 * If the resulting similarity is less than zero then it is set to zero and returned.
	 * 
	 * @param SG2F
	 * @param SG1F
	 * @param nSG2F
	 * @param nSG1F
	 * @param test
	 * @return
	 */
	public static double getTest(double SG2F, double SG1F, double nSG2F, double nSG1F, String test){
		double[][] observed = formatInputs(SG2F, SG1F, nSG2F, nSG1F);
		double[][] expected = generateExpectedFromObserved(observed);
		double toReturn = 0;
		if(test.equals("Tscore")){
			toReturn = ttest(observed, expected);
		}
		else if(test.equals("Zscore")){
			toReturn = zscore(observed, expected);
		}
		else if(test.equals("InfoGain")){
			toReturn = infoGain(observed);
		}
		else if(test.equals("F")){
			toReturn = F(observed);
		}
		else if(test.equals("PMI")){
			toReturn = pmi(observed, expected);
		}
		else if(test.equals("LL")){
			toReturn = LL(observed, expected);
		}
		else if(test.equals("Chi2")){
			toReturn = chi2(observed, expected);
		}
		
		/*if(SG2F < 0  || SG1F < 0 || nSG2F < 0 || nSG1F < 0){
			System.out.println(SG2F + "  " + SG1F);
			System.out.println(nSG2F + "  " + nSG1F);
			System.out.println(toReturn);
			System.out.println();
		}*/
		if(toReturn < 0){
			toReturn = 0;
		}
		//if(Double.isNaN(toReturn)){
		//	return 0;
		//}
		
		return toReturn;
	}
	

	
	/**
	 * Takes as input a matrix of observed cooccurrences and produces a matrix
	 * of the expected co-occurrences.
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
	
	
	/**
	 * Calculates information gain, this one only works on a 2x2 matrix.
	 * Takes only the observed matrix.
	 * 
	 * @param O
	 * @return
	 */
	public static double infoGain(double[][] O) {
		//if(SG2F == 0 || nSG2F == 0 || SG1F == 0){
		//	return 0;
		//}
		double positives = O[0][0] + O[0][1]; //SG2F + SG1F
		double negatives = O[1][0] + O[1][1]; //nSG2F + nSG1F
		double total = positives + negatives;
		
		double initInfoA = positives/total * log2(positives/total);
		if(positives == 0){
			initInfoA = 0;
		}
		double initInfoB = negatives/total * log2(negatives/total);
		if(negatives == 0){
			initInfoB = 0;
		}
		double initInfo = -(initInfoA + initInfoB);

		double info2A = O[0][0]/(O[0][0]+O[1][0]) * log2(O[0][0]/(O[0][0]+O[1][0]));
		if(O[0][0] == 0){
			info2A = 0;
		}
		double info2B = O[1][0]/(O[0][0]+O[1][0]) * log2(O[1][0]/(O[0][0]+O[1][0]));
		if(O[1][0] == 0){
			info2B = 0;
		}
		double info2 = -(info2A + info2B);
		
		double info1A = O[0][1]/(O[0][1]+O[1][1]) * log2(O[0][1]/(O[0][1]+O[1][1]));
		if(O[0][1] == 0){
			info1A = 0;
		}
		double info1B = O[1][1]/(O[0][1]+O[1][1]) * log2(O[1][1]/(O[0][1]+O[1][1]));
		if(O[1][1] == 0){
			info1B = 0;
		}
		double info1 = -(info1A + info1B);
		
		//System.out.println("info1A: " + info1A);
		//System.out.println("info1B: " + info1B);
		
		return initInfo - ((O[0][0]+O[1][0])/total * info2 + (O[0][1]+O[1][1])/total * info1);
	}

}
