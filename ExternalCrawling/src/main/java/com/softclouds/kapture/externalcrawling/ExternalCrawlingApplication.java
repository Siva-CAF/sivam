package com.softclouds.kapture.externalcrawling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * External Crawling main application
 * 
 * @author sivam
 *
 */
@SpringBootApplication
@EnableScheduling
public class ExternalCrawlingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExternalCrawlingApplication.class, args);
	}

}
