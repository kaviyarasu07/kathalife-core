package com.kathalife.core.stt.exception;

public class SttTranscriptionException extends RuntimeException {

    public SttTranscriptionException(String message) {
        super(message);
    }

    public SttTranscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}