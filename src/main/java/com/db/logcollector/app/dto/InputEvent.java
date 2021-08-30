package com.db.logcollector.app.dto;

public abstract class InputEvent {
private String userName;
private String websiteName;

public String getUserName() {
	return userName;
}
public void setUserName(String userName) {
	this.userName = userName;
}
public String getWebsiteName() {
	return websiteName;
}
public void setWebsiteName(String websiteName) {
	this.websiteName = websiteName;
}

}
