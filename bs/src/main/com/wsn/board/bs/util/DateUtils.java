package com.wsn.board.bs.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
	public static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
	public static final String ISO_DATE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public static final String LEXICOGRAPHIC_DATE = "yyyyMMddHHmmssSSS";
	public static final String LEXICOGRAPHIC_DATE_2 = "yyyy-MM-dd HH:mm:ss.SSS";

	public static final String READABLE_TIMESTAMP = "yyyy-MM-dd 'at' HH:mm:ss";
	public static final String BACKUP_FOLDER_TIMESTAMP = "yyyy_MM_dd_HHmmss";

	public static Date strToDate(String str, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINESE);
		Date date = null;
		try {
			date = format.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static String formatDateToString(Date date, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINESE);
		String str = null;
		str = format.format(date);
		return str;
	}
}