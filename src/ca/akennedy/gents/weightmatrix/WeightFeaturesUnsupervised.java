package ca.akennedy.gents.weightmatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;



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
 * java WeightFeaturesUnsupervised <PMI|LL|Dice|Tscore|Zscore|Chi2> <row matrix file> <column matrix file>
 * 
 * @author akennedy
 *
 */
public class WeightFeaturesUnsupervised {
	private static final Logger LOGGER = Logger.getLogger(WeightFeaturesUnsupervised.class.getName());
	//public List<String> words;
	public List<Double> featureWeight;
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
		if(args.length < 3){
			LOGGER.info("To Run Program: java WeightFeaturesUnsupervised <PMI|LL|Dice|Tscore|Zscore|Chi2> <row matrix file> <column matrix file>");
		}
		else{
			String association = args[0]; // PMI
			//String rlabelFile = args[1];// "/Users/akennedy/Research/buildMatrix/finalMatrix_"+POS+"
			String rowMatrixFile = args[1]; // finalMatrix_n
			String columnMatrixFile = args[2];
			
			//names of reweighted files
			String newRowMatrixFile = rowMatrixFile+".u-"+association;
			String newColMatrixFile = columnMatrixFile+".u-"+association;
			
			LOGGER.info("Association measure: " + association);
			
			WeightFeaturesUnsupervised wfu = new WeightFeaturesUnsupervised();
	
			//loads the column matrix
			wfu.loadColumnFeatures(columnMatrixFile);
			
			//loads the row matrix and performs all re-weighting.
			wfu.weightRowFeatures(rowMatrixFile, newRowMatrixFile, association);
	
			//create new column matrix
			wfu.rowsToColumns(newRowMatrixFile, newColMatrixFile);
		}
	}

	/**
	 * Constructor initializes a WeightFeaturesUnsupervised object.
	 */
	public WeightFeaturesUnsupervised(){
		//words = new ArrayList<String>();
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
			BufferedWriter matrixWriter = new BufferedWriter(new FileWriter(outName));
			
			BufferedReader matrixReader = new BufferedReader(new FileReader(fname));
			String first = matrixReader.readLine(); // get first line
			matrixWriter.write(first + "\n");
			
			//read from the row matrix file
			for ( ; ; ) {
				String line = matrixReader.readLine();
	
				if (line == null) {
					matrixReader.close();
					matrixWriter.flush();
					matrixWriter.close();
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
						
						double truePos = Double.parseDouble(parts[i]);
						double falsePos = rowWeight-truePos;
						double falseNeg = columnWeight-truePos;
						double trueNeg = totalWeight-(truePos +falsePos +falseNeg);
						
						//create new weight
						double value = MatrixWeighter.getAssociation(truePos, falsePos, falseNeg, trueNeg, type);
						
						//print new value unless it is extremely small 
						if(value > 0.000000000000000000001){
							matrixWriter.write(parts[i-1] + " " + value + " ");
						}
					}
					
					matrixWriter.write("\n");
				}
			}
				
		} catch (Exception e) {
	    	 LOGGER.warning(e.getMessage());
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
			BufferedReader columnReader = new BufferedReader(new FileReader(fname));
			columnReader.readLine(); // get first line
			int count = 0;
			
			for ( ; ; ) {
				String line = columnReader.readLine();
	
				if (line == null) {
					columnReader.close();
					break;
				}
	
				else {
					double weight = 0;
					if(!"".equals(line)){
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
			LOGGER.info("weight: "+totalWeight);
	
		} catch (Exception e) {
	    	 LOGGER.warning(e.getMessage());
		}

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
			BufferedWriter colMatrixWriter = new BufferedWriter(new FileWriter(outFile));
			BufferedReader rowMatrixReader = new BufferedReader(new FileReader(fname));
			String line = rowMatrixReader.readLine(); // get first line
			
			colMatrixWriter.write(line + "\n");

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
				line = rowMatrixReader.readLine();
	
				if (line == null) {
					rowMatrixReader.close();
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
						LOGGER.info("Rows read: " + count);
					}
				}
			}
			
			//iterate through the value array and 
			
			for(int i = 0; i < valueArray.length; i++){
		        if(valueArray[i].size() > 0){
	        		ArrayList<Integer> ids = idArray[i];
	        		ArrayList<Double> vals = valueArray[i];
		        	for(int j = 0; j < vals.size(); j++){
		        		colMatrixWriter.write(ids.get(j) + " " + vals.get(j) + " ");
		        	}
		        	colMatrixWriter.write("\n");
		        }
		        else{
		        	colMatrixWriter.write("\n");
		        }
			}
			colMatrixWriter.close();
		} 
		catch (Exception e) {
	    	 LOGGER.warning(e.getMessage());
		}

	}
	

}
