package com.softclouds.kapture.externalcrawling.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import com.softclouds.kapture.externalcrawling.util.CrawlingConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * This config class defines the RestHighLevelClient using to connect the
 * Elastic search
 * 
 * @author sivam
 *
 */
@Slf4j
public class RestHighLevelClientUtil {

	private RestHighLevelClient client = null;
	private static Properties properties = null;

	private RestHighLevelClientUtil() {
		// Exists only to defeat multiple instantiation.
		try {
			client = initRestClient();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	public static RestHighLevelClientUtil getInstance() {
		return Builder.INSTANCE;
	}

	public RestHighLevelClient getRestClient() {
		return this.client;
	}

	private static class Builder {
		private final static RestHighLevelClientUtil INSTANCE = new RestHighLevelClientUtil();
	}

	// Connected to Elastic Search
	private RestHighLevelClient initRestClient() throws IOException {
		log.info("Start of getRestClient()::::");
		RestHighLevelClient client = null;
		RestClientBuilder builder = null;

		String username = getProperty("elasticsearch.username");
		String password = getProperty("elasticsearch.password");
		String host = getProperty("elasticsearch.host");
		int port = getIntProperty("elasticsearch.port");

		log.info("ES-Connection " + " = " + host + " :: " + port + " :: " + username + " :: " + password);

		try {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

			builder = RestClient.builder(new HttpHost(host, port)).setHttpClientConfigCallback(
					httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

			client = new RestHighLevelClient(builder);
			log.info("elastic client initialized {}", client.cluster());

		} catch (Exception e) {
			log.error("initRestClient failed::", e);
		}

		log.info("End Rest Client:::");

		return client;

	}

	public static Properties getProperties() throws IOException {
		if (properties == null) {
			properties = new Properties();
			properties.load(RestHighLevelClientUtil.class.getClassLoader()
					.getResourceAsStream(CrawlingConstants.ELASTIC_SEARCH_PROPERTIES));
		}
		return properties;
	}

	public static String getProperty(String key) throws IOException {
		return getProperties().getProperty(key);
	}

	public static int getIntProperty(String key) throws IOException {
		String value = getProperties().getProperty(key);
		return Integer.valueOf(value);
	}

	public static void main(String[] args) {
		RestHighLevelClientUtil.getInstance().getRestClient();
	}

}
