package API;

import java.util.ArrayList;

import CB_Core.CacheTypes;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.SearchCoordinate;
import CB_Core.Api.SearchForGeocaches_Core;
import CB_Core.Api.SearchGC;
import CB_Core.Api.SearchGCName;
import CB_Core.Api.SearchGCOwner;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_UI.Config;
import CB_Utils.Lists.CB_List;
import __Static.InitTestDBs;
import junit.framework.TestCase;

/**
 * Enth�lt die Tests um die Caches zu einer bestimmten Position �ber die API abzufragen
 * 
 * @author Longri
 */
public class searchForGeoCache_Test extends TestCase {

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

		Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home
		// of
		// Katipa(like
		// Longri)

		CB_List<Cache> apiCaches = new CB_List<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
		SearchCoordinate searchC = new SearchCoordinate(50, searchCoord, 500000);

		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0, null);

		assertFalse("Keine Caches gefunden", apiCaches.size() < 1);

	}

	public void testSearchGcCode() {
		SearchGC searchC = new SearchGC("GC1T33T");

		searchC.number = 1;

		CB_List<Cache> apiCaches = new CB_List<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0, null);

		boolean Assert = false;
		if (apiCaches != null && apiCaches.size() == 1 && apiCaches.get(0).getGcCode().equalsIgnoreCase("GC1T33T")) {
			Assert = true;
		}

		assertTrue("Nicht den Richtigen Cache gefunden", Assert);

	}

	public void testSearchName() {

		Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home of Katipa(like Longri)

		SearchGCName searchC = new SearchGCName(30, searchCoord, 50000, "Kleiner Multi - Quer durch die Heide");

		CB_List<Cache> apiCaches = new CB_List<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0, null);

		boolean Assert = false;
		if (apiCaches != null && apiCaches.size() == 1 && apiCaches.get(0).getGcCode().equalsIgnoreCase("GC4AA8H")) {
			Assert = true;
		}

		assertTrue("Nicht den Richtigen Cache gefunden", Assert);
	}

	public void testSearchByOwner() {

		Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128); // Home of Katipa(like Longri)

		SearchGCOwner searchC = new SearchGCOwner(30, searchCoord, 50000, "bros");

		CB_List<Cache> apiCaches = new CB_List<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		// String result =
		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0, null);

		boolean Assert = false;
		if (apiCaches != null && apiCaches.size() > 0) {

			for (int i = 0; i < apiCaches.size(); i++) {
				if (apiCaches.get(i).getGcCode().equalsIgnoreCase("GC2JT2F"))
					Assert = true;
			}

		}

		assertTrue("Nicht den Richtigen Cache gefunden", Assert);
	}

	public void testChkState() {
		ArrayList<Cache> chkList = new ArrayList<Cache>();

		Cache c = new Cache(0.0, 0.0, "", CacheTypes.Traditional, "GC2JT2F");

		chkList.add(c);

		// int result =
		GroundspeakAPI.GetGeocacheStatus(chkList, null);

		boolean Assert = false;

		boolean changedState = false;
		Cache cacheNew = null;
		for (Cache cache : chkList) {
			changedState = cache.isAvailable();
			cache.setAvailable(!changedState);
			cacheNew = cache;
			break;
		}

		chkList.clear();
		chkList.add(cacheNew);

		// result =
		GroundspeakAPI.GetGeocacheStatus(chkList, null);

		for (Cache cache : chkList) {
			if (changedState == cache.isAvailable())
				Assert = true;
		}

		assertTrue("Nicht richtig aktualisiert", Assert);
	}

	public void test_searchLite() {
		SearchGC searchC = new SearchGC("GC1T33T");

		searchC.number = 1;
		searchC.setIsLite(true);

		CB_List<Cache> apiCaches = new CB_List<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0, null);

		boolean Assert = false;
		if (apiCaches != null && apiCaches.size() == 1 && apiCaches.get(0).getGcCode().equalsIgnoreCase("GC1T33T")) {
			Assert = true;
		}

		assertTrue("Nicht den Richtigen Cache gefunden", Assert);

		Cache ca = apiCaches.get(0);

		assertEquals("Date must be", ca.detail.DateHidden.toString(), "Sun May 31 09:00:00 CEST 2009");

	}

}
