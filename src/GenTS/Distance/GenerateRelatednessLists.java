package MatrixFormat.Distance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GenerateRelatednessLists implements Runnable {
	
	private String word;
	private LoadForCosine loader;
	private int index;
	private ArrayList<String> wordDistsList;
	private int listLength;

	/**
	 * Loads a file and calculates all the most closely semantically related words as follows:
	 * java GenerateRelatednessLists <MatrixWordsFile> <matrixRowsFile> <queryWords> <listLengths> <num Threads>
	 * 
	 * This program calculates semantic distance then prints out results once finished. It does not print
	 * results as it progresses.
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		if(args.length < 5){
			System.out.println("To Run Program: java GenerateRelatednessLists <MatrixWordsFile> <matrixRowsFile> <queryWords> <listLengths> <num Threads>");
			return;
		}
		String wordsFile = args[0];
		String matrixFile = args[1];
		
		LoadForCosine load = new LoadForCosine(wordsFile, matrixFile);
		
		String queryFile = args[2];
		
		int resultsPerQuery = Integer.parseInt(args[3]);
		int numberThreads = Integer.parseInt(args[4]);
		
		ArrayList<String> resultsList = new ArrayList<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(queryFile));
			
			int lineCount = 0;
			
			ExecutorService executor = Executors.newFixedThreadPool(numberThreads);
			
			for ( ; ; ) {
				
				String input = br.readLine();
	
				if (input == null) {
					br.close();
					break;
				}
				else{
					resultsList.add(null);

					Runnable runner = new GenerateRelatednessLists(input, load, resultsPerQuery, resultsList, lineCount);
					executor.execute(runner);
					lineCount++;
				}
			}
			try{
				while(!executor.isTerminated()){
					Thread.sleep(1000);
				}
		    } catch (InterruptedException e) {
		    	e.printStackTrace();
		    }
			
			
			for(String results : resultsList){
				System.out.println(results);
			}
			
			br.close();
		}
		
		catch (Exception e) {
	    	 e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new object for measuring semantic relatedness for a single word.
	 * 
	 * @param wrd
	 * @param l
	 * @param resultsPerQuery
	 * @param resultsList
	 * @param number
	 */
	public GenerateRelatednessLists(String wrd, LoadForCosine l, int resultsPerQuery, ArrayList<String> resultsList, int number){
		word = wrd;
		loader = l;
		listLength = resultsPerQuery;
		wordDistsList = resultsList;
		index = number;
	}

	/**
	 * Calculates the most semantically related words.
	 */
	@Override
	public void run() {
		WordDist[] wd = loader.getClosestCosine(word, listLength);
		
		wordDistsList.set(index, word + " : "+loader.wordArray2String(wd));
		
		synchronized(this){
			System.out.println(wordDistsList.get(index));
		}
	}
}
