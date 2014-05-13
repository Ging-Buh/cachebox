package API;

import java.util.ArrayList;

import __Static.InitTestDBs;
import junit.framework.TestCase;
import CB_Core.Api.ApiGroundspeak_SearchForGeocaches;
import CB_Core.Api.SearchLiveMap;
import CB_Core.Types.Cache;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_UI.Config;

public class searchLiveMapTests extends TestCase
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

	public void testSearchLive()
	{

		Coordinate searchCoord = new CoordinateGPS(52.581892, 13.398128);

		ArrayList<Cache> apiCaches = new ArrayList<Cache>();
		SearchLiveMap searchC = new SearchLiveMap(2, searchCoord, 5000);

		ApiGroundspeak_SearchForGeocaches apis = new ApiGroundspeak_SearchForGeocaches(searchC, apiCaches);
		apis.execute();

		assertFalse("Keine Caches gefunden", apiCaches.size() < 1);

	}
}
