/**
 * My Reminder Bot
 * Copyright (c) 2011 by Romin Irani. All Rights Reserved.
 */
package com.thirdchimpanzee.myreminderbot.entity;

/**
 * Utility class that encapsulates the Reminder syntax that the Bot understands
 * 
 * The class always normalizes the reminder trigger time in minutes. For e.g. if the user
 * gives the following command 10h Call Dentist, the minutes field will be populated with 
 * 10 * 60 mins = 600 (minutes) and the msg field will be populated with  'Call Dentist'
 * 
 * @author irani_r
 * @version 1.0
 * 
 */

public class ReminderCommand {
	

	private static final long serialVersionUID = 1L;
	private int minutes;
	private String message;
	/**
	 * @return the minutes
	 */
	public int getMinutes() {
		return minutes;
	}
	/**
	 * @param minutes the minutes to set
	 */
	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
