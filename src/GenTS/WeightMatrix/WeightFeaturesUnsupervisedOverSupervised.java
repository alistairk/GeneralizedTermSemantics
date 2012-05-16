package GenTS.WeightMatrix;


/**
 * Applies unsupervised feature weighting on top of a matrix where supervised feature
 * weighting has already been applied.
 * 
 * @author akennedy
 *
 */
public class WeightFeaturesUnsupervisedOverSupervised {
	

	/**
	 * Runs the unsupervised feature weighting.
	 * @param args
	 */
	public static void main(String args[]){
		for(int num = 1; num <= 1; num++){
			String POS = "a";
			
			//String[] types = new String[]{"F", "PMI", "Tscore", "Zscore", "LL", "Chi2"};
			String[] types = new String[]{"PMI"};
			for(String t : types){
				String TYPE = t;
				System.out.println(TYPE);
				
				WeightFeaturesUnsupervised wfu = new WeightFeaturesUnsupervised();
	
				String rowFile = "/Users/akennedy/Research/buildMatrix/final_mod/supervised"+num+"/matrix_crs.mat.r"+TYPE+"_"+POS;
				String tmpColumnFile = "/Users/akennedy/Research/buildMatrix/final_mod/tmpColumns.txt";
				
				wfu.loadRows("/Users/akennedy/Research/buildMatrix/finalMatrix_"+POS+"/finalMatrix_"+POS+".rlabel");
				
				wfu.rowsToColumns(rowFile, tmpColumnFile);
	
				wfu.loadColumnFeatures(tmpColumnFile);
				wfu.weightRowFeatures(rowFile,
						"/Users/akennedy/Research/buildMatrix/final_mod/supervised"+num+"/matrix_crs.mat.rc"+TYPE+"_"+POS, TYPE);
				
			}
		}
	}
	
}
