package CB_Core.CB_Core.Api;

import java.util.ArrayList;

import CB_Core.CacheTypes;
import CB_Core.Api.SearchForGeocaches_Core;
import CB_Core.Api.SearchGC;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_UI.Config;
import CB_Utils.Lists.CB_List;
import __Static.InitTestDBs;
import junit.framework.TestCase;

public class Bug384 extends TestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		LoadConfig();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

	}

	/**
	 * l�dt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine g�ltige cachebox.config Datei liegen. Diese Datei
	 * ist auf der Ignore list von SVN, so das diese Pers�nliche config nicht ver�ffentlicht werden kann. (zum Schutz des Pers�nlichen API
	 * Keys)
	 */
	private void LoadConfig() {
		InitTestDBs.InitalConfig();
		String key = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testSearchCache() {
		SearchGC searchC = new SearchGC("GC166HV");

		searchC.number = 1;

		CB_List<Cache> apiCaches = new CB_List<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		// String result =
		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0, null);

		Cache c = apiCaches.get(0);
		Waypoint w = c.waypoints.get(0);
		assertTrue("Falsche WP.Type zuordnung", w.Type == CacheTypes.ReferencePoint);

	}

}
