package com.softclouds.kapture.kloader.service;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.softclouds.kapture.kloader.bo.ImTransaction;
import com.softclouds.kapture.kloader.bo.ImTransactionProcessed;
import com.softclouds.kapture.kloader.repository.LoaderRepository;
import com.softclouds.kapture.kloader.repository.WatcherRepository;
import com.softclouds.kapture.kloader.util.KloaderReuseUtil;
import com.softclouds.kapture.kloader.util.LoaderUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This class defines the get the XML files and index the article content in ES
 * 
 * @author sivam
 *
 */
@Slf4j
@Component
@Transactional
public class LoaderServiceImpl implements ILoaderService {

	@Autowired
	WatcherRepository watcherRepository;

	@Autowired
	LoaderRepository loaderRepository;

	/**
	 * Process the events
	 */
	@Override
	public void processEvents() {
		log.info("Start of Process Events::");
		String prevPath = "";
		String path = null;
		int id = 0;
		String event = null;
		BigInteger bigid;
		try {
			List<ImTransaction> imTransactionsList = watcherRepository.findAll();
			if (null != imTransactionsList && imTransactionsList.size() > 0) {
				for (ImTransaction imTransaction : imTransactionsList) {
					event = imTransaction.getEvent();
					path = imTransaction.getFullPath();
					bigid = imTransaction.getId();
					id = bigid.intValue();

					if (prevPath.contains(path.replace("~secure", ""))) {
						// Redundant path that has already been processed.
						log.info("Skipping all ready processed path: " + path);
						moveToProcessedTable(String.valueOf(id));
					} else {
						processFile(event, String.valueOf(id), path);
					}
					prevPath = path;
				}
			}
		} catch (Exception e) {
			log.error("LoaderServiceImpl.processEvents()::Error while find the the articles XML oaths::"
					+ e.getMessage());
		}
	}

	/**
	 * Process the XML file and Save to ES
	 * 
	 * @param event
	 * @param id
	 * @param pathStr
	 */
	private void processFile(String event, String id, String pathStr) {
		log.info("Start of processfile:::{}", event, id, pathStr);
		String ESID = null;
		try {
			if (event.equals("CREATE_DIR")) {
				pathStr = LoaderUtil.getPathWithFile(pathStr);
			}

			Path path = Paths.get(pathStr);
			boolean isRegularFile = Files.isRegularFile(path);
			log.info("isRegular file:::" + isRegularFile);

			isRegularFile = true;
			if (isRegularFile) {
				log.info("Is a regular file");
				ESID = LoaderUtil.saveToES(pathStr, id);

			} else {
				// XML file does not exist. Remove the corresponding document from
				// Elasticsearch.
				log.info("Is not a regular file");
				// removeFromES(getDocInfoFromPath(pathStr));
			}

			// Move the corresponding record from the MySQL im_transaction table to the
			// im_transaction_processed table.
			log.info("before move to im_transaction_processed table." + String.valueOf(id));
			if (ESID != null && !ESID.isEmpty()) {
				moveToProcessedTable(String.valueOf(id));
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error("LoaderServiceImpl::processFile():: Error while process a file" + e.getMessage());
		}

	}

	/**
	 * Once Done the XML Process that file moved to ImTransactionProcessed table and
	 * delete in ImTransaction table
	 * 
	 * @param id
	 */
	private void moveToProcessedTable(String id) {
		ImTransaction fileDetails = watcherRepository.getById(BigInteger.valueOf(Integer.parseInt(id)));
		ImTransactionProcessed processed = null;
		if (fileDetails != null) {
			processed = new ImTransactionProcessed();
			processed.setEvent(fileDetails.getEvent());
			processed.setFullPath(fileDetails.getFullPath());
			processed.setProcessedTS(KloaderReuseUtil.getCurrentDateTime());
			processed.setPublishedTS(fileDetails.getPublishedTs());
			processed = loaderRepository.save(processed);
			if (processed.getId() != null) {
				log.info("Moved to im_transaction_processed table.");
				watcherRepository.deleteById(BigInteger.valueOf(Integer.parseInt(id)));
				log.info(" Deleted Record in im_transaction table.");
			}
		}

	}

}
