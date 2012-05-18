package GenTS.WeightMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;


/**
 * This class performs supervised Matrix weighting. Much like WeightFeaturesUnsupervised this program takes
 * in an already constructed matrix and produces one with a different weight. This is based off of the work
 * of:
 * Alistair Kennedy, Stan Szpakowicz (2012). “Supervised Distributional Semantic Relatedness”. 
 * To appear in the Proceedings of Text Speech Dialogue 2012.
 * 
 * Groups of contexts in the term-context matrix are grouped together based on syntactic relationship and then these
 * groups are given scores based on how frequently they contain known synonym and non-synonym pairs of words.
 * 
 * This program takes in a measure of association, a file containing training data, the rowfeatures.cvs file, the 
 * row and column matrix files and an output file for the column weights as parameters. The output file for column
 * weights is not essential for this program, but may be of some interest to those running it.
 * 
 * To Run Program: 
 * java WeightFeaturesUnsupervised <PMI|LL|Dice|Tscore|Zscore|Chi2> <training data> <row_features.csv file> <row matrix file> <column matrix file>
 * 
 * Three new files will be created, new row and column matrix files and another file indicating the weights of the 
 * different contexts.
 * 
 * This class extends WeightFeaturesContextSupervised as almost all the methods are identical except for one
 * that determines weights for each context. A new method performs this task.
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
			System.out.println("To Run Program: java WeightFeaturesUnsupervised <PMI|LL|Dice|Tscore|Zscore|Chi2> <training data> <row_features.csv file> <row matrix file> <column matrix file> <column boundary file>");
			return;
		}
		String association = args[0]; 
		String trainingData = args[1]; 
		String rowFeaturesFile = args[2]; 
		String rowMatrixFile = args[3]; 
		String columnMatrixFile = args[4];
		String columnBoundaryFile = args[5];
		String outputColumnWeights = args[4].substring(0, args[4].lastIndexOf("/"))+"/columns_relation_"+association+".txt";
		
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
	 * argument and the second argument is a file containing boundaries on groups
	 * of columns and the third argument is name of the file that the newly weighted
	 * columns are printed to. 
	 * 
	 * This function groups together columns in the matrix as indicated by the 
	 * boundaryFname. This will group together features based on their syntactic
	 * relationship. Then the function will iterate through the groups of columns 
	 * in the matrix counting the number of synonyms found in it, number of 
	 * synonyms where only one is found in that column, number of non synonyms in 
	 * that column and the number of non-synonyms where only one is found in that 
	 * column. These will then be used to measure the association between the 
	 * column and "synonymy" as defined by the training data.
	 * 
	 * @param fname
	 * @param boundaryFname
	 * @param outputFile
	 */
	public void loadColumnFeatures(String fname, String boundaryFname, String outputFile) {
		int totalFeaturesCount = 0;
		int startValue = 0;
		//String relation = "";
		int featureNumber = 0;
		int boundaryValue = 0;
		boolean[] goodWeights = new boolean[weights.length];
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			BufferedReader boundaryReader = new BufferedReader(new FileReader(boundaryFname));
			br.readLine(); // get first line
			
			double tp = 0;
			double fp = 0;
			double tn = 0;
			double fn = 0;
			
			for ( ; ; ) {
				
				if(featureNumber == boundaryValue){
					String boundaryLine = boundaryReader.readLine();
					if(boundaryLine != null){
						String[] bParts = boundaryLine.split(" := ");
						boundaryValue = Integer.parseInt(bParts[1]);
						//System.out.println(relation + " : " + startValue + " " + featureNumber);
						
						double value = 1;
						boolean isGood = true;
						value = MatrixWeighter.getAssociation(tp, fp, tn, fn, TYPE);
						if(fn > 0 && (Double.isNaN(value) || Double.isInfinite(value))){
							System.out.println("Error at feature: "+featureNumber);
							System.out.println((long)tp + " " + (long)fp);
							System.out.println((long)tn + " " + (long)fn);
							System.out.println(value);
							System.out.println();
						}
						if(tp + fp + tn + fn == 0){
							//System.out.println("zero");
							isGood = false;
							value = 1;
						}
						for(int i = startValue; i < featureNumber; i++){
							weights[i] = value;
							goodWeights[i] = isGood;
						}
						//relation = bParts[0];
						
						tp = 0;
						fp = 0;
						tn = 0;
						fn = 0;
	
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
						
						tp += PairsRelatedSharingFeature/10000.0;
						fp += PairsUnrelatedSharingFeautre/10000.0;
						tn += PairRelatedNotSharingFeature/10000.0;// - SG2F;
						fn += (PairsNotSharingFeature - tn)/10000.0; // pairs unrelated & not sharing feature
						
					}
					featureNumber++;
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
		System.out.println("Features included: " + totalFeaturesCount);
		double ave = 0;
		double totalGoodWeights = 0;
		for(int i = 0; i < weights.length; i++){
			if(goodWeights[i]){
				ave += weights[i];
				totalGoodWeights++;
			}
		}
		ave = ave / totalGoodWeights;
		if(Double.isNaN(ave)){
			System.out.println("Not a number");
		}
		System.out.println("Average score: " + ave);
		
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
