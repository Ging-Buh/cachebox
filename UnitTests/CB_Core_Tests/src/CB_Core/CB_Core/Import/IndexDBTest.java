package CB_Core.CB_Core.Import;

import CB_Utils.fileProvider.File;

import CB_Utils.fileProvider.FileFactory;
import __Static.InitTestDBs;
import junit.framework.TestCase;
import CB_Core.Import.CacheInfoList;

public class IndexDBTest extends TestCase {

	public static void testGpxImport() throws Exception {

		// initialize Database
		String database = "./testdata/test.db3";
		File dbFile = FileFactory.createFile(database);

		if (!dbFile.exists()) {
			// Um diesen Test ausf�hren zu k�nnen, muss eine db3 exestieren.
			// Sollte sie nicht exestieren, wird sie �ber den ImportTest angelegt.
			GpxImportTest.testGpxImport();
		} else {
			// die DB muss erstmal geladen werden.
			InitTestDBs.InitTestDB(database);
		}

		CacheInfoList.IndexDB();

		CacheInfoList.dispose();

	}

}
