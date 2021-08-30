package com.test.eventcollector.app.dto;

public final class ServerLogEvent extends InputEvent {
	private String type;
	private String host;


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

}
