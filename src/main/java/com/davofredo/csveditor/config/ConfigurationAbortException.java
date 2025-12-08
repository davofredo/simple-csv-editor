package com.davofredo.csveditor.config;

public class ConfigurationAbortException extends RuntimeException {
    public ConfigurationAbortException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationAbortException(String message) {
        super(message);
    }
}
