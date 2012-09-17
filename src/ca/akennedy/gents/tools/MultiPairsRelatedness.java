package ca.akennedy.gents.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Logger;

import ca.akennedy.gents.relatedness.LoadForRelatedness;

/**
 * This program loads a matrix and calculate the semantic relatedness between word pairs. This program
 * is intended to be used with Rubenstein and Goodenough style data sets in the format of:
 * word1	word2	distance
 * The words and assigned distance scores are tab separated. For example:
 * 
 * cord    smile   0.02
 * noon    string  0.04
 * rooster voyage  0.04
 * fruit   furnace 0.05
 * autograph       shore   0.06
 * automobile      wizard  0.11
 * 
 * The scores can be human assigned, or some dummy score can be inserted if it is unknown.
 * 
 * The program is run by passing a rlabel file, the matrix_crs file an input file in the style above and
 * the name of an output file to print the results to.
 * 
 * java MultiPairsRelatedness <path to rlabel file> <path to matrix_crs.mat file> <inputFile> <outputFile>
 * 
 * The output file will be identical to the input file, but with a fourth column, the automatically generated
 * relatedness scores.
 * 
 * @author akennedy
 *
 */
public class MultiPairsRelatedness {
	private static final Logger LOGGER = Logger.getLogger(MultiPairsRelatedness.class.getName());

	/**
	 * Takes the arguments and creates a new MultiPairsRelatedness object.
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		if(args.length < 4){
			LOGGER.info("To Run Program: java MultiPairsRelatedness <path to rlabel file> <path to matrix_crs.mat file> <inputFile> <outputFile>");
		}
		else{
			new MultiPairsRelatedness(args[0], args[1], args[2], args[3]);
		}
	}
	
	/**
	 * Constructor is passed the arguments and creates a new LoadForRelatedness class.
	 * 
	 * @param labels
	 * @param matrix
	 * @param inputFile
	 * @param outputFile
	 */
	public MultiPairsRelatedness(String labels, String matrix, String inputFile, String outputFile){
		LoadForRelatedness loader = new LoadForRelatedness(labels, matrix);
		getPairs(loader, inputFile, outputFile);
	}
	
	/**
	 * This function takes the input and output files opens them and uses
	 * the loader to measure the distance between the word pairs.
	 * 
	 * @param loader
	 * @param inFile
	 * @param outputFile
	 */
	private void getPairs(LoadForRelatedness loader, String inFile, String outputFile) {
		try {
			BufferedReader inputReader = new BufferedReader(new FileReader(inFile));
			BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));
			for ( ; ; ) {
				String line = inputReader.readLine();
	
				if (line == null) {
					inputReader.close();
					outputWriter.close();
					break;
				}
				else {
					String[] parts = line.split("\t");
					Float relatedness = loader.distance(parts[0], parts[1]);
					if(relatedness < 0){
						relatedness = 0.0f;
					}
					outputWriter.write(parts[0] + "\t" + parts[1] + "\t" + parts[2] + "\t" + relatedness + "\n");
				}
			}
		}
		catch(Exception e){
	    	 LOGGER.warning(e.getMessage());
		}
	}
}
