package CB_Core.Import;

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
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;

public class GSAKGpxImportTest extends TestCase
{

	public static void testGpxImport() throws Exception
	{

		// initialize Database
		String database = "./testdata/test.db3";
		InitTestDBs.InitTestDB(database);

		ImportHandler importHandler = new ImportHandler();

		Database.Data.beginTransaction();

		try
		{
			GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/GSAK_1_1.gpx"));
			assertTrue("Objekt muss konstruierbar sein", importer != null);
			importer.doImport(importHandler, 0);

			Database.Data.setTransactionSuccessful();
		}
		finally
		{
		}

		Database.Data.endTransaction();

		CacheDAO cacheDAO = new CacheDAO();

		Cache cache = cacheDAO.getFromDbByGcCode("GC1XCEW");

		assertTrue("Cache muss zurückgegeben werden", cache != null);

		assertTrue("Pos: Latitude falsch", cache.Pos.Latitude == 52.579333);
		assertTrue("Pos: Longitude falsch", cache.Pos.Longitude == 13.40545);
		assertTrue("Pos ist ungültig", cache.Pos.Valid);

		assertEquals("GcCode falsch", "GC1XCEW", cache.GcCode);
		assertEquals("DateHidden falsch", "Mon Aug 17 08:00:00 CEST 2009", cache.DateHidden.toString());
		assertEquals("url falsch", "", cache.Url);// URL wird noch nicht
													// ausgelesen
		assertTrue("Found ist falsch", cache.Found);

		assertEquals("Id ist falsch", cache.GcId, "1358542");
		assertFalse("ist available ist falsch", cache.Available);
		assertTrue("ist archived ist falsch", cache.Archived);
		assertEquals("Name falsch", "Schlossblick # 2/ View at the castle  #2", cache.Name);
		assertEquals("Placed by falsch", "Risou", cache.PlacedBy);
		assertEquals("Owner falsch", "Risou", cache.Owner);
		assertTrue("Typ ist falsch", cache.Type == CacheTypes.Mystery);
		assertTrue("Size ist falsch", cache.Size == CacheSizes.micro);
		assertTrue("Difficulty ist falsch", cache.Difficulty == 2);
		assertTrue("Terrain ist falsch", cache.Terrain == 2);

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
		Iterator RestInterator = attributes.iterator();

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

		assertEquals("Hint falsch", "", cache.hint);

		ArrayList<LogEntry> logs = new ArrayList<LogEntry>();
		logs = Database.Logs(cache);

		LogEntry log = logs.get(0);

		assertEquals("CacheId ist falsch", log.CacheId, 24564478518575943L);
		assertEquals("Id ist falsch", log.Id, 140640156);
		assertEquals("Timestamp falsch", "Sat Jan 08 20:00:00 CET 2011", log.Timestamp.toString());
		assertEquals("Finder falsch", "Katipa", log.Finder);
		assertTrue("LogTyp falsch", log.Type == LogTypes.found);

		assertEquals("Log Entry falsch",
				"Jaja. Lange gesucht an den typischen Stellen, um dann letztendlich ganz woanders fündig zu werden...", log.Comment);

		Database.Data.Close();
	}
}
