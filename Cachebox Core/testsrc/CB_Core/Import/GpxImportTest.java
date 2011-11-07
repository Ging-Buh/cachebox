package CB_Core.Import;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.DAO.CacheDAO;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import junit.framework.TestCase;

public class GpxImportTest extends TestCase {
	
	public static void testGpxImport() throws Exception
	{
		ImportHandler importHandler = new ImportHandler();
		GPXFileImporter importer = new GPXFileImporter( "./testdata/gpx/GC2T9RW.gpx" );
		assertTrue( "Objekt muss konstruierbar sein", importer!=null );
		importer.doImport( importHandler, 0 );
		
		CacheDAO cacheDAO = new CacheDAO();
		
		Cache cache = cacheDAO.getFromDbByGcCode("GC2T9RW");
		
		assertTrue( "Cache muss zur�ckgegeben werden", cache!=null );
		
		assertTrue( "Pos: Latitude falsch", cache.Pos.Latitude == 49.349817 );
		assertTrue( "Pos: Longitude falsch", cache.Pos.Longitude == 8.62925 );
		assertTrue( "Pos ist ung�ltig", cache.Pos.Valid );
		
		assertEquals( "GcCode falsch", "GC2T9RW", cache.GcCode );
		assertEquals( "DateHidden falsch", "Sat Apr 16 07:00:00 CEST 2011", cache.DateHidden.toString() );
		assertEquals( "url falsch", "http://www.geocaching.com/seek/cache_details.aspx?guid=f26f18bd-9aaa-4499-944b-3e8cb62e41a7", cache.Url );
		assertTrue( "Found ist falsch", cache.Found );

		assertEquals( "Id ist falsch", cache.GcId, "2190117" );
		assertTrue( "ist available ist falsch", cache.Available );
		assertFalse( "ist archived ist falsch", cache.Archived );
		assertEquals( "Name falsch", "der Hampir - T5 - ", cache.Name );
		assertEquals( "Placed by falsch", "Team Rabbits", cache.PlacedBy );
		assertEquals( "Owner falsch", "Team Rabbits", cache.Owner );
		assertTrue( "Typ ist falsch", cache.Type == CacheTypes.Traditional );
		assertTrue( "Size ist falsch", cache.Size == CacheSizes.small );
		assertTrue( "Difficulty ist falsch", cache.Difficulty == 2 );
		assertTrue( "Terrain ist falsch", cache.Terrain == 5 );
		
		assertTrue( "Attribut falsch", cache.isAttributePositiveSet( Attributes.Bicycles ) );
		assertFalse( "Attribut falsch", cache.isAttributeNegativeSet( Attributes.Bicycles ) );
		assertFalse( "Attribut falsch", cache.isAttributePositiveSet( Attributes.Boat ) );
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
		
		
		
		
		
		Iterator positiveInterator = PositvieList.iterator();
		Iterator negativeInterator = NegativeList.iterator();
		
		while(positiveInterator.hasNext())
		{
			Attributes attr = (Attributes) positiveInterator.next();
			assertTrue( attr.toString()+ " Attribut falsch", cache.isAttributePositiveSet( attr ) );
		}
		
		while(negativeInterator.hasNext())
		{
			Attributes attr = (Attributes) negativeInterator.next();
			assertTrue( attr.toString()+ " Attribut falsch", cache.isAttributeNegativeSet( attr ) );
		}
		
		
		// f�lle eine Liste mit allen Attributen
		ArrayList<Attributes> attributes= new ArrayList<Attributes>();
		Attributes[] tmp = Attributes.values();
		for ( Attributes item : tmp)
		{
			attributes.add(item);
		}
		
		//L�sche die vergebenen Atribute aus der Kommplett Liste
		positiveInterator = PositvieList.iterator();
		negativeInterator = NegativeList.iterator();
		
		while(positiveInterator.hasNext())
		{
			attributes.remove(positiveInterator.next());
		}
		
		while(negativeInterator.hasNext())
		{
			attributes.remove(negativeInterator.next());
		}
		
		
		attributes.remove(Attributes.getAttributeEnumByGcComId(64));
		attributes.remove(Attributes.getAttributeEnumByGcComId(65));
		attributes.remove(Attributes.getAttributeEnumByGcComId(66));
		
		
		//Teste ob die �brig gebliebenen Atributte auch nicht vergeben wurden.
		Iterator RestInterator = attributes.iterator();
		
		while(RestInterator.hasNext())
		{
			Attributes attr = (Attributes) RestInterator.next();
			assertFalse( attr.toString()+ " Attribut falsch", cache.isAttributePositiveSet( attr ) );
			assertFalse( attr.toString()+ " Attribut falsch", cache.isAttributeNegativeSet( attr ) );
		}
		
		
		// TODO Beschreibungstexte �berpr�fen
		// System.out.println( cache.shortDescription );
		// System.out.println( cache.longDescription );
		
		assertEquals( "Hint falsch", "wenn du ihn nicht findest, findet er dich!!", cache.hint );
		
		
		//TODO Log Test neu schreiben
//		Iterator<LogEntry> logIterator = importHandler.getLogIterator();
//		LogEntry log = logIterator.next();
//		
//		assertEquals( "CacheId ist falsch", log.CacheId, 24578729153020743L );
//		assertEquals( "Id ist falsch", log.Id, 170855167 );
//		assertEquals( "Timestamp falsch", "Mon Jul 04 19:00:00 CEST 2011", log.Timestamp.toString() );
//		assertEquals( "Finder falsch", "SaarFuchs", log.Finder );
//		assertTrue( "LogTyp falsch", log.Type == LogTypes.found );

		// TODO Beschreibungstexte �berpr�fen
		// System.out.println( log.Comment );

		
	}

}
