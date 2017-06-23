package org.rohit.test.bloodsugar.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.rohit.test.bloodsugar.model.InputEntry;
import org.rohit.test.bloodsugar.model.MyQueueEvent;
import org.rohit.test.bloodsugar.model.OutputPoint;
import org.rohit.test.bloodsugar.util.DateHelper;

/**
 * This class will act the simulator engine and will be responsible for providing
 * the points that will be plotted on the graph
 * 
 * @author Frozenfire
 *
 */
public class SimulatorEngine {

	private List<OutputPoint> bloodSugarList;
	private List<OutputPoint> glycationList;
	
	private Queue<MyQueueEvent> eventQueue;

	private Double bloodSugar;
	private long glycation;
	
	/**
	 * Default constructor
	 * Initializing the 2 lists, one for Blood Sugar and other for Glycation output points
	 * Defaulting blood sugar and glycation levels to 80 and 0 respectively
	 */
	public SimulatorEngine() {
		eventQueue = new LinkedList();
		bloodSugarList = new ArrayList<OutputPoint>();
		glycationList = new ArrayList<OutputPoint>();
		bloodSugar = 80.0;
		glycation = 0;
		
	}
	
	/**
	 * This method will take user input data and calculate its impact on Blood Sugar
	 * and Glycation levels
	 */
	public void processUserInputs(List<InputEntry> input)
	{
		input = addEndPointOfDay(input);
		Date inputTime;
		//process all inputs one by one
		for(InputEntry ip:input)
		{
			// check queue for previous events
			if(eventQueue.isEmpty())
			{
				// add the first event to queue
				eventQueue.add(new MyQueueEvent(ip.getTimestamp(), ip.getType(), ip.getItem()));
				//plot the point
				addToBloodSugarList(this.bloodSugar, ip.getTimestamp());
			}
			else
			{
				// create a temp queue
				Queue<MyQueueEvent> tempQueue = new LinkedList<MyQueueEvent>();
				double tempValue = 0;
				Date expiredTimeStamp = null;
				Date timePlotTimeStamp = null;
				//process all events in event queue one by one
				while(!eventQueue.isEmpty())
				{
					// fetch the first event
					MyQueueEvent temp = eventQueue.poll();
					// if the current event is before the the head element end time
					if(ip.getTimestamp().before(temp.getEventEndTime()))
					{
						// get the difference in minutes
						long tempDiff = DateHelper.getDiffInMinutes(ip.getTimestamp(),temp.getEventLastProcessedTime());
						// update the minutes that are left for this event to expire
						temp.setMinsLeftToExpire(temp.getMinsLeftToExpire() - tempDiff);
						// update the event's last processed time
						temp.setEventLastProcessedTime(ip.getTimestamp());
						// calculate the blood sugar for the time window
						tempValue = temp.getGlycemicIndexRate() * tempDiff;
						// update the blood sugar
						updateBloodSugar(tempValue);
						// add the event to temp queue to be processed again
						tempQueue.add(temp);
						// update the timestamp to be plotted next
						timePlotTimeStamp = ip.getTimestamp();
					}
					else
					{
						if(expiredTimeStamp == null)
						{
							// Expire the events till their end time
							long tempDiff = temp.getMinsLeftToExpire();
							expiredTimeStamp = DateHelper.getDateMinutesAhead(temp.getEventLastProcessedTime(), tempDiff);
							tempValue = temp.getGlycemicIndexRate() * tempDiff;
							updateBloodSugar(tempValue);
							timePlotTimeStamp = expiredTimeStamp;
						}
						else
						{
					
							if(temp.getEventStartTime().before(expiredTimeStamp) && expiredTimeStamp.before(temp.getEventEndTime()))
							{
								long tempDiff = DateHelper.getDiffInMinutes(expiredTimeStamp, temp.getEventLastProcessedTime());
								temp.setMinsLeftToExpire(temp.getMinsLeftToExpire() - tempDiff);
								temp.setEventLastProcessedTime(expiredTimeStamp);
								tempValue = temp.getGlycemicIndexRate() * tempDiff;
								updateBloodSugar(tempValue);
								tempQueue.add(temp);
								timePlotTimeStamp = expiredTimeStamp;
							}
							else if(temp.getEventStartTime().before(expiredTimeStamp) && expiredTimeStamp.after(temp.getEventEndTime()))
							{
								// adjusting scenario where another event has already finished
								long tempDiff = temp.getMinsLeftToExpire();
								Date previousTimeStamp = DateHelper.getDateMinutesBefore(expiredTimeStamp, tempDiff);
								tempValue = temp.getGlycemicIndexRate() * tempDiff;
								timePlotTimeStamp = previousTimeStamp;
								addToBloodSugarList(this.bloodSugar - tempValue, timePlotTimeStamp);
							}
							else if(temp.getEventStartTime().after(expiredTimeStamp))
							{
								addToBloodSugarList(this.bloodSugar, expiredTimeStamp);
								// start normalisation till this event starts
								long normaliseTime = DateHelper.getDiffInMinutes(temp.getEventStartTime(), expiredTimeStamp);
								if(normaliseTime >= this.bloodSugar - 80.0)
								{
									timePlotTimeStamp = DateHelper.getDateMinutesAhead(expiredTimeStamp, (long)(this.bloodSugar - 80.0));
									addToBloodSugarList(80.0, timePlotTimeStamp);
									this.bloodSugar = 80.0;
									tempQueue.add(temp);
									timePlotTimeStamp = temp.getEventStartTime();
								}
								else
								{
									timePlotTimeStamp = DateHelper.getDateMinutesAhead(expiredTimeStamp, normaliseTime);
									addToBloodSugarList(bloodSugar - normaliseTime, timePlotTimeStamp);
									this.bloodSugar = bloodSugar - normaliseTime;
									tempQueue.add(temp);
									timePlotTimeStamp = temp.getEventStartTime();
								}
								
							}
						}
						
					}
					
				}
				
				addToBloodSugarList(this.bloodSugar, timePlotTimeStamp);
				while(!tempQueue.isEmpty())
					eventQueue.add(tempQueue.poll());
				eventQueue.add(new MyQueueEvent(ip.getTimestamp(), ip.getType(), ip.getItem()));
			}
		}
		
		processRemainingEvents();
		
	
	}
	
