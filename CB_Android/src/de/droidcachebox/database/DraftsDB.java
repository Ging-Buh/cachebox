package de.droidcachebox.database;

import android.app.Activity;

public class DraftsDB extends DraftsDatabase {
    public DraftsDB(Activity activity) {
        super();
        this.sql = new SQLiteClass(activity);
    }

}
