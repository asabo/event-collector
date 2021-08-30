package com.db.logcollector.app.extractor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.db.logcollector.app.dto.Activity;
import com.db.logcollector.app.dto.Event;

class JsonExtractorTest {

	private static final String WEBSITE_NAME = "WebsiteName";
	private static final String USER_NAME = "UserName";
	private static final Date LOGGED_IN_TIME = new Date();
	private static final String ACT_TYPE_DESC = "ActTypeDesc";
	static Activity activity;
	static JsonExtractor jsonExtractor;
	
	@BeforeAll
	static void setup() {
		activity = new Activity();
		activity.setActivityTypeDescription(ACT_TYPE_DESC);
		activity.setSignedInTime(LOGGED_IN_TIME);
		activity.setUserName(USER_NAME);
		activity.setWebsiteName(WEBSITE_NAME);
		jsonExtractor = new JsonExtractor();
	}
	
	@Test
	void testValidExtraction() {
		Event res = jsonExtractor.extract(activity);
		
		assertEquals (res.getActivityTypeDescription(), ACT_TYPE_DESC);
		assertEquals (res.getSignedInTime(), LOGGED_IN_TIME);
		assertEquals(res.getUser(), USER_NAME);
		assertEquals(res.getWebsite(), WEBSITE_NAME);
	}

}
