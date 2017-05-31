package com.esrisy.LuceneDemo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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

import oracle.jdbc.pool.OracleDataSource;

public class IndexYbuData {
    
    static final String dbUrl = "<Oracle connection url>";
    
    private String indexPath;
    private String docPath;
    
    public IndexYbuData(String indexPath, String docPath) {
        this.indexPath = indexPath;
        this.docPath = docPath;
    }
    
    public void indexData(String dataPath) throws SQLException, IOException, ParseException {
        
        String sql = "SELECT A.NEWSID, A.TITLE, A.AUTHOR, B.NAME AS COUNTRY, A.NEWSSOURCE, A.PUBLISHDATE, A.NEWSCONTENT";
        sql += " FROM YBU_EQ_GIVESOURCENEWS A, YBU_DICTIONARY B";
        sql += " WHERE A.COUNTRY = B.CODE AND PUBLISHPLACE = '中国北京'";
        sql += " and NEWSID is not null and TITLE is not null and AUTHOR is not null and";
        sql += " country is not null and NEWSSOURCE is not null";
        sql += " and PUBLISHDATE is not null and NEWSCONTENT is not null";
        
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
        
        OracleDataSource ods = new OracleDataSource();
        ods.setURL(dbUrl);
        Connection conn = ods.getConnection();
        
        DatabaseMetaData meta = conn.getMetaData();
        System.out.println("JDBC driver version is " + meta.getDriverVersion());
        
        Statement stmt = conn.createStatement();
        
        ResultSet rset = stmt.executeQuery(sql);
        
        int indexCount = 0;
        while (rset.next()) {
            String id = rset.getString(1);
            String title = rset.getString(2);
            String author = rset.getString(3);
            String country = rset.getString(4);
            String newsSource = rset.getString(5);
            String publishDate = rset.getString(6);
            Clob clob = rset.getClob(7);
            String newsContent = clob.getSubString(1, (int)clob.length());
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
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
            indexCount++;
            if (indexCount % 1000 == 0) {
                System.out.println("indexing count:" + indexCount);
            }
        }
        rset.close();
        stmt.close();
        conn.close();
        System.out.println("JDBC closed.");
        writer.close();
        System.out.println("Index completed.");
    }

}
