This is the README file for the Java package Generalized TermSemantics (GenTS) v0.2
This file contains instructions for building, reweighting and using a term-context matrix for measuring
semantic relatedness between pairs of terms. Its main functionality is to determine a similarity score
between word pairs and to generate lists of the closest related words to a given target word.

This package includes the following
GenTS.jar	-- a jar file containing executables of GenTS
README		-- this readme
COPYING		-- the license
src/		-- the source code for GenTS.jar
data/		-- sample data including a sample matrix, training data and sample parsed text
data/sampleMatrix_n/	-- sample matrix and its files
data/sampleParsed.txt	-- sample of Minipar parsed data
data/trainingData/	-- sample training data for nouns, verbs and adjectives.

Further explanation as to what this package does and some of the experiments conducted with it can be found 
in the following paper. To cite this work please reference:

Alistair Kennedy, Stan Szpakowicz (2010). "Supervised Distributional Semantic Relatedness". 
To appear in the Proceedings of the 15th International Conference on Text, Speech and Dialogue 
TSD 2012, Brno, Czech Republic.

For more information please contact:
Alistair Kennedy
akennedy@eecs.uottawa.ca


Building and using a term-context matrix:

Step 0

Parse some text with a dependency parser, an example of such a file can be seen in: data/sampleParsed.txt
This parse was done using Minipar, however any dependency parse can be used provided the output is altered
to be of the same format.

Step 1

This step builds a matrix. Separate matrices can be built from nouns (N), verbs (V) or adjectives (A). 
An output directory must be specified and the name of the matrix is also needed. A new directory will be
created using the name of the matrix. The minimum term and feature (context) frequency is also requested.
It is recommend to use 35 and 2 for nouns and adjectives and 10 and 2 for verbs. Next a list of parsed 
files must be listed.

The matrix files are nearly identical in design to those produced by the SuperMatrix package.

To Run Program: java BuildMatrix <N|V|A> <output Directory> <output File Name> <min Term Frequency> <min Context Frequency> <parsedFile 1> ... <parsedFile n>
example command and output:
	$ java -cp GenTS.jar:. ca.uottawa.gents.buildmatrix.BuildMatrix N . newMatrix 35 2 data/sampleParsed.txt 
	Jul 7, 2012 7:26:31 PM ca.uottawa.gents.buildmatrix.BuildMatrix createDirectory
	INFO: Directory: ./newMatrix created
	Jul 7, 2012 7:26:31 PM ca.uottawa.gents.buildmatrix.BuildMatrix loadFile
	INFO: Loading: data/sampleParsed.txt
	Jul 7, 2012 7:26:38 PM ca.uottawa.gents.buildmatrix.BuildMatrix loadFile
	INFO: 14036 : 52728 : 270615
	Jul 7, 2012 7:26:38 PM ca.uottawa.gents.buildmatrix.BuildMatrix generateColumnMap
	INFO: Building: ./newMatrix/newMatrix.clabel
	Jul 7, 2012 7:26:38 PM ca.uottawa.gents.buildmatrix.BuildMatrix generateRowMap
	INFO: Building: ./newMatrix/newMatrix.rlabel
	Jul 7, 2012 7:26:38 PM ca.uottawa.gents.buildmatrix.BuildMatrix generateRowMap
	INFO: 2328 : 23321 : 179944
	Jul 7, 2012 7:26:38 PM ca.uottawa.gents.buildmatrix.BuildMatrix generateCRS
	INFO: Building: ./newMatrix/matrix_crs.mat
	Jul 7, 2012 7:26:38 PM ca.uottawa.gents.buildmatrix.BuildMatrix generateCRS
	INFO: Processed 10000 rows
	Jul 7, 2012 7:26:39 PM ca.uottawa.gents.buildmatrix.BuildMatrix generateCCS
	INFO: Building: ./newMatrix/matrix_ccs.mat
	Jul 7, 2012 7:26:39 PM ca.uottawa.gents.buildmatrix.BuildMatrix generateCCS
	INFO: Written 10000 columns
	Jul 7, 2012 7:26:40 PM ca.uottawa.gents.buildmatrix.BuildMatrix generateCCS
	INFO: Written 20000 columns

