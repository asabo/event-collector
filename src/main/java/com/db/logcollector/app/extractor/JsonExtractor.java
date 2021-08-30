package com.db.logcollector.app.extractor;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.db.logcollector.app.dto.Event;
import com.db.logcollector.app.dto.Activity;

public class JsonExtractor implements Extractor<Activity> {
	private static final Logger LOG = Log.getLogger(JsonExtractor.class);
	
	@Override
	public Event extract(Activity input) {
		Event e = new Event();
		e.setActivityTypeDescription(input.getActivityTypeDescription());
		e.setSignedInTime(input.getSignedInTime());
		e.setUser(input.getUserName());
		e.setWebsite(input.getWebsiteName());
		
		return e;
	}

}
