/**
 * My Reminder Bot
 * Copyright (c) 2011 by Romin Irani. All Rights Reserved.
 */
package com.thirdchimpanzee.myreminderbot.entity;

import java.util.Date;

import javax.persistence.Id;

/**
 * Entity class for Reminder object
 * 
 * @author irani_r
 * @version 1.0
 * 
 * id           : Internal ID used for storage
 * userid       : Jabber/XMPP ID of the user making the request
 * emailAddress : Email address of the user making the request 
 * msg          : The Reminder text
 * requestDate  : The date/time of making the request
 * reminderDate : The date/time of when the Reminder needs to be triggered
 * status       : The current status of the Reminder object. When it is created, the status is ACTIVE,
 *                when the reminder is triggered and sent successfully, it is set to DONE.
 */
public class Reminder {
	@Id private Long id;
	private String userid;
	private String emailAddress;
	private String msg;
	private Date requestDate;
	private Date reminderDate;
	private String status;
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the userid
	 */
	public String getUserid() {
		return userid;
	}
	/**
	 * @param userid the userid to set
	 */
	public void setUserid(String userid) {
		this.userid = userid;
	}
	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	/**
	 * @param emailAddress the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
	/**
	 * @return the requestDate
	 */
	public Date getRequestDate() {
		return requestDate;
	}
	/**
	 * @param requestDate the requestDate to set
	 */
	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}
	/**
	 * @return the reminderDate
	 */
	public Date getReminderDate() {
		return reminderDate;
	}
	/**
	 * @param reminderDate the reminderDate to set
	 */
	public void setReminderDate(Date reminderDate) {
		this.reminderDate = reminderDate;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	

}
