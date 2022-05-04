package com.softclouds.kapture.kloader.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.json.JSONObject;
import org.json.XML;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softclouds.kapture.kloader.ESConfig.RestHighLevelClientUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoaderUtil {

	public static String getPathWithFile(String pathStr) {
		// pathStr will be like
		// /data/softclouds/unfoldlabs/kapture/data/live_test/SOLUTIONS/100000/SOL100001/en_US/~secure
		String path = pathStr.replace("~secure", "");
		File folder = new File(path);
		File[] files = folder.listFiles();
		String filename = null;

		for (int i = 0; i < files.length; i++) {
			filename = files[i].getName();
			log.info("filename:::" + filename);
			if (filename.endsWith(".xml") || filename.endsWith(".XML")) {
				path = path + filename;
				break;
			}
		}

		return path;
	}

	/**
	 * Fetch the XML file and Parse the content then article content index in ES
	 * (kapture.channelname.locale)
	 * 
	 * @param path
	 * @param id
	 * @return
	 */

	public static String saveToES(String path, String id) {
		log.info("Start of LoaderUtil::SaveTOES {} ", path);

		JSONObject channelJSONObject = null;
		JSONObject attachments = null;
		String attachmentName = null;
		String locale = null;
		String channelName = null;
		String docu_id = null;
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, String>> attachmentMap = null;
		Map<String, String> attMappp = null;

		String uniqueID = null;
		String indexStartsWithName = KloaderConstants.INDEXSTARTSNAME;
		String type = KloaderConstants.INDEXTYPE;
		IndexRequest request = null;
		IndexResponse response = null;
		Map<String, Object> source = null;
		Map<String, Object> attachementsource = null;
		String attachementIndexName = null;
		List<Map<String, Object>> finalAttaMap = null;
		Map<String, Object> docMap = null;

		Map<String, Object> finalDocObj = null;

		Boolean isCMSArticle = false;
		Boolean isIndexExists = false;
		Boolean moveFlag = false;
		String artilcleTitle = null;
		JSONObject json = null;
		JSONObject mapObj = null;
		Map<String, String> map = null;
		// docdates
		String expireDt = null;
		String reviewDt = null;
		String createdDt = null;
		try {

			// Get the document ID, type (IM channel), and locale from the path
			// String[] docInfo = getDocInfoFromPath(path);
			log.info("***Before Converting XML to JSON ***");
			FileInputStream is = new FileInputStream(new File(path));
			String xml = IOUtils.toString(is);
			json = XML.toJSONObject(xml);
			// log.info(json.toString(2));
			log.info("*** After Converted XML to JSON ***");

			if (json != JSONObject.NULL) {
				map = mapper.readValue(json.toString(2), Map.class);
				mapObj = new JSONObject(map).getJSONObject("content");
			}

			if (mapObj.has("DOCUMENTID")) {
				docu_id = (String) mapObj.get("DOCUMENTID");
			}

			log.info(docu_id, "DOCUMENTID");

			if (mapObj.has("title")) {
				artilcleTitle = (String) mapObj.get("title");
			}

			String transLocale = null;
			if (mapObj.has("translateLocale")) {
				transLocale = (String) mapObj.get("translateLocale");
				if (transLocale != null && !transLocale.isEmpty()) {
					locale = transLocale;
				}

			}

			if (transLocale == null) {
				if (mapObj.has("primaryLocale")) {
					locale = (String) mapObj.get("primaryLocale");
				}
			}

			if (mapObj.has("type")) {
				channelName = mapObj.getString("type").toUpperCase();
			}

			if (mapObj.has("TYPE")) {
				channelName = mapObj.getString("TYPE");
			}
			if (mapObj.has("LOCALECODE")) {
				locale = (String) mapObj.get("LOCALECODE");
			}

			if (mapObj.has("ISCMSARTICLE")) {
				isCMSArticle = mapObj.getBoolean("ISCMSARTICLE");
			}

			if (mapObj.has("expireDate")) {
				expireDt = (String) mapObj.get("expireDate");
			}
			if (mapObj.has("reviewDate")) {
				reviewDt = (String) mapObj.get("reviewDate");
			}
			if (mapObj.has("createdDate")) {
				createdDt = (String) mapObj.get("createdDate");
			}
			Map<String, String> docdatesMap = new HashMap<String, String>();
			docdatesMap.put("expireDate", expireDt);
			docdatesMap.put("reviewDate", reviewDt);
			docdatesMap.put("createdDate", createdDt);

			// JSONObject docdatesObj = new JSONObject(docdatesMap);

			if (channelName != null && !channelName.isEmpty()) {
				channelJSONObject = mapObj.getJSONObject(channelName);
				// ATTACHMENTS code Need to Implements
				if (channelJSONObject.has("ATTACHMENTS")) {
					Object attachements = channelJSONObject.opt("ATTACHMENTS");
					if (attachements != null) {
						JSONObject channelUpdatedJSONObject = AttachmentsUtil.processAttachements(channelJSONObject,
								path, docu_id, artilcleTitle, locale, channelName);

						if (mapObj.has(channelName) && channelUpdatedJSONObject != null) {
							mapObj.put(channelName, channelJSONObject);
						}
					}
				}
			}

			uniqueID = UUID.randomUUID().toString();

			log.info("uniqueID- Main channel" + uniqueID);

			String indexName = indexStartsWithName + "." + channelName.toLowerCase() + "." + locale.toLowerCase();
			log.info("Main channel indexName::::" + indexName);

			// delete existing Documents records form ES
			isIndexExists = ElasticSearchUtil.indexIsExistsOrNotInES(indexName);

			log.info("Main channel- indexIsExistsOrNotInES  >>>::" + isIndexExists);
			if (Boolean.TRUE.equals(isIndexExists)) {
				// delete if record exists in ES
				String indexType = "MAIN_INDEX";
				ElasticSearchUtil.deleteRecordsFromES(indexName, docu_id, indexType);
			}

			finalDocObj = new HashMap<String, Object>();
			finalDocObj.put("docdates", docdatesMap);
			finalDocObj.put("doctext", new ObjectMapper().readValue(mapObj.toString(), Map.class));

			// New record/document indexing in the Main index
			source = new HashMap<>();

			source.put("analyzedFields", finalDocObj);
			// new JSONObject(source);

			request = new IndexRequest().index(indexName).id(uniqueID).source(source);

			try {
				response = RestHighLevelClientUtil.getInstance().getRestClient().index(request, RequestOptions.DEFAULT);

				// log.info("IndexResponse FROM ES =>> " + response.status());

				log.info("response::" + response.getIndex() + "::::::" + response.getId());

				if (response.getId() != null && !response.getId().isEmpty()) {
					/*
					 * boolean checkFlag = false; checkFlag = beforeCheckDocExistsOrnot(docu_id);
					 */

				}

			} catch (Exception e) {
				e.printStackTrace();
				log.error("LoaderUtil.SaveToES()::Error while to index the Document", e);
			}

		} catch (Exception e) {
			log.error("Exception in failed to index the Document", e);
		}
		// log.info("END of LoaderUtil::SaveTOES {} ", response.getId());
		return response.getId();
	}

}
