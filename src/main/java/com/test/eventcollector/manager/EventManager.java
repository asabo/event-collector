package com.test.eventcollector.manager;

import com.test.eventcollector.app.dto.InputEvent;
import com.test.eventcollector.app.dto.ServerLogEvent;

public interface EventManager {

	 void consume(InputEvent event);
	 void consume(ServerLogEvent event);
}
