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
		String key = Config.GetStringEncrypted("GcAPI");
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", key.equals(""));
	}

	public void testSearchCache()
	{
		String accessToken = Config.GetStringEncrypted("GcAPI");

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

		int i = 0;
	}

}
