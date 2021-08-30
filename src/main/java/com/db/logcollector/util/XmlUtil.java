package com.db.logcollector.util;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.xml.sax.SAXException;

public final class XmlUtil {
	private final static Logger LOG = Log.getLogger(XmlUtil.class);
	
	public static boolean validateAgainstXSD(InputStream xml, InputStream xsd)
	{
	    try
	    {
	        Schema schema = getSchema(xsd);
	        Validator validator = schema.newValidator();
	        validator.validate(new StreamSource(xml));
	        return true;
	    }
	    catch(Exception ex)
	    {
	        return false;
	    }
	}


	public static Schema getSchema(InputStream xsd) throws SAXException {
		SchemaFactory factory = 
		    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(new StreamSource(xsd));
		return schema;
	}
	

	public static boolean validateAgainstSchema(InputStream xml,  Schema schema)
	{
	    try
	    {
	    	 	
 	        Validator validator = schema.newValidator();
	        validator.validate(new StreamSource(xml));
	        return true;
	    }
	    catch(Exception ex)
	    {
	    	LOG.info("Error validating schema", ex);
	        return false;
	    }
	}
	
}
