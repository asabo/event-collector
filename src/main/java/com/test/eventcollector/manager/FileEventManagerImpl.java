package com.test.eventcollector.manager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.eventcollector.app.dto.EventStats;
import com.test.eventcollector.app.dto.InputEvent;
import com.test.eventcollector.app.dto.ServerLogEvent;
import com.test.eventcollector.app.dto.State;

public class FileEventManagerImpl implements EventManager {
	private static final Logger LOG = Log.getLogger(FileEventManagerImpl.class);

	private static Map<String, Long> eventStarts = new HashMap<>();
	private static Map<String, Long> eventEnds = new HashMap<>();

	private ConcurrentLinkedQueue<InputEvent> eventQueue = new ConcurrentLinkedQueue<>();
	private ScheduledExecutorService scheduledExecutorService;
	private Properties properties;
	private ObjectMapper mapper;
	private HsqlDbManager dbManager;

	private String storeFileName;

	private boolean fileOpened = false;
	private boolean storeLogsToFile = false;

	private FileWriter fw;
	private BufferedWriter bw;
	private PrintWriter out;

	private ScheduledFuture<?> fileWriteTask;

	@Inject
	public FileEventManagerImpl(ScheduledExecutorService scheduledExecutorService, HsqlDbManager dbManager,
			Properties properties, ObjectMapper mapper) {
		this.scheduledExecutorService = scheduledExecutorService;
		this.properties = properties;
		this.mapper = mapper;
		this.dbManager = dbManager;

		String storeFile = this.properties.getProperty("result-file.name");

		if (storeFile != null) {
			this.storeFileName = storeFile;

			this.fileWriteTask = this.scheduledExecutorService.scheduleAtFixedRate(() -> storeDataToFileSystem(), 1, 10,
					TimeUnit.SECONDS);
			LOG.info("FileEventManager instantiated, logs to be stored to: " + this.storeFileName);
			openFile();
			storeLogsToFile = true;
		} else {
			storeLogsToFile = false;
		}

	}

	@Override
	public void consume(ServerLogEvent event) {
		LOG.info("ServerLogEvent came in: " + event);
		if (event != null) {
			if (storeLogsToFile) {
				eventQueue.add(event);
			}
			checkEventRunningTimes(event);
		}
	}

	@Override
	public void consume(InputEvent event) {
		LOG.info("InputEvent came in: " + event);
		if (event != null) {
			if (storeLogsToFile) {
				eventQueue.add(event);
			}
			checkEventRunningTimes(event);
		}
	}

	public void finalize() {
		if (this.storeLogsToFile) {
			this.fileWriteTask.cancel(false);
			this.closeFile();
		}
	}

	private void openFile() {
		try {
			fw = new FileWriter(this.storeFileName, true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			fileOpened = true;
		} catch (IOException ioe) {
			LOG.warn("Opening of logs file did not get initialized properly", ioe);
		}
	}

	private void closeFile() {
		try {
			out.close();
			bw.close();
			fw.close();
			fileOpened = false;
		} catch (IOException ioe) {
			LOG.warn("Closing of logs file did not get finished properly", ioe);
		}
	}

	private void storeDataToFileSystem() {
		int stores = 10_000;

		while (fileOpened && !eventQueue.isEmpty() && stores-- > 0) {
			InputEvent event = eventQueue.poll();
			try {
				out.println(mapper.writeValueAsString(event));
			} catch (JsonProcessingException e) {
				LOG.warn("Exception converting event object into a string:" + event.toString(), e);
			}
		}
		out.flush();
	}

	/*
	 * checks if event state is 'started' or 'finished' and calculates duration of
	 * event. If event is longer than proposed threshold, fact will get stored to db
	 */
	private void checkEventRunningTimes(InputEvent event) {
		State eventState = event.getState();
		if (State.STARTED.equals(eventState)) {
			eventStarts.put(event.getId(), event.getTimestamp());
		} else if (State.FINISHED.equals(eventState)) {
			eventEnds.put(event.getId(), event.getTimestamp());
		} else {
			LOG.warn("Unrecognized state came in for InputEvent " + event.getId() + " state: " + eventState);
		}
		consumeEventIfComplete(event);
	}

	private void consumeEventIfComplete(InputEvent event) {
		String eventId = event.getId();
		if (eventStarts.containsKey(eventId) && eventEnds.containsKey(eventId)) {
			long started = eventStarts.get(eventId);
			long finished = eventEnds.get(eventId);

			int duration = (int) (finished - started);
			if (duration > 4) {
				// log to DB
				EventStats es = new EventStats();
				es.setId(eventId);
				es.setAlert(true);
				es.setDuration(duration);

				if (event instanceof ServerLogEvent) {
					ServerLogEvent sl = (ServerLogEvent) event;
					es.setHost(sl.getHost());
					es.setType(sl.getType());
				}

				dbManager.storeEventStats(es);
				eventStarts.remove(eventId);
				eventEnds.remove(eventId);
			}
		}
	}
}
