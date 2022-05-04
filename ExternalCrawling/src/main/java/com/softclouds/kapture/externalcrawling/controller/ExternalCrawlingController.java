package com.softclouds.kapture.externalcrawling.controller;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.softclouds.kapture.externalcrawling.bo.ExternalCrawlingContent;
import com.softclouds.kapture.externalcrawling.bo.WebContentURLs;
import com.softclouds.kapture.externalcrawling.service.ExternalCrawlingService;

import lombok.extern.slf4j.Slf4j;

/**
 * Using the scheduler and get the web URLs and web content will be index in
 * elastic search
 * 
 * @author sivam
 *
 */
@RestController
@Slf4j
public class ExternalCrawlingController {

	@Autowired
	ExternalCrawlingService crawlingService;

	/**
	 * Scheduler Run at every 24 hours
	 */
	@Scheduled(cron = "0 0 * * * ?")
	//@Scheduled(cron = "1 * * * * ?")
	public void run() {
		log.info("Start ExternalCrawlingController");

		log.info("Sheduler started {}", Calendar.getInstance().getTime());
		log.info("Current time is :: " + Calendar.getInstance().getTime());
		Optional<ExternalCrawlingContent> externalCrawlingDetails = null;
		ExternalCrawlingContent externalCrawling = null;
		Integer collectionCrawlId = 0;
		try {
			List<WebContentURLs> webContentURLs = crawlingService.fetchWebURLS();

			if (null != webContentURLs && !webContentURLs.isEmpty()) {
				for (WebContentURLs webUrLs : webContentURLs) {
					collectionCrawlId = webUrLs.getCrawlingId();
					if (null != collectionCrawlId) {

						externalCrawlingDetails = crawlingService.findByExternalCrawlingDetails(collectionCrawlId);

						if (externalCrawlingDetails.isPresent()) {
							externalCrawling = externalCrawlingDetails.get();
						} else {
							// No details found
						}

						crawlingService.readContentFromWebURLs(webUrLs, externalCrawling);
					}
				}
			}

		} catch (Exception e) {
			log.error("WebCrawlingController:::Sheduler run()::" + e.getMessage());
		}
		log.info("End ExternalCrawlingController");
	}
}
