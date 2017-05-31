package com.esrisy.LuceneDemo;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class ClassifyNewsGroups {

    public static final String ENCODING = "UTF-8";
    
    public static final String[] NEWSGROUPS = {
            "alt.atheism",
            "comp.graphics",
            "comp.os.ms-windows.misc",
            "comp.sys.ibm.pc.hardware",
            "comp.sys.mac.hardware",
            "comp.windows.x",
            "misc.forsale",
            "rec.autos",
            "rec.motorcycles",
            "rec.sport.baseball",
            "rec.sport.hockey",
            "sci.crypt",
            "sci.electronics",
            "sci.med",
            "sci.space",
            "soc.religion.christian",
            "talk.politics.guns",
            "talk.politics.mideast",
            "talk.politics.misc",
            "talk.religion.misc"
    };
    
    private String mType;
    
    private Analyzer analyzer;
    
    private String indexPath = "C:/Users/qiudong/Documents/esri/study_arcmap/docRoot";
    private String trainPath = "C:/Users/qiudong/Documents/esri/study_arcmap/doc_data/20news-bydate-train";
    private String testPath  = "C:/Users/qiudong/Documents/esri/study_arcmap/doc_data/20news-bydate-test";
    
    private File trainDir;
    private File indexDir;
    private File testDir;
    
    public ClassifyNewsGroups() {
        mType = "std";
        
        analyzer = new StandardAnalyzer();
        
        trainDir = new File(trainPath);
        indexDir = new File(indexPath);
        testDir = new File(testPath);
    }
    
    public void buildIndex() throws IOException {
        
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        
        IndexWriter writer = new IndexWriter(dir, iwc);
        
        File[] groupsDir = trainDir.listFiles();
        for (File group : groupsDir) {
            int postCt = 0;
            String groupName = group.getName();
            
            File[] posts = group.listFiles();
            for (File postFile : posts) {
                String number = postFile.getName();
                
                NewsPost post = parse(postFile, groupName, number);
                
                Document d = new Document();
                d.add(new StringField("category", post.group(), Store.YES));
                d.add(new TextField("text", post.subject(), Store.NO));
                d.add(new TextField("text", post.body(), Store.NO));
                
                writer.addDocument(d);
                postCt++;
            }
            System.out.println("training items for " + groupName + ": " + postCt);
        }
        int numDocs = writer.numDocs();
        writer.forceMerge(1);
        writer.commit();
        writer.close();
        System.out.println("index=" + indexDir.getName());
        System.out.println("num docs=" + numDocs);
    }
    
    public NewsPost parse(File inFile, String newsgroup, String number) throws IOException, FileNotFoundException {
        FileInputStream inStream = null;
        InputStreamReader inReader = null;
        BufferedReader bufReader = null;
        
        try {
            inStream = new FileInputStream(inFile);
            inReader = new InputStreamReader(inStream, ENCODING);
            bufReader = new BufferedReader(inReader);
            
            String subject = null;
            
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                // parse out subject from Subject line
                if (line.startsWith("Subject:")) {
                    subject = line.substring("Subject:".length());
                } else {
                    if (line.length() == 0) break;
                }
            }
            
            if (line == null) 
                throw new IOException("unexpected EOF");
            
            StringBuilder body = new StringBuilder();
            int lines = 1;
            while ((line = bufReader.readLine()) != null) {
                body.append(line + " ");
                lines++;
            }
            return new NewsPost(newsgroup,number,subject,body.toString());
        } finally {
            close(bufReader);
            close(inReader);
            close(inStream);
        }
        
    }
    
    public void close(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException e) {
            // ignore
        }
    }
    
    public void testIndex() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);
        
        IndexSearcher searcher = new IndexSearcher(reader);
        int[][] confusionMatrix = new int[NEWSGROUPS.length][NEWSGROUPS.length];
        
        File[] groupsDir = testDir.listFiles();
        for (File group : groupsDir) {
            int postCt = 0;
            String groupName = group.getName();
            int rowIdx = Arrays.binarySearch(NEWSGROUPS,groupName);
            
            File[] posts = group.listFiles();
            for (File postFile : posts) {
                postCt++;
                String number = postFile.getName();
                NewsPost post = parse(postFile, groupName, number);
                
                BooleanQuery termsQuery = buildQuery(post.subject() + " " + post.body());
                TopDocs hits = searcher.search(termsQuery,1);
                ScoreDoc[] scoreDocs = hits.scoreDocs;
                for (int n = 0; n < scoreDocs.length; ++n) {
                    ScoreDoc sd = scoreDocs[n];
                    int docId = sd.doc;
                    Document d = searcher.doc(docId);
                    String category = d.get("category");
                    
                    int colIdx = Arrays.binarySearch(NEWSGROUPS,category);
                    confusionMatrix[rowIdx][colIdx]++;
                }
            }
            System.out.print(groupName);
            for (int i=0; i<NEWSGROUPS.length; i++) 
                System.out.printf("| %4d ", confusionMatrix[rowIdx][i]);
            System.out.println("|");
        }
    }
    
    public BooleanQuery buildQuery(String text) throws IOException {
        //BooleanQuery termsQuery = new BooleanQuery();
        BooleanQuery.Builder termsQuery = new BooleanQuery.Builder();
        
        Reader textReader = new StringReader(text);
        TokenStream tokStream = analyzer.tokenStream("text", textReader);
        
        try {
            tokStream.reset();
            CharTermAttribute terms = tokStream.addAttribute(CharTermAttribute.class);
            int ct = 0;
            
            while (tokStream.incrementToken() && ct++ < 1024) {
                termsQuery.add(new TermQuery(new Term("text", terms.toString())), Occur.SHOULD);
            }
            tokStream.end();
        } finally {
            tokStream.close();
            textReader.close();
        }
        
        return termsQuery.build();
    }
    
    public class NewsPost {
        private final String mGroup;
        private final String mNumber;
        private final String mSubject;
        private final String mBody;

        public NewsPost (String group, 
                         String number,
                         String subject, 
                         String body) {
            mGroup = group;
            mNumber = number;
            mSubject = subject;
            mBody = body;
        }
        public String group() { return mGroup; }
        public String number() { return mNumber; }
        public String subject() { return mSubject; }
        public String body() { return mBody; }
    }
}
