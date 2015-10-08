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
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Export.GpxSerializer;
import CB_Core.Export.GpxSerializer.ProgressListener;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.ImportHandler;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_UI_Base.Global;
import Types.CacheTest;
import __Static.InitTestDBs;
import junit.framework.TestCase;

/**
 * Test the GPX Export
 * 
 * @author Longri
 */
public class GPX_Export extends TestCase {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GPX_Export.class);

    @Test
    public void testSingleExport() throws Exception {
	InitTestDBs.InitalConfig();
	// initialize Database
	String database = "./testdata/test.db3";
	InitTestDBs.InitTestDB(database);

	String exportPath = "./testdata/gpx/ExportTest_GC2T9RW.gpx";
	File exportFile = new File(exportPath);

	// Delete File if exist
	if (exportFile.exists())
	    exportFile.delete();

	// Delete Cache from DB end import from Real-GPX
	{
	    Database.Data.delete("Caches", "GcCode='" + "GC2T9RW" + "'", null);
	    // Logs
	    log.debug("Delete Logs");
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

	CacheTest.assertCache_GC2T9RW_with_details(true);

	{ // Set Notes,Solver and Waypoint.Clue
	    CacheDAO cacheDAO = new CacheDAO();

	    Cache cache = cacheDAO.getFromDbByGcCode("GC2T9RW", false);
	    cache.loadDetail();

	    Database.Data.SetNote(cache, "Test Note for In/Ex-port");
	    Database.Data.SetSolver(cache, "Test Solver for In/Ex-port");

	    cache.waypoints.get(0).setClue("Test Clue for In/Ex-port");

	    WaypointDAO wpDao = new WaypointDAO();
	    wpDao.UpdateDatabase(cache.waypoints.get(0));

	}

	ArrayList<String> allGeocodesIn = new ArrayList<String>();
	allGeocodesIn.add("GC2T9RW");
	try {
	    final GpxSerializer ser = new GpxSerializer();
	    // final FileWriter writer = new FileWriter(exportFile);
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile), "UTF-8"));

	    ser.writeGPX(allGeocodesIn, writer, new ProgressListener() {

		@Override
		public void publishProgress(int countExported, String msg) {

		}
	    });
	} catch (IOException e) {

	}

	// File must created
	assertTrue("ExportFile must created @ " + exportFile.getAbsolutePath(), exportFile.exists());

	// Delete Cache from DB and import the created GPX
	{
	    Database.Data.delete("Caches", "GcCode='" + "GC2T9RW" + "'", null);
	    // Logs
	    log.debug("Delete Logs");
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

	    CacheTest.assertCache_GC2T9RW_with_detailsAndChangedNote(true);

	}
    }

    @Test
    public void testMultiExport() throws ClassNotFoundException, FileNotFoundException {

	InitTestDBs.InitalConfig();
	// initialize Database
	String database = "./testdata/test.db3";
	InitTestDBs.InitTestDB(database);

	String exportPath = "./testdata/gpx/ExportTestAll_GC2T9RW.gpx";
	File exportFile = new File(exportPath);

	// Delete File if exist
	if (exportFile.exists())
	    exportFile.delete();

	CacheListDAO DAO = new CacheListDAO();
	Database.Data.Query = DAO.ReadCacheList(Database.Data.Query, "", false, false);

	// Export all Caches from DB
	ArrayList<String> allGeocodesForExport = Database.Data.Query.getGcCodes();

	final int count = allGeocodesForExport.size();

	try {
	    final GpxSerializer ser = new GpxSerializer();
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile), "UTF-8"));

	    ser.writeGPX(allGeocodesForExport, writer, new ProgressListener() {

		@Override
		public void publishProgress(int countExported, String msg) {
		    System.out.print("Export:" + countExported + "/" + count + Global.br);
		}
	    });
	} catch (IOException e) {

	}

	// File must created
	assertTrue("ExportFile must created @ " + exportFile.getAbsolutePath(), exportFile.exists());

	// Check TAG counts
	final int mustWptCount = 548;// 549;
	final int mustLogCount = 2138;// 2134;

	int WptCount = 0;
	int LogCount = 0;

	Scanner sc = new Scanner(exportFile);
	sc.useDelimiter("(<wpt|<groundspeak:log )");
	while (sc.hasNext()) {
	    String next = sc.next();

	    if (next.startsWith(" lat=")) {
		WptCount++;
		continue;
	    }

	    if (next.startsWith("id=")) {
		LogCount++;
		continue;
	    }
	}

	sc.close();
	sc = null;

	assertTrue("Exportet Waypoint count must be " + mustWptCount + " but was " + WptCount, mustWptCount == WptCount);
	assertTrue("Exportet Logs count must be " + mustLogCount + " but was " + LogCount, mustLogCount == LogCount);

    }

    @Test
    public void testExportOwnCacheWithWP() throws Exception {

	InitTestDBs.InitalConfig();
	// initialize Database
	String database = "./testdata/test.db3";
	InitTestDBs.InitTestDB(database);

	String exportPath = "./testdata/gpx/ExportTestOwnWithWP.gpx";
	File exportFile = new File(exportPath);

	// Delete File if exist
	if (exportFile.exists())
	    exportFile.delete();

	CacheListDAO DAO = new CacheListDAO();
	Database.Data.Query = DAO.ReadCacheList(Database.Data.Query, "", false, false);

	// create own Cache
	Cache newCache = new Cache(true);
	newCache.Type = CacheTypes.Traditional;
	newCache.Size = CacheSizes.micro;
	newCache.setDifficulty(1);
	newCache.setTerrain(1);
	newCache.Pos = new Coordinate("52° 33,355N / 13° 24,873E");
	// GC - Code bestimmen für freies CWxxxx = CustomWaypint
	String prefix = "CW";
	int count = 0;
	do {
	    count++;
	    newCache.setGcCode(prefix + String.format("%04d", count));
	} while (Database.Data.Query.GetCacheById(Cache.GenerateCacheId(newCache.getGcCode())) != null);
	newCache.setName(newCache.getGcCode());
	newCache.setOwner("Unbekannt");
	newCache.setDateHidden(new Date());
	newCache.setArchived(false);
	newCache.setAvailable(true);
	newCache.setFound(false);
	newCache.NumTravelbugs = 0;
	newCache.setShortDescription("");
	newCache.setLongDescription("");

	String newGcCode = "";
	try {
	    newGcCode = Database.Data.CreateFreeGcCode(newCache.getGcCode());
	} catch (Exception e) {
	    return;
	}
	Coordinate coord = new Coordinate("52° 33,301N / 13° 24,873E");
	Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), newCache.Id, "", "wyptDefTitle");

	newCache.waypoints.add(newWP);

	CacheDAO cDAO = new CacheDAO();
	cDAO.WriteToDatabase(newCache);

	WaypointDAO wDAO = new WaypointDAO();
	wDAO.WriteToDatabase(newWP);

	// Export all Caches from DB
	ArrayList<String> allGeocodesForExport = new ArrayList<String>();
	allGeocodesForExport.add(newCache.getGcCode());

	try {
	    final GpxSerializer ser = new GpxSerializer();
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile), "UTF-8"));

	    ser.writeGPX(allGeocodesForExport, writer, new ProgressListener() {

		@Override
		public void publishProgress(int countExported, String msg) {

		}
	    });
	} catch (IOException e) {

	}

	// File must created
	assertTrue("ExportFile must created @ " + exportFile.getAbsolutePath(), exportFile.exists());

	// Delete Cache from DB and import the created GPX
	{
	    Database.Data.delete("Caches", "GcCode='" + newCache.getGcCode() + "'", null);
	    // Logs
	    log.debug("Delete Logs");
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

	    InitTestDBs.InitalConfig();

	    CacheDAO cacheDAO = new CacheDAO();

	    Cache cache = cacheDAO.getFromDbByGcCode(newCache.getGcCode(), true);

	    assertTrue("Cache muss zurückgegeben werden", cache != null);

	    assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 52.555916);
	    assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 13.41455);
	    assertTrue("Pos ist ungültig", cache.Pos.isValid());

	    assertEquals("GcCode falsch", newCache.getGcCode(), cache.getGcCode());
	    assertEquals("DateHidden falsch", newCache.getDateHidden().toString(), cache.getDateHidden().toString());

	    assertTrue("Found ist falsch", !cache.isFound());

	    assertEquals("Id ist falsch", newCache.getGcId(), cache.getGcId());
	    assertTrue("ist available ist falsch", cache.isAvailable());
	    assertFalse("ist archived ist falsch", cache.isArchived());
	    assertEquals("Name falsch", newCache.getName(), cache.getName());

	    assertTrue("Typ ist falsch", cache.Type == CacheTypes.Traditional);
	    assertTrue("Size ist falsch", cache.Size == CacheSizes.micro);
	    assertTrue("Difficulty ist falsch", cache.getDifficulty() == 1);
	    assertTrue("Terrain ist falsch", cache.getTerrain() == 1);

	    // Attribute Tests

	    ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
	    ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();

	    Iterator<Attributes> positiveInterator = PositvieList.iterator();
	    Iterator<Attributes> negativeInterator = NegativeList.iterator();

	    // fülle eine Liste mit allen Attributen
	    ArrayList<Attributes> attributes = new ArrayList<Attributes>();
	    Attributes[] tmp = Attributes.values();
	    for (Attributes item : tmp) {
		attributes.add(item);
	    }

	    // Lösche die vergebenen Atribute aus der Kommplett Liste
	    positiveInterator = PositvieList.iterator();
	    negativeInterator = NegativeList.iterator();

	    while (positiveInterator.hasNext()) {
		attributes.remove(positiveInterator.next());
	    }

	    while (negativeInterator.hasNext()) {
		attributes.remove(negativeInterator.next());
	    }

	    // Teste ob die übrig gebliebenen Atributte auch nicht vergeben wurden.
	    Iterator<Attributes> RestInterator = attributes.iterator();

	    while (RestInterator.hasNext()) {
		Attributes attr = (Attributes) RestInterator.next();
		assertFalse(attr.toString() + " Attribut falsch", cache.isAttributePositiveSet(attr));
		assertFalse(attr.toString() + " Attribut falsch", cache.isAttributeNegativeSet(attr));
	    }

	    assertEquals("shortDescription must be empty", "", cache.getShortDescription());

	    assertEquals("longDescription must be empty", "", cache.getLongDescription());

	    // Check exportetd Waypoint with Parent

	    cache.deleteDetail(false);
	    cache.loadDetail();

	    assertEquals("Anzahl der WyPoints muss gleich sein", 1, cache.waypoints.size());

	    Waypoint wp = cache.waypoints.get(0);
	    assertTrue("WpPos: Latitude falsch", wp.Pos.getLatitude() == 52.555016);
	    assertTrue("WpPos: Longitude falsch", wp.Pos.getLongitude() == 13.41455);
	    assertTrue("WpPos ist ungï¿½ltig", wp.Pos.isValid());
	    assertEquals("Titel muss gleich sein", "wyptDefTitle", wp.getTitle());
	    assertEquals("Description muss gleich sein", "", wp.getDescription());
	    assertEquals("parent CacheID muss gleich sein", cache.Id, wp.CacheId);
	    assertEquals("WP-Type muss gleich sein", CacheTypes.ReferencePoint, wp.Type);

	}

    }
}
