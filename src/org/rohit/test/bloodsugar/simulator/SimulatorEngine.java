package org.rohit.test.bloodsugar.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeSet;

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
	
	private Queue<MyQueueEvent> eventQueue;
	
	private TreeSet<Date> plottingTimes;
	
	private Date lastPlottedTime ;
	private Date toBePlottedTime ;

	private Double combinedIndex =0.0;
	private long plottingMinutes = 0;
	
	private Double bloodSugar;
	private int glycation;
	private Date glycationHighTimeStamp;
	private boolean glycationFlag;
	private Double lastGlycationBloodSugar;
	
	/**
	 * Default constructor
	 * Initializing the 2 lists, one for Blood Sugar and other for Glycation output points
	 * Defaulting blood sugar and glycation levels to 80 and 0 respectively
	 */
	public SimulatorEngine() {
		this.plottingTimes = new TreeSet<Date>();
		this.eventQueue = new LinkedList();
		this.bloodSugarList = new ArrayList<OutputPoint>();
		this.bloodSugar = 80.0;
		this.glycation = 0;
		this.glycationFlag = false;
		
	}
	
	/**
	 * This method will take user input data and calculate its impact on Blood Sugar
	 * and Glycation levels
	 */
	public void processUserInputs(List<InputEntry> input)
	{
		//this.earliestEventEndTime = DateHelper.getEndOfDayTime();
		input = addEndPointOfDay(input);
		//process all inputs one by one
		for(InputEntry ip:input)
		{
			MyQueueEvent currentInputEvent = new MyQueueEvent(ip.getTimestamp(), ip.getType(), ip.getItem());
			processEventQueue(currentInputEvent);
		}
		
		// processing the remaining events once all input points are captured
		while(!this.plottingTimes.isEmpty())
		{
			processEventQueue(null);
		}
		
		System.out.println("all events processed");
	}

	private void processEventQueue(MyQueueEvent currentInputEvent) {
		
		boolean normalisationFlag = true;
		// check queue for previous events
		if(this.eventQueue.isEmpty())
		{
			// add the first event to queue
			currentInputEvent.setEventNextPlotTime(DateHelper.getDateMinutesAhead(currentInputEvent.getEventStartTime(),
					currentInputEvent.getMinsLeftToExpire()));
			updatePlottingTimeSet(currentInputEvent.getEventStartTime(), currentInputEvent.getEventEndTime());
			
			//this.newPlotTimeStamp = temp.getEventStartTime();
			//this.earliestEventEndTime = temp.getEventEndTime();
			//plot the point
			addToBloodSugarList();
			this.eventQueue.add(currentInputEvent);
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
				if(currentInputEvent !=null)
				updatePlottingTimeSet(currentInputEvent.getEventStartTime(), currentInputEvent.getEventEndTime());
				else
					updatePlottingTimeSet(null, null);
				
				// set the next timestamp until which current event from queue needs to be processed
				if(temp.getEventLastProcessedTime().before(this.toBePlottedTime))
				{
					temp.setEventNextPlotTime(this.toBePlottedTime);
					normalisationFlag = false;
				}
				else
				{
					// case where event is starting after the current time stamp
					if(normalisationFlag)
					{
						startNormalisation(temp);
						normalisationFlag = false;
					}
					tempQueue.add(temp);
					continue;
				}
				
				
				if(temp.getMinsLeftToExpire() > 0)
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
					if(temp.getMinsLeftToExpire()>0)
					tempQueue.add(temp);
					// update the timestamp to be plotted next
					//this.newPlotTimeStamp = temp.getEventNextPlotTime();
				}
			}
				
			
			// plot the point
			addToBloodSugarList();
			// move the unprocessed events back to the event queue to be picked again for processing
			while(!tempQueue.isEmpty())
				this.eventQueue.add(tempQueue.poll());
			if(currentInputEvent!=null)
			this.eventQueue.add(currentInputEvent);
			
			
		}
	}
	
	public void startNormalisation(MyQueueEvent temp)
	{
		long normaliseTime = DateHelper.getDiffInMinutes(temp.getEventStartTime(), this.lastPlottedTime);
		// Window of time gap is larger than difference between current and base Blood Sugar
		if(normaliseTime >= this.bloodSugar - 80.0)
		{
		  	this.toBePlottedTime = DateHelper.getDateMinutesAhead(this.lastPlottedTime, (long)(this.bloodSugar - 80.0));
		  	this.bloodSugar = 80.0;
		  	addToBloodSugarListNormalised();								
		}
		else
		{
			// Window if time gap is smaller than difference between current and base blood sugar
		}
	}
	
	public void updatePlottingTimeSet(Date eventStartTime, Date eventEndTime)
	{
		if(eventStartTime==null)
		{
			this.toBePlottedTime = this.plottingTimes.first();
		}
		else
		{
			this.plottingTimes.add(eventStartTime);
			this.plottingTimes.add(eventEndTime);
			this.toBePlottedTime = this.plottingTimes.first();
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
		while(!this.eventQueue.isEmpty()){
			MyQueueEvent temp = this.eventQueue.poll();
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
		this.bloodSugar = this.bloodSugar + this.plottingMinutes * this.combinedIndex;
		checkGlycation();
	}
	
	/**
	 * This method tracks the glycation value
	 */
	public void checkGlycation()
	{
		// check the number of minutes blood sugar was above 150
		if(this.bloodSugar > 150 && !this.glycationFlag)
		{
			this.glycationFlag = true;
			this.glycationHighTimeStamp = this.toBePlottedTime;
			Double bloodSugarDiff = this.bloodSugar - 150;
			this.glycation = (int) (this.glycation + bloodSugarDiff/Math.abs(this.combinedIndex));
		}
		else
		{
			if(this.bloodSugar > 150 && this.glycationFlag)
			{
				this.glycation = (int) (this.glycation + DateHelper.getDiffInMinutes(this.toBePlottedTime, this.glycationHighTimeStamp));
				this.lastGlycationBloodSugar = this.bloodSugar;
				this.glycationHighTimeStamp = this.toBePlottedTime;
			}
			else if(this.glycationFlag)
			{
				this.glycationFlag = false;
				long bloodSugarDiff = (long) (this.lastGlycationBloodSugar - 150);
				//this.glycationHighTimeStamp = DateHelper.getDateMinutesAhead(this.glycationHighTimeStamp, bloodSugarDiff);
				this.glycation = (int) (this.glycation + bloodSugarDiff/Math.abs(this.combinedIndex));
				
			}
				
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
		this.lastPlottedTime = this.plottingTimes.pollFirst();
		output.setTimestamp(this.lastPlottedTime);
		this.bloodSugarList.add(output);
		System.out.println("******************BLOOD SUGAR GRAPH POINT******************");
		System.out.println("blood Sugar--->" + this.bloodSugar);
		System.out.println("Time--->" + this.lastPlottedTime.toString());
		
	}
	
	/**
	 * List to capture the plotting points on the graph for blood sugar
	 * @param bloodSugar
	 * @param timestamp
	 */
	public void addToBloodSugarListNormalised()
	{
		OutputPoint output = new OutputPoint();
		output.setBloodSugar(this.bloodSugar);
		output.setTimestamp(this.toBePlottedTime);
		this.bloodSugarList.add(output);
		System.out.println("******************BLOOD SUGAR GRAPH POINT******************");
		System.out.println("blood Sugar--->" + this.bloodSugar);
		System.out.println("Time--->" + this.toBePlottedTime);
		
	}
	
	
	public List getBloodSugarList() {
		return bloodSugarList;
	}
	public void setBloodSugarList(List bloodSugarList) {
		this.bloodSugarList = bloodSugarList;
	}

	public int getGlycation() {
		return glycation;
	}

	public void setGlycation(int glycation) {
		this.glycation = glycation;
	}
	
	
}
