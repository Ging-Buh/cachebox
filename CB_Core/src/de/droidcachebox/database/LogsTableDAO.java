package de.droidcachebox.database;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.LogEntry;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.log.Log;

public class LogsTableDAO {
    private static final String sClass = "LogsTableDAO";
    private static LogsTableDAO logsTableDAO;
    private final CB_List<LogEntry> cacheLogs; // depends on the last used GeoCache
    private String lastGeoCache;

    private LogsTableDAO() {
        cacheLogs = new CB_List<>();
        lastGeoCache = "";
        logsTableDAO = this;
    }

    static public LogsTableDAO getInstance() {
        if (logsTableDAO == null) {
            logsTableDAO = new LogsTableDAO();
        }
        return logsTableDAO;
    }

    public void forceRereadingOfGeoCacheLogs() {
        lastGeoCache = "";
    }

    public CB_List<LogEntry> getLogs(Cache cache) {
        if (cache == null) {
            cacheLogs.clear();
            return cacheLogs;
        }
        if (cache.getGeoCacheCode().equals(lastGeoCache)) return cacheLogs;
        cacheLogs.clear();
        lastGeoCache = cache.getGeoCacheCode();
        CoreCursor c = CBDB.getInstance().rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId=@CacheId order by Timestamp desc", new String[]{Long.toString(cache.generatedId)});
        if (c != null) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                LogEntry logEntry = getLogEntry(c);
                if (logEntry != null)
                    cacheLogs.add(logEntry);
                c.moveToNext();
            }
            c.close();
        } else {
            lastGeoCache = "";
        }
        return cacheLogs;
    }

    private LogEntry getLogEntry(CoreCursor reader) {
        int intLogType = reader.getInt("Type");
        if (intLogType < 0 || intLogType >= LogType.values().length)
            return null;

        LogEntry retLogEntry = new LogEntry();

        retLogEntry.cacheId = reader.getLong("CacheId");

        String sDate = reader.getString("Timestamp");
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            retLogEntry.logDate = iso8601Format.parse(sDate);
        } catch (ParseException ignored) {
        }
        retLogEntry.finder = reader.getString("Finder");
        retLogEntry.logType = LogType.values()[intLogType];
        retLogEntry.logText = reader.getString("Comment");
        retLogEntry.logId = reader.getLong("Id");

        int lIndex;

        while ((lIndex = retLogEntry.logText.indexOf('[')) >= 0) {
            int rIndex = retLogEntry.logText.indexOf(']', lIndex);

            if (rIndex == -1)
                break;

            retLogEntry.logText = retLogEntry.logText.substring(0, lIndex) + retLogEntry.logText.substring(rIndex + 1);
        }

        return retLogEntry;
    }

    /**
     * Delete Logs for caches, that no longer exist in table Caches
     */
    public void ClearOrphanedLogs() {
        String SQL = "DELETE  FROM  Logs WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Logs.CacheId = c.Id)";
        CBDB.getInstance().execSQL(SQL);
        forceRereadingOfGeoCacheLogs();
    }

    /**
     * Delete all Logs for Cache
     */
    public void deleteLogs(long cacheId) {
        String SQL = "DELETE  FROM  Logs WHERE Logs.CacheId = " + cacheId;
        CBDB.getInstance().execSQL(SQL);
        forceRereadingOfGeoCacheLogs();
    }

    public void WriteLogEntry(LogEntry logEntry) {
        Database_Core.Parameters args = new Database_Core.Parameters();
        args.put("Id", logEntry.logId);
        args.put("Finder", logEntry.finder);
        args.put("Type", logEntry.logType.ordinal());
        args.put("Comment", logEntry.logText);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String sTimeStamp = iso8601Format.format(logEntry.logDate);
        args.put("Timestamp", sTimeStamp);
        args.put("CacheId", logEntry.cacheId);
        try {
            CBDB.getInstance().insertWithConflictReplace("Logs", args);
        } catch (Exception ex) {
            Log.err(sClass, "Write Log", ex);
        }
        forceRereadingOfGeoCacheLogs();
    }

    /**
     * @param minToKeep      Config.settings.LogMinCount.getValue()
     * @param LogMaxMonthAge Config.settings.LogMaxMonthAge.getValue()
     */
    public void deleteOldLogs(int minToKeep, int LogMaxMonthAge) {

        ArrayList<Long> oldLogCaches = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, -LogMaxMonthAge);
        // hint:
        // months are numbered from 0 onwards in Calendar
        // and month and day have leading zeroes in logs Timestamp
        String TimeStamp = (now.get(Calendar.YEAR)) + "-" + String.format(Locale.US, "%02d", (now.get(Calendar.MONTH) + 1)) + "-" + String.format(Locale.US, "%02d", now.get(Calendar.DATE));

        // #############################################################################
        // Get CacheId's from Caches with older logs and having more logs than minToKeep
        // #############################################################################
        {
            try {
                String command = "SELECT CacheId FROM logs WHERE Timestamp < '" + TimeStamp + "' GROUP BY CacheId HAVING COUNT(Id) > " + minToKeep;
                CoreCursor c = CBDB.getInstance().rawQuery(command, null);
                if (c != null) {
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        long tmp = c.getLong(0);
                        if (!oldLogCaches.contains(tmp))
                            oldLogCaches.add(c.getLong(0));
                        c.moveToNext();
                    }
                    c.close();
                }
            } catch (Exception ex) {
                Log.err(sClass, "deleteOldLogs", ex);
            }
        }

        // ###################################################
        // Get Logs
        // ###################################################
        {
            try {
                CBDB.getInstance().beginTransaction();
                for (long oldLogCache : oldLogCaches) {
                    ArrayList<Long> minLogIds = new ArrayList<>();
                    String command = "select id from logs where CacheId = " + oldLogCache + " order by Timestamp desc";
                    int count = 0;
                    CoreCursor c = CBDB.getInstance().rawQuery(command, null);
                    if (c != null) {
                        c.moveToFirst();
                        while (!c.isAfterLast()) {
                            if (count == minToKeep)
                                break;
                            minLogIds.add(c.getLong(0));
                            c.moveToNext();
                            count++;
                        }
                        c.close();
                    }
                    StringBuilder sb = new StringBuilder();
                    for (long id : minLogIds)
                        sb.append(id).append(",");
                    // now delete all Logs out of Date but keep the ones in minLogIds
                    String delCommand;
                    if (sb.length() > 0)
                        delCommand = "DELETE FROM Logs WHERE Timestamp<'" + TimeStamp + "' AND CacheId = " + oldLogCache + " AND id NOT IN (" + sb.substring(0, sb.length() - 1) + ")";
                    else
                        delCommand = "DELETE FROM Logs WHERE Timestamp<'" + TimeStamp + "' AND CacheId = " + oldLogCache;
                    CBDB.getInstance().execSQL(delCommand);
                }
                CBDB.getInstance().setTransactionSuccessful();
            } catch (Exception ex) {
                Log.err(sClass, "deleteOldLogs", ex);
            } finally {
                CBDB.getInstance().endTransaction();
            }
        }
    }

}
