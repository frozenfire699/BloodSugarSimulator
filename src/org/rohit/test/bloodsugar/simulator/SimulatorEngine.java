package org.rohit.test.bloodsugar.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.rohit.test.bloodsugar.model.InputEntry;
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
	
	private Date lastProcessedTime;
	private long bloodSugar;
	private long glycation;
	
	/**
	 * Default constructor
	 * Initializing the 2 lists, one for Blood Sugar and other for Glycation output points
	 * Defaulting blood sugar and glycation levels to 80 and 0 respectively
	 */
	public SimulatorEngine() {
		bloodSugarList = new ArrayList<OutputPoint>();
		glycationList = new ArrayList<OutputPoint>();
		lastProcessedTime = DateHelper.getMidnightTime(); // defaulting to start of day
		bloodSugar = 80;
		glycation = 0;
		
	}
	
	/**
	 * This method will take user input data and calculate its impact on Blood Sugar
	 * and Glycation levels
	 */
	public void processUserInputs(List<InputEntry> input)
	{
		Date inputTime;
		//process all inputs one by one
		for(InputEntry ip:input)
		{
			inputTime = ip.getTimestamp();
			OutputPoint output;
			// check if the current entry is within 2 hours
			if(isNormalisationRequired(inputTime))
			{
				// More than 2 hours have passed by
				// Create a graph point, to capture the 2 hour mark
				Date newTimePoint = DateHelper.getTwoHourPointFromLastProcessedTime(lastProcessedTime);
				
				// add the additional output point to be captured on graph
				addToBloodSugarList(this.bloodSugar, newTimePoint);
				
				// start normalising the blood Sugar from this new time point
				normaliseBloodSugar(inputTime);
				
				// add the normalised blood sugar at the current input point
				addToBloodSugarList(this.bloodSugar, inputTime);
			
			}
			else
			{
				// New input is within 2 hours of last input
				
			}
		}
	
	}
	
	
	
	/**
	 * This method checks if 2 hours have passed by before the last processed input
	 * If yes, normalisation of blood sugar should start so method return true
	 * If no, method returns false
	 * @param inputTime
	 * @return
	 */
	public boolean isNormalisationRequired(Date inputTime)
	{
		boolean normalisationFlag = false;
		long diffInMillies = inputTime.getTime() - lastProcessedTime.getTime();
		long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
		if(diffInMinutes > 120)
			normalisationFlag = true;
		return normalisationFlag;
	}
	
	/**
	 * This method will normalise the blood sugar level to the current timestamp
	 * @param inputTime
	 */
	public void normaliseBloodSugar(Date inputTime)
	{
		long diffInMillies = inputTime.getTime() - lastProcessedTime.getTime();
		long diffInMinutes = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
		long bloodSugarGap = bloodSugar - 80;
		if(diffInMinutes <= bloodSugarGap)
		bloodSugar = bloodSugar - diffInMinutes;
		else
		{
			Date newTimePoint = DateHelper.getDateMinutesAhead(lastProcessedTime, diffInMinutes );
			bloodSugar = 80;
			OutputPoint output = new OutputPoint();
			output.setBloodSugar(this.bloodSugar);
			output.setTimestamp(newTimePoint);
			lastProcessedTime = newTimePoint;
		}
	}
	
	public void addToBloodSugarList(long bloodSugar, Date timestamp)
	{
		OutputPoint output = new OutputPoint();
		output.setBloodSugar(bloodSugar);
		output.setTimestamp(timestamp);
		this.bloodSugarList.add(output);
		
		lastProcessedTime = timestamp;
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
