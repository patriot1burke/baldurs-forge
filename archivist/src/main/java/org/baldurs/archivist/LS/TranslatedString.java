package org.baldurs.archivist.LS;

/**
 * Translated string class
 */
public class TranslatedString {
    public int version = 0;
    public String value;
    public String handle;
    
    @Override
    public String toString() {
        if (value != null && !value.isEmpty()) {
            return value;
        } else {
            return handle + ";" + version;
        }
    }
} 