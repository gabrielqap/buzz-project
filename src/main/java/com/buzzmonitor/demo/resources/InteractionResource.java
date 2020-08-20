package com.buzzmonitor.demo.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.buzzmonitor.demo.BuzzProjectApplication;
import com.buzzmonitor.demo.controllers.*;

public class InteractionResource {
	private static RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
	private MatchQueryBuilder queryBuilder = null;
	private MatchQueryBuilder secondQueryBuilder = null;
	private SearchHit[] searchHits = null;
	private Map<String, String> idsAndContents = null;
	private String json = "";
	private SearchResponse searchResponse = null;
	
	public void checkHist(SearchHit[] hits) {
		if (hits.length == 0) {
			throw new ElementNotFound(); 
		}
	}
	
	public ResponseEntity<String> getAllResource() {
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
	
	public ResponseEntity<String> getByName(String socialMedia, String name) {
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
	
	public ResponseEntity<String> getPostsByName(String socialMedia, String name) {
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
	
	public ResponseEntity<String> getRepliesByName(String socialMedia,String name) {
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
	
	public ResponseEntity<String> getById(String id){
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
	
	public ResponseEntity<String> deleteById(String id){
		DeleteRequest request = new DeleteRequest("buzz-database", id);
		DeleteResponse response = null;
		try {
			response = esClient.delete(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
		if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
			return new ResponseEntity<String>("Deleted!", HttpStatus.OK);
		}
		else {
			throw new ElementNotFound();
		}
	}
	
	public ResponseEntity<String> putById(String id, String requestBody){
		JSONObject reqBody = null;
		HashMap<String, String> requestJson = new HashMap<String, String>();
		try {
			reqBody = new JSONObject(requestBody);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Iterator keysToCopyIterator = reqBody.keys();
		while(keysToCopyIterator.hasNext()) {
		    String key = (String) keysToCopyIterator.next();
		    try {
				requestJson.put(key, reqBody.getString(key));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Map<String, Object> jsonMap = new HashMap<>();
		for(String key : requestJson.keySet()) {
			jsonMap.put(key, requestJson.get(key));
		}
		UpdateRequest request = new UpdateRequest("buzz-database", id)
		        .doc(jsonMap);
		UpdateResponse response = null;
		try {
			response = esClient.update(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
		if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
			return new ResponseEntity<String>("Updated!", HttpStatus.OK);
		}
		else {
			throw new ElementNotFound();
		}
	}
	
	public ResponseEntity<String> Post(String requestBody){
		BulkRequest request = new BulkRequest();
		request.add(new IndexRequest("buzz-database").source(requestBody, XContentType.JSON));
		BulkResponse bulkResp = null;
    	try {
			bulkResp = esClient.bulk(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	if (bulkResp.hasFailures()) {
			throw new ElementError();
		}
		else {
			return new ResponseEntity<String>("Created!", HttpStatus.CREATED);
		}
	}
}
