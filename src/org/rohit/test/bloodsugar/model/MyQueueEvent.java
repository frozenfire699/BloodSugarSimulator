package org.rohit.test.bloodsugar.model;

import java.util.Date;

import org.rohit.test.bloodsugar.mockups.ExcerciseDB;
import org.rohit.test.bloodsugar.mockups.FoodDB;
import org.rohit.test.bloodsugar.util.DateHelper;

public class MyQueueEvent {
	
	private Date eventStartTime;
	private Date eventEndTime;
	private Date eventLastProcessedTime;
	private long minsLeftToExpire;
	private Double glycemicIndexRate;
	
	public MyQueueEvent()
	{
		
	}
	
	public MyQueueEvent(Date inputEventTime, String eventType, String item)
	{
		this.eventStartTime = inputEventTime;
		if(eventType.equalsIgnoreCase("FDD")){
			this.eventEndTime = DateHelper.getDateMinutesAhead(inputEventTime, 120);
			this.minsLeftToExpire = 120;
			this.glycemicIndexRate = FoodDB.getInstance().hmFoodDB.get(item).doubleValue()/120;
		}
		else if(eventType.equalsIgnoreCase("EXC")){
			this.eventEndTime = DateHelper.getDateMinutesAhead(inputEventTime, 60);
			this.minsLeftToExpire = 60;
			this.glycemicIndexRate = (ExcerciseDB.getInstance().hmExcerciseDB.get(item).doubleValue()/60) * -1;
		}
		else
		{
			this.minsLeftToExpire = 0;
		}
		this.eventLastProcessedTime = inputEventTime;
		
	}
	
	
	public Date getEventStartTime() {
		return eventStartTime;
	}
	public void setEventStartTime(Date eventStartTime) {
		this.eventStartTime = eventStartTime;
	}
	public Date getEventEndTime() {
		return eventEndTime;
	}
	public void setEventEndTime(Date eventEndTime) {
		this.eventEndTime = eventEndTime;
	}
	public Date getEventLastProcessedTime() {
		return eventLastProcessedTime;
	}
	public void setEventLastProcessedTime(Date eventLastProcessedTime) {
		this.eventLastProcessedTime = eventLastProcessedTime;
	}
	public long getMinsLeftToExpire() {
		return minsLeftToExpire;
	}
	public void setMinsLeftToExpire(long minsLeftToExpire) {
		this.minsLeftToExpire = minsLeftToExpire;
	}
	public Double getGlycemicIndexRate() {
		return glycemicIndexRate;
	}
	public void setGlycemicIndexRate(Double glycemicIndexRate) {
		this.glycemicIndexRate = glycemicIndexRate;
	}
	
	

}
