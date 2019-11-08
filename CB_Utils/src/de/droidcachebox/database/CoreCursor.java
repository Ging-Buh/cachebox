package de.droidcachebox.database;

public abstract class CoreCursor {
    public CoreCursor() {
    }

    public abstract boolean moveToFirst();

    public abstract boolean isAfterLast();

    public abstract boolean moveToNext();

    public abstract void close();

    public abstract String getString(int columnIndex);

    public abstract String getString(String column);

    public abstract long getLong(int columnIndex);

    public abstract long getLong(String column);

    public abstract int getInt(int columnIndex);

    public abstract int getInt(String column);

    public abstract boolean isNull(int columnIndex);

    public abstract boolean isNull(String column);

    public abstract double getDouble(int columnIndex);

    public abstract double getDouble(String column);

    public abstract short getShort(int columnIndex);

    public abstract short getShort(String column);

    public abstract int getCount();

}
