package com.test.lucene;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.test.lucene.qexpansion.ExpandQuery;
import com.test.lucene.qexpansion.ScorePair;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
// import org.apache.lucene.search.DiversifiedTopDocsCollector;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 搜索
 */
public class IndexSearch {
    public static String[] QUREY_FIELDS = { "title", "text" };
    static IRUtils irutils = new IRUtils();
    static DirectoryReader directoryReader;
    // 创建索引检索对象
    static IndexSearcher searcher;
    public static void main(final String[] args) {
        // 索引存放的位置
        Directory directory = null;
        AnalAndSim anals = new AnalAndSim();
        // IndexCreate ic = new IndexCreate();
        // // ic.writeIndex();
        // ic.writeIndex(IRUtils.absPathFedRegister, "DOCNO", "DOCTITLE", "TEXT");
        // ic.writeIndex(IRUtils.absPathFT, "DOCNO", "HEADLINE", "TEXT");
        // ic.writeIndex(IRUtils.absPathFB, "DOCNO", "TI", "TEXT");
        // ic.writeIndex(IRUtils.absPathLA, "DOCNO", "HEADLINE", "TEXT");
        try {
            // 索引硬盘存储路径
            directory = FSDirectory.open(Paths.get("index"));
            // 读取索引
            directoryReader = DirectoryReader.open(directory);
            // 创建索引检索对象
            searcher = new IndexSearcher(directoryReader);
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

        QueryParser symparser = new MultiFieldQueryParser(QUREY_FIELDS, new symanalyzer(), bstparameter);
        try {
            qryarr = irutils.getArrQueries();
            for (int j = 0; j < qryarr.size(); j++) {
                BooleanQuery.Builder query = getquery(parser, qryarr, j,symparser);
                TopDocs results = searcher.search(query.build(), 1000);
                ScoreDoc[] hits = results.scoreDocs;
                final int num = (int) Math.min(results.totalHits, 1000);
                // Set idset=new HashSet();
                for (int i = 0; i < num; i++) {
                    final int indexDocNo = hits[i].doc;
                    final Document value = directoryReader.document(indexDocNo);
                    // final String content = value.get(QUREY_FIELDS[0]);
                    final String content = value.get("docno");
                    // idset.add(content);
                    final QueryResult qrs = new QueryResult(counter, qryarr.get(j).get(5), content, i + 1,
                            hits[i].score);
                    allResults.add(qrs);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return allResults;
    }

    private static BooleanQuery.Builder getquery(QueryParserBase parser, ArrayList<ArrayList<String>> qryarr, int j,
            QueryParser symparser)
            throws ParseException {
        Query titlequery = parser.parse(QueryParser.escape(qryarr.get(j).get(0)));
        Query descquery = parser.parse(QueryParser.escape(qryarr.get(j).get(1)));
        Query relquery = null;
        if (qryarr.get(j).get(2) != null && qryarr.get(j).get(2).length() != 0) {
            relquery = parser.parse(QueryParser.escape(qryarr.get(j).get(2)));
        }
        Query mustquery = null;
        if (qryarr.get(j).get(4) != null && qryarr.get(j).get(4).length() != 0) {
            mustquery = parser.parse(QueryParser.escape(qryarr.get(j).get(4)));
        }
        // Query countryquery = null;
        Query countrysymquery= null;
        if (qryarr.get(j).get(6) != null && qryarr.get(j).get(6).length() != 0) {
            countrysymquery = symparser.parse(QueryParser.escape(qryarr.get(j).get(6)));
            // countryquery = parser.parse(QueryParser.escape(qryarr.get(j).get(6)));
        }
        Query simtitlequery = symparser.parse(QueryParser.escape(qryarr.get(j).get(0)));
        
        
        BooleanQuery.Builder query = new BooleanQuery.Builder();
        query.add(new BoostQuery(titlequery, 3f), BooleanClause.Occur.SHOULD);
        query.add(new BoostQuery(descquery, 5f), BooleanClause.Occur.SHOULD);
        query.add(new BoostQuery(simtitlequery, 8f), BooleanClause.Occur.SHOULD);
        if (relquery != null) {
            query.add(new BoostQuery(relquery, 5f), BooleanClause.Occur.SHOULD);
        }
        // if (negquery!=null)
        // {
        // query.add(new BoostQuery(negquery, 1f), BooleanClause.Occur.MUST_NOT);
        // }
        if (mustquery != null) {
            query.add(new BoostQuery(mustquery, 10f), BooleanClause.Occur.SHOULD);
            // Query simmustquery = symparser.parse(QueryParser.escape(qryarr.get(j).get(4)));
            // query.add(new BoostQuery(simmustquery, 2f), BooleanClause.Occur.SHOULD);
        }
        if (countrysymquery != null) {
            // query.add(new BoostQuery(countryquery, 4f), BooleanClause.Occur.SHOULD);
            query.add(new BoostQuery(countrysymquery, 6f), BooleanClause.Occur.SHOULD);
        }  
        ExpandQuery eq=new ExpandQuery();
		try {
            List<ScorePair> eqarr = eq.expandQtfidf(searcher, directoryReader, query);
            for (int i = 0; i < eqarr.size(); i++) {
                ScorePair pair = eqarr.get(i);
                if (pair.docString.length()<3){
                    continue;
                }
                Query eQuery = parser.parse(QueryParser.escape(pair.docString));
                query.add(new BoostQuery(eQuery, (float) pair.score), BooleanClause.Occur.SHOULD);


                eqarr = eq.expandQ(searcher, directoryReader, query);
                for ( i = 0; i < eqarr.size(); i++) {
                     pair = eqarr.get(i);
                    if (pair.docString.length()<3){
                        continue;
                    }
                    eQuery = parser.parse(QueryParser.escape(pair.docString));
                    query.add(new BoostQuery(eQuery, (float) pair.getBoost()), BooleanClause.Occur.SHOULD);
                    }
                
		}
    } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
        return query;
    }





}