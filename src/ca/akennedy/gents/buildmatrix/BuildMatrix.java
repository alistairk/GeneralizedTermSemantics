package ca.akennedy.gents.buildmatrix;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * This program creates a matrix in the style of SuperMatrix. See:
 * Bartosz Broda,Maciej Piasecki.SuperMatrix: a General tool for lexical semantic knowledge acquisition. In Proceedings of IMCSIT'2008. pp.345~352
 * 
 * This program reads in files of dependency triples generated using Minipar and produces a matrix.
 * The parsed input files should be as follows:
 * be			VBE:pred:N	philosophy
 * philosophy	N:subj:N	anarchism
 * 
 * Words and contexts are extracted, such as "philosophy" appears in the context <N:subj:N, anarchism> and
 * "anarchism" appears in the context <philosophy, N:subj:N>
 * 
 * See:
 * Lin, Dekang: Dependency-based Evaluation of MINIPAR. In: Proc. Workshop on the Evaluation of Parsing Systems, 1998 (Granada)
 * 
 * Matrices can be produces for nouns, verbs or adjectives/adverbs. The name of the location of a directory 
 * in which to place the matrix files and the name of the matrix are given as the second and third argument
 * while the nimum term frequency and minimum context frequency are the fourth and fifth arguments. After
 * this enter a list of parsed files. Execute the program as follows:
 * 
 * java BuildMatrix <N|V|A> <output Directory> <output Matrix Name> <min Term Frequency> <min Context Frequency> <parsedFile 1> ... <parsedFile n>
 * 
 * TODO:
 * The matrix only accepts words in lower case with no spaces. This is done in the function "loadFile".
 * Perhaps  a regular expression could be made a parameter of this function.
 * 
 * @author akennedy
 *
 */
public class BuildMatrix {
	//the POS, directory and name of matrix
	protected final String pos;
	protected String fullDirectory;
	protected final String matrixName;
	
	//minimum column and row counts
	protected final int minColumns;
	protected final int minRows;
	
	//counts of words, contexts and non zero entries in the matrix
	protected int wordCounter;
	protected int contextCounter;
	protected int nonZeroEnties;
	
	// maps words and columns to their index values
	protected final Map<String, Integer> word2Index;
	protected final Map<String, Integer> context2Index;
	
	//maps an index value to the word or context
	protected final List<String> index2Word;
	protected final List<String> index2Context;
	
	// count of each specific word or context and count of each pair
	protected final List<Integer> wordCount; //index is word ID
	protected final List<Integer> contextCount; //index is context ID
	protected final List<Map<Integer, Integer>> pairCounter; // ArrayList index is Word ID, TreeMap index is context ID

	protected final List<Set<Integer>> uniqueWordCount; //index is word ID
	protected final List<Set<Integer>> uniqueContextCount; //index is context ID
	
	//maps the sorted row/column number to the original
	protected int[] rowSort2orig;
	protected int[] columnSort2orig;
	
	//maps the original row/column number to sorted
	//private int[] rowOrig2sort;
	protected int[] columnOrig2sort;
	
	//counts the number of rows, columns and entries
	protected int rowCount;
	protected int columnCount;
	protected int entryCount;
	
	//keeps track of the previous context's relation type
	protected String prevFeatureType;
	
	protected static final Logger LOGGER = Logger.getLogger(BuildMatrix.class.getName());
	
	
	/**
	 * Takes arguments as follows:
	 * <N|V|A> <output Directory> <output File Name> <min Term Frequency> <min Context Frequency> <parsedFile 1> ... <parsedFile n>
	 * 
	 * The parsed input files should be as follows:
	 * be			VBE:pred:N	philosophy
	 * philosophy	N:subj:N	anarchism
	 * 
	 * It is recommended that min term Frequency is 35 for Nouns and Adjectives while 10 for Verbs.
	 * Min Context Frequency can be anything, but I recommend something low, I used 2.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 6){
			LOGGER.info("To Run Program: java BuildMatrix <N|V|A> <output Directory> <output File Name> <min Term Frequency> <min Context Frequency> <parsedFile 1> ... <parsedFile n>");
		}
		else{
			String POS = args[0];
			String directory = args[1];
			String matrixName = args[2];
			BuildMatrix buildMat = new BuildMatrix(matrixName, POS, Integer.parseInt(args[3]), Integer.parseInt(args[4])); //N:35, V:10, A:35
			boolean dirCreated = buildMat.createDirectory(directory, matrixName);
			if(!dirCreated){
				return;
			}
			for(int i = 5; i < args.length; i++){ 
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
	 * Writes out the info file. This file contains a string, ideally telling
	 * what was parsed to build this, it also details the total number of
	 * words, featues and non-zero entries in the matrix as well as the number
	 * left after reducing the matrix.
	 * 
	 * @param info
	 */
	protected void writeInfo(String info) {
		try{
			String infoFile = fullDirectory + "/info.txt";
			BufferedWriter infoWriter = new BufferedWriter(new FileWriter(infoFile));
			infoWriter.write(info + "\n");
			infoWriter.write("Total Words: "+wordCounter+"\tTotal Contexts: "+contextCounter+"\tTotal Entries: "+nonZeroEnties+"\n");
			infoWriter.write("Used Words: "+rowCount+"\tUsed Contexts: "+columnCount+"\tUsed Entries: "+entryCount + "\n");
			infoWriter.flush();
			infoWriter.close();
		}
		catch(Exception e){
			//e.printStackTrace();
			LOGGER.warning(e.getMessage());
		}
	}


