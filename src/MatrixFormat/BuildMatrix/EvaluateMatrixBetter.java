package MatrixFormat.BuildMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.TreeSet;

import MatrixFormat.Distance.LoadForCosine;
import MatrixFormat.Distance.WordDist;

import semDist.SemDist;
import ca.site.elkb.RogetELKB;

/**
 * this randomly selects triples of words to generate synonym problems for.
 * It also prints out the first word in the triple
 * @author akennedy
 *
 */
public class EvaluateMatrixBetter {
	private static final String STOPWORDS = "./stops.txt";
	private static TreeSet<String> stopWords;
	
	private static final int YEAR = 1987;
	private static String POS = "N.";
	private static String directory = "";
	private static int candidatesPerFrequency = 100;
	private static int threshold = 10;
	private static int maxFrequencyEvaluation = 100;
	private static int maxListSize = 10000;
	

	private static ArrayList<ArrayList<String>> matrixWords;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		POS = args[0];
		directory = args[1];
		
		stopWords = new TreeSet<String>();
		loadStopWordList(STOPWORDS);
		
		matrixWords = new ArrayList<ArrayList<String>>();
		for(int i = 0; i <= maxFrequencyEvaluation; i++){
			matrixWords.add(new ArrayList<String>());
		}
		
		String pos = "n";
		if(POS.equals("VB.")){
			pos = "v";
		}
		else if(POS.equals("ADJ.")){
			pos = "a";
		}
		else if(POS.equals("ADV.")){
			pos = "a";
		}
		// "/Users/akennedy/Research/buildMatrix/fullMatrix_"+pos+"
		loadMatrixWords(directory+"/row_features.csv");
		
		evaluateRandomWords2(pos);
	}
	
	private static void evaluateRandomWords2(String pos) {
		LoadForCosine loader = new LoadForCosine(directory+"/fullMatrix_"+pos+".rlabel", directory+"/matrix_crs.mat");
		
		SemDist sd = new SemDist(YEAR);
		
		for(int minFreq = 1; minFreq <= maxFrequencyEvaluation; minFreq++){
			int count = 0;
			int score = maxListSize;
			System.out.print(minFreq);
			if(matrixWords.get(minFreq).size() > 0){
				double averageScore = 0;
				while(count < candidatesPerFrequency){
					int id = (int)(Math.random() * matrixWords.get(minFreq).size());
					String word = matrixWords.get(minFreq).get(id);
					WordDist[] wd = loader.getClosestCosine(word, maxListSize);
					//loader.printWordArray(wd);
					score = findFirst(sd, wd, word, minFreq);
					//System.out.print("" + score);
					averageScore += (double)score/(double)candidatesPerFrequency;
					count++;
				}
				System.out.println("\t" + averageScore);
			}
			else{
				System.out.println("\tX");
			}
		}

	}
	
	private static int findFirst(SemDist sd, WordDist[] wd, String word, int wordTF) {
		int counter = 0;
		for(int i = 0; i < wd.length; i++){
			String candidate = wd[i].getWord();
			double candidateTF = wd[i].getTF();
			if(candidateTF >= wordTF){
				int dist = sd.getSimilarity(word, candidate);
				if(dist >= threshold){
					//System.out.print("\t" + word + ":" + candidate + ":");
					return counter;
				}
				counter++;
			}
		}
		return maxListSize;
		//return counter;
	}

	/*private static void evaluateRandomWords(String pos) {
		LoadForCosine loader = new LoadForCosine("/Users/akennedy/Research/buildMatrix/fullMatrix_"+pos+"/fullMatrix_"+pos+".rlabel", 
		"/Users/akennedy/Research/buildMatrix/fullMatrix_"+pos+"/matrix_crs.mat");
		
		SemDist sd = new SemDist(YEAR);
		
		for(int minFreq = 1; minFreq <= maxFrequencyEvaluation; minFreq++){
			int count = 0;
			double score = -1;
			System.out.print(minFreq);
			double averageScore = 0;
			while(count < candidatesPerFrequency){
				int id = (int)(Math.random() * matrixWords.get(minFreq).size());
				String word = matrixWords.get(minFreq).get(id);
				
				//System.out.println(minFreq + " : " + count + " : " + word);
				WordDist[] wd = loader.getClosestCosine(word, maxListSize);
				//loader.printWordArray(wd);
				score = averagePrecision(sd, wd, word, minFreq);
				//System.out.print("\t" + score);
				averageScore += score/(double)candidatesPerFrequency;
				count++;
			}
			System.out.println("\t" + averageScore);
		}

	}

	private static double averagePrecision(SemDist sd, WordDist[] wd, String word, int wordTF) {
		double right = 0;
		double wrong = 0;
		for(int i = 0; i < wd.length && right + wrong < maxWordstoConsider; i++){
			String candidate = wd[i].getWord();
			double candidateTF = wd[i].getTF();
			if(candidateTF >= wordTF){
				int dist = sd.getSimilarity(word, candidate, POS);
				if(dist >= threshold){
					right++;
				}
				else{
					wrong++;
				}
			}
		}
		//System.out.println(averagePrecision + " " + right + " " + wrong);
		if(right + wrong == 0){
			return 0;
		}
		return right/(right+wrong);
	}*/

	/**
	 * Load stop list from file.
	 * 
	 * @param fname
	 */
	private static void loadMatrixWords(String fname) {
		RogetELKB elkb = new RogetELKB(1987);
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			
			String line = br.readLine();
			
			for ( ; ; ) {
				line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
				else {
					String[] parts = line.split(";");
						if(!stopWords.contains(parts[0]) && Integer.parseInt(parts[2]) <= maxFrequencyEvaluation && elkb.index.getEntryListNumerical(parts[0], true, POS).size() > 0){
						matrixWords.get(Integer.parseInt(parts[2])).add(parts[0]);
					}
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
	}
	
	/**
	 * Loads stop word list
	 * 
	 * @param fname
	 */
	private static void loadStopWordList(String fname) {
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

}
