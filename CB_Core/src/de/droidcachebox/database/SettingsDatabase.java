package de.droidcachebox.database;

import de.droidcachebox.utils.log.Log;

public class SettingsDatabase extends Database_Core {
    private static final String sKlasse = "SettingsDatabase";
    public static SettingsDatabase Settings;

    public SettingsDatabase() {
        super();
        latestDatabaseChange = DatabaseVersions.SettingsLatestVersion;
        Settings = this;
    }

    @Override
    protected void alterDatabase(int lastDatabaseSchemeVersion) {
        Settings.sql.beginTransaction();
        try {
            if (lastDatabaseSchemeVersion <= 0) {
                // First Initialization of the Database
                Settings.sql.execSQL("CREATE TABLE [Config] ([Key] nvarchar (30) NOT NULL, [Value] nvarchar (255) NULL);");
                Settings.sql.execSQL("CREATE INDEX [Key_idx] ON [Config] ([Key] ASC);");
            }
            if (lastDatabaseSchemeVersion < 1002) {
                // Long Text Field for long Strings
                Settings.sql.execSQL("ALTER TABLE [Config] ADD [LongString] ntext NULL;");
            }
            Settings.sql.setTransactionSuccessful();
        } catch (Exception exc) {
            Log.err(sKlasse, "alterDatabase", "", exc);
        } finally {
            Settings.sql.endTransaction();
        }
    }

}
