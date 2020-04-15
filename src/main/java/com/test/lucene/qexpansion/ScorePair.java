package com.test.lucene.qexpansion;

import org.apache.lucene.index.Term;

public class ScorePair implements Comparable {
	Term term ;
	double boost;
	public String docString;
	private int count;
	private double idf;
	private double tfcnt;
	private double decay;
	public double score;
	private double beta;

	public ScorePair(String docString,double boost) {
		
		this.boost = boost;
		this.docString = docString;
	}

	public void increment() {
		count++;
	}

	public double score() {

		double weight= Math.sqrt(count) * idf/tfcnt;
		score= (weight-weight*decay)*beta;
		score=score>2 ? 2:score;
		return score;
	}

	public ScorePair(String term2, double freq, double doccnt, double tfcnt, double decay2,double beta) {
		count=1;
		this.docString=term2;
		this.idf  = Math.pow((1 + Math.log(doccnt/ ( freq + 1))) , 2);
		this.tfcnt=tfcnt;
		this.decay=decay2;
		this.beta=beta;
	}

	// Standard Lucene TF/IDF calculation
	@Override
	public int compareTo(Object o) {
		ScorePair pair = (ScorePair) o;
		if (this.score() > pair.score())
			return -1;
		else if (this.score() < pair.score())
			return 1;
		else
			return 0;
	}

	// public int getCount() {
	// 	return count;
	// }

	// public void setCount(int count) {
	// 	this.count = count;
	// }

	// public double getIdf() {
	// 	return idf;
	// }

	// public void setIdf(double idf) {
	// 	this.idf = idf;
	// }

	// public String getField() {
	// 	return field;
	// }

	// public void setField(String field) {
	// 	this.field = field;
	// }

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public double getBoost() {
		return boost;
	}

	public void setBoost(double boost) {
		this.boost = boost;
	}
}
