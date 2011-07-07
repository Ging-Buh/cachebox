package CB_Core.Import;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.LogEntry;

public class GPXFileImporter {
	
	private static SimpleDateFormat datePattern1 = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.S'Z'" );
	private static SimpleDateFormat datePattern2 = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );

	private String mGpxFileName;
	private IImportHandler mImportHandler; 
	
	public GPXFileImporter(String gpxFileName) {
		super();
		mGpxFileName = gpxFileName;
	}
	
	public void doImport( IImportHandler importHandler ) throws Exception {
		mImportHandler = importHandler;
		KXmlParser parser = new KXmlParser();
		Reader fr = new InputStreamReader( new FileInputStream(mGpxFileName), "UTF-8" );
        parser.setInput(fr);  

        // hier wird über die Elemente der ersten Ebene iteriert - sollten nur wpt-Elemente sein
        int eventType = parser.getEventType();
        boolean done = false;
        while (eventType != XmlPullParser.END_DOCUMENT && !done){
            String tagName = parser.getName();
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if (tagName.equalsIgnoreCase( "gpx" ) ){
                    	// TODO GPX-Elemente noch bearbeiten?
                    } else if (tagName.equalsIgnoreCase( "wpt" ) ){
                    	Cache cache = parseWptElement( parser );
                    	mImportHandler.handleCache( cache );
                    }
                    break;
                case XmlPullParser.END_TAG:
    		        if (tagName.equalsIgnoreCase( "gpx" ) ){
    		        	done = true;
    		        }
    		        break;
            }
        	eventType = parser.next();
        }
        
        mImportHandler = null;
    }

	private Cache parseWptElement( KXmlParser parser ) throws Exception {
		// Hier wird ein wpt-Element aufgebaut

		Cache cache = new Cache();
		parseWptAttributes(parser, cache);
		
		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done){
            String tagName = parser.getName();
			switch (eventType){
			    case XmlPullParser.START_TAG:
			        if (tagName.equalsIgnoreCase( "name" ) ){
			        	cache.GcCode = parser.nextText();
			        	cache.Id = Cache.GenerateCacheId( cache.GcCode );
			        } else if (tagName.equalsIgnoreCase( "time" ) ){
			        	parseWptTimeElement(parser, cache);
			        } else if (tagName.equalsIgnoreCase( "url" ) ){
			        	cache.Url = parser.nextText();
			        } else if (tagName.equalsIgnoreCase( "sym" ) ){
			        	cache.Found = parser.nextText().equalsIgnoreCase( "Geocache Found" );
			        } else if (tagName.equalsIgnoreCase( "groundspeak:cache" ) ){
			        	parseWptCacheElement( parser, cache );
			        } else {
			        	skipUntilEndTag(parser, tagName);
			        }
			        break;
			    case XmlPullParser.END_TAG:
			        if (tagName.equalsIgnoreCase( "wpt" ) ){
			        	done = true;
			        }
			        break;
		    }
			eventType = parser.next();
		}
		
		return cache;
	}

	private void parseWptCacheElement( KXmlParser parser, Cache cache ) throws Exception {
		// Hier wird ein Groundspeak-Cache Element aufgebaut
		
		parseWptCacheAttributes(parser, cache);
		
		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done){
            String tagName = parser.getName();
			switch (eventType){
			    case XmlPullParser.START_TAG:
			        if (tagName.equalsIgnoreCase( "groundspeak:name" ) ){
			        	cache.Name = parser.nextText();
			        } else if (tagName.equalsIgnoreCase( "groundspeak:placed_by" ) ){
			        	cache.PlacedBy = parser.nextText();
			        } else if (tagName.equalsIgnoreCase( "groundspeak:owner" ) ){
			        	cache.Owner = parser.nextText();
			        } else if (tagName.equalsIgnoreCase( "groundspeak:type" ) ){
			        	cache.Type = CacheTypes.parseString( parser.nextText() );
			        } else if (tagName.equalsIgnoreCase( "groundspeak:container" ) ){
			        	cache.Size = CacheSizes.parseString( parser.nextText() );
			        } else if (tagName.equalsIgnoreCase( "groundspeak:difficulty" ) ){
			        	cache.Difficulty = Float.parseFloat( parser.nextText() );
			        } else if (tagName.equalsIgnoreCase( "groundspeak:terrain" ) ){
			        	cache.Terrain = Float.parseFloat( parser.nextText() );
			        } else if (tagName.equalsIgnoreCase( "groundspeak:short_description" ) ){
			        	// TODO im nicht HTML-Fall die Zeilenumbrüche ersetzen
			        	cache.shortDescription = parser.nextText();
			        } else if (tagName.equalsIgnoreCase( "groundspeak:long_description" ) ){
			        	// TODO im nicht HTML-Fall die Zeilenumbrüche ersetzen
			        	cache.longDescription = parser.nextText();
			        } else if (tagName.equalsIgnoreCase( "groundspeak:encoded_hints" ) ){
			    		cache.hint = parser.nextText();
			        } else if (tagName.equalsIgnoreCase( "groundspeak:logs" ) ){
			        	parseWptCacheLogsElement(parser, cache);
			        } else {
			        	skipUntilEndTag(parser, tagName);
			        }
			        break;
			    case XmlPullParser.END_TAG:
			        if (tagName.equalsIgnoreCase( "groundspeak:cache" ) ){
			        	done = true;
			        }
			        break;
		    }
			eventType = parser.next();
		}
	}

	private void parseWptCacheLogsElement(KXmlParser parser, Cache cache) throws Exception {
		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done){
            String tagName = parser.getName();
			switch (eventType){
			    case XmlPullParser.START_TAG:
			        if (tagName.equalsIgnoreCase( "groundspeak:log" ) ){
			        	LogEntry log = parseWptCacheLogsLogElement(parser, cache);
			        } else {
			        	skipUntilEndTag(parser, tagName);
			        }
			        break;
			    case XmlPullParser.END_TAG:
			        if (tagName.equalsIgnoreCase( "groundspeak:logs" ) ){
			        	done = true;
			        }
			        break;
		    }
			eventType = parser.next();
		}
	}

	private LogEntry parseWptCacheLogsLogElement(KXmlParser parser, Cache cache) throws Exception {
		LogEntry log = new LogEntry();
		
		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done){
            String tagName = parser.getName();
			switch (eventType){
			    case XmlPullParser.START_TAG:
			        if (tagName.equalsIgnoreCase( "xxgroundspeak:log" ) ){
			        	//
			        } else {
			        	skipUntilEndTag(parser, tagName);
			        }
			        break;
			    case XmlPullParser.END_TAG:
			        if (tagName.equalsIgnoreCase( "groundspeak:log" ) ){
			        	mImportHandler.handleLog( log );
			        	done = true;
			        }
			        break;
		    }
			eventType = parser.next();
		}
		
		return log;
	}

	private void skipUntilEndTag(KXmlParser parser, String tagName)
			throws XmlPullParserException, IOException {
		while( true ) {
			if( parser.next() == XmlPullParser.END_TAG ) {
				if( parser.getName().equalsIgnoreCase( tagName ) ) {
					break;
				}
			}
		}
	}

	private void parseWptCacheAttributes(KXmlParser parser, Cache cache) {
		int attributeCount = parser.getAttributeCount();
		for( int i=0; i<attributeCount; ++i ) {
			if( parser.getAttributeName( i ).equalsIgnoreCase( "id" ) ) {
				cache.GcId = parser.getAttributeValue( i );
			}
			else if( parser.getAttributeName( i ).equalsIgnoreCase( "available" ) ) {
				if( parser.getAttributeValue( i ).equalsIgnoreCase( "True" ) ) {
					cache.Available = true;
				}
				else {
					cache.Available = false;
				}
			}
			else if( parser.getAttributeName( i ).equalsIgnoreCase( "archived" ) ) {
				if( parser.getAttributeValue( i ).equalsIgnoreCase( "True" ) ) {
					cache.Archived = true;
				}
				else {
					cache.Archived = false;
				}
			}
		}
	}

	
	private void parseWptTimeElement(KXmlParser parser, Cache cache) throws Exception {
		String text = parser.nextText();
		Date date = parseDate(datePattern1, text);
		if( date != null ) {
			cache.DateHidden = date;
		} else {
			date = parseDate(datePattern2, text);
			if( date != null ) {
				cache.DateHidden = date;
			}
		}
	}

	private Date parseDate(SimpleDateFormat df, String text) throws Exception {
		// TODO hier müsste mal über die Zeitzone nachgedacht werden - 
		// irgendwas ist an den Daten, die von GC.com kommen, komisch 
		Date date = null;
		try {
			date = df.parse( text );
		}
		catch( ParseException e ) {
		}
		return date;
	}

	private void parseWptAttributes(KXmlParser parser, Cache cache) {
		Double lat = null;
		Double lon = null;
		int attributeCount = parser.getAttributeCount();
		for( int i=0; i<attributeCount; ++i ) {
			if( parser.getAttributeName( i ).equalsIgnoreCase( "lat" ) ) {
				lat = new Double( parser.getAttributeValue( i ) );
			}
			else if( parser.getAttributeName( i ).equalsIgnoreCase( "lon" ) ) {
				lon = new Double( parser.getAttributeValue( i ) );
			}
		}
		if( lat!=null && lon !=null ) {
			cache.Pos = new Coordinate( lat.doubleValue(), lon.doubleValue() );
		}
	}

}
