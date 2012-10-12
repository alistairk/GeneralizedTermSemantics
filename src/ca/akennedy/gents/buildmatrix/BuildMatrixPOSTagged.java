package ca.akennedy.gents.buildmatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;


/**
 * This class builds a term-context matrix from POS tagged data. It works in two
 * different languages, French and English, though is intended to work for German as
 * well. Most of the code is general, however there are language specific parts for
 * the POS tags.
 * 
 * The program works on POS tagged code created by the Stanford POS tagger.
 * 
 * To Run Program: java BuildMatrixPOSTagged <en|fr|de> <N|V|A> <output Directory> <output File Name> <min Term Frequency> <min Context Frequency> <window size> <parsedFile 1> ... <parsedFile n>
 * 
 * @author akennedy
 *
 */
public class BuildMatrixPOSTagged extends BuildMatrix {
	
	protected final int windowSize;
	protected final String language;

	/**
	 * main method takes arguments, builds an object for constructing the matrix and calls
	 * its methods.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 8){
			LOGGER.info("To Run Program: java BuildMatrixPOSTagged <en|fr|de> <N|V|A> <output Directory> <output File Name> <min Term Frequency> <min Context Frequency> <window size> <parsedFile 1> ... <parsedFile n>");
		}
		else{
			String lang = args[0];
			String POS = args[1];
			String directory = args[2];
			String matrixName = args[3];
			BuildMatrixPOSTagged buildMat = new BuildMatrixPOSTagged(matrixName, lang, POS, Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6])); //N:35, V:10, A:35
			boolean dirCreated = buildMat.createDirectory(directory, matrixName);
			if(!dirCreated){
				return;
			}
			for(int i = 7; i < args.length; i++){ 
				String file = args[i];
				buildMat.loadFile(file);
			}
	
			//Generates lists of columns and lists of rows
			buildMat.generateColumnMap();
			buildMat.generateRowMap();
			
			//records matrix size information
			buildMat.writeInfo("Matrix Info File");
			
			//generates two sparse matrix files, one by row, one by column
			buildMat.generateCRS();
			buildMat.generateCCS();
		}
	}
	
	/**
	 * The constructor, takes the name of the matrix, the part of speech
	 * and the minimum number of rows, columns and the window size. It 
	 * initializes the TreeMaps and ArrayLists to store the words and contexts.
	 * 
	 * @param matName
	 * @param pos
	 * @param rowMin
	 * @param colMin
	 * @param window
	 */
	public BuildMatrixPOSTagged(String matName, String lang, String pos, int rowMin, int colMin, int window){
		super(matName, pos, rowMin, colMin);
		windowSize = window-1; // decrease window size by one to take account of the target word
		language = lang;
	}
	
	/**
	 * Loads a file and records all the word context pairs.
	 * The context can contain any pat of speech as long as the word it is related
	 * to is a single word, not a phrase and is made up completely of letters. No
	 * numbers of punctuation.
	 * 
	 * Edit this method if you want to allow for different parts of speech, multi-word 
	 * expressions or capitals
	 * 
	 * @param fname
	 */
	public void loadFile(String fname) {
		synchronized(this){
			LOGGER.info("Loading: " + fname);
			try {
				BufferedReader parseReader = new BufferedReader(new FileReader(fname));
		        int lineNumber = 0;
				for ( ; ; ) {
					String line = parseReader.readLine();
					lineNumber++;
		
					if (line == null) {
						parseReader.close();
						break;
					}
					else {
						char firstChar = line.charAt(0);
						firstChar = Character.toLowerCase(firstChar);
						line = firstChar + line.substring(1);
						
						String[] parts = line.split("\\s+");
						for(int target = 0; target < parts.length; target++){
							String targetWord = wordPOSMap(parts[target]);
							if(wordMatches(targetWord)){
								for(int neighbour = Math.max(0, target-windowSize); neighbour < Math.min(target+windowSize, parts.length); neighbour++){
									String possibleContext = wordPOSMap(parts[neighbour]);
									//System.out.println(possibleContext);
									if(neighbour != target && contextMatches(possibleContext)){
										//System.out.println(targetWord + "\t" + possibleContext);
										String word = targetWord.substring(0, targetWord.lastIndexOf("_"));
										String context = possibleContext.substring(0, possibleContext.lastIndexOf("_")).toLowerCase() + possibleContext.substring(possibleContext.lastIndexOf("_"));
										int wordID = getWord(word);
										int contextID = getContext(context);
										countPair(wordID, contextID);
									}
								}
							}
						}
					}
					if(lineNumber % 100000 == 0){
						LOGGER.info("File: " + fname + "\tLine: " + lineNumber);
					}
					
				}
		
			} catch (Exception e) {
		    	 LOGGER.warning(e.getMessage());
			}
			LOGGER.info(wordCounter + " : " + contextCounter + " : " + nonZeroEnties);
		}
	}

