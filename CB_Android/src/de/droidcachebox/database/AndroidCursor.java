package de.droidcachebox.database;

import android.database.Cursor;

public class AndroidCursor extends CoreCursor {
    private Cursor cursor;

    public AndroidCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public boolean moveToFirst() {
        return cursor.moveToFirst();
    }

    @Override
    public boolean isAfterLast() {
        return cursor.isAfterLast();
    }

    @Override
    public boolean moveToNext() {
        return cursor.moveToNext();
    }

    @Override
    public void close() {
        cursor.close();
        cursor = null;
    }

    @Override
    public String getString(int columnIndex) {
        String tmp = cursor.getString(columnIndex);
        if (tmp == null) tmp = "";
        return tmp;
    }

    @Override
    public String getString(String column) {
        return cursor.getString(Math.max(0,cursor.getColumnIndex(column)));
    }

    @Override
    public long getLong(int columnIndex) {
        return cursor.getLong(columnIndex);
    }

    @Override
    public long getLong(String column) {
        return cursor.getLong(Math.max(0,cursor.getColumnIndex(column)));
    }

    @Override
    public int getInt(int columnIndex) {
        return cursor.getInt(columnIndex);
    }

    @Override
    public int getInt(String column) {
        return cursor.getInt(Math.max(0,cursor.getColumnIndex(column)));
    }

    @Override
    public boolean isNull(int columnIndex) {
        return cursor.isNull(columnIndex);
    }

    @Override
    public boolean isNull(String column) {
        return cursor.isNull(Math.max(0,cursor.getColumnIndex(column)));
    }

    @Override
    public double getDouble(int columnIndex) {
        return cursor.getDouble(columnIndex);
    }

    @Override
    public double getDouble(String column) {
        return cursor.getDouble(Math.max(0,cursor.getColumnIndex(column)));
    }

    @Override
    public short getShort(int columnIndex) {
        return cursor.getShort(columnIndex);
    }

    @Override
    public short getShort(String column) {
        return cursor.getShort(Math.max(0,cursor.getColumnIndex(column)));
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }
}
