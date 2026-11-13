package com.knowledgeassistant.usermanagementservice.customexception;

@SuppressWarnings("serial")
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}