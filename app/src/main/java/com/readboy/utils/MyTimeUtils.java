package com.readboy.utils;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

@SuppressLint("SimpleDateFormat")
public class MyTimeUtils {

    public static final String DEFAULT_DATE = "yyyy-MM-dd";
    public static final String DEFAULT_DATE1 = "yyyy-MM-dd HH:mm:ss";
    public static final String START = " 00:00:00";
    public static final String END = " 23:59:59";
    public static final String DEFAULT_SYMBOL = ".";
    public static final SimpleDateFormat SDF = new SimpleDateFormat(DEFAULT_DATE);
    public static final SimpleDateFormat SDF_1 = new SimpleDateFormat(DEFAULT_DATE1);

    /**
     * 根据时间戳得到对应年月日 时分，格式：yyyy-MM-dd HH:mm
     *
     * @param timestamp 时间戳
     * @return
     */
    public static String getTime(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(new Date(timestamp * 1000));
    }

    /**
     * 根据时间戳得到对应月和日，格式：MM-dd
     *
     * @return
     */
    public static String getMonthAndDay(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd");
        return format.format(new Date(timestamp * 1000));
    }

    /**
     * 根据时间戳得到对应年月日，格式：yyyy-MM-dd
     *
     * @return
     */
    public static String getYearMonthAndDay(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date(timestamp * 1000));
    }

    /**
     * 根据时间戳得到对应小时和分钟，格式：HH:mm
     *
     * @return
     */
    public static String getHourAndMin(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(timestamp * 1000));
    }

    /**
     * 根据时间戳得到对应月和日，格式：MM-dd HH:mm
     *
     * @return
     */
    public static String getDateAndTime(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        return format.format(new Date(timestamp * 1000));
    }

    /**
     * 时间戳转换成聊天界面列表的显示时间
     *
     * @return
     */
    public static String getChatTime(long timestamp) {
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd");    //日
        Date today = new Date(System.currentTimeMillis());
        Date otherDay = new Date(timestamp * 1000);
        int temp = Integer.parseInt(sdf.format(today))
                - Integer.parseInt(sdf.format(otherDay));

        switch (temp) {
            case 0:
                result = getHourAndMin(timestamp);
                break;
            case 1:
                result = "昨天 " + getHourAndMin(timestamp);
                break;
            case 2:
                result = "前天 " + getHourAndMin(timestamp);
                break;
            default:
                result = getTime(timestamp);
                break;
        }

        return result;
    }

    /**
     * 时间戳转换成列表的显示时间
     *
     * @return
     */
    public static String getListTime(long timestamp) {
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        Date today = new Date(System.currentTimeMillis());
        Date otherDay = new Date(timestamp * 1000);
        int temp = Integer.parseInt(sdf.format(today))
                - Integer.parseInt(sdf.format(otherDay));
        SimpleDateFormat yy = new SimpleDateFormat("yyyy");    //年
        int year = Integer.parseInt(yy.format(today))
                - Integer.parseInt(yy.format(otherDay));

        switch (temp) {
            case 0:
                result = getHourAndMin(timestamp);
                break;
            case 1:
                result = "昨天" + getHourAndMin(timestamp);
                break;
            case 2:
                result = "前天" + getHourAndMin(timestamp);
                break;

            default:
                //一年以内，显示月-日 + 时间
                if (year == 0) {
                    result = getMonthAndDay(timestamp) + " " + getHourAndMin(timestamp);
                } else {
                    result = getYearMonthAndDay(timestamp) + " " + getHourAndMin(timestamp);
                }
                break;
        }
        return result;
    }

    /**
     * 把秒转换成其他单位的值，让用户给然后看出时间，比如：180秒转成3分钟
     *
     * @return 180返回 3分钟
     */
    public static String secondToOtherUnit(Context context, int secondValue) {
        String timeSecond = "秒";
        String timeMinute = "分";
        String timeHour = "时";
        String time = "";
        if (secondValue < 0) {
            time = 0 + timeSecond;
        } else if (secondValue < 60) {
            time = secondValue + timeSecond;
        } else if (secondValue < 60 * 60) {
            int min = secondValue / 60;
            int second = secondValue % 60;
            time = min + timeMinute + second + timeSecond;
        } else {
            int hour = secondValue / 3600;
            int leftTime = secondValue % 3600;
            int min = leftTime / 60;
            int second = leftTime % 60;
            time = hour + timeHour + min + timeMinute + second + timeSecond;
        }
        return time;
    }

    /**
     * 返回系统的时间戳
     *
     * @return
     */
    public static long getTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 得到当前时间戳的字符串
     *
     * @return 时间戳字符串
     */
    public static String getTimeString() {
        long time = getTimestamp();
        String k = "";
        try {
            k = String.valueOf(time);
        } catch (Exception e) {
            k = "" + time;
        }
        return k;
    }

    /**
     * 得到当前时间的字符串，毫秒为单位
     *
     * @return 时间戳字符串
     */
    public static String getMillisecondString() {
        long time = System.currentTimeMillis();
        String k = "";
        try {
            k = String.valueOf(time);
        } catch (Exception e) {
            k = "" + time;
        }
        return k;
    }

    /**
     * 毫秒转成时间戳
     *
     * @return
     */
    public static long convertMilToTimestamp(long millis) {
        return millis / 1000;
    }

    /**
     * 返回消息的时间，app用系统时间就可以了
     *
     * @return 返回系统时间
     */
    public static long getAllMessageTime() {
        return System.currentTimeMillis();
    }

    public static String getSystemMil() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 根据时间字符串转换成时间戳
     *
     * @param timeString 时间字符串
     * @return 返回时间戳，如果为0表示解析失败
     */
    public static long getTimeLongFromString(String timeString) {
        long timeLong = 0;
        if (TextUtils.isEmpty(timeString)) {
            return timeLong;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = null;
        try {
            date = formatter.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null) {
            timeLong = date.getTime();
        }
        return timeLong;
    }

    /**
     * 根据时间字符串转换成时间戳
     *
     * @param timeString 时间字符串
     * @param formatter  格式
     * @return 返回时间戳，如果为0表示解析失败
     */
    public static long getTimestampFromString(String timeString, SimpleDateFormat formatter) {
        long timeLong = 0;
        if (TextUtils.isEmpty(timeString)) {
            return timeLong;
        }
        Date date = null;
        try {
            date = formatter.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null) {
            timeLong = date.getTime();
        }
        return timeLong;
    }

    /**
     * 时间戳转换成相应格式的时间字符串
     *
     * @param timestamp    时间戳
     * @param formatString 时间格式，比如："yyyy-MM-dd HH:mm"
     * @return 返回与时间格式对应的字符串
     */
    public static String timestampToDateString(Long timestamp, String formatString) {
        SimpleDateFormat formatter = new SimpleDateFormat(formatString);
        return formatter.format(new Date(timestamp * 1000));
    }

    /**
     * 获取现在时间
     *
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
     */
    public static Date getNowDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        ParsePosition pos = new ParsePosition(8);
        return formatter.parse(dateString, pos);
    }

    /**
     * 获取现在时间
     *
     * @return返回短时间格式 yyyy-MM-dd
     */
    public static Date getNowDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        ParsePosition pos = new ParsePosition(8);
        return formatter.parse(dateString, pos);
    }

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyyMMddHHmmss
     */
    public static String getNowTimeString() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(currentTime);
    }

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
     */
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(currentTime);
    }

    /**
     * 获取现在时间
     *
     * @return 返回短时间字符串格式yyyy-MM-dd
     */
    public static String getStringDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(currentTime);
    }

    /**
     * 获取时间 小时:分;秒 HH:mm:ss
     *
     * @return
     */
    public static String getTimeShort() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date currentTime = new Date();
        return formatter.format(currentTime);
    }

    /**
     * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     *
     * @param strDate
     */
    public static Date strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        return formatter.parse(strDate, pos);
    }

    /**
     * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
     *
     * @param dateDate
     * @return
     */
    public static String dateToStrLong(java.util.Date dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(dateDate);
    }

    /**
     * 将短时间格式时间转换为字符串 yyyy-MM-dd
     *
     * @param dateDate
     * @return
     */
    public static String dateToStr(java.util.Date dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(dateDate);
    }

    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     *
     * @param strDate
     * @return
     */
    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        try {
            return formatter.parse(strDate, pos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到现在时间
     *
     * @return
     */
    public static Date getNow() {
        return new Date();
    }

    /**
     * 根据时间戳返回date
     *
     * @param time
     * @return
     */
    public static Date getDateFromLong(long time) {
        return new Date(time);
    }

    /**
     * 提取一个月中的最后一天
     *
     * @param day
     * @return
     */
    public static Date getLastDate(long day) {
        Date date = new Date();
        long date_3_hm = date.getTime() - 3600000 * 34 * day;
        return new Date(date_3_hm);
    }

    /**
     * 得到现在时间
     *
     * @return 字符串 yyyyMMdd HHmmss
     */
    public static String getStringToday() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
        return formatter.format(currentTime);
    }

    /**
     * 得到现在的时间：分钟；
     *
     * @return 返回格式：HH:mm
     */
    public static String getHourMin() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String dateString = formatter.format(currentTime);
        String hourMin;
        hourMin = dateString;
        return hourMin;
    }

    /**
     * 得到现在小时
     */
    public static String getHour() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        String hour;
        hour = dateString.substring(11, 13);
        return hour;
    }

    /**
     * 得到现在分钟
     *
     * @return
     */
    public static String getTimeMin() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        String min;
        min = dateString.substring(14, 16);
        return min;
    }

    /**
     * 根据用户传入的时间表示格式，返回当前时间的格式 如果是yyyyMMdd，注意字母y不能大写。
     *
     * @param sformat yyyyMMddhhmmss
     * @return
     */
    public static String getUserDate(String sformat) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(sformat);
        return formatter.format(currentTime);
    }

    /**
     * 两个小时时间的差值,必须保证两个时间都是"HH:MM"的格式，返回字符型的分钟
     */
    public static String getTwoHour(String endTime, String startTime) {
        String[] kk = null;
        String[] jj = null;
        kk = endTime.split(":");
        jj = startTime.split(":");
        if (Integer.parseInt(kk[0]) < Integer.parseInt(jj[0])) {
            return "0";
        } else {
            double y = Double.parseDouble(kk[0]) + Double.parseDouble(kk[1]) / 60;
            double u = Double.parseDouble(jj[0]) + Double.parseDouble(jj[1]) / 60;
            if ((y - u) > 0) {
                return y - u + "";
            } else {
                return "0";
            }
        }
    }

    /**
     * 判断结束时间是否大于开始时间，是返回true，否则返回false。 必须保证两个时间都是"HH:MM"的格式，返回字符型的分钟
     */
    public static boolean isEndBiggerStart(String startTime, String endTime) {
        String[] kk = null;
        String[] jj = null;
        kk = endTime.split(":");
        jj = startTime.split(":");
        if (Integer.parseInt(kk[0]) < Integer.parseInt(jj[0])) {
            return false;
        } else {
            double y = Double.parseDouble(kk[0]) + Double.parseDouble(kk[1]) / 60;
            double u = Double.parseDouble(jj[0]) + Double.parseDouble(jj[1]) / 60;
            return (y - u) > 0;
        }
    }

    /**
     * 得到二个日期间的间隔天数
     */
    public static String getTwoDay(String sj1, String sj2) {
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
        long day = 0;
        try {
            java.util.Date date = myFormatter.parse(sj1);
            java.util.Date mydate = myFormatter.parse(sj2);
            day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
        } catch (Exception e) {
            return "";
        }
        return day + "";
    }

    /**
     * 时间前推或后推分钟,其中JJ表示分钟.
     */
    public static String getPreTime(String sj1, String jj) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String mydate1 = "";
        try {
            Date date1 = format.parse(sj1);
            long time = (date1.getTime() / 1000) + Integer.parseInt(jj) * 60;
            date1.setTime(time * 1000);
            mydate1 = format.format(date1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mydate1;
    }

    /**
     * 得到一个时间延后或前移几天的时间,nowdate为时间,delay为前移或后延的天数
     */
    public static String getNextDay(String nowdate, String delay) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String mdate = "";
            Date d = strToDate(nowdate);
            long myTime = (d.getTime() / 1000) + Integer.parseInt(delay) * 24 * 60 * 60;
            d.setTime(myTime * 1000);
            mdate = format.format(d);
            return mdate;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 得到一个时间往后或前移几天的时间,nowdate为时间,delay为前移或后延的天数
     *
     * @param nowdate 日期字符串
     * @param delay   0为与nowdate同一天，正数为往后，负数为前移
     * @return 返回日期时间，格式：yyyy-MM-dd HH:mm:ss
     */
    public static String getNextDayDetail(String nowdate, int delay) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String mdate = "";
            Date d = strToDate(nowdate);
            long myTime = (d.getTime() / 1000) + delay * 24 * 60 * 60;
            d.setTime(myTime * 1000);
            mdate = format.format(d);
            mdate = mdate + " 00:00:00";
            return mdate;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 根据传进来的两个日期，计算出两个日期之间的日期，然后返回这个两个日期之间的日期list
     *
     * @return 日期list, date的格式是“2015-08-06 00:00:00”
     */
    public static ArrayList<String> getDateListWithTwoDay(String startTime, String endTime) {
        ArrayList<String> list = new ArrayList<String>();
        long k = MyTimeUtils.getDays(endTime, startTime); // 结束时间与开始时间之差
        if (k > 0) {
            for (int i = 0; i <= k; i++) {
                String value = getNextDayDetail(startTime, i);
                list.add(value);
            }
            return list;
        } else { // 如果两个日期都在一天之内就只用第一个日期
            list.add(startTime);
            return list;
        }
    }

    /**
     * 2015-08-06 00:00:00转成20150806
     *
     * @param dateString
     * @return
     */
    public static String convertToYYYYMMDD(String dateString) {
        if (TextUtils.isEmpty(dateString)) {
            return "";
        }
        if (dateString.length() == DEFAULT_DATE.length() || dateString.length() == DEFAULT_DATE1.length()) {
            return dateString.substring(0, 4) + dateString.substring(5, 7) + dateString.substring(8, 10);
        } else {
            return dateString;
        }
    }

    /**
     * 2015-08-06 00:00:00转成8.06
     *
     * @param date   2015-08-06 00:00:00
     * @param symbol 月与日之间的分隔符，可以是"." "-"等
     * @return 8.06
     */
    public static String convertToMMDD(String date, String symbol) {

        if (TextUtils.isEmpty(date)) {
            return date;
        }
        try {
            Date strtodate = strToDate(date);
            Calendar calendar = Calendar.getInstance();
            if (strtodate != null) {
                calendar.setTime(strtodate);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DATE);
                if (TextUtils.isEmpty(symbol)) {
                    return month + DEFAULT_SYMBOL + day;
                } else {
                    return month + symbol + day;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date.length() >= DEFAULT_DATE.length()) {
            return date.substring(5, 7) + date.substring(8, 10);
        }
        return date;
    }

    /**
     * 判断是否润年
     *
     * @param ddate
     * @return
     */
    public static boolean isLeapYear(String ddate) {

        /**
         * 详细设计： 1.被400整除是闰年，否则： 2.不能被4整除则不是闰年 3.能被4整除同时不能被100整除则是闰年
         * 3.能被4整除同时能被100整除则不是闰年
         */
        Date d = strToDate(ddate);
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(d);
        int year = gc.get(Calendar.YEAR);
//		if ((year % 400) == 0) {
//			return true;
//		} else if ((year % 4) == 0) {
//			return (year % 100) != 0;
//		} else {
//			return false;
//		}
        return (year % 400) == 0 || (year % 4) == 0 && (year % 100) != 0;
    }

    /**
     * 返回美国时间格式 26 Apr 2006
     *
     * @param str
     * @return
     */
    public static String getEDate(String str) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(str, pos);
        String j = strtodate.toString();
        String[] k = j.split(" ");
        return k[2] + k[1].toUpperCase() + k[5].substring(2, 4);
    }

    /**
     * 获取一个月的最后一天
     *
     * @param dat
     * @return
     */
    public static String getEndDateOfMonth(String dat) {// yyyy-MM-dd
        String str = dat.substring(0, 8);
        String month = dat.substring(5, 7);
        int mon = Integer.parseInt(month);
        if (mon == 1 || mon == 3 || mon == 5 || mon == 7 || mon == 8 || mon == 10 || mon == 12) {
            str += "31";
        } else if (mon == 4 || mon == 6 || mon == 9 || mon == 11) {
            str += "30";
        } else {
            if (isLeapYear(dat)) {
                str += "29";
            } else {
                str += "28";
            }
        }
        return str;
    }

    /**
     * 判断两个时间是否在同一个周
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameWeekDates(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if (0 == subYear) {
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
                return true;
            }
        } else if (1 == subYear && Calendar.DECEMBER == cal2.get(Calendar.MONTH)) {
            // 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
                return true;
            }
        } else if (-1 == subYear && Calendar.DECEMBER == cal1.get(Calendar.MONTH)) {
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 产生周序列,即得到当前时间所在的年度是第几周
     *
     * @return
     */
    public static String getSeqWeek() {
        Calendar c = Calendar.getInstance(Locale.CHINA);
        String week = Integer.toString(c.get(Calendar.WEEK_OF_YEAR));
        if (week.length() == 1) {
            week = "0" + week;
        }
        String year = Integer.toString(c.get(Calendar.YEAR));
        return year + week;
    }

    /**
     * 获得一个日期所在的周的星期几的日期，如要找出2002年2月3日所在周的星期一是几号
     *
     * @param sdate
     * @param num
     * @return
     */
    public static String getWeek(String sdate, String num) {
        // 再转换为时间
        Date dd = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(dd);
        if ("1".equals(num)) // 返回星期一所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else if ("2".equals(num)) // 返回星期二所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        } else if ("3".equals(num)) // 返回星期三所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        } else if ("4".equals(num)) // 返回星期四所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        } else if ("5".equals(num)) // 返回星期五所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        } else if ("6".equals(num)) // 返回星期六所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        } else if ("0".equals(num)) // 返回星期日所在的日期
        {
            c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }

    /**
     * 获取当天是星期几
     *
     * @return 1=星期日 7=星期六，其他类推
     */
    public static int getNowWeek() {
        Date date = getNow();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 根据一个日期，返回是星期几的字符串
     *
     * @param sdate
     * @return
     */
    public static String getWeek(String sdate) {
        // 再转换为时间
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int k = c.get(Calendar.DAY_OF_WEEK);// k中存的就是星期几了，其范围 1~7
        return "" + k;
//		return new SimpleDateFormat("EEEE").format(c.getTime());// 直接返回星期一、星期二...
    }

    public static String getWeekStr(String sdate) {
        String str = "";
        str = getWeek(sdate);
        if ("1".equals(str)) {
            str = "周日";
        } else if ("2".equals(str)) {
            str = "周一";
        } else if ("3".equals(str)) {
            str = "周二";
        } else if ("4".equals(str)) {
            str = "周三";
        } else if ("5".equals(str)) {
            str = "周四";
        } else if ("6".equals(str)) {
            str = "周五";
        } else if ("7".equals(str)) {
            str = "周六";
        }
        return str;
    }

    /**
     * 两个时间之间的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static long getDays(String date1, String date2) {
        if (date1 == null || "".equals(date1)) {
            return 0;
        }
        if (date2 == null || "".equals(date2)) {
            return 0;
        }
        // 转换为标准时间
        SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = null;
        java.util.Date mydate = null;
        try {
            date = myFormatter.parse(date1);
            mydate = myFormatter.parse(date2);
        } catch (Exception e) {
        }
        return (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
    }

    /**
     * 秒转为时分秒格式，格式：02:55:34
     *
     * @param time 秒数值
     * @return
     */
    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0) {
            return "00:00";
        } else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99) {
                    return "99:59:59";
                }
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10) {
            retStr = "0" + Integer.toString(i);
        } else {
            retStr = "" + i;
        }
        return retStr;
    }

    /**
     * 秒转为时分秒格式，格式：2h14'05''
     *
     * @param time 秒数值
     * @return
     */
    public static String secToTimeSuperscript(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0) {
            return "00'00''";
        } else if (time < 60) {
            return time + "''";
        } else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = minute + "'" + unitFormat(second) + "''";
            } else {
                hour = minute / 60;
                if (hour > 99) {
                    return "99h59'59''";
                }
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = hour + "h" + unitFormat(minute) + "'" + unitFormat(second) + "''";
            }
        }
        return timeStr;
    }

    /**
     * 形成如下的日历 ， 根据传入的一个时间返回一个结构 星期日 星期一 星期二 星期三 星期四 星期五 星期六 下面是当月的各个时间
     * 此函数返回该日历第一行星期日所在的日期
     *
     * @param sdate
     * @return
     */
    public static String getNowMonth(String sdate) {
        // 取该时间所在月的一号
        sdate = sdate.substring(0, 8) + "01";

        // 得到这个月的1号是星期几
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int u = c.get(Calendar.DAY_OF_WEEK);
        return getNextDay(sdate, (1 - u) + "");
    }

    /**
     * 取得数据库主键 生成格式为yyyymmddhhmmss+k位随机数
     *
     * @param k 表示是取几位随机数，可以自己定
     */

    public static String getNo(int k) {

        return getUserDate("yyyyMMddhhmmss") + getRandom(k);
    }

    /**
     * 返回一个随机数
     *
     * @param i
     * @return
     */
    public static String getRandom(int i) {
        Random jjj = new Random();
        // int suiJiShu = jjj.nextInt(9);
        if (i == 0) {
            return "";
        }
        String jj = "";
        for (int k = 0; k < i; k++) {
            jj = jj + jjj.nextInt(9);
        }
        return jj;
    }

    /**
     *
     */
    public static boolean rightDate(String date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        if (date == null) {
            return false;
        }
        if (date.length() > 10) {
            sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        } else {
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        }
        try {
            sdf.parse(date);
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    //加密，方法名随便起，你懂的
    public static final String RC4_KEY = "22690dfba7ab83b4";

    public static String enKaTime(String string) throws Exception {
        Cipher cipher = Cipher.getInstance("RC4");
        SecretKeySpec key = new SecretKeySpec(RC4_KEY.getBytes("UTF-8"), "RC4");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] ddata = cipher.update(string.getBytes(Charset.forName("UTF-8")));
        byte[] data = Base64.encode(ddata, Base64.NO_WRAP);
        return new String(data, Charset.forName("UTF-8"));
    }

    public static String deKaTime(String t) throws Exception {
        byte[] data = Base64.decode(t.getBytes(), Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("RC4");
        SecretKeySpec key = new SecretKeySpec(RC4_KEY.getBytes("UTF-8"), "RC4");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] ddata = cipher.update(data);
        return new String(ddata, Charset.forName("UTF-8"));
    }


}
