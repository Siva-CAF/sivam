package com.softclouds.kapture.kloader.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softclouds.kapture.kloader.ESConfig.RestHighLevelClientUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticSearchUtil {

	public static void deleteRecordsFromES(String indexName, String docu_id, String indexType) {
		DeleteByQueryRequest deleteRequest = null;
		BulkByScrollResponse bulkResponse = null;

		log.info("deleteRecordsFromES - indexName:: " + indexName);
		log.info("deleteRecordsFromES -docu_id :: " + docu_id);
		log.info("deleteRecordsFromES -indexType :: " + indexType);
		try {
			// Mention index name
			deleteRequest = new DeleteByQueryRequest(indexName);
			deleteRequest.setConflicts("proceed");
			deleteRequest.setBatchSize(100);
			deleteRequest.setRefresh(true);

			// Term Query to Delete the document. Note the field name and value in below
			// code

			// deleterequest.setQuery(new TermQueryBuilder("analyzedFields.", "ABC"));

			// Match Query to Delete the Document
			if (indexType.equalsIgnoreCase("MAIN_INDEX")) {
				deleteRequest.setQuery(QueryBuilders.matchQuery("analyzedFields.doctext.DOCUMENTID", docu_id));
			} else {
				deleteRequest.setQuery(QueryBuilders.matchQuery("analyzedFields.documentId", docu_id));
			}

			// Execute the request to delete based on above details
			bulkResponse = RestHighLevelClientUtil.getInstance().getRestClient().deleteByQuery(deleteRequest,
					RequestOptions.DEFAULT);
			// By this time your delete query got executed and you have the response with
			// you.
			long totalDocs = bulkResponse.getTotal();
			long deletedDocs = bulkResponse.getDeleted();
			// Print the response details
			log.info("Total Docs Processed :: " + totalDocs);
			log.info("Total Docs Deleted :: " + deletedDocs);

		} catch (Exception e) {
			log.error("Exception in deleteRecordsFromES():::" + e.getMessage());
		}

	}

	public static Boolean indexIsExistsOrNotInES(String indexName) {
		GetIndexRequest request = null;
		boolean exists = false;
		try {
			request = new GetIndexRequest(indexName);
			exists = RestHighLevelClientUtil.getInstance().getRestClient().indices().exists(request,
					RequestOptions.DEFAULT);
		} catch (Exception e) {
			log.info("Exception In indexIsExistsOrNotInES()::" + e.getMessage());
		}
		return exists;
	}

	public static JSONObject attachmentsContentIndexInElaticSearch(List<Map<String, String>> attachmentMap,
			JSONObject channelJSONObject, String docu_id, String artilcleTitle, String locale, String channelName) {
		log.info("Start of ElasticSearch Util:::attachmentsContentIndexInElaticSearch()::{}", channelName);
		List<Map<String, Object>> finalAttaMap = null;
		Map<String, Object> docMap = null;
		String uniqueID = null;
		String indexStartsWithName = "kapture";
		Map<String, Object> attachementsource = null;
		String attachementIndexName = null;
		Boolean isIndexExists = false;
		IndexRequest request = null;
		IndexResponse response = null;
		finalAttaMap = new ArrayList<>();
		try {
			for (int i = 0; i < attachmentMap.size(); i++) {

				JSONObject mapnew = new JSONObject(attachmentMap.get(i));

				// mapnew.put("ISCMSARTICLE", isCMSArticle);
				// mapnew.put("primaryLocale", locale);
				mapnew.put("title", artilcleTitle);
				// mapnew.put("type", channelName);
				// mapnew.put("createdDate", mapObj.get("createdDate"));

				mapnew.put("attachmentURLPath", mapnew.remove("s3ServerPath"));
				mapnew.put("attachementContent", mapnew.remove("filecontent"));
				mapnew.put("documentId", docu_id);
				// mapnew.put("articleId", docu_id);
				mapnew.put("ATTACHMENT_TITLE", mapnew.get("ATTACHMENT_TITLE"));

				// Start indexing the attachments Documents
				uniqueID = UUID.randomUUID().toString();
				attachementIndexName = indexStartsWithName + "." + channelName.toLowerCase() + "." + "attachments" + "."
						+ locale.toLowerCase();

				log.info("attachementIndexName", attachementIndexName);

				// delete existing attachment records form ES
				isIndexExists = indexIsExistsOrNotInES(attachementIndexName);
				if (Boolean.TRUE.equals(isIndexExists) && i == 0) {
					String attIndexType = "ATTA_INDEX";
					deleteRecordsFromES(attachementIndexName, docu_id, attIndexType);
				}

				attachementsource = new HashMap<>();
				attachementsource.put("analyzedFields", new ObjectMapper().readValue(mapnew.toString(), Map.class));

				request = new IndexRequest().index(attachementIndexName).id(uniqueID).source(attachementsource);

				try {
					response = RestHighLevelClientUtil.getInstance().getRestClient().index(request,
							RequestOptions.DEFAULT);

					log.info("Attachments document index _id::" + response.getId());

				} catch (Exception e) {
					log.error("Exception in failed to index the attachament Document", e);
				}

				docMap = new HashMap<>();
				Map<String, String> finalmappp = new ObjectMapper().readValue(mapnew.toString(), Map.class);
				docMap.putAll(finalmappp);

				docMap.remove("attachementContent");
				docMap.remove("locale");
				docMap.remove("documentId");

				docMap.put("attachmentId", uniqueID);
				finalAttaMap.add(docMap);

			}

			channelJSONObject.put("attachements_meta", finalAttaMap);

		} catch (Exception e) {
			log.error("Error while index the attachement document in ES::" + e.getMessage());
		}
		return channelJSONObject;
	}

}
