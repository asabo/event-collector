package com.test.eventcollector.rest.exception;

import javax.xml.ws.WebServiceException;

public final class InternalServerErrorException extends WebServiceException {
	private static final long serialVersionUID = -1884891730407957304L;
	private int statusCode;

	public InternalServerErrorException(String message, Exception cause, int statusCode) {
		super(message, cause);
		this.statusCode = statusCode;
	}
	
	public int getStatusCode() { 
		return this.statusCode;
	}
};