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
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 创建索引
 */
public class AnalAndSim {
    private Analyzer analyzer;
    private Similarity similarity;
    public AnalAndSim(){
        //Analyzer analyzer = new EnglishAnalyzer();
        //Analyzer analyzer = new StandardAnalyzer();
        analyzer = new MyAnalyzer();

        // similarity=new BM25Similarity();
        // similarity=new ClassicSimilarity();
        similarity=IRUtils.getSimilarity();
        /*
             * Map<String, Analyzer> map = new HashMap<>(); map.put("Abstract", new
             * myAnalyzer(myStopSet));
             * 
             * Analyzer analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), map);
             */
    }
    public Analyzer getAnalyzer(){
        return this.analyzer;
    }
    public Similarity getSimilarity(){
        return this.similarity;
    }
}