package org.rohit.test.bloodsugar.model;

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
