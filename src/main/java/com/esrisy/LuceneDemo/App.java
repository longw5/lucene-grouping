package com.esrisy.LuceneDemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

/**
 * Hello world!
 *
 */
public class App {
	
    static final String indexPath = "<path of index>";
    static final String docPath = "<path of index>";
    static final String dataPath = "<testData of index>";
	
    public static void main( String[] args ) throws Exception {
        //OtherDemoCode other = new OtherDemoCode();
        //other.demo2();
    	//myIndexDocs();
    	
    	//IndexData indexData = new IndexData(indexPath, docPath);
    	//indexData.indexData(dataPath);
    	
    	//ParseQueryData parseQueryData = new ParseQueryData(indexPath);
    	//parseQueryData.query("newsContent", "金正恩");
    	
    	TermQueryData termQueryData = new TermQueryData(indexPath);
    	termQueryData.query("newsContent", "朴槿惠");
    	
    	//RangeQueryData rangeQueryData = new RangeQueryData(indexPath);
    	//rangeQueryData.query("qPublishdate", "20161101", "20161130");
    	
    	//BooleanQueryData booleanQueryData = new BooleanQueryData(indexPath);
    	//booleanQueryData.query();
    	
    	//GroupQueryData groupQueryData = new GroupQueryData(indexPath);
    	//groupQueryData.query("qPublishDataMonth", "newsContent", "朴槿惠");
    	
    	//groupQueryData.query("qNewsSource", "newsContent", "朴槿惠");
        
        //ClassficationData classficationData = new ClassficationData(indexPath);
        //classficationData.process();
        
        //ClassifyNewsGroups cng = new ClassifyNewsGroups();
        //cng.buildIndex();
        
        //ClassifyNewsGroups cng = new ClassifyNewsGroups();
        //cng.testIndex();
        
        //IndexYbuData indexYbuData = new IndexYbuData(indexPath, docPath);
        //indexYbuData.indexData(dataPath);
    }

    static void myIndexDocs() {

        Path docDir = Paths.get(docPath);
        if (!Files.isReadable(docDir)) {
        	System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
        	System.exit(1);
        }
        
        try {
        	Directory dir = FSDirectory.open(Paths.get(indexPath));
        	IKAnalyzer analyzer = new IKAnalyzer(true);
        	IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        	
        	iwc.setOpenMode(OpenMode.CREATE);
        	
        	IndexWriter writer = new IndexWriter(dir, iwc);
        	//indexDocs(writer, docDir);
        	
        	Document doc = new Document();
        	
        	doc.add(new StringField("id", "1", Field.Store.YES));
        	doc.add(new StringField("content", "王五好吃懒做，溜须拍马，跟着李四，也过着小康的日子 诛仙 林光远", Field.Store.YES));
        	doc.add(new TextField("contents", new StringReader("王五好吃懒做，溜须拍马，跟着李四，也过着小康的日子 诛仙 林光远")));
        	writer.addDocument(doc);
        	
        	writer.close();
        } catch (IOException e) {
        	System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }
    
    static void indexDocs(final IndexWriter writer, Path path) throws IOException {
    	if (Files.isDirectory(path)) {
    		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
    			@Override
    			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    				try {
    					indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
    				} catch (IOException ignore) {
    					
    				}
    				return FileVisitResult.CONTINUE;
    			}
    		});
    	} else {
    		indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
    	}
    }
    
    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
    	try (InputStream stream = Files.newInputStream(file)) {
    		Document doc = new Document();
    		
    		Field pathField = new StringField("path", file.toString(), Field.Store.YES);
    		doc.add(pathField);
    		
    		doc.add(new LongPoint("modified", lastModified));
    		
    		doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
    		
    		if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
    			System.out.println("adding " + file);
    			writer.addDocument(doc);
    		} else {
    			System.out.println("updating " + file);
    			writer.updateDocument(new Term("path", file.toString()), doc);
    		}
    	}
    }
}
