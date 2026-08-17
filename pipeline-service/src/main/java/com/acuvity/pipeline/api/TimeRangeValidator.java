package com.acuvity.pipeline.api;

import java.time.Instant;

final class TimeRangeValidator {

    private TimeRangeValidator() {
    }

    static void validate(Instant from, Instant to) {
        if (to.isBefore(from)) {
            throw new InvalidRequestException("to must not be before from");
        }
    }
}
