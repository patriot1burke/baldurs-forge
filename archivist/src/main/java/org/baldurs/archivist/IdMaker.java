package org.baldurs.archivist;

import java.util.UUID;

public class IdMaker {
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String handle() {
        String uuid = UUID.randomUUID().toString();
        return "h" + uuid.replace('-', 'g');
    }

}
