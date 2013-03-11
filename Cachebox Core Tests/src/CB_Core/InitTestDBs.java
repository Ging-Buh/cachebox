package CB_Core;

import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.DB.TestDB;
import CB_Core.Types.Categories;

/**
 * Initialisiert die Config oder eine TestDB
 * 
 * @author Longri
 */
public class InitTestDBs
{
	/**
	 * Initialisiert die Config für die Tests! initialisiert wird die Config mit der unter Testdata abgelegten config.db3
	 */
	public static void InitalConfig()
	{

		if (Config.settings != null && Config.settings.isLoaded()) return;

		// Read Config
		String workPath = "./testdata";

		Config.Initialize(workPath, workPath + "/cachebox.config");

		// hier muss die Config Db initialisiert werden
		try
		{
			Database.Settings = new TestDB(DatabaseType.Settings);
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		if (!FileIO.createDirectory(Config.WorkPath)) return;
		Database.Settings.StartUp(Config.WorkPath + "/Config.db3");
		Config.settings.ReadFromDB();
	}

	/**
	 * Initialisiert eine CacheBox DB für die Tests
	 * 
	 * @param database
	 *            Pfad zur DB
	 * @throws ClassNotFoundException
	 */
	public static void InitTestDB(String database) throws ClassNotFoundException
	{
		Database.Data = new TestDB(DatabaseType.CacheBox);
		Database.Data.StartUp(database);
		GlobalCore.Categories = new Categories();
		Database.Data.GPXFilenameUpdateCacheCount();
	}
}
