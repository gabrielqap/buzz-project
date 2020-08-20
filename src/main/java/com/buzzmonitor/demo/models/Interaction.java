package com.buzzmonitor.demo.models;

public class Interaction {
	private String interactionType;
	private String postId;
	private String sentiment;
	private String service;
	private String application;
	private String origin;
	private boolean archived;
	private int count;
	private int engagement;
	private String collected_time;
	private char[] source;
	private String date;
	private String elasticSearchId;
	private String elasticSearchRouting;
	private String elasticSearchIndex;
	private String brand;
	private String user;
	
	public String getInteractionType() {
		return interactionType;
	}
	
	public void setInteractionType(String interactionType) {
		this.interactionType = interactionType;
	}
	public String getPostId() {
		return postId;
	}
	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getSentiment() {
		return sentiment;
	}

	public void setSentiment(String sentiment) {
		this.sentiment = sentiment;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getOrigin() {
		return origin;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getEngagement() {
		return engagement;
	}

	public void setEngagement(int engagement) {
		this.engagement = engagement;
	}

	public String getCollected_time() {
		return collected_time;
	}

	public void setCollected_time(String collected_time) {
		this.collected_time = collected_time;
	}

	public char[] getSource() {
		return source;
	}

	public void setSource(char[] source) {
		this.source = source;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getElasticSearchId() {
		return elasticSearchId;
	}

	public void setElasticSearchId(String elasticSearchId) {
		this.elasticSearchId = elasticSearchId;
	}

	public String getElasticSearchRouting() {
		return elasticSearchRouting;
	}

	public void setElasticSearchRouting(String elasticSearchRouting) {
		this.elasticSearchRouting = elasticSearchRouting;
	}

	public String getElasticSearchIndex() {
		return elasticSearchIndex;
	}

	public void setElasticSearchIndex(String elasticSearchIndex) {
		this.elasticSearchIndex = elasticSearchIndex;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
