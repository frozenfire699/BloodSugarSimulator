package org.rohit.test.bloodsugar.mockups;

import java.util.HashMap;

public class ExcerciseDB {
	
	public HashMap<String, Integer> hmExcerciseDB = new HashMap<String, Integer>();

	public void constructMap()
	{
		
		hmExcerciseDB.put("Crunching",47);
		hmExcerciseDB.put("Walking",55);
		hmExcerciseDB.put("Running",46);
		hmExcerciseDB.put("Spriting",42);
		hmExcerciseDB.put("Squats",44);
		hmExcerciseDB.put("Bench press",48);
		
	}

	public HashMap<String, Integer> getHmExcerciseDB() {
		return hmExcerciseDB;
	}

	public void setHmExcerciseDB(HashMap<String, Integer> hmExcerciseDB) {
		this.hmExcerciseDB = hmExcerciseDB;
	}

}
