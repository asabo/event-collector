package com.test.eventcollector.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.eventcollector.app.dto.ServerLogEvent;
import com.test.eventcollector.app.dto.State;
import com.test.eventcollector.rest.resource.EventCollectorRestResourceConfig;

public class EventCollectorApplicationTest {

	ObjectMapper mapper = EventCollectorRestResourceConfig.jacksonObjectMapper();
	
	@Test
	public void testJsonConversionServerEvent() {
		String json =  "{\"id\":\"scsmbstgra\", \"state\":\"STARTED\", \"type\":\"APPLICATION_LOG\", \"host\":\"12345\", \"timestamp\":1491377495212}";
		ServerLogEvent serverLogEvent = EventCollectorApplication.toServerLogEvent(json, mapper);
		assertNotNull(serverLogEvent);	
		assertEquals("scsmbstgra", serverLogEvent.getId());
		assertEquals(State.STARTED, serverLogEvent.getState());
		assertEquals(1491377495212L, serverLogEvent.getTimestamp());
		assertEquals("12345", serverLogEvent.getHost());
		assertEquals("APPLICATION_LOG", serverLogEvent.getType());
	}
	
	@Test
	public void testJsonConversionEvent() {
		String json =  "{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}";
		ServerLogEvent serverLogEvent = EventCollectorApplication.toServerLogEvent(json, mapper);
		assertNotNull(serverLogEvent);	
		assertEquals("scsmbstgrb", serverLogEvent.getId());
		assertEquals(State.STARTED, serverLogEvent.getState());
		assertEquals(1491377495213L, serverLogEvent.getTimestamp());
		assertEquals(null, serverLogEvent.getType());
		assertEquals(null, serverLogEvent.getHost());
	}
}
