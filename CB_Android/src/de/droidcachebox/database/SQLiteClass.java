package de.droidcachebox.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;

public class SQLiteClass implements SQLiteInterface {
    private static final String sClass = "SQLiteClass";
    private final Activity activity;
    private SQLiteDatabase myDB = null;

    public SQLiteClass(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean open(String databasePath) {
        try {
            myDB = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READWRITE);
            return true;
        } catch (Exception ex) {
            // Log.err(log, "open: ", ex); // for a new database
            return false;
        }
    }

    @Override
    public boolean openReadOnly(String databasePath) {
        try {
            myDB = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY);
            return true;
        } catch (Exception ex) {
            Log.err(sClass, "openReadOnly: ", ex);
            return false;
        }
    }

    @Override
    public boolean create(String databasePath) {

        // if exists, delete old database file
        AbstractFile abstractFile = FileFactory.createFile(databasePath);
        if (abstractFile.exists()) {
            try {
                abstractFile.delete();
            } catch (IOException ex) {
                Log.err(sClass, "createDB: delete", ex);
            }
        }

        try {
            AbstractFile dbAbstractFile = FileIO.createFile(databasePath);
            if (dbAbstractFile == null)
                return false;
            myDB = activity.openOrCreateDatabase(dbAbstractFile.getAbsolutePath(), 0, null);
            return true;
        } catch (Exception exc) {
            Log.err(sClass, "createDB: openOrCreateDatabase", exc);
            return false;
        }
    }

    @Override
    public CoreCursor rawQuery(String sql, String[] args) {
        if (myDB == null)
            return null;
        Cursor c = myDB.rawQuery(sql, args);
        return new AndroidCursor(c);
    }

    @Override
    public boolean execSQL(String sql) {
        try {
            myDB.execSQL(sql);
            return true;
        } catch (Exception ex) {
            // Log.err(log, "execSQL: ", ex); // use throw ?
            return false;
        }
    }

    private ContentValues getContentValues(HashMap<String, Object> val) {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            Object o = entry.getValue();
            if (o instanceof Boolean)
                values.put(entry.getKey(), (Boolean) entry.getValue());
            else if (o instanceof Byte)
                values.put(entry.getKey(), (Byte) entry.getValue());
            else if (o instanceof byte[])
                values.put(entry.getKey(), (byte[]) entry.getValue());
            else if (o instanceof Double)
                values.put(entry.getKey(), (Double) entry.getValue());
            else if (o instanceof Float)
                values.put(entry.getKey(), (Float) entry.getValue());
            else if (o instanceof Integer)
                values.put(entry.getKey(), (Integer) entry.getValue());
            else if (o instanceof Long)
                values.put(entry.getKey(), (Long) entry.getValue());
            else if (o instanceof Short)
                values.put(entry.getKey(), (Short) entry.getValue());
            else if (o instanceof String)
                values.put(entry.getKey(), (String) entry.getValue());
            else
                values.put(entry.getKey(), entry.getValue().toString());
        }
        return values;
    }

    @Override
    public long insert(String tableName, HashMap<String, Object> val) {
        long ret = -1;
        try {
            return myDB.insert(tableName, null, getContentValues(val));
        } catch (Exception ex) {
            Log.err(sClass, "insert: ", ex);
        }

        return ret;
    }

    @Override
    public long update(String tableName, HashMap<String, Object> val, String whereClause, String[] whereArgs) {
        try {
            return myDB.update(tableName, getContentValues(val), whereClause, whereArgs);
        } catch (Exception ex) {
            Log.err(sClass, "update: ", ex);
            return 0;
        }
    }

    @Override
    public long delete(String tableName, String whereClause, String[] whereArgs) {
        return myDB.delete(tableName, whereClause, whereArgs);
    }

    @Override
    public void beginTransaction() {
        if (myDB != null)
            myDB.beginTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        if (myDB != null)
            myDB.setTransactionSuccessful();
    }

    @Override
    public void endTransaction() {
        if (myDB != null)
            myDB.endTransaction();
    }

    @Override
    public void insertWithConflictReplace(String tableName, HashMap<String, Object> val) {
        myDB.insertWithOnConflict(tableName, null, getContentValues(val), SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public void insertWithConflictIgnore(String tableName, HashMap<String, Object> val) {
        myDB.insertWithOnConflict(tableName, null, getContentValues(val), SQLiteDatabase.CONFLICT_IGNORE);
    }

    @Override
    public void close() {
        try {
            if (myDB != null)
                myDB.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        myDB = null;
    }

}
