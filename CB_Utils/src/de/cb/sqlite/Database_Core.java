package de.cb.sqlite;

import org.slf4j.LoggerFactory;

public abstract class Database_Core
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Database_Core.class);

	public long DatabaseId = 0;
	public long MasterDatabaseId = 0;

	public final SQLite db;

	public Database_Core(SQLite DB)
	{
		this.db = DB;
	}

	public abstract void AlterDatabase(int lastDatabaseSchemeVersion);

	public abstract void Initialize();

	public abstract void Reset();

	public abstract void Close();

	public abstract boolean StartUp();

}
