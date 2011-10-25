package CB_Core.Api;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DAO.CategoryDAO;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Descriptor.PointD;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.Coordinate;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.LogEntry;
import CB_Core.Types.MeasuredCoordList;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;

import junit.framework.TestCase;

/**
 * Enthält die Tests um die Caches zu einer bestimmten Position über die API
 * abzufragen
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
	 * lädt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie
	 * muss eine gültige cachebox.config Datei liegen. Diese Datei ist auf der
	 * Ignore list von SVN, so das diese Persönliche config nicht veröffentlicht
	 * werden kann. (zum Schutz des Persönlichen API Keys)
	 */
	private void LoadConfig()
	{
		Config.Initialize("./testdata/", "./testdata/cachebox.config");
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
		CB_Core.Api.SearchForGeocaches.SearchCoordinate searchC = new CB_Core.Api.SearchForGeocaches.SearchCoordinate();
		searchC.pos = searchCoord;
		searchC.distanceInMeters = 50000;
		searchC.number = 50;
		String result = CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, 0);

		assertFalse("Keine Caches gefunden", apiCaches.size() < 1);

	}

	public void testSearchGcCode()
	{
		String accessToken = Config.GetAccessToken();

		CB_Core.Api.SearchForGeocaches.SearchGC searchC = new CB_Core.Api.SearchForGeocaches.SearchGC();
		searchC.gcCode = "GC30NZN";
		
		searchC.number = 1;

		ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();

		String result = CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, 0);

		boolean Assert = false;
		if (apiCaches != null && apiCaches.size() == 1 && apiCaches.get(0).GcCode.equalsIgnoreCase("GC30NZN"))
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

		CB_Core.Api.SearchForGeocaches.SearchGCName searchC = new CB_Core.Api.SearchForGeocaches.SearchGCName();
		searchC.gcName = "c40";
		searchC.number = 30;
		searchC.pos = searchCoord;

		ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();

		String result = CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, 0);

		boolean Assert = false;
		if (apiCaches != null && apiCaches.size() == 1 && apiCaches.get(0).GcCode.equalsIgnoreCase("GC2NFTY"))
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

		CB_Core.Api.SearchForGeocaches.SearchGCOwner searchC = new CB_Core.Api.SearchForGeocaches.SearchGCOwner();
		searchC.OwnerName = "bros";
//		searchC.OwnerName = "nimaci2001";
		searchC.number = 30;
		searchC.pos = searchCoord;

		ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();

		String result = CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, 0);

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

}
