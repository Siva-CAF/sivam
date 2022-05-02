package com.softclouds.kapture.externalcrawling.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author sivam
 *
 */
@Entity
@Table(name = "k_web_content_urls")
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class WebContentURLs {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "starting_point_urls")
	private String startingPointURLs;

	@Column(name = "crawl_id")
	private Integer crawlingId;

	@Column(name = "creation_date")
	private String crawlingCreationDate;

	@Column(name = "createdby")
	private String createdBy;

	@Column(name = "url_content_crawling_status")
	private boolean urlContentCrawlingStatus;

	@Column(name = "title")
	private String title;

	@Column(name = "document_type")
	private String documentType;

	@Column(name = "encoding")
	private String encoding;

}
