package de.droidcachebox.database;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.log.Log;

public class LogsTableDAO {
    private static final String log = "LogsTableDAO";
    private static LogsTableDAO logsTableDAO;
    private SQLiteInterface sql;
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
        logsTableDAO.sql = CBDB.getInstance().sql;
        return logsTableDAO;
    }

    public void forceRereadingOfGeoCacheLogs() {
        lastGeoCache = "";
    }

    public CB_List<LogEntry> getLogs(Cache cache) {
        if (cache == null || cache.isDisposed()) {
            cacheLogs.clear();
            return cacheLogs;
        }
        if (cache.getGeoCacheCode().equals(lastGeoCache)) return cacheLogs;
        cacheLogs.clear();
        lastGeoCache = cache.getGeoCacheCode();
        Log.info(log, "Start getLogs for cache: " + cache.getGeoCacheCode());
        CoreCursor reader = sql.rawQuery("select CacheId, Timestamp, Finder, Type, Comment, Id from Logs where CacheId=@CacheId order by Timestamp desc", new String[]{Long.toString(cache.generatedId)});
        if (reader != null) {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                LogEntry logEntry = getLogEntry(reader);
                if (logEntry != null)
                    cacheLogs.add(logEntry);
                reader.moveToNext();
            }
            reader.close();
        } else {
            lastGeoCache = "";
        }
        Log.info(log, "Ready getLogs for cache: " + cache.getGeoCacheCode());
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
        sql.execSQL(SQL);
        forceRereadingOfGeoCacheLogs();
    }

    /**
     * Delete all Logs for Cache
     */
    public void deleteLogs(long cacheId) {
        String SQL = "DELETE  FROM  Logs WHERE Logs.CacheId = " + cacheId;
        sql.execSQL(SQL);
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
            sql.insertWithConflictReplace("Logs", args);
        } catch (Exception exc) {
            Log.err(log, "Write Log", exc);
        }
        forceRereadingOfGeoCacheLogs();
    }

    /**
     * @param minToKeep      Config.settings.LogMinCount.getValue()
     * @param LogMaxMonthAge Config.settings.LogMaxMonthAge.getValue()
     */
    public void deleteOldLogs(int minToKeep, int LogMaxMonthAge) {

        Log.debug(log, "deleteOldLogs but keep " + minToKeep + " and not older than " + LogMaxMonthAge);

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
                Log.debug(log, command);
                CoreCursor reader = sql.rawQuery(command, null);
                reader.moveToFirst();
                while (!reader.isAfterLast()) {
                    long tmp = reader.getLong(0);
                    if (!oldLogCaches.contains(tmp))
                        oldLogCaches.add(reader.getLong(0));
                    reader.moveToNext();
                }
                reader.close();
            } catch (Exception ex) {
                Log.err(log, "deleteOldLogs", ex);
            }
        }

        // ###################################################
        // Get Logs
        // ###################################################
        {
            try {
                sql.beginTransaction();
                for (long oldLogCache : oldLogCaches) {
                    ArrayList<Long> minLogIds = new ArrayList<>();
                    String command = "select id from logs where CacheId = " + oldLogCache + " order by Timestamp desc";
                    Log.debug(log, command);
                    int count = 0;
                    CoreCursor reader = sql.rawQuery(command, null);
                    reader.moveToFirst();
                    while (!reader.isAfterLast()) {
                        if (count == minToKeep)
                            break;
                        minLogIds.add(reader.getLong(0));
                        reader.moveToNext();
                        count++;
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
                    Log.debug(log, delCommand);
                    sql.execSQL(delCommand);
                }
                sql.setTransactionSuccessful();
            } catch (Exception ex) {
                Log.err(log, "deleteOldLogs", ex);
            } finally {
                sql.endTransaction();
            }
        }
    }

}
