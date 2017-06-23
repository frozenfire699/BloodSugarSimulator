package org.rohit.test.bloodsugar.model;

import java.util.Calendar;
import java.util.Date;

/**
 * Model class for Input data entry
 * @author Frozenfire
 *
 */
public class InputEntry {

	private String Item;
	private String type;
	private Date timestamp;
	
	public InputEntry(){
	}
	
	public InputEntry (String item, String type, int hour, int min, int sec)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY,hour);
		cal.set(Calendar.MINUTE,min);
		cal.set(Calendar.SECOND,sec);
		Date d = cal.getTime();
		this.Item = item;
		this.type = type;
		this.timestamp = d;
		
	}
	
	
	public String getItem() {
		return Item;
	}
	public void setItem(String item) {
		Item = item;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