The following files will be produced:
newMatrix/boundary.txt		-- boundaries between features types (syntactic relations)
newMatrix/info.txt		-- information about the size of the matrix
newMatrix/newMatrix.rlabel	-- lists the words in the matrix
newMatrix/column_features.csv	-- lists column features along with counts and entropy information
newMatrix/matrix_ccs.mat	-- a sparse matrix which lists non-zero rows for each column
newMatrix/matrix_crs.mat	-- a sparse matrix which lists non-zero columns for each row
newMatrix/newMatrix.clabel	-- lists the contexts in the matrix
newMatrix/row_features.csv	-- lists row features along with counts and entropy information


Step 2

Reweight the matrix with either the unsupervised, context-supervised or relation-supervised system. One of six measures of association can
be used in re-weighting the matrix, those are Pointwise Mutual Information (PMI), Log Likelihood (LL), Dice, T-score, Z-score and Chi-squared (Chi2).
Our experiments found PMI to be the best for re-weighting matrices. Re-weighting can be done in the standard unsupervised manner or using training
data to provide supervision. These methods will create new row and column matrix files with the re-weighted matrix

Choose one of these three:

unsupervised:
To Run Program: java WeightFeaturesUnsupervised <PMI|LL|F|Tscore|Zscore|Chi2> <rlabel file> <row matrix file> <column matrix file>
example command and output:
	$ java -cp GenTS.jar:. ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised PMI newMatrix/newMatrix.rlabel newMatrix/matrix_crs.mat newMatrix/matrix_ccs.mat 
	Jul 7, 2012 7:27:27 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised main
	INFO: Association measure: PMI
	Jul 7, 2012 7:27:27 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised loadRows
	INFO: words: 2328
	Jul 7, 2012 7:27:27 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised loadColumnFeatures
	INFO: weight: 381624.0
	Jul 7, 2012 7:27:29 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised rowsToColumns
	INFO: Rows read: 1000
	Jul 7, 2012 7:27:29 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised rowsToColumns
	INFO: Rows read: 2000
	
This creates a new matrix with the suffix "u-PMI":
newMatrix/matrix_ccs.mat.u-PMI	
newMatrix/matrix_crs.mat.u-PMI


context-supervised:

This program will make use of supervised training data for example, see the training data files in: data/trainingData/
There should be one for each of three parts of speech.

To Run Program: java WeightFeaturesContextSupervised <PMI|LL|Dice|Tscore|Zscore|Chi2> <training data> <row_features.csv file> <row matrix file> <column matrix file>
example command and output:
	$ java -cp GenTS.jar:. ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised Dice data/trainingData/nouns.txt newMatrix/row_features.csv newMatrix/matrix_crs.mat newMatrix/matrix_ccs.mat
	Jul 7, 2012 7:27:42 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadRows
	INFO: Unique training words: 1978
	Jul 7, 2012 7:27:42 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadRows
	INFO: Total words: 2328
	Jul 7, 2012 7:27:42 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadRows
	INFO: Occurrences of training words: 318382
	Jul 7, 2012 7:27:42 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised colleceRelatedPairs
	INFO: Pairs: 0
	Jul 7, 2012 7:27:43 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised colleceRelatedPairs
	INFO: Pairs: 1000
	Jul 7, 2012 7:27:43 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised colleceRelatedPairs
	INFO: Pairs: 2000
	Jul 7, 2012 7:27:43 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised colleceRelatedPairs
	INFO: Unique Pairs: 1955253
	Jul 7, 2012 7:27:43 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 1000
	Jul 7, 2012 7:27:43 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 2000
	Jul 7, 2012 7:27:43 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 3000
	Jul 7, 2012 7:27:43 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 4000
	Jul 7, 2012 7:27:43 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 5000
	Jul 7, 2012 7:27:45 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 6000
	Jul 7, 2012 7:27:45 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 7000
	Jul 7, 2012 7:27:45 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 8000
	Jul 7, 2012 7:27:46 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 9000
	Jul 7, 2012 7:27:47 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 10000
	Jul 7, 2012 7:27:47 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 11000
	Jul 7, 2012 7:27:47 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 12000
	Jul 7, 2012 7:27:47 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 13000
	Jul 7, 2012 7:27:47 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 14000
	Jul 7, 2012 7:27:47 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 15000
	Jul 7, 2012 7:27:48 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 16000
	Jul 7, 2012 7:27:48 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 17000
	Jul 7, 2012 7:27:48 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 18000
	Jul 7, 2012 7:27:50 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 19000
	Jul 7, 2012 7:27:50 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 20000
	Jul 7, 2012 7:27:51 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 21000
	Jul 7, 2012 7:27:51 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 22000
	Jul 7, 2012 7:27:51 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features processed: 23000
	Jul 7, 2012 7:27:51 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Features included: 15305
	Jul 7, 2012 7:27:51 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadColumnFeatures
	INFO: Average score: 6.873022157179207E-4
	Jul 7, 2012 7:27:53 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised rowsToColumns
	INFO: Rows read: 1000
	Jul 7, 2012 7:27:53 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised rowsToColumns
	INFO: Rows read: 2000

