package API;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.LiveMapQue;
import CB_Core.Types.Cache;
import CB_Locator.Coordinate;
import CB_Locator.Map.Descriptor;
import CB_UI.Config;
import CB_Utils.MathUtils.CalculationType;
import __Static.InitTestDBs;

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

		Descriptor.Init();

		// Descriptor Zoom Level 14 = search radius 2km
		// Center of Descriptor x=8801 y=5368 z=14 => 52° 34,982N / 13° 23,540E (Pankow)
		Descriptor descPankow = new Descriptor(8801, 5368, 14, false);
		Coordinate corPankow = new Coordinate("52° 34,9815N / 13° 23,540E");

		// List of Coordinates are into x=8801 y=5368 z=14
		Coordinate[] coordList = new Coordinate[]
			{ new Coordinate("52° 34,973N / 13° 23,531E"), new Coordinate("52° 35,364N / 13° 24,170E"),
					new Coordinate("52° 35,367N / 13° 22,908E"), new Coordinate("52° 34,601N / 13° 22,923E"),
					new Coordinate("52° 34,598N / 13° 24,170E"), new Coordinate("52° 34,773N / 13° 23,346E"),
					new Coordinate("52° 34,933N / 13° 23,938E") };

		// check
		for (Coordinate cor : coordList)
		{
			Descriptor desc = new Descriptor(cor, 14);
			assertEquals("mustEquals", desc, descPankow);

			// Check center coordinate of Descriptor
			Coordinate cord = desc.getCenterCoordinate();
			assertEquals("mustEquals", cord, corPankow);
		}
	}

	@Test
	public void testLiveQue()
	{
		// get API limits and Check the limits after request

		GroundspeakAPI.GetCacheLimits();
		int CachesLeft = GroundspeakAPI.CachesLeft;

		Descriptor.Init();
		Coordinate coord = new Coordinate("52° 34,9815N / 13° 23,540E");
		Descriptor desc = new Descriptor(coord, 14);

		assertTrue("muss ausgeführt werden", LiveMapQue.quePosition(new Coordinate("52° 34,9815N / 13° 23,540E")));
		assertFalse("darf nicht ausgeführt werden", LiveMapQue.quePosition(new Coordinate("52° 34,9815N / 13° 23,540E")));

		// Chk all Caches are in to the Descriptor of new Coordinate("52° 34,973N / 13° 23,531E")

		for (int i = 0; i < LiveMapQue.LiveCaches.size(); i++)
		{
			Cache ca = LiveMapQue.LiveCaches.get(i);

			Descriptor targetDesc = new Descriptor(ca.Pos, 14);

			if (!targetDesc.equals(desc))
			{
				// Check max Distance from Center
				float distance = coord.Distance(ca.Pos, CalculationType.ACCURATE);
				assertTrue("Distance from center must be closer then request distance", distance <= LiveMapQue.Used_max_request_radius);
			}

		}

		// Check if count are not same like requested (increase Max Count)
		assertTrue("count mast be lower then requested", LiveMapQue.LiveCaches.size() < LiveMapQue.MAX_REQUEST_CACHE_COUNT);

		GroundspeakAPI.GetCacheLimits();

		assertTrue("CacheLimits must not changed", CachesLeft == GroundspeakAPI.CachesLeft);

	}
}
