package com.softclouds.kapture.kwatcher.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.softclouds.kapture.kwatcher.bo.ImTransaction;
import com.softclouds.kapture.kwatcher.repository.WatcherRepository;
import com.softclouds.kapture.kwatcher.util.KwatcherUtility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WatcherImpl implements Watcher {

	@Autowired
	private WatcherRepository repository;

	private WatchService watcher = null;
	private Map<WatchKey, Path> keys = null;
	private boolean recursive = true;
	private boolean trace = false;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, StandardWatchEventKinds.OVERFLOW, StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				log.info("register: {} ", dir);
				String path = dir.toString();
				if (path.indexOf("~secure") > 0) {
					log.info("register method::: Before saveEvent()::");
					
					saveEvent("CREATE_DIR", path);
				}
			} else {
				if (!dir.equals(prev)) {
					log.info("update: " + prev + "->" + dir);
				}
			}
		}
		keys.put(key, dir);
	}

	private void saveEvent(String event, String path) {
		ImTransaction transaction = new ImTransaction();
		transaction.setEvent(event);
		transaction.setFullPath(path);
		transaction.setPublishedTs(Timestamp.from(Instant.now()));
		repository.save(transaction);

	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		log.info("Before Walk file Tree:: Read recursively- preVisitDirectory()::");
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		log.info("After Walk file Tree:: Read recursively- preVisitDirectory()::");
	}

	/**
	 * Creates a WatchService and registers the given directory
	 */

	public void registerPath(Path dir, boolean recursive) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		this.recursive = recursive;

		if (recursive) {
			log.info("Scanning " + dir + "\n");
			registerAll(dir);
			log.info("Done.");
		} else {
			register(dir);
		}
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	public void processEvents() {
		for (;;) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {

				String watchDir = null;
				try {
					watchDir = KwatcherUtility.getProperty("kwatcher.watchDir");
					log.info("before  while Fail Watcher Service Alteration Monitor");
					// whileFailWatcherServiceAlterationMonitor(Paths.get(watchDir));
				} catch (IOException e) {
					e.printStackTrace();
				}

				// eventFailureEmailSent("WAIT_FOR_KEY_EVENT", "PATH_EMPTY", x.getMessage());
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				log.error("WatchKey not recognized: " + key.toString());
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();

				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);
				String childPath = child.toString();

				if (childPath.indexOf(".xml") > 0) {

					log.info("EVENT- NAME::" + event.kind().name());
					
					saveEvent(event.kind().name(), childPath);
				}

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (recursive && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
					try {
						if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
							registerAll(child);
						}
					} catch (IOException ex) {
						log.error(ex.getMessage());
						// eventFailureEmailSent("RECURSIVELY_READ_FAIL_DIR", "REGISTER_PATH_FAIL",
						// ex.getMessage());
					}
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

}
