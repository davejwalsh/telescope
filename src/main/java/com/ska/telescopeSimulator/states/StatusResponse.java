package com.ska.telescopeSimulator.states;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ska.telescopeSimulator.dto.StatusCode;

public class StatusResponse {

    private final StatusCode status;
    private final String message;

    public StatusResponse(StatusCode status, String message) {
        this.status = status;
        this.message = message;
    }

    public StatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize StatusResponse", e);
        }
    }
}
