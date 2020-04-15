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
			if (beta<0)
			{
				break;
			}
			ScorePair sp=new ScorePair(docTxtBuffer.toString(),beta);
			resarr.add(sp);
		}
		return resarr;
	}
	
	void putTermInMap(String term, Map<String, ScorePair> map, int freq, int doccnt, double tfcnt, double decay,
			double beta) {
		// String key = field + ":" + term;
		if (map.containsKey(term))
			map.get(term).increment();
		else
			map.put(term, new ScorePair(term,freq,doccnt,tfcnt,decay,beta));
	}

}