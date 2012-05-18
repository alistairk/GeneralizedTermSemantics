package GenTS.Relatedness;

/**
 * this is a class used to keep track of a word, similarity pair, it is used
 * when finding a ranked list of words.
 * 
 * @author akennedy
 *
 */
public class WordDist {

	private String word;
	private float similarity;
	private float tf;
	
	/**
	 * Constructor sets the word and its distance.
	 * 
	 * @param w
	 * @param s
	 */
	public WordDist(String w, float s){
		word = w;
		similarity = s;
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param wd
	 */
	public WordDist(WordDist wd){
		word = wd.getWord();
		similarity = wd.getSimilarity();
		tf = wd.getTF();
	}
	
	/**
	 * Constructor sets the word, its distance from the target
	 * and also records the term frequency of the word.
	 *  
	 * @param w
	 * @param s
	 * @param t
	 */
	public WordDist(String w, float s, float t){
		word = w;
		similarity = s;
		tf = t;
	}
	
	/**
	 * Gets the word.
	 * 
	 * @return
	 */
	public String getWord(){
		return word;
	}
	
	/**
	 * Gets term frequency
	 * 
	 * @return
	 */
	public float getTF(){
		return tf;
	}
	
	/**
	 * Get similarity.
	 * 
	 * @return
	 */
	public float getSimilarity(){
		return similarity;
	}
	
	/**
	 * Returns string "word (similarity)
	 */
	public String toString(){
		//return word + " (" + similarity + ", " + tf + ")";
		return word + " (" + similarity + ")";
	}
	
}
