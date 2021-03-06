package ca.uottawa.gents.relatedness;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * This class is used for loading a matrix and performing cosine similarity between
 * pairs of words represented in the matrix. It can be used to find distances between
 * a pair of words, or to get an ordered list of related words. It takes two arguments
 * one being the path to the list of words, the "rlabel" file and the second being to
 * the row matrix file "matrix_crs.mat". It will work with re-weighted matrices,
 * "matrix_crs.mat.TYPE_POS" generated by WeightFeaturesUnsupervised.
 * 
 * To run:
 * java LoadForCosine <path to rlabel file> <path to matrix_crs.mat file>
 * 
 * This program will simply execute some tests from its main method, but more likely you
 * will want to create another class that calls it. See the main method for some examples.
 * 
 * A new LoadForCosine is created: LoadForCosine loader = new LoadForCosine(args[0], args[1]);
 * There are two main methods of using this class, the first is to find the cosine similarity
 * between two words, e.g.: loader.cosine("cat", "dog");
 * 
 * When measuring cosine similarity between two words, if the first is not found it returns
 * -1, if the second word is not found it returns -2.
 * 
 * The second main function of this class is to generate a list of the closest related words
 * to a given word. This can be run as follows: loader.getClosestCosine("monkey", 100);
 * 
 * This will return an array of type WordDist containing the 100 closest related words to 
 * "monkey" from the matrix. This function runs much slower than cosine similarity as it
 * requires the distance between the word, "monkey", and all other words in the matrix to
 * be calculated.
 * 
 * An easy method for printing out an array of type wordDist is provided as well:
 * loader.printWordArray(loader.getClosestCosine("monkey", 100));
 * 
 * This method will print out lists of neighbouring words in the following format:
 * beet (0.44928065), electrodynamics (0.34833947), sleeve (0.30810094), trivium (0.30095348) ...
 * where the cosine similarity to "monkey" is indicated in brackets.
 * 
 * @author akennedy
 *
 */
public class LoadForRelatedness {
	//TreeMap maps words to their row number in the matrix
	private Map<String,Integer> words;
	//holds the denominator scores for each word, calculated at load time.
	private float[] wordVectorValue;
	// holds the matrix, every word is made up of a TreeMap of column, value pairs.
	private List<Map<Integer,Float>> matrix;
	
	private static final Logger LOGGER = Logger.getLogger(LoadForRelatedness.class.getName());
	
	
	/**
	 * This constructor takes two arguments, the first being a list of words found
	 * in the matrix, the "rlabel" file. The second is the path to the sparse row
	 * matrix "matrix_crs.mat". This calls functions to load the words and the
	 * matrix.
	 * 
	 * @param wordsFile
	 * @param matrixFile
	 */
	public LoadForRelatedness(String wordsFile, String matrixFile){
		loadWords(wordsFile);
		loadMatrix(matrixFile);
	}

	/**
	 * Loads the sparse matrix rows file "matrix_crs.mat". It reads the file
	 * and simultaneously calculates the magnitude of each word vector in the matrix
	 * thus calculating most of the denominator at load time. This slows down the
	 * load time somewhat, but hopefully will save time when running many queries
	 * on the matrix.
	 * 
	 * 
	 * @param file
	 */
	private void loadMatrix(String file) {
		try{
			BufferedReader matrixReader = new BufferedReader(new FileReader(file));
			String line = matrixReader.readLine();
			String[] parts = line.split(" ");
			wordVectorValue = new float[Integer.parseInt(parts[0])];
			matrix = new ArrayList<Map<Integer,Float>>();
			int count = 0;
			
			for ( ; ; ) {
				line = matrixReader.readLine();
	
				if (line == null) {
					matrixReader.close();
					break;
				}
				else {
					String[] values = line.split(" ");
					Map<Integer,Float> rowMap = new TreeMap<Integer,Float>();
					double wordScore = 0;
					for(int i = 1; i < values.length; i+=2){
						int contextID = Integer.parseInt(values[i-1]);
						float magnitued = Float.parseFloat(values[i]);
						rowMap.put(contextID, magnitued);
						wordScore += magnitued * magnitued;
					}
					matrix.add(rowMap);
					wordVectorValue[count] = (float)Math.sqrt(wordScore);
					
					count++;
					if(count % 10000 == 0){
						LOGGER.info("Loaded "+count+" lines");
					}
				}
			}
		}
		catch(Exception e){
			LOGGER.warning(e.getMessage());
		}
	}

	/**
	 * Loads from the "rlabel" file and stores every word in the words TreeMap
	 * connecting each word to its corresponding line in the array.
	 * 
	 * @param file
	 */
	private void loadWords(String file) {
		words = new TreeMap<String,Integer>();
		try{
			BufferedReader labelReader = new BufferedReader(new FileReader(file));
			int count = 0;
			for ( ; ; ) {
				String line = labelReader.readLine();
	
				if (line == null) {
					labelReader.close();
					break;
				}
				else {
					//System.out.println(line);
					words.put(line, count);
					count++;
				}
			}
		}
		catch(Exception e){
			LOGGER.warning(e.getMessage());
		}
		
	}
	
