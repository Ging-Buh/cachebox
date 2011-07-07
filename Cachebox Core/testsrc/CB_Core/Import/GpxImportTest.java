package CB_Core.Import;

import java.util.Iterator;

import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import junit.framework.TestCase;

public class GpxImportTest extends TestCase {
	
	public void testGpxImport() throws Exception
	{
		ImportHandler importHandler = new ImportHandler();
		GPXFileImporter importer = new GPXFileImporter( "./testdata/gpx/GC2T9RW.gpx" );
		assertTrue( "Objekt muss konstruierbar sein", importer!=null );
		importer.doImport( importHandler );
		
		Iterator<Cache> cacheIterator = importHandler.getCacheIterator();
		Cache cache = cacheIterator.next();
		
		assertTrue( "Cache muss zurückgegeben werden", cache!=null );
		
		assertTrue( "Pos: Latitude falsch", cache.Pos.Latitude == 49.349817 );
		assertTrue( "Pos: Longitude falsch", cache.Pos.Longitude == 8.62925 );
		assertTrue( "Pos ist ungültig", cache.Pos.Valid );
		
		assertEquals( "GcCode falsch", "GC2T9RW", cache.GcCode );
		assertEquals( "DateHidden falsch", "Sat Apr 16 07:00:00 CEST 2011", cache.DateHidden.toString() );
		assertEquals( "url falsch", "http://www.geocaching.com/seek/cache_details.aspx?guid=f26f18bd-9aaa-4499-944b-3e8cb62e41a7", cache.Url );

		assertTrue( "Id ist falsch", cache.Id == 2190117 );
		assertTrue( "ist available ist falsch", cache.Available );
		assertFalse( "ist archived ist falsch", cache.Archived );
		assertEquals( "Name falsch", "der Hampir - T5 - ", cache.Name );
		assertEquals( "Placed by falsch", "Team Rabbits", cache.PlacedBy );
		assertEquals( "Owner falsch", "Team Rabbits", cache.Owner );
		assertTrue( "Typ ist falsch", cache.Type == CacheTypes.Traditional );
		assertTrue( "Size ist falsch", cache.Size == CacheSizes.small );
		assertTrue( "Difficulty ist falsch", cache.Difficulty == 2 );
		assertTrue( "Terrain", cache.Terrain == 5 );
		
		// TODO Beschreibungstexte überprüfen
		// System.out.println( cache.shortDescription );
		// System.out.println( cache.longDescription );
		
		assertEquals( "Hint falsch", "wenn du ihn nicht findest, findet er dich!!", cache.hint );
		
		Iterator<LogEntry> logIterator = importHandler.getLogIterator();
		LogEntry log = logIterator.next();
		
		
	}

}
