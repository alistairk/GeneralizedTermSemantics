/*
 * 
 *    Copyright (C) 2009
 *    Mario Jarmasz, Alistair Kennedy and Stan Szpakowicz
 *    School of Information Technology and Engineering (SITE)
 *    University of Ottawa, 800 King Edward Avenue
 *    Ottawa, Ontario, Canada, K1N 6N5
 *    
 *    and
 *    
 *    Olena Medelyan
 *    Department of Computer Science,
 *    The University of Waikato
 *    Private Bag 3105, Hamilton, New Zealand
 *    
 *    This file is part of Open Roget's Thesaurus ELKB.
 *    
 *    Copyright (c) 2009, Mario Jarmasz, Alistair Kennedy, Stan Szpakowicz and Olena Medelyan
 *    All rights reserved.
 *    
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *        * Neither the name of the University of Ottawa nor the
 *          names of its contributors may be used to endorse or promote products
 *          derived from this software without specific prior written permission.  
 *        * All advertising materials mentioning features or use of this software
 *          must display the following acknowledgement:
 *          This product includes software developed by the University of
 *          Ottawa and its contributors.
 *    
 *    THIS SOFTWARE IS PROVIDED BY Mario Jarmasz, Alistair Kennedy, Stan Szpakowicz 
 *    and Olena Medelyan ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 *    BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 *    A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Mario Jarmasz, Alistair 
 *    Kennedy, Stan Szpakowicz and Olena Medelyan BE LIABLE FOR ANY DIRECT, 
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 *    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY 
 *    OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *    EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *     
 */

package ca.akennedy.gents.weightmatrix;

/**
 * This interface is for distance functions, originally from Roget's SemDist. The three
 * methods are to compute the distance between two words, the distance between two words 
 * of the same known POS and the distance between two words of two different known POSs.
 * @author akennedy
 *
 */
public interface Distance {

	int getSimilarity(String word1, String word2);
	int getSimilarity(String word1, String word2, String pos);
	int getSimilarity(String word1, String pos1, String word2, String pos2);
	
}
