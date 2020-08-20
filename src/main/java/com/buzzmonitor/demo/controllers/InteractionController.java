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
import org.elasticsearch.action.bulk.BulkItemResponse;
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
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buzzmonitor.demo.resources.InteractionResource;

@RestController
@RequestMapping(value = "/api/interactions")
public class InteractionController {
	private static RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
	private MatchQueryBuilder queryBuilder = null;
	private MatchQueryBuilder secondQueryBuilder = null;
	private SearchHit[] searchHits = null;
	private Map<String, String> idsAndContents = null;
	private String json = "";
	private SearchResponse searchResponse = null;
	private InteractionResource interactionSource = new InteractionResource();
	
	@GetMapping(produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getAll() {
		return interactionSource.getAllResource();
	}
	
	@GetMapping(value="/{socialmedia}/{name}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getByName(@PathVariable("socialmedia") String socialMedia, @PathVariable("name") String name) {
		return interactionSource.getByName(socialMedia, name);
	}
	

	@GetMapping(value="/{socialmedia}/{name}/posts", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getPostsByName(@PathVariable("socialmedia") String socialMedia, 
												@PathVariable("name") String name) {
		return interactionSource.getByName(socialMedia, name);
	}
	
	@GetMapping(value="/{socialmedia}/{name}/replies", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getRepliesByName(@PathVariable("socialmedia") String socialMedia, 
												@PathVariable("name") String name) {
		return interactionSource.getRepliesByName(socialMedia, name);
	}

	
	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getById(@PathVariable("id") String id){
		return interactionSource.getById(id);
	}
	
	@DeleteMapping(value="/{id}")
	public ResponseEntity<String> deleteById(@PathVariable("id") String id){
		return interactionSource.deleteById(id);
	}
	
	@PutMapping(value="/{id}",  
		    consumes={MediaType.APPLICATION_JSON_VALUE}, 
		    produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> putById(@PathVariable("id") String id, @RequestBody String requestBody){
		return interactionSource.putById(id, requestBody);
	}
	
	@PostMapping(consumes={MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> Post(@RequestBody String requestBody){
		return interactionSource.Post(requestBody);
	}
	
	
}
