package de.droidcachebox.database;

import java.util.HashMap;

import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.log.Log;

/*
 Each database (<cachebox>, FieldNotes, Config) has its own Config table with the columns Key, Value and LongString (not for Fieldnotes),
 with at least the Keys DatabaseSchemeVersion and DatabaseSchemeVersionWin, that have identical values.

 The <cachebox> may be a copy or an extract of a master-database in Windows by WinCacheBox (, but must not necessary be one).
 A replication table is filled with changes, to be simply re-transferred into the master-database.
 */

public abstract class Database_Core {
    private static final String sClass = "Database_Core";
    public long MasterDatabaseId;
    protected SQLiteInterface sql;
    protected long DatabaseId; // for Database replication with WinCachebox
    protected int latestDatabaseChange;
    protected String databasePath;
    protected boolean isOpen;
    private boolean isNewDB;

    public Database_Core() {
        databasePath = "";
        isOpen = false;
        isNewDB = false;
        latestDatabaseChange = 0;
        DatabaseId = 0;
        MasterDatabaseId = 0;
    }

    public void beginTransaction() {
        // Log.info(sClass, "beginTransaction");
        sql.beginTransaction();
    }

    public void setTransactionSuccessful() {
        // Log.info(sClass, "setTransactionSuccessful");
        sql.setTransactionSuccessful();
    }

    public void endTransaction() {
        // Log.info(sClass, "endTransaction");
        sql.endTransaction();
    }

    public void execSQL(String sqlCommand) {
        // Log.info(sClass, "execSQL");
        sql.execSQL(sqlCommand);
    }

    public CoreCursor rawQuery(String sqlCommand, String[] args) {
        // Log.info(sClass, "rawQuery");
        return sql.rawQuery(sqlCommand, args);
    }

    public long insert(String table, HashMap<String, Object> parameters) {
        // Log.info(sClass, "insert");
        return sql.insert(table, parameters);
    }

    public void insertWithConflictReplace(String table, HashMap<String, Object> parameters) {
        // Log.info(sClass, "insertWithConflictReplace");
        sql.insertWithConflictReplace(table, parameters);
    }

    public void insertWithConflictIgnore(String table, HashMap<String, Object> parameters) {
        // Log.info(sClass, "insertWithConflictIgnore");
        sql.insertWithConflictIgnore(table, parameters);
    }

    public long update(String table, HashMap<String, Object> parameters, String whereClause, String[] whereArgs) {
        // Log.info(sClass, "update");
        return sql.update(table, parameters, whereClause, whereArgs);
    }

    public long delete(String table, String whereClause, String[] whereArgs) {
        // Log.info(sClass, "delete");
        return sql.delete(table, whereClause, whereArgs);
    }

    public boolean isDatabaseNew() {
        // Log.info(sClass, "isDatabaseNew");
        return isNewDB;
    }

    public String getDatabasePath() {
        // Log.info(sClass, "getDatabasePath");
        return databasePath;
    }

    public boolean isOpen() {
        return isOpen;
    }


    public void startUp(String databasePath) {
        // Log.info(sClass, "DB Startup : " + databasePath);

        this.databasePath = databasePath;

        AbstractFile dbFile = FileFactory.createFile(databasePath);
        if (dbFile.exists()) {
            if (!sql.open(databasePath)) {
                Log.err(sClass, "Error open " + databasePath);
            }
        } else {
            if (sql.create(databasePath)) {
                isNewDB = true;
            } else {
                Log.err(sClass, "Error create " + databasePath);
            }
        }
        isOpen = true;

        int databaseSchemeVersion = getDatabaseSchemeVersion();
        if (databaseSchemeVersion < latestDatabaseChange) {
            alterDatabase(databaseSchemeVersion);
            setDatabaseSchemeVersion();
        }
        setDatabaseSchemeVersion();
    }

    protected abstract void alterDatabase(int lastDatabaseSchemeVersion);

    /**
     * @return -1 --> must create a new database
     */
    private int getDatabaseSchemeVersion() {
        int result = -1;
        CoreCursor c;
        try {
            c = sql.rawQuery("select Value from Config where [Key] like ?", new String[]{"DatabaseSchemeVersionWin"});
        } catch (Exception exc) {
            return -1;
        }
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                String databaseSchemeVersion = c.getString(0);
                result = Integer.parseInt(databaseSchemeVersion);
                c.moveToNext();
            }
        } catch (Exception exc) {
            result = -1;
        }
        if (c != null) {
            c.close();
        }

        return result;
    }

    private void setDatabaseSchemeVersion() {
        Parameters val = new Parameters();
        val.put("Value", latestDatabaseChange);
        long anz = sql.update("Config", val, "[Key] like 'DatabaseSchemeVersionWin'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", "DatabaseSchemeVersionWin");
            sql.insert("Config", val);
        }
        // for Compatibility with WinCB
        val.put("Value", latestDatabaseChange);
        anz = sql.update("Config", val, "[Key] like 'DatabaseSchemeVersion'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist, so insert
            val.put("Key", "DatabaseSchemeVersion");
            sql.insert("Config", val);
        }
    }

    public void writeConfigString(String key, String value) {
        writeConfig(key,value,false);
    }

    public void writeConfigLongString(String key, String value) {
        writeConfig(key,value,true);
    }

    private void writeConfig(String key, String value, boolean longString) {
        Parameters val = new Parameters();
        if (longString)
            val.put("LongString", value);
        else
            val.put("Value", value);
        long anz = sql.update("Config", val, "[Key] like '" + key + "'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", key);
            sql.insert("Config", val);
        }
    }

    public String readConfigString(String key) throws Exception {
        return readConfig(key, "select Value from Config where [Key] like ?");
    }

    public String readConfigLongString(String key) throws Exception {
        return readConfig(key, "select LongString from Config where [Key] like ?");
    }

    private String readConfig(String key, String select) throws Exception {
        CoreCursor c = sql.rawQuery(select, new String[]{key});
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                String retValue = c.getString(0);
                c.close();
                return retValue;
            } else {
                c.close();
            }
        }
        throw new Exception("not in DB");
    }

    public void writeConfigLong(String key, long value) {
        writeConfigString(key, String.valueOf(value));
    }

    protected long readConfigLong(String key) {
        try {
            String value = readConfigString(key);
            return Long.parseLong(value);
        } catch (Exception ex) {
            return 0;
        }
    }

    public abstract void close();

    public static class Parameters extends HashMap<String, Object> {
        private static final long serialVersionUID = 6506158947781669528L;
    }

}
