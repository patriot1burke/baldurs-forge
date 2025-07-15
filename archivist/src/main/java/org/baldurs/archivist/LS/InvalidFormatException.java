package org.baldurs.archivist.LS;

/**
 * Exception for invalid format errors
 */
public class InvalidFormatException extends RuntimeException {
    public InvalidFormatException(String message) {
        super(message);
    }
} 