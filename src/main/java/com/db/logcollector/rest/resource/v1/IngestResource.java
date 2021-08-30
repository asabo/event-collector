package com.db.logcollector.rest.resource.v1;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXB;
import javax.xml.validation.Schema;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.db.logcollector.app.dto.Event;
import com.db.logcollector.app.dto.JsonInputEvent;
import com.db.logcollector.app.dto.XMLInputEvent;
import com.db.logcollector.app.extractor.JsonExtractor;
import com.db.logcollector.app.extractor.JsonValidator;
import com.db.logcollector.app.extractor.XMLExtractor;
import com.db.logcollector.app.extractor.XMLValidator;
import com.db.logcollector.manager.EventManager;
import com.db.logcollector.rest.exception.IllegalRequestException;
import com.db.logcollector.util.FileUtil;
import com.db.logcollector.util.XmlUtil;

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

	private static XMLExtractor XML_EXTRACTOR = new XMLExtractor();
	private static JsonExtractor JSON_EXTRACTOR = new JsonExtractor();

	private static XMLValidator XML_VALIDATOR = new XMLValidator();
	private static JsonValidator JSON_VALIDATOR = new JsonValidator();

	
	EventManager eventManager;
	ExecutorService executorService;
	
	static Schema XML_INPUT_EVENT_SCHEMA = null; 

	@Inject
	public IngestResource(EventManager eventManager, ExecutorService executorService) {
		this.eventManager = eventManager;
		this.executorService = executorService;
        
		if (XML_INPUT_EVENT_SCHEMA == null ) {
			try {
			InputStream xsdFile = FileUtil.findConfFileInCommonSpace("XmlInputEventSchema.xml");
			LOG.info("xml schema file contains bytes: "+ xsdFile.available());
			XML_INPUT_EVENT_SCHEMA = XmlUtil.getSchema(xsdFile);
			} catch (Exception e) {
				LOG.info("Exception while instantianting XML input schema", e);
			}
		}
	}

	@POST
	@Path("/event")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String jsonEvent(JsonInputEvent jsonInputEvent) {
		LOG.info("ingest JSON event came in");
		
		//1. validate input event
	    boolean validationResult = JSON_VALIDATOR.validate(jsonInputEvent.getActivity());
	    if (!validationResult) {
			throw new IllegalRequestException("JSON event not valid!", null, 400);
		}
	    
		//2. normalize it
		Event event = JSON_EXTRACTOR.extract(jsonInputEvent.getActivity());
		
		//3. send it out for storage
		executorService.submit( () -> {
			eventManager.consume(event);
		});

		return OK;
	}

	@POST
	@Path("/event")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON)
	public String xmlEvent(final byte[] xmlInputEvent) {

		LOG.info("ingest XML event came in");
		
		ByteArrayInputStream xml = new ByteArrayInputStream(xmlInputEvent);	   
		// 0.0 validate xml doc against xslt 
		boolean validationResult = XmlUtil.validateAgainstSchema(xml, XML_INPUT_EVENT_SCHEMA);
		
		if (!validationResult) {
			throw new IllegalRequestException("XML event not valid!", null, 400);
		}
		
		LOG.info("About to unmarshal xml data:"+ xml.available()+ " data: " + new String ( xmlInputEvent) );
		// 0.1 convert xml doc into pojo
		XMLInputEvent inputEvent = JAXB.unmarshal(new ByteArrayInputStream(xmlInputEvent), XMLInputEvent.class);
		
		//1. validate input event data
		validationResult = XML_VALIDATOR.validate(inputEvent);
		if (!validationResult) {
			throw new IllegalRequestException("XML event data not valid!", null, 400);
		}
		
		//2. normalize it 
		Event event =XML_EXTRACTOR.extract(inputEvent);
		
		//3. send it out for storage
		executorService.submit( () -> {
			eventManager.consume(event);
		});

		return OK;
	}

}
