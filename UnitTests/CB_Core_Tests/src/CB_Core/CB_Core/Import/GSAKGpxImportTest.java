package CB_Core.CB_Core.Import;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Attributes;
import CB_Core.CacheSizes;
import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.DAO.CacheDAO;
import CB_Core.Import.GPXFileImporter;
import CB_Core.Import.ImportHandler;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Utils.Lists.CB_List;
import __Static.InitTestDBs;
import junit.framework.TestCase;

public class GSAKGpxImportTest extends TestCase {

    public static void testGpxImport() throws Exception {

	// initialize Database
	InitTestDBs.InitalConfig();

	ImportHandler importHandler = new ImportHandler();

	Database.Data.beginTransaction();

	try {
	    File importFile = new File("./testdata/gpx/CorrectedCoordinates1.1.gpx");
	    assertTrue("Import-TestFile missing", importFile.exists());
	    GPXFileImporter importer = new GPXFileImporter(importFile);
	    assertTrue("Objekt muss konstruierbar sein", importer != null);
	    importer.doImport(importHandler, 0);

	    Database.Data.setTransactionSuccessful();
	} finally {
	}

	Database.Data.endTransaction();

	CacheDAO cacheDAO = new CacheDAO();

	Cache cache = cacheDAO.getFromDbByGcCode("GCC0RR1", true);

	assertTrue("Cache muss zurückgegeben werden", cache != null);

	assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 50.85);
	assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 9.85);
	assertTrue("Pos ist ungültig", cache.Pos.isValid());

	assertEquals("GcCode falsch", "GCC0RR1", cache.getGcCode());
	assertEquals("DateHidden falsch", "Tue Jun 24 08:00:00 CEST 2014", cache.getDateHidden().toString());
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

	while (positiveInterator.hasNext()) {
	    assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
	}

	while (negativeInterator.hasNext()) {
	    Attributes tmp = negativeInterator.next();

	    assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
	}

	// fülle eine Liste mit allen Attributen
	ArrayList<Attributes> attributes = new ArrayList<Attributes>();
	Attributes[] tmp = Attributes.values();
	for (Attributes item : tmp) {
	    attributes.add(item);
	}

	// Lösche die vergebenen Atribute aus der Kommplett Liste
	positiveInterator = PositvieList.iterator();
	negativeInterator = NegativeList.iterator();

	while (positiveInterator.hasNext()) {
	    attributes.remove(positiveInterator.next());
	}

	while (negativeInterator.hasNext()) {
	    attributes.remove(negativeInterator.next());
	}

	attributes.remove(Attributes.getAttributeEnumByGcComId(64));
	attributes.remove(Attributes.getAttributeEnumByGcComId(65));
	attributes.remove(Attributes.getAttributeEnumByGcComId(66));

	// Teste ob die Übrig gebliebenen Atributte auch nicht vergeben wurden.
	Iterator<Attributes> RestInterator = attributes.iterator();

	while (RestInterator.hasNext()) {
	    Attributes attr = (Attributes) RestInterator.next();
	    assertFalse(attr.name() + "Attribut falsch", cache.isAttributePositiveSet(attr));
	    assertFalse(attr.name() + "Attribut falsch", cache.isAttributeNegativeSet(attr));
	}
	//

	// TODO Beschreibungstexte überprüfen
	// System.out.println( cache.shortDescription );
	// System.out.println( cache.longDescription );

	assertEquals("Hint falsch", "Final: Im Loch", cache.getHint());

    }

    public static void testGpxImportWithCorrected() throws Exception {

	// initialize Database
	InitTestDBs.InitalConfig();

	ImportHandler importHandler = new ImportHandler();

	Database.Data.beginTransaction();

	try {
	    GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/CorrectedCoordinates.gpx"));
	    assertTrue("Objekt muss konstruierbar sein", importer != null);
	    importer.doImport(importHandler, 0);

	    Database.Data.setTransactionSuccessful();
	} finally {
	}

	Database.Data.endTransaction();

	CacheDAO cacheDAO = new CacheDAO();

	Cache cache = cacheDAO.getFromDbByGcCode("GCC0RR1", true);

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

	while (positiveInterator.hasNext()) {
	    assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
	}

	while (negativeInterator.hasNext()) {
	    Attributes tmp = negativeInterator.next();

	    assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
	}

	// fülle eine Liste mit allen Attributen
	ArrayList<Attributes> attributes = new ArrayList<Attributes>();
	Attributes[] tmp = Attributes.values();
	for (Attributes item : tmp) {
	    attributes.add(item);
	}

	// Lösche die vergebenen Atribute aus der Kommplett Liste
	positiveInterator = PositvieList.iterator();
	negativeInterator = NegativeList.iterator();

	while (positiveInterator.hasNext()) {
	    attributes.remove(positiveInterator.next());
	}

	while (negativeInterator.hasNext()) {
	    attributes.remove(negativeInterator.next());
	}

	attributes.remove(Attributes.getAttributeEnumByGcComId(64));
	attributes.remove(Attributes.getAttributeEnumByGcComId(65));
	attributes.remove(Attributes.getAttributeEnumByGcComId(66));

	// Teste ob die Übrig gebliebenen Atributte auch nicht vergeben wurden.
	Iterator<Attributes> RestInterator = attributes.iterator();

	while (RestInterator.hasNext()) {
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

    public static void testGpxImportWithCorrected1_1() throws Exception {

	// initialize Database
	InitTestDBs.InitalConfig();

	ImportHandler importHandler = new ImportHandler();

	Database.Data.beginTransaction();

	try {
	    GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/CorrectedCoordinates1.1.gpx"));
	    assertTrue("Objekt muss konstruierbar sein", importer != null);
	    importer.doImport(importHandler, 0);

	    Database.Data.setTransactionSuccessful();
	} finally {
	}

	Database.Data.endTransaction();

	CacheDAO cacheDAO = new CacheDAO();
	Cache cache = cacheDAO.getFromDbByGcCode("GCC0RR1", false);
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

	while (positiveInterator.hasNext()) {
	    assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
	}

	while (negativeInterator.hasNext()) {
	    Attributes tmp = negativeInterator.next();

	    assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
	}

	// fülle eine Liste mit allen Attributen
	ArrayList<Attributes> attributes = new ArrayList<Attributes>();
	Attributes[] tmp = Attributes.values();
	for (Attributes item : tmp) {
	    attributes.add(item);
	}

	// Lösche die vergebenen Atribute aus der Kommplett Liste
	positiveInterator = PositvieList.iterator();
	negativeInterator = NegativeList.iterator();

	while (positiveInterator.hasNext()) {
	    attributes.remove(positiveInterator.next());
	}

	while (negativeInterator.hasNext()) {
	    attributes.remove(negativeInterator.next());
	}

	attributes.remove(Attributes.getAttributeEnumByGcComId(64));
	attributes.remove(Attributes.getAttributeEnumByGcComId(65));
	attributes.remove(Attributes.getAttributeEnumByGcComId(66));

	// Teste ob die Übrig gebliebenen Atributte auch nicht vergeben wurden.
	Iterator<Attributes> RestInterator = attributes.iterator();

	while (RestInterator.hasNext()) {
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
	assertTrue("Cache muss einen Final WP haben", cache.GetFinalWaypoint() != null);

	Waypoint wp = cache.GetFinalWaypoint();

	assertTrue("FinalWpPos: Latitude falsch", wp.Pos.getLatitude() == 50.85205);
	assertTrue("FinalWpPos: Longitude falsch", wp.Pos.getLongitude() == 9.8576);
	assertTrue("FinalWpPos ist ungültig", cache.Pos.isValid());

    }

    public static void testGpxImportWithCorrectedAndParent() throws Exception {

	// initialize Database
	InitTestDBs.InitalConfig();

	ImportHandler importHandler = new ImportHandler();

	Database.Data.beginTransaction();

	try {
	    GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/TestCache3_WP_Parents_1_0.gpx"));
	    assertTrue("Objekt muss konstruierbar sein", importer != null);
	    importer.doImport(importHandler, 0);

	    Database.Data.setTransactionSuccessful();
	} finally {
	}

	Database.Data.endTransaction();

	CacheDAO cacheDAO = new CacheDAO();

	Cache cache = cacheDAO.getFromDbByGcCode("ACWP003", true);

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

	while (positiveInterator.hasNext()) {
	    assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
	}

	while (negativeInterator.hasNext()) {
	    Attributes tmp = negativeInterator.next();

	    assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
	}

	// fülle eine Liste mit allen Attributen
	ArrayList<Attributes> attributes = new ArrayList<Attributes>();
	Attributes[] tmp = Attributes.values();
	for (Attributes item : tmp) {
	    attributes.add(item);
	}

	// Lösche die vergebenen Atribute aus der Kommplett Liste
	positiveInterator = PositvieList.iterator();
	negativeInterator = NegativeList.iterator();

	while (positiveInterator.hasNext()) {
	    attributes.remove(positiveInterator.next());
	}

	while (negativeInterator.hasNext()) {
	    attributes.remove(negativeInterator.next());
	}

	attributes.remove(Attributes.getAttributeEnumByGcComId(64));
	attributes.remove(Attributes.getAttributeEnumByGcComId(65));
	attributes.remove(Attributes.getAttributeEnumByGcComId(66));

	// Teste ob die Übrig gebliebenen Atributte auch nicht vergeben wurden.
	Iterator<Attributes> RestInterator = attributes.iterator();

	while (RestInterator.hasNext()) {
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

    public static void testGpxImportWithCorrectedAndParent1_1() throws Exception {

	// initialize Database
	InitTestDBs.InitalConfig();

	ImportHandler importHandler = new ImportHandler();

	Database.Data.beginTransaction();

	try {
	    GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/TestCache3_WP_Parents_1_1.gpx"));
	    assertTrue("Objekt muss konstruierbar sein", importer != null);
	    importer.doImport(importHandler, 0);

	    Database.Data.setTransactionSuccessful();
	} finally {
	}

	Database.Data.endTransaction();

	CacheDAO cacheDAO = new CacheDAO();

	Cache cache = cacheDAO.getFromDbByGcCode("ACWP003", true);

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

	while (positiveInterator.hasNext()) {
	    assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
	}

	while (negativeInterator.hasNext()) {
	    Attributes tmp = negativeInterator.next();

	    assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
	}

	// fülle eine Liste mit allen Attributen
	ArrayList<Attributes> attributes = new ArrayList<Attributes>();
	Attributes[] tmp = Attributes.values();
	for (Attributes item : tmp) {
	    attributes.add(item);
	}

	// Lösche die vergebenen Atribute aus der Kommplett Liste
	positiveInterator = PositvieList.iterator();
	negativeInterator = NegativeList.iterator();

	while (positiveInterator.hasNext()) {
	    attributes.remove(positiveInterator.next());
	}

	while (negativeInterator.hasNext()) {
	    attributes.remove(negativeInterator.next());
	}

	attributes.remove(Attributes.getAttributeEnumByGcComId(64));
	attributes.remove(Attributes.getAttributeEnumByGcComId(65));
	attributes.remove(Attributes.getAttributeEnumByGcComId(66));

	// Teste ob die Übrig gebliebenen Atributte auch nicht vergeben wurden.
	Iterator<Attributes> RestInterator = attributes.iterator();

	while (RestInterator.hasNext()) {
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

    public static void testGpxImportParent1_1() throws Exception {

	// initialize Database
	InitTestDBs.InitalConfig();

	ImportHandler importHandler = new ImportHandler();

	Database.Data.beginTransaction();

	try {
	    GPXFileImporter importer = new GPXFileImporter(new File("./testdata/gpx/OCF19A.gpx"));
	    assertTrue("Objekt muss konstruierbar sein", importer != null);
	    importer.doImport(importHandler, 0);

	    Database.Data.setTransactionSuccessful();
	} finally {
	}

	Database.Data.endTransaction();

	CacheDAO cacheDAO = new CacheDAO();

	Cache cache = cacheDAO.getFromDbByGcCode("OCF19A", true);

	cache.loadDetail();

	assertTrue("Cache muss zurückgegeben werden", cache != null);

	assertTrue("Pos: Latitude falsch", cache.Pos.getLatitude() == 53.00727);
	assertTrue("Pos: Longitude falsch", cache.Pos.getLongitude() == 9.00923);
	assertTrue("Pos ist ungültig", cache.Pos.isValid());

	assertEquals("GcCode falsch", cache.getGcCode(), "OCF19A");
	assertEquals("DateHidden falsch", "Wed Jan 09 00:00:00 CET 2013", cache.getDateHidden().toString());
	assertEquals("url falsch", "http://www.opencaching.de/viewcache.php?cacheid=164939", cache.getUrl());// URL wird noch nicht
	// ausgelesen
	assertTrue("Found ist falsch", cache.isFound());

	assertEquals("Id ist falsch", cache.getGcId(), "164939");
	assertTrue("ist available ist falsch", cache.isAvailable());
	assertFalse("ist archived ist falsch", cache.isArchived());
	assertEquals("Bierdener Marsch", cache.getName());
	assertEquals("Placed by falsch", "Danlex", cache.getPlacedBy());
	assertEquals("Owner falsch", "Danlex", cache.getOwner());
	assertTrue("Typ ist falsch", cache.Type == CacheTypes.Multi);
	assertTrue("Size ist falsch", cache.Size == CacheSizes.small);
	assertTrue("Difficulty ist falsch", cache.getDifficulty() == 2);
	assertTrue("Terrain ist falsch", cache.getTerrain() == 2.5);

	// Attribute Tests

	ArrayList<Attributes> PositvieList = new ArrayList<Attributes>();
	ArrayList<Attributes> NegativeList = new ArrayList<Attributes>();

	{
	    PositvieList.add(Attributes.Hunting);
	    PositvieList.add(Attributes.Thorns);
	    PositvieList.add(Attributes.Ticks);

	    NegativeList.add(Attributes.Recommended_at_night);
	    NegativeList.add(Attributes.Seasonal_Access);
	}

	Iterator<Attributes> positiveInterator = PositvieList.iterator();
	Iterator<Attributes> negativeInterator = NegativeList.iterator();

	while (positiveInterator.hasNext()) {
	    assertTrue("Attribut falsch", cache.isAttributePositiveSet((Attributes) positiveInterator.next()));
	}

	while (negativeInterator.hasNext()) {
	    Attributes tmp = negativeInterator.next();

	    assertTrue(tmp.name() + " negative Attribut falsch", cache.isAttributeNegativeSet((tmp)));
	}

	// fülle eine Liste mit allen Attributen
	ArrayList<Attributes> attributes = new ArrayList<Attributes>();
	Attributes[] tmp = Attributes.values();
	for (Attributes item : tmp) {
	    attributes.add(item);
	}

	// Lösche die vergebenen Atribute aus der Kommplett Liste
	positiveInterator = PositvieList.iterator();
	negativeInterator = NegativeList.iterator();

	while (positiveInterator.hasNext()) {
	    attributes.remove(positiveInterator.next());
	}

	while (negativeInterator.hasNext()) {
	    attributes.remove(negativeInterator.next());
	}

	attributes.remove(Attributes.getAttributeEnumByGcComId(64));
	attributes.remove(Attributes.getAttributeEnumByGcComId(65));
	attributes.remove(Attributes.getAttributeEnumByGcComId(66));

	// Teste ob die Übrig gebliebenen Atributte auch nicht vergeben wurden.
	Iterator<Attributes> RestInterator = attributes.iterator();

	while (RestInterator.hasNext()) {
	    Attributes attr = (Attributes) RestInterator.next();
	    assertFalse(attr.name() + "Attribut falsch", cache.isAttributePositiveSet(attr));
	    assertFalse(attr.name() + "Attribut falsch", cache.isAttributeNegativeSet(attr));
	}
	//

	String Hint = "Start: Bei geformtem in gewachsenem Holz\nFinal: Am Boden";
	assertEquals("Hint falsch", Hint, cache.getHint());

	CB_List<LogEntry> logs = new CB_List<LogEntry>();
	logs = Database.Logs(cache);

	assertTrue("es müsste 2 Logs geben", logs.size() == 2);

	// Check WP count
	assertTrue("Cache muss drei WP's haben", cache.waypoints.size() == 3);

	{
	    Waypoint wp = cache.waypoints.get(0);
	    assertTrue("WpPos: Latitude falsch", wp.Pos.getLatitude() == 53.00888);
	    assertTrue("WpPos: Longitude falsch", wp.Pos.getLongitude() == 9.00828);
	    assertTrue("WpPos ist ungültig", wp.Pos.isValid());
	    assertEquals("Titel muss gleich sein", "Parkplatz", wp.getTitle());
	    assertEquals("Description muss gleich sein", "Hier haben einige Cachemobile platz", wp.getDescription());
	    assertEquals("GC-Code muss gleich sein", "OCF19A-1", wp.getGcCode());
	    assertEquals("parent CacheID muss gleich sein", cache.Id, wp.CacheId);
	    assertEquals("WP-Type muss gleich sein", CacheTypes.ParkingArea, wp.Type);
	}

	{
	    Waypoint wp = cache.waypoints.get(1);
	    assertTrue("WpPos: Latitude falsch", wp.Pos.getLatitude() == 53.00462);
	    assertTrue("WpPos: Longitude falsch", wp.Pos.getLongitude() == 8.99772);
	    assertTrue("WpPos ist ungültig", wp.Pos.isValid());
	    assertEquals("Titel muss gleich sein", "Station oder Referenzpunkt", wp.getTitle());
	    assertEquals("Description muss gleich sein", "Auf dieser Bank kann sich ausgeruht werden, sofern notwendig", wp.getDescription());
	    assertEquals("GC-Code muss gleich sein", "OCF19A-2", wp.getGcCode());
	    assertEquals("parent CacheID muss gleich sein", cache.Id, wp.CacheId);
	    assertEquals("WP-Type muss gleich sein", CacheTypes.ReferencePoint, wp.Type);
	}

	{
	    Waypoint wp = cache.waypoints.get(2);
	    assertTrue("WpPos: Latitude falsch", wp.Pos.getLatitude() == 52.99973);
	    assertTrue("WpPos: Longitude falsch", wp.Pos.getLongitude() == 9.00903);
	    assertTrue("WpPos ist ungültig", wp.Pos.isValid());
	    assertEquals("Titel muss gleich sein", "Station oder Referenzpunkt", wp.getTitle());
	    assertEquals("Description muss gleich sein", "Auf dieser Bank kann sich ausgeruht werden, sofern notwendig", wp.getDescription());
	    assertEquals("GC-Code muss gleich sein", "OCF19A-3", wp.getGcCode());
	    assertEquals("parent CacheID muss gleich sein", cache.Id, wp.CacheId);
	    assertEquals("WP-Type muss gleich sein", CacheTypes.ReferencePoint, wp.Type);
	}
    }
}
