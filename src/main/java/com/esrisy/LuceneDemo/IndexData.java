package com.esrisy.LuceneDemo;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.opencsv.CSVReader;

public class IndexData {
	
	private String indexPath;
	private String docPath;
	
	public IndexData(String indexPath, String docPath) {
		this.indexPath = indexPath;
		this.docPath = docPath;
	}

	public void indexData(String dataPath) throws IOException, ParseException {
		Path docDir = Paths.get(this.docPath);
		
		if (!Files.isReadable(docDir)) {
        	System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
        	System.exit(1);
        }
		
		Directory dir = FSDirectory.open(Paths.get(indexPath));
    	IKAnalyzer analyzer = new IKAnalyzer(true);
    	IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    	
    	iwc.setOpenMode(OpenMode.CREATE);
    	
    	IndexWriter writer = new IndexWriter(dir, iwc);
    	
    	CSVReader reader = new CSVReader(new FileReader(dataPath));
    	String [] nextLine;
    	while ((nextLine = reader.readNext()) != null) {
    		String id = generateUUID();
    		String title = nextLine[0];
    		String author = nextLine[1];
    		String country = nextLine[2];
    		String newsSource = nextLine[3];
    		String publishDate = nextLine[4];
    		String newsContent = nextLine[5];
    		
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm");
    		String qPublishdate = DateTools.dateToString(sdf.parse(publishDate), Resolution.DAY);
    		System.out.println(qPublishdate);
    		String qPublishDataMonth = DateTools.dateToString(sdf.parse(publishDate), Resolution.MONTH);
    		System.out.println(qPublishDataMonth);
    		
    		Document doc = new Document();
    		
    		doc.add(new StringField("id", id, Field.Store.YES));
    		doc.add(new StringField("title", title, Field.Store.YES));
    		doc.add(new StringField("author", author, Field.Store.YES));
    		doc.add(new StringField("country", country, Field.Store.YES));
    		doc.add(new StringField("newsSource", newsSource, Field.Store.YES));
    		doc.add(new SortedDocValuesField("qNewsSource", new BytesRef(newsSource)));
    		doc.add(new StringField("publishDate", publishDate, Field.Store.YES));
    		doc.add(new StringField("qPublishdate", qPublishdate, Field.Store.YES));
    		doc.add(new StringField("qPublishDataMonth", qPublishDataMonth, Field.Store.YES));
    		doc.add(new SortedDocValuesField("qPublishDataMonth", new BytesRef(qPublishDataMonth)));
    		doc.add(new TextField("newsContent", newsContent, Field.Store.YES));
    		writer.addDocument(doc);
    		writer.flush();
    	}
    	writer.close();
	}
	
	private String generateUUID() {
		UUID uuid = UUID.randomUUID();
    	String strUUID = uuid.toString();
    	return strUUID.replaceAll("-", "");
	}
}
