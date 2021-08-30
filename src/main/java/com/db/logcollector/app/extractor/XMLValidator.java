package com.db.logcollector.app.extractor;

import org.apache.commons.lang3.StringUtils;

import com.db.logcollector.app.dto.XMLInputEvent;

public class XMLValidator implements Validator<XMLInputEvent> {

	@Override
	public boolean validate(XMLInputEvent input) {
		
		if (input.getActivityTypeCode() <1 || input.getActivityTypeCode()>2)
			return false; 
		
		if (input.getLoggedInTime() == null)
			return false;
		
		if ( StringUtils.isBlank(input.getUserName() ) || input.getUserName().length()<3 )
			return false;
		
		//probably website address validation should get in here
		if ( StringUtils.isBlank(input.getWebsiteName() ) || input.getWebsiteName().length()<6 )
			return false;
		
		
		return true;
	}

}
