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
package CB_Core.CB_Core.Import;

import java.io.File;

import org.slf4j.LoggerFactory;

import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DB.Database;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.ImportHandler;
import CB_Core.Types.Cache;
import Types.CacheTest;
import __Static.InitTestDBs;
import junit.framework.TestCase;

/**
 * Test the GPX Import
 * 
 * @author saarfuchs
 * @author Longri
 */
public class GpxImportTest extends TestCase {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GpxImportTest.class);

    public static void testGpxImport() throws Exception {

	InitTestDBs.InitalConfig();

	// initialize Database
	String database = "./testdata/test.db3";
	InitTestDBs.InitTestDB(database);

	// First must delete DB entry from last TestRun
	{
	    Database.Data.db.delete("Caches", "GcCode='" + "GC2T9RW" + "'", null);
	    // Logs
	    log.debug("Delete Logs");
	    LogDAO logdao = new LogDAO();
	    logdao.ClearOrphanedLogs();
	    logdao = null;
	}

	ImportHandler importHandler = new ImportHandler();

	Database.Data.db.beginTransaction();

	try {
	    GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/GC2T9RW.gpx"));
	    assertTrue("Objekt muss konstruierbar sein", importer != null);
	    importer.doImport(importHandler, 0);

	    Database.Data.db.setTransactionSuccessful();
	} finally {
	}

	Database.Data.db.endTransaction();

	CacheTest.assertCache_GC2T9RW_with_details(true);
    }

    public static void testGpxImportShortDesc() throws Exception {
	// issue # 999 => http://mantis.team-cachebox.de/view.php?id=999

	InitTestDBs.InitalConfig();

	// initialize Database
	String database = "./testdata/test.db3";
	InitTestDBs.InitTestDB(database);

	// First must delete DB entry from last TestRun
	{
	    Database.Data.db.delete("Caches", "GcCode='" + "GC52BKF" + "'", null);
	    // Logs
	    log.debug("Delete Logs");
	    LogDAO logdao = new LogDAO();
	    logdao.ClearOrphanedLogs();
	    logdao = null;
	}

	ImportHandler importHandler = new ImportHandler();

	Database.Data.db.beginTransaction();

	try {
	    GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/GC52BKF.gpx"));
	    assertTrue("Objekt muss konstruierbar sein", importer != null);
	    importer.doImport(importHandler, 0);

	    Database.Data.db.setTransactionSuccessful();
	} finally {
	}

	Database.Data.db.endTransaction();

	CacheDAO cacheDAO = new CacheDAO();
	Cache cache = cacheDAO.getFromDbByGcCode("GC52BKF", true);

	assertEquals("", cache.getLongDescription());

	String sd = "<p>Drive In. Eine nette Zusatzeule. Bewohner ist informiert. Dennoch oft �muggelig. Das Grundst�ck muss nicht betreten werden!�<img alt=\"enlightened\" src=\"http://www.geocaching.com/static/js/CKEditor/4.1.2/plugins/smiley/images/lightbulb.gif\" title=\"enlightened\" style=\"height:20px;width:20px;\" /></p>";
	assertEquals(sd, cache.getShortDescription());

    }

}
