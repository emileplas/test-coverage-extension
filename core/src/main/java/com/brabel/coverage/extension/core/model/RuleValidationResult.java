package com.brabel.coverage.extension.core.model;

public class RuleValidationResult {

    boolean isSuccessful;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    String message;

    public String getMessage() {
        return message;
    }

    public RuleValidationResult(boolean isSuccessful, String message) {
        this.isSuccessful = isSuccessful;
        this.message = message;
    }
}
