package com.test.eventcollector.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public final class FileUtil {
	private final static Logger LOG = Log.getLogger(FileUtil.class);

	private FileUtil() {
	}

	public static File findConfFileInClassSpace(String fileName) {
		URL url = FileUtil.class.getResource(fileName);
		File propFile = null;

		try {
			propFile = url == null ? null : new File(url.toString());
		} catch (Exception e) {
			LOG.info("Exception while reading " + fileName + " message: " + e.getMessage(), e);

			throw new RuntimeException(e);
		}
		return propFile;
	}

	public static InputStream findConfFileInCommonSpace(String fileName) {
		try {

			LOG.info("Trying to find conf file in common space.. fname: " + fileName);
			// Use Any Environmental Variable , here I have used CATALINA_HOME
			String propertyHome = System.getenv("CATALINA_HOME");
			LOG.info("Trying to find conf file in common space, catalina home:" + propertyHome);

			if (null == propertyHome) {
				// This is a system property that is passed
				// using the -D option in the Tomcat startup script
				propertyHome = System.getProperty("PROPERTY_HOME");
				LOG.info("Since catalina home not set, trying with property home:" + propertyHome);
			}

			if (propertyHome == null) {
				// let's try in folder where app resides...
				propertyHome = System.getProperty("user.dir");
			}

			String filePath = propertyHome + "/properties/" + fileName;
			LOG.info("Trying to open properties file:" + filePath);

			File f = new File(filePath);

			if (!f.exists()) {
				LOG.info("Since file does not exist on " + filePath + " last resort would be /etc/" + fileName);
				f = new File("/etc/" + fileName);
			}

			InputStream resourceAsStream = new FileInputStream(f);
			return resourceAsStream;
		} catch (Exception e) {
			LOG.warn("Problem finding conf file in common space", e);
			return null;
		}
	}

	public static boolean storeDataAsFile(byte[] data, String fileName) {

		File f = new File(fileName);
		FileOutputStream fos = null;

		try {
			f.createNewFile();
			fos = new FileOutputStream(f);
			fos.write(data);
			return true;
		} catch (IOException ioe) {
			LOG.warn("Problem storing data as a file: ", ioe);
			return false;
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}

	}

	public static List<String> readTextFile(String fileName) {

		File txtFile = new File(fileName);
	 
		List<String> res = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(txtFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				res.add(line);
			}
		} catch (IOException e) {
			LOG.info("Exception in reasdTextFile", e);
			return null;
		}
       return res;
	}

	public static List<CSVRecord> readCSVFile(String fileName) {

		File csvFile = new File(fileName);
		InputStream ins;
		try {
			ins = new FileInputStream(csvFile);

			return readCSVFile(ins);
		} catch (FileNotFoundException e) {
			LOG.info("file not found ex in reasdCSVFile", e);
			return null;
		}

	}

	public static List<CSVRecord> readCSVFile(InputStream ins) {

		try {

			CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';')
					.parse(new InputStreamReader(ins));
			List<CSVRecord> res = new ArrayList<>();
			for (CSVRecord record : csvParser) {
				res.add(record);
			}

			return res;
		} catch (FileNotFoundException e) {
			LOG.info("file not found ex", e);
			return null;
		} catch (IOException e) {
			LOG.info("IO Ex while readinug CSV file", e);
			return null;
		}
	}
}
