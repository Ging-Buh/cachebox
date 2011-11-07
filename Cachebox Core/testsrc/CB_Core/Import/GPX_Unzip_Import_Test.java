package CB_Core.Import;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;

import junit.framework.TestCase;

/**
 * Enthält die Tests zum Entpacken von Zip Files und deren anschliesenden GPX
 * Import
 * 
 * @author Longri
 */
public class GPX_Unzip_Import_Test extends TestCase
{

	/**
	 * Startet den Unzip Test und Importiert dann die Entpackten GPX Files
	 * 
	 * @throws Exception
	 */
	public static void testUnzip_Import() throws Exception
	{

		// starte Unzip Test
		UnzipTest.testUnzip();

		// Importiere all GPX files
		ImportHandler importHandler = new ImportHandler();

		File Dir = new File("./testdata/gpx/GS_PQ");
		ArrayList<String> ordnerInhalt = Importer.recursiveDirectoryReader(Dir,
				new ArrayList<String>());
		for (String tmp : ordnerInhalt)
		{
			GPXFileImporter importer = new GPXFileImporter(tmp);
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);
		}

		// aufgrund der Fülle von Caches und Logs bei diesem Import
		// wird nur auf die Anzahl getestet!

		int CacheCount = importHandler.cacheCount;
		assertTrue("Anzahl der Importierten Caches stimmt nicht",
				CacheCount == 500);

		int LogCount = importHandler.logCount;
		assertTrue("Anzahl der Importierten Logs stimmt nicht",
				LogCount == 5068);
		
		int WaypointCount = importHandler.waypointCount;
		assertTrue("Anzahl der Importierten Waypoints stimmt nicht",
				LogCount == 183);

	}
}
