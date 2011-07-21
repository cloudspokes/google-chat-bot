/**
 * My Reminder Bot
 * Copyright (c) 2011 by Romin Irani. All Rights Reserved.
 */

package com.thirdchimpanzee.myreminderbot;

/**
 * Custom Exception class that is raised when the Reminder Syntax is given incorrectly by the user
 * 
 * @author irani_r
 * @version 1.0
 */ 

@SuppressWarnings("serial")
public class MessageParseException extends Exception{

	public MessageParseException(String message){
		super(message);
	}
}
