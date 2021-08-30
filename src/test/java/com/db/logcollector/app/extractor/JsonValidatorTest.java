package com.db.logcollector.app.extractor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.db.logcollector.app.dto.Activity;
import com.db.logcollector.app.dto.JsonInputEvent;
import com.db.logcollector.app.dto.XMLInputEvent;

class JsonValidatorTest {

	private static final String WEBSITE_NAME = "WebsiteName";
	private static final String USER_NAME = "UserName";
	private static final Date LOGGED_IN_TIME = new Date();
	private static final String ACT_TYPE_DESC = "ActTypeDesc";
 
	static JsonInputEvent jsonEvent;
	static JsonValidator jsonValidator;
	
	@BeforeAll
	static void setup() {
		generateNewJsonEvent();
		jsonValidator = new JsonValidator();
	}


	private static void generateNewJsonEvent() {
		jsonEvent = new JsonInputEvent();
		Activity act = new Activity();
		act.setActivityTypeDescription(ACT_TYPE_DESC);
		act.setSignedInTime(LOGGED_IN_TIME);
		act.setUserName(USER_NAME);
		act.setWebsiteName(WEBSITE_NAME);
		jsonEvent.setActivity(act);
	}
	
	
	@Test
	void testValidElement() {
		 assertTrue(jsonValidator.validate(jsonEvent.getActivity()));
	}
	
	@Test
	void testInvalidElementNoWebsite() {
		generateNewJsonEvent();
		jsonEvent.getActivity().setWebsiteName(null);
		 assertFalse(jsonValidator.validate(jsonEvent.getActivity()));
	}
	
	@Test
	void testInvalidElementNoUsername() {
		generateNewJsonEvent();
		jsonEvent.getActivity().setUserName(null);
		 assertFalse(jsonValidator.validate(jsonEvent.getActivity()));
	}

	@Test
	void testInvalidElementNoDate() {
		generateNewJsonEvent();
		jsonEvent.getActivity().setSignedInTime(null);
		assertFalse(jsonValidator.validate(jsonEvent.getActivity()));
	}
}