	/**
	 * The constructor, takes the name of the matrix, the part of speech
	 * and the minimum number of rows and columns. It initializes the
	 * TreeMaps and ArrayLists to store the words and contexts.
	 * 
	 * @param matName
	 * @param pos
	 * @param rowMin
	 * @param colMin
	 */
	public BuildMatrix(String matName, String pos, int rowMin, int colMin){
		matrixName = matName;
		this.pos = pos;
		
		word2Index = new TreeMap<String, Integer>();
		index2Word = new ArrayList<String>();
		
		context2Index = new TreeMap<String, Integer>();
		index2Context = new ArrayList<String>();
		
		wordCount = new ArrayList<Integer>();
		contextCount = new ArrayList<Integer>();
		
		uniqueWordCount = new ArrayList<Set<Integer>>();
		uniqueContextCount = new ArrayList<Set<Integer>>();
		
		pairCounter = new ArrayList<Map<Integer, Integer>>();
		wordCounter = 0;
		contextCounter = 0;
		nonZeroEnties = 0;
		
		minColumns = colMin;
		minRows = rowMin;
		
		rowCount = 0;
		columnCount = 0;
		entryCount = 0;
		
		prevFeatureType = "";
	}
	
	/**
	 * This prints out the rows files, including the matrix as well as the one counting
	 * the number of features in the row.
	 */
	public void generateCRS() {
		try{
			String outFile = fullDirectory + "/matrix_crs.mat";
			BufferedWriter rowWriter = new BufferedWriter(new FileWriter(outFile));
			LOGGER.info("Building: " + outFile);

			String featureFile = fullDirectory + "/row_features.csv";
			BufferedWriter featureWriter = new BufferedWriter(new FileWriter(featureFile));

			featureWriter.write("label;nz;tf;TF;entropy\n");
			
			rowWriter.write(rowCount + " " + columnCount + " " + entryCount + "\n");
			
			for(int i = 0; i < rowSort2orig.length; i++){
				int rowID = rowSort2orig[i];
				if(rowID != -1){
					Map<Integer, Integer> row = pairCounter.get(rowID);
					List<Integer> values = getLine(row, rowWriter);
					writeFeature(index2Word.get(rowID), featureWriter, values);
				}
				if((i+1) % 10000 == 0){
					LOGGER.info("Processed " + (i+1) + " rows");
				}
			}
			rowWriter.close();
			featureWriter.close();
		}
		catch(Exception e){
			LOGGER.warning(e.getMessage());
		}
		pairCounter.clear();
	}
	
	/**
	 * Gets the contents of a row. Prints out values in to the matrix and returns
	 * a List of values.
	 * 
	 * @param row
	 * @param rowWriter
	 * @return
	 * @throws IOException
	 */
	public List<Integer> getLine(Map<Integer, Integer> row, BufferedWriter rowWriter) throws IOException{
		Map<Integer, Integer> mappedKeyValue = new TreeMap<Integer, Integer>();
		//String rowString = "";
		List<Integer> values = new ArrayList<Integer>();
		for(int key : row.keySet()){
			if(columnOrig2sort[key] != -1){
				mappedKeyValue.put(columnOrig2sort[key], row.get(key));
			}
		}
		ArrayList<Integer> fields = new ArrayList<Integer>(mappedKeyValue.keySet());
		Collections.sort(fields);
		for(int i = 0; i < fields.size(); i++){
			int key = fields.get(i);
			rowWriter.write(key + " " + mappedKeyValue.get(key));
			if(i != fields.size()-1){
				rowWriter.write(" ");
			}
			values.add(mappedKeyValue.get(key));
		}
		rowWriter.write("\n");
		rowWriter.flush();
		return values;
	}
	

