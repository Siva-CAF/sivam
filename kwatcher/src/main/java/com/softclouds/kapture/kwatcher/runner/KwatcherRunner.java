package com.softclouds.kapture.kwatcher.runner;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.softclouds.kapture.kwatcher.service.Watcher;
import com.softclouds.kapture.kwatcher.util.KwatcherUtility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KwatcherRunner implements CommandLineRunner {

	@Autowired
	Watcher watcherService;

	@Override
	public void run(String... args) throws Exception {
		boolean recursive = false;
		Path dir = null;

		String watchDir = KwatcherUtility.getProperty("kwatcher.watchDir");
		String recursiveStr = KwatcherUtility.getProperty("kwatcher.recursive");

		log.info("WatchDIR::: > " + watchDir + " :: " + "recursiveStr :::" + recursiveStr);
		if (recursiveStr.equals("true")) {
			recursive = true;
		}
		// register directory and process its events
		dir = Paths.get(watchDir);
		try {
			watcherService.registerPath(dir, recursive);
			watcherService.processEvents();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
