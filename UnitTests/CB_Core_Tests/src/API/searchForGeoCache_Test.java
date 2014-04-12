package API;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import CB_Core.InitTestDBs;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.SearchForGeocaches_Core;
import CB_Core.Api.SearchForGeocaches_Core.SearchCoordinate;
import CB_Core.Api.SearchForGeocaches_Core.SearchGC;
import CB_Core.Api.SearchForGeocaches_Core.SearchGCName;
import CB_Core.Api.SearchForGeocaches_Core.SearchGCOwner;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Locator.Coordinate;
import CB_UI.Config;

/**
 * Enthält die Tests um die Caches zu einer bestimmten Position über die API abzufragen
 * 
 * @author Longri
 */
public class searchForGeoCache_Test extends TestCase
{

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		LoadConfig();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();

	}

	/**
	 * lädt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine gültige cachebox.config Datei liegen. Diese Datei
	 * ist auf der Ignore list von SVN, so das diese Persönliche config nicht veröffentlicht werden kann. (zum Schutz des Persönlichen API
	 * Keys)
	 */
	private void LoadConfig()
	{
		InitTestDBs.InitalConfig();
		String key = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testSearchCache()
	{
		String accessToken = Config.GetAccessToken();

		Coordinate searchCoord = new Coordinate(52.581892, 13.398128); // Home
																		// of
																		// Katipa(like
																		// Longri)

		ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
		SearchCoordinate searchC = new SearchCoordinate();
		searchC.pos = searchCoord;
		searchC.distanceInMeters = 500000;
		searchC.number = 50;

		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0);

		assertFalse("Keine Caches gefunden", apiCaches.size() < 1);

	}

	public void testSearchGcCode()
	{
		String accessToken = Config.GetAccessToken();

		SearchGC searchC = new SearchGC();
		searchC.gcCode = "GC2TZPJ";

		searchC.number = 1;

		ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0);

		boolean Assert = false;
		if (apiCaches != null && apiCaches.size() == 1 && apiCaches.get(0).GcCode.equalsIgnoreCase("GC2TZPJ"))
		{
			Assert = true;
		}

		assertTrue("Nicht den Richtigen Cache gefunden", Assert);

	}

	public void testSearchName()
	{
		String accessToken = Config.GetAccessToken();

		Coordinate searchCoord = new Coordinate(52.581892, 13.398128); // Home
																		// of
																		// Katipa(like
																		// Longri)

		SearchGCName searchC = new SearchGCName();
		searchC.gcName = "Kleiner Multi - Quer durch die Heide";
		searchC.number = 30;
		searchC.pos = searchCoord;

		ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0);

		boolean Assert = false;
		if (apiCaches != null && apiCaches.size() == 1 && apiCaches.get(0).GcCode.equalsIgnoreCase("GC4AA8H"))
		{
			Assert = true;
		}

		assertTrue("Nicht den Richtigen Cache gefunden", Assert);
	}

	public void testSearchByOwner()
	{
		String accessToken = Config.GetAccessToken();
		Coordinate searchCoord = new Coordinate(52.581892, 13.398128); // Home
		// of
		// Katipa(like
		// Longri)

		SearchGCOwner searchC = new SearchGCOwner();
		searchC.OwnerName = "bros";
		// searchC.OwnerName = "nimaci2001";
		searchC.number = 30;
		searchC.pos = searchCoord;

		ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		// String result =
		CB_Core.Api.SearchForGeocaches_Core t = new SearchForGeocaches_Core();
		t.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, 0);

		boolean Assert = false;
		if (apiCaches != null && apiCaches.size() > 0)
		{

			Iterator<Cache> iterator = apiCaches.iterator();

			do
			{
				if (iterator.next().GcCode.equalsIgnoreCase("GC2JT2F")) Assert = true;
			}
			while (iterator.hasNext());

		}

		assertTrue("Nicht den Richtigen Cache gefunden", Assert);
	}

	public void testChkState()
	{
		String accessToken = Config.GetAccessToken();

		ArrayList<Cache> chkList = new ArrayList<Cache>();

		Cache c = new Cache(0.0, 0.0, "", CacheTypes.Traditional, "GC2JT2F");

		chkList.add(c);

		// int result =
		GroundspeakAPI.GetGeocacheStatus(chkList);

		boolean Assert = false;

		boolean changedState = false;
		Cache cacheNew = null;
		for (Cache cache : chkList)
		{
			changedState = cache.Available;
			cache.Available = !changedState;
			cacheNew = cache;
			break;
		}

		chkList.clear();
		chkList.add(cacheNew);

		// result =
		GroundspeakAPI.GetGeocacheStatus(chkList);

		for (Cache cache : chkList)
		{
			if (changedState == cache.Available) Assert = true;
		}

		assertTrue("Nicht richtig aktualisiert", Assert);
	}

}
