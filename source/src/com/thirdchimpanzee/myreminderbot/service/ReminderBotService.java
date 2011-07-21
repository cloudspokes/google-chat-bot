/**
 * My Reminder Bot
 * Copyright (c) 2011 by Romin Irani. All Rights Reserved.
 */
package com.thirdchimpanzee.myreminderbot.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.thirdchimpanzee.myreminderbot.MessageParseException;
import com.thirdchimpanzee.myreminderbot.entity.Reminder;
import com.thirdchimpanzee.myreminderbot.entity.ReminderCommand;

/**
 * Main Service Functionality
 * 
 * This is the workhorse of the application. This class is responsible for 2 main types of functionalities:
 * 
 * 1. All Database interactions
 * 2. The method sendNotifications() which is invoked by the Cron Job to send out Reminders that have got triggered.
 * 
 * It also contains other utility methods that are central to the Bot like parsing the Reminder string.
 * 
 * @author irani_r
 * @version 1.0
 * 
 */

public class ReminderBotService {
	public static final Logger _logger = Logger.getLogger(ReminderBotService.class.toString());

	private static ReminderBotService _self = null;

	private ReminderBotService() {
	}

	public static ReminderBotService getInstance() {
		if (_self == null) {
			_self = new ReminderBotService();
			ObjectifyService.register(Reminder.class);
		}
		return _self;
	}
	
	/**
	 * This method is invoked by the Chat Servlet to interpret and parse out the Reminder message. The input parameter message is checked for a 
	 * Regular Expression that contains the day (d) , minutes (m) and hours (h). On successful parsing, it builds a ReminderCommand that always 
	 * normalizes the reminder trigger time in minutes
	 * @param message The string to parse
	 * @return ReminderCommand The object that contains the successfully parsed Reminder message in interval of minutes
	 * @throws MessageParseException
	 */
	public ReminderCommand validateAndParseMessage(String message) throws MessageParseException{
		ReminderCommand reminder = null;
		if (message != null && message.trim().length() > 0){
			String remindeAtStr = message.substring(0, message.indexOf(" ")).trim().toLowerCase();			
			String regexPattern = "(\\d*)([dhm])";
			Pattern pattern = Pattern.compile(regexPattern);
			Matcher matcher = pattern.matcher(remindeAtStr);
			if (matcher.matches()){
				int reminderValue;
				try{
					reminderValue = Integer.parseInt(matcher.group(1));
				}catch (Exception e) {
					throw new MessageParseException("Invalid Value");
				}
				String reminderType = matcher.group(2);
				if (reminderType.equals("d")){
					reminderValue = reminderValue * 24 * 60; 
				}else if (reminderType.equals("h")){
					reminderValue = reminderValue * 60;
				}
				reminder = new ReminderCommand();
				reminder.setMessage(message.substring(message.indexOf(" "))); 
				reminder.setMinutes(reminderValue);
			}else{
				throw new MessageParseException("Invalid Reminder format. Reminder format is as follows : A positve number followed by d or h or m (where d = days, h = hours and m = minutes) and then followed by space and a reminder message. For example 10d Dentist Appointment");
			}
		}
		
		return reminder;
	}
	
	/*****************************************************************************************************
	 * Database Calls
	 * The Google App Engine uses the Big Table datastore. All interactions with the Datastore are handled via the 
	 * Objectify Framework, which is a simple to use framework for the Google App Engine Datastore. 
	 * Refer to http://code.google.com/p/objectify-appengine/
	 *****************************************************************************************************/
	
	/**
	 * This method adds a Reminder to the Datastore.
	 * @param userId The Jabber Id of the user
	 * @param emailAddress The Email Address of the user
	 * @param msg The Reminder message that the user would like to receive when the reminder is triggered.
	 * @param requestDate The date / time on which the user made the request to add the reminder.
	 * @param reminderDate The date / time on which the reminder will be triggered 
	 * @param status The current Status of the reminder. On creation, it is set as ACTIVE. On successful sending of the Reminder, it is set to SENT
	 * @return A status message that indicates the success of the call
	 * @throws Exception
	 */
	public String addReminder(String userId, String emailAddress,String msg, Date requestDate, Date reminderDate, String status) throws Exception {
		Objectify obj = ObjectifyService.begin();
		Reminder _record = new Reminder();
		_record.setUserid(userId);
		_record.setEmailAddress(emailAddress);
		_record.setMsg(msg);
		_record.setStatus(status);
		_record.setRequestDate(requestDate);
		_record.setReminderDate(reminderDate);
		obj.put(_record);
		return "success";
	}
	
	/**
	 * This method updates the Status of the Reminder. 
	 * @param reminderId The ID for the Reminder datastore object
	 * @param status The status to set for the Reminder
	 * @return A status message that indicates the success of the call
	 * @throws Exception
	 */
	public String updateReminderStatus(String reminderId,String status) throws Exception {
		Objectify obj = ObjectifyService.begin();
		Reminder _record = findReminderByID(reminderId);
		_record.setStatus(status);
		obj.put(_record);
		return "success";
	}
	
