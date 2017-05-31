package com.esrisy.LuceneDemo;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.classification.ClassificationResult;
import org.apache.lucene.classification.SimpleNaiveBayesClassifier;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class ClassficationData {
    
    private String indexPath;
    
    public ClassficationData(String indexPath) {
        this.indexPath = indexPath;
    }

    public void process() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        
        IKAnalyzer analyzer = new IKAnalyzer(true);
        
        Term term = new Term("newsContent", "朴槿惠");
        Query query = new TermQuery(term);
        
        SimpleNaiveBayesClassifier simpleNBClassifier
            = new SimpleNaiveBayesClassifier(reader, analyzer, query, "title", "newsContent");
        ClassificationResult result = simpleNBClassifier.assignClass("朴槿惠");
        BytesRef br = (BytesRef)result.getAssignedClass();
        System.out.println(new String(br.bytes, "utf-8"));
    } 
}
