package com.esrisy.LuceneDemo;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class RangeQueryData {
	
	private String indexPath;
	
	public RangeQueryData(String indexPath) {
		this.indexPath = indexPath;
	}
	
	public void query(String fieldName, String start, String end) throws IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		
		IndexSearcher searcher = new IndexSearcher(reader);
		
		TermRangeQuery termRangeQuery = TermRangeQuery.newStringRange(fieldName, start, end, true, true);
		
		TopDocs results = searcher.search(termRangeQuery, 100);
		ScoreDoc[] hits = results.scoreDocs;
    	int numTotalHits = results.totalHits;
    	System.out.println(numTotalHits + " total matching documents");
    	for (int i = 0; i < numTotalHits; i++) {
	    	System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
	    	
	    	Document doc = searcher.doc(hits[i].doc);
	    	String title = doc.get("title");
	    	System.out.println("   title: " + title);
    	}
	}

}
