package com.softclouds.kapture.externalcrawling.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.UUID;

import org.apache.tika.Tika;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.softclouds.kapture.externalcrawling.config.RestHighLevelClientUtil;
import com.softclouds.kapture.externalcrawling.modal.URLContentModal;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author sivam
 *
 */
@Slf4j
@Component("crawlingHelper")
public class CrawlingHelper {

	/**
	 * Get the URL content using jsoup
	 * 
	 * @param contentURL
	 * @return
	 */
	public URLContentModal connetToURLAndParseContent(String contentURL) {
		log.info("Strat of CrawlingHelper::connetToURLAndParseContent{}", contentURL);
		URLContentModal content = null;
		String title = null;
		String body = null;
		String html = null;
		Document doc = null;
		try {
			html = Jsoup.connect(contentURL).get().html();
			doc = Jsoup.parse(html);
			title = doc.title();
			body = doc.body().text();
			log.info("Title: %s%n", title);
			log.info("Body: %s", body);
			content = new URLContentModal();
			content.setCrawlURL(contentURL);
			content.setTitle(title);
			content.setBody(body);

		} catch (Exception e) {
			log.error("CrawlingHelper::connetToURLAndParseContent{} :::Error while prase the url content "
					+ e.getMessage());
		}
		return content;

	}
	
	/**
	 * Web content has indexed to elastic search and return ESID
	 * 
	 * @param source
	 * @param webIndexName
	 * @return
	 */
	public String webContentIndexInElasticSearch(Map<String, Object> source, String webIndexName) {
		log.info("Start of CrawlingHelper::webContentIndexInElasticSearch:::");
		String uniqueID = null;
		IndexRequest request = null;
		IndexResponse response = null;
		try {
			uniqueID = UUID.randomUUID().toString();
			request = new IndexRequest().index(webIndexName).id(uniqueID).source(source);
			response = RestHighLevelClientUtil.getInstance().getRestClient().index(request, RequestOptions.DEFAULT);
			log.info("response::" + response.getIndex() + "::::::" + response.getId());
			if (response.getId() != null && !response.getId().isEmpty()) {
				log.info("Index Document id::{}", response.getId());
			}

		} catch (Exception e) {
			log.error("CrawlingHelper::webContentIndexInElasticSearch::error while index the document in ES()"
					+ e.getMessage());
		}
		log.info("END of webContentIndexInElasticSearch::documents ES ID{}", response.getId());
		return response.getId();
	}

	/**
	 * Connect to URL and parse pdf content
	 * @param contentURL
	 * @return
	 * @throws IOException
	 */
	public URLContentModal connetToURLAndParsePdfContent(String contentURL) {
		log.info("Strat of CrawlingHelper::connetToURLAndParsePdfContent{}", contentURL);
		URLContentModal content = null;
		Tika tika = null;
		String body = null;
		try {
			URL url = new URL(contentURL);
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();

			File f = new File(contentURL);
			String[] parts = f.getName().split(".pdf");
			String title = parts[0];

			tika = new Tika();
			body = tika.parseToString(is);

			content = new URLContentModal();
			content.setTitle(title);
			content.setBody(body);

			log.info("body prased {}", body);

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("End of CrawlingHelper::connetToURLAndParsePdfContent{}", contentURL);
		return content;
	}
}
