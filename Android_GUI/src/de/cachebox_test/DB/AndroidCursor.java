package de.cachebox_test.DB;

import CB_Utils.DB.CoreCursor;
import android.database.Cursor;

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
	public long getLong(int columnIndex)
	{
		return cursor.getLong(columnIndex);
	}

	@Override
	public int getInt(int columnIndex)
	{
		return cursor.getInt(columnIndex);
	}

	@Override
	public boolean isNull(int columnIndex)
	{
		return cursor.isNull(columnIndex);
	}

	@Override
	public double getDouble(int columnIndex)
	{
		return cursor.getDouble(columnIndex);
	}

	@Override
	public short getShort(int columnIndex)
	{
		return cursor.getShort(columnIndex);
	}

	@Override
	public int getCount()
	{
		return cursor.getCount();
	}
}
