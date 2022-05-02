package com.softclouds.kapture.externalcrawling.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.softclouds.kapture.externalcrawling.bo.WebContentURLs;

/**
 * Web ContentURLs Repository
 * 
 * @author sivam
 *
 */
@Repository
public interface WebContentURLsRepository extends JpaRepository<WebContentURLs, Integer> {

	List<WebContentURLs> findAllByUrlContentCrawlingStatus(Boolean flag);

	WebContentURLs findByIdAndCrawlingId(Integer id, Integer crawlingId);

}
