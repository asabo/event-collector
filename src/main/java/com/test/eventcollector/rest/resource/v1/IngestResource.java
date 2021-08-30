package com.test.eventcollector.rest.resource.v1;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.test.eventcollector.app.dto.InputEvent;
import com.test.eventcollector.app.dto.ServerLogEvent;
import com.test.eventcollector.manager.EventManager;

/**
 * ingestion endpoint, all sort of data entry points should reside here
 * 
 * @author ante
 *
 */
@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class IngestResource {
	private static final String OK = "OK";

	private static final Logger LOG = Log.getLogger(IngestResource.class);
 
	EventManager eventManager;
	ExecutorService executorService;
	

	@Inject
	public IngestResource(EventManager eventManager, ExecutorService executorService) {
		this.eventManager = eventManager;
		this.executorService = executorService;

	}

	@POST
	@Path("/server-event")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String serverLogEvent(ServerLogEvent event) {
		LOG.info("ingest ServerLogEvent came in");
		
		//1. validate input event?
		//2. normalize it?
		 
		//3. send it out for storage
		executorService.submit( () -> {
			eventManager.consume(event);
		});

		return OK;
	}

	@POST
	@Path("/event")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String inputEvent(InputEvent event) {
		LOG.info("ingest ServerLogEvent came in");
		
		//1. validate input event?
		//2. normalize it?
		 
		//3. send it out for storage
		executorService.submit( () -> {
			eventManager.consume(event);
		});

		return OK;
	}

}
