package com.online;

import java.util.Date;

public class EventBean {
	private String from;
	private String message;
	private Date receiveTime;
	private String messageType = "USER";

	public EventBean(String from, String messageType) {
		this.from = from;
		this.messageType = messageType;
		this.receiveTime = new Date();
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

}
