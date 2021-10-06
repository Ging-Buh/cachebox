package de.droidcachebox.database;

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.utils.log.Log;

public class SettingsDatabase extends Database_Core {
    private static final String sKlasse = "SettingsDatabase";
    private static SettingsDatabase settings;

    private SettingsDatabase() {
        super();
        latestDatabaseChange = DatabaseVersions.SettingsLatestVersion;
        if (PlatformUIBase.canUsePlatformSettings()) sql = PlatformUIBase.getSQLInstance();
        settings = this;
    }

    public static SettingsDatabase getInstance() {
        if (settings == null) settings = new SettingsDatabase();
        return settings;
    }

    public void setSQL(SQLiteInterface _sql) {
        sql = _sql;
    }

    @Override
    protected void alterDatabase(int lastDatabaseSchemeVersion) {
        sql.beginTransaction();
        try {
            if (lastDatabaseSchemeVersion <= 0) {
                // First Initialization of the Database
                sql.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                sql.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
            }
            if (lastDatabaseSchemeVersion < 1002) {
                // Long Text Field for long Strings
                sql.execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");
            }
            sql.setTransactionSuccessful();
        } catch (Exception exc) {
            Log.err(sKlasse, "alterDatabase", "", exc);
        } finally {
            sql.endTransaction();
        }
    }

    @Override
    public void close() {
        if (sql != null) sql.close();
        sql = null;
        settings = null;
    }

}
