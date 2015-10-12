package CB_Core.CB_Core.Import;

import java.io.File;
import java.util.ArrayList;

import CB_Core.DB.Database;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.ImportHandler;
import CB_Utils.Util.FileIO;
import __Static.InitTestDBs;
import junit.framework.TestCase;

/**
 * Enth�lt die Tests zum Entpacken von Zip Files und deren anschliesenden GPX Import
 * 
 * @author Longri
 */
public class GPX_Unzip_Import_Test extends TestCase {

    /**
     * Startet den Unzip Test und Importiert dann die Entpackten GPX Files
     * 
     * @throws Exception
     */
    public static void testUnzip_Import() throws Exception {

	// starte Unzip Test
	UnzipTest.testUnzip();

	// initialize Database
	String database = "./testdata/test.db3";
	InitTestDBs.InitTestDB(database);

	// Importiere all GPX files
	ImportHandler importHandler = new ImportHandler();

	Database.Data.db.beginTransaction();

	try {

	    File Dir = new File("./testdata/gpx/GS_PQ");
	    ArrayList<File> ordnerInhalt = FileIO.recursiveDirectoryReader(Dir, new ArrayList<File>());
	    for (File tmp : ordnerInhalt) {
		GPXFileImporter importer = new GPXFileImporter(tmp);
		assertTrue("Objekt muss konstruierbar sein", importer != null);
		importer.doImport(importHandler, 0);
	    }

	    Database.Data.db.setTransactionSuccessful();
	} finally {
	}

	Database.Data.db.endTransaction();

	// aufgrund der F�lle von Caches und Logs bei diesem Import
	// wird nur auf die Anzahl getestet!

	int CacheCount = importHandler.cacheCount;
	assertTrue("Anzahl der Importierten Caches stimmt nicht", CacheCount == 500);

	int LogCount = importHandler.logCount;
	assertTrue("Anzahl der Importierten Logs stimmt nicht", LogCount == 2534);

	int WaypointCount = importHandler.waypointCount;
	assertTrue("Anzahl der Importierten Waypoints stimmt nicht", WaypointCount == 183);

	// Database.Data.Close();
    }
}
