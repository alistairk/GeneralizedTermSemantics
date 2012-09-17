package ca.uottawa.gents.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.logging.Logger;

import ca.uottawa.gents.relatedness.LoadForRelatedness;

/**
 * This program loads a matrix and calculate the nearest neighbour of a given word from a choice
 * of two possible neighbours. It appears in the following format:
 * target	neighbour	false_neighbour
 * 
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
public class PickNearestNeighbour {
	private static final Logger LOGGER = Logger.getLogger(PickNearestNeighbour.class.getName());

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
			new PickNearestNeighbour(args[0], args[1], args[2], args[3]);
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
	public PickNearestNeighbour(String labels, String matrix, String inputFile, String outputFile){
		LoadForRelatedness loader = new LoadForRelatedness(labels, matrix);
		getPairs(loader, inputFile, outputFile);
	}
	
	/**
	 * This function takes the input and output files opens them and uses
	 * the loader to find the distance between the target and both candidates
	 * for synonymy. These scores are printed out with the word triple in a 
	 * file.
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
					Float realNeighbour = loader.distance(parts[0], parts[1]);
					if(realNeighbour < 0){
						realNeighbour = 0.0f;
					}

					Float falseNeighbour = loader.distance(parts[0], parts[2]);
					if(falseNeighbour < 0){
						falseNeighbour = 0.0f;
					}
					
					outputWriter.write(parts[0] + "\t" + parts[1] + "\t" + parts[2] + "\t" + realNeighbour + "\t" + falseNeighbour + "\n");
				}
			}
		}
		catch(Exception e){
	    	 LOGGER.warning(e.getMessage());
		}
	}
}
