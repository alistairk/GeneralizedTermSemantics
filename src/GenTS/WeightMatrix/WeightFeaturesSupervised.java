package MatrixFormat.WeightMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import semDist.BinaryDist;

/**
 * This class performs supervised Matrix weighting.
 * 
 * @author akennedy
 *
 */
public class WeightFeaturesSupervised {
	private HashSet<String> stopWords;
	private ArrayList<String> words;
	private ArrayList<Double> wordsCount;
	private ArrayList<Boolean> goodWords;
	private HashSet<Integer>[] relatedPairs;
	//private RogetELKB elkb;
	private BinaryDist bd;
	private double[] weights;
	private long legitWords;
	//private int positivePairs;
	
	private String TYPE;
	//private String POS;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//for(int num = 3; num <= 3; num++){
			int num = 2;
			//int YEAR = 1911;
			String POS = "a";
			//String STOPWORDS = "./stops.txt";
			boolean REMOVE_TERMS = true;
	
			WeightFeaturesSupervised wfrs = new WeightFeaturesSupervised("/Users/akennedy/Research/buildMatrix/synonymSets/wordnet/adjs.txt");
			
			//wfrs.setRogetPOS("N.");
			String testsFile = "nounList"+num+".txt";
			//String testsFile = "nounEmoList2.txt";
			//String testsFile = "nounSentiList.txt";
			if(POS.equals("v")){
				//wfrs.setRogetPOS("VB.");
				testsFile  = "verbList"+num+".txt";
				//testsFile  = "verbEmoList2.txt";
				//testsFile  = "verbSentiList.txt";
			}
			else if (POS.equals("a")){
				//wfrs.setRogetPOS("ADJ.");
				testsFile = "adjList"+num+".txt";
				//testsFile = "adjEmoList2.txt";
				//testsFile = "adjSentiList.txt";
			}
			
			//wfrs.loadStopWordList(STOPWORDS);
			if(REMOVE_TERMS){
				wfrs.loadExemptWords("/Users/akennedy/workspace/Roget2/selectBest/finalLists/" + testsFile);
				//wfrs.loadStopWordList("/Users/akennedy/workspace/Roget2/selectBest/" + testsFile);
			}
	
			wfrs.loadRows("/Users/akennedy/Research/buildMatrix/finalMatrix_"+POS+"/row_features.csv");
			
			wfrs.colleceRelatedPairs();
	
