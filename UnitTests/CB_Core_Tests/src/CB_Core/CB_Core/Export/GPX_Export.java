/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_Core.CB_Core.Export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DB.Database;
import CB_Core.Export.GpxSerializer;
import CB_Core.Export.GpxSerializer.ProgressListener;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.ImportHandler;
import CB_UI_Base.Global;
import CB_Utils.Log.Logger;
import Types.CacheTest;
import __Static.InitTestDBs;

/**
 * Test the GPX Export
 * 
 * @author Longri
 */
public class GPX_Export extends TestCase
{

	@Test
	public void testSingleExport() throws Exception
	{
		InitTestDBs.InitalConfig();
		// initialize Database
		String database = "./testdata/test.db3";
		InitTestDBs.InitTestDB(database);

		String exportPath = "./testdata/gpx/ExportTest_GC2T9RW.gpx";
		File exportFile = new File(exportPath);

		// Delete File if exist
		if (exportFile.exists()) exportFile.delete();

		// Delete Cache from DB end import from Real-GPX
		{
			Database.Data.delete("Caches", "GcCode='" + "GC2T9RW" + "'", null);
			// Logs
			Logger.DEBUG("Delete Logs");
			LogDAO logdao = new LogDAO();
			logdao.ClearOrphanedLogs();
			logdao = null;

			ImportHandler importHandler = new ImportHandler();
			Database.Data.beginTransaction();
			GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/GC2T9RW.gpx"));
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);
			Database.Data.setTransactionSuccessful();
			Database.Data.endTransaction();
		}

		CacheTest.test_assertCache_GC2T9RW_with_details(false);

		ArrayList<String> allGeocodesIn = new ArrayList<String>();
		allGeocodesIn.add("GC2T9RW");
		try
		{
			final GpxSerializer ser = new GpxSerializer();
			// final FileWriter writer = new FileWriter(exportFile);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile), "UTF-8"));

			ser.writeGPX(allGeocodesIn, writer, new ProgressListener()
			{

				@Override
				public void publishProgress(int countExported)
				{

				}
			});
		}
		catch (IOException e)
		{

		}

		// File must created
		assertTrue("ExportFile must created @ " + exportFile.getAbsolutePath(), exportFile.exists());

		// Delete Cache from DB and import the created GPX
		{
			Database.Data.delete("Caches", "GcCode='" + "GC2T9RW" + "'", null);
			// Logs
			Logger.DEBUG("Delete Logs");
			LogDAO logdao = new LogDAO();
			logdao.ClearOrphanedLogs();
			logdao = null;

			ImportHandler importHandler = new ImportHandler();
			Database.Data.beginTransaction();
			GPXFileImporter importer = new GPXFileImporter(exportFile);
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);
			Database.Data.setTransactionSuccessful();
			Database.Data.endTransaction();

			CacheTest.test_assertCache_GC2T9RW_with_details(true);

		}
	}

	@Test
	public void testMultiExport() throws ClassNotFoundException, FileNotFoundException
	{

		InitTestDBs.InitalConfig();
		// initialize Database
		String database = "./testdata/test.db3";
		InitTestDBs.InitTestDB(database);

		String exportPath = "./testdata/gpx/ExportTestAll_GC2T9RW.gpx";
		File exportFile = new File(exportPath);

		// Delete File if exist
		if (exportFile.exists()) exportFile.delete();

		CacheListDAO DAO = new CacheListDAO();
		Database.Data.Query = DAO.ReadCacheList(Database.Data.Query, "", false, false);

		// Export all Caches from DB
		ArrayList<String> allGeocodesForExport = Database.Data.Query.getGcCodes();

		final int count = allGeocodesForExport.size();

		try
		{
			final GpxSerializer ser = new GpxSerializer();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile), "UTF-8"));

			ser.writeGPX(allGeocodesForExport, writer, new ProgressListener()
			{

				@Override
				public void publishProgress(int countExported)
				{
					System.out.print("Export:" + countExported + "/" + count + Global.br);
				}
			});
		}
		catch (IOException e)
		{

		}

		// File must created
		assertTrue("ExportFile must created @ " + exportFile.getAbsolutePath(), exportFile.exists());

		// Check TAG counts
		final int mustWptCount = 551;
		final int mustLogCount = 2139;

		int WptCount = 0;
		int LogCount = 0;

		Scanner sc = new Scanner(exportFile);
		sc.useDelimiter("(<wpt|<groundspeak:log )");
		while (sc.hasNext())
		{
			String next = sc.next();

			if (next.startsWith(" lat="))
			{
				WptCount++;
				continue;
			}

			if (next.startsWith("id="))
			{
				LogCount++;
				continue;
			}
		}

		assertTrue("Exportet Waypoint count must be " + mustWptCount + " but was " + WptCount, mustWptCount == WptCount);
		assertTrue("Exportet Logs count must be " + mustLogCount + " but was " + LogCount, mustLogCount == LogCount);

	}

}
