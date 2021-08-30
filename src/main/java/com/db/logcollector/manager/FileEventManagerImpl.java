package com.db.logcollector.manager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.db.logcollector.app.dto.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileEventManagerImpl implements EventManager {
	private static final Logger LOG = Log.getLogger(FileEventManagerImpl.class);

	private ConcurrentLinkedQueue<Event> eventQueue = new ConcurrentLinkedQueue<>();
	private ScheduledExecutorService scheduledExecutorService;
	private Properties properties;
	private ObjectMapper mapper;

	private String storeFileName;

	FileWriter fw;
	BufferedWriter bw;
	PrintWriter out;
	
	ScheduledFuture<?> fileWriteTask;

	@Inject
	public FileEventManagerImpl(ScheduledExecutorService scheduledExecutorService, Properties properties, ObjectMapper mapper) {
		this.scheduledExecutorService = scheduledExecutorService;
		this.properties = properties;
		this.mapper = mapper;

		this.storeFileName = this.properties.getProperty("result-file.name");

		this.fileWriteTask = this.scheduledExecutorService
				.scheduleAtFixedRate(() -> storeDataToFileSystem(), 10, 10, TimeUnit.SECONDS);
		LOG.info("FileEventManager instantiated, logs to be stored to:" + this.storeFileName);
		
		openFile();
	}

	private void openFile() {
		try {
			fw = new FileWriter(this.storeFileName, true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);
		} catch (IOException ioe) {
			LOG.warn("Opening logs file did not get initialized properly", ioe);
		}
	}
	
	private void closeFile() {
		try {
			out.close();
			bw.close();
			fw.close();
		} catch (IOException ioe) {
			LOG.warn("Closing of logs file did not get finish properly", ioe);
		}
	}

	private void storeDataToFileSystem() {
		int stores = 10_000;
		 
		 while (!eventQueue.isEmpty() && stores-- >0 ) {
			 Event event = eventQueue.poll();
			 try {
				out.println(mapper.writeValueAsString(event));
			} catch (JsonProcessingException e) {
				LOG.warn("Exception converting event object into a string:" + event.toString(), e);
			}
		 }
		 out.flush();
	}

	@Override
	public void consume(Event event) {
		LOG.info("Event came in: " + event + " null: " + ( event == null ));
		eventQueue.add(event);
	}
	
	public void finalize() {
		this.fileWriteTask.cancel(false);
		this.closeFile();
	}

}
