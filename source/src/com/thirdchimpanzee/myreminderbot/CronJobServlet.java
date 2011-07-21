/**
 * My Reminder Bot
 * Copyright (c) 2011 by Romin Irani. All Rights Reserved.
 */

package com.thirdchimpanzee.myreminderbot;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.thirdchimpanzee.myreminderbot.service.ReminderBotService;

/**
 * Google App Engine Cron Job Class
 * 
 * This is the only CRON Job currently in the system that is fired at an interval of 1 minute.
 * The cron job configuration is present in WEB-INF\cron.xml file. In the initial release we are making
 * this single CRON Job do all the work but moving forward, we will need to have several jobs that
 * can process reminders by groups of users and even do other backend tasks like cleaning up 
 * processed reminders from the datastore.
 * 
 * The Cron Job currently invokes the sendNotifications method in the ReminderBotService that 
 * takes care of the sending out the notifications.
 * 
 * @author irani_r
 * @version 1.0
 * 
 */
@SuppressWarnings("serial")
public class CronJobServlet extends HttpServlet {
 private static final Logger _logger = Logger.getLogger(CronJobServlet.class.getName());
 public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	 try {
		 _logger.info("Cron Job has been executed");
		 ReminderBotService.getInstance().sendNotifications();
	 }
	 catch (Exception ex) {
		 //Log any exceptions in your Cron Job
		 _logger.info("Error in executing Cron Job : " + ex.getMessage());
	 }
 }

 @Override
 public void doPost(HttpServletRequest req, HttpServletResponse resp)
 throws ServletException, IOException {
 doGet(req, resp);
 }
}
