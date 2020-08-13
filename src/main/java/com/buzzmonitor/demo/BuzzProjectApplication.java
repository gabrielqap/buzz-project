package com.buzzmonitor.demo;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
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

@SpringBootApplication
public class BuzzProjectApplication {
	static URL url = null;
	
	public static void main(String[] args) {
		SpringApplication.run(BuzzProjectApplication.class, args);
		try {
			JSONArray db = readJsonFromUrl("https://gist.githubusercontent.com/fabiosl/bfef293c110d3513b334f4134bff8ca2/raw/cb1ff2de0899f1c9a6c11c006ba24ff56e9f0571/dataset.json");
			//loadDatabase(db);
			launchElasticSearch(db);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void launchElasticSearch(JSONArray db) {
		try {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("user", "password"));
		    RestHighLevelClient client = new RestHighLevelClient(
		            RestClient.builder(
		               new HttpHost("localhost", 9200))
		                 .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)));
		    String indexName="buzz-database";
	        Response response = client.getLowLevelClient().performRequest(new Request("HEAD", "/" + indexName));
	        int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode == 404) {
	            CreateIndexRequest cireq = new CreateIndexRequest(indexName);
	            CreateIndexResponse ciresp = client.indices().create(cireq, RequestOptions.DEFAULT);
	            System.out.println("Created index");
	        } else {
	            System.out.println("Index exists");
	        }
	        
	        BulkRequest request = new BulkRequest();
	        
	        for (int i = 0; i < db.length(); i++) {
	        	request.add(new IndexRequest(indexName).source(db.getJSONObject(i), XContentType.JSON));
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
	
	public static void loadDatabase(JSONObject json) {
		IndexRequest request = new IndexRequest("posts");
		request.id("1"); 
		request.source(json, XContentType.JSON);
	}

}
