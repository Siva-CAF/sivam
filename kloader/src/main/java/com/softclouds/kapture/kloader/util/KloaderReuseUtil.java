package com.softclouds.kapture.kloader.util;

import java.io.File;
import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KloaderReuseUtil {

	public static KloaderReuseUtil kapiUtils;

	private KloaderReuseUtil() {

	}

	public static KloaderReuseUtil getInstance() {
		log.info("getInstance()");
		if (kapiUtils == null) {
			kapiUtils = new KloaderReuseUtil();
		}
		return kapiUtils;
	}

	public String convertNullOrEmptyToString(Object input) {
		if (null != input) {
			return input.toString();
		} else {
			return "";
		}
	}

	/*
	 * public BigInteger convertNullOrEmptyToBigDecimal(Object input) { if (null !=
	 * input) { return BigInte } else { return 0.00; } }
	 */
	public static Timestamp getCurrentDateTime() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return timestamp;
	}

	public static String checkNullorEmpty(String inputStr) {
		if (null == inputStr || inputStr.isEmpty() || inputStr == "null") {
			return "";
		}
		return inputStr;
	}

	public static String getFileExtension(File file) {
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		else
			return "";
	}

}
