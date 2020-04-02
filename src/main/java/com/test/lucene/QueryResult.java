package com.test.lucene;

public class QueryResult {

	private int queryNumber;
	private String content;
	private int documentRank;
	private float documentScore;
	private String cranQueryNumber;

	public QueryResult(int queryNumber, String cranQueryNumber, String content, int documentRank,
			float documentScore) {
		this.content = content;
		this.documentRank = documentRank;
		this.documentScore = documentScore;
		this.queryNumber = queryNumber;
		this.cranQueryNumber = cranQueryNumber;
	}

	public int getQueryNumber() {
		return queryNumber;
	}

	public void setQueryNumber(int queryNumber) {
		this.queryNumber = queryNumber;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getDocumentRank() {
		return documentRank;
	}

	public void setDocumentRank(int i) {
		this.documentRank = i;
	}

	public float getDocumentScore() {
		return documentScore;
	}

	public void setDocumentScore(float documentScore) {
		this.documentScore = documentScore;
	}

	public String getCranQueryNumber() {
		return cranQueryNumber;
	}

	public void setCranQueryNumber(String cranQueryNumber) {
		this.cranQueryNumber = cranQueryNumber;
	}
}
