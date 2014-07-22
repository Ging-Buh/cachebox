package CB_Core.CB_Core.Import;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
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
import CB_Core.Types.Waypoint;
import CB_Utils.Lists.CB_List;
import __Static.InitTestDBs;

public class GSAKGpxImportTest extends TestCase
{

	public static void testGpxImport() throws Exception
	{

		// initialize Database
		InitTestDBs.InitalConfig();

		ImportHandler importHandler = new ImportHandler();

		Database.Data.beginTransaction();

		try
		{
			GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/CorrectedCoordinates1.1.gpx"));
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);

			Database.Data.setTransactionSuccessful();
		}
		finally
		{
		}

		Database.Data.endTransaction();

		CacheDAO cacheDAO = new CacheDAO();

		Cache cache = cacheDAO.getFromDbByGcCode("GC1XCEW", true, true);

		assertTrue("Cache muss zurückgegeben werden", cache != null);

		assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 52.579333);
		assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 13.40545);
		assertTrue("Pos ist ungültig", cache.Pos.isValid());

		assertEquals("GcCode falsch", "GC1XCEW", cache.getGcCode());
		assertEquals("DateHidden falsch", "Mon Aug 17 08:00:00 CEST 2009", cache.getDateHidden().toString());
		assertEquals("url falsch", "", cache.getUrl());// URL wird noch nicht
														// ausgelesen
		assertTrue("Found ist falsch", cache.isFound());

		assertEquals("Id ist falsch", cache.getGcId(), "1358542");
		assertFalse("ist available ist falsch", cache.isAvailable());
		assertTrue("ist archived ist falsch", cache.isArchived());
		assertEquals("Name falsch", "Schlossblick # 2/ View at the castle  #2", cache.getName());
		assertEquals("Placed by falsch", "Risou", cache.getPlacedBy());
		assertEquals("Owner falsch", "Risou", cache.getOwner());
		assertTrue("Typ ist falsch", cache.Type == CacheTypes.Mystery);
		assertTrue("Size ist falsch", cache.Size == CacheSizes.micro);
		assertTrue("Difficulty ist falsch", cache.getDifficulty() == 2);
		assertTrue("Terrain ist falsch", cache.getTerrain() == 2);

		// Attribute Tests

		ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
		ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();

		PositvieList.add(Attributes.Bicycles);
		PositvieList.add(Attributes.Dogs);
		PositvieList.add(Attributes.Ticks);
		PositvieList.add(Attributes.Thorns);
		PositvieList.add(Attributes.Takes_less_than_an_hour);

		NegativeList.add(Attributes.Available_at_all_times);
		NegativeList.add(Attributes.Recommended_at_night);

		Iterator<Attributes> positiveInterator = PositvieList.iterator();
		Iterator<Attributes> negativeInterator = NegativeList.iterator();

		while (positiveInterator.hasNext())
		{
			assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
		}

		while (negativeInterator.hasNext())
		{
			Attributes tmp = negativeInterator.next();

			assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
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
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributePositiveSet(attr));
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributeNegativeSet(attr));
		}
		//

		// TODO Beschreibungstexte überprüfen
		// System.out.println( cache.shortDescription );
		// System.out.println( cache.longDescription );

		assertEquals("Hint falsch", "", cache.getHint());

		CB_List<LogEntry> logs = new CB_List<LogEntry>();
		logs = Database.Logs(cache);

		LogEntry log = logs.get(0);

		assertEquals("CacheId ist falsch", log.CacheId, 24564478518575943L);
		assertEquals("Id ist falsch", log.Id, 140640156);
		assertEquals("Timestamp falsch", "Sat Jan 08 20:00:00 CET 2011", log.Timestamp.toString());
		assertEquals("Finder falsch", "Katipa", log.Finder);
		assertTrue("LogTyp falsch", log.Type == LogTypes.found);

		assertEquals("Log Entry falsch",
				"Jaja. Lange gesucht an den typischen Stellen, um dann letztendlich ganz woanders fündig zu werden...", log.Comment);

	}

	public static void testGpxImportWithCorrected() throws Exception
	{

		// initialize Database
		InitTestDBs.InitalConfig();

		ImportHandler importHandler = new ImportHandler();

		Database.Data.beginTransaction();

		try
		{
			GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/CorrectedCoordinates.gpx"));
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);

			Database.Data.setTransactionSuccessful();
		}
		finally
		{
		}

		Database.Data.endTransaction();

		CacheDAO cacheDAO = new CacheDAO();

		Cache cache = cacheDAO.getFromDbByGcCode("GCC0RR1", false, true);

		cache.loadDetail();

		assertTrue("Cache muss zurückgegeben werden", cache != null);

		assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 50.85);
		assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 9.85);
		assertTrue("Pos ist ungültig", cache.Pos.isValid());

		assertEquals("GcCode falsch", "GCC0RR1", cache.getGcCode());
		assertEquals("DateHidden falsch", "Tue Jun 24 08:00:00 CEST 2014", cache.getDateHidden().toString());
		assertEquals("url falsch", "", cache.getUrl());// URL wird noch nicht
														// ausgelesen
		assertTrue("Found ist falsch", !cache.isFound());

		assertEquals("Id ist falsch", cache.getGcId(), "99000003");
		assertTrue("ist available ist falsch", cache.isAvailable());
		assertFalse("ist archived ist falsch", cache.isArchived());
		assertEquals("Test-Cache für GSAK Corrected Coordinates", cache.getName());
		assertEquals("Placed by falsch", "Test Owner", cache.getPlacedBy());
		assertEquals("Owner falsch", "Test Owner", cache.getOwner());
		assertTrue("Typ ist falsch", cache.Type == CacheTypes.Mystery);
		assertTrue("Size ist falsch", cache.Size == CacheSizes.regular);
		assertTrue("Difficulty ist falsch", cache.getDifficulty() == 1);
		assertTrue("Terrain ist falsch", cache.getTerrain() == 1);

		// Attribute Tests

		ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
		ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();

		// { Keine Attribute gesetzt
		// PositvieList.add(Attributes.Bicycles);
		//
		// NegativeList.add(Attributes.Recommended_at_night);
		// }

		Iterator<Attributes> positiveInterator = PositvieList.iterator();
		Iterator<Attributes> negativeInterator = NegativeList.iterator();

		while (positiveInterator.hasNext())
		{
			assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
		}

		while (negativeInterator.hasNext())
		{
			Attributes tmp = negativeInterator.next();

			assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
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
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributePositiveSet(attr));
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributeNegativeSet(attr));
		}
		//

		// TODO Beschreibungstexte überprüfen
		// System.out.println( cache.shortDescription );
		// System.out.println( cache.longDescription );

		assertEquals("Hint falsch", "Final: Im Loch", cache.getHint());

		CB_List<LogEntry> logs = new CB_List<LogEntry>();
		logs = Database.Logs(cache);

		assertTrue("es dürfte keine Logs geben", logs.size() == 0);

		// Check Final WP
		assertTrue("Cache einen Final WP haben", cache.GetFinalWaypoint() != null);

		Waypoint wp = cache.GetFinalWaypoint();

		assertTrue("FinalWpPos: Latitude falsch", wp.Pos.getLatitude() == 50.85205);
		assertTrue("FinalWpPos: Longitude falsch", wp.Pos.getLongitude() == 9.8576);
		assertTrue("FinalWpPos ist ungültig", cache.Pos.isValid());

	}

	public static void testGpxImportWithCorrected1_1() throws Exception
	{

		// initialize Database
		InitTestDBs.InitalConfig();

		ImportHandler importHandler = new ImportHandler();

		Database.Data.beginTransaction();

		try
		{
			GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/CorrectedCoordinates1.1.gpx"));
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);

			Database.Data.setTransactionSuccessful();
		}
		finally
		{
		}

		Database.Data.endTransaction();

		CacheDAO cacheDAO = new CacheDAO();

		Cache cache = cacheDAO.getFromDbByGcCode("GCC0RR1", false, true);

		cache.loadDetail();

		assertTrue("Cache muss zurückgegeben werden", cache != null);

		assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 50.85);
		assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 9.85);
		assertTrue("Pos ist ungültig", cache.Pos.isValid());

		assertEquals("GcCode falsch", "GCC0RR1", cache.getGcCode());
		assertEquals("DateHidden falsch", "Tue Jun 24 08:00:00 CEST 2014", cache.getDateHidden().toString());
		assertEquals("url falsch", "", cache.getUrl());// URL wird noch nicht
														// ausgelesen
		assertTrue("Found ist falsch", !cache.isFound());

		assertEquals("Id ist falsch", cache.getGcId(), "99000003");
		assertTrue("ist available ist falsch", cache.isAvailable());
		assertFalse("ist archived ist falsch", cache.isArchived());
		assertEquals("Test-Cache für GSAK Corrected Coordinates", cache.getName());
		assertEquals("Placed by falsch", "Test Owner", cache.getPlacedBy());
		assertEquals("Owner falsch", "Test Owner", cache.getOwner());
		assertTrue("Typ ist falsch", cache.Type == CacheTypes.Mystery);
		assertTrue("Size ist falsch", cache.Size == CacheSizes.regular);
		assertTrue("Difficulty ist falsch", cache.getDifficulty() == 1);
		assertTrue("Terrain ist falsch", cache.getTerrain() == 1);

		// Attribute Tests

		ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
		ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();

		// { Keine Attribute gesetzt
		// PositvieList.add(Attributes.Bicycles);
		//
		// NegativeList.add(Attributes.Recommended_at_night);
		// }

		Iterator<Attributes> positiveInterator = PositvieList.iterator();
		Iterator<Attributes> negativeInterator = NegativeList.iterator();

		while (positiveInterator.hasNext())
		{
			assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
		}

		while (negativeInterator.hasNext())
		{
			Attributes tmp = negativeInterator.next();

			assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
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
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributePositiveSet(attr));
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributeNegativeSet(attr));
		}
		//

		// TODO Beschreibungstexte überprüfen
		// System.out.println( cache.shortDescription );
		// System.out.println( cache.longDescription );

		assertEquals("Hint falsch", "Final: Im Loch", cache.getHint());

		CB_List<LogEntry> logs = new CB_List<LogEntry>();
		logs = Database.Logs(cache);

		assertTrue("es dürfte keine Logs geben", logs.size() == 0);

		// Check Final WP
		assertTrue("Cache einen Final WP haben", cache.GetFinalWaypoint() != null);

		Waypoint wp = cache.GetFinalWaypoint();

		assertTrue("FinalWpPos: Latitude falsch", wp.Pos.getLatitude() == 50.85205);
		assertTrue("FinalWpPos: Longitude falsch", wp.Pos.getLongitude() == 9.8576);
		assertTrue("FinalWpPos ist ungültig", cache.Pos.isValid());

	}

	public static void testGpxImportWithCorrectedAndParent() throws Exception
	{

		// initialize Database
		InitTestDBs.InitalConfig();

		ImportHandler importHandler = new ImportHandler();

		Database.Data.beginTransaction();

		try
		{
			GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/TestCache3_WP_Parents_1_0.gpx"));
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);

			Database.Data.setTransactionSuccessful();
		}
		finally
		{
		}

		Database.Data.endTransaction();

		CacheDAO cacheDAO = new CacheDAO();

		Cache cache = cacheDAO.getFromDbByGcCode("ACWP003", false, true);

		cache.loadDetail();

		assertTrue("Cache muss zurückgegeben werden", cache != null);

		assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 50.891667);
		assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 9.891667);
		assertTrue("Pos ist ungültig", cache.Pos.isValid());

		assertEquals("GcCode falsch", "ACWP003", cache.getGcCode());
		assertEquals("DateHidden falsch", "Tue Jun 24 08:00:00 CEST 2014", cache.getDateHidden().toString());
		assertEquals("url falsch", "http://team-cachebox.de/cache.php?id=ACWP003", cache.getUrl());// URL wird noch nicht
		// ausgelesen
		assertTrue("Found ist falsch", !cache.isFound());

		assertEquals("Id ist falsch", cache.getGcId(), "99000004");
		assertTrue("ist available ist falsch", cache.isAvailable());
		assertFalse("ist archived ist falsch", cache.isArchived());
		assertEquals("Test-Cache 3 für Wegpunkte", cache.getName());
		assertEquals("Placed by falsch", "Test Owner", cache.getPlacedBy());
		assertEquals("Owner falsch", "Test Owner", cache.getOwner());
		assertTrue("Typ ist falsch", cache.Type == CacheTypes.Multi);
		assertTrue("Size ist falsch", cache.Size == CacheSizes.regular);
		assertTrue("Difficulty ist falsch", cache.getDifficulty() == 1.5);
		assertTrue("Terrain ist falsch", cache.getTerrain() == 2.5);

		// Attribute Tests

		ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
		ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();

		// { Keine Attribute gesetzt
		// PositvieList.add(Attributes.Bicycles);
		//
		// NegativeList.add(Attributes.Recommended_at_night);
		// }

		Iterator<Attributes> positiveInterator = PositvieList.iterator();
		Iterator<Attributes> negativeInterator = NegativeList.iterator();

		while (positiveInterator.hasNext())
		{
			assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
		}

		while (negativeInterator.hasNext())
		{
			Attributes tmp = negativeInterator.next();

			assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
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
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributePositiveSet(attr));
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributeNegativeSet(attr));
		}
		//

		// TODO Beschreibungstexte überprüfen
		// System.out.println( cache.shortDescription );
		// System.out.println( cache.longDescription );

		assertEquals("Hint falsch", "Final: Im Loch", cache.getHint());

		CB_List<LogEntry> logs = new CB_List<LogEntry>();
		logs = Database.Logs(cache);

		assertTrue("es müsste 2 Logs geben", logs.size() == 2);

		// Check WP count
		assertTrue("Cache muss einen WP haben", cache.waypoints.size() == 1);

		Waypoint wp = cache.waypoints.get(0);

		assertTrue("WpPos: Latitude falsch", wp.Pos.getLatitude() == 50.895);
		assertTrue("WpPos: Longitude falsch", wp.Pos.getLongitude() == 9.895);
		assertTrue("WpPos ist ungültig", wp.Pos.isValid());
		assertEquals("Titel muss gleich sein", "WP 1 von ACWP003", wp.getTitle());
		assertEquals("Description muss gleich sein", "Dieser Wegpunkt muss dem Cache ACWP003 zugeordnet werden", wp.getDescription());
		assertEquals("GC-Code muss gleich sein", "S1WP003", wp.getGcCode());

		assertEquals("parent CacheID muss gleich sein", 14408207876703041L, wp.CacheId);

	}

	public static void testGpxImportWithCorrectedAndParent1_1() throws Exception
	{

		// initialize Database
		InitTestDBs.InitalConfig();

		ImportHandler importHandler = new ImportHandler();

		Database.Data.beginTransaction();

		try
		{
			GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/TestCache3_WP_Parents_1_1.gpx"));
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);

			Database.Data.setTransactionSuccessful();
		}
		finally
		{
		}

		Database.Data.endTransaction();

		CacheDAO cacheDAO = new CacheDAO();

		Cache cache = cacheDAO.getFromDbByGcCode("ACWP003", false, true);

		cache.loadDetail();

		assertTrue("Cache muss zurückgegeben werden", cache != null);

		assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 50.891667);
		assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 9.891667);
		assertTrue("Pos ist ungültig", cache.Pos.isValid());

		assertEquals("GcCode falsch", "ACWP003", cache.getGcCode());
		assertEquals("DateHidden falsch", "Tue Jun 24 08:00:00 CEST 2014", cache.getDateHidden().toString());
		assertEquals("url falsch", "", cache.getUrl());// URL wird noch nicht
		// ausgelesen
		assertTrue("Found ist falsch", !cache.isFound());

		assertEquals("Id ist falsch", cache.getGcId(), "99000004");
		assertTrue("ist available ist falsch", cache.isAvailable());
		assertFalse("ist archived ist falsch", cache.isArchived());
		assertEquals("Test-Cache 3 für Wegpunkte", cache.getName());
		assertEquals("Placed by falsch", "Test Owner", cache.getPlacedBy());
		assertEquals("Owner falsch", "Test Owner", cache.getOwner());
		assertTrue("Typ ist falsch", cache.Type == CacheTypes.Multi);
		assertTrue("Size ist falsch", cache.Size == CacheSizes.regular);
		assertTrue("Difficulty ist falsch", cache.getDifficulty() == 1.5);
		assertTrue("Terrain ist falsch", cache.getTerrain() == 2.5);

		// Attribute Tests

		ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
		ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();

		// { Keine Attribute gesetzt
		// PositvieList.add(Attributes.Bicycles);
		//
		// NegativeList.add(Attributes.Recommended_at_night);
		// }

		Iterator<Attributes> positiveInterator = PositvieList.iterator();
		Iterator<Attributes> negativeInterator = NegativeList.iterator();

		while (positiveInterator.hasNext())
		{
			assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
		}

		while (negativeInterator.hasNext())
		{
			Attributes tmp = negativeInterator.next();

			assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
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
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributePositiveSet(attr));
			assertFalse(attr.name() + "Attribut falsch", cache.isAttributeNegativeSet(attr));
		}
		//

		// TODO Beschreibungstexte überprüfen
		// System.out.println( cache.shortDescription );
		// System.out.println( cache.longDescription );

		assertEquals("Hint falsch", "Final: Im Loch", cache.getHint());

		CB_List<LogEntry> logs = new CB_List<LogEntry>();
		logs = Database.Logs(cache);

		assertTrue("es müsste 2 Logs geben", logs.size() == 2);

		// Check WP count
		assertTrue("Cache muss einen WP haben", cache.waypoints.size() == 1);

		Waypoint wp = cache.waypoints.get(0);

		assertTrue("WpPos: Latitude falsch", wp.Pos.getLatitude() == 50.895);
		assertTrue("WpPos: Longitude falsch", wp.Pos.getLongitude() == 9.895);
		assertTrue("WpPos ist ungültig", wp.Pos.isValid());
		assertEquals("Titel muss gleich sein", "WP 1 von ACWP003", wp.getTitle());
		assertEquals("Description muss gleich sein", "Dieser Wegpunkt muss dem Cache ACWP003 zugeordnet werden", wp.getDescription());
		assertEquals("GC-Code muss gleich sein", "S1WP003", wp.getGcCode());

		assertEquals("parent CacheID muss gleich sein", 14408207876703041L, wp.CacheId);

	}
}
