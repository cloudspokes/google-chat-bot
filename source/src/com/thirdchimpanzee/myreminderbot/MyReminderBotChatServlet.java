/**
 * My Reminder Bot
 * Copyright (c) 2011 by Romin Irani. All Rights Reserved.
 */
package com.thirdchimpanzee.myreminderbot;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.thirdchimpanzee.myreminderbot.entity.Reminder;
import com.thirdchimpanzee.myreminderbot.entity.ReminderCommand;
import com.thirdchimpanzee.myreminderbot.service.ReminderBotService;

/**
 * This is the Chat Interface to the Application. All interactions between the Google Talk User and the application is routed through this.
 * 
 * It is easy to follow the code. 
 * 1. The main message pump is the doGet method below from which we first use some XMPP semantics to retrieve out who is sending us the message
 * and the text of the message.
 * 
 * 2. Once we have the message, we need to interpret it and compare it against the commands that we understand. If we understand the command i.e. help,
 * about, remove then we can process them otherwise we need to send back a message saying that we do not understand the command. 
 * 
 * @author irani_r
 * @version 1.0
 * 
 */


@SuppressWarnings("serial")
public class MyReminderBotChatServlet extends HttpServlet {
	public static final Logger _log = Logger.getLogger(MyReminderBotChatServlet.class.getName());
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String strCallResult="";
		String strStatus="";
		resp.setContentType("text/plain");
		XMPPService xmpp = null;
		JID fromJid = null;
		try {
	
			//STEP 1 - Extract out the message and the Jabber Id of the user sending us the message via the Google Talk client
			xmpp = XMPPServiceFactory.getXMPPService();
			Message msg = xmpp.parseMessage(req);

			fromJid = msg.getFromJid();
			String body = msg.getBody();
			
			_log.info("Received a message from " + fromJid.getId() + " and body = " + body);
			String emailId = fromJid.getId().substring(0,fromJid.getId().indexOf("/"));
			String userId = fromJid.getId();
			//String userId = fromJid.getId();
			_log.info("Email Id : " + userId);
			
			//String strWord = req.getParameter("command");
			String strCommand = body;
			
			//Do validations here. Only basic ones i.e. cannot be null/empty
			if (strCommand == null) throw new Exception("You must give a command.");
			
			//Trim the stuff
			strCommand = strCommand.trim();
			if (strCommand.length() == 0) throw new Exception("You must give a command.");
			
			/**
			 * STEP 2 : Now that we have something, compare it against the commands that understand and process them accordingly.
			 * 
			 * We currently support only 2 commands that are not single word commands. 
			 * 1. remove [ID] : This removes a particular specified ACTIVE Reminder in the system. ACTIVE Reminders are those reminders that have not yet 
			 * got triggered. The reminder is specified by an ID, which are retrieved by the list command
			 * 2. 
			 */
			
			String[] words = strCommand.split(" ");
			if (words.length >= 2) {
				try {
					boolean bRemoveCommand = false;
					if (words.length == 2) {
						String command = words[0];
						String command_data = words[1];
						//Parse the stuff over here
						if (command.equalsIgnoreCase("remove")) {
							//Get the email id
							//Get user by email id
							String txtStatus = ReminderBotService.getInstance().removeReminderByIDandEmailId(command_data,emailId);
							//if user present, sent reply about Status
							if (txtStatus.equals("success")) {
								strCallResult = "The Reminder has been deleted successfully.";
								bRemoveCommand = true;
							}
							else {
								strCallResult = "Sorry! This does not seem to be a valid Reminder record in my system or you do not own that Reminder.";
								throw new Exception(strCallResult);
							}
						}
					}
						if (!bRemoveCommand) {
							/**
							 * Call the validateAndParseMessage to determine if the format is correct. If correct, the ReminderCommand object will
							 * have the value in minutes.
							 */
							
							ReminderCommand _command = ReminderBotService.getInstance().validateAndParseMessage(strCommand);
							if (_command != null){
								
								//Create the reminder
								long requestTime = System.currentTimeMillis();
								long reminderTime = requestTime + _command.getMinutes()*60*1000;
								Date requestDate = new Date(requestTime);
								Date reminderDate = new Date(reminderTime);
								//Add the Reminder (Persist) to the Datastore
								String txtStatus = ReminderBotService.getInstance().addReminder(userId, emailId,_command.getMessage(), requestDate, reminderDate, "ACTIVE");
								if (txtStatus.equals("success")) {
									strCallResult = "Added the Reminder successfully.";
								}
								else {
									strCallResult = "There was an error in adding the Reminder. Please try again.";
								}
							}
						}
				}
				catch (Exception ex) {
					strCallResult = ex.getMessage();
				}
			}			
			/**
			 * THESE are single word commands that we understand. Currently we understands help, about and list
			 * 
			 * 1. help : This prints out a mini user manual to help the user understand what commands the Bot can request. 
			 * 2. about : A brief message indicating who wrote this Bot or maybe some more details about the Bot
			 * 3. list : This command is used to list down the current Reminders that the user owns and which are ACTIVE in the system. By ACTIVE, it means
			 * that the Reminders have not yet got triggered. A user can look at the Ids returned in this list and use those ids to remove the Reminder
			 * at any time via the : remove [ID] command
			 */
			else if (words.length == 1) {
				if (words[0].equalsIgnoreCase("help")) {
					//Print out help
					//strCallResult = "Help Text Over Here";
					StringBuffer SB = new StringBuffer();
					SB.append("***** Welcome to MyReminderBot *****");
					SB.append("\r\nI understand the following commands:");
					SB.append("\r\n1. Type help to get the list of commands.");
					SB.append("\r\n2. To create a reminder, type a text that follows this format [When] [ReminderText], where [When] is a positive number followed by either d or h or m. d = Days, h = Hours and m = Minutes. [ReminderText] is the message that you want to receive when the reminder goes off. Examples: 10m Send email on Project Status. This will set a reminder 10 minutes from now and when the reminder is triggered, you will receive a message \"Send email on Project Status\" from the bot");
					SB.append("\r\n3. Type list to get your currently active list of reminders which are set for some time in the future.");
					SB.append("\r\n4. Type remove [ID] to delete any active reminder, where [ID] is the reminder ID. You can get your current active list of reminders by typing list.");
					SB.append("\r\n5. Type about to get more information about this Agent.");
					strCallResult = SB.toString();
					
				}
				else if (words[0].equalsIgnoreCase("about")) {
					strCallResult = "Hello! I am the MyReminderBot version 1.0"+"\r\n"+"Developer: Romin Irani"+"\r\n"+"(http://myreminderbot.appspot.com)";
				}
				else if (words[0].equalsIgnoreCase("list")) {
					//Retrieve the list of the Reminders via the Service and list down with the IDs/
					List<Reminder> ReminderList = ReminderBotService.getInstance().getAllRemindersByEmailId(emailId,"ACTIVE");
					Iterator<Reminder> it = ReminderList.iterator();
					while (it.hasNext()) {
						Reminder _Reminder = it.next();
						strCallResult = strCallResult + "ID=" + _Reminder.getId() + " " + "Reminder Msg = " + _Reminder.getMsg() + " on " + _Reminder.getReminderDate().toString() + "\r\n";
					}
					if (ReminderList.size() == 0) strCallResult = "You do not have any Reminders currently in the system.";
				}
			}
			else {
				strCallResult = "Sorry! Could not understand your command.";
			}
			
			//Send out the Response message on the same XMPP channel. This will be delivered to the user via the Google Talk client.
	        Message replyMessage = new MessageBuilder().withRecipientJids(fromJid).withBody(strCallResult).build();
                
	        boolean messageSent = false;
	        //if (xmpp.getPresence(fromJid).isAvailable()) {
	        SendResponse status = xmpp.sendMessage(replyMessage);
	        messageSent = (status.getStatusMap().get(fromJid) == SendResponse.Status.SUCCESS);
	        //}
	        if (messageSent) {
	        	strStatus = "Message has been sent successfully";
	        }
	        else {
	        	strStatus = "Message could not be sent";
	        }
	        _log.info(strStatus);
		}
		catch (Exception ex) {
			
			//If there is an exception then we send back a generic message to the client i.e. MyReminderBot could not understand your command. Please
			//try again. We log the exception internally.
			_log.info("Something went wrong. Please try again!" + ex.getMessage());
	        Message replyMessage = new MessageBuilder()
            .withRecipientJids(fromJid)
            .withBody("MyReminderBot could not understand your command. Please try again.")
            .build();
                
	        boolean messageSent = false;
	        //The condition is commented out so that it can work over non Google Talk XMPP providers also.
	        //if (xmpp.getPresence(fromJid).isAvailable()) {
	        SendResponse status = xmpp.sendMessage(replyMessage);
	        messageSent = (status.getStatusMap().get(fromJid) == SendResponse.Status.SUCCESS);
	        //}
	        if (messageSent) {
	        	strStatus = "Message has been sent successfully";
	        }
	        else {
	        	strStatus = "Message could not be sent";
	        }
	        _log.info(strStatus);
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		 doGet(req, resp);
	}
}
