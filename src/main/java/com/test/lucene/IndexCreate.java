package com.test.lucene;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field;
//import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 创建索引
 */
public class IndexCreate {
    public static String CRAN_DOCS_PATH = "/cran/cran.all.1400";
    private final static String PathFedRegister = "/Docs/fr94";
    //find absolute path in classpath , src/main/java
    public static String CRAN_QUERIES_PATH = "/cran/cran.qry";
    private static IRUtils irutils=new IRUtils();
	public static String CRAN_DOC_PATTERN = "\n.[A-Z]";
	public static String CRAN_QUERY_PATTERN = "\\r\\n.W\\r\\n";
    private static String[] indexFields= { "id", "title", "authors", "bib", "content"};
    

    private final static Path currentRelativePath = Paths.get("").toAbsolutePath();
	private final static String absPathFedRegister = String.format("%s/Docs/fr94",currentRelativePath);
    public static void main(final String[] args) throws IOException {

        final IndexCreate ic=new IndexCreate();
        //ic.writeIndex();
        ic.writeIndex(IRUtils.absPathFedRegister,"DOCNO","DOCTITLE","TEXT");
        ic.writeIndex(IRUtils.absPathFT,"DOCNO","HEADLINE","TEXT");
        ic.writeIndex(IRUtils.absPathFB,"DOCNO","TI","TEXT");
        ic.writeIndex(IRUtils.absPathLA,"DOCNO","HEADLINE","TEXT");
    }
    public void writeIndex(String path, String id, String title, String text) {
        // Analyzer analyzer = new EnglishAnalyzer();
        //Analyzer analyzer = new StandardAnalyzer();
        List<Document> FRDoc;
        AnalAndSim anals=new AnalAndSim();
        final Analyzer analyzer = anals.getAnalyzer();
        final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

        // indexWriterConfig.setOpenMode(OpenMode.CREATE);
        indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
        indexWriterConfig.setSimilarity(anals.getSimilarity());
        Directory directory = null;
        IndexWriter indexWriter = null;
        try {
            // 索引在硬盘上的存储路径
            // FRDoc = IRUtils.loadDocs();
            directory = FSDirectory.open(Paths.get("index"));
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            FRDoc =IRUtils.loadFedRegisterDocs(path,id,title,text);
            indexWriter.addDocuments(FRDoc);
            indexWriter.commit();
            // FRDoc=IRUtils.loadFedRegisterDocs(IRUtils.absPathFT,"DOCNO","HEADLINE","TEXT");
            // indexWriter.addDocuments(FRDoc);
            // indexWriter.commit();
            // FRDoc=IRUtils.loadFedRegisterDocs(IRUtils.absPathFB,"DOCNO","TI","TEXT");
            // indexWriter.addDocuments(FRDoc);
            // indexWriter.commit();
            // FRDoc=IRUtils.loadFedRegisterDocs(IRUtils.absPathLA,"DOCNO","HEADLINE","TEXT");
            // indexWriter.addDocuments(FRDoc);
            // indexWriter.commit();
            indexWriter.close();
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}