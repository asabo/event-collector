package com.test.eventcollector.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.eventcollector.app.dto.InputEvent;
import com.test.eventcollector.app.dto.ServerLogEvent;
import com.test.eventcollector.manager.EventManager;
import com.test.eventcollector.manager.FileEventManagerImpl;
import com.test.eventcollector.manager.HsqlDbManagerImpl;
import com.test.eventcollector.rest.resource.EventCollectorRestResourceConfig;

public class EventCollectorApplication {

	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("You need to specify file name to be handled as a param to application!");
			return;
		}
		
		String fileName = args[0];
		
		if (StringUtils.isBlank(fileName)) {
			System.out.println("File name not specified or empty");
			return;
		}		
		
		ObjectMapper objectMapper = EventCollectorRestResourceConfig.jacksonObjectMapper();
		 
		EventManager eventManager  = createFileEventManager();
		
		File txtFile = new File(fileName);	 
		
		try (BufferedReader br = new BufferedReader(new FileReader(txtFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				ServerLogEvent serverLogEvent = toServerLogEvent(line, objectMapper);
				if (serverLogEvent == null ) continue;
				
				//Generic log events would not have host & type set, so a quick & dirty hack to differ them, though it does not matter now, can be bug generator in future
				if (serverLogEvent.getHost() == null && serverLogEvent.getType() == null) {
					eventManager.consume((InputEvent)serverLogEvent);	
				} else {
					eventManager.consume(serverLogEvent);
				}
			}
		} catch (IOException e) {
			System.err.println("Exception in reading file: " + e);
			return;
		}
   }

	static ServerLogEvent toServerLogEvent(String json, ObjectMapper mapper) {
		ServerLogEvent event = null;
		try {
			event = mapper.readValue(json, ServerLogEvent.class);
		} catch (JsonProcessingException e) {
			System.err.println("Exception converting json string into event, json: " + json + " Exception: " + e);
		}

		return event;
	}
	
	private static EventManager createFileEventManager() {
		ScheduledExecutorService scheduler = EventCollectorRestResourceConfig.schedulerService();
		 ObjectMapper objectMapper = EventCollectorRestResourceConfig.jacksonObjectMapper();
		 Properties properties = new Properties(); // no file specified - it won't store events to a file, only db records
		 HsqlDbManagerImpl dbManager = new HsqlDbManagerImpl();
		 
		return  new FileEventManagerImpl( scheduler, dbManager, properties, objectMapper );
	}
	
	
}
