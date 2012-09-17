package ca.akennedy.gents.relatedness;

/**
 * this is a class used to keep track of a word, similarity pair, it is used
 * when finding a ranked list of words.
 * 
 * @author akennedy
 *
 */
public class WordDist {

	private final String word;
	private final float similarity;
	private float termFrequency;
	
	/**
	 * Constructor sets the word and its distance.
	 * 
	 * @param wrd
	 * @param sim
	 */
	public WordDist(String wrd, float sim){
		word = wrd;
		similarity = sim;
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param dist
	 */
	public WordDist(WordDist dist){
		word = dist.getWord();
		similarity = dist.getSimilarity();
		termFrequency = dist.getTF();
	}
	
	/**
	 * Constructor sets the word, its distance from the target
	 * and also records the term frequency of the word.
	 *  
	 * @param wrd
	 * @param sim
	 * @param termFreq
	 */
	public WordDist(String wrd, float sim, float termFreq){
		word = wrd;
		similarity = sim;
		termFrequency = termFreq;
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
		return termFrequency;
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
