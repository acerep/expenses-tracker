package com.igel.expenses.tracker;

import java.util.Calendar;

public final class CalendarUtils {

	public static Calendar getFirstDayOfMonth(Calendar date) {
        Calendar calendar = (Calendar)date.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.MILLISECOND);
        return calendar;
	}

	public static Calendar getLastDayOfMonth(Calendar date) {
        Calendar calendar = getFirstDayOfMonth(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar;
	}
}
