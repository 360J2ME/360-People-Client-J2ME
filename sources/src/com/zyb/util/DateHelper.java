/*******************************************************************************
 * CDDL HEADER START
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * src/com/vodafone360/people/VODAFONE.LICENSE.txt or
 * http://github.com/360/360-Engine-for-Android
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 *  include the License file at src/com/vodafone360/people/VODAFONE.LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 * 
 * CDDL HEADER END
 * 
 * Copyright 2010 Vodafone Sales & Services Ltd.  All rights reserved.
 * Use is subject to license terms.
 ******************************************************************************/

package com.zyb.util;

import java.util.Calendar;
import java.util.Date;

import de.enough.polish.util.Locale;

/**
 * @author Jens Vesti
 *
 */
public class DateHelper {

	/**
	 * one second as milliseconds
	 */
	public static final long SECOND = 1000;
	
	/**
	 * one minute as milliseconds
	 */
	public static final long MINUTE = 60 * SECOND;
	
	/**
	 * one hour as milliseconds
	 */
	public static final long HOUR = 60 * MINUTE;
	
	/**
	 * one day as milliseconds
	 */
	public static final long DAY = 24 * HOUR;
	
	/**
	 * @param dob Date of Birth
	 * @param now substitute for now, can be null which will default to now()
	 * @param acceptedAge Minimum age which must be passed 
	 * @return
	 */
	public static boolean ageAcceptable(final Date doB, final Date now, final int acceptedAge) {
		Calendar dateOfBirth = Calendar.getInstance();
		dateOfBirth.setTime(doB);		
	    
		Calendar today = Calendar.getInstance();
		
		if(now != null)
			today.setTime(now);
		
	    // Age based on year
	    int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
	    
	    if (dateOfBirth.get(Calendar.MONTH) > today.get(Calendar.MONTH)) {
	    	age--;
	    }
	    else
	    if (dateOfBirth.get(Calendar.MONTH) == today.get(Calendar.MONTH) && dateOfBirth.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH)) {
	    	age--;
	    }
	
	    return age>=acceptedAge;
	}	
	
	/**
	 * Returns the string for a section in the activities 
	 * @param time 
	 * @return
	 */
	public static String getActivitySection(long time, boolean cosiderNow)
	{
		Calendar now = getCalendar(System.currentTimeMillis());
		Calendar then = getCalendar(time);
		
		if(cosiderNow && isNow(time))
		{
			return Locale.get("nowplus.client.java.date.now");
		}
		else 
		if((!cosiderNow && isNow(time)) || isToday(now,then))
		{
			return Locale.get("nowplus.client.java.date.today");
		}
		else if(isYesterday(then))
		{
			return Locale.get("nowplus.client.java.date.yesterday");
		}
		else if(isWithinWeek(time))
		{
			return getDay(then);
		}
		else
		{
			return getDate(then);	
		}
	}
	
	/**
	 * Returns true if the given time is within the last hour
	 * @param time the time
	 * @return true if the given time is within the last hour otherwise false
	 */
	public static boolean isNow(long time)
	{
		long now  = System.currentTimeMillis();
		return (now - time) < HOUR;
	}

  	/**
	 * Returns true if the given date is later than today
	 * @param now
     * @param then
	 * @return true if the given date is later than today
	 */
	public static boolean isFuture(Calendar now, Calendar then)
	{
        if (now.get(Calendar.YEAR) < then.get(Calendar.YEAR))
            return true;
        if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) < then.get(Calendar.MONTH))
            return true;
        if (now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == then.get(Calendar.MONTH) &&
                now.get(Calendar.DAY_OF_MONTH) < then.get(Calendar.DAY_OF_MONTH))
            return true;

        return false;
	}

	/**
	 * Returns true if the given time is today
	 * @param time the time
	 * @return true if the given time is today otherwise false
	 */
	public static boolean isToday(Calendar now, Calendar then)
	{	
		return 	now.get(Calendar.DAY_OF_MONTH) == then.get(Calendar.DAY_OF_MONTH) && 
				now.get(Calendar.MONTH) == then.get(Calendar.MONTH) &&
				now.get(Calendar.YEAR) == then.get(Calendar.YEAR);
	}
	
	/**
	 * Returns true if the given time is yesterday
	 * @param time the time
	 * @return true if the given time is yesterday otherwise false
	 */
	public static boolean isYesterday(Calendar then)
	{
		Calendar yesterday = getCalendar(System.currentTimeMillis() - DAY);
		return 	yesterday.get(Calendar.DAY_OF_MONTH) == then.get(Calendar.DAY_OF_MONTH) && 
				yesterday.get(Calendar.MONTH) == then.get(Calendar.MONTH) &&
				yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR);
	}
	
	/**
	 * Returns true if the given time is within 7 days
	 * @param then the time
	 * @return true if the given time is within 7 days otherwise false
	 */
	public static boolean isWithinWeek(long then)
	{
		return (System.currentTimeMillis() - then < (DAY * 7));
	}
	
	/**
	 * Returns the given date as a formatted string
	 * @param then the date
	 * @return the formatted string
	 */
	public static String getDate(Calendar then)
	{
		int day = then.get(Calendar.DAY_OF_MONTH);
		int month = then.get(Calendar.MONTH);
		int year = then.get(Calendar.YEAR);
		
		StringBuffer date = new StringBuffer();
	
		date.append(getFormattedValue(day));
		date.append('.');
		date.append(getFormattedValue(month + 1));
		date.append('.');
		date.append(year);
		
		return date.toString();
	}
	
	/**
	 * Returns the given Time as a formatted string
	 * @param then the date
	 * @return the formatted string
	 */
	public static String getTime(long time)
	{
		Calendar then = getCalendar(time);
		int houre = then.get(Calendar.HOUR_OF_DAY);
		int min = then.get(Calendar.MINUTE);
		
		StringBuffer date = new StringBuffer();
	
		date.append(getFormattedValue(houre));
		date.append(':');
		date.append(getFormattedValue(min));
		
		return date.toString();
	}
	
	/**
	 * Returns the text for the given value and
	 * append 0 if the value is less than 10 
	 * @param value the value
	 * @return the text
	 */
	static String getFormattedValue(int value)
	{
		if(value >= 10)
		{
			return Integer.toString(value);
		}
		else
		{
			return "0" + Integer.toString(value);
		}
	}
	
	/**
	 * Returns a string representing the day of the week for the given time
	 * @param time the time
	 * @return the string representing the day of the week
	 */
	public static String getDay(Calendar then)
	{
		switch(then.get(Calendar.DAY_OF_WEEK))
		{
			case Calendar.MONDAY 	: return Locale.get("nowplus.client.java.date.monday");
			case Calendar.TUESDAY 	: return Locale.get("nowplus.client.java.date.tuesday");
			case Calendar.WEDNESDAY	: return Locale.get("nowplus.client.java.date.wednesday");
			case Calendar.THURSDAY 	: return Locale.get("nowplus.client.java.date.thursday");
			case Calendar.FRIDAY 	: return Locale.get("nowplus.client.java.date.friday");
			case Calendar.SATURDAY	: return Locale.get("nowplus.client.java.date.saturday");
			case Calendar.SUNDAY 	: return Locale.get("nowplus.client.java.date.sunday");
			default : return null;
		}
	}
	
	/**
	 * Returns a Calendar instance for the given time
	 * @param time the time
	 * @return the Calendar instance
	 */
	public static Calendar getCalendar(long time)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		return calendar;
	}
}
