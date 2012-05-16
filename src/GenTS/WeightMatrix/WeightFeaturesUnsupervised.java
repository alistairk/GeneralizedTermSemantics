package MatrixFormat.WeightMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;



/**
 * Performs unsupervised feature weighting on an unweighted matrix.
 * 
 * @author akennedy
 *
 */
public class WeightFeaturesUnsupervised {
	
	public ArrayList<String> words;
	public ArrayList<Double> featureWeight;
	public double totalWeight;
	public double[] weights;
	
	//public static final String POS = "a";
	//public static  String TYPE = "PMI"; //types are PMI, InfoGain, LL, TTest, Chi2 and F
	//public static boolean REMOVE_TERMS = true;
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String POS = "a";
		String TYPE = "";
		String[] types = new String[]{"F", "PMI", "Tscore", "Zscore", "LL", "Chi2"};
		//String[] types = new String[]{"PMI"};
		for(String t : types){
			TYPE = t;
			System.out.println(TYPE);
			
			WeightFeaturesUnsupervised wfu = new WeightFeaturesUnsupervised();

			
			wfu.loadRows("/Users/akennedy/Research/buildMatrix/finalMatrix_"+POS+"/finalMatrix_"+POS+".rlabel");

			wfu.loadColumnFeatures("/Users/akennedy/Research/buildMatrix/finalMatrix_"+POS+"/matrix_ccs.mat");
			wfu.weightRowFeatures("/Users/akennedy/Research/buildMatrix/finalMatrix_"+POS+"/matrix_crs.mat",
					"/Users/akennedy/Research/buildMatrix/final_mod/unsupervised/matrix_crs.mat."+TYPE+"_"+POS, TYPE);
			
		}
	}
	
	/**
	 * Constructor, initializes the words ArrayList
	 */
	public WeightFeaturesUnsupervised(){
		words = new ArrayList<String>();
	}


	/**
	 * Reweights the row matrix file using whatever weighting scheme was selected.
	 * 
	 * @param fname
	 * @param outName
	 */
	public void weightRowFeatures(String fname, String outName, String type) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outName));
			
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String first = br.readLine(); // get first line
			bw.write(first + "\n");
			
			for ( ; ; ) {
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					bw.flush();
					bw.close();
					break;
				}
	
				else {
					double rowWeight = 0;
					String[] parts = line.split(" ");
					for(int i = 1; i < parts.length; i+=2){ // calculate row weight
						double value = Double.parseDouble(parts[i]);
						rowWeight += value;
					}
					
					for(int i = 1; i < parts.length; i+=2){ // calculate the weight
						double columnWeight = featureWeight.get(Integer.parseInt(parts[i-1]));
						
						double tp = Double.parseDouble(parts[i]);
						double fp = rowWeight-tp;
						double fn = columnWeight-tp;
						double tn = totalWeight-(tp +fp +fn);
						
						double value = MatrixWeighter.getTest(tp, fp, fn, tn, type);
						
						if(value > 0.000000000000000000001){
							bw.write(parts[i-1] + " " + value + " ");
						}
					}
					
					bw.write("\n");
				}
			}
			//bw.flush();
			//bw.close();
				
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
	}

	/**
	 * Loads the column features and calculates the weight of the feature.
	 * 
	 * @param fname
	 */
	public void loadColumnFeatures(String fname) {
		featureWeight = new ArrayList<Double>();
		totalWeight = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			br.readLine(); // get first line
			int count = 0;
			
			for ( ; ; ) {
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
	
				else {
					double weight = 0;
					if(!line.equals("")){
						String[] parts = line.split(" ");
						//System.out.println(line);
						for(int i = 0; i < parts.length; i+=2){
							double value = Double.parseDouble(parts[i+1]);
							totalWeight += value;
							weight += value;
						}
						//System.out.println(count + " : " + weight);
					}
					featureWeight.add(count, weight);
					count++;
				}
			}
			System.out.println();
			System.out.println(totalWeight);
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}

	}


	/**
	 * Loads the words and stores them in an arrayList. It also keeps track of which words can be used
	 * and which are not useful (i.e. do not appear in Roget's)
	 * 
	 * @param fname
	 */
	public void loadRows(String fname) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
	         
			for ( ; ; ) {
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
	
				else {
					words.add(line);
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
		System.out.println("words: " + words.size());
	}
	
	/**
	 * Translates a matrix
	 * @param fname
	 * @param outFile
	 */
	public void rowsToColumns(String fname, String outFile) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String line = br.readLine(); // get first line
			
			bw.write(line + "\n");

			String[] firstBreak = line.split(" ");
			//String[] valueArray = new String[Integer.parseInt(firstBreak[1])];
			ArrayList<Integer>[] idArray = new ArrayList[Integer.parseInt(firstBreak[1])];
			ArrayList<Double>[] valueArray = new ArrayList[Integer.parseInt(firstBreak[1])];
			for(int i = 0; i < idArray.length; i++){
				idArray[i] = new ArrayList<Integer>();
				valueArray[i] = new ArrayList<Double>();
			}
			int count = 0;
			
			for ( ; ; ) {
				line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
				else {
					String[] parts = line.split(" ");
					for(int i = 1; i < parts.length; i += 2){
						//valueArray[Integer.parseInt(parts[i])] += count + " " + parts[i+1]+ " ";
						idArray[Integer.parseInt(parts[i-1])].add(count);
						valueArray[Integer.parseInt(parts[i-1])].add(Double.parseDouble(parts[i]));
					}
					count++;
					if(count %1000 == 0){
						System.out.println(count);
					}
				}
			}
			for(int i = 0; i < valueArray.length; i++){
		        if(valueArray[i].size() > 0){
	        		ArrayList<Integer> ids = idArray[i];
	        		ArrayList<Double> vals = valueArray[i];
		        	for(int j = 0; j < vals.size(); j++){
		        		bw.write(ids.get(j) + " " + vals.get(j) + " ");
		        	}
		        	bw.write("\n");
		        }
		        else{
		        	bw.write("\n");
		        }
			}
			bw.close();
		} 
		catch (Exception e) {
	    	 e.printStackTrace();
		}
		
		

	}
	

}