	/**
	 * This method is used to retrieve a particular Reminder from the datastore by ID
	 * @param reminderId The ID for the Reminder datastore object
	 * @return A status message that indicates the success of the call
	 */
	public Reminder findReminderByID(String reminderId) {
		try {
			Objectify obj = ObjectifyService.begin();
			Reminder r = obj.query(Reminder.class).filter("id",reminderId).get();
			if (r != null)
				return r;
			return null;
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * This method retrieves all the Reminders for a particular account and by status
	 * 
	 * @param emailId The emailid i.e. account under which you want to search for reminders
	 * @param status The status of the Reminders that you want to search for
	 * @return A status message that indicates the success of the call
	 * @throws Exception
	 */
	public List<Reminder> getAllRemindersByEmailId(String emailId, String status) throws Exception {
		List<Reminder> _results = new ArrayList<Reminder>();
		Objectify obj = ObjectifyService.begin();
		_results = obj.query(Reminder.class).filter("emailAddress",emailId).filter("status",status).list();
		return _results;
	}

	/**
	 * This method retrieves all the Reminders which have a specific Status value.
	 * 
	 * @param status The status of the Reminders that you want to search for
	 * @return A status message that indicates the success of the call
	 * @throws Exception
	 */
	public List<Reminder> getAllReminders(String status) throws Exception {
		List<Reminder> _results = new ArrayList<Reminder>();
		Objectify obj = ObjectifyService.begin();
		_results = obj.query(Reminder.class).filter("status",status).list();
		return _results;
	}

	/**
	 * This method is used to retrieve a particular Reminder object in the datastore by ID and EmailId
	 * @param reminderId The ID of the Reminder Object in the datastore
	 * @param emailId The emailid of the account that is the owner of the Reminder
	 * @return A status message that indicates the success of the call
	 */
	public Reminder findReminderByIDandEmailId(String reminderId, String emailId) {
		try {
			Objectify obj = ObjectifyService.begin();
			Reminder r = obj.query(Reminder.class).filter("id",reminderId).filter("emailAddress",emailId).get();
			if (r != null)
				return r;
			return null;
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * This method is used to deelte a Reminder object in the datastore by providing the Reminder Object Key and the Email Id of the account that is the 
	 * owner of the Reminder
	 * @param reminderId The ID of the Reminder Object in the datastore
	 * @param emailId The emailid of the account that is the owner of the Reminder
	 * @return A status message that indicates the success of the call
	 * @throws Exception
	 */
	public String removeReminderByIDandEmailId(String reminderId,String emailId) throws Exception {
			Objectify obj = ObjectifyService.begin();
			try {
				Reminder r = obj.query(Reminder.class).filter("id",Long.parseLong(reminderId)).filter("emailAddress",emailId).get();
				if (r != null) {
					obj.delete(r);
					return "success";
				}
				else {
					return "fail";
				}
				
			}
			catch (Exception ex) {
				throw new Exception("Could not remove the Reminder. Please check the format for the Reminder Id and/or try again.");
			}
	}
	
	/**
	 * This method is invoked by the CRON Job (CronJobServlet.java) that is executed every 1 minute to send out Reminders that are triggered. 
	 * 
	 * The logic is straightforward:
	 * 
	 * Step 1 is to determine which Reminders are ACTIVE. 
	 * 
	 * Step 2 is to iterate through these reminders and determine if the trigger has passed or it is within the threshold of 1 minute to go. 
	 * 
	 * Step 3 If Yes, then the sendIM() method is used to send out the Reminder message via XMPP protocol.
	 * 
	 * On successful dispatch of the message, the status of the Reminder Object in the datastore is set to SENT
	 */
	public void sendNotifications() {
		//For every Request in ACTIVE
		try {
			long tCurrent = System.currentTimeMillis();
			List<Reminder> _notifications = getAllReminders("ACTIVE");
			Iterator<Reminder> it = _notifications.iterator();
			while (it.hasNext()) {
				Reminder R = (Reminder)it.next();
				//Determine if its time is up. Also continue the loop if one fails
				boolean bNotify = false;
				long tReminder = R.getReminderDate().getTime();
				//Let us send the notification if time passed or is nearing it
				//threshold taken is 60 seconds (1 minute)
				long tDiff = tReminder - tCurrent;
				if (tDiff < 0) {
					bNotify = true;
				}
				else if ((tDiff > 0) && (tDiff <=60)) {
				    bNotify = true;	
				}
				
				if (bNotify) {
					String msg = R.getMsg();
					//Send Message
					_logger.info("Sending IM Notification : " + R.getId() + " to " + R.getUserid() + " Message : " + msg);
					sendIM(R.getUserid(), msg);
					_logger.info("Message Sent");
					//Update Status to "SENT"
					Objectify obj = ObjectifyService.begin();
					R.setStatus("SENT");
					obj.put(R);
				}
			}
		}
		catch (Exception ex) {
			_logger.info("Error in sending IM To Recipient : " + ex.getMessage());
		}
	}
	
	/**
	 * 
	 * @param JabberId The JabberId of the user to send out the XMPP message to
	 * @param msg The message i.e. the text that we need to send out in the XMPP message. Usually this message is the Reminder text that the user has 
	 * specified at the time of adding a Reminder.
	 * @throws Exception
	 */
	private void sendIM(String JabberId, String msg) throws Exception {
		XMPPService xmpp = null;
		JID fromJid = new JID(JabberId);
		xmpp = XMPPServiceFactory.getXMPPService();
		Message replyMessage = new MessageBuilder()
        .withRecipientJids(fromJid)
        .withBody(msg)
        .build();
        boolean messageSent = false;
        //The condition is commented out so that it can work over non Google Talk XMPP providers also.
        //if (xmpp.getPresence(fromJid).isAvailable()) {  
        SendResponse status = xmpp.sendMessage(replyMessage);
        messageSent = (status.getStatusMap().get(fromJid) == SendResponse.Status.SUCCESS);
        //}
        if (messageSent) {
        	_logger.info("Message has been sent successfully");
        }
        else {
        	_logger.info("Message could not be sent");
        }
	}
}
