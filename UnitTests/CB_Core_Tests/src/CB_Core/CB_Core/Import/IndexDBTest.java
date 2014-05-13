package CB_Core.CB_Core.Import;

import java.io.File;

import __Static.InitTestDBs;
import junit.framework.TestCase;
import CB_Core.Import.CacheInfoList;

public class IndexDBTest extends TestCase
{

	public static void testGpxImport() throws Exception
	{

		// initialize Database
		String database = "./testdata/test.db3";
		File dbFile = new File(database);

		if (!dbFile.exists())
		{
			// Um diesen Test ausführen zu können, muss eine db3 exestieren.
			// Sollte sie nicht exestieren, wird sie über den ImportTest angelegt.
			GpxImportTest.testGpxImport();
		}
		else
		{
			// die DB muss erstmal geladen werden.
			InitTestDBs.InitTestDB(database);
		}

		CacheInfoList.IndexDB();

		CacheInfoList.dispose();

	}

}
