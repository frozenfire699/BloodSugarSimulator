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
	
	private Date newPlotTimeStamp = null;
	private Date earliestEventEndTime = null;

	private Double combinedIndex =0.0;
	private long plottingMinutes = 0;
	
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
		this.earliestEventEndTime = DateHelper.getEndOfDayTime();
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
		
		System.out.println("#####processing input entry with timestamp######" + ip.getTimestamp().toString());
		MyQueueEvent currentInputEvent = new MyQueueEvent(ip.getTimestamp(), ip.getType(), ip.getItem());
		boolean expiredEventFlag = false;
		// check queue for previous events
		if(this.eventQueue.isEmpty())
		{
			// add the first event to queue
			MyQueueEvent temp = new MyQueueEvent(ip.getTimestamp(), ip.getType(), ip.getItem());
			temp.setEventNextPlotTime(DateHelper.getDateMinutesAhead(temp.getEventStartTime(), temp.getMinsLeftToExpire()));
			this.newPlotTimeStamp = temp.getEventStartTime();
			this.earliestEventEndTime = temp.getEventEndTime();
			//plot the point
			addToBloodSugarList();
			eventQueue.add(currentInputEvent);
		}
		else
		{
			// create a temp queue
			Queue<MyQueueEvent> tempQueue = new LinkedList<MyQueueEvent>();
			double tempValue = 0;
			this.combinedIndex = 0.0;
			
			//process all events in event queue one by one
			while(!this.eventQueue.isEmpty())
			{
				// fetch the first event
				MyQueueEvent temp = this.eventQueue.poll();
				
				// set the next timestamp until which current event from queue needs to be processed
				if(temp.getEventLastProcessedTime().before(currentInputEvent.getEventStartTime()))
				{
					temp.setEventNextPlotTime(currentInputEvent.getEventStartTime());
					// update the earliest event end time for non expired events
					if(temp.getMinsLeftToExpire()>0 && temp.getEventEndTime().before(this.earliestEventEndTime))
					{
						this.earliestEventEndTime = temp.getEventEndTime();
						//System.out.println("Updating earliest end time to " + );
					}
					if(this.earliestEventEndTime.before(temp.getEventNextPlotTime()))
					temp.setEventNextPlotTime(this.earliestEventEndTime);
				}
				
				
				if(temp.getMinsLeftToExpire() > 0 && 
						(DateHelper.getDiffInMinutes(temp.getEventEndTime(), temp.getEventNextPlotTime())) >0)
				{
					// get the difference in minutes
					long tempDiff = DateHelper.getDiffInMinutes(temp.getEventNextPlotTime(), temp.getEventLastProcessedTime());
					// update the minutes that are left for this event to expire
					temp.setMinsLeftToExpire(temp.getMinsLeftToExpire() - tempDiff);
					// update the event's last processed time
					temp.setEventLastProcessedTime(temp.getEventNextPlotTime());
					// calculate the blood sugar for the time window
					//tempValue = temp.getGlycemicIndexRate() * tempDiff;
					this.combinedIndex = this.combinedIndex + temp.getGlycemicIndexRate();
					this.plottingMinutes = tempDiff;
					// add the event to temp queue to be processed again
					tempQueue.add(temp);
					// update the timestamp to be plotted next
					this.newPlotTimeStamp = temp.getEventNextPlotTime();
				}
				else if(temp.getMinsLeftToExpire() > 0 && 
						(DateHelper.getDiffInMinutes(temp.getEventEndTime(), temp.getEventNextPlotTime())) == 0)
				{
					// expire the current event
					// Expire the events till their end time
					long tempDiff = temp.getMinsLeftToExpire();
					
					temp.setMinsLeftToExpire(0);
					// update the event's last processed time
					temp.setEventLastProcessedTime(temp.getEventNextPlotTime());
					// calculate the blood sugar for the time window
					//tempValue = temp.getGlycemicIndexRate() * tempDiff;
					this.combinedIndex = this.combinedIndex + temp.getGlycemicIndexRate();
					this.plottingMinutes = tempDiff;
					this.newPlotTimeStamp = temp.getEventNextPlotTime();
					expiredEventFlag = true;
					
					
				}		
				
			}
				
			
			// plot the point
			addToBloodSugarList();
			// move the unprocessed events back to the event queue to be picked again for processing
			while(!tempQueue.isEmpty())
				eventQueue.add(tempQueue.poll());
			
			if(expiredEventFlag)
				this.earliestEventEndTime = DateHelper.getEndOfDayTime();
			
			eventQueue.add(currentInputEvent);
				
			/*// add the current input event to the event queue
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
			}*/
			
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

				addToBloodSugarList();
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
	public void updateBloodSugar()
	{
		bloodSugar = this.bloodSugar + this.plottingMinutes * this.combinedIndex;
		checkGlycation();
	}
	
	/**
	 * This method tracks the glycation value
	 */
	public void checkGlycation()
	{
		// check the number of minutes blood sugar was above 150
		if(this.bloodSugar > 150)
		{
			
			Double bloodSugarDiff = this.bloodSugar - 150;
			this.glycation = (int) (this.glycation + bloodSugarDiff/this.combinedIndex);
		}
			
		
	}
	
	/**
	 * List to capture the plotting points on the graph for blood sugar
	 * @param bloodSugar
	 * @param timestamp
	 */
	public void addToBloodSugarList()
	{
		updateBloodSugar();
		OutputPoint output = new OutputPoint();
		output.setBloodSugar(this.bloodSugar);
		output.setTimestamp(this.newPlotTimeStamp);
		this.bloodSugarList.add(output);
		System.out.println("******************BLOOD SUGAR GRAPH POINT******************");
		System.out.println("blood Sugar--->" + bloodSugar);
		System.out.println("Time--->" + this.newPlotTimeStamp.toString());
		
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