	/**
	 * Write out the features, nz, tf and entropy.
	 * 
	 * @param word
	 * @param featureWriter
	 * @param values
	 * @throws IOException
	 */
	protected void writeFeature(String word, BufferedWriter featureWriter, List<Integer> values) throws IOException {
		int nonZero = values.size();
		int termFreq = 0;
		for(int i = 0; i < values.size(); i++){
			termFreq += values.get(i);
		}
		double entropy = 0.0;
		for(int i = 0; i < values.size(); i++){
			double prob = values.get(i) / (double) termFreq;
			entropy += prob * Math.log(prob)/Math.log(2.0);
		}

		if(entropy < 0){
			entropy = -entropy;
		}
        featureWriter.write(word + ";" + nonZero + ";" + termFreq + ";" + termFreq + ";" + entropy + "\n");
        featureWriter.flush();
	}
	
	/**
	 * Generates the column files.
	 */
	public void generateCCS() {
		try {
			String inFile = fullDirectory + "/matrix_crs.mat";
			String outFile = fullDirectory + "/matrix_ccs.mat";
			BufferedWriter columnWriter = new BufferedWriter(new FileWriter(outFile));
			BufferedReader rowReader = new BufferedReader(new FileReader(inFile));
			LOGGER.info("Building: " + outFile);
			

			String featureFile = fullDirectory + "/column_features.csv";
			BufferedWriter featureWriter = new BufferedWriter(new FileWriter(featureFile));
			

			String boundaryFile = fullDirectory + "/boundary.txt";
			BufferedWriter boundaryWriter = new BufferedWriter(new FileWriter(boundaryFile));
			
			featureWriter.write("label;nz;tf;TF;entropy\n");
			
			String line = rowReader.readLine(); // get first line
			
			columnWriter.write(line + "\n");

			String[] firstBreak = line.split("\\s+");
			int arrayLength = Integer.parseInt(firstBreak[1]);
			@SuppressWarnings("unchecked")
			List<Integer>[] idArray = new List[arrayLength];
			@SuppressWarnings("unchecked")
			List<Integer>[] valueArray = new List[arrayLength];
			for(int i = 0; i < idArray.length; i++){
				idArray[i] = new ArrayList<Integer>();
				valueArray[i] = new ArrayList<Integer>();
			}
			int count = 0;
			
			for ( ; ; ) {
				line = rowReader.readLine();
	
				if (line == null) {
					rowReader.close();
					break;
				}
				else {
					String[] parts = line.split(" ");
					for(int i = 1; i < parts.length; i += 2){
						idArray[Integer.parseInt(parts[i-1])].add(count);
						valueArray[Integer.parseInt(parts[i-1])].add(Integer.parseInt(parts[i]));
					}
					count++;
					if(count % 10000 == 0){
						LOGGER.info("Loaded " + count + " rows");
					}
				}
			}
			for(int i = 0; i < valueArray.length; i++){
				//String outputLine = "";
		        if(valueArray[i].size() > 0){
	        		List<Integer> ids = idArray[i];
	        		List<Integer> vals = valueArray[i];
		        	for(int j = 0; j < vals.size(); j++){
		        		columnWriter.write(ids.get(j) + " " + vals.get(j));
		        		if(j != vals.size()-1){
		        			columnWriter.write(" ");
		        		}
		        	}
		        }
		        columnWriter.write("\n");
		        columnWriter.flush();
		        
				writeFeature(index2Context.get(columnSort2orig[i]), featureWriter, valueArray[i]);
				printBoundary(boundaryWriter, index2Context.get(columnSort2orig[i]), i);
		        
				if((i+1) % 10000 == 0){
					LOGGER.info("Written " + (i+1) + " columns");
				}
			}
			int lastBoundary = valueArray.length -1;
			boundaryWriter.write(lastBoundary + "\n");
			columnWriter.close();
			featureWriter.close();
			boundaryWriter.close();
		} 
		catch (Exception e) {
	    	 LOGGER.warning(e.getMessage());
		}
		
	}


