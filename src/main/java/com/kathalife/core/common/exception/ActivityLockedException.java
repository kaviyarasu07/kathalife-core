package com.kathalife.core.common.exception;

public class ActivityLockedException extends RuntimeException {
    public ActivityLockedException(String message) {
        super(message);
    }
}
