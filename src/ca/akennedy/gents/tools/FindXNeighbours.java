package ca.akennedy.gents.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import ca.akennedy.gents.relatedness.LoadForRelatedness;
import ca.akennedy.gents.relatedness.WordDist;

/**
 * This program loads a matrix and finds the top X neighbours of all the words in a given file.
 * The fine contains multiple lines and multiple words on each line. The output is a triple, the line
 * number of the word, the word itself and a list of its top X neighbours.
 * 
 * @author akennedy
 *
 */
public class FindXNeighbours implements Runnable{
	private static final Logger LOGGER = Logger.getLogger(FindXNeighbours.class.getName());
	
	private static LoadForRelatedness loader;
	private static List<String> results;
	private static int topX;
	
	private final String word;
	private final int lineNumber;
	private final int idNumber;

	/**
	 * Takes the arguments and creates a new FindXNeighbours object.
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		if(args.length == 6){
			String labels = args[0];
			String matrix = args[1];
			topX = Integer.parseInt(args[2]);
			String inputFile = args[3];
			String outputFile = args[4];
			int threadCount = Integer.parseInt(args[5]);
			loader = new LoadForRelatedness(labels, matrix);
			results = new ArrayList<String>();
			getNeighbours(topX, inputFile, outputFile, threadCount);
		}
		else{
			LOGGER.info("To Run Program: java FindXNeighbours <path to rlabel file> <path to matrix_crs.mat file> <X> <inputFile> <outputFile> <thread count>");
		}
	}
	
	/**
	 * This constructor records the word, line number and id number.
	 * @param word
	 * @param lineNumber
	 * @param idNumber
	 */
	public FindXNeighbours(String word, int lineNumber, int idNumber){
		this.word = word;
		this.lineNumber = lineNumber;
		this.idNumber = idNumber;
	}
	
	/**
	 * Goes through the file of words and creates a new thread for each one finding all the
	 * nearest neighbours.
	 * 
	 * @param topX
	 * @param inFile
	 * @param outputFile
	 * @param threadCount
	 */
	private static void getNeighbours(int topX, String inFile, String outputFile, int threadCount) {
		try {
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);
			
			BufferedReader inputReader = new BufferedReader(new FileReader(inFile));
			int lineNumber = 0;
			
			int counter = 0;
			for ( ; ; ) {
				lineNumber++;
				String line = inputReader.readLine();
	
				if (line == null) {
					inputReader.close();
					break;
				}
				else {
					String[] parts = line.split("\\s+");
					for(String word : parts){
						results.add("");
						Runnable runner = new FindXNeighbours(word, lineNumber, counter);
						executor.execute(runner);
						counter++;
					}
				}
			}
			executor.shutdown();
			while(!executor.isTerminated()){
				Thread.sleep(1000);
			}
			

			BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));
			for(String result : results){
				outputWriter.write(result);
			}
			outputWriter.close();
			
		}
		catch(Exception e){
	    	 LOGGER.warning(e.getMessage());
		}
	}

	/**
	 * Finds nearest neighbours and places them into the results arrayList.
	 */
	@Override
	public void run() {
		WordDist[] words = loader.getClosestWords(word, topX);
		String neighbours = loader.getWordArrayString(words);
		synchronized(results){
			String output = lineNumber + "\t" + word + "\t" + neighbours + "\n";
			results.set(idNumber, output);
			LOGGER.info("Finished thread: " + idNumber);
		}
	}
}
