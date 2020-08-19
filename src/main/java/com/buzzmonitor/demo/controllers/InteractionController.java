package com.buzzmonitor.demo.controllers;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.elasticsearch.ResourceNotFoundException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/interactions")
public class InteractionController {
	private static RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
	private MatchQueryBuilder queryBuilder = null;
	private MatchQueryBuilder secondQueryBuilder = null;
	private SearchHit[] searchHits = null;
	private Map<String, String> idsAndContents = null;
	private String json = "";
	private SearchResponse searchResponse = null;
	
	@GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getAll() {
		String jsonResponse = "";
		try {
			SearchRequest searchRequest = new SearchRequest("buzz-database");
			final SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] searchHits = searchResponse.getHits().getHits();
			for (SearchHit searchHit : searchHits) {
			      String hitJson = searchHit.getSourceAsString() + ",";
			      jsonResponse += hitJson;
			}
			jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 1);	// remove latest ",".
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(jsonResponse != "")
	        return new ResponseEntity<String>(jsonResponse, HttpStatus.OK);
		else
			throw new ElementNotFound();
	}
	
	@GetMapping(value="/{socialmedia}/{name}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getByName(@PathVariable("socialmedia") String socialMedia, @PathVariable("name") String name) {
		String jsonResponse = "";
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		MatchQueryBuilder secondQueryBuilder; 
		try {
			SearchRequest searchRequest = new SearchRequest("buzz-database");
			MatchQueryBuilder queryBuilder = new MatchQueryBuilder("origin", socialMedia);
			if(socialMedia.equals("twitter")) {
				secondQueryBuilder = new MatchQueryBuilder("author.screenname", name);
			}
			else { 
				secondQueryBuilder = new MatchQueryBuilder("author.login", name);
			}
			
			BoolQueryBuilder query = QueryBuilders.boolQuery()
					   .filter(queryBuilder)
					   .filter(secondQueryBuilder);
			searchSourceBuilder.query(query);
			searchRequest.source(searchSourceBuilder);
		
			SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] searchHits = searchResponse.getHits().getHits();
			for (SearchHit searchHit : searchHits) {
			      String hitJson = searchHit.getSourceAsString() + ",";
			      jsonResponse += hitJson;
			}
			
			jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 1);	// remove latest ",".
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(jsonResponse != "") { 
			return new ResponseEntity<String>(jsonResponse, HttpStatus.OK);
		}
		else { 
			throw new ElementNotFound(); 
		}
	}
	// to update
	@GetMapping(value="/{socialmedia}/{name}/posts", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getPostsByName(@PathVariable("socialmedia") String socialMedia, 
												@PathVariable("name") String name) {
		String jsonResponse = "{\"content\": [\"";
		MatchQueryBuilder queryBuilder = null;
		MatchQueryBuilder secondQueryBuilder = null;
		SearchHit[] searchHits = null;
		
		try {
			SearchRequest searchRequest = new SearchRequest("buzz-database");
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			queryBuilder = new MatchQueryBuilder("origin", socialMedia);
			if(socialMedia.equals("twitter")) {
				secondQueryBuilder = new MatchQueryBuilder("author.screenname", name);
			}
			else { 
				secondQueryBuilder = new MatchQueryBuilder("author.login", name);
			}
			
			BoolQueryBuilder query = QueryBuilders.boolQuery()
					   .filter(queryBuilder)
					   .filter(secondQueryBuilder);
			searchSourceBuilder.query(query);
			searchRequest.source(searchSourceBuilder);
			
			SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
			searchHits = searchResponse.getHits().getHits();
			for (SearchHit searchHit : searchHits) {
			      String hitJson = searchHit.getSourceAsString();
			      System.out.println(hitJson);
			      JSONObject jsonObj = new JSONObject(hitJson);
			      jsonResponse = jsonResponse + jsonObj.getString("content") + "\",\"";
			}
			jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 2) + "]}";
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(jsonResponse != "{\"content\": [\"") { 
			return new ResponseEntity<String>(jsonResponse, HttpStatus.OK);
		}
		
		else { 
			throw new ElementNotFound(); 
		}
	}
	
	@GetMapping(value="/{socialmedia}/{name}/replies", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getRepliesByName(@PathVariable("socialmedia") String socialMedia, 
												@PathVariable("name") String name) {
		if(socialMedia.equals("twitter")) {
			return getTwitterResponse(name);
		}
		else if(socialMedia.equals("instagram")){
			return getInstaResponse(name);
		}
		else { 
			throw new ElementNotFound(); 
		}
		
	}

	private ResponseEntity<String> getInstaResponse(String login) {
		idsAndContents = new HashMap<String, String>();
		SearchRequest searchRequest = new SearchRequest("buzz-database");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		queryBuilder = new MatchQueryBuilder("origin", "instagram");
		secondQueryBuilder = new MatchQueryBuilder("author.login", login);
		MatchQueryBuilder thirdQueryBuilder = new MatchQueryBuilder("type", "image");
		
		BoolQueryBuilder query = QueryBuilders.boolQuery()
				   .filter(queryBuilder)
				   .filter(secondQueryBuilder)
				   .filter(thirdQueryBuilder);
		searchSourceBuilder.query(query);
		searchRequest.source(searchSourceBuilder);
		
		try {
			searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		searchHits = searchResponse.getHits().getHits();
		checkHist(searchHits);
		for (SearchHit searchHit : searchHits) {
		      String hitJson = searchHit.getSourceAsString();
		      JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject(hitJson);
		    	idsAndContents.put(jsonObj.getString("link"), jsonObj.getString("content"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		      
		}
		
		for(String post : idsAndContents.keySet()) {
			queryBuilder = new MatchQueryBuilder("link", post);
			secondQueryBuilder = new MatchQueryBuilder("type", "comment");
			searchSourceBuilder.query(queryBuilder);
			query = QueryBuilders.boolQuery()
					   .filter(queryBuilder)
					   .filter(secondQueryBuilder);
			searchSourceBuilder.query(query);
			searchRequest.source(searchSourceBuilder);
			try {
				searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			searchHits = searchResponse.getHits().getHits();
			checkHist(searchHits);
			json = "{\"link\": \"" + post + "\", \"content\":\"" + idsAndContents.get(post) + "\", \"comments\": [\"";
			for (SearchHit searchHit : searchHits) {
				String hitJson = searchHit.getSourceAsString();
			      JSONObject jsonObj = null;
				try {
					jsonObj = new JSONObject(hitJson);
					json += jsonObj.getString("content") + "\",\"";
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			json = json.substring(0, json.length() - 2) + "]}";
		}
		return new ResponseEntity<String>(json, HttpStatus.OK);

	}

	private ResponseEntity<String> getTwitterResponse(String name) {
		SearchRequest searchRequest = new SearchRequest("buzz-database");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		idsAndContents = new HashMap<String, String>();
		queryBuilder = new MatchQueryBuilder("origin", "twitter");
		secondQueryBuilder = new MatchQueryBuilder("author.screenname", name);
		
		BoolQueryBuilder query = QueryBuilders.boolQuery()
				   .filter(queryBuilder)
				   .filter(secondQueryBuilder);
		searchSourceBuilder.query(query);
		searchRequest.source(searchSourceBuilder);

		try {
			searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		searchHits = searchResponse.getHits().getHits();
		checkHist(searchHits);
		
		for (SearchHit searchHit : searchHits) {
		      String hitJson = searchHit.getSourceAsString();
		      JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject(hitJson);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		      try {
				idsAndContents.put(jsonObj.getString("post_id"), jsonObj.getString("content"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		for(String key : idsAndContents.keySet()) {
			queryBuilder = new MatchQueryBuilder("in_reply_to", key);
			searchSourceBuilder.query(queryBuilder);
			searchRequest.source(searchSourceBuilder);
			try {
				searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			searchHits = searchResponse.getHits().getHits();
			checkHist(searchHits);
			json = "{\"post_id\": \"" + key + "\", \"content\":\"" + idsAndContents.get(key) + "\", \"replies\": [\"";
			for (SearchHit searchHit : searchHits) {
				String hitJson = searchHit.getSourceAsString();
			      JSONObject jsonObj = null;
				try {
					jsonObj = new JSONObject(hitJson);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			      try {
					json += jsonObj.getString("content") + "\",\"";
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			json = json.substring(0, json.length() - 1) + "]}";
		}
		return new ResponseEntity<String>(json, HttpStatus.OK);
	}
	
	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getById(@PathVariable("id") String id){
		SearchRequest searchRequest = new SearchRequest("buzz-database");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		queryBuilder = new MatchQueryBuilder("_id", id);
		BoolQueryBuilder query = QueryBuilders.boolQuery()
				   .filter(queryBuilder);
		searchSourceBuilder.query(query);
		searchRequest.source(searchSourceBuilder);

		try {
			searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		searchHits = searchResponse.getHits().getHits();
		checkHist(searchHits);
		String hitJson = searchHits[0].getSourceAsString();
		return new ResponseEntity<String>(hitJson, HttpStatus.OK);
	}
	
	public void checkHist(SearchHit[] hits) {
		if (hits.length == 0) {
			throw new ElementNotFound(); 
		}
	}
}
