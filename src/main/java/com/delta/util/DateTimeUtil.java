package com.delta.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Desription:
 * @Author: yuzhuojun
 * @CreateDate: 2021/3/15 12:20 AM
 */
public class DateTimeUtil {

    private DateTimeUtil() {}

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static String formatDateString(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime.format(dateTimeFormatter);
    }

    public static Date parseStringDate(String date) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, dateTimeFormatter);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
