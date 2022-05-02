package com.softclouds.kapture.externalcrawling.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;

import lombok.extern.slf4j.Slf4j;

/**
 * Date convert reusing Method
 *
 * @author Siva M
 */

@Slf4j
public class DateUtils {

    /**
     * Convert String to Date
     *
     * @param stringData
     * @return formattedTime
     * @throws ParseException
     */
    public static String convertStringToDate(String stringData) throws ParseException {
        String formattedTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat(CrawlingConstants.SIMPLEDATEFORMAT);// yyyy-MM-dd'T'HH:mm:ss
        // HH:mm:ss.SSS
        SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        if (stringData != null && !stringData.isEmpty()) {
            Date data = sdf.parse(stringData.trim());
            formattedTime = output.format(data);
        }
        return formattedTime;
    }

    /**
     * Convert string to ESMXDate  (other locale Format)
     *
     * @param stringData
     * @return formattedTime
     * @throws ParseException
     */
    public static String convertStringToESMXDate(String stringData) throws ParseException {
        String formattedTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat(CrawlingConstants.SIMPLEDATEFORMAT);// yyyy-MM-dd'T'HH:mm:ss
        SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        if (stringData != null && !stringData.isEmpty()) {
            Date data = sdf.parse(stringData.trim());
            formattedTime = output.format(data);
        }
        return formattedTime;
    }

    /**
     * Convert String to MCDate(JA_JP) format
     *
     * @param stringData
     * @return formattedTime
     * @throws ParseException
     */
    public static String convertStringToMCDate(String stringData) throws ParseException {
        String formattedTime = "";
        // 2020-03-19 07:00:00
        SimpleDateFormat sdf = new SimpleDateFormat(CrawlingConstants.SIMPLEDATEFORMAT);// yyyy-MM-dd'T'HH:mm:ss
        SimpleDateFormat output = new SimpleDateFormat(CrawlingConstants.SIMPLEDATEFORMAT);
        if (stringData != null && !stringData.isEmpty()) {
            Date data = sdf.parse(stringData.trim());
            formattedTime = output.format(data);
        }
        return formattedTime;
    }

    /**
     * Generate current date and time for EN_US
     *
     * @return formattedTime
     */
    public static String currentDateTime() {
        String formattedTime = "";
        SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        output.setTimeZone(TimeZone.getTimeZone("GMT-7"));
        formattedTime = output.format(new Date());
        log.info("formattedTime :currentDateTime " + formattedTime);

        return formattedTime;
    }

    /**
     * Generate current date and time other locales
     *
     * @return formattedTime
     */
    public static String currentDateTimeMME() {
        String formattedTime = "";
        SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        output.setTimeZone(TimeZone.getDefault());
        formattedTime = output.format(new Date());
        log.info("formattedTime : currentDateTimeMME" + formattedTime);

        return formattedTime;

    }

    /**
     * Generate current date and time in JA_JP locale
     *
     * @return
     */
    public static String currentDateTimeMC() {
        String formattedTime = "";
        SimpleDateFormat output = new SimpleDateFormat(CrawlingConstants.SIMPLEDATEFORMAT);
        output.setTimeZone(TimeZone.getDefault());
        formattedTime = output.format(new Date());
        log.info("formattedTime : currentDateTimeMC" + formattedTime);

        return formattedTime;
    }

    /**
     * Get year
     *
     * @param stringData
     * @return
     * @throws ParseException
     */
    public static String getYear(String stringData) throws ParseException {
        String formattedTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat(CrawlingConstants.SIMPLEDATEFORMAT);// yyyy-MM-dd'T'HH:mm:ss
        SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy");
        if (stringData != null && !stringData.isEmpty()) {
            Date date = sdf.parse(stringData.trim());
            formattedTime = output.format(date);
        }
        String[] dateParts = formattedTime.split("/");
        String day = dateParts[0];
        String month = dateParts[1];
        String year = dateParts[2];
        log.info("DAY:::" + day + "MONTH::" + month + "YEAR::" + year);
        return year;
    }

    public static String getDateForMMERevised(String stringData) throws ParseException {
        String formattedTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// yyyy-MM-dd
        SimpleDateFormat output = new SimpleDateFormat("MMM dd, yyyy");
        if (stringData != null && !stringData.isEmpty()) {
            Date data = sdf.parse(stringData.trim());
            formattedTime = output.format(data);
        }
        return formattedTime;
    }

    public static Timestamp jodaToSQLTimestamp(DateTime localDateTime) {
        return new Timestamp(localDateTime.toDateTime().getMillis());
    }

    /**
     * Get the date based on locale
     *
     * @param primaryLocale
     * @param date
     * @param type
     * @return
     * @throws ParseException
     */
    public static String getDate(String primaryLocale, String date, String type) throws ParseException {
        String convertDate = null;
        if (primaryLocale != null && !primaryLocale.isEmpty()) {
            if (primaryLocale.equals(CrawlingConstants.EN_US_LOCALE)) {
                if (type.equals(CrawlingConstants.TYPE)) {
                    convertDate = DateUtils.currentDateTime();
                } else {
                    convertDate = DateUtils.convertStringToDate(date);
                }

            } else if (primaryLocale.equals(CrawlingConstants.JA_JP_LOCALE)) {
                if (type.equals(CrawlingConstants.TYPE)) {
                    convertDate = DateUtils.currentDateTimeMC();
                } else {
                    convertDate = DateUtils.convertStringToMCDate(date);
                }

            } else {
                if (type.equals(CrawlingConstants.TYPE)) {
                    convertDate = DateUtils.currentDateTimeMME();
                } else {
                    convertDate = DateUtils.convertStringToESMXDate(date);
                }

            }

        }
        return convertDate;
    }
}
