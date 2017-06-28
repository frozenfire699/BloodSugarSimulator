package org.rohit.test.bloodsugar.model;

import java.util.Date;

import org.rohit.test.bloodsugar.mockups.ExcerciseDB;
import org.rohit.test.bloodsugar.mockups.FoodDB;
import org.rohit.test.bloodsugar.util.DateHelper;

public class MyQueueEvent {
	
	private Date eventStartTime;
	private Date eventEndTime;
	private Date eventNextPlotTime;
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
			this.minsLeftToExpire = 120;
			this.eventEndTime = DateHelper.getDateMinutesAhead(inputEventTime, 120);
			this.eventNextPlotTime = this.eventEndTime;
			this.glycemicIndexRate = FoodDB.getInstance().hmFoodDB.get(item).doubleValue()/120;
		}
		else if(eventType.equalsIgnoreCase("EXC")){
			this.minsLeftToExpire = 60;
			this.eventEndTime = DateHelper.getDateMinutesAhead(inputEventTime, 60);
			this.eventNextPlotTime = this.eventEndTime;
			this.glycemicIndexRate = (ExcerciseDB.getInstance().hmExcerciseDB.get(item).doubleValue()/60) * -1;
		}
		else
		{
			this.eventEndTime = this.eventStartTime;
			this.eventNextPlotTime = this.eventStartTime;
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

	public Date getEventNextPlotTime() {
		return eventNextPlotTime;
	}

	public void setEventNextPlotTime(Date eventNextPlotTime) {
		if(eventNextPlotTime.after(DateHelper.getDateMinutesAhead(eventLastProcessedTime, minsLeftToExpire)))
		this.eventNextPlotTime = DateHelper.getDateMinutesAhead(eventLastProcessedTime, minsLeftToExpire);
		else
			this.eventNextPlotTime = eventNextPlotTime;
	}

	public Date getEventEndTime() {
		return eventEndTime;
	}

	public void setEventEndTime(Date eventEndTime) {
		this.eventEndTime = eventEndTime;
	}
	
	

}
