package com.db.logcollector.app.extractor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.db.logcollector.app.dto.XMLInputEvent;

class XMLValidatorTest {

	private static final String WEBSITE_NAME = "WebsiteName";
	private static final String USER_NAME = "UserName";
	private static final Date LOGGED_IN_TIME = new Date();
	private static final int ACT_TYPE_CODE = 1;
 
	static XMLInputEvent xmlEvent;
	static XMLValidator xmlValidator;
	
	@BeforeAll
	static void setup() {
		generateNewXmlEvent();
		xmlValidator = new XMLValidator();
	}


	private static void generateNewXmlEvent() {
		xmlEvent = new XMLInputEvent();
		xmlEvent.setActivityTypeCode(ACT_TYPE_CODE);
		xmlEvent.setLoggedInTime(LOGGED_IN_TIME);
		xmlEvent.setUserName(USER_NAME);
		xmlEvent.setWebsiteName(WEBSITE_NAME);
	}
	
	
	@Test
	void testValidElement() {
		 assertTrue(xmlValidator.validate(xmlEvent));
	}
	
	@Test
	void testInvalidElementNoWebsite() {
		generateNewXmlEvent();
		xmlEvent.setWebsiteName(null);
		 assertFalse(xmlValidator.validate(xmlEvent));
	}
	
	@Test
	void testInvalidElementNoUsername() {
		generateNewXmlEvent();
		xmlEvent.setUserName("   ");
		assertFalse(xmlValidator.validate(xmlEvent));
	}

	@Test
	void testInvalidElementNoDate() {
		generateNewXmlEvent();
		xmlEvent.setLoggedInTime(null);
		assertFalse(xmlValidator.validate(xmlEvent));
	}
}
