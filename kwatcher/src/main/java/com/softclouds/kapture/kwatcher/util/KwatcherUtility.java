package com.softclouds.kapture.kwatcher.util;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Properties;

import com.softclouds.kapture.kwatcher.service.WatcherImpl;

public class KwatcherUtility {
	private static Properties properties = null;

	public static Properties getProperties() throws IOException {
		if (properties == null) {
			properties = new Properties();
			properties.load(
					WatcherImpl.class.getClassLoader().getResourceAsStream(KwatcherConstants.WATCHER_PROPERTIES));
		}
		return properties;
	}

	public static String getProperty(String key) throws IOException {
		return getProperties().getProperty(key);
	}

	public static int getIntProperty(String key) throws IOException {
		String value = getProperties().getProperty(key);
		return Integer.valueOf(value);
	}

	public static Timestamp getCurrentDateTime() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return timestamp;
	}
}
