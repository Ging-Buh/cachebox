package de.cb.sqlite;

public class DesktopDatabaseFactory extends DatabaseFactory
{

	@Override
	protected SQLite createInstanz(String Path, AlternateDatabase alter)
	{
		try
		{
			return new DesktopDB(Path, alter);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		return null;
	}

}
