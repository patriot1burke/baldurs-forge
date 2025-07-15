package org.baldurs.archivist.LS;

/**
 * Exception for invalid data errors
 */
public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String message) {
        super(message);
    }
}
