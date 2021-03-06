package com.softclouds.kapture.externalcrawling.util;

import java.util.Random;

import lombok.extern.slf4j.Slf4j;

/**
 * This CrawlingUtil class uses the reusing the methods
 * 
 * @author sivam
 *
 */
@Slf4j
public class CrawlingUtil {

	public static String getRandomNumberString() {
		// It will generate 4 digit random Number.
		// from 0 to 9999
		Random rnd = new Random();
		int number = rnd.nextInt(9999);

		// this will convert any number sequence into 6 character.
		log.info(String.format("%04d", number));

		return String.format("%04d", number);
	}

	public static String getCollectionPrefixName(String collectionName) {
		String input = collectionName; // input string
		String firstFourChars = ""; // substring containing first 4 characters
		if (input.length() > 3) {
			firstFourChars = input.substring(0, 3);
		} else {
			firstFourChars = input;
		}
		log.info(firstFourChars);
		return firstFourChars;
	}

	public static String findCategoryType(String extension) {
		String name = null;
		if (extension.equals("html")) {
			name = CrawlingConstants.WEB_TYPE;

		} else if (extension.equals("pdf")) {
			name = CrawlingConstants.PDF_TYPE;

		} else if (extension.equals("jpg")) {
			name = CrawlingConstants.IMAGE_TYPE;

		} else if (extension.equals("png")) {
			name = CrawlingConstants.IMAGE_TYPE;

		} else if (extension.equals("mp4")) {
			name = CrawlingConstants.VIDEO_TYPE;

		} else if (extension.equals("mp3")) {
			name = CrawlingConstants.AUDIO_TYPE;

		} else if (extension.equals("xls")) {
			name = CrawlingConstants.EXCEL_TYPE;

		} else if (extension.equals("word")) {
			name = CrawlingConstants.WORD_TYPE;
		}
		return name;
	}
}
