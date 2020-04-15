package com.test.lucene.qexpansion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

public class ExpandQuery {
	HashMap<String, ScorePair> map = new HashMap<String, ScorePair>();
	public List<ScorePair> expandQtfidf(IndexSearcher searcher, DirectoryReader reader, BooleanQuery.Builder query)
	throws IOException {
			TopDocs tmpres = searcher.search(query.build(), 3);
			double beta = 0.3;
			double decay = 0.1;
			List<ScorePair> resarr = new ArrayList<ScorePair>();
			for (int i = 0; i < tmpres.scoreDocs.length; i++) {
				decay = decay * (double)(i+1);				
				Fields doctermvector1 = reader.getTermVectors(tmpres.scoreDocs[i].doc);
				Iterator<String> fields = doctermvector1.iterator();
				// Iterate over each field
				while (fields.hasNext()) {
					String fieldName = fields.next();
					Terms terms = doctermvector1.terms(fieldName);
					if (terms != null) {
						TermsEnum tEnum = terms.iterator();
						
						while (tEnum.next() != null) {
							this.putTermInMap(tEnum.term().utf8ToString(),map,tEnum.docFreq(), reader.numDocs(),(double)terms.size(),decay,beta);
						}
					}
				}
				
			}
			List<ScorePair> byScore = new ArrayList<ScorePair>(map.values());
			Collections.sort(byScore);
			for (int i = 0; i < 10; i++) {
				// Add all our found terms to the final query
				ScorePair pair = byScore.get(i);
				resarr.add(pair);
			}
			return resarr;
	}

	public List<ScorePair> expandQ(IndexSearcher searcher, DirectoryReader reader, BooleanQuery.Builder query)
			throws IOException {
		// Vector<Document> vHits = new Vector<Document>();
		TopDocs tmpres = searcher.search(query.build(), 3);
		// Vector<QueryTermVector> docsTerms = new Vector<QueryTermVector>();
		double beta = 2.0;
		double decay = 0.01;
		String fieldname="title";
		List<ScorePair> resarr = new ArrayList<ScorePair>();
		for (int i = 0; i < tmpres.scoreDocs.length; i++) {
			Document doctermvector = searcher.doc(tmpres.scoreDocs[i].doc);
			StringBuffer docTxtBuffer = new StringBuffer();
			String[] docTxtFlds = doctermvector.getValues(fieldname);
			if (docTxtFlds.length == 0)
				continue;
			for (int j = 0; j < docTxtFlds.length; j++) {
				docTxtBuffer.append(docTxtFlds[j] + " ");
			}
			// docTxtFlds = doctermvector.getValues("text");
			// if (docTxtFlds.length == 0)
			// 	continue;
			// for (int j = 0; j < docTxtFlds.length; j++) {
			// 	docTxtBuffer.append(docTxtFlds[j] + " ");
			// }
			decay = decay * (double)(i+1);
			beta=beta-decay*beta;
			System.out.println(docTxtBuffer.toString());
			System.out.println(beta);
			if (beta<0)
			{
				break;
			}
			ScorePair sp=new ScorePair(docTxtBuffer.toString(),beta);
			resarr.add(sp);
			// QueryTermVector docTerms = new QueryTermVector(docTxtBuffer.toString(), new MyAnalyzer());
			// docsTerms.add(docTerms );
			// // QueryTermVector docTerms = docsTerms.elementAt(g);
			// String[] termsTxt = docTerms.getTerms();
			// int[] termFrequencies = docTerms.getTermFrequencies();
			// // Increase decay
			// decay = decay * i;
			// for (int j = 0; j < docTerms.size(); j++) {
			// 	String termTxt = termsTxt[j];
			// 	Term term = new Term(fieldname, termTxt);
			// 	float tf = termFrequencies[j];
			// 	// double idf  = Math.pow((1 + Math.log(reader.numDocs() / ((double) .docFreq() + 1))) , 2);
			// 	// double weight = tf * idf;
			// 	double weight = tf;
			// 	// Adjust weight by decay factor
			// 	weight = weight - (weight * decay);
			// 	// Create TermQuery and add it to the collection
			// 	ScorePair sp=new ScorePair(term,beta*weight);
			// 	System.out.println(term);
			// 	System.out.println((beta*weight));
			// 	resarr.add(sp);
	        // }
		}
		return resarr;
		// Vector<TermQuery> BoostdocsTerms = setBoost( docsTerms, beta, decay );
		// expandQuery( queryStr, vHits );


		// 	Iterator<String> fields = doctermvector.iterator();
		// 	while (fields.hasNext()) {
		// 		String fieldName = fields.next();
		// 		Terms terms = doctermvector.terms(fieldName);
		// 		if (terms != null) {
		// 			TermsEnum tEnum = terms.iterator();
		// 			while (tEnum.next() != null) {
		// 				putTermInMap(fieldName, tEnum.term().utf8ToString(), tEnum.docFreq(), map, reader);
		// 			}
		// 		}
		// 	}
		// }
		// // org.apache.lucene.search.BooleanQuery.Builder bq = new BooleanQuery.Builder();
		// query.createWeight(searcher, true, 15f);
		// bq.add(query, BooleanClause.Occur.SHOULD);
        
        // return byScore;
		// Collections.sort(byScore);
		// for (int i = 0; i < 25; i++) {
		// 	ScorePair pair = byScore.get(i);
		// 	bq.add(new TermQuery(new Term(pair.getField(), pair.getTerm())), BooleanClause.Occur.SHOULD);
		// }

		// return bq.build();
	}
	
