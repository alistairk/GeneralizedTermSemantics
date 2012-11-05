package ca.akennedy.gents.weightmatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Logger;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;


public class WeightFeaturesSVD {
	private static final Logger LOGGER = Logger.getLogger(WeightFeaturesSVD.class.getName());
	
	protected RealMatrix rowMatrix;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 3){
			LOGGER.info("To Run Program: java WeightFeaturesSVD  <row matrix file> ");
		}
		else{
			String rowMatrixFile = args[0];
			
			//names of reweighted files
			String fullRowFile = rowMatrixFile+".lsa";
			String sigma = rowMatrixFile+".lsa_singVal";
			
			
			WeightFeaturesSVD wfsvd = new WeightFeaturesSVD(rowMatrixFile);
	
			
			
		}
	}
	
	public WeightFeaturesSVD(String fname){
		loadMatrix(fname);
		LOGGER.info("loaded matrix: " + fname);
		SingularValueDecomposition svd = new SingularValueDecomposition(rowMatrix);
		System.out.println(svd.getU());
	}
	
		
	protected void loadMatrix(String fname){	
		try {
			BufferedReader columnReader = new BufferedReader(new FileReader(fname));
			String line = columnReader.readLine(); // get first line
			String[] dimensions = line.split("\\s+");
			
			rowMatrix = new OpenMapRealMatrix(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]));
			int rowNumber = 0;
			
			for ( ; ; ) {
				line = columnReader.readLine();
	
				if (line == null) {
					columnReader.close();
					break;
				}
	
				else {
					double weight = 0;
					if(!"".equals(line)){
						String[] parts = line.split(" ");
						for(int i = 0; i < parts.length; i+=2){
							int columnNumber = Integer.parseInt(parts[i]);
							double value = Double.parseDouble(parts[i+1]);
							rowMatrix.addToEntry(rowNumber, columnNumber, value);
						}
					}
					rowNumber++;
				}
			}
	
		} catch (Exception e) {
	    	 LOGGER.warning(e.getMessage());
		}

	}

}
