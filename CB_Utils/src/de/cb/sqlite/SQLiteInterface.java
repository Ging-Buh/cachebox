package de.cb.sqlite;

import java.util.HashMap;

public interface SQLiteInterface {
    boolean open(String databasePath);
    boolean openReadOnly(String databasePath);
    boolean create(String databasePath);
    CoreCursor rawQuery(String sql, String[] args);
    boolean execSQL(String sql);
    long insert(String table, HashMap<String, Object> parameters);
    long insertWithConflictReplace(String table, HashMap<String, Object> parameters);
    long insertWithConflictIgnore(String table, HashMap<String, Object> parameters);
    long update(String table, HashMap<String, Object> parameters, String whereClause, String[] whereArgs);
    long delete(String table, String whereClause, String[] whereArgs);
    void beginTransaction();
    void setTransactionSuccessful();
    void endTransaction();
    void close();
    }
