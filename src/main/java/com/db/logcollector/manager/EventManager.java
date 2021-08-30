package com.db.logcollector.manager;

import com.db.logcollector.app.dto.Event;

public interface EventManager {

	 void consume(Event event);
}
