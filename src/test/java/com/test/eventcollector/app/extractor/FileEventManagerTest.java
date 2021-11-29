package com.test.eventcollector.app.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.eventcollector.app.dto.EventStats;
import com.test.eventcollector.app.dto.InputEvent;
import com.test.eventcollector.app.dto.ServerLogEvent;
import com.test.eventcollector.app.dto.State;
import com.test.eventcollector.manager.EventManager;
import com.test.eventcollector.manager.FileEventManagerImpl;
import com.test.eventcollector.manager.HsqlDbManager;
import com.test.eventcollector.manager.HsqlDbManagerImpl;
import com.test.eventcollector.rest.resource.EventCollectorRestResourceConfig;
import com.test.eventcollector.util.FileUtil;

class FileEventManagerTest {

	private static final String TEST_LOGFILE = "test-logfile.txt";
	private static final long NOW = System.currentTimeMillis();
	private static final String SERVERLOGEVENTID = "ServerLogEventId";
	private static final State STATE_STARTED = State.valueOf("STARTED");
	private static final State STATE_FINISHED = State.valueOf("FINISHED");

	private static final String HOST = "someHost";
	private static final String TYPE = "TypeOfLog";
	static ServerLogEvent serverLogEvent;
	static InputEvent inputEvent;

	static EventManager fileEventManager;
	static HsqlDbManager hsqlDbManager;
	static ObjectMapper mapper;

	@BeforeAll
	static void setup() {
		serverLogEvent = new ServerLogEvent();
		serverLogEvent.setId(SERVERLOGEVENTID);
		serverLogEvent.setState(STATE_STARTED);
		serverLogEvent.setTimestamp(NOW);
		serverLogEvent.setType(TYPE);
		serverLogEvent.setHost(HOST);

		inputEvent = new InputEvent();
		inputEvent.setId(SERVERLOGEVENTID);
		inputEvent.setTimestamp(NOW+3);
		inputEvent.setState(STATE_FINISHED);
		
		ScheduledExecutorService scheduler = EventCollectorRestResourceConfig.schedulerService();
		Properties properties = new Properties();
		properties.put("result-file.name", TEST_LOGFILE);
		mapper = EventCollectorRestResourceConfig.jacksonObjectMapper();
		
		//should mock this class, but it's easier  to query data directly and verify them stored, TODO mock this class
		hsqlDbManager =  new HsqlDbManagerImpl();

		fileEventManager = new FileEventManagerImpl(scheduler, hsqlDbManager, properties, mapper);
	}

	@AfterAll
	static void tearDown() {		
		fileEventManager = null;
		File f = new File(TEST_LOGFILE);
		f.delete();
	}

	@Test
	void testConsumptionOfLogEvent1ServerLogEvent() {
		
		fileEventManager.consume(serverLogEvent);

		List<String> lines = null;
		int tries = 30;

		do {
			lines = FileUtil.readTextFile(TEST_LOGFILE);
			if (lines == null || lines.size() == 0) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException inte) {
				}
			}
		} while (tries-- > 0 && (lines == null || lines.size() < 1));

		assert (lines != null);
		assert (lines.size() > 0);

		ServerLogEvent foundEvent = toServerLogEvent(lines.get(0));

		assertEquals(foundEvent.getHost(), serverLogEvent.getHost());
		assertEquals(foundEvent.getId(), SERVERLOGEVENTID);
		assertEquals(foundEvent.getTimestamp(), NOW);
		assertEquals(foundEvent.getState(), STATE_STARTED);
		assertEquals(foundEvent.getType(), TYPE);
	}
	
	
	@Test
	void testValidConsumptionOfLogEvent2InputEvent() {
		fileEventManager.consume(inputEvent);

		List<String> lines = null;
		int tries = 50;

		do {
			lines = FileUtil.readTextFile(TEST_LOGFILE);
			if (lines == null || lines.size() < 2) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException inte) {
				}
			}
		} while (tries-- > 0 && (lines == null || lines.size() < 2));

		assertNotNull(lines);
		assertEquals(lines.size(), 2);

		InputEvent foundEvent = toInputEvent(lines.get(1));
	 
		assertEquals(foundEvent.getId(), SERVERLOGEVENTID);
		assertEquals(foundEvent.getTimestamp(), NOW+3);
		assertEquals(foundEvent.getState(), STATE_FINISHED);
		
		//since less than 4ms, should not leave any trace in database
		EventStats eventStats = hsqlDbManager.getEventStats(SERVERLOGEVENTID);
		assertNull(eventStats);
	}


	@Test
	void testValidLongRunningEventWillGetStoredToDb() {
		String id = UUID.randomUUID().toString();
		
		serverLogEvent.setId(id);
		fileEventManager.consume(serverLogEvent);

		inputEvent.setId(id);
		inputEvent.setTimestamp(inputEvent.getTimestamp()+2); //on +5ms
		
		fileEventManager.consume(inputEvent);

		 
		EventStats eventStats = hsqlDbManager.getEventStats(id);
		assertNotNull(eventStats);
		assertEquals(eventStats.getId(), id);
		assertEquals(eventStats.getDuration(), 5);
		assertNull(eventStats.getHost());
		assertNull(eventStats.getType());
	}
	
	private ServerLogEvent toServerLogEvent(String json) {
		ServerLogEvent event = null;
		try {
			event = mapper.readValue(json, ServerLogEvent.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return event;
	}
	
	private InputEvent toInputEvent(String json) {
		InputEvent event = null;
		try {
			event = mapper.readValue(json, InputEvent.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return event;
	}

}
