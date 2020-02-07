package com.custom.diab.demos.agents;

import com.yantra.yfc.date.YTimestamp;

public class SEDataExtractor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	      YTimestamp currentTime = YTimestamp.newTimestamp();
	      YTimestamp nextRun = YTimestamp.newTimestamp(currentTime, 10);

	      System.out.println ("Current Time = " + currentTime + " Next Run = " + nextRun);

	}

}
