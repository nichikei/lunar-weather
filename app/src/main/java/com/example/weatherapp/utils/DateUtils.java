package com.example.weatherapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class để xử lý ngày tháng và thời gian
 */
public class DateUtils {

    // Common date formats
    public static final String FORMAT_DATE_TIME = "dd/MM/yyyy HH:mm";
    public static final String FORMAT_DATE = "dd/MM/yyyy";
    public static final String FORMAT_TIME = "HH:mm";
    public static final String FORMAT_TIME_12H = "hh:mm a";
    public static final String FORMAT_DAY_NAME = "EEEE";
    public static final String FORMAT_DAY_SHORT = "EEE";
    public static final String FORMAT_MONTH_YEAR = "MMMM yyyy";

    /**
     * Chuyển đổi timestamp (seconds) thành Date
     */
    public static Date timestampToDate(long timestamp) {
        return new Date(timestamp * 1000L);
    }

    /**
     * Chuyển đổi timestamp (milliseconds) thành Date
     */
    public static Date millisToDate(long millis) {
        return new Date(millis);
    }

    /**
     * Format Date thành String với pattern tùy chỉnh
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Format timestamp (seconds) thành String
     */
    public static String formatTimestamp(long timestamp, String pattern) {
        Date date = timestampToDate(timestamp);
        return formatDate(date, pattern);
    }

    /**
     * Lấy tên ngày trong tuần từ timestamp
     */
    public static String getDayName(long timestamp) {
        return formatTimestamp(timestamp, FORMAT_DAY_NAME);
    }

    /**
     * Lấy tên ngày trong tuần viết tắt
     */
    public static String getDayShortName(long timestamp) {
        return formatTimestamp(timestamp, FORMAT_DAY_SHORT);
    }

    /**
     * Lấy giờ từ timestamp (format 24h)
     */
    public static String getHour(long timestamp) {
        return formatTimestamp(timestamp, FORMAT_TIME);
    }

    /**
     * Lấy giờ từ timestamp (format 12h)
     */
    public static String getHour12(long timestamp) {
        return formatTimestamp(timestamp, FORMAT_TIME_12H);
    }

    /**
     * Kiểm tra timestamp có phải là hôm nay không
     */
    public static boolean isToday(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp * 1000L);

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Kiểm tra timestamp có phải là ngày mai không
     */
    public static boolean isTomorrow(long timestamp) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp * 1000L);

        return tomorrow.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               tomorrow.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Lấy thời gian hiện tại dạng timestamp (seconds)
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * Lấy thời gian hiện tại dạng Date
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * Parse string thành Date
     */
    public static Date parseDate(String dateString, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tính khoảng thời gian giữa 2 timestamp (seconds)
     */
    public static long getDifferenceInSeconds(long timestamp1, long timestamp2) {
        return Math.abs(timestamp1 - timestamp2);
    }

    /**
     * Tính khoảng thời gian giữa 2 timestamp (minutes)
     */
    public static long getDifferenceInMinutes(long timestamp1, long timestamp2) {
        return getDifferenceInSeconds(timestamp1, timestamp2) / 60;
    }

    /**
     * Tính khoảng thời gian giữa 2 timestamp (hours)
     */
    public static long getDifferenceInHours(long timestamp1, long timestamp2) {
        return getDifferenceInMinutes(timestamp1, timestamp2) / 60;
    }

    /**
     * Tính khoảng thời gian giữa 2 timestamp (days)
     */
    public static long getDifferenceInDays(long timestamp1, long timestamp2) {
        return getDifferenceInHours(timestamp1, timestamp2) / 24;
    }

    /**
     * Format khoảng thời gian thành chuỗi dễ đọc (e.g., "2 hours ago")
     */
    public static String getTimeAgo(long timestamp) {
        long now = getCurrentTimestamp();
        long diff = now - timestamp;

        if (diff < 60) {
            return diff + " seconds ago";
        } else if (diff < 3600) {
            return (diff / 60) + " minutes ago";
        } else if (diff < 86400) {
            return (diff / 3600) + " hours ago";
        } else {
            return (diff / 86400) + " days ago";
        }
    }

    // Prevent instantiation
    private DateUtils() {
        throw new AssertionError("Cannot instantiate DateUtils class");
    }
}

