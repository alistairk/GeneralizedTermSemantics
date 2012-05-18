package GenTS.WeightMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * This class performs supervised Matrix weighting. Much like WeightFeaturesUnsupervised this program takes
 * in an already constructed matrix and produces one with a different weight. This is based off of the work
 * of:
 * Alistair Kennedy, Stan Szpakowicz (2011). “A Supervised Method of Feature Weighting for Measuring Semantic Relatedness”. 
 * In Proceedings of Canadian AI 2011, St. John’s, Newfoundland, Canada, May 25-27, 222-233.
 * and
 * Alistair Kennedy, Stan Szpakowicz (2012). “Supervised Distributional Semantic Relatedness”. 
 * To appear in the Proceedings of Text Speech Dialogue 2012.
 * 
 * Every context in the term-context matrix is given a unique weight based upon the number of synonym and non-synonym
 * pairs that appear in it.
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
 * @author akennedy
 *
 */
public class WeightFeaturesContextSupervised {
	protected HashSet<String> stopWords;
	protected ArrayList<String> words;
	protected ArrayList<Double> wordsCount;
	protected ArrayList<Boolean> goodWords;
	protected HashSet<Integer>[] relatedPairs;

	protected BinaryDist bd;
	protected double[] weights;
	protected long legitWords;
	
	protected String TYPE;
	
