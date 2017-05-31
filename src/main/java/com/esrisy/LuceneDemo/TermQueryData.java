package com.esrisy.LuceneDemo;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class TermQueryData {
	
	private String indexPath;

	public TermQueryData(String indexPath) {
		this.indexPath = indexPath;
	}
	
	public void query(String fieldName, String key) throws IOException, InvalidTokenOffsetsException {
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		
		IndexSearcher searcher = new IndexSearcher(reader);
		IKAnalyzer analyzer = new IKAnalyzer(true);
    	
    	Term term = new Term(fieldName, key);
    	Query query = new TermQuery(term);
    	
    	TopDocs results = searcher.search(query, 10000);
    	
    	ScoreDoc[] hits = results.scoreDocs;
    	int numTotalHits = results.totalHits;
    	System.out.println(numTotalHits + " total matching documents");
    	System.out.println(hits.length + " total matching doc");
    	
    	if (numTotalHits > 1000) {
    	    numTotalHits = 1000;
    	}
    	
    	SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>");
    	Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));
    	
    	for (int i = 0; i < hits.length; i++) {
	    	System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
	    	
	    	Document doc = searcher.doc(hits[i].doc);
	    	String title = doc.get("title");
	    	System.out.println("   title: " + title + ", data: " + doc.get("qPublishdate"));
	    	
	    	TokenStream tokenStream = analyzer.tokenStream("newsContent", new StringReader(doc.get("newsContent")));
	    	String content = highlighter.getBestFragment(tokenStream, doc.get("newsContent"));
	    	System.out.println(content.split("<span style='color:red'>").length-1);
    	}
	}
}
