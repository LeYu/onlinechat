package com.online;

import java.util.Date;

public class MessageEvent {
	private String from;
	private String to;
	private String content;
	private String type = "USER";
	private Date sendDate;

	public MessageEvent() {
	}

	public MessageEvent(String from, String to, String type) {
		this.from = from;
		this.to = to;
		this.type = type;
		this.sendDate = new Date();
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

}
