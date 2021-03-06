package de.droidcachebox.database;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AndroidDB extends Database {

    public AndroidDB(DatabaseType databaseType, Activity activity) {
        super(databaseType);
        this.sql = new SQLiteClass(activity);
    }

    @Override
    public int getCacheCountInDB(String filename) {
        try {
            SQLiteDatabase myDB = SQLiteDatabase.openDatabase(filename, null, SQLiteDatabase.OPEN_READONLY);
            Cursor c = myDB.rawQuery("select count(*) from caches", null);
            c.moveToFirst();
            int count = c.getInt(0);
            c.close();
            myDB.close();
            return count;
        } catch (Exception ex) {
        }
        return 0;
    }
}
