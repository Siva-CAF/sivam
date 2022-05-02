package com.softclouds.kapture.kloader.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tika.Tika;
import org.json.JSONArray;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AttachmentsUtil {

	public static JSONObject processAttachements(JSONObject channelJSONObject, String path, String docu_id,
			String artilcleTitle, String locale, String channelName) {
		List<Map<String, String>> attachementMapList = new ArrayList<>();
		JSONObject attachmentsJSONObj = null;
		Object attachementObject = null;
		String currentKey = null;
		List<Map<String, String>> attachmentsMap = new ArrayList<>();
		//List<Map<String, String>> arrayAttachmentMap = new ArrayList<>();
		Map<String, String> arrayattchMap = null;
		Map<String, String> cmsattachementMap = null;
		Map<String, Object> finalMap = null;
		JSONObject channelUpdatedObject = null;
		JSONObject attachmet =null;
		JSONObject iteratorJSONObj =null;
		try {

			if (channelJSONObject.has("ATTACHMENTS")) {
				Object attachements = channelJSONObject.opt("ATTACHMENTS");
				if (attachements != null) {
					/* ATTACHEMENTS CHECK JSONOBJECT */
					if (attachements instanceof JSONObject) {
						attachmentsJSONObj = channelJSONObject.getJSONObject("ATTACHMENTS");
						if (attachmentsJSONObj instanceof JSONObject) {
							Iterator<String> itr = attachmentsJSONObj.keys();
							while (itr.hasNext()) {
								List<Map<String, String>> arrayAttachmentMap= new ArrayList<>();
								String key = (String) itr.next();
								Object object = attachmentsJSONObj.get(key);

								if (object instanceof JSONObject) {
									 attachmet = ((JSONObject) object).getJSONObject("ATTACHMENT");
									cmsattachementMap = praseAttachment(attachmet, path);
									attachmentsMap.add(cmsattachementMap);
								}
								if (object instanceof JSONArray) {
									JSONArray array = attachmentsJSONObj.getJSONArray(key);
									for (int i = 0; i < array.length(); i++) {
										iteratorJSONObj = array.getJSONObject(i);
										attachmet = iteratorJSONObj.getJSONObject("ATTACHMENT");
										cmsattachementMap = praseAttachment(attachmet, path);
										arrayAttachmentMap.add(cmsattachementMap);
									}
									attachmentsMap.addAll(arrayAttachmentMap);
								}
								/*
								 * // set Attachment path to Map(ES) attachmentsMap.add(cmsattachementMap);
								 */
								log.info("set all attachments meta in attachementsMAp::");

							}

						}

					}

					if (attachements instanceof JSONArray) {
						JSONArray array = channelJSONObject.getJSONArray("ATTACHMENTS");
						for (int i = 0; i < array.length(); i++) {
							attachementObject = array.getJSONObject(i);

							if (attachementObject instanceof JSONArray) {
								// JSON Array
							}
							if (attachementObject instanceof JSONObject) {
								// JSON Object
							}

						}
					}

					if (attachmentsMap != null && !attachmentsMap.isEmpty()) {
						log.info("AttachmentsMap:::" + attachmentsMap.size());
						channelUpdatedObject = ElasticSearchUtil.attachmentsContentIndexInElaticSearch(attachmentsMap,
								channelJSONObject, docu_id, artilcleTitle, locale, channelName);
					}
				}

			}

		} catch (Exception e) {
			log.error("Failed To parse Exception::", e);
		}
		return channelUpdatedObject;
	}

	private static Map<String, String> praseAttachment(JSONObject attachmet, String path) {
		String fileContent = null;
		String fileType = null;
		String cmsattachementName = null;
		String getS3UrlPath = null;
		String fileExtension = null;
		String attachmentTitle = null;
		Map<String, String> cmsattachementMap = null;
		Object finalAttachcheck = null;
		if (attachmet != null && attachmet != JSONObject.NULL) {

			if (attachmet.has("filename")) {
				cmsattachementName = KloaderReuseUtil.checkNullorEmpty(attachmet.getString("filename"));
			}
			if (attachmet.has("fileType")) {
				fileType = KloaderReuseUtil.checkNullorEmpty(attachmet.getString("fileType"));
			}
			if (attachmet.has("filepath")) {
				getS3UrlPath = KloaderReuseUtil.checkNullorEmpty(attachmet.getString("filepath"));
			}
			if (attachmet.has("fileExtensionType")) {
				fileExtension = KloaderReuseUtil.checkNullorEmpty(attachmet.getString("fileExtensionType"));
			}
			if (attachmet.has("attachmentTitle")) {
				attachmentTitle = KloaderReuseUtil.checkNullorEmpty(attachmet.getString("attachmentTitle"));
			}

			if (null != cmsattachementName) {
				fileContent = parseAttachementContentUsingTika(cmsattachementName, path, fileExtension);
			}

			cmsattachementMap = new HashMap<String, String>();
			cmsattachementMap.put("s3ServerPath", getS3UrlPath);
			cmsattachementMap.put("filecontent", fileContent);
			cmsattachementMap.put("ATTACHMENT_TITLE", attachmentTitle);
			cmsattachementMap.put("type", fileType);

		}
		return cmsattachementMap;

	}

	private static String parseAttachementContentUsingTika(String attachementName, String path, String fileExtension) {
		log.info("Start of getTheAttachementPareseAttachementContent()..");
		String filecontent = null;
		String attachementPathLocation = null;
		Tika tika = null;
		if (attachementName != null) {
			if (null != path && path.length() > 0) {
				int endIndex = path.lastIndexOf(KloaderConstants.FILEPATHBRACES);
				if (endIndex != -1) {
					attachementPathLocation = path.substring(0, endIndex); // not forgot to put check
																			// if(endIndex !=
																			// -1)
					log.info("attachementPathLocation--:" + attachementPathLocation);
				}
			}

			try {
				File file = new File(attachementPathLocation + KloaderConstants.FILEPATHBRACES + attachementName + "."
						+ fileExtension);
				log.info("att-Path-Location+file- name::" + file.getName());

				// Instantiating Tika facade class
				if (file.getName() != null && !file.getName().isEmpty()) {
					tika = new Tika();
					filecontent = tika.parseToString(file);
				}
				log.info("filecontent prased..");

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return filecontent;
	}

}
