package com.db.logcollector.app.extractor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.db.logcollector.app.dto.Activity;
import com.db.logcollector.app.dto.Event;
import com.db.logcollector.app.dto.XMLInputEvent;

class XMLExtractorTest {

	private static final String WEBSITE_NAME = "WebsiteName";
	private static final String USER_NAME = "UserName";
	private static final Date LOGGED_IN_TIME = new Date();
	private static final int ACT_TYPE_CODE = 1;
	private static final String ACT_TYPE_DESC = "Viewed";
	static XMLInputEvent xmlEvent;
	static XMLExtractor xmlExtractor;
	
	@BeforeAll
	static void setup() {
		xmlEvent = new XMLInputEvent();
		xmlEvent.setActivityTypeCode(ACT_TYPE_CODE);
		xmlEvent.setLoggedInTime(LOGGED_IN_TIME);
		xmlEvent.setUserName(USER_NAME);
		xmlEvent.setWebsiteName(WEBSITE_NAME);
		xmlExtractor = new XMLExtractor();
	}
	
	@Test
	void testValidExtraction() {
		Event res = xmlExtractor.extract(xmlEvent);
		
		assertEquals (res.getActivityTypeDescription(), ACT_TYPE_DESC);
		assertEquals (res.getSignedInTime(), LOGGED_IN_TIME);
		assertEquals(res.getUser(), USER_NAME);
		assertEquals(res.getWebsite(), WEBSITE_NAME);
	}

}
