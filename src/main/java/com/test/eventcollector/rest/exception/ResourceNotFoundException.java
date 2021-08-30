package com.test.eventcollector.rest.exception;

import javax.xml.ws.WebServiceException;

public final class ResourceNotFoundException extends WebServiceException {
	private static final long serialVersionUID = -1884891730407957304L;
	private int statusCode;

	public ResourceNotFoundException(String message, Exception cause) {
		super(message, cause);
		this.statusCode = 404;
	}
	
	public int getStatusCode() { 
		return this.statusCode;
	}
}