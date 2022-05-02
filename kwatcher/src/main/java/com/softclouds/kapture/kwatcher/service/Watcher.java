package com.softclouds.kapture.kwatcher.service;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

@Service
public interface Watcher {

	void processEvents();

	void registerPath(Path dir, boolean recursive) throws IOException;
}
