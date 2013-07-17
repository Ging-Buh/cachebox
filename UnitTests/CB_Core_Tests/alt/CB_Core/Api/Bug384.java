package CB_Core.Api;

import java.util.ArrayList;

import junit.framework.TestCase;
import CB_Core.Config;
import CB_Core.InitTestDBs;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public class Bug384 extends TestCase
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
	 * l�dt die Config Datei aus dem Ordner "trunk\Cachebox Core\testdata" Hie muss eine g�ltige cachebox.config Datei liegen. Diese Datei
	 * ist auf der Ignore list von SVN, so das diese Pers�nliche config nicht ver�ffentlicht werden kann. (zum Schutz des Pers�nlichen API
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

		CB_Core.Api.SearchForGeocaches.SearchGC searchC = new CB_Core.Api.SearchForGeocaches.SearchGC();
		searchC.gcCode = "GC166HV";

		searchC.number = 1;

		ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
		ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

		// String result =
		CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, apiImages, 0);

		Cache c = apiCaches.get(0);
		Waypoint w = c.waypoints.get(0);
		assertTrue("Falsche WP.Type zuordnung", w.Type == CacheTypes.ReferencePoint);

	}

}