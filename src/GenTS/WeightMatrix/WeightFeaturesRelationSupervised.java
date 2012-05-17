package GenTS.WeightMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;


/**
 * This class performs supervised Matrix weighting.
 * 
 * @author akennedy
 *
 */
public class WeightFeaturesRelationSupervised extends WeightFeaturesContextSupervised{
	
	/**
	 * The main function reads in the arguments and then creates a new WeightFeaturesSuprvised
	 * class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 6){
			System.out.println("To Run Program: java WeightFeaturesUnsupervised <PMI|LL|F|Tscore|Zscore|Chi2> <training data> <row_features.csv file> <row matrix file> <column matrix file> <column boundary file> <output column file>");
			return;
		}
		String association = args[0]; 
		String trainingData = args[1]; 
		String rowFeaturesFile = args[2]; 
		String rowMatrixFile = args[3]; 
		String columnMatrixFile = args[4];
		String columnBoundaryFile = args[5];
		String outputColumnWeights = args[5];
		
		//names of re-weighted files
		String newRowMatrixFile = rowMatrixFile+".r-"+association;
		String newColumnMatrixFile = columnMatrixFile+".r-"+association;
				
		WeightFeaturesRelationSupervised wfrs = new WeightFeaturesRelationSupervised(trainingData, rowMatrixFile, association);
		
		//load row file
		wfrs.loadRows(rowFeaturesFile);
		
		//assembles training data into a hashtable, only words found in the matrix are used.
		wfrs.colleceRelatedPairs();

		//loads columns and calculates the weights for each feature
		wfrs.loadColumnFeatures(columnMatrixFile, columnBoundaryFile, outputColumnWeights);
		
		//creates new row matrix using these features
		wfrs.weightRowFeatures(rowMatrixFile, newRowMatrixFile);
		
		//translates the row matrix into a column matrix using the function in WeightFeaturesUnsupervised
		WeightFeaturesUnsupervised wfu = new WeightFeaturesUnsupervised();
		wfu.rowsToColumns(newRowMatrixFile, newColumnMatrixFile);
	}
	
	/**
	 * The constructor takes in the training data, the rows file
	 * from the matrix and the measure of association. Using this
	 * it loads all the training data, sets the measure of association
	 * and uses the matrix file in order to determine how many columns
	 * there are. The row matrix is also loaded
	 * 
	 * @param trainingData
	 * @param rowFile
	 * @param association
	 */
	public WeightFeaturesRelationSupervised(String trainingData, String rowFile, String association){
		super(trainingData, rowFile, association);
	}
	

