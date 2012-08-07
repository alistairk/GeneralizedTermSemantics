package ca.uottawa.gents;
import java.util.logging.Logger;

import ca.uottawa.gents.relatedness.LoadForRelatedness;


public class GentsExample {
	private static final Logger LOGGER = Logger.getLogger(GentsExample.class.getName());

	/**
	 * This runs several test cases for this class.
	 * 
	 * @param args
	 */
	public static void main(String args[]){
		if(args.length < 2){
			LOGGER.info("To Run Program: java GentsExample <path to rlabel file> <path to matrix_crs.mat file>");
		}
		else{
			new GentsExample(args[0], args[1]);
		}
	}
	
	public GentsExample(String labels, String matrix){
		LoadForRelatedness loader = new LoadForRelatedness(labels, matrix);
		
		calculateDistances(loader);
	}
	
	public void calculateDistances(LoadForRelatedness loader){
		LOGGER.info("Distance between \"boy\" and \"boy\": " + loader.distance("boy", "boy"));
		LOGGER.info("Distance between \"boy\" and \"girl\": " + loader.distance("boy", "girl"));
		LOGGER.info("Distance between \"girl\" and \"boy\": " + loader.distance("girl", "boy"));

		LOGGER.info("List of the top 10 closest words to \"geometry\":");
		LOGGER.info(loader.getWordArrayString(loader.getClosestWords("geometry", 10)));
	}
	
}