	/**
	 * This method performs a language specific mapping from different parts of speech.
	 * Nouns, verbs, adjectives and adverbs are represented by N, V, A and R respectively.
	 * 
	 * @param word
	 * @return
	 */
	private String wordPOSMap(String word) {
		String context = "";
		try{
			String fixedWord = new String(word.getBytes(), "UTF-8");
			String[] wordParts = fixedWord.split("_");
			if("en".equals(language)){
				if(wordParts[1].matches("NN.*")){
					context = wordParts[0]+"_N";
				}
				else if(wordParts[1].matches("VB.*")){
					context = wordParts[0]+"_V";
				}
				else if(wordParts[1].matches("JJ.*")){
					context = wordParts[0]+"_A";
				}
				else if(wordParts[1].matches("RB.*")){
					context = wordParts[0]+"_R";
				}
			}
			else if("fr".equals(language)){
				if(wordParts[1].matches("N.*")){
					context = wordParts[0]+"_N";
				}
				else if(wordParts[1].matches("V.*")){
					context = wordParts[0]+"_V";
				}
				else if(wordParts[1].matches("A.*")){
					context = wordParts[0]+"_A";
				}
				else if(wordParts[1].matches("ADV.*")){
					context = wordParts[0]+"_R";
				}
			}
			else if("de".equals(language)){
				if(wordParts[1].matches("NN")){
					context = wordParts[0].toLowerCase()+"_N";
				}
				else if(wordParts[1].matches("V.*")){
					context = wordParts[0].toLowerCase()+"_V";
				}
				else if(wordParts[1].matches("ADJ.*")){
					context = wordParts[0].toLowerCase()+"_A";
				}
				else if(wordParts[1].matches("ADV")){
					context = wordParts[0].toLowerCase()+"_R";
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.warning(e.getMessage());
		}
		return context;
	}

	/**
	 * This program checks to see that a given word matches the desired
	 * part-of-speech, be it noun, verb or adjective. 
	 * 
	 * @param word
	 * @return
	 */
	private boolean wordMatches(String word) {
		boolean goodWord = false;
		if("N".equals(pos) && word.matches("^[\\p{Ll}]+_N")){
			goodWord = true;
		}
		else if("V".equals(pos) && word.matches("^[\\p{Ll}]+_V")){
			goodWord = true;
		}
		else if("A".equals(pos) && word.matches("^[\\p{Ll}]+_A")){
			goodWord = true;
		}
		return goodWord;
	}
	
	/**
	 * Checks to see if a word can be used as a context. 
	 * - The context of a noun must be either a verb or an adjective. 
	 * - The context of a verb must be either an adverb or a noun.
	 * - The context of an adjective must be either an adverb or a noun.
	 * 
	 * @param word
	 * @return
	 */
	private boolean contextMatches(String word) {
		boolean goodWord = false;
		if("N".equals(pos) && word.matches("^[\\p{L}]+_(V|A|N)")){
			goodWord = true;
		}
		else if("V".equals(pos) && word.matches("^[\\p{L}]+_(R|N)")){
			goodWord = true;
		}
		else if("A".equals(pos) && word.matches("^[\\p{L}]+_(R|N)")){
			goodWord = true;
		}
		return goodWord;
	}
}