This creates a new matrix with the suffix "c-Dice":
newMatrix/matrix_ccs.mat.c-Dice		-- re-weighted spearse column oriented matrix
newMatrix/matrix_crs.mat.c-Dice		-- re-weighted sparse row oriented matrix
columns_context_Dice.txt		-- list of new column weights


relation-supervised:

This runs much like context-supervised but requires the boundary.txt file to be included when running.

java WeightFeaturesRelationSupervised <PMI|LL|Dice|Tscore|Zscore|Chi2> <training data> <row_features.csv file> <row matrix file> <column matrix file> <column boundary file>
example command and output:
	$ java -cp GenTS.jar:. ca.uottawa.gents.weightmatrix.WeightFeaturesRelationSupervised LL data/trainingData/nouns.txt newMatrix/row_features.csv newMatrix/matrix_crs.mat newMatrix/matrix_ccs.mat newMatrix/boundary.txt 
	Jul 7, 2012 7:30:15 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadRows
	INFO: Unique training words: 1978
	Jul 7, 2012 7:30:15 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadRows
	INFO: Total words: 2328
	Jul 7, 2012 7:30:15 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised loadRows
	INFO: Occurrences of training words: 318382
	Jul 7, 2012 7:30:15 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised colleceRelatedPairs
	INFO: Pairs: 0
	Jul 7, 2012 7:30:16 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised colleceRelatedPairs
	INFO: Pairs: 1000
	Jul 7, 2012 7:30:16 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised colleceRelatedPairs
	INFO: Pairs: 2000
	Jul 7, 2012 7:30:16 PM ca.uottawa.gents.weightmatrix.WeightFeaturesContextSupervised colleceRelatedPairs
	INFO: Unique Pairs: 1955253
	Jul 7, 2012 7:30:16 PM ca.uottawa.gents.weightmatrix.WeightFeaturesRelationSupervised loadColumnFeatures
	INFO:  : 0 0
	Jul 7, 2012 7:30:16 PM ca.uottawa.gents.weightmatrix.WeightFeaturesRelationSupervised loadColumnFeatures
	INFO: amount-value-R : 0 1
	...
	...
	...
	Jul 7, 2012 7:30:23 PM ca.uottawa.gents.weightmatrix.WeightFeaturesRelationSupervised loadColumnFeatures
	INFO: pred-R : 23313 23314
	Jul 7, 2012 7:30:24 PM ca.uottawa.gents.weightmatrix.WeightFeaturesRelationSupervised loadColumnFeatures
	INFO: s-R : 23314 23315
	Jul 7, 2012 7:30:24 PM ca.uottawa.gents.weightmatrix.WeightFeaturesRelationSupervised loadColumnFeatures
	INFO: Features included: 15305
	Jul 7, 2012 7:30:24 PM ca.uottawa.gents.weightmatrix.WeightFeaturesRelationSupervised loadColumnFeatures
	INFO: Average score: 3.045732792391912
	Jul 7, 2012 7:30:25 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised rowsToColumns
	INFO: Rows read: 1000
	Jul 7, 2012 7:30:25 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised rowsToColumns
	INFO: Rows read: 2000

