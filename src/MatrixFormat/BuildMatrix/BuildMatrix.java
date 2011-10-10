package MatrixFormat.BuildMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class BuildMatrix {
	
	private String POS;
	private String fullDirectory;
	private String matrixName;
	
	private int min_columns;
	private int min_rows;
	
	private int wordCounter;
	private int contextCounter;
	private int nonZeroEnties;
	
	private TreeMap<String, Integer> word2Index;
	private TreeMap<String, Integer> context2Index;
	
	private ArrayList<String> index2Word;
	private ArrayList<String> index2Context;
	
	private ArrayList<Integer> wordCount; //index is word ID
	private ArrayList<Integer> contextCount; //index is context ID
	private ArrayList<TreeMap<Integer, Integer>> pairCounter; // ArrayList index is Word ID, TreeMap index is context ID
	
	private int[] rowSorted2original;
	private int[] columnSorted2original;
	

	private int[] rowOriginal2sorted;
	private int[] columnOriginal2sorted;
	
	private int rowCount;
	private int columnCount;
	private int entryCount;
	
	private String previousFeatureType;
	
	
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
		// TODO: check input length
		if(args.length < 6){
			System.out.println("To Run Program: java BuildMatrix <N|V|A> <output Directory> <output File Name> <min Term Frequency> <min Context Frequency> <parsedFile 1> ... <parsedFile n>");
			return;
		}
		String POS = args[0];//"N";
		String directory = args[1];//"/Users/akennedy/Research/buildMatrix/";
		String matrixName = args[2];//"finalMatrix_"+POS.toLowerCase();
		BuildMatrix bm = new BuildMatrix(matrixName, POS, Integer.parseInt(args[3]), Integer.parseInt(args[4])); //N:35, V:10, A:35
		boolean dirCreated = bm.createDirectory(directory, matrixName);
		if(!dirCreated){
			return;
		}
		for(int i = 5; i < args.length; i++){ 
			String file = args[i];
			bm.loadFile(file);
			
		}

		bm.generateColumnMap();
		bm.generateRowMap();
		
		bm.writeInfo("Matrix Info File");
		
		bm.generateCRS();
		bm.generateCCS();
		
	}


	/**
	 * Writes out the info file. This file contains a string, ideally telling
	 * what was parsed to build this, it also details the total number of
	 * words, featues and non-zero entries in the matrix as well as the number
	 * left after reducing the matrix.
	 * 
	 * @param info
	 */
	private void writeInfo(String info) {
		try{
			String infoFile = fullDirectory + "/info.txt";
			BufferedWriter bw = new BufferedWriter(new FileWriter(infoFile));
			bw.write(info + "\n");
			bw.write("Total Words: "+wordCounter+"\tTotal Contexts: "+contextCounter+"\tTotal Entries: "+nonZeroEnties+"\n");
			bw.write("Used Words: "+rowCount+"\tUsed Contexts: "+columnCount+"\tUsed Entries: "+entryCount + "\n");
			bw.flush();
			bw.close();
			//System.out.println(rowCount + " : " + columnCount + " : " + entryCount);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * The constructor, takes the name of the matrix, the part of speech
	 * and the minimum number of rows and columns. It initializes the
	 * TreeMaps and ArrayLists to store the words and contexts.
	 * 
	 * @param mn
	 * @param pos
	 * @param rowMin
	 * @param colMin
	 */
	public BuildMatrix(String mn, String pos, int rowMin, int colMin){
		matrixName = mn;
		POS = pos;
		
		word2Index = new TreeMap<String, Integer>();
		index2Word = new ArrayList<String>();
		
		context2Index = new TreeMap<String, Integer>();
		index2Context = new ArrayList<String>();
		
		wordCount = new ArrayList<Integer>();
		contextCount = new ArrayList<Integer>();
		
		pairCounter = new ArrayList<TreeMap<Integer, Integer>>();
		wordCounter = 0;
		contextCounter = 0;
		nonZeroEnties = 0;
		
		min_columns = colMin;
		min_rows = rowMin;
		
		rowCount = 0;
		columnCount = 0;
		entryCount = 0;
		
		previousFeatureType = "";
	}
	
	/**
	 * This prints out the rows files, including the matrix as well as the one counting
	 * the number of features in the row.
	 */
	public void generateCRS() {
		try{
			String outFile = fullDirectory + "/matrix_crs.mat";
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
			System.out.println("Building: " + outFile);

			String featureFile = fullDirectory + "/row_features.csv";
			BufferedWriter featureWriter = new BufferedWriter(new FileWriter(featureFile));

			featureWriter.write("label;nz;tf;TF;entropy\n");
			
			bw.write(rowCount + " " + columnCount + " " + entryCount + "\n");
			
			for(int i = 0; i < rowSorted2original.length; i++){
				int rowID = rowSorted2original[i];
				if(rowID != -1){
					TreeMap<Integer, Integer> row = pairCounter.get(rowID);
					//String rowString = "";
					ArrayList<Integer> values = getLine(row, bw);
					writeFeature(index2Word.get(rowID), featureWriter, values);
				}
				if((i+1) % 10000 == 0){
					System.out.println("Processed " + (i+1) + " rows");
				}
			}
			bw.close();
			featureWriter.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		pairCounter.clear();
		System.gc();
	}
	
	/**
	 * Gets the contents of a row. Prints out values in to the matrix and returns
	 * an arraylist of values.
	 * 
	 * @param row
	 * @param bw
	 * @return
	 * @throws IOException
	 */
	public ArrayList<Integer> getLine(TreeMap<Integer, Integer> row, BufferedWriter bw) throws IOException{
		TreeMap<Integer, Integer> mappedKeyValue = new TreeMap<Integer, Integer>();
		//String rowString = "";
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(int key : row.keySet()){
			if(columnOriginal2sorted[key] != -1){
				mappedKeyValue.put(columnOriginal2sorted[key], row.get(key));
			}
		}
		ArrayList<Integer> fields = new ArrayList<Integer>(mappedKeyValue.keySet());
		Collections.sort(fields);
		for(int i = 0; i < fields.size(); i++){
			int key = fields.get(i);
			bw.write(key + " " + mappedKeyValue.get(key));
			if(i != fields.size()-1){
				bw.write(" ");
			}
			values.add(mappedKeyValue.get(key));
		}
		bw.write("\n");
		bw.flush();
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
	private void writeFeature(String word, BufferedWriter featureWriter, ArrayList<Integer> values) throws IOException {
		int nz = values.size();
		int tf = 0;
		for(int i = 0; i < values.size(); i++){
			tf += values.get(i);
		}
		double entropy = 0.0;
		for(int i = 0; i < values.size(); i++){
			double p = values.get(i) / (double) tf;
			entropy += p * Math.log(p)/Math.log(2.0);
		}

		if(entropy < 0){
			entropy = -entropy;
		}
        featureWriter.write(word + ";" + nz + ";" + tf + ";" + tf + ";" + entropy + "\n");
        featureWriter.flush();
	}
	
	/**
	 * Generates the column files.
	 */
	public void generateCCS() {
		try {
			String inFile = fullDirectory + "/matrix_crs.mat";
			String outFile = fullDirectory + "/matrix_ccs.mat";
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
			BufferedReader br = new BufferedReader(new FileReader(inFile));
			System.out.println("Building: " + outFile);

			String featureFile = fullDirectory + "/column_features.csv";
			BufferedWriter featureWriter = new BufferedWriter(new FileWriter(featureFile));
			

			String boundaryFile = fullDirectory + "/boundary.txt";
			BufferedWriter boundaryWriter = new BufferedWriter(new FileWriter(boundaryFile));
			
			featureWriter.write("label;nz;tf;TF;entropy\n");
			
			String line = br.readLine(); // get first line
			
			bw.write(line + "\n");

			String[] firstBreak = line.split("\\s+");
			//String[] valueArray = new String[Integer.parseInt(firstBreak[1])];
			int arrayLength = Integer.parseInt(firstBreak[1]);
			@SuppressWarnings("unchecked")
			ArrayList<Integer>[] idArray = new ArrayList[arrayLength];
			@SuppressWarnings("unchecked")
			ArrayList<Integer>[] valueArray = new ArrayList[arrayLength];
			for(int i = 0; i < idArray.length; i++){
				idArray[i] = new ArrayList<Integer>();
				valueArray[i] = new ArrayList<Integer>();
			}
			int count = 0;
			
			for ( ; ; ) {
				line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
				else {
					String[] parts = line.split(" ");
					for(int i = 1; i < parts.length; i += 2){
						//valueArray[Integer.parseInt(parts[i])] += count + " " + parts[i+1]+ " ";
						idArray[Integer.parseInt(parts[i-1])].add(count);
						valueArray[Integer.parseInt(parts[i-1])].add(Integer.parseInt(parts[i]));
					}
					count++;
					if(count % 10000 == 0){
						System.out.println("Loaded " + count + " rows");
					}
				}
			}
			for(int i = 0; i < valueArray.length; i++){
				//String outputLine = "";
		        if(valueArray[i].size() > 0){
	        		ArrayList<Integer> ids = idArray[i];
	        		ArrayList<Integer> vals = valueArray[i];
		        	for(int j = 0; j < vals.size(); j++){
		        		//outputLine += ids.get(j) + " " + vals.get(j) + " ";
		        		bw.write(ids.get(j) + " " + vals.get(j));
		        		if(j != vals.size()-1){
		        			bw.write(" ");
		        		}
		        	}
		        }
		        //if(!outputLine.equals("")){
		        //	outputLine = outputLine.substring(0, outputLine.length()-1);
		        //}
		        bw.write("\n");
		        bw.flush();
		        
				writeFeature(index2Context.get(columnSorted2original[i]), featureWriter, valueArray[i]);
				printBoundary(boundaryWriter, index2Context.get(columnSorted2original[i]), i);
		        
				if((i+1) % 10000 == 0){
					System.out.println("Written " + (i+1) + " columns");
				}
			}
			int lastBoundary = valueArray.length -1;
			boundaryWriter.write(lastBoundary + "\n");
			bw.close();
			featureWriter.close();
			boundaryWriter.close();
		} 
		catch (Exception e) {
	    	 e.printStackTrace();
		}
		
	}


	/**
	 * This prints out the boundary file. This file gives a line number where the
	 * last instance of a context with a given relationship is found.
	 * @param boundaryWriter
	 * @param feature
	 * @param line
	 * @throws IOException
	 */
	private void printBoundary(BufferedWriter boundaryWriter, String feature, int line) throws IOException {
		String[] parts = feature.split(":");
		if(!previousFeatureType.equals(parts[1])){
			if(line != 0){
				boundaryWriter.write(line+"\n");
			}
			boundaryWriter.write(parts[1]+" := ");
			boundaryWriter.flush();
			previousFeatureType = parts[1];
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
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
			System.out.println("Building: " + outFile);
			ArrayList<String> rows = new ArrayList<String>(word2Index.keySet());
		    Collections.sort(rows);
			rowSorted2original = new int[rows.size()];
			rowOriginal2sorted = new int[rows.size()];
			for(int i = 0; i < rowOriginal2sorted.length; i++){
				rowOriginal2sorted[i] = -1;
			}
			for(int i = 0; i < rows.size(); i++){
				int id = word2Index.get(rows.get(i));
				if(wordCount.get(id) >= min_rows){
					rowSorted2original[i] = id;
					rowOriginal2sorted[id] = i;
					bw.write(rows.get(i) + "\n");
					rowCount++;
					entryCount += getCount(pairCounter.get(id));
				}
				else{
					rowSorted2original[i] = -1;
				}
			}
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(rowCount + " : " + columnCount + " : " + entryCount);
		
	}


	private int getCount(TreeMap<Integer, Integer> row) {
		int count = 0;
		for(int key : row.keySet()){
			if(columnOriginal2sorted[key] != -1){
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
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
			System.out.println("Building: " + outFile);
			ArrayList<String> columns = new ArrayList<String>(context2Index.keySet());
		    Collections.sort(columns);
			columnSorted2original = new int[contextCounter];
			columnOriginal2sorted = new int[contextCounter];
			for(int i = 0; i < columnOriginal2sorted.length; i++){
				columnOriginal2sorted[i] = -1;
				columnSorted2original[i] = -1;
			}
			int goodWordCount = 0;
			for(int i = 0; i < columns.size(); i++){
				int id = context2Index.get(columns.get(i));
				if(contextCount.get(id) >= min_columns){
					columnSorted2original[goodWordCount] = id;
					columnOriginal2sorted[id] = goodWordCount;
					bw.write(columns.get(i) + "\n");
					columnCount++;
					goodWordCount++;
				}
				else{
					columnSorted2original[i] = -1;
				}
			}
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
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
			success = (new File(fullDirectory)).mkdir();
		    if (success) {
		      System.out.println("Directory: " + fullDirectory + " created");
		    }  
		    else{
		      System.out.println("Directory: " + fullDirectory + " could not be created");
		    } 
		}
		catch(Exception e){
			e.printStackTrace();
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
		System.out.println("Loading: " + fname);
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
	         
			for ( ; ; ) {
				String line = br.readLine();
	
				if (line == null) {
					br.close();
					break;
				}
				else {
					String[] parts = line.split("\\t");
					if(parts.length == 3){
						//parts[0] = parts[0].toLowerCase();
						//parts[2] = parts[2].toLowerCase();
						//if(parts[1].startsWith(POS+":") && parts[0].matches("^[a-z]+$") && parts[2].matches("^[a-zA-Z]+$")){ 
						if(parts[1].startsWith(POS+":") && parts[0].matches("^[a-zA-Z _-]+$") && parts[2].matches("^[a-zA-Z _-]+$")){ 
							String word = parts[0];
							String context = parts[1] + ":" + parts[2];
							int wordID = getWord(word);
							int contextID = getContext(context);
							countPair(wordID, contextID);
						}
						//if(parts[1].endsWith(":"+POS) && parts[0].matches("^[a-zA-Z]+$") && parts[2].matches("^[a-z]+$")){
						if(parts[1].endsWith(":"+POS) && parts[0].matches("^[a-zA-Z _-]+$") && parts[2].matches("^[a-zA-Z _-]+$")){
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
	    	 e.printStackTrace();
		}
		System.out.println(wordCounter + " : " + contextCounter + " : " + nonZeroEnties);
	}
	

	/**
	 * Records a new appearance of the word context pair. This method also
	 * counts the number of contexts in which it appears.
	 * 
	 * @param word
	 * @param context
	 */
	private void countPair(int word, int context) {
		int wc = 1;
		wc += wordCount.get(word);
		wordCount.set(word, wc);
		
		int cc = 1;
		cc += contextCount.get(context);
		contextCount.set(context, cc);
		
		if(pairCounter.size() == word){
			TreeMap<Integer, Integer> ht = new TreeMap<Integer, Integer>();
			ht.put(context, 1);
			
			//pairCounter.add(word, ht);
			pairCounter.add(ht);
			nonZeroEnties++;
		}
		else if(pairCounter.size() > word){
			TreeMap<Integer, Integer> ht = pairCounter.get(word);
			int value = 1;
			if(ht.containsKey(context)){
				value += ht.get(context);
			}
			else{
				nonZeroEnties++;
			}
			ht.put(context, value);
			pairCounter.set(word, ht);
			//System.out.println("B " + ht.size());
		}
		else{
			System.out.println("Danger Will Robinson!");
		}
	}

	/**
	 * Returns a word's ID if one has been assigned, otherwise it assigns a new
	 * ID and returns it.
	 * 
	 * @param word
	 * @return
	 */
	private int getWord(String word){
		if(word2Index.containsKey(word)){
			return word2Index.get(word);
		}
		else{
			word2Index.put(word, wordCounter);
			index2Word.add(word);
			wordCount.add(0);
			wordCounter++;
			return wordCounter-1;
		}
	}
	
	/**
	 * Gets a given context's ID or assigns a new ID and returns it.
	 * 
	 * @param context
	 * @return
	 */
	private int getContext(String context){
		if(context2Index.containsKey(context)){
			return context2Index.get(context);
		}
		else{
			context2Index.put(context, contextCounter);
			index2Context.add(context);
			contextCount.add(0);
			contextCounter++;
			return contextCounter-1;
		}
	}
	

}