	/**
	 * This prints out the boundary file. This file gives a line number where the
	 * last instance of a context with a given relationship is found.
	 * 
	 * @param boundaryWriter
	 * @param feature
	 * @param line
	 * @throws IOException
	 */
	protected void printBoundary(BufferedWriter boundaryWriter, String feature, int line) throws IOException {
		String[] parts = feature.split(":");
		if(parts.length >= 2 && !prevFeatureType.equals(parts[1])){
			if(line != 0){
				boundaryWriter.write(line+"\n");
			}
			boundaryWriter.write(parts[1]+" := ");
			boundaryWriter.flush();
			prevFeatureType = parts[1];
		}
	}


	/**
	 * Creates a map for each word to a list of features that the word appears in.
	 * The word must appear at least as frequently as the minimum set in the 
	 * constructor. 
	 */
	public void generateRowMap() {
		try{
			String outFile = fullDirectory + "/" + matrixName + ".rlabel";
			BufferedWriter rowLabelWriter = new BufferedWriter(new FileWriter(outFile));
			LOGGER.info("Building: " + outFile);
			ArrayList<String> rows = new ArrayList<String>(word2Index.keySet());
		    Collections.sort(rows);
			rowSort2orig = new int[rows.size()];
			//rowOrig2sort = new int[rows.size()];
			//for(int i = 0; i < rowOrig2sort.length; i++){
			//	rowOrig2sort[i] = -1;
			//}
			for(int i = 0; i < rows.size(); i++){
				int wordID = word2Index.get(rows.get(i));
				//if(wordCount.get(wordID) >= minRows){
				if(uniqueWordCount.get(wordID).size() >= minRows){
					rowSort2orig[i] = wordID;
					//rowOrig2sort[wordID] = i;
					rowLabelWriter.write(rows.get(i) + "\n");
					rowCount++;
					entryCount += getCount(pairCounter.get(wordID));
				}
				else{
					rowSort2orig[i] = -1;
				}
			}
			rowLabelWriter.close();
		}
		catch(Exception e){
	    	 LOGGER.warning(e.getMessage());
		}
		LOGGER.info(rowCount + " : " + columnCount + " : " + entryCount);
		
	}


	/**
	 * Counts the the number of columns in a Map that will not
	 * be be used in the final matrix.
	 * 
	 * @param row
	 * @return
	 */
	protected int getCount(Map<Integer, Integer> row) {
		int count = 0;
		for(int key : row.keySet()){
			if(columnOrig2sort[key] != -1){
				count++;
			}
		}
		return count;
	}


	/**
	 * This builds a map of all the features to the words in which those
	 * features are found. Only features that appear greater than the 
	 * assigned number of times will be kept.
	 */
	public void generateColumnMap() {
		try{
			String outFile = fullDirectory + "/" + matrixName + ".clabel";
			BufferedWriter columnLabelWriter = new BufferedWriter(new FileWriter(outFile));
			//System.out.println("Building: " + outFile);
			LOGGER.info("Building: " + outFile);
			ArrayList<String> columns = new ArrayList<String>(context2Index.keySet());
		    Collections.sort(columns);
			columnSort2orig = new int[contextCounter];
			columnOrig2sort = new int[contextCounter];
			for(int i = 0; i < columnOrig2sort.length; i++){
				columnOrig2sort[i] = -1;
				columnSort2orig[i] = -1;
			}
			int goodWordCount = 0;
			for(int i = 0; i < columns.size(); i++){
				int contextID = context2Index.get(columns.get(i));
				//if(contextCount.get(contextID) >= minColumns){
				if(uniqueContextCount.get(contextID).size() >= minColumns){
					columnSort2orig[goodWordCount] = contextID;
					columnOrig2sort[contextID] = goodWordCount;
					columnLabelWriter.write(columns.get(i) + "\n");
					columnCount++;
					goodWordCount++;
				}
				else{
					columnSort2orig[i] = -1;
				}
			}
			columnLabelWriter.close();
		}
		catch(Exception e){
	    	 LOGGER.warning(e.getMessage());
		}
	}