	/**
	 * Performs cosine similarity between two words. If the first word is missing
	 * it returns -1, if the second word is missing it returns -2, otherwise it
	 * returns the similarity between 0..1.
	 * 
	 * @param word1
	 * @param word2
	 * @return
	 */
	public float distance(String word1, String word2){
		float returnValue = -1;
		int id1 = 0;
		int id2 = 0;
		boolean id1Found = true;
		boolean id2Found = true;
		try{
			id1 = words.get(word1);
		} catch(Exception e){
			id1Found = false;
		}
		try{
			id2 = words.get(word2);
		} catch(Exception e){
			id2Found = false;
		}
		
		if(id1Found && id2Found){
			//get TreeMap vectors for both the words and calculate the denominator
			Map<Integer, Float> row1 = matrix.get(id1);
			Map<Integer, Float> row2 = matrix.get(id2);
			float denominator = wordVectorValue[id1] * wordVectorValue[id2];
			//calculate and return cosine similarity
			returnValue = cosineValue(row1, row2, denominator);
		}
		return returnValue;
	}
	
	/**
	 * Takes row1 and row2 as Maps and the pre-calculated denominator. The 
	 * shorter of these two rows is selected and iterated through. Common features 
	 * between the two rows are identified and the numerator is calculated. Finally
	 * it returns the cosine similarity.
	 * 
	 * @param row1
	 * @param row2
	 * @param denominator
	 * @return
	 */
	private float cosineValue(Map<Integer, Float> row1, Map<Integer, Float> row2, float denominator){
		float numerator = 0;
		if(row1.keySet().size() <= row2.keySet().size()){
			for(int key : row1.keySet()){
				if(row2.containsKey(key)){
					numerator += row1.get(key) * row2.get(key);
				}
			}
		}
		else{
			for(int key : row2.keySet()){
				if(row1.containsKey(key)){
					numerator += row1.get(key) * row2.get(key);
				}
			}
		}
		
		return numerator/denominator;
	}
	
	/**
	 * This method calculates a list of the closest related words to a given word.
	 * The first argument is a word and the second argument is the number of nearest
	 * neighbours that are desired. If the word is not found null is returned.
	 * Otherwise it goes through the set of words and calculates the
	 * distance between the word passed as an argument and all other
	 * words in the matrix using the cosineValue method. 
	 * 
	 * @param word
	 * @param topX
	 * @return
	 */
	public WordDist[] getClosestWords(String word, int topX){
		int id1 = 0;
		boolean id1Found = true;
		WordDist[] toReturn = null;
		
		try{
			id1 = words.get(word);
		}
		catch(Exception e){
			id1Found = false;
		}
		
		if(id1Found){
			//declare the array to be returned.
			toReturn = new WordDist[topX];
			for(int i = 0; i < topX; i++){
				WordDist wrdDst = new WordDist("", 0);
				toReturn[i] = wrdDst;
			}
			
			Map<Integer, Float> row1 = matrix.get(id1);
			float denominator1 = wordVectorValue[id1];
			//go through each word in the matrix
			for(String key : words.keySet()){
				int id2 = words.get(key);
					if(id1 != id2){
					Map<Integer, Float> row2 = matrix.get(id2);
					float denominator = denominator1 * wordVectorValue[id2];
					
					float cosine =  cosineValue(row1, row2, denominator);
					WordDist wordDist = new WordDist(key,cosine);
					//insert the new WordDist into the array to return
					insert(toReturn, wordDist);
				}
			}
			
		}
		return toReturn;
	}

	/**
	 * Inserts takes an array of type WordDist and a WordDist to be inserted into
	 * the list. The WordDist w is placed in order into the array.
	 * 
	 * @param toReturn
	 * @param wordDist
	 */
	private void insert(WordDist[] toReturn, WordDist wordDist) {
		int index = toReturn.length-1;
		while(index >= 0 && toReturn[index].getSimilarity() < wordDist.getSimilarity()){
			if(index < toReturn.length-1){
				toReturn[index+1] = toReturn[index];
			}
			index--;
		}
		if(index < toReturn.length-1){
			toReturn[index+1] = wordDist;
			//printWordArray(toReturn);
		}
		
	}
	
	/**
	 * Prints out the content of an array of type WordDist. It is
	 * synchronized so that multiple threads can be called at
	 * once.
	 * 
	 * @param words
	 */
	public String getWordArrayString(WordDist[] words){
		synchronized(this){
			String toReturn = "";
			if(words != null){
				for(int i = 0; i < words.length-1; i++){
					toReturn += words[i] + ", ";
				}
				toReturn += words[words.length-1];
			}
			return toReturn;
		}
	}

	
	/**
	 * Prints out the content of an array of type WordDist with the
	 * target word as well. It is synchronized so that multiple threads
	 * can be called at once.
	 * 
	 * @param words
	 */
	public String getWordArrayString(String target, WordDist[] words){
		synchronized(this){
			String toReturn = "";
			toReturn += target + " : " + getWordArrayString(words);
			return toReturn;
		}
	}

}
