package com.archer.server.common.util;

import com.archer.server.common.exception.RestRuntimeException;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 时间工具
 *
 * @author Shinobu
 */
public class TimeUtil {

    private static final String BASIC_SEC_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final String BASIC_MILLIS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final String BASIC_DATE_PATTERN = "yyyy-MM-dd";

    private static final String BASIC_MONTH_PATTERN = "yyyy-MM";

    private static final String BASIC_YEAR_PATTERN = "yyyy";

    private static final String NUMBER_TIME_PATTERN = "yyyyMMddHHmmss";

    public static @NotNull String nowMsecStr() {
        return new SimpleDateFormat(BASIC_MILLIS_PATTERN).format(new Date());
    }

    public static @NotNull String nowDateStr() {
        return new SimpleDateFormat(BASIC_DATE_PATTERN).format(new Date());
    }

    /**
     * Date -> String
     * pattern: yyyy-MM-dd HH:mm:ss
     */
    public static @Nullable String toStandardTimeStr(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(BASIC_SEC_PATTERN).format(date);
    }

    /**
     * Date -> String
     * pattern: yyyyMMddHHmmssSSS
     */
    public static String toNumberTimeStr(Date date) {
        return new SimpleDateFormat(NUMBER_TIME_PATTERN).format(date);
    }

    /**
     * Date -> String
     * pattern: yyyy-MM-dd
     */
    public static String toDateStr(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(BASIC_DATE_PATTERN).format(date);
    }

    /**
     * Date -> String
     * pattern: yyyy-MM
     */
    public static String toMonthStr(Date date) {
        return new SimpleDateFormat(BASIC_MONTH_PATTERN).format(date);
    }

    /**
     * Date -> String
     * pattern: yyyy
     */
    public static String toYearStr(Date date) {
        return new SimpleDateFormat(BASIC_YEAR_PATTERN).format(date);
    }

    public static String getMonthStr(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.valueOf(calendar.get(Calendar.MONTH));
    }

    /**
     * 相差天数
     */
    public static int dateInterval(String start, String end) {
        Date a = dayStartPoint(start);
        Date b = plusDays(dayStartPoint(end), 1);
        long i = 1000 * 60 * 60 * 24;
        return (int) ((b.getTime() - a.getTime()) / i);
    }

    /**
     * 日期增加
     */
    public static Date plusDays(Date date, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, n);
        return calendar.getTime();
    }

    /**
     * 月份增加
     */
    public static Date plusMonths(Date date, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, n);
        return calendar.getTime();
    }

    /**
     * 年份增加
     */
    private static Date plusYears(Date date, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, n);
        return calendar.getTime();
    }

    /**
     * 今天的零点
     */
    public static Date todayStartPoint() {
        return dayStartPoint(new SimpleDateFormat(BASIC_DATE_PATTERN).format(new Date()));
    }

    /**
     * 今天的23:59:59
     */
    public static Date todayEndPoint() {
        return dayEndPoint(new SimpleDateFormat(BASIC_DATE_PATTERN).format(new Date()));
    }

    /**
     * 本月第一天的零点
     */
    public static Date thisMonthStartPoint() {
        return monthStartPoint(new SimpleDateFormat(BASIC_MONTH_PATTERN).format(new Date()));
    }

    /**
     * 本月最后一天的23:59:59
     */
    public static Date thisMonthEndPoint() {
        return monthEndPoint(new SimpleDateFormat(BASIC_MONTH_PATTERN).format(new Date()));
    }

    /**
     * 某年第一天的零点
     */
    public static Date yearStartPoint(String year) {
        try {
            return new SimpleDateFormat(BASIC_YEAR_PATTERN).parse(year);
        } catch (ParseException e) {
            throw new RestRuntimeException("日期解析失败", e);
        }
    }

    /**
     * 某年最后一天的23:59:59
     */
    public static Date yearEndPoint(String year) {
        return new Date(plusYears(yearStartPoint(year), 1).getTime() - 1);
    }

    public static List<Date[]> daysStartAndEnd(String start, String end) {
        List<Date[]> result = new ArrayList<>(32);
        Date dateStart = dayStartPoint(start);
        Date dateEnd = dayStartPoint(end);
        while (dateStart.getTime() <= dateEnd.getTime()) {
            Date[] node = new Date[2];
            Date dayStart = dayStartPoint(dateStart);
            node[0] = dayStart;
            node[1] = dayEndPoint(dateStart);
            result.add(node);
            dateStart = plusDays(dayStart, 1);
        }
        return result;
    }

    public static List<Date[]> monthsStartAndEnd(String year) {
        List<Date[]> result = new ArrayList<>(12);
        Date yearStart = yearStartPoint(year);
        Date yearEnd = yearEndPoint(year);
        while (yearStart.getTime() <= yearEnd.getTime()) {
            Date[] node = new Date[2];
            Date monthStart = monthStartPoint(yearStart);
            node[0] = monthStart;
            node[1] = monthEndPoint(monthStart);
            result.add(node);
            yearStart = plusMonths(monthStart, 1);
        }
        return result;
    }

    /**
     * 某天的零点
     */
    public static Date dayStartPoint(String date) {
        try {
            return new SimpleDateFormat(BASIC_DATE_PATTERN).parse(date);
        } catch (ParseException e) {
            throw new RestRuntimeException("日期解析失败", e);
        }
    }

    public static Date dayStartPoint(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(BASIC_DATE_PATTERN);
        String dateStr = format.format(date);
        return dayStartPoint(dateStr);
    }

    /**
     * 某天的23:59:59
     */
    public static Date dayEndPoint(String date) {
        return new Date(plusDays(dayStartPoint(date), 1).getTime() - 1);
    }

    public static Date dayEndPoint(Date date) {
        return new Date(plusDays(dayStartPoint(date), 1).getTime() - 1);
    }

    /**
     * 某月第一天的零点
     */
    public static Date monthStartPoint(String month) {
        try {
            return new SimpleDateFormat(BASIC_MONTH_PATTERN).parse(month);
        } catch (ParseException e) {
            throw new RestRuntimeException("日期解析失败", e);
        }
    }

    private static Date monthStartPoint(Date month) {
        return monthStartPoint(toMonthStr(month));
    }

    private static Date monthEndPoint(Date month) {
        return monthEndPoint(toMonthStr(month));
    }

    /**
     * 某月最后一天的23:59:59
     */
    public static Date monthEndPoint(String month) {
        return new Date(plusMonths(monthStartPoint(month), 1).getTime() - 1);
    }

}
