package com.softclouds.kapture.externalcrawling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.softclouds.kapture.externalcrawling.bo.ExternalCrawlingContent;

/**
 * External Crawling Repository to fetch and update the records
 * 
 * @author sivam
 *
 */
@Repository
public interface ExternalCrawlingRepository
		extends JpaRepository<ExternalCrawlingContent, Integer>, CrudRepository<ExternalCrawlingContent, Integer> {

}
