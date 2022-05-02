package com.softclouds.kapture.externalcrawling.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.softclouds.kapture.externalcrawling.bo.ExternalCrawlingContent;
import com.softclouds.kapture.externalcrawling.bo.WebContentURLs;

/**
 * External Crawling Service interface
 * 
 * @author sivam
 *
 */
@Service
public interface ExternalCrawlingService {

	List<WebContentURLs> fetchWebURLS();

	Optional<ExternalCrawlingContent> findByExternalCrawlingDetails(Integer collectionCrawlId);

	void readContentFromWebURLs(WebContentURLs webUrLs, ExternalCrawlingContent externalCrawling);

}
