package com.db.logcollector.app.extractor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.db.logcollector.app.dto.Event;
import com.db.logcollector.app.dto.XMLInputEvent;
import com.db.logcollector.util.FileUtil;

public class XMLExtractor implements Extractor<XMLInputEvent> {
	private static final Logger LOG = Log.getLogger(XMLExtractor.class);

	private static HashMap<Integer, String> ACTIVATIONS = null;

	public XMLExtractor() {
		if (ACTIVATIONS == null) {
			try {
				InputStream csvFile = FileUtil.findConfFileInCommonSpace("activity.csv");
				LOG.info("activity file contains bytes: " + csvFile.available());
				List<CSVRecord> data = FileUtil.readCSVFile(csvFile);
				ACTIVATIONS = new HashMap<>();
				for (CSVRecord record : data) {
					ACTIVATIONS.put(Integer.parseInt(record.get(0)), record.get(1));
				}
			} catch (Exception e) {
				LOG.info("Exception while instantianting XML input schema", e);
			}
		}
	}

	@Override
	public Event extract(XMLInputEvent input) {
		Event e = new Event();
		e.setActivityTypeDescription(ACTIVATIONS.get(input.getActivityTypeCode()));
		e.setSignedInTime(input.getLoggedInTime());
		e.setUser(input.getUserName());
		e.setWebsite(input.getWebsiteName());
		
		return e;
	}

}
