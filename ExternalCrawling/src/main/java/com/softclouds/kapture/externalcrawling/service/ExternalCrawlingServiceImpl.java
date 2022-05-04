package com.softclouds.kapture.externalcrawling.service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softclouds.kapture.externalcrawling.bo.ExternalCrawlingContent;
import com.softclouds.kapture.externalcrawling.bo.WebContentURLs;
import com.softclouds.kapture.externalcrawling.config.RestHighLevelClientUtil;
import com.softclouds.kapture.externalcrawling.helper.CrawlingHelper;
import com.softclouds.kapture.externalcrawling.modal.URLContentModal;
import com.softclouds.kapture.externalcrawling.modal.WebContent;
import com.softclouds.kapture.externalcrawling.modal.WebContentCategroies;
import com.softclouds.kapture.externalcrawling.modal.WebContentUserGroups;
import com.softclouds.kapture.externalcrawling.repository.ExternalCrawlingRepository;
import com.softclouds.kapture.externalcrawling.repository.WebContentURLsRepository;
import com.softclouds.kapture.externalcrawling.util.CrawlingConstants;
import com.softclouds.kapture.externalcrawling.util.CrawlingUtil;
import com.softclouds.kapture.externalcrawling.util.DateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * This class defines the Web content crawling and index into Elastic search
 * 
 * @author sivam
 *
 */
@Component
@Service
@Slf4j
@Transactional
public class ExternalCrawlingServiceImpl implements ExternalCrawlingService {

	@Autowired
	RestHighLevelClient client;

	@Autowired
	WebContentURLsRepository webContentURLsRepository;

	@Autowired
	ExternalCrawlingRepository externalCrwalingRepository;

	@Autowired
	CrawlingHelper crawlingHelper;

	@Override
	public List<WebContentURLs> fetchWebURLS() {
		log.info("Start of WebCrawlingServiceImpl::fetchWebURLS ()");
		Boolean flag = false;
		List<WebContentURLs> webcontentUrls = webContentURLsRepository.findAllByUrlContentCrawlingStatus(flag);
		return webcontentUrls;
	}

	@Override
	public Optional<ExternalCrawlingContent> findByExternalCrawlingDetails(Integer collectionCrawlId) {
		log.info("Start of WebCrawlingServiceImpl::findByExternalCrawlingDetails {}", collectionCrawlId);
		Optional<ExternalCrawlingContent> externalCrawlingDetails = externalCrwalingRepository
				.findById(collectionCrawlId);
		return externalCrawlingDetails;
	}

