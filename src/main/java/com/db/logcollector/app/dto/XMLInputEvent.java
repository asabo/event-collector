package com.db.logcollector.app.dto;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "XMLInputEvent")
public final class XMLInputEvent extends InputEvent {
	private int activityTypeCode;
	private Date loggedInTime;

	@XmlElement(name = "loggedInTime")
	public Date getLoggedInTime() {
		return loggedInTime;
	}

	public void setLoggedInTime(Date loggedInTime) {
		this.loggedInTime = loggedInTime;
	}

	@XmlElement(name = "activityTypeCode")
	public int getActivityTypeCode() {
		return activityTypeCode;
	}

	public void setActivityTypeCode(int activityTypeCode) {
		this.activityTypeCode = activityTypeCode;
	}
}
