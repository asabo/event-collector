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

	private ConcurrentLinkedQueue<InputEvent> eventQueue = new ConcurrentLinkedQueue<>();
	private ScheduledExecutorService scheduledExecutorService;
	private Properties properties;
	private ObjectMapper mapper;
	private HsqlDbManager dbManager;

	private String storeFileName;

	private boolean operational = false;

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

		this.storeFileName = this.properties.getProperty("result-file.name");

		this.fileWriteTask = this.scheduledExecutorService.scheduleAtFixedRate(() -> storeDataToFileSystem(), 1, 10,
				TimeUnit.SECONDS);
		LOG.info("FileEventManager instantiated, logs to be stored to:" + this.storeFileName);

		openFile();
	}

	@Override
	public void consume(ServerLogEvent event) {
		LOG.info("ServerLogEvent came in: " + event);
		eventQueue.add(event);

		checkEventRunningTimes(event);
	}

	@Override
	public void consume(InputEvent event) {
		LOG.info("InputEvent came in: " + event);
		eventQueue.add(event);
		checkEventRunningTimes(event);
	}

	public void finalize() {
		this.fileWriteTask.cancel(false);
		this.closeFile();
	}

	private void openFile() {
		try {
			fw = new FileWriter(this.storeFileName, true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
			operational = true;
		} catch (IOException ioe) {
			LOG.warn("Opening of logs file did not get initialized properly", ioe);
		}
	}

	private void closeFile() {
		try {
			out.close();
			bw.close();
			fw.close();
			operational = false;
		} catch (IOException ioe) {
			LOG.warn("Closing of logs file did not get finished properly", ioe);
		}
	}

	private void storeDataToFileSystem() {
		int stores = 10_000;

		while (operational && !eventQueue.isEmpty() && stores-- > 0) {
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
	 * checks if event state is 'finished' and calculates duration of event. If event is longer 
	 * than proposed threshold, fact would get stored to db
	 */
	private void checkEventRunningTimes(InputEvent event) {
		if (event.getState().equals(State.STARTED)) {
			eventStarts.put(event.getId(), event.getTimestamp());
		} else {
			long started = eventStarts.get(event.getId());
			long finished = event.getTimestamp();

			int duration = (int) (finished - started);
			if (duration > 4) {
				// log to DB
				EventStats es = new EventStats();
				es.setId(event.getId());
				es.setAlert(true);
				es.setDuration(duration);

				if (event instanceof ServerLogEvent) {
					ServerLogEvent sl = (ServerLogEvent) event;
					es.setHost(sl.getHost());
					es.setType(sl.getType());
				}  

				dbManager.storeEventStats(es);
				eventStarts.remove(event.getId());
			}
		}
	}
}
