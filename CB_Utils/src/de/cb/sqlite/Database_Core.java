package de.cb.sqlite;

import CB_Utils.Log.Log;

import java.util.HashMap;


public abstract class Database_Core{
    private static final String log = "Database_Core";
    public long DatabaseId = 0; // for Database replication with WinCachebox
    public long MasterDatabaseId = 0;
    protected String databasePath;
    protected boolean newDB = false;
    protected int latestDatabaseChange = 0;
    public SQLiteInterface sql;

    public Database_Core() {
    }

    public boolean isDbNew() {
        return newDB;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public boolean StartUp(String databasePath) {
        try {
            Log.debug(log, "DB Startup : " + databasePath);
        } catch (Exception e) {
            // gibt beim splash - Start: NPE in Translation.readMissingStringsFile
            // Nachfolgende Starts sollten aber protokolliert werden
        }

        this.databasePath = databasePath;

        if (!sql.open(databasePath)) {
            sql.create(databasePath);
            newDB = true;
        }

        int databaseSchemeVersion = GetDatabaseSchemeVersion();
        if (databaseSchemeVersion < latestDatabaseChange) {
            AlterDatabase(databaseSchemeVersion);
            SetDatabaseSchemeVersion();
        }
        SetDatabaseSchemeVersion();
        return true;
    }

    protected void AlterDatabase(int lastDatabaseSchemeVersion) {
    }

    private int GetDatabaseSchemeVersion() {
        int result = -1;
        CoreCursor c = null;
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

    private void SetDatabaseSchemeVersion() {
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
            // Update not possible because Key does not exist
            val.put("Key", "DatabaseSchemeVersion");
            sql.insert("Config", val);
        }
    }

    public void WriteConfigString(String key, String value) {
        Parameters val = new Parameters();
        val.put("Value", value);
        long anz = sql.update("Config", val, "[Key] like '" + key + "'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", key);
            sql.insert("Config", val);
        }
    }

    public void WriteConfigLongString(String key, String value) {
        Parameters val = new Parameters();
        val.put("LongString", value);
        long anz = sql.update("Config", val, "[Key] like '" + key + "'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", key);
            sql.insert("Config", val);
        }
    }

    public String ReadConfigString(String key) throws Exception {
        String result = "";
        CoreCursor c = null;
        boolean found = false;
        try {
            c = sql.rawQuery("select Value from Config where [Key] like ?", new String[]{key});
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result = c.getString(0);
                found = true;
                c.moveToNext();
            }
        } catch (Exception exc) {
            throw new Exception("not in DB");
        } finally {
            c.close();
        }

        if (!found)
            throw new Exception("not in DB");

        return result;
    }

    public String ReadConfigLongString(String key) throws Exception {
        String result = "";
        CoreCursor c = null;
        boolean found = false;
        try {
            c = sql.rawQuery("select LongString from Config where [Key] like ?", new String[]{key});
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result = c.getString(0);
                found = true;
                c.moveToNext();
            }
        } catch (Exception exc) {
            throw new Exception("not in DB");
        }
        c.close();

        if (!found)
            throw new Exception("not in DB");

        return result;
    }

    public void WriteConfigLong(String key, long value) {
        WriteConfigString(key, String.valueOf(value));
    }

    public long ReadConfigLong(String key) {
        try {
            String value = ReadConfigString(key);
            return Long.valueOf(value);
        } catch (Exception ex) {
            return 0;
        }
    }

    public abstract int getCacheCountInDB(String filename);

    // Zur Parameter Übergabe an die DB
    public static class Parameters extends HashMap<String, Object> {

        /**
         *
         */
        private static final long serialVersionUID = 6506158947781669528L;
    }

}
