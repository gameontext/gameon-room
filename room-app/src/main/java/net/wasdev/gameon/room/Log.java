package net.wasdev.gameon.room;

import java.util.logging.Logger;

public class Log {
    private final static Logger log = Logger.getLogger("gameon");
    static final String log_format = "%-10s - %s";

    public static void endPoint(Object source, String message) {
        log.fine(String.format(log_format, source == null ? "null" : System.identityHashCode(source), message));
    }
}