	/**
	 * This function does most of the work. It reads in the column matrix and then
	 * finds weights for each entry in the column. The column matrix is the first
	 * argument and the second argument is the file that we will be column values
	 * to. 
	 * 
	 * This function will iterate through each column in the matrix counting the 
	 * number of synonyms found in it, number of synonyms where only one is 
	 * found in that column, number of non synonyms in that column and the
	 * number of non-synonyms where only one is found in that column. These will
	 * then be used to measure the association between the column and "synonymy"
	 * as defined by the training data.
	 * 
	 * @param fname
	 * @param boundaryFname
	 * @param outputFile
	 */
	public void loadColumnFeatures(String fname, String boundaryFname, String outputFile) {
		int totalFeaturesCount = 0;
		int startValue = 0;
		String relation = "";
		int featureNumber = 0;
		int boundaryValue = 0;
		boolean[] goodWeights = new boolean[weights.length];
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			BufferedReader boundaryReader = new BufferedReader(new FileReader(boundaryFname));
			br.readLine(); // get first line
			
			double SG2F = 0;
			double nSG2F = 0;
			double SG1F = 0;
			double nSG1F = 0;
			
			for ( ; ; ) {
				
				if(featureNumber == boundaryValue){
					String boundaryLine = boundaryReader.readLine();
					if(boundaryLine != null){
						String[] bParts = boundaryLine.split(" := ");
						boundaryValue = Integer.parseInt(bParts[1]);
						System.out.println(relation + " : " + startValue + " " + featureNumber);
						
						double value = 1;
						boolean isGood = true;
						value = MatrixWeighter.getAssociation(SG2F, nSG2F, SG1F, nSG1F, TYPE);
						if(nSG1F > 0 && (Double.isNaN(value) || Double.isInfinite(value))){
							System.out.println(featureNumber);
							System.out.println((int)SG2F + " " + (int)nSG2F);
							System.out.println((int)SG1F + " " + (int)nSG1F);
							System.out.println(value);
							System.out.println();
						}
						if(SG2F + nSG2F + SG1F + nSG1F == 0){
							System.out.println("zero");
							isGood = false;
							value = 1;
						}
						for(int i = startValue; i < featureNumber; i++){
							weights[i] = value;
							goodWeights[i] = isGood;
						}
						relation = bParts[0];
						
						SG2F = 0;
						nSG2F = 0;
						SG1F = 0;
						nSG1F = 0;
	
						startValue = featureNumber;
					}
					else{
						boundaryReader.close();
					}
				}
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
	
				else {
					String[] parts = line.split(" ");
					double TFofWordsInContext = 0;
					double instancesOfWordsInContext = 0;
					int uniqueCount = 0;
					Hashtable<Integer, Double> wordsWithCurrentFeature = new Hashtable<Integer, Double>();
					for(int i = 0; i < parts.length; i+=2){
						if(parts.length > 1 && goodWords.get(Integer.parseInt(parts[i]))){
							wordsWithCurrentFeature.put(Integer.parseInt(parts[i]), Double.parseDouble(parts[i+1]));
							instancesOfWordsInContext += Double.parseDouble(parts[i+1]);
							TFofWordsInContext += wordsCount.get(Integer.parseInt(parts[i]));
							uniqueCount++;
						}
					}
					if(uniqueCount >= 2){
						double[] d = getPairCounts(wordsWithCurrentFeature); // counts found pairs sharing the same feautre
						double PairsRelatedSharingFeature = d[0]; // SG2F
						double PairsUnrelatedSharingFeautre = d[1]; // nSG2F
						double PairRelatedNotSharingFeature = countRelatedPairsInDifferentContexts(wordsWithCurrentFeature); // counts all pairs sharing the same feature
						
						
						totalFeaturesCount ++;
						double PairsNotSharingFeature = instancesOfWordsInContext * (legitWords-TFofWordsInContext); 
						
						SG2F += PairsRelatedSharingFeature/10000.0;
						nSG2F += PairsUnrelatedSharingFeautre/10000.0;
						SG1F += PairRelatedNotSharingFeature/10000.0;// - SG2F;
						nSG1F += (PairsNotSharingFeature - SG1F)/10000.0; // pairs unrelated & not sharing feature
						
					}
					featureNumber++;
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
		System.out.println("Features included: " + totalFeaturesCount);
		double ave = 0;
		double min = Double.MAX_VALUE;
		double totalGoodWeights = 0;
		for(int i = 0; i < weights.length; i++){
			if(goodWeights[i]){
				ave += weights[i];
				totalGoodWeights++;
				if(weights[i] < min){
					min = weights[i];
				}
			}
		}
		ave = ave / totalGoodWeights;
		if(Double.isNaN(ave)){
			System.out.println("Not a number");
		}
		System.out.println("Average score: " + ave);
		System.out.println("Min score: " + min);
		
		//normalize average to 1.0

		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			for(int i = 0; i < weights.length; i++){
				if(goodWeights[i]){
					weights[i] = weights[i]/ave;
				}
				else{
					weights[i] = ave;
				}
				bw.write(weights[i] + "\n");
			}
	
			bw.close();
		}
		catch(Exception e){e.printStackTrace();}
	}

}
