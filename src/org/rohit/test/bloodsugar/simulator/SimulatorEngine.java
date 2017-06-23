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
	
	Date lastPlotTimeStamp = null;

	private Double bloodSugar;
	private int glycation;
	
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
		//process all inputs one by one
		for(InputEntry ip:input)
		{
			processEventQueue(ip);
		}
		
		// processing the remaining events once all input points are captured
		processRemainingEvents();
		
	
	}

	private void processEventQueue(InputEntry ip) {
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
			
			Date timePlotTimeStamp = null;
			//process all events in event queue one by one
			while(!eventQueue.isEmpty())
			{
				// fetch the first event
				MyQueueEvent temp = eventQueue.poll();
				if(lastPlotTimeStamp != null && temp.getEventStartTime().after(lastPlotTimeStamp))
				{
					// case of normalisation
					//addToBloodSugarList(this.bloodSugar, expiredTimeStamp);
					// start normalisation till this event starts
					long normaliseTime = DateHelper.getDiffInMinutes(temp.getEventStartTime(), lastPlotTimeStamp);
					// Window of time gap is larger than difference between current and base Blood Sugar
					if(normaliseTime >= this.bloodSugar - 80.0)
					{
						timePlotTimeStamp = DateHelper.getDateMinutesAhead(lastPlotTimeStamp, (long)(this.bloodSugar - 80.0));
						addToBloodSugarList(80.0, timePlotTimeStamp);
						// default to 80
						this.bloodSugar = 80.0;
						tempQueue.add(temp);
						timePlotTimeStamp = temp.getEventStartTime();
					}
					else
					{
						//  Window of time gap is smaller than difference between current and base Blood Sugar
						timePlotTimeStamp = DateHelper.getDateMinutesAhead(lastPlotTimeStamp, normaliseTime);
						this.bloodSugar = bloodSugar - normaliseTime;
						tempQueue.add(temp);
						timePlotTimeStamp = temp.getEventStartTime();
					}
					
					lastPlotTimeStamp = null;
					
				}
				else
				{
					// Case: Current event starts before an earlier event expires
					// Process the earlier event till the current input event start time
					if(ip.getTimestamp().before(DateHelper.getDateMinutesAhead(temp.getEventLastProcessedTime(), temp.getMinsLeftToExpire())))
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
						// Expire the events till their end time
						long tempDiff = temp.getMinsLeftToExpire();
						
						if(timePlotTimeStamp!=null && DateHelper.getDateMinutesAhead(temp.getEventLastProcessedTime(), temp.getMinsLeftToExpire()).after(timePlotTimeStamp))
						{
							tempDiff = DateHelper.getDiffInMinutes(timePlotTimeStamp, temp.getEventLastProcessedTime());
							temp.setEventLastProcessedTime(timePlotTimeStamp);
							temp.setMinsLeftToExpire(temp.getMinsLeftToExpire() - tempDiff);
							tempValue = temp.getGlycemicIndexRate() * tempDiff;
							updateBloodSugar(tempValue);
							tempQueue.add(temp);
							
						}
						else if(timePlotTimeStamp!=null && DateHelper.getDateMinutesAhead(temp.getEventLastProcessedTime(), temp.getMinsLeftToExpire()).before(timePlotTimeStamp))
						{
							// Adjustment event as current event is finishing before previous event
							
							tempDiff = temp.getMinsLeftToExpire();
							Date previousTimeStamp = DateHelper.getDateMinutesBefore(timePlotTimeStamp, tempDiff);
							tempValue = temp.getGlycemicIndexRate() * tempDiff;
							temp.setEventLastProcessedTime(timePlotTimeStamp);
							temp.setMinsLeftToExpire(0);
							addToBloodSugarList(this.bloodSugar - tempValue, previousTimeStamp);
							
						}
						else
						{
							timePlotTimeStamp = DateHelper.getDateMinutesAhead(temp.getEventLastProcessedTime(), temp.getMinsLeftToExpire());
							temp.setEventLastProcessedTime(timePlotTimeStamp);
							temp.setMinsLeftToExpire(0);
							tempValue = temp.getGlycemicIndexRate() * tempDiff;
							updateBloodSugar(tempValue);
						}
						
						}
				}
				
				
			}
			
			// plot the point
			addToBloodSugarList(this.bloodSugar, timePlotTimeStamp);
			// move the unprocessed events back to the event queue to be picked again for processing
			while(!tempQueue.isEmpty())
				eventQueue.add(tempQueue.poll());
			if(eventQueue.isEmpty())
			{
				lastPlotTimeStamp = timePlotTimeStamp;
				return;
			}
				
			// add the current input event to the event queue
			MyQueueEvent tempEvent = eventQueue.peek();
			System.out.println("Current Input event Time is "+ ip.getTimestamp().toString());
			System.out.println("Current head queue event last start times is :" + tempEvent.getEventStartTime());
			System.out.println("Current head queue event last processed times is :" + tempEvent.getEventLastProcessedTime());
			
			Date tempDate = DateHelper.getDateMinutesAhead(tempEvent.getEventLastProcessedTime(), tempEvent.getMinsLeftToExpire());
			System.out.println("Current head end time is "+ tempDate.toString());
			if(tempDate.after(ip.getTimestamp()))
			{
				System.out.println("current head end time is after current input start time");
				System.out.println("Conitnuing normally");
			}
			else
			{
				System.out.println("current head end time is before current input start time");
				System.out.println("Reprocessing event queue");
				processEventQueue(ip);
			}
			eventQueue.add(new MyQueueEvent(ip.getTimestamp(), ip.getType(), ip.getItem()));
		}
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
				continue;
				//timePlotTimeStamp = DateHelper.getDateMinutesAhead(timePlotTimeStamp, (long)(this.bloodSugar - 80.0));
				//addToBloodSugarList(80.0, timePlotTimeStamp);
			}
			else
			{
				timePlotTimeStamp = DateHelper.getDateMinutesAhead(temp.getEventStartTime(), minsToProcess);
				if(timePlotTimeStamp.after(DateHelper.getEndOfDayTime()))
					minsToProcess = DateHelper.getDiffInMinutes(DateHelper.getEndOfDayTime(), temp.getEventStartTime());
					// last user provided input point 
				tempValue = temp.getGlycemicIndexRate() * minsToProcess;
				updateBloodSugar(tempValue);
				addToBloodSugarList(this.bloodSugar, DateHelper.getEndOfDayTime());
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
		checkGlycation();
	}
	
	public void checkGlycation()
	{
		if(this.bloodSugar > 150)
			this.glycation++;
		
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