	/**
	 * This method will process the remaining events in the event queue
	 * 
	 */
	public void processRemainingEvents()
	{
		double tempValue = 0;
		Date timePlotTimeStamp = null;
		while(!eventQueue.isEmpty()){
			MyQueueEvent temp = eventQueue.poll();
			long minsToProcess = temp.getMinsLeftToExpire();
			// this is the manually added last time point of the day
			if(minsToProcess==0)
			{
				timePlotTimeStamp = DateHelper.getDateMinutesAhead(timePlotTimeStamp, (long)(this.bloodSugar - 80.0));
				addToBloodSugarList(80.0, timePlotTimeStamp);
			}
			else
			{
				// last user provided input point 
				tempValue = temp.getGlycemicIndexRate() * minsToProcess;
				updateBloodSugar(tempValue);
				timePlotTimeStamp = DateHelper.getDateMinutesAhead(temp.getEventStartTime(), minsToProcess);
				addToBloodSugarList(this.bloodSugar, timePlotTimeStamp);
			}
			
		}
		
	}
	
	/**
	 * This is a method to add an additional input point to mark the end of the day
	 * @param list
	 * @return
	 */

	public List<InputEntry> addEndPointOfDay(List<InputEntry> list)
	{
		list.add(new InputEntry("","", 23, 59, 59));
		return list;
	}
	
	
	/**
	 * Method to update blood sugar
	 * @param value
	 */
	public void updateBloodSugar(Double value)
	{
		bloodSugar = bloodSugar + value;
	}
	
	/**
	 * List to capture the plotting points on the graph for blood sugar
	 * @param bloodSugar
	 * @param timestamp
	 */
	public void addToBloodSugarList(Double bloodSugar, Date timestamp)
	{
		OutputPoint output = new OutputPoint();
		output.setBloodSugar(bloodSugar);
		output.setTimestamp(timestamp);
		this.bloodSugarList.add(output);
		
	}
	
	public List getBloodSugarList() {
		return bloodSugarList;
	}
	public void setBloodSugarList(List bloodSugarList) {
		this.bloodSugarList = bloodSugarList;
	}
	public List getGlycationList() {
		return glycationList;
	}
	public void setGlycationList(List glycationList) {
		this.glycationList = glycationList;
	}
	
	
}
