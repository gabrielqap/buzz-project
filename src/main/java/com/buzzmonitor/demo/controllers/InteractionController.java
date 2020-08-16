package com.buzzmonitor.demo.controllers;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/interactions")
public class InteractionController {
	private static RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
	
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
	
	@GetMapping(value="/{socialmedia}/{name}/posts", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getPostsByName(@PathVariable("socialmedia") String socialMedia, 
												@PathVariable("name") String screenname) {
		String jsonResponse = "{\"content\": [\"";
		SearchHit[] searchHits = null;
		try {
			SearchRequest searchRequest = new SearchRequest("buzz-database");
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			MatchQueryBuilder queryBuilder = new MatchQueryBuilder("origin", "twitter");
			MatchQueryBuilder secondQueryBuilder = new MatchQueryBuilder("author.screenname", "meek_amm");
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
			      jsonResponse = jsonResponse + jsonObj.getString("contents") + "\",\"";
			}
			jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 2) + "]}";
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(searchHits.length != 0) { 
			return new ResponseEntity<String>(jsonResponse, HttpStatus.OK);
		}
		
		else { 
			throw new ElementNotFound(); 
		}
		
	
		
	}
}
