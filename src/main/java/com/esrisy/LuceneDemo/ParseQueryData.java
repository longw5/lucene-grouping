package com.esrisy.LuceneDemo;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class ParseQueryData {
	
	private String indexPath;
	
	public ParseQueryData(String indexPath) {
		this.indexPath = indexPath;
	}
	
	public void query(String fieldName, String key) throws IOException, ParseException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		
		IndexSearcher searcher = new IndexSearcher(reader);
    	IKAnalyzer analyzer = new IKAnalyzer(true);
    	
    	QueryParser parser = new QueryParser(fieldName, analyzer);
    	
    	Query query = parser.parse(key);
    	
    	TopDocs results = searcher.search(query, 100);
    	
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
