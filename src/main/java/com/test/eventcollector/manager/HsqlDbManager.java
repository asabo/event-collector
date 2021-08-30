package com.test.eventcollector.manager;

import com.test.eventcollector.app.dto.EventStats;

public interface HsqlDbManager {

	public boolean storeEventStats(EventStats eventStats);

	EventStats getEventStats(String statsId);
}