	/**
	 * Creates a new directory where the matrix is to be created.
	 * Returns true if directory is created successfully, false otherwise.
	 * A directory that already exists cannot be built again, this is a safety
	 * feature to stop me from deleting my own work.
	 * 
	 * @param directory
	 * @param matrixName
	 * @return
	 */
	public boolean createDirectory(String directory, String matrixName) {
		boolean success = false;
		fullDirectory = directory + "/" + matrixName;
		try{
			success = new File(fullDirectory).mkdir();
		    if (success) {
		      LOGGER.info("Directory: " + fullDirectory + " created");
		    }  
		    else{
		      LOGGER.warning("Directory: " + fullDirectory + " could not be created");
		    } 
		}
		catch(Exception e){
	    	 LOGGER.warning(e.getMessage());
		}
	    return success;
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
		         
				for ( ; ; ) {
					String line = parseReader.readLine();
		
					if (line == null) {
						parseReader.close();
						break;
					}
					else {
						String[] parts = line.split("\\t");
						if(parts.length == 3){
							//parts[0] = parts[0].toLowerCase();
							//parts[2] = parts[2].toLowerCase();
							if(parts[1].startsWith(pos+":") && parts[0].matches("^[a-z]+$") && parts[2].matches("^[a-zA-Z]+$")){
							//if(parts[1].startsWith(POS+":") && parts[0].matches("^[a-zA-Z _-]+$") && parts[2].matches("^[a-zA-Z _-]+$")){ 
								String word = parts[0];
								String context = parts[1] + ":" + parts[2];
								int wordID = getWord(word);
								int contextID = getContext(context);
								countPair(wordID, contextID);
							}
							if(parts[1].endsWith(":"+pos) && parts[0].matches("^[a-zA-Z]+$") && parts[2].matches("^[a-z]+$")){
							//if(parts[1].endsWith(":"+POS) && parts[0].matches("^[a-zA-Z _-]+$") && parts[2].matches("^[a-zA-Z _-]+$")){
								String word = parts[2];
								String[] bits = parts[1].split(":");
								String context = bits[0]+":"+bits[1]+"-R:"+ bits[2] + ":" + parts[0];
								int wordID = getWord(word);
								int contextID = getContext(context);
								countPair(wordID, contextID);
							}
						}
					}
					
				}
		
			} catch (Exception e) {
		    	 LOGGER.warning(e.getMessage());
			}
			LOGGER.info(wordCounter + " : " + contextCounter + " : " + nonZeroEnties);
		}
	}
	

	/**
	 * Records a new appearance of the word context pair. This method also
	 * counts the number of contexts in which it appears.
	 * 
	 * @param word
	 * @param context
	 */
	protected void countPair(int word, int context) {
		int wordCt = 1;
		wordCt += wordCount.get(word);
		wordCount.set(word, wordCt);
		uniqueWordCount.get(word).add(context);
		
		int contextCt = 1;
		contextCt += contextCount.get(context);
		contextCount.set(context, contextCt);
		uniqueContextCount.get(context).add(word);
		
		if(pairCounter.size() == word){
			Map<Integer, Integer> mapper = new TreeMap<Integer, Integer>();
			mapper.put(context, 1);
			
			pairCounter.add(mapper);
			nonZeroEnties++;
		}
		else if(pairCounter.size() > word){
			Map<Integer, Integer> mapper = pairCounter.get(word);
			int value = 1;
			if(mapper.containsKey(context)){
				value += mapper.get(context);
			}
			else{
				nonZeroEnties++;
			}
			mapper.put(context, value);
			pairCounter.set(word, mapper);
		}
		else{
			LOGGER.severe("Impossible word value being counted!");
		}
	}

	/**
	 * Returns a word's ID if one has been assigned, otherwise it assigns a new
	 * ID and returns it.
	 * 
	 * @param word
	 * @return
	 */
	protected int getWord(String word){
		synchronized(this){
			int wordID = -1;
			if(word2Index.containsKey(word)){
				wordID = word2Index.get(word);
			}
			else{
				word2Index.put(word, wordCounter);
				index2Word.add(word);
				wordCount.add(0);
				uniqueWordCount.add(new TreeSet<Integer>());
				wordCounter++;
				wordID = wordCounter-1;
			}
			return wordID;
		}
	}
	
	/**
	 * Gets a given context's ID or assigns a new ID and returns it.
	 * 
	 * @param context
	 * @return
	 */
	protected int getContext(String context){
		synchronized(this){
			int contextID = -1;
			if(context2Index.containsKey(context)){
				contextID = context2Index.get(context);
			}
			else{
				context2Index.put(context, contextCounter);
				index2Context.add(context);
				contextCount.add(0);
				uniqueContextCount.add(new TreeSet<Integer>());
				contextCounter++;
				contextID = contextCounter-1;
			}
			return contextID;
		}
	}
	

}
