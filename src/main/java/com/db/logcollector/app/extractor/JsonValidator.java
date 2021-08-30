package com.db.logcollector.app.extractor;

import org.apache.commons.lang3.StringUtils;

import com.db.logcollector.app.dto.Activity;

public class JsonValidator implements Validator<Activity> {

	@Override
	public boolean validate(Activity input) {
		
		if (StringUtils.isBlank( input.getActivityTypeDescription()) || input.getActivityTypeDescription().length() < 3) 
			return false;
		
		if (input.getSignedInTime() == null)
			return false;
		
		if ( StringUtils.isBlank(input.getUserName() ) || input.getUserName().length()<3 )
			return false;
		
		//probably website address validation should get in here
		if ( StringUtils.isBlank(input.getWebsiteName() ) || input.getWebsiteName().length()<6 )
			return false;
		
		
		return true;
	}

}
