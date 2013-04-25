package de;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;

import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;

public class TestDB extends Database
{

	Connection myDB = null;

	public TestDB(DatabaseType databaseType) throws ClassNotFoundException
	{
		super(databaseType);

		System.setProperty("sqlite.purejava", "true");
		Class.forName("org.sqlite.JDBC");
	}

	@Override
	public void Close()
	{
		try
		{
			myDB.close();
			myDB = null;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void Initialize()
	{
		if (myDB == null)
		{
			File dbfile = new File(databasePath);
			if (!dbfile.exists()) Reset();

			try
			{
				myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
			}
			catch (Exception exc)
			{
				return;
			}
		}
	}

	@Override
	public void Reset()
	{
		// if exists, delete old database file
		File file = new File(databasePath);
		if (file.exists()) file.delete();

		try
		{
			myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
			myDB.commit();
			myDB.close();

		}
		catch (Exception exc)
		{

		}
	}

	@Override
	public CoreCursor rawQuery(String sql, String[] args)
	{
		if (myDB == null) return null;
		ResultSet rs = null;

		try
		{

			PreparedStatement statement = myDB.prepareStatement(sql);

			if (args != null)
			{
				for (int i = 0; i < args.length; i++)
				{
					statement.setString(i + 1, args[i]);
				}
			}
			rs = statement.executeQuery();

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		// TODO Hack to get Rowcount
		ResultSet rs2 = null;
		int rowcount = 0;

		try
		{

			PreparedStatement statement2 = myDB.prepareStatement("select count(*) from (" + sql + ")");

			if (args != null)
			{
				for (int i = 0; i < args.length; i++)
				{
					statement2.setString(i + 1, args[i]);
				}
			}
			rs2 = statement2.executeQuery();

			rs2.next();

			rowcount = Integer.parseInt(rs2.getString(1));

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return new TestCursor(rs, rowcount);
	}

	@Override
	public void execSQL(String sql)
	{
		if (myDB == null) return;
		Statement statement;
		try
		{
			statement = myDB.createStatement();
			statement.execute(sql);
		}
		catch (SQLException e)
		{

			e.printStackTrace();
		}

	}

	@Override
	public long update(String tablename, Parameters val, String whereClause, String[] whereArgs)
	{
		if (myDB == null) return 0;

		StringBuilder sql = new StringBuilder();

		sql.append("update ");
		sql.append(tablename);
		sql.append(" set");

		int i = 0;
		for (Entry<String, Object> entry : val.entrySet())
		{
			i++;
			sql.append(" ");
			sql.append(entry.getKey());
			sql.append("=?");
			if (i != val.size())
			{
				sql.append(",");
			}
		}

		if (!whereClause.isEmpty())
		{
			sql.append(" where ");
			sql.append(whereClause);
		}

		try
		{
			PreparedStatement st = myDB.prepareStatement(sql.toString());

			int j = 0;
			for (Entry<String, Object> entry : val.entrySet())
			{
				j++;
				st.setObject(j, entry.getValue());
			}

			if (whereArgs != null)
			{
				for (int k = 0; k < whereArgs.length; k++)
				{
					st.setString(j + k + 1, whereArgs[k]);
				}
			}

			return st.executeUpdate();

		}
		catch (SQLException e)
		{
			return 0;
		}

	}

	@Override
	public long insert(String tablename, Parameters val)
	{
		if (myDB == null) return 0;
		StringBuilder sql = new StringBuilder();

		sql.append("insert into ");
		sql.append(tablename);
		sql.append(" (");

		int i = 0;
		for (Entry<String, Object> entry : val.entrySet())
		{
			i++;
			sql.append(" ");
			sql.append(entry.getKey());
			if (i != val.size())
			{
				sql.append(",");
			}
		}

		sql.append(" ) Values(");

		for (int k = 1; k <= val.size(); k++)
		{
			sql.append(" ");
			sql.append("?");
			if (k < val.size())
			{
				sql.append(",");
			}
		}

		sql.append(" )");

		try
		{
			PreparedStatement st = myDB.prepareStatement(sql.toString());

			int j = 0;
			for (Entry<String, Object> entry : val.entrySet())
			{
				j++;
				st.setObject(j, entry.getValue());
			}

			// return st.executeUpdate();
			return st.execute() ? 0 : 1;

		}
		catch (SQLException e)
		{
			return 0;
		}

	}

	@Override
	public long delete(String tablename, String whereClause, String[] whereArgs)
	{
		if (myDB == null) return 0;
		StringBuilder sql = new StringBuilder();

		sql.append("delete from ");
		sql.append(tablename);

		if (!whereClause.isEmpty())
		{
			sql.append(" where ");
			sql.append(whereClause);
		}

		try
		{
			PreparedStatement st = myDB.prepareStatement(sql.toString());

			if (whereArgs != null)
			{
				for (int i = 0; i < whereArgs.length; i++)
				{
					st.setString(i + 1, whereArgs[i]);
				}
			}

			return st.executeUpdate();

		}
		catch (SQLException e)
		{
			return 0;
		}

	}

	@Override
	public void beginTransaction()
	{
		try
		{
			if (myDB != null) myDB.setAutoCommit(false);
		}
		catch (SQLException e)
		{

			e.printStackTrace();
		}
	}

	@Override
	public void setTransactionSuccessful()
	{
		try
		{
			if (myDB != null) myDB.commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void endTransaction()
	{
		try
		{
			if (myDB != null) myDB.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public long insertWithConflictReplace(String tablename, Parameters val)
	{
		if (myDB == null) return 0;

		StringBuilder sql = new StringBuilder();

		sql.append("insert OR REPLACE into ");
		sql.append(tablename);
		sql.append(" (");

		int i = 0;
		for (Entry<String, Object> entry : val.entrySet())
		{
			i++;
			sql.append(" ");
			sql.append(entry.getKey());
			if (i != val.size())
			{
				sql.append(",");
			}
		}

		sql.append(" ) Values(");

		for (int k = 1; k <= val.size(); k++)
		{
			sql.append(" ");
			sql.append("?");
			if (k < val.size())
			{
				sql.append(",");
			}
		}

		sql.append(" )");

		try
		{
			PreparedStatement st = myDB.prepareStatement(sql.toString());

			int j = 0;
			for (Entry<String, Object> entry : val.entrySet())
			{
				j++;
				st.setObject(j, entry.getValue());
			}

			return st.executeUpdate();

		}
		catch (SQLException e)
		{
			return 0;
		}

	}

	@Override
	public long insertWithConflictIgnore(String tablename, Parameters val)
	{
		if (myDB == null) return 0;

		StringBuilder sql = new StringBuilder();

		sql.append("insert OR IGNORE into ");
		sql.append(tablename);
		sql.append(" (");

		int i = 0;
		for (Entry<String, Object> entry : val.entrySet())
		{
			i++;
			sql.append(" ");
			sql.append(entry.getKey());
			if (i != val.size())
			{
				sql.append(",");
			}
		}

		sql.append(" ) Values(");

		for (int k = 1; k <= val.size(); k++)
		{
			sql.append(" ");
			sql.append("?");
			if (k < val.size())
			{
				sql.append(",");
			}
		}

		sql.append(" )");

		try
		{
			PreparedStatement st = myDB.prepareStatement(sql.toString());

			int j = 0;
			for (Entry<String, Object> entry : val.entrySet())
			{
				j++;
				st.setObject(j, entry.getValue());
			}

			return st.executeUpdate();

		}
		catch (SQLException e)
		{
			return 0;
		}

	}

	@Override
	public int getCacheCountInDB(String filename)
	{

		if (myDB == null) return 0;

		int count = 0;
		Connection myDB = null;
		try
		{
			myDB = DriverManager.getConnection("jdbc:sqlite:" + filename);

			Statement statement = myDB.createStatement();
			ResultSet result = statement.executeQuery("select count(*) from caches");
			// result.first();
			count = result.getInt(1);
			result.close();
			myDB.close();
		}
		catch (SQLException e)
		{
			// String s = e.getMessage();
		}
		return count;
	}

}
