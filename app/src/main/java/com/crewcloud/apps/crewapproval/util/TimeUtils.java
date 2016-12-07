package com.crewcloud.apps.crewapproval.util;

import java.util.Calendar;

public class TimeUtils {
    private static final Calendar FIRST_DAY_OF_TIME;
    private static final Calendar LAST_DAY_OF_TIME;

    static {
        FIRST_DAY_OF_TIME = Calendar.getInstance();
        FIRST_DAY_OF_TIME.set(Calendar.getInstance().get(Calendar.YEAR) - 100, Calendar.JANUARY, 1);
        LAST_DAY_OF_TIME = Calendar.getInstance();
        LAST_DAY_OF_TIME.set(Calendar.getInstance().get(Calendar.YEAR) + 100, Calendar.DECEMBER, 31);
    }

    private static Calendar getTimeFromStr(String strTime) {
        String hours = strTime.substring(3, 5);
        String minutes = strTime.substring(6, 8);
        int intHours = Integer.parseInt(hours);
        int intMinutes = Integer.parseInt(minutes);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, intHours);
        calendar.set(Calendar.MINUTE, intMinutes);
        return calendar;
    }

    public static boolean isBetweenTime(String strFromTime, String strToTime) {
        Calendar calendar = Calendar.getInstance();
        Calendar calFromTime = getTimeFromStr(strFromTime);
        Calendar calToTime = getTimeFromStr(strToTime);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int hourFromTime = calFromTime.get(Calendar.HOUR_OF_DAY);
        int minuteFromTime = calFromTime.get(Calendar.MINUTE);
        int hourToTime = calToTime.get(Calendar.HOUR_OF_DAY);
        int minuteToTime = calToTime.get(Calendar.MINUTE);

        if (hourFromTime == hour || hour == hourToTime) {
            if (hourFromTime == hour && hour == hourToTime) {
                if (minuteFromTime <= minute && minute <= minuteToTime) {
                    return true;
                } else {
                    return false;
                }
            } else if (hourFromTime == hour && hour != hourToTime) {
                if (minuteFromTime <= minute) {
                    return true;
                } else {
                    return false;
                }
            } else if(hourFromTime != hour && hour == hourToTime) {
                if (minute <= minuteToTime) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }else {
            if (hourFromTime < hour && hour < hourToTime) {
                return true;
            } else {
                return false;
            }
        }
    }
}