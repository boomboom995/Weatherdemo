// 文件路径: app/src/main/java/com/example/weatherforecast2/util/LunarUtil.java
package com.example.weatherforecast2.util;

import java.util.Calendar;
import java.util.Date;

public final class LunarUtil {

    private final static int[] LUNAR_INFO = {
            0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
            0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
            0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
            0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
            0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
            0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
            0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
            0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
            0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
            0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
            0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
            0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
            0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
            0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
            0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
            0x14b63
    };

    private final static String[] LUNAR_MONTH_NAME = {"", "正月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "冬月", "腊月"};
    private final static String[] LUNAR_DAY_NAME = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };

    private int year;
    private int month;
    private int day;
    private boolean isLeap;

    private LunarUtil(Calendar cal) {
        int yearCyl, monCyl, dayCyl;
        int leapMonth = 0;
        Date baseDate = null;
        try {
            baseDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse("1900-1-31");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int offset = (int) ((cal.getTime().getTime() - baseDate.getTime()) / 86400000L);
        dayCyl = offset + 40;
        monCyl = 14;

        int i;
        for (i = 1900; i < 2050 && offset > 0; i++) {
            int daysInYear = daysInYear(i);
            offset -= daysInYear;
            monCyl += 12;
        }
        if (offset < 0) {
            offset += daysInYear(i - 1);
            i--;
            monCyl -= 12;
        }
        this.year = i;
        yearCyl = i - 1864;
        leapMonth = leapMonth(i);
        isLeap = false;
        int j;
        for (j = 1; j < 13 && offset > 0; j++) {
            int temp;
            if (leapMonth > 0 && j == (leapMonth + 1) && !isLeap) {
                --j;
                isLeap = true;
                temp = leapDays(this.year);
            } else {
                isLeap = false;
                temp = daysInMonth(this.year, j);
            }

            offset -= temp;
            if (!isLeap) {
                monCyl++;
            }
        }
        if (offset == 0 && leapMonth > 0 && j == leapMonth + 1) {
            if (isLeap) {
                isLeap = false;
            } else {
                isLeap = true;
                --j;
                --monCyl;
            }
        }
        if (offset < 0) {
            offset += daysInMonth(this.year, j - 1);
            --j;
            --monCyl;
        }
        this.month = j;
        this.day = offset + 1;
    }

    public static String getLunarDateString(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        LunarUtil lunar = new LunarUtil(cal);
        
        if (lunar.day == 1) {
             return (lunar.isLeap ? "闰" : "") + LUNAR_MONTH_NAME[lunar.month];
        } else {
            return LUNAR_DAY_NAME[lunar.day - 1];
        }
    }

    private static int daysInYear(int year) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((LUNAR_INFO[year - 1900] & i) != 0) {
                sum += 1;
            }
        }
        return (sum + leapDays(year));
    }

    private static int leapDays(int year) {
        if (leapMonth(year) != 0) {
            return ((LUNAR_INFO[year - 1900] & 0x10000) != 0 ? 30 : 29);
        } else {
            return 0;
        }
    }

    private static int leapMonth(int year) {
        return (LUNAR_INFO[year - 1900] & 0xf);
    }

    private static int daysInMonth(int year, int month) {
        return ((LUNAR_INFO[year - 1900] & (0x10000 >> month)) != 0 ? 30 : 29);
    }
}