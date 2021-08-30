package com.db.logcollector.app.dto;

import java.util.Date;

public class Event {
 String user;
 String website;
 String activityTypeDescription;
 Date signedInTime;
 
public String getUser() {
	return user;
}
public void setUser(String user) {
	this.user = user;
}
public String getWebsite() {
	return website;
}
public void setWebsite(String website) {
	this.website = website;
}
public String getActivityTypeDescription() {
	return activityTypeDescription;
}
public void setActivityTypeDescription(String activityTypeDescription) {
	this.activityTypeDescription = activityTypeDescription;
}
public Date getSignedInTime() {
	return signedInTime;
}
public void setSignedInTime(Date date) {
	this.signedInTime = date;
}
 
}
