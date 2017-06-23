package org.rohit.test.bloodsugar.model;

import java.util.Date;

/** Model class that will contain information to be plotted on the graph
 * 
 * @author Frozenfire
 *
 */
public class OutputPoint {

	private Double value;
	private Date timestamp;
	public Double getValue() {
		return value;
	}
	public void setBloodSugar(double value) {
		this.value = value;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
