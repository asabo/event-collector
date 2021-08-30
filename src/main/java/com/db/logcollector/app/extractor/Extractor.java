package com.db.logcollector.app.extractor;

import com.db.logcollector.app.dto.Event;
import com.db.logcollector.app.dto.InputEvent;

public interface Extractor<INPUT extends InputEvent> {
	Event extract(INPUT input);
}
