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

	private List<OutputPoint> bloodSugarList; // List which contains blood sugar points to be plotted on the graph
	
	private Queue<MyQueueEvent> eventQueue; // Queue collection to be used by the engine to process input data points
	
	private TreeSet<Date> plottingTimes; // Collection to store all plotting timestamps on the graph
	
	private Date lastPlottedTime ; // Variable to capture the last plotted timestamp
	private Date toBePlottedTime ; // Variable to capture the next timestamp to be plotted

	private Double combinedIndex =0.0; // Cumilative index for the plotting period
	private long plottingMinutes = 0; // Number of minutes for the plotting period
	
	private Double bloodSugar; // variable to capture blood sugar at a particular timestamp 
	private int glycation; // variable to capture glycation at a particular timestamp
	private Date glycationHighTimeStamp; // variable to capture the last processed glycation timestamp
	private boolean glycationFlag; // glycation flag to check the glycation zone
	private Double lastGlycationBloodSugar; // variable to capture last processed blood sugar in glycation zone
	
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

	/**
	 * This method will process  previous input points until the start time of the current event
	 * 
	 * @param currentInputEvent
	 */
	private void processEventQueue(MyQueueEvent currentInputEvent) {
		
		boolean normalisationFlag = true;
		// check queue for previous events
		if(this.eventQueue.isEmpty())
		{
			// add the first event to queue
			currentInputEvent.setEventNextPlotTime(DateHelper.getDateMinutesAhead(currentInputEvent.getEventStartTime(),
					currentInputEvent.getMinsLeftToExpire()));
			updatePlottingTimeSet(currentInputEvent.getEventStartTime(), currentInputEvent.getEventEndTime());
			
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
				}
			}
				
			
			// plot the point
			addToBloodSugarList();
			// move the unprocessed events back to the event queue to be picked again for processing
			while(!tempQueue.isEmpty())
				this.eventQueue.add(tempQueue.poll());
			// Do not add the last marker event back to queue
			if(currentInputEvent!=null)
			this.eventQueue.add(currentInputEvent);
			
			
		}
	}
	
	/**
	 * This method plots the normalisation aspect of the blood sugar level
	 * @param temp
	 */
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
	
	/**
	 * This method adds all the time stamps that needs to be plotted on the graph in a Treeset
	 *
	 * @param eventStartTime
	 * @param eventEndTime
	 */
	public void updatePlottingTimeSet(Date eventStartTime, Date eventEndTime)
	{
		// This is a marker event for end of day
		if(eventStartTime==null)
		{
			this.toBePlottedTime = this.plottingTimes.first();
		}
		else
		{
			// Add starting and ending time of current input event
			this.plottingTimes.add(eventStartTime);
			this.plottingTimes.add(eventEndTime);
			this.toBePlottedTime = this.plottingTimes.first();
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
			// Currently in the glycation zone
			if(this.bloodSugar > 150 && this.glycationFlag)
			{
				this.glycation = (int) (this.glycation + DateHelper.getDiffInMinutes(this.toBePlottedTime, this.glycationHighTimeStamp));
				this.lastGlycationBloodSugar = this.bloodSugar;
				this.glycationHighTimeStamp = this.toBePlottedTime;
			}
			else if(this.glycationFlag)
			{
				// blood sugar has decreased from 150 in the current plot time zone
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
