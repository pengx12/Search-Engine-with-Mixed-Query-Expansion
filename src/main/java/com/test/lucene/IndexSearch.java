package com.test.lucene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.AfterEffectB;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BasicModelIn;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.NormalizationH1;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

/**
 * 搜索
 */
public class IndexSearch {
    public static String[] QUREY_FIELDS = { "title", "text" };
    static IRUtils irutils = new IRUtils();

    public static void main(final String[] args) {
        // 索引存放的位置
        Directory directory = null;
        AnalAndSim anals=new AnalAndSim();
        // new IndexCreate().writeIndex();
        try {
            // 索引硬盘存储路径
            directory = FSDirectory.open(Paths.get("index"));
            // 读取索引
            final DirectoryReader directoryReader = DirectoryReader.open(directory);
            // 创建索引检索对象
            final IndexSearcher searcher = new IndexSearcher(directoryReader);
            // 分词技术
            searcher.setSimilarity(anals.getSimilarity());
            Analyzer analyzer = anals.getAnalyzer();
            ArrayList<QueryResult> allResults = querysearch(searcher, analyzer, directoryReader);
            IRUtils.WriteResults(allResults, "res");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<QueryResult> querysearch(IndexSearcher searcher, Analyzer analyzer,
            DirectoryReader directoryReader) {
        Map<String, Float> bstparameter = new HashMap<String, Float>();
        bstparameter.put("title", 5f);
        bstparameter.put("text", 10f);
        QueryParser parser = new MultiFieldQueryParser(QUREY_FIELDS, analyzer, bstparameter);
        // QueryParser parser = new MultiFieldQueryParser(QUREY_FIELDS , analyzer);
        // QueryParser parser = new QueryParser("content", analyzer);
        int counter = 1;
        final ArrayList<QueryResult> allResults = new ArrayList<QueryResult>();
        ArrayList<ArrayList<String>> qryarr;
        try {
            qryarr = IRUtils.getArrQueries();
            for (int j=0;j<qryarr.size();j++){
                Query titlequery = parser.parse(QueryParser.escape(qryarr.get(j).get(0)));
                Query descquery = parser.parse(QueryParser.escape(qryarr.get(j).get(1)));
                Query relquery=null;
                if (qryarr.get(j).get(2)!=null && qryarr.get(j).get(2).length()!=0)
                {
                    relquery = parser.parse(QueryParser.escape(qryarr.get(j).get(2)));
                }
                Query negquery=null;
                if (qryarr.get(j).get(3)!=null && qryarr.get(j).get(3).length()!=0)
                {
                    negquery = parser.parse(QueryParser.escape(qryarr.get(j).get(3)));
                }
                Query mustquery=null;
                if (qryarr.get(j).get(4)!=null && qryarr.get(j).get(4).length()!=0)
                {
                    negquery = parser.parse(QueryParser.escape(qryarr.get(j).get(4)));
                }
                BooleanQuery.Builder query = new BooleanQuery.Builder();
                query.add(new BoostQuery(titlequery, 7f), BooleanClause.Occur.SHOULD);
                query.add(new BoostQuery(descquery, 7f), BooleanClause.Occur.SHOULD);
                if (relquery!=null)
                {
                    query.add(new BoostQuery(relquery, 10f), BooleanClause.Occur.SHOULD);
                }
                // if (negquery!=null)
                // {
                //     query.add(new BoostQuery(negquery, 1f), BooleanClause.Occur.MUST_NOT);
                // }
                if (mustquery!=null)
                {
                    query.add(new BoostQuery(mustquery, 25f), BooleanClause.Occur.SHOULD);
                }
                final TopDocs results = searcher.search(query.build(), 1000);
                final ScoreDoc[] hits = results.scoreDocs;

                final int num = (int) Math.min(results.totalHits, 1000);
                for (int i = 0; i < num; i++) {
                    final int indexDocNo = hits[i].doc;
                    final Document value = directoryReader.document(indexDocNo);
                    //final String content = value.get(QUREY_FIELDS[0]);
                    final String content = value.get("docno");
                    final QueryResult qrs = new QueryResult(counter,qryarr.get(j).get(5), content, i+1, hits[i].score);
                    allResults.add(qrs);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return allResults;
    }
}