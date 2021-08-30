package com.db.logcollector.app.extractor;

import com.db.logcollector.app.dto.InputEvent;

public interface Validator<INPUT extends InputEvent> {
boolean validate(INPUT input);
}
