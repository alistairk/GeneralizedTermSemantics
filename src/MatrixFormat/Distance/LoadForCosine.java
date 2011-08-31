package MatrixFormat.Distance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

/**
 * This class is used for loading a matrix and performing cosine similarity between
 * pairs of words represented in the matrix. It can be used to find distances between
 * a pair of words, or to get an ordered list of related words.
 * 
 * @author akennedy
 *
 */
public class LoadForCosine {
	
	private TreeMap<String,Integer> words;
	private float[] wordVectorValue;
	private ArrayList<TreeMap<Integer,Float>> matrix;
	
	/**
	 * This loads several test cases for this class.
	 * 
	 * @param args
	 */
	public static void main(String args[]){
		LoadForCosine loader = new LoadForCosine("/Users/akennedy/Research/buildMatrix/fullMatrix_n/fullMatrix_n.rlabel", 
				"/Users/akennedy/Research/buildMatrix/fullMatrix_n/matrix_crs.mat");
		
		//LoadForCosine loader = new LoadForCosine("/Users/akennedy/Research/buildMatrix/Matrix_V_1_1/Matrix_V_1_1.rlabel", 
		//"/Users/akennedy/Research/buildMatrix/Matrix_V_1_1/matrix_crs.mat");
		
		System.out.println(loader.cosine("cat", "cat"));
		System.out.println(loader.cosine("cat", "dog"));
		System.out.println(loader.cosine("dog", "cat"));
		System.out.println(loader.cosine("laugh", "fight"));
		System.out.println(loader.cosine("fight", "laugh"));
		System.out.println(loader.cosine("fight", "asdf"));
		long start = new Date().getTime();
		loader.printWordArray(loader.getClosestCosine("monkey", 100));
		long end = new Date().getTime();
		long difference = end-start;
		System.out.println("time: " + difference);
	}
	
	/**
	 * Constructor, creates a cosine similarity object for a given matrix.
	 * Files for the words in that matrix and the matrix itself are provided.
	 * 
	 * @param wordsFile
	 * @param matrixFile
	 */
	public LoadForCosine(String wordsFile, String matrixFile){
		loadWords(wordsFile);
		loadMatrix(matrixFile);
	}

	/**
	 * Loads a matrix into an ArrayList of hashtables.
	 * 
	 * @param file
	 */
	private void loadMatrix(String file) {
		try{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			String[] parts = line.split(" ");
			wordVectorValue = new float[Integer.parseInt(parts[0])];
			matrix = new ArrayList<TreeMap<Integer,Float>>();
			int count = 0;
			while((line = in.readLine()) != null){
				String[] values = line.split(" ");
				TreeMap<Integer,Float> ht = new TreeMap<Integer,Float>();
				double wordScore = 0;
				for(int i = 1; i < values.length; i+=2){
					int id = Integer.parseInt(values[i-1]);
					float magnitued = Float.parseFloat(values[i]);
					ht.put(id, magnitued);
					wordScore += magnitued * magnitued;
				}
				matrix.add(ht);
				wordVectorValue[count] = (float)Math.sqrt(wordScore);
				
				count++;
				if(count % 10000 == 0){
					System.err.println(count);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Loads words from a file, gives each word an ID and puts it in a hashtable.
	 * 
	 * @param file
	 */
	private void loadWords(String file) {
		words = new TreeMap<String,Integer>();
		try{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			int count = 0;
			while((line = in.readLine()) != null){
				//System.out.println(line);
				words.put(line, count);
				count++;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Performs cosine similarity between two words, first finding their ID 
	 * then calculating the rest.
	 * 
	 * @param word1
	 * @param word2
	 * @return
	 */
	public float cosine(String word1, String word2){
		int id1 = 0;
		int id2 = 0;
		try{
			id1 = words.get(word1);
		} catch(Exception e){
			return -1;
		}
		try{
			id2 = words.get(word2);
		} catch(Exception e){
			return -2;
		}
		TreeMap<Integer, Float> row1 = matrix.get(id1);
		TreeMap<Integer, Float> row2 = matrix.get(id2);
		float denominator = wordVectorValue[id1] * wordVectorValue[id2];
		return cosineValue(row1, row2, denominator);
	}
	
	private float cosineValue(TreeMap<Integer, Float> row1, TreeMap<Integer, Float> row2, float denominator){
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
	 * Gets a list of closest words to a given word.
	 * The second argument is the number of closest words.
	 * 
	 * @param word
	 * @param topX
	 * @return
	 */
	public WordDist[] getClosestCosine(String word, int topX){
		WordDist[] toReturn = new WordDist[topX];
		for(int i = 0; i < topX; i++){
			WordDist d = new WordDist("", 0);
			toReturn[i] = d;
		}
		int id1 = 0;
		try{
			id1 = words.get(word);
		}
		catch(Exception e){
			return null;
		}
		
		TreeMap<Integer, Float> row1 = matrix.get(id1);
		float denominator1 = wordVectorValue[id1];
		for(String key : words.keySet()){
			int id2 = words.get(key);
				if(id1 != id2){
				double numerator = 0;
				TreeMap<Integer, Float> row2 = matrix.get(id2);
				float denominator = denominator1 * wordVectorValue[id2];
				
				for(int feature : row1.keySet()){
					if(row2.containsKey(feature)){
						numerator += row1.get(feature) * row2.get(feature);
					}
				}
				float cosine =  cosineValue(row1, row2, denominator);
				//double featCount = 0;
				//for(int feature : row2.keySet()){
				//	featCount += row2.get(feature);
				//}
				//double cosine = numerator/denominator;
				WordDist w = new WordDist(key,cosine);
				insert(toReturn, w);
			}
		}
		
		return toReturn;
	}

	/**
	 * Inserts a word into a list of closest related words.
	 * @param toReturn
	 * @param w
	 */
	private void insert(WordDist[] toReturn, WordDist w) {
		int index = toReturn.length-1;
		while(index >= 0 && toReturn[index].getSimilarity() < w.getSimilarity()){
			if(index < toReturn.length-1){
				toReturn[index+1] = toReturn[index];
			}
			index--;
		}
		if(index < toReturn.length-1){
			toReturn[index+1] = w;
			//printWordArray(toReturn);
		}
		
	}
	
	/**
	 * Prints out an array of words in a pleasing format.
	 * 
	 * @param words
	 */
	public void printWordArray(WordDist[] words){
		if(words != null){
			for(int i = 0; i < words.length-1; i++){
				System.out.print(words[i] + ", ");
			}
			System.out.println(words[words.length-1]);
		}
		else{
			System.out.println();
		}
	}
	
	/**
	 * Prints out an array of words in a pleasing format.
	 * 
	 * @param words
	 */
	public String wordArray2String(WordDist[] words){
		String toReturn = "";
		if(words != null){
			for(int i = 0; i < words.length-1; i++){
				toReturn += words[i] + ", ";
			}
			toReturn += words[words.length-1];
		}
		return toReturn;
	}
	
	/**
	 * Prints out an array of words in a pleasing format.
	 * 
	 * @param words
	 */
	public synchronized void printWordArray(String target, WordDist[] words){
		System.out.print(target + " : ");
		if(words != null){
			for(int i = 0; i < words.length-1; i++){
				System.out.print(words[i] + ", ");
			}
			System.out.println(words[words.length-1]);
		}
		else{
			System.out.println();
		}
	}

}
