package GenTS.WeightMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;



/**
 * This class re-weights a matrix using one of the measures of association
 * defined in SemanticMatrix.WeightMatrix.MatrixWeighter. It reads in a matrix
 * and then re-weights the row matrix file matrix_crs.mat to be re-weighted
 * with the correct measure of association.
 * 
 * This program takes in the part-of-speech (POS), the association measure (TYPE) the directory 
 * in which the matrix is located (DIRECTORY) and the matrix name (MATRIX_NAME). It then re-weights
 * the matrix_crs.mat file to be of the matirx_crs.mat.TYPE_POS.
 * 
 * Run the program like this:
 * 
 * java WeightFeaturesUnsupervised <n|v|a> <PMI|LL|F|Tscore|Zscore|Chi2> <rlabel file> <row matrix file> <column matrix file>
 * 
 * @author akennedy
 *
 */
public class WeightFeaturesUnsupervised {
	public ArrayList<String> words;
	public ArrayList<Double> featureWeight;
	public double totalWeight;
	public double[] weights;

	/**
	 * This program takes in the part-of-speech (POS), the association measure (TYPE) the directory 
	 * in which the matrix is located (DIRECTORY) and the matrix name (MATRIX_NAME). It then re-weights
	 * the matrix_crs.mat file to be of the matirx_crs.mat.TYPE_POS.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 4){
			System.out.println("To Run Program: java WeightFeaturesUnsupervised <PMI|LL|F|Tscore|Zscore|Chi2> <rlabel file> <row matrix file> <column matrix file>");
			return;
		}
		
		String association = args[0]; // PMI
		String rlabelFile = args[1];// "/Users/akennedy/Research/buildMatrix/finalMatrix_"+POS+"
		String rowMatrixFile = args[2]; // finalMatrix_n
		String columnMatrixFile = args[3];
		
		//names of reweighted files
		String newRowMatrixFile = rowMatrixFile+"."+association;
		String newColumnMatrixFile = columnMatrixFile+"."+association;
		
		System.out.println(association);
		
		WeightFeaturesUnsupervised wfu = new WeightFeaturesUnsupervised();

		//loads all the words in the array from the rlabel file
		wfu.loadRows(rlabelFile);

		//loads the column matrix
		wfu.loadColumnFeatures(columnMatrixFile);
		
		//loads the row matrix and performs all re-weighting.
		wfu.weightRowFeatures(rowMatrixFile, newRowMatrixFile, association);

		//create new column matrix
		wfu.rowsToColumns(newRowMatrixFile, newColumnMatrixFile);
	}

	/**
	 * Constructor initializes a WeightFeaturesUnsupervised object.
	 */
	public WeightFeaturesUnsupervised(){
		words = new ArrayList<String>();
		featureWeight = new ArrayList<Double>();
		totalWeight = 0;
	}

	/**
	 * This method performs the actual re-weighting of the matrix. It takes two arguments
	 * the input file, which is the sparse matrix by row, and the output file which is the
	 * re-weighted sparse matrix by row.
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
			
			//read from the row matrix file
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
					
					for(int i = 1; i < parts.length; i+=2){ // calculate weights for each entry in the row
						double columnWeight = featureWeight.get(Integer.parseInt(parts[i-1]));
						
						double tp = Double.parseDouble(parts[i]);
						double fp = rowWeight-tp;
						double fn = columnWeight-tp;
						double tn = totalWeight-(tp +fp +fn);
						
						//create new weight
						double value = MatrixWeighter.getAssociation(tp, fp, fn, tn, type);
						
						//print new value unless it is extremely small 
						if(value > 0.000000000000000000001){
							bw.write(parts[i-1] + " " + value + " ");
						}
					}
					
					bw.write("\n");
				}
			}
				
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
	}

	/**
	 * Opens the file passed as an argument and loads the sparse matrix by columns.
	 * Each column is treated as a feature and loaded into the TreeMap featureWeight.
	 * This is done to find out how many times a given feature appears across all
	 * rows in the matrix. The total weight of the matrix is also recorded.
	 * 
	 * @param fname
	 */
	public void loadColumnFeatures(String fname) {
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
	 * Opens the file passed as the argument and reads all its lines into the words
	 * ArrayList. The file passed to it should be a list of all the words in the array.
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
	 * This method takes the row matrix and translates it into a new column matrix.
	 * This method can be called after the row matrix has been re-weighted. It takes
	 * two arguments the row matrix and the column matrix that is to be created.
	 * 
	 * @param fname
	 * @param outFile
	 */
	public void rowsToColumns(String fname, String outFile) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String line = br.readLine(); // get first line
			
			bw.write(line + "\n");

			//initialize arrays for IDs of contexts and their values.
			String[] firstBreak = line.split(" ");
			@SuppressWarnings("unchecked")
			ArrayList<Integer>[] idArray = new ArrayList[Integer.parseInt(firstBreak[1])];
			@SuppressWarnings("unchecked")
			ArrayList<Double>[] valueArray = new ArrayList[Integer.parseInt(firstBreak[1])];
			for(int i = 0; i < idArray.length; i++){
				idArray[i] = new ArrayList<Integer>();
				valueArray[i] = new ArrayList<Double>();
			}
			int count = 0;
			
			//read matrix and load it into the id and values arrays
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
			
			//iterate through the value array and 
			
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
