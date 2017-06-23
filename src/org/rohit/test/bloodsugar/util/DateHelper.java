package org.rohit.test.bloodsugar.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class DateHelper {
	
	public static Date getStartOfDayTime()
	{
		Calendar c = new GregorianCalendar();
	    c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
	    c.set(Calendar.MINUTE, 1);
	    c.set(Calendar.SECOND, 0);
	    Date d1 = c.getTime();
	    return d1;
	}
	
	public static Date getEndOfDayTime()
	{
		Calendar c = new GregorianCalendar();
	    c.set(Calendar.HOUR_OF_DAY, 23); //anything 0 - 23
	    c.set(Calendar.MINUTE, 59);
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
	
	public static long getDiffInMinutes(Date d1, Date d2)
	{
		long diffInMillies = d1.getTime() - d2.getTime();
		long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
		
		return diffInMinutes;
	}
	
	public static Date getDateMinutesAhead(Date baseDate, long minutes)
	{
		int mins = Math.abs((int) minutes);
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(baseDate); 
		cal.add(Calendar.MINUTE, mins); 
		Date newDate = cal.getTime();
		return newDate;
	}
	
	public static Date getDateMinutesBefore(Date baseDate, long minutes)
	{
		int mins = (int) minutes * -1;
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(baseDate); 
		cal.add(Calendar.MINUTE, mins); 
		Date newDate = cal.getTime();
		return newDate;
	}

}