This creates a new matrix with the suffix "r-LL":
newMatrix/matrix_ccs.mat.r-LL		-- re-weighted spearse column oriented matrix
newMatrix/matrix_crs.mat.r-LL		-- re-weighted sparse row oriented matrix
newMatrix/columns_relation_LL.txt	-- list of new column weights


Step 3 (optional)

If you used supervised Matrix reweighting then you can run the unsupervised re-weighting on top of it.
This was found to create the best results.

example command and output:
	$ java -cp GenTS.jar:. ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised Tscore newMatrix/newMatrix.rlabel newMatrix/matrix_crs.mat.c-Dice newMatrix/matrix_ccs.mat.c-Dice 
	Jul 7, 2012 7:33:18 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised main
	INFO: Association measure: Tscore
	Jul 7, 2012 7:33:18 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised loadRows
	INFO: words: 2328
	Jul 7, 2012 7:33:18 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised loadColumnFeatures
	INFO: weight: 2770159.342211558
	Jul 7, 2012 7:33:19 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised rowsToColumns
	INFO: Rows read: 1000
	Jul 7, 2012 7:33:19 PM ca.uottawa.gents.weightmatrix.WeightFeaturesUnsupervised rowsToColumns
	INFO: Rows read: 2000

creates a new matrix with the suffix "c-Dice.u-Tscore"
newMatrix/matrix_ccs.mat.c-Dice.u-Tscore	-- re-weighted spearse column oriented matrix
newMatrix/matrix_crs.mat.c-Dice.u-Tscore	-- re-weighted sparse row oriented matrix


Step 4

The next step is to actually use the matrix. For the most part you may want to write your own code
to run experiments. Accessing the matrix is done through the class LoadForRelatedness. First 
instantiate the class passing it the path to the rlable file and the matrix_crs.mat file of your choice.
Then use either the distance or getClosestWords functions.

String rlab = "newMatrix/newMatrix.rlabel";
String mat = "newMatrix/matrix_crs.mat.c-Dice.u-Tscore";

LoadForRelatedness loader = new LoadForRelatedness(rlab, mat);

float dist= loader.distance("cat", "dog");

System.out.println("Distance from cat to dog: " + dist);

WordDist[] neighbours = loader.getClosestWords("monkey", 10);
loader.printWordArray(neighbours);

If one word in a word pair is not found then it receives a score of -1.

To see an example of this running you can run the LoadForRelatedness class:

To Run Program: java LoadForCosine <path to rlabel file> <path to matrix_crs.mat file>
example command and output:
	$ java -cp GenTS.jar:. ca.uottawa.gents.GentsExample newMatrix/newMatrix.rlabel newMatrix/matrix_crs.mat.c-Dice.u-Tscore
	Jul 7, 2012 7:34:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: Distance between "boy" and "boy": 1.0000001
	Jul 7, 2012 7:34:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: Distance between "boy" and "girl": 0.4097439
	Jul 7, 2012 7:34:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: Distance between "girl" and "boy": 0.4097439
	Jul 7, 2012 7:34:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: List of the top 10 closest words to "geometry":
	Jul 7, 2012 7:34:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: anthropology (0.74332416), alchemy (0.71357536), aikido (0.6964221), art (0.6943861), communism (0.6789571), cinema (0.678228), protein (0.6683792), folklore (0.6606066), ambergris (0.65492034), antimony (0.64818305)

 another example command and output:
	$ java -cp GenTS.jar:. ca.uottawa.gents.GentsExample newMatrix/newMatrix.rlabel newMatrix/matrix_crs.mat
	Jul 7, 2012 7:35:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: Distance between "boy" and "boy": 1.0
	Jul 7, 2012 7:35:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: Distance between "boy" and "girl": 0.48241818
	Jul 7, 2012 7:35:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: Distance between "girl" and "boy": 0.48241818
	Jul 7, 2012 7:35:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: List of the top 10 closest words to "geometry":
	Jul 7, 2012 7:35:57 PM ca.uottawa.gents.GentsExample <init>
	INFO: alchemy (0.65953034), agriculture (0.6533324), chemistry (0.6336132), philosophy (0.6304994), choice (0.62729865), art (0.6239595), vision (0.623698), light (0.62046444), violence (0.6185485), humanity (0.6164602)

