package org.rohit.test.bloodsugar.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateHelper {
	
	public static Date getMidnightTime()
	{
		Calendar c = new GregorianCalendar();
	    c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
	    c.set(Calendar.MINUTE, 0);
	    c.set(Calendar.SECOND, 0);
	    Date d1 = c.getTime();
	    return d1;
	}
	
	public static Date getTwoHourPointFromLastProcessedTime(Date oldDate)
	{
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(oldDate); 
		cal.add(Calendar.HOUR_OF_DAY, 2); 
		Date newDate = cal.getTime();
		return newDate;
	}
	
	public static Date getDateMinutesAhead(Date baseDate, long minutes)
	{
		int mins = (int) minutes;
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(baseDate); 
		cal.add(Calendar.MINUTE, mins); 
		Date newDate = cal.getTime();
		return newDate;
	}

}
