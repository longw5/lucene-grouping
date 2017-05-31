package com.esrisy.LuceneDemo;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


public class GroupQueryData {

	private String indexPath;
	
	public GroupQueryData(String indexPath) {
		this.indexPath = indexPath;
	}
	
	public void query(String groupFieldName, String queryField, String key) throws IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
		IndexSearcher searcher = new IndexSearcher(reader);

		GroupingSearch groupingSearch = new GroupingSearch(groupFieldName);
		groupingSearch.setGroupSort(new Sort(SortField.FIELD_SCORE));
	    groupingSearch.setFillSortFields(true);
	    groupingSearch.setCachingInMB(4.0, true);
	    groupingSearch.setAllGroups(true);
	    groupingSearch.setGroupDocsLimit(10);
	    
	    Term term = new Term(queryField, key);
    	Query query = new TermQuery(term);
	    TopGroups<BytesRef> result = groupingSearch.search(searcher, query, 0, 1000);
	    
	    System.out.println("搜索命中数：" + result.totalHitCount);
	    
	    GroupDocs<BytesRef>[] docs = result.groups;
	    for (GroupDocs<BytesRef> groupDocs : docs) {
            System.out.println("group:" + new String(groupDocs.groupValue.bytes));
            System.out.println(groupDocs.totalHits);
            for (ScoreDoc scoreDoc: groupDocs.scoreDocs) {
            	Document doc = searcher.doc(scoreDoc.doc);
            	System.out.println(doc.get("title"));
            }
        }
	}
}
