package CB_Core.DB;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;

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
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
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
			Connection myDB = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new TestCursor(rs, rowcount);
	}

	@Override
	public void execSQL(String sql)
	{
		Statement statement;
		try
		{
			statement = myDB.createStatement();
			statement.execute(sql);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public long update(String tablename, Parameters val, String whereClause, String[] whereArgs)
	{

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

			return st.executeUpdate();

		}
		catch (SQLException e)
		{
			return 0;
		}

	}

	@Override
	public long delete(String tablename, String whereClause, String[] whereArgs)
	{

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
			myDB.setAutoCommit(false);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setTransactionSuccessful()
	{
		try
		{
			myDB.commit();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void endTransaction()
	{
		try
		{
			myDB.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public long insertWithConflictReplace(String tablename, Parameters val)
	{
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

}