			//String[] types = new String[]{"F", "PMI", "Tscore", "Zscore", "LL", "Chi2"};
			String[] types = new String[]{"PMI"};
			for(String t : types){
				System.out.println(t);
				wfrs.setType(t);
	
				wfrs.initializeWeights(POS);
				
				wfrs.loadColumnFeatures("/Users/akennedy/Research/buildMatrix/finalMatrix_"+POS+"/matrix_ccs.mat",
						"/Users/akennedy/Research/buildMatrix/final_mod/supervised"+num+"/column.t"+t+"_"+POS);
				wfrs.weightRowFeatures("/Users/akennedy/Research/buildMatrix/finalMatrix_"+POS+"/matrix_crs.mat",
						"/Users/akennedy/Research/buildMatrix/final_mod/supervised"+num+"/matrix_crs.mat.t"+t+"_"+POS);
			}
		//}
		
	}
	
	public WeightFeaturesSupervised(String groupingsFile){
		legitWords = 0;
		stopWords = new HashSet<String>();
		words = new ArrayList<String>();
		wordsCount = new ArrayList<Double>();
		goodWords = new ArrayList<Boolean>();
		
		bd = new BinaryDist(groupingsFile);
		
	}
	
	public void setType(String t) {
		TYPE = t;
	}

	/*public void setRogetPOS(String pos) {
		POS = pos;
	}*/

	public void initializeWeights(String POS){
		int columnCount = 1050178; //321152; // noun columns
		if(POS.equals("v")){
			columnCount = 1423665; //407421;
		}
		else if(POS.equals("a")){
			columnCount = 360436; //25890;
		}
		
		weights = new double[columnCount]; // hard coded value, number of columns
		for(int i = 0; i < weights.length; i++){
			weights[i] = 1;
		}
	}



	/**
	 * Reweights the row matrix file using whatever weighting scheme was selected.
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
						if(value > 0.000000000000000000001){
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
	 * Loads the column features and calculates the weight of the feature.
	 * 
	 * @param fname
	 */
	public void loadColumnFeatures(String fname, String outputFile) {
		int totalFeaturesCount = 0;
		int featureNumber = 0;
		int linesCount = 0;
		boolean[] goodWeights = new boolean[weights.length];
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
					String[] parts = line.split(" ");
					double TFofWordsInContext = 0;
					double instancesOfWordsInContext = 0;
					int uniqueCount = 0;
					Hashtable<Integer, Double> wordsWithCurrentFeature = new Hashtable<Integer, Double>();
					for(int i = 0; i < parts.length; i+=2){
						//System.out.println(parts.length);
						if(parts.length > 1 && goodWords.get(Integer.parseInt(parts[i]))){
							wordsWithCurrentFeature.put(Integer.parseInt(parts[i]), Double.parseDouble(parts[i+1]));
							//goodCount += Integer.parseInt(parts[i+1]);
							instancesOfWordsInContext += Double.parseDouble(parts[i+1]);
							TFofWordsInContext += wordsCount.get(Integer.parseInt(parts[i]));
							uniqueCount++;
						}
					}
					if(uniqueCount >= 2){
						//System.out.println(uniqueCount);
						//System.out.println(wordsWithCurrentFeature.size());
						double[] d = getPairCounts(wordsWithCurrentFeature); // counts found pairs sharing the same feautre
						double PairsRelatedSharingFeature = d[0]; // SG2F
						double PairsUnrelatedSharingFeautre = d[1]; // nSG2F
						double PairRelatedNotSharingFeature = countRelatedPairsInDifferentContexts(wordsWithCurrentFeature); // counts all pairs sharing the same feature
						//int entries = parts.length/2;
					
						//System.out.println("* " + goodCount);
						totalFeaturesCount ++;
						//double PairCountSharingFeaturesNotNecessariallySG = goodCount * (goodCount-1); // counts every pair twice, so divide by 2
						double PairsNotSharingFeature = instancesOfWordsInContext * (legitWords-TFofWordsInContext); 
						
						double SG2F = PairsRelatedSharingFeature;
						double nSG2F = PairsUnrelatedSharingFeautre;
						double SG1F = PairRelatedNotSharingFeature;// - SG2F;
						double nSG1F = PairsNotSharingFeature - (SG1F); // pairs unrelated & not sharing feature
						
						double value = MatrixWeighter.getTest(SG2F, nSG2F, SG1F, nSG1F, TYPE);
						
						if(Double.isNaN(value) || Double.isInfinite(value) || nSG1F < 0){
							System.out.println(featureNumber);
							System.out.println((int)SG2F + " " + (int)nSG2F);
							System.out.println((int)SG1F + " " + (int)nSG1F);
							System.out.println(value);
							System.out.println();
						}
						
						weights[featureNumber] = value;
						goodWeights[featureNumber] = true;
					}
					else{
						//weights[featureNumber] = 0;
						goodWeights[featureNumber] = false;
					}
					featureNumber++;
				}

				linesCount++;
				if(linesCount % 1000 == 0){
					System.out.println("Features processed: " + linesCount);
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
		
		ave = 0;
		for(int i = 0; i < weights.length; i++){
			if(goodWeights[i]){
				ave += weights[i];
			}
		}
		ave = ave / totalGoodWeights;
		System.out.println("Average score: " + ave);
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
	private double[] getPairCounts(Hashtable<Integer, Double> words) {
		double positiveCount = 0;
		double negativeCount = 0;
		for(int word1 : words.keySet()){
			for(int word2 : words.keySet()){
				if(word1 != word2){
					if(relatedPairs[word1].contains(word2)){
						//positiveCount+= wordsCount.get(word1) * wordsCount.get(word2); //pairs with feature same SG
						positiveCount+= words.get(word1) * words.get(word2); //pairs with feature same SG
					}
					else{
						//negativeCount+= wordsCount.get(word1) * wordsCount.get(word2); // pairs with feature different SG
						negativeCount+= words.get(word1) * words.get(word2); // pairs with feature different SG
					}
				}
				/*else{
					positiveCount+= wordsCount.get(word1) * (wordsCount.get(word2)-1.0); //pair of the same word
				}*/
			}
		}
		return new double[]{positiveCount, negativeCount};//return both
	}
	
	/**
	 * Counts all pairs of words related by Semicolon Group in the given HashSet.
	 * 
	 * Counts number of related words that appear in different contexts.
	 * 
	 * @param words
	 * @return
	 */
	private double countRelatedPairsInDifferentContexts(Hashtable<Integer, Double> words) {
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
	 * Identifies pairs of words that appear in the same Roget's Grouping and records them in a hashtable.
	 */
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
	 * and which are not useful (i.e. do not appear in Roget's)
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
	 * Adjusts the row weights and legitWords mid run. Only used
	 * when applying supervision on top of another method.
	 * 
	 * @param fname
	 */
	public void adjustRows(String fname) {
		legitWords = 0;
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
					String parts[] = line.split(" ");
					double wordCount = 0;
					for(int i = 0; i < parts.length; i+=2){
						wordCount += Double.parseDouble(parts[i+1]);
					}
					//words.add(parts[0]);
					//double wordCount = Double.parseDouble(parts[2]);
					wordsCount.set(count, wordCount);
					if(goodWords.get(count)){
						legitWords += wordCount;
					}
					count++;
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
		System.out.println(words.size());
		System.out.println(legitWords);
		
	}
	
	/**
	 * Loads stop word list
	 * 
	 * @param fname
	 */
	public void loadStopWordList(String fname) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
	         
			for ( ; ; ) {
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
	
				else {
					stopWords.add(line);
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
	}
	
	/**
	 * Loads exempt words and stores them in the stopWords list.
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
