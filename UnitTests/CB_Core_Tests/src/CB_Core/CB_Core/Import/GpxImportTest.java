package CB_Core.CB_Core.Import;

import java.io.File;

import junit.framework.TestCase;
import CB_Core.DAO.LogDAO;
import CB_Core.DB.Database;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.ImportHandler;
import CB_Utils.Log.Logger;
import Types.CacheTest;
import __Static.InitTestDBs;

public class GpxImportTest extends TestCase
{

	public static void testGpxImport() throws Exception
	{

		InitTestDBs.InitalConfig();

		// initialize Database
		String database = "./testdata/test.db3";
		InitTestDBs.InitTestDB(database);

		// First must delete DB entry from last TestRun
		{
			Database.Data.delete("Caches", "GcCode='" + "GC2T9RW" + "'", null);
			// Logs
			Logger.DEBUG("Delete Logs");
			LogDAO logdao = new LogDAO();
			logdao.ClearOrphanedLogs();
			logdao = null;
		}

		ImportHandler importHandler = new ImportHandler();

		Database.Data.beginTransaction();

		try
		{
			GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/GC2T9RW.gpx"));
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);

			Database.Data.setTransactionSuccessful();
		}
		finally
		{
		}

		Database.Data.endTransaction();

		CacheTest.test_assertCache_GC2T9RW_with_details();
	}

}
