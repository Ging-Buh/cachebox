package de.droidcachebox.database;

import de.droidcachebox.Platform;
import de.droidcachebox.utils.log.Log;

public class SettingsDatabase extends Database_Core {
    private static final String sClass = "SettingsDatabase";
    private static SettingsDatabase settingsDatabase;

    private SettingsDatabase() {
        super();
        latestDatabaseChange = DatabaseVersions.SettingsLatestVersion;
        if (!Platform.canNotUsePlatformSettings()) sql = Platform.createSQLInstance();
        settingsDatabase = this;
    }

    public static SettingsDatabase getInstance() {
        if (settingsDatabase == null) settingsDatabase = new SettingsDatabase();
        return settingsDatabase;
    }

    public void setSQL(SQLiteInterface _sql) {
        sql = _sql;
    }

    @Override
    protected void alterDatabase(int lastDatabaseSchemeVersion) {
        beginTransaction();
        try {
            if (lastDatabaseSchemeVersion <= 0) {
                // First Initialization of the Database
                execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
            }
            if (lastDatabaseSchemeVersion < 1002) {
                // Long Text Field for long Strings
                execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");
            }
            setTransactionSuccessful();
        } catch (Exception exc) {
            Log.err(sClass, "alterDatabase", "", exc);
        } finally {
            endTransaction();
        }
    }

    @Override
    public void close() {
        databasePath = "";
        if (sql != null) sql.close();
        sql = null;
        settingsDatabase = null;
    }

}
