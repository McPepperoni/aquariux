package com.aquariux.trading.api;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class ApiErrorResponse {

    Instant timestamp;
    int status;
    String error;
    String message;
    String path;
}
