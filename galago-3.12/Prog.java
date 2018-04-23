import org.lemurproject.galago.utility.ByteUtil;
import org.lemurproject.galago.core.index.stats.FieldStatistics;
import org.lemurproject.galago.core.index.stats.IndexPartStatistics;
import org.lemurproject.galago.core.index.stats.NodeStatistics;

import org.lemurproject.galago.core.retrieval.iterator.LengthsIterator;
import org.lemurproject.galago.core.index.corpus.CorpusReader;
import org.lemurproject.galago.core.index.corpus.DocumentReader;
import org.lemurproject.galago.core.parse.Document.DocumentComponents;

import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.core.tokenize.Tokenizer;
import org.lemurproject.galago.core.util.WordLists;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.core.retrieval.iterator.CountIterator;
import org.lemurproject.galago.core.index.KeyIterator;
import org.lemurproject.galago.core.index.IndexPartReader;
import org.lemurproject.galago.core.index.disk.DiskIndex;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;

import org.lemurproject.galago.tupleflow.Utility;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.*;
import java.util.Iterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.Math;

public class Prog {


	public static void main (String[] args) throws Exception {

  	Retrieval retrieval = RetrievalFactory.instance("./wiki-small-index", Parameters.create());
        Node n = new Node();
    	n.setOperator("lengths");
    	n.getNodeParameters().set("part", "lengths");

    	FieldStatistics stat = retrieval.getCollectionStatistics(n);

	//Part 3A
    	double avgLength = stat.avgLength;
    	long documentCount = stat.documentCount;
		

	System.out.println("");
	System.out.println("Number of Documents : " +  documentCount);
        System.out.println("Average length : " + avgLength);
	
		Retrieval r = RetrievalFactory.instance("./wiki-small-index", Parameters.create());

            	String query = "an example query";
        	Node node = StructuredQuery.parse(query);
        	node.getNodeParameters().set("queryType", "count");
        	node = r.transformQuery(node, Parameters.create());
	
	 long maxLength = stat.maxLength; // longest doc length
	System.out.println("Longest doc length : " + maxLength);
	
//Used documentation from https://sourceforge.net/p/lemur/wiki/Galago:%20Extracting%20Statistics%20from%20the%20Index/ to extract statistics

String pathIndex = "./wiki-small-index/";

String field = "text";
String term = "probabilistic";


File pathPosting = new File( new File( pathIndex ), "postings" );
DiskIndex index = new DiskIndex( pathIndex );
IndexPartReader posting = DiskIndex.openIndexPart( pathPosting.getAbsolutePath() );


        KeyIterator iter = posting.getIterator();
        int line = 0;
        while (!iter.isDone()) {
        	line++;
		iter.nextKey();
    
	}   

        System.out.println("Number of unique words in corpus : " + line);


System.out.println("");

//Used https://github.com/jjfiv/galago-git for java implementations of galago command lines, such as dump-keys, dump-corpus, and dump-lengths

int frequency =  getFrequency("probabilistic", posting, index );
System.out.println("Number of documents containing the word probabilistic : " + frequency);

int frequency2 =  getFrequency("analysis", posting, index );
System.out.println("Number of documents containing the word analysis : " + frequency2);

int frequency3 =  getFrequency("decision", posting, index );
System.out.println("Number of documents containing the word decision : " + frequency3);

int frequency4 =  getFrequency("model", posting, index );
System.out.println("Number of documents containing the word model : " + frequency4);


System.out.println("");
longestDocs(maxLength);


System.out.println("");
int calc1 = getMaxOccur("probabilistic", "model", index, posting);

System.out.println("");
int calc2 = getMaxOccur("entropy", "science", index, posting);

System.out.println("");

double idf = getIDF( frequency);
System.out.println("IDF for probabilistic : " + idf );

double idf2 = getIDF( frequency2);
System.out.println("IDF for analysis : " + idf2 );

double idf3 = getIDF( frequency3);
System.out.println("IDF for decision : " + idf3 );

double idf4 = getIDF( frequency4);
System.out.println("IDF for model : " + idf4 );


posting.close();
index.close();
} //end of main

private static int getFrequency(String term, IndexPartReader posting, DiskIndex index) throws IOException {

int totalfreq = 0;	
int docs = 0;
KeyIterator vocabulary = posting.getIterator();

if ( vocabulary.skipToKey( ByteUtil.fromString( term ) ) && term.equals( vocabulary.getKeyString() ) ) { 
    CountIterator iterator = (CountIterator) vocabulary.getValueIterator();
    ScoringContext sc = new ScoringContext();
    
	while ( !iterator.isDone() ) { 
        sc.document = iterator.currentCandidate();
        int freq = iterator.count( sc ); 
        totalfreq+= freq;
	docs++;
	String docno = index.getName( sc.document ); 
	iterator.movePast( iterator.currentCandidate() ); 
	
	}   
}

return docs;
} //end of getFrequency method

private static int longestDocs(long maxLength) throws Exception {


DiskIndex index = new DiskIndex("./wiki-small-index");
LengthsIterator lengthsItr = index.getLengthsIterator();

    ScoringContext sc = new ScoringContext();
	System.out.println("DOCID|	Length");
    while (!lengthsItr.isDone()) {
      long docId = lengthsItr.currentCandidate();
      sc.document = docId;
      lengthsItr.syncTo(docId);
      int docLen = lengthsItr.length(sc);
      if (docLen == maxLength) {
		System.out.println(docId + "\t" + docLen);
      }
	lengthsItr.movePast(docId);
    }

System.out.println("Length of longest document : " + maxLength);
return 0;
} //end of longestDocs method

private static int getMaxOccur(String word1, String word2, DiskIndex index, IndexPartReader posting) throws Exception {
int numOfDocs =  0; 

LengthsIterator lengthsItr = index.getLengthsIterator();

int maxoccurrences = 0;
ArrayList<Document> maxID = new ArrayList<Document>(); 


    ScoringContext sc = new ScoringContext();
    while (!lengthsItr.isDone()) {
      long docId = lengthsItr.currentCandidate();
      sc.document = docId;
      lengthsItr.syncTo(docId);
        Document doc = index.getDocument(index.getName(docId), DocumentComponents.All);
	int occurrences = 0;

	List<String> words = doc.terms;
	for ( String term : words ) {
		if( term.equals(word1) || term.equals(word2) )
			occurrences++;
	} 
	
	if (occurrences > maxoccurrences) {
		maxoccurrences = occurrences;
		maxID.clear();
	}

	if (occurrences == maxoccurrences) {
		maxID.add(doc);
	}
	lengthsItr.movePast(docId);
	}

System.out.println("DOCID|	Total occurrences of the words " + word1 + " , " + word2);
for ( Document d : maxID) {
	numOfDocs++;
	System.out.println(d.identifier +"	" + maxoccurrences + "	");
	System.out.println("The TF is: " + getTF(maxoccurrences, d.terms.size()  ));
	
	System.out.println ("Okapi is " + getOkapi(d, word1, word2, posting, index));
 	
	System.out.println("The IDF is : " + getIDF(maxID.size() ) );
	System.out.println("The TF-IDF is : " + ( getTF(maxoccurrences, d.terms.size()) *getIDF(maxID.size() ) )  );
}


return numOfDocs;
} //end of getMaxOccur method



private static double getIDF( int frequency) throws Exception {



        Retrieval retrieval = RetrievalFactory.instance("./wiki-small-index", Parameters.create());
        Node n = new Node();
        n.setOperator("lengths");
        n.getNodeParameters().set("part", "lengths");

        FieldStatistics stat = retrieval.getCollectionStatistics(n);

        long documentCount = stat.documentCount;
//documentCount = 6043;



double idf = Math.log(documentCount/ (double)frequency);

//System.out.println("docCount " + documentCount);
//System.out.println("freq " + (double)frequency);

//double idf = Math.log(6043 / 4);

//CAST TO DOUBLE!!!!

//System.out.println(Math.log(documentCount / frequency));

//idf = Math.log(1096.6332);

//idf = Math.log(1096.63315843);

//idf = Math.log(Math.exp(7));
return idf;
}


private static double getTF(long maxoccurrences, int length) throws Exception {
double tf = maxoccurrences / (double) length;
//double tf = maxoccurrences;
return tf;
}

private static double getOkapi( Document doc, String word1, String word2, IndexPartReader posting, DiskIndex index) throws Exception {

        Retrieval retrieval = RetrievalFactory.instance("./wiki-small-index", Parameters.create());
        Node n = new Node();
        n.setOperator("lengths");
        n.getNodeParameters().set("part", "lengths");

FieldStatistics stat = retrieval.getCollectionStatistics(n);
        double avgLength = stat.avgLength;
	double documentCount = stat.documentCount;

double occurWord1 = 0;
double occurWord2 = 0;

for (String term : doc.terms ) {
                if (term.equals(word1) )
                        occurWord1++;
                if (term.equals(word2) )
                        occurWord2++;
}

int length = doc.terms.size();


//over here
//occurWord1 = occurWord1/ length;
//occurWord2 = occurWord2 / length;

double numerator = occurWord1 * 2.2;
double seg = (length * 0.75) / avgLength; 
double denominator = occurWord1 + 1.2 * ( 1 - 0.75 + seg); 

double numerator2 = occurWord2 * 2.2;
double seg2 = (length * 0.75) / avgLength; 
double denominator2 = occurWord2 + 1.2 * ( 1 - 0.75 + seg2); 

double x1 =  numerator/denominator;
double x2 = numerator2/denominator2;
	
	double word1num = documentCount - getFrequency(word1, posting, index) + 0.5;
	double word2num = documentCount - getFrequency(word2, posting, index) + 0.5;
	double word1den = getFrequency(word1, posting, index) + 0.5;
	double word2den = getFrequency(word2, posting, index) + 0.5;

double idf1 = word1num/word1den;
double idf2 = word2num/word2den;

//double idf1 = word1num;
//double idf2 = word2num;

double okapi1 = Math.log(idf1) * x1;
double okapi2 = Math.log(idf2) * x2;

return okapi1 + okapi2;	

}

} //end of Prog class
