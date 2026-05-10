package com.kathalife.core.stt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SarvamSttResponse(
        String transcript,
        @JsonProperty("language_code") String languageCode,
        @JsonProperty("request_id") String requestId
) {}