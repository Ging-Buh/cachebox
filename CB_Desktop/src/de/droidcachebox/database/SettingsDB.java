package de.droidcachebox.database;

public class SettingsDB extends SettingsDatabase {
    public SettingsDB() {
        super();
        this.sql = new SQLiteClass();
    }
}
