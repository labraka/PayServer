package com.lrj.pay.utils;

import com.lrj.pay.enums.DateTimeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;


/**
 * @ClassName: DateUtil
 * @Description: 时间工具类
 * @Date: 2022/8/12 14:17
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */

@Slf4j
public class DateUtil {
    /**
     * 格式化日期
     *
     * @param date
     * @param pattern
     * @author: luorenjie
     * @date: 2022/8/12 14:18
     * @return: java.lang.String
     */
    public static String dateToStr(LocalDateTime date, String pattern) {
        if (date == null || date.equals(""))
            return null;
        String res = DateTimeFormatter.ofPattern(pattern).format(date);
        return res;
    }

    /**
     * 根据时间类型加减（毫秒>>>年）
     *
     * @param curTime
     * @param type
     * @param times
     * @author: luorenjie
     * @date: 2022/8/12 15:36
     * @return: java.time.LocalDateTime
     */
    public static LocalDateTime addTime(LocalDateTime curTime, int type, long times) {
        DateTimeTypeEnum dateTimeTypeEnum = DateTimeTypeEnum.getDateTimeType(type);
        if (ObjectUtils.isEmpty(dateTimeTypeEnum)) {
            log.error("无此时间类型：{}", type);
            return null;
        }
        LocalDateTime newTime = null;
        switch (dateTimeTypeEnum) {
            case MILLIS:
                newTime = curTime.plus(times, ChronoUnit.MILLIS);
                break;
            case SECONDS:
                newTime = curTime.plusSeconds(times);
                break;
            case MINUTES:
                newTime = curTime.plusMinutes(times);
                break;
            case HOUR:
                newTime = curTime.plusHours(times);
                break;
            case DAY:
                newTime = curTime.plusDays(times);
                break;
            case WEEK:
                newTime = curTime.plusWeeks(times);
                break;
            case MONTH:
                newTime = curTime.plusMonths(times);
                break;
            case YEAR:
                newTime = curTime.plusYears(times);
                break;
        }
        return newTime;
    }

    /**
     * 根据时间类型计算时间差值（毫秒>>>年）
     * @author: luorenjie
     * @date: 2022/8/24 15:49
     * @param begin
     * @param end
     * @param type
     * @return: long
     */
    public static long differTimeNums(LocalDateTime begin, LocalDateTime end, int type){
        DateTimeTypeEnum dateTimeTypeEnum = DateTimeTypeEnum.getDateTimeType(type);
        if (ObjectUtils.isEmpty(dateTimeTypeEnum)) {
            log.error("无此时间类型：{}", type);
            return -1;
        }
        long times = 0;
        switch (dateTimeTypeEnum) {
            case MILLIS:
                times = ChronoUnit.MILLIS.between(begin, end);
                break;
            case SECONDS:
                times = ChronoUnit.SECONDS.between(begin, end);
                break;
            case MINUTES:
                times = ChronoUnit.MINUTES.between(begin, end);
                break;
            case HOUR:
                times = ChronoUnit.HOURS.between(begin, end);
                break;
            case DAY:
                times = ChronoUnit.DAYS.between(begin, end);
                break;
            case WEEK:
                times = ChronoUnit.WEEKS.between(begin, end);
                break;
            case MONTH:
                times = ChronoUnit.MONTHS.between(begin, end);
                break;
            case YEAR:
                times = ChronoUnit.YEARS.between(begin, end);
                break;
        }
        return times;
    }

    /**
     * localDateTime转rfc3339标准格式
     * @author: luorenjie
     * @date: 2022/8/23 20:40
     * @param date
     * @return: java.lang.String
     */
    public static String transRFC3339(LocalDateTime date) {
        DateTimeFormatter dtf = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss+08:00")
                .withZone(ZoneId.of("Asia/Shanghai"));
        return dtf.format(date);
    }

    /**
     * rfc3339标准格式时间转localDateTime
     * @author: luorenjie
     * @date: 2022/8/23 20:41
     * @param rfc3339Time
     * @return: java.time.LocalDateTime
     */
    public static LocalDateTime retransRFC3339(String rfc3339Time) {
        DateTime dateTime = new DateTime(rfc3339Time);
        long timeInMillis = dateTime.toCalendar(Locale.getDefault()).getTimeInMillis();
        Date date = new Date(timeInMillis);
        LocalDateTime times = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return times;
    }

    public static void main(String[] args) throws IOException, ParseException {
//        System.out.println(dateToStr(LocalDateTime.now(), "yyMMdd"));
//        System.out.println(dateToStr(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"));
//        System.out.println(transRFC3339(LocalDateTime.now()));
//        System.out.println(addTime(LocalDateTime.now(), 6, 3));
//
//        LocalDateTime times = retransRFC3339("2022-08-23T18:28:26+08:00");
//        System.out.println(times);
//
//        long time = differTimeNums(LocalDateTime.parse("2022-02-01T18:28:26"), LocalDateTime.parse("2022-02-25T18:28:26"), 3);
//        System.out.println(time);
        System.out.println((Math.round(13 * 100 / 30) / 100.0));
    }
}