	/**
	 * The main function reads in the arguments and then creates a new WeightFeaturesSuprvised
	 * class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 5){
			System.out.println("To Run Program: java WeightFeaturesContextSupervised <PMI|LL|Dice|Tscore|Zscore|Chi2> <training data> <row_features.csv file> <row matrix file> <column matrix file>");
			return;
		}
		String association = args[0]; 
		String trainingData = args[1]; 
		String rowFeaturesFile = args[2]; 
		String rowMatrixFile = args[3]; 
		String columnMatrixFile = args[4];
		String outputColumnWeights = args[4].substring(0, args[4].lastIndexOf("/"))+"/columns_context_"+association+".txt";
		
		//names of re-weighted files
		String newRowMatrixFile = rowMatrixFile+".c-"+association;
		String newColumnMatrixFile = columnMatrixFile+".c-"+association;
				
		WeightFeaturesContextSupervised wfcs = new WeightFeaturesContextSupervised(trainingData, rowMatrixFile, association);
		
		//load row file
		wfcs.loadRows(rowFeaturesFile);
		
		//assembles training data into a hashtable, only words found in the matrix are used.
		wfcs.colleceRelatedPairs();

		//loads columns and calculates the weights for each feature
		wfcs.loadColumnFeatures(columnMatrixFile, outputColumnWeights);
		
		//creates new row matrix using these features
		wfcs.weightRowFeatures(rowMatrixFile, newRowMatrixFile);
		
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
	public WeightFeaturesContextSupervised(String trainingData, String rowFile, String association){
		legitWords = 0;
		stopWords = new HashSet<String>();
		words = new ArrayList<String>();
		wordsCount = new ArrayList<Double>();
		goodWords = new ArrayList<Boolean>();
		
		bd = new BinaryDist(trainingData);
		
		initializeWeights(rowFile);
		
		TYPE = association;
		
	}

	/**
	 * This method initializes the weights for each column in the
	 * matrix. Initially they are set to 1 but will later be changed
	 * to reflect how well the given context indicates synonymy.
	 * 
	 * @param rowMatrix
	 */
	public void initializeWeights(String rowMatrix){
		try {
			BufferedReader br = new BufferedReader(new FileReader(rowMatrix));
	
			String firstLine = br.readLine(); // get first line
			br.close();
		
			String[] sizes = firstLine.split("\\s+");
			
			int columnCount = Integer.parseInt(sizes[1]); 
	
			weights = new double[columnCount]; 
			for(int i = 0; i < weights.length; i++){
				weights[i] = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * This function takes on two arguments, the input row matrix
	 * and the output row matrix. It iterates through every entry
	 * in the input matrix and multiplies the value of a given column
	 * by the weight as determined by the Supervised Weighter.
	 * This is then printed to the new file
	 * 
	 * @param fname
	 * @param outName
	 */
	public void weightRowFeatures(String fname, String outName) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outName));
			
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String first = br.readLine(); // get first line
			bw.write(first + "\n");
			
			for ( ; ; ) {
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					bw.close();
					break;
				}
	
				else {
					String[] parts = line.split(" ");
					for(int i = 0; i < parts.length; i+=2){
						double value = Double.parseDouble(parts[i+1]) * weights[Integer.parseInt(parts[i])];
						if(value > 0.00000000001){ // do not print extremely small values
							bw.write(parts[i] + " " + value + " ");
						}
					}
					bw.write("\n");
				}
			}
			//bw.close();
				
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
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
	 * @param outputFile
	 */
	public void loadColumnFeatures(String fname, String outputFile) {
		int totalFeaturesCount = 0;
		int featureNumber = 0;
		int linesCount = 0;
		boolean[] goodWeights = new boolean[weights.length];
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			br.readLine(); // get first line
			
			//read file
			for ( ; ; ) {
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
					
					//place the words from a context into a Hashtable
					Hashtable<Integer, Double> wordsWithCurrentFeature = new Hashtable<Integer, Double>();
					for(int i = 0; i < parts.length; i+=2){
						if(parts.length > 1 && goodWords.get(Integer.parseInt(parts[i]))){
							wordsWithCurrentFeature.put(Integer.parseInt(parts[i]), Double.parseDouble(parts[i+1]));
							instancesOfWordsInContext += Double.parseDouble(parts[i+1]);
							TFofWordsInContext += wordsCount.get(Integer.parseInt(parts[i]));
							uniqueCount++;
						}
					}
					//if there are more than 2 words that can be used for training count tp, fp, fn and tn.
					if(uniqueCount >= 2){
						double[] d = getPairCounts(wordsWithCurrentFeature); // counts found pairs sharing the same feautre
						double PairsRelatedSharingFeature = d[0]; // tp
						double PairsUnrelatedSharingFeautre = d[1]; // fp
						double PairRelatedNotSharingFeature = countRelatedPairsInDifferentContexts(wordsWithCurrentFeature); // counts all pairs sharing the same feature
						
						totalFeaturesCount ++;

						double PairsNotSharingFeature = instancesOfWordsInContext * (legitWords-TFofWordsInContext); 
						
						double tp = PairsRelatedSharingFeature;
						double fp = PairsUnrelatedSharingFeautre;
						double fn = PairRelatedNotSharingFeature;
						double tn = PairsNotSharingFeature - (fn); // pairs unrelated & not sharing feature
						
						//find association
						double value = MatrixWeighter.getAssociation(tp, fp, fn, tn, TYPE);
						
						//check for a few common errors
						//shouldn't matter now but factored in during debugging.
						if(Double.isNaN(value) || Double.isInfinite(value) || tn < 0){
							System.out.println("Error at feature: "+featureNumber);
							System.out.println((long)tp + " " + (long)fp);
							System.out.println((long)fn + " " + (long)tn);
							System.out.println(value);
							System.out.println();
						}
						
						//set feature weight
						weights[featureNumber] = value;
						goodWeights[featureNumber] = true;
					}
					else{
						goodWeights[featureNumber] = false;
					}
					featureNumber++;
				}

				//track progress
				linesCount++;
				if(linesCount % 1000 == 0){
					System.out.println("Features processed: " + linesCount);
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
		System.out.println("Features included: " + totalFeaturesCount);
		
		//find average feature weight
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
		//print out the new weights into the column weight file.
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


	/**
	 * Counts the number of pairs of words in the same semicolon group from the provided hashSet.
	 * 
	 * Counts words sharing the context that are related, and counts words sharing the context that
	 * are unrelated.
	 * 
	 * @param words
	 * @return
	 */
	protected double[] getPairCounts(Hashtable<Integer, Double> words) {
		double positiveCount = 0;
		double negativeCount = 0;
		for(int word1 : words.keySet()){
			for(int word2 : words.keySet()){
				if(word1 != word2){
					if(relatedPairs[word1].contains(word2)){
						positiveCount+= words.get(word1) * words.get(word2); //pairs with feature same SG
					}
					else{
						negativeCount+= words.get(word1) * words.get(word2); // pairs with feature different SG
					}
				}
			}
		}
		return new double[]{positiveCount, negativeCount};//return both
	}
	
	/**
	 * Counts all pairs of words related by the training data in the given HashSet.
	 * 
	 * Counts number of related words that appear in different contexts.
	 * 
	 * @param words
	 * @return
	 */
	protected double countRelatedPairsInDifferentContexts(Hashtable<Integer, Double> words) {
		double relatedTotal = 0;
		for(int word1 : words.keySet()){
			for(int word2 : relatedPairs[word1]){
				if(!words.containsKey(word2)){
					relatedTotal += words.get(word1) * wordsCount.get(word2);
				}
			}
		}
		return relatedTotal;
	}

	/**
	 * Identifies pairs of words that appear on the same line in the training data and record 
	 * them in a hashtable. This allows for fast lookups of synonyms.
	 */
	@SuppressWarnings("unchecked")
	public void colleceRelatedPairs() {
		relatedPairs = new HashSet[words.size()];
		for(int i = 0; i < relatedPairs.length; i++){
			HashSet<Integer> hs = new HashSet<Integer>();
			relatedPairs[i] = hs;
		}
		
		long uniquePairs = 0;
		for(int i = 0; i < words.size(); i++){
			if(goodWords.get(i)){
				String word1 = words.get(i);
				for(int j = i+1; j < words.size(); j++){
					if(goodWords.get(j)){
						String word2 = words.get(j);
						
						uniquePairs++;
						int similarity = bd.getSimilarity(word1, word2);
						if(similarity == 1){
							//System.out.println(word1 + "\t" + word2);
							addPair(i, j);
							addPair(j, i);
						}
					}
				}
			}
			if(i % 1000 == 0){
				System.out.println(i);
			}
		}
		System.out.println("Unique Pairs: " + uniquePairs);
	}

	/**
	 * Adds related words to a hashtable.
	 * 
	 * @param word1
	 * @param word2
	 */
	private void addPair(int word1, int word2) {
		relatedPairs[word1].add(word2);
	}

	/**
	 * Loads the words and stores them in an arrayList. It also keeps track of which words can be used
	 * and which are not useful (i.e. do not appear in Roget's). If a set of "stopwords" is removed then
	 * these words will be labeled as such and will not be included in the training procedure. This 
	 * can be used to remove a testing set as well, as for the purposes of this program these words
	 * will be considered stopwords too.
	 * 
	 * @param fname
	 */
	public void loadRows(String fname) {
		int goodWordCount = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));

			br.readLine(); // get first line
	         
			for ( ; ; ) {
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
	
				else {
					String parts[] = line.split(";");
					//System.out.println(line);
					words.add(parts[0]);
					double wordCount = Double.parseDouble(parts[2]);
					wordsCount.add(wordCount);
					if(!stopWords.contains(parts[0]) && bd.existsWord(parts[0])){
						legitWords += wordCount;
						goodWords.add(true);
						goodWordCount++;
					}
					else{
						goodWords.add(false);
					}
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
		System.out.println("Unique training words: " + goodWordCount);
		System.out.println("Total words: " + words.size());
		System.out.println("Occurrences of training words: " + legitWords);
	}
	

	/**
	 * This function loads a list of words that appear in the training set but need
	 * to be removed for the purposes of testing later on. In the current version
	 * of this program the function is not used.
	 * 
	 * @param fname
	 */
	public void loadExemptWords(String fname) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
	         
			for ( ; ; ) {
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
	
				else {
					String[] parts = line.split(" ");
					stopWords.add(parts[0]);
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
	}

}
