package com.esrisy.LuceneDemo;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class BooleanQueryData {
	
	private String indexPath;
	
	public BooleanQueryData(String indexPath) {
		this.indexPath = indexPath;
	}
	
	public void query() throws IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Term term = new Term("newsContent", "朴槿惠");
    	Query terQuery = new TermQuery(term);
    	
    	TermRangeQuery termRangeQuery = TermRangeQuery.newStringRange("qPublishdate", "20161101", "20161130", true, true);
    	
    	BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
    	booleanQueryBuilder.add(terQuery, BooleanClause.Occur.MUST);
    	booleanQueryBuilder.add(termRangeQuery, BooleanClause.Occur.MUST);
    	
    	TopDocs results = searcher.search(booleanQueryBuilder.build(), 100);
    	
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
