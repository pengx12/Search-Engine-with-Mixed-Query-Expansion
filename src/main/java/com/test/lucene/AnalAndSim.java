package com.test.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

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

        similarity=new BM25Similarity();
        // similarity=new ClassicSimilarity();
        // similarity=IRUtils.getSimilarity();
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