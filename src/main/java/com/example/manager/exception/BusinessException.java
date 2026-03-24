package com.example.manager.exception;

/**
 * Custom exception cho business rule violation (400)
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