	void putTermInMap(String term, Map<String, ScorePair> map, int freq, int doccnt, double tfcnt, double decay,
			double beta) {
		// String key = field + ":" + term;
		if (map.containsKey(term))
			map.get(term).increment();
		else
			map.put(term, new ScorePair(term,freq,doccnt,tfcnt,decay,beta));
	}

	// public Vector<TermQuery> setBoost( Vector<QueryTermVector> docsTerms, double beta, double decay)
	// 		throws IOException {
	// 	Vector<TermQuery> terms = new Vector<TermQuery>();

	// 	// setBoost for each of the terms of each of the docs
	// 	for (int g = 0; g < docsTerms.size(); g++) {
	// 		QueryTermVector docTerms = docsTerms.elementAt(g);
	// 		String[] termsTxt = docTerms.getTerms();
	// 		int[] termFrequencies = docTerms.getTermFrequencies();
	// 		// Increase decay
	// 		decay = decay * g;
	// 		// Populate terms: with TermQuries and set boost
	// 		for (int i = 0; i < docTerms.size(); i++) {
	// 			// Create Term
	// 			String termTxt = termsTxt[i];
	// 			Term term = new Term(Defs.FLD_TEXT, termTxt);

	// 			// Calculate weight
	// 			float tf = termFrequencies[i];
	// 			float idf = similarity.idf((long) tf, docTerms.size());
	// 			float weight = tf * idf;
	// 			// Adjust weight by decay factor
	// 			weight = weight - (weight * decay);
	// 			logger.finest("weight: " + weight);

	// 			// Create TermQuery and add it to the collection
	// 			TermQuery termQuery = new TermQuery(term);
	// 			// Calculate and set boost
	// 			termQuery.setBoost(beta * weight);
	//             terms.add( termQuery );
	//         }
	// 	}
		
	// 	// Get rid of duplicates by merging termQueries with equal terms
	// 	merge( terms );		
        
    //     return terms;
    // }
    


    // public Query queryexpansion(Analyzer analyzer, IndexSearcher searcher, TFIDFSimilarity similarity,
    //         Properties properties, String queryStr, TopDocs hits) throws IOException {
    //     QueryExpansion queryExpansion;
    //     queryExpansion = new QueryExpansion( analyzer, searcher, similarity, properties );
    //     Query query = queryExpansion.expandQuery(queryStr, hits, properties);
    //     String expandedQuery = query.toString("contents");
    //     System.out.println( "Expanded Query: " + query );
    //     int hitsCount=1000;
    //     hits = searcher.search( query, hitsCount );
    //     return query;
    //     // Vector<TermQuery> expandedQueryTerms = queryExpansion.getExpandedTerms();
    //     //generateOutput( hits, expandedQueryTerms, query_num, writer, termCount, outCount, searcher, similarity, idxReader );
            
    // }
}