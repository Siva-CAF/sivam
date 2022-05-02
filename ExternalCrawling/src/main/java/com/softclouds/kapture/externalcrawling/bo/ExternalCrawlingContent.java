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
@Table(name = "k_external_crawling_content")
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class ExternalCrawlingContent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "collection_name")
	private String collectionName;

	@Column(name = "locale")
	private String locale;

	@Column(name = "collection_des")
	private String collectionDescription;

	@Column(name = "depth_crawl")
	private Integer depthCrawl;

	@Column(name = "enable_this_collection")
	private boolean enableThisCollection;

	@Column(name = "type")
	private String type;

	@Column(name = "number_of_docs")
	private Long numberOfDocuments;

	@Column(name = "raw_size")
	private Long rawSize;

	@Column(name = "creation_date")
	private String crawlingCreationDate;

	@Column(name = "createdby")
	private String createdBy;

	@Column(name = "modified_date")
	private String crawlingModifiedDate;

	@Column(name = "modifiedby")
	private String modifiedBy;

}
