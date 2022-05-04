package com.softclouds.kapture.externalcrawling.modal;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * This Class defines the Web Content
 * 
 * @author sivam
 *
 */
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
	
	List<WebContentCategroies> articlelistcategory =null;
	List<WebContentUserGroups> articlelistusergroup = null;

}
