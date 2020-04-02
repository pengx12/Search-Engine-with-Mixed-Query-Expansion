package com.test.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class MyAnalyzer extends Analyzer {
    public MyAnalyzer() {
        //super(stopWords);
    }
	@Override
	protected TokenStreamComponents createComponents(String s) {
		StandardTokenizer tokenizer = new StandardTokenizer();
		TokenStream filter = new LowerCaseFilter(tokenizer);
		filter = new StandardFilter(filter);
		filter = new TrimFilter(filter);
        filter = new PorterStemFilter(filter);

        filter = new EnglishPossessiveFilter(filter);
        //filter = new StopFilter(filter, stopwords);
        filter = new KStemFilter(filter);
		//return new TokenStreamComponents(tokenizer, filter);
		filter = new StopFilter(filter, StandardAnalyzer.ENGLISH_STOP_WORDS_SET);
		filter = new SnowballFilter(filter, "English");
		return new TokenStreamComponents(tokenizer, filter);
	}
}
