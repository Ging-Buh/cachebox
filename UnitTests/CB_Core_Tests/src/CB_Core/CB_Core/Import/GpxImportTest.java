package CB_Core.CB_Core.Import;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import CB_Core.InitTestDBs;
import CB_Core.DAO.CacheDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.ImportHandler;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Utils.Lists.CB_List;

public class GpxImportTest extends TestCase
{

	public static void testGpxImport() throws Exception
	{

		InitTestDBs.InitalConfig();

		// initialize Database
		String database = "./testdata/test.db3";
		InitTestDBs.InitTestDB(database);

		ImportHandler importHandler = new ImportHandler();

		Database.Data.beginTransaction();

		try
		{
			GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/GC2T9RW.gpx"));
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);

			Database.Data.setTransactionSuccessful();
		}
		finally
		{
		}

		Database.Data.endTransaction();

		CacheDAO cacheDAO = new CacheDAO();

		Cache cache = cacheDAO.getFromDbByGcCode("GC2T9RW");

		assertTrue("Cache muss zurückgegeben werden", cache != null);

		assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 49.349817);
		assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 8.62925);
		assertTrue("Pos ist ungültig", cache.Pos.isValid());

		assertEquals("GcCode falsch", "GC2T9RW", cache.getGcCode());
		assertEquals("DateHidden falsch", "Sat Apr 16 07:00:00 CEST 2011", cache.getDateHidden().toString());
		assertEquals("url falsch", "http://www.geocaching.com/seek/cache_details.aspx?guid=f26f18bd-9aaa-4499-944b-3e8cb62e41a7",
				cache.getUrl());
		assertTrue("Found ist falsch", cache.isFound());

		assertEquals("Id ist falsch", cache.getGcId(), "2190117");
		assertTrue("ist available ist falsch", cache.isAvailable());
		assertFalse("ist archived ist falsch", cache.isArchived());
		assertEquals("Name falsch", "der Hampir - T5 -", cache.getName());
		assertEquals("Placed by falsch", "Team Rabbits", cache.getPlacedBy());
		assertEquals("Owner falsch", "Team Rabbits", cache.getOwner());
		assertTrue("Typ ist falsch", cache.Type == CacheTypes.Traditional);
		assertTrue("Size ist falsch", cache.Size == CacheSizes.small);
		assertTrue("Difficulty ist falsch", cache.Difficulty == 2);
		assertTrue("Terrain ist falsch", cache.Terrain == 5);

		assertTrue("Attribut falsch", cache.isAttributePositiveSet(Attributes.Bicycles));
		assertFalse("Attribut falsch", cache.isAttributeNegativeSet(Attributes.Bicycles));
		assertFalse("Attribut falsch", cache.isAttributePositiveSet(Attributes.Boat));
		// Attribute Tests

		ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
		ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();

		PositvieList.add(Attributes.Bicycles);
		PositvieList.add(Attributes.Dogs);
		PositvieList.add(Attributes.Available_at_all_times);
		PositvieList.add(Attributes.Public_restrooms_nearby);
		PositvieList.add(Attributes.Parking_available);
		PositvieList.add(Attributes.Fuel_Nearby);
		PositvieList.add(Attributes.Short_hike);
		PositvieList.add(Attributes.Climbing_gear);
		PositvieList.add(Attributes.Ticks);
		PositvieList.add(Attributes.Hunting);

		Iterator<Attributes> positiveInterator = PositvieList.iterator();
		Iterator<Attributes> negativeInterator = NegativeList.iterator();

		while (positiveInterator.hasNext())
		{
			Attributes attr = (Attributes) positiveInterator.next();
			assertTrue(attr.toString() + " Attribut falsch", cache.isAttributePositiveSet(attr));
		}

		while (negativeInterator.hasNext())
		{
			Attributes attr = (Attributes) negativeInterator.next();
			assertTrue(attr.toString() + " Attribut falsch", cache.isAttributeNegativeSet(attr));
		}

		// fülle eine Liste mit allen Attributen
		ArrayList<Attributes> attributes = new ArrayList<Attributes>();
		Attributes[] tmp = Attributes.values();
		for (Attributes item : tmp)
		{
			attributes.add(item);
		}

		// Lösche die vergebenen Atribute aus der Kommplett Liste
		positiveInterator = PositvieList.iterator();
		negativeInterator = NegativeList.iterator();

		while (positiveInterator.hasNext())
		{
			attributes.remove(positiveInterator.next());
		}

		while (negativeInterator.hasNext())
		{
			attributes.remove(negativeInterator.next());
		}

		attributes.remove(Attributes.getAttributeEnumByGcComId(64));
		attributes.remove(Attributes.getAttributeEnumByGcComId(65));
		attributes.remove(Attributes.getAttributeEnumByGcComId(66));

		// Teste ob die Übrig gebliebenen Atributte auch nicht vergeben wurden.
		Iterator<Attributes> RestInterator = attributes.iterator();

		while (RestInterator.hasNext())
		{
			Attributes attr = (Attributes) RestInterator.next();
			assertFalse(attr.toString() + " Attribut falsch", cache.isAttributePositiveSet(attr));
			assertFalse(attr.toString() + " Attribut falsch", cache.isAttributeNegativeSet(attr));
		}

		// TODO Beschreibungstexte überprüfen
		// System.out.println( cache.shortDescription );
		// System.out.println( cache.longDescription );

		assertEquals("Hint falsch", "wenn du ihn nicht findest, findet er dich!!", cache.getHint());

		CB_List<LogEntry> logs = new CB_List<LogEntry>();
		logs = Database.Logs(cache);

		LogEntry log = logs.get(0);

		assertEquals("CacheId ist falsch", log.CacheId, 24578729153020743L);
		assertEquals("Id ist falsch", log.Id, 170855167);
		assertEquals("Timestamp falsch", "Mon Jul 04 19:00:00 CEST 2011", log.Timestamp.toString());
		assertEquals("Finder falsch", "SaarFuchs", log.Finder);
		assertTrue("LogTyp falsch", log.Type == LogTypes.found);

		// TODO Beschreibungstexte überprüfen
		// System.out.println( log.Comment );

		// Database.Data.Close();
	}

}
