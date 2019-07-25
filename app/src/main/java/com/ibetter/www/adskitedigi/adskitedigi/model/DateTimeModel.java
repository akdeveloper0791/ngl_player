package com.ibetter.www.adskitedigi.adskitedigi.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ibetter-Dell on 18-11-16.
 */


public class DateTimeModel {



    public String getDate(SimpleDateFormat format,long longdate)
    {

        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(longdate);

        return format.format(calendar.getTime());
    }

    public static int getDayOfWeek(Calendar calendar)
    {

        switch (calendar.get(Calendar.DAY_OF_WEEK))
        {
            case Calendar.SUNDAY:
                return 1;
            case Calendar.MONDAY:
                return 2;
            case Calendar.TUESDAY:
                return 3;
            case Calendar.WEDNESDAY:
                return 4;
            case Calendar.THURSDAY:
                return 5;
            case Calendar.FRIDAY:
                return 6;
            case Calendar.SATURDAY:
                return 7;
                default:
                    return 10;
        }
    }



}
