package GenTS.WeightMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;

/**
 * This class is used by WeightFeaturesSupervised in order to calculate whether two words
 * are related or not. This implements the interface Distance which was originally used in
 * Roget's SemDist. Some of its functionality does not really apply to this class but is 
 * included reasons of compatability.
 * 
 * @author akennedy
 *
 */
public class BinaryDist implements Distance {
	
	private Hashtable<String, TreeSet<Integer>> wordLines;
	private ArrayList<String> words;
	
	/**
	 * Constructor takes in a file where each row contains a set of related words.
	 * Words across different rows should be unrelated. The words are loaded along
	 * with their row number and can be used to determine if two words appeared on 
	 * the same row or not.
	 * 
	 * @param fname
	 */
	public BinaryDist(String fname) {
		wordLines = new Hashtable<String, TreeSet<Integer>>();
		words = new ArrayList<String>();
		int lineCount = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
	         
			for ( ; ; ) {
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
	
				else {
					String[] words = line.split(" ");
					for(String word : words){
						addWord(word, lineCount);
					}
					lineCount++;
				}
			}
	
		} catch (Exception e) {
	    	 e.printStackTrace();
		}
	}

	/**
	 * Adds a new word and line number to the hashtable.
	 * 
	 * @param word
	 * @param lineCount
	 */
	private void addWord(String word, int lineCount) {
		TreeSet<Integer> al;
		if(wordLines.containsKey(word)){
			al = wordLines.get(word);
		}
		else{
			al = new TreeSet<Integer>();
			words.add(word);
		}
		al.add(lineCount);
		wordLines.put(word, al);
	}
	
	/**
	 * Checks to see if a word exists in wordLines.
	 * 
	 * @param word
	 * @return
	 */
	public boolean existsWord(String word){
		return wordLines.containsKey(word);
	}

	/**
	 * Calculates similarity between two words, returns -1 if the first
	 * word does not exist, returns -2 if the second word does not exist
	 * and returns -3 if neither word exists.
	 * 
	 */
	@Override
	public int getSimilarity(String word1, String word2) {
		boolean exist1 = existsWord(word1);
		boolean exist2 = existsWord(word2);
		if(!exist1 && !exist2){
			return -3;
		}
		else if(!exist2){
			return -2;
		}
		else if(!exist1){
			return -1;
		}
		TreeSet<Integer> set1 = wordLines.get(word1);
		TreeSet<Integer> set2 = wordLines.get(word2);
		for(int id1 : set1){
			if(set2.contains(id1)){
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Calculates distance when the POS of the two words is included, does not really
	 * apply to this class.
	 */
	@Override
	public int getSimilarity(String word1, String word2, String pos) {
		return getSimilarity(word1, word2);
	}

	/**
	 * Calculates distance when the POS of each word is included, does not really
	 * apply to this class.
	 */
	@Override
	public int getSimilarity(String word1, String pos1, String word2, String pos2) {
		return getSimilarity(word1, word2);
	}

}
