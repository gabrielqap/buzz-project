package com.buzzmonitor.demo;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tomcat.util.json.JSONParser;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class BuzzProjectApplication {
	private static URL url = null;
	private static RestHighLevelClient client;
	private static RestClient lowClient;
	private static RestHighLevelClient esClient = new RestHighLevelClient(RestClient.builder(new HttpHost("127.0.0.1", 9200, "http")));
	
	public static void main(String[] args) {
		SpringApplication.run(BuzzProjectApplication.class, args);
		try {
			//JSONArray db = readJsonFromUrl("https://gist.githubusercontent.com/fabiosl/bfef293c110d3513b334f4134bff8ca2/raw/cb1ff2de0899f1c9a6c11c006ba24ff56e9f0571/dataset.json");
			//loadDatabase(db);
			//initElasticSearch();
			//createIndex();
			//insertDatabase(db);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void createIndex() {
	    String indexName="buzz-database";
	    try {
	        Response response = client.getLowLevelClient().performRequest(new Request("HEAD", "/" + indexName));
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode == 404) {
	            CreateIndexRequest cireq = new CreateIndexRequest(indexName);
	            CreateIndexResponse ciresp = client.indices().create(cireq, RequestOptions.DEFAULT);
	            System.out.println("Created index");
	        } else {
	            System.out.println("Index exists");
	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}


	public static void initElasticSearch() {
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("user", "password"));
	    client = new RestHighLevelClient(
	            RestClient.builder(
	               new HttpHost("localhost", 9200))
	                 .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)));
	}

	public static void insertDatabase(JSONArray db) {
		try {
	     
	        BulkRequest request = new BulkRequest();
	        
	        for (int i = 0; i < db.length(); i++) {
	        	request.add(new IndexRequest("buzz-database").source(db.getJSONObject(i).toString(), XContentType.JSON));
	        	BulkResponse bulkresp = client.bulk(request, RequestOptions.DEFAULT);
	        	if (bulkresp.hasFailures()) {
		            for (BulkItemResponse bulkItemResponse : bulkresp) {
		                if (bulkItemResponse.isFailed()) {
		                    BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
		                    System.out.println("Error " + failure.toString());
		                }
		            }
		        }
		        else {
		        	 System.out.println("Uploaded!");
		        }
	        	request=new BulkRequest();
	        }
	        client.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	}
	
	public static JSONArray readJsonFromUrl(String url) throws IOException, JSONException {
	    InputStream is = new URL(url).openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      JSONArray data = json.getJSONArray("data");
	      return data;
	    } finally {
	      is.close();
	    }
	}

}
