package de.droidcachebox.database;

import android.app.Activity;

public class SettingsDB extends SettingsDatabase {
    public SettingsDB(Activity activity) {
        super();
        this.sql = new SQLiteClass(activity);
    }
}
