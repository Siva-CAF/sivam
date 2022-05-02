package com.softclouds.kapture.externalcrawling.modal;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebContent {

	private String title;
	private String type;
	private String DOCUMENTID;
	private String articleId;
	private String primaryLocale;

	private String createdDate;

	private String status;
	private String articleState;

}