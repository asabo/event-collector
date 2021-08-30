package com.test.eventcollector.app.dto;

public class InputEvent {
	private String id;
	private State state;

	private long timestamp;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long started) {
		this.timestamp = started;
	}

}
