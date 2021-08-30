package com.db.logcollector.app.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public final class Activity extends InputEvent {
	private String activityTypeDescription;
	private Date signedInTime;

	public Date getSignedInTime() {
		return signedInTime;
	}

	@JsonFormat
	  (shape = JsonFormat.Shape.STRING, pattern = "MM/DD/YYYY")
	public void setSignedInTime(Date loggedInTime) {
		this.signedInTime = loggedInTime;
	}

	public String getActivityTypeDescription() {
		return activityTypeDescription;
	}

	public void setActivityTypeDescription(String activityTypeDescription) {
		this.activityTypeDescription = activityTypeDescription;
	}

}
