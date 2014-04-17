package de;

import java.sql.ResultSet;
import java.sql.SQLException;

import CB_Utils.DB.CoreCursor;

public class TestCursor extends CoreCursor
{
	private ResultSet rs;
	private int rowcount;

	public TestCursor(ResultSet rs)
	{
		this.rs = rs;
	}

	public TestCursor(ResultSet rs, int rowcount)
	{
		this.rs = rs;
		this.rowcount = rowcount;

	}

	@Override
	public boolean moveToFirst()
	{
		try
		{
			if (rs.isFirst()) return true;
			return rs.first();
		}
		catch (Exception e)
		{
			return false;
		}
	}

	@Override
	public boolean isAfterLast()
	{

		try
		{
			return rs.isAfterLast();
		}
		catch (Exception e)
		{
			return true;
		}

	}

	@Override
	public boolean moveToNext()
	{
		try
		{
			return rs.next();
		}
		catch (SQLException e)
		{
			return false;
		}
	}

	@Override
	public void close()
	{
		try
		{
			if (rs != null) rs.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		rs = null;
	}

	@Override
	public String getString(int columnIndex)
	{

		try
		{
			return rs.getString(columnIndex + 1);
		}
		catch (SQLException e)
		{
			return null;
		}

	}

	@Override
	public long getLong(int columnIndex)
	{

		try
		{
			return rs.getLong(columnIndex + 1);
		}
		catch (SQLException e)
		{
			return 0;
		}

	}

	@Override
	public int getInt(int columnIndex)
	{
		try
		{
			return rs.getInt(columnIndex + 1);
		}
		catch (SQLException e)
		{
			return 0;
		}
	}

	@Override
	public boolean isNull(int columnIndex)
	{

		try
		{
			if (rs.getObject(columnIndex + 1) == null || rs.getObject(columnIndex + 1).toString().length() == 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (SQLException e)
		{
			return false;
		}

	}

	@Override
	public double getDouble(int columnIndex)
	{
		try
		{
			return rs.getDouble(columnIndex + 1);
		}
		catch (SQLException e)
		{
			return 0;
		}
	}

	@Override
	public short getShort(int columnIndex)
	{
		try
		{
			return rs.getShort(columnIndex + 1);
		}
		catch (SQLException e)
		{
			return 0;
		}
	}

	@Override
	public int getCount()
	{
		return rowcount;
	}

}