	/**
	 * Index the web content in elastic search based on collection Name and locale
	 * index pattern
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void readContentFromWebURLs(WebContentURLs webUrLs, ExternalCrawlingContent externalCrawling) {
		log.info("Start of WebCrawlingServiceImpl::readContentFromWebURLs()");

		String contentURL = null;
		String extension = null;
		URL url = null;
		URLContentModal urlContent = null;
		String webIndexName = null;
		String collectionName = null;
		String locale = null;
		String firstFourChars = null;
		String primaryLocale = null;
		Map<String, Object> finalWebMap = null;
		Map<String, String> docdatesMap = null;
		WebContent webContent = null;
		List<WebContentCategroies> contentListCategories = new ArrayList<>();
		List<WebContentUserGroups> contentListUsergroups = new ArrayList<>();
		WebContentCategroies webCategroies = null;
		WebContentUserGroups webContentUserGroups = null;
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> collectionObject = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, Object> articleData = null;
		Map<String, Object> source = null;
		try {

			contentURL = webUrLs.getStartingPointURLs();
			url = new URL(contentURL);
			extension = FilenameUtils.getExtension(url.getPath());

			urlContent = crawlingHelper.connetToURLAndParseContent(contentURL);

			collectionName = externalCrawling.getCollectionName().replaceAll(" ", "_").toLowerCase();
			locale = externalCrawling.getLocale().toLowerCase();
			primaryLocale = externalCrawling.getLocale();

			firstFourChars = CrawlingUtil.getCollectionPrefixName(collectionName);
			log.info("Prefix-of-Collecton Name::{}", firstFourChars);

			String randomId = CrawlingUtil.getRandomNumberString();

			String documentId = firstFourChars.concat(randomId);
			log.info("DocumentId -- Web ::{}", documentId);

			// Web Categories setting
			webCategroies = new WebContentCategroies();
			if (extension != null && !extension.isEmpty()) {
				webCategroies.setCategoryName(CrawlingUtil.findCategoryType(extension));
				webCategroies.setCategoryRefKey(CrawlingConstants.WEB_CATEGORY_TYPE);
				contentListCategories.add(webCategroies);
			}
			if (collectionName != null && !collectionName.isEmpty()) {
				webCategroies.setCategoryName(collectionName);
				webCategroies.setCategoryRefKey(collectionName + randomId + "_KEY");
				contentListCategories.add(webCategroies);
			}

			// Web content User groups
			webContentUserGroups = new WebContentUserGroups();
			webContentUserGroups.setUserGroupName(CrawlingConstants.USER_GROUP_NAME);
			webCategroies.setCategoryRefKey(CrawlingConstants.USER_GROUP_REF_KEY);
			contentListUsergroups.add(webContentUserGroups);

			webContent = new WebContent();
			webContent.setTitle(urlContent.getTitle());
			webContent.setArticleId(documentId);
			webContent.setDOCUMENTID(documentId);
			webContent.setPrimaryLocale(primaryLocale);
			webContent.setType(collectionName.toUpperCase());
			webContent.setStatus(CrawlingConstants.STATUS);
			webContent.setArticleState(CrawlingConstants.ARTICLESTATE);
			webContent.setCreatedDate(
					DateUtils.getDate(primaryLocale, CrawlingConstants.CREATEDDATE, CrawlingConstants.TYPE));

			
			webContent.setArticlelistcategory(contentListCategories);
			webContent.setArticlelistusergroup(contentListUsergroups);
			
			HashMap<String,Object> channelMap=mapper.readValue(mapper.writeValueAsString(urlContent), HashMap.class);
			collectionObject.put(collectionName.toUpperCase(), channelMap);

			articleData = mapper.readValue(mapper.writeValueAsString(webContent), HashMap.class);
			map.putAll(articleData);
			map.putAll(collectionObject);

			// Setting doc dates Map
			docdatesMap = new HashMap<String, String>();
			docdatesMap.put("createdDate",
					DateUtils.getDate(primaryLocale, CrawlingConstants.CREATEDDATE, CrawlingConstants.TYPE));

			finalWebMap = new HashMap<>();
			finalWebMap.put("docdates", docdatesMap);
			finalWebMap.put("doctext", map);

			source = new HashMap<String, Object>();
			source.put("analyzedFields", finalWebMap);

			webIndexName = CrawlingConstants.WEB_INDEX_START_PREFIX + collectionName + "." + locale;
			log.info("Web Crawling -IndexName {}", webIndexName);

			String ESID = crawlingHelper.webContentIndexInElasticSearch(source, webIndexName);
			if (ESID != null && !ESID.isEmpty()) {

				// update URL record status in web content table as TRUE
				updateWebContentURLRecordStatus(extension, urlContent.getTitle(), webUrLs);

				// update index document count and raw size in External crawling content table
				updateDocumentsCountAndSizeInExternalCrawlingTable(webIndexName, externalCrawling, locale);
			}

		} catch (Exception e) {
			log.error("WebCrawlingServiceImpl::readContentFromWebURLs::error while read the content in URls"
					+ e.getMessage());
		}

	}

	/**
	 * Update web content URLs record status is indexed or not in ES and status will
	 * update in Web content URLs table
	 * 
	 * @param extension
	 * @param title
	 * @param webUrLs
	 */
	private void updateWebContentURLRecordStatus(String extension, String title, WebContentURLs webUrLs) {
		log.info("Start ExternalCrawlingServiceImpl in updateWebContentURLRecordStatus method {} {} {}", extension, title, webUrLs);
		String encoding = null;
		String documentType = null;
		try {

			switch (extension) {
			case "html":
				encoding = CrawlingConstants.HTML_ENCODING;
				documentType = CrawlingConstants.HTML_DOCUMENT_TYPE;
				break;
			case "pdf":
				encoding = CrawlingConstants.PDF_ENCODING;
				documentType = CrawlingConstants.PDF_DOCUMENT_TYPE;
				break;

			default:
				encoding = CrawlingConstants.HTML_ENCODING;
				documentType = CrawlingConstants.HTML_DOCUMENT_TYPE;
				break;
			}
			WebContentURLs dbWebUrls = null;
			if (null != webUrLs.getId() && webUrLs.getCrawlingId() != null) {
				dbWebUrls = webContentURLsRepository.findByIdAndCrawlingId(webUrLs.getId(), webUrLs.getCrawlingId());
				dbWebUrls.setUrlContentCrawlingStatus(true);
				dbWebUrls.setDocumentType(documentType);
				dbWebUrls.setEncoding(encoding);
				dbWebUrls.setTitle(title);
				webContentURLsRepository.save(dbWebUrls);
			}

		} catch (Exception e) {
			log.error(
					"WebCrawlingServiceImpl::updateWebContentURLRecordStatus:: error while updating the urls status::",
					e.getMessage());
		}
		log.info("End ExternalCrawlingServiceImpl in updateWebContentURLRecordStatus method");
	}

	private void updateDocumentsCountAndSizeInExternalCrawlingTable(String collectionName,
			ExternalCrawlingContent externalCrawling, String locale) throws IOException {
		log.info("Start ExternalCrawlingServiceImpl in updateDocumentsCountAndSizeInExternalCrawlingTable method {} {} {}", collectionName,
				externalCrawling, locale);
		try {
			Request request = new Request("GET", collectionName + "/_stats");
			Response resp = RestHighLevelClientUtil.getInstance().getRestClient().getLowLevelClient()
					.performRequest(request);
			JsonNode body = new ObjectMapper().readTree(resp.getEntity().getContent());
			JsonNode size = body.get("indices").get(collectionName).get("primaries").get("store").get("size_in_bytes");
			log.info("Size In bytes:::" + size);
			JsonNode count = body.get("indices").get(collectionName).get("primaries").get("docs").get("count");
			log.info("count::" + count);

			Optional<ExternalCrawlingContent> crawlingContent = externalCrwalingRepository.findById(externalCrawling.getId());

			if (crawlingContent.isPresent()) {
				ExternalCrawlingContent content = crawlingContent.get();
				content.setNumberOfDocuments(count.asLong());
				content.setRawSize(size.asLong());
				externalCrwalingRepository.save(content);
			}
		} catch (Exception e) {
			log.error(
					"WebCrawlingServiceImpl::updateDocumentsCountAndSizeInExternalCrawlingTable::error whil update raz size and number of documents count in ES::"
							+ e.getMessage());
		}
		log.info("End ExternalCrawlingServiceImpl in updateDocumentsCountAndSizeInExternalCrawlingTable method");
	}
}
