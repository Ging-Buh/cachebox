package de.droidcachebox.database;

import java.io.Serializable;
import java.util.Date;

public class LogEntry implements Serializable {

    private static final long serialVersionUID = -4269566289864187308L;

    /**
     * Benutzername des Loggers
     */
    public String finder = "";

    /**
     * Logtyp, z.B. "Found it!"
     */
    public LogTypes logTypes;

    /**
     * Geschriebener Text
     */
    public String logText = "";

    /**
     * Zeitpunkt
     */
    public Date logDate = new Date(0);

    /**
     * Id des Caches
     */
    public long cacheId = -1;

    /**
     * Id des Logs
     */
    public long logId = -1;

    public void clear() {
        finder = "";
        logTypes = null;
        logText = "";
        logDate = new Date(0);
        cacheId = -1;
        logId = -1;
    }

    public void dispose() {
        finder = null;
        logTypes = null;
        logText = null;
        logDate = null;
    }

}
