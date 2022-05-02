package com.softclouds.kapture.externalcrawling.helper;

import java.util.Map;
import java.util.UUID;

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

			System.out.printf("Title: %s%n", title);
			System.out.printf("Body: %s", body);

			content = new URLContentModal();
			content.setTitle(title);
			content.setBody(body);

		} catch (Exception e) {
			log.error("CrawlingHelper::connetToURLAndParseContent{} ::Error while prase the url content "
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
}
