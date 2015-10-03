package de.cb.sqlite;

import android.database.Cursor;
import de.cb.sqlite.CoreCursor;

public class AndroidCursor extends CoreCursor
{
	private Cursor cursor;

	public AndroidCursor(Cursor cursor)
	{
		this.cursor = cursor;
	}

	@Override
	public boolean moveToFirst()
	{
		return cursor.moveToFirst();
	}

	@Override
	public boolean isAfterLast()
	{
		return cursor.isAfterLast();
	}

	@Override
	public boolean moveToNext()
	{
		return cursor.moveToNext();
	}

	@Override
	public void close()
	{
		cursor.close();
		cursor = null;
	}

	@Override
	public String getString(int columnIndex)
	{
		return cursor.getString(columnIndex);
	}

	@Override
	public String getString(String column)
	{
		return cursor.getString(cursor.getColumnIndex(column));
	}

	@Override
	public long getLong(int columnIndex)
	{
		return cursor.getLong(columnIndex);
	}

	@Override
	public long getLong(String column)
	{
		return cursor.getLong(cursor.getColumnIndex(column));
	}

	@Override
	public int getInt(int columnIndex)
	{
		return cursor.getInt(columnIndex);
	}

	@Override
	public int getInt(String column)
	{
		return cursor.getInt(cursor.getColumnIndex(column));
	}

	@Override
	public boolean isNull(int columnIndex)
	{
		return cursor.isNull(columnIndex);
	}

	@Override
	public boolean isNull(String column)
	{
		return cursor.isNull(cursor.getColumnIndex(column));
	}

	@Override
	public double getDouble(int columnIndex)
	{
		return cursor.getDouble(columnIndex);
	}

	@Override
	public double getDouble(String column)
	{
		return cursor.getDouble(cursor.getColumnIndex(column));
	}

	@Override
	public short getShort(int columnIndex)
	{
		return cursor.getShort(columnIndex);
	}

	@Override
	public short getShort(String column)
	{
		return cursor.getShort(cursor.getColumnIndex(column));
	}

	@Override
	public int getCount()
	{
		return cursor.getCount();
	}
}
