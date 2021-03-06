package de.droidcachebox.database;

import de.droidcachebox.utils.log.Log;

import java.util.HashMap;


public abstract class Database_Core {
    private static final String log = "Database_Core";
    public long MasterDatabaseId = 0;
    public SQLiteInterface sql;
    protected long DatabaseId = 0; // for Database replication with WinCachebox
    protected int latestDatabaseChange = 0;
    private String databasePath;
    private boolean newDB = false;

    public Database_Core() {
    }

    public boolean isDbNew() {
        return newDB;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public boolean startUp(String databasePath) {
        Log.debug(log, "DB Startup : " + databasePath);

        this.databasePath = databasePath;

        if (!sql.open(databasePath)) {
            sql.create(databasePath);
            newDB = true;
        }

        int databaseSchemeVersion = getDatabaseSchemeVersion();
        if (databaseSchemeVersion < latestDatabaseChange) {
            alterDatabase(databaseSchemeVersion);
            setDatabaseSchemeVersion();
        }
        setDatabaseSchemeVersion();
        return true;
    }

    protected void alterDatabase(int lastDatabaseSchemeVersion) {
    }

    /**
     *
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
            // Update not possible because Key does not exist
            val.put("Key", "DatabaseSchemeVersion");
            sql.insert("Config", val);
        }
    }

    public void writeConfigString(String key, String value) {
        Parameters val = new Parameters();
        val.put("Value", value);
        long anz = sql.update("Config", val, "[Key] like '" + key + "'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", key);
            sql.insert("Config", val);
        }
    }

    public void writeConfigLongString(String key, String value) {
        Parameters val = new Parameters();
        val.put("LongString", value);
        long anz = sql.update("Config", val, "[Key] like '" + key + "'", null);
        if (anz <= 0) {
            // Update not possible because Key does not exist
            val.put("Key", key);
            sql.insert("Config", val);
        }
    }

    public String readConfigString(String key) throws Exception {
        String result = "";
        CoreCursor c;
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

    public String readConfigLongString(String key) throws Exception {
        String result = "";
        CoreCursor c;
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

    public abstract int getCacheCountInDB(String filename);

    // Zur Parameter Übergabe an die DB
    public static class Parameters extends HashMap<String, Object> {
        private static final long serialVersionUID = 6506158947781669528L;
    }

}
