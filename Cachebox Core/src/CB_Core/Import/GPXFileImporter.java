package CB_Core.Import;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public class GPXFileImporter
{

	private static SimpleDateFormat datePattern1 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.S");
	private static SimpleDateFormat datePattern2 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static SimpleDateFormat datePattern3 = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");

	private String mGpxFileName;
	private IImportHandler mImportHandler;
	private ImporterProgress mip;

	public GPXFileImporter(String gpxFileName)
	{
		super();
		mGpxFileName = gpxFileName;
		mip = null;
	}

	public GPXFileImporter(String gpxFileName, ImporterProgress ip)
	{
		super();
		mGpxFileName = gpxFileName;
		mip = ip;
	}

	public void doImport(IImportHandler importHandler, Integer countwpt ) throws Exception
	{
		mImportHandler = importHandler;
		KXmlParser parser = new KXmlParser();
		Reader fr = new InputStreamReader(new FileInputStream(mGpxFileName),
				"UTF-8");

		BufferedReader br = new BufferedReader(fr);

		String strLine;
		String section = "";
		Integer currentwpt = 0;
	
		while ((strLine = br.readLine()) != null)
		{
			if (strLine.contains("</wpt>"))
			{
				section = section + strLine;

				parser.setInput(new StringReader(section));

				// hier wird über die Elemente der ersten Ebene iteriert -
				// sollten nur wpt-Elemente sein
				int eventType = parser.getEventType();

				boolean done = false;

				while (eventType != XmlPullParser.END_DOCUMENT && !done)
				{
					String tagName = parser.getName();
					switch (eventType)
					{
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:

						if (tagName.equalsIgnoreCase("gpx"))
						{
							// TODO GPX-Elemente noch bearbeiten?
						}
						else if (tagName.equalsIgnoreCase("wpt"))
						{

							// TODO check if this is a real Cache or Additional
							// Waypoint

							if (section.contains("<type>Geocache|"))
							{
								Cache cache = parseWptCacheElement(parser);

								// TODO at Cache GPXFileCategorie and last
								// Import Info

								currentwpt++;
								
								if (mip != null) mip.ProgressInkrement(
										"ImportGPX", "Cache: " + currentwpt + "/" + countwpt + " - " + cache.GcCode);

								mImportHandler.handleCache(cache);
							}
							else if (section.contains("<type>Waypoint|"))
							{
								Waypoint waypoint = parseWptAdditionalWaypointElement(parser);

								currentwpt++;
								
								if (mip != null) mip.ProgressInkrement(
										"ImportGPX", "Waypoint: " + currentwpt + "/" + countwpt + " - " + waypoint.GcCode);

								mImportHandler.handleWaypoint(waypoint);
							}
							else
							{
								// Should not happen
							}

						}
						break;
					case XmlPullParser.END_TAG:
						if (tagName.equalsIgnoreCase("gpx"))
						{
							done = true;
						}
						break;
					}
					eventType = parser.next();
				}

				section = "";
			}
			else
			{
				section = section + strLine;
			}
		}

		// parser.setInput(fr);

		mImportHandler = null;
	}

	private Cache parseWptCacheElement(KXmlParser parser) throws Exception
	{
		// Hier wird ein wpt-Element aufgebaut

		Cache cache = new Cache();
		parseWptAttributes(parser, cache);

		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done)
		{
			String tagName = parser.getName();
			switch (eventType)
			{
			case XmlPullParser.START_TAG:
				if (tagName.equalsIgnoreCase("name"))
				{
					cache.GcCode = parser.nextText();
					cache.Id = Cache.GenerateCacheId(cache.GcCode);
				}
				else if (tagName.equalsIgnoreCase("time"))
				{
					cache.DateHidden = parseDate(parser.nextText());
				}
				else if (tagName.equalsIgnoreCase("url"))
				{
					cache.Url = parser.nextText();
				}
				else if (tagName.equalsIgnoreCase("link"))
				{
					cache.Url = "http"; // irgendwie komme ich nicht an die URL
										// ran
				}
				else if (tagName.equalsIgnoreCase("sym"))
				{
					cache.Found = parser.nextText().equalsIgnoreCase(
							"Geocache Found");
				}
				else if (tagName.equalsIgnoreCase("groundspeak:cache"))
				{
					parseWptCacheElement(parser, cache);
				}
				else if (tagName.equalsIgnoreCase("extensions"))
				{
					parseExtensionsCacheElement(parser, cache);
				}
				else
				{
					skipUntilEndTag(parser, tagName);
				}
				break;
			case XmlPullParser.END_TAG:
				if (tagName.equalsIgnoreCase("wpt"))
				{
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}

		return cache;
	}

	private Waypoint parseWptAdditionalWaypointElement(KXmlParser parser)
			throws Exception
	{

		Waypoint waypoint = new Waypoint();

		parseWptAdditionalWaypointsAttributes(parser, waypoint);

		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done)
		{
			String tagName = parser.getName();
			switch (eventType)
			{
			case XmlPullParser.START_TAG:
				if (tagName.equalsIgnoreCase("name"))
				{
					waypoint.GcCode = parser.nextText();
					waypoint.Title = waypoint.GcCode;
					// TODO Hack to get parent Cache
					waypoint.CacheId = Cache.GenerateCacheId("GC"
							+ waypoint.GcCode.substring(2,
									waypoint.GcCode.length()));
				}
				else if (tagName.equalsIgnoreCase("desc"))
				{
					waypoint.Description = parser.nextText();
				}
				else if (tagName.equalsIgnoreCase("type"))
				{
					waypoint.parseTypeString(parser.nextText());
				}
				else if (tagName.equalsIgnoreCase("cmt"))
				{
					waypoint.Clue = parser.nextText();
				}
				break;
			case XmlPullParser.END_TAG:
				if (tagName.equalsIgnoreCase("wpt"))
				{
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}

		return waypoint;
	}

	private void parseExtensionsCacheElement(KXmlParser parser, Cache cache)
			throws Exception
	{

		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done)
		{
			String tagName = parser.getName();
			switch (eventType)
			{
			case XmlPullParser.START_TAG:
			{
				if (tagName.equalsIgnoreCase("groundspeak:cache"))
				{
					parseWptCacheElement(parser, cache);
				}
				else if (tagName.equalsIgnoreCase("gsak:wptExtension"))
				{
					parseGSAKCacheElement(parser, cache);
				}
			}
				break;
			case XmlPullParser.END_TAG:
				if (tagName.equalsIgnoreCase("extensions"))
				{
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}

	}

	private void parseGSAKCacheElement(KXmlParser parser, Cache cache)
			throws Exception
	{
		// bei GSAk gehts eine Ebene tiefer weiter

		// parser.nextTag();
		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done)
		{
			String tagName = parser.getName();
			switch (eventType)
			{
			case XmlPullParser.START_TAG:
			{
				if (tagName.equalsIgnoreCase("gsak:LatBeforeCorrect"))
				{
					cache.CorrectedCoordinates = true;
				}
			}
				break;
			case XmlPullParser.END_TAG:
				if (tagName.equalsIgnoreCase("gsak:wptExtension"))
				{
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}

	}

	private void parseWptCacheElement(KXmlParser parser, Cache cache)
			throws Exception
	{
		// Hier wird ein Groundspeak-Cache Element aufgebaut

		parseWptCacheAttributes(parser, cache);

		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done)
		{
			String tagName = parser.getName();
			switch (eventType)
			{
			case XmlPullParser.START_TAG:
				if (tagName.equalsIgnoreCase("groundspeak:name"))
				{
					cache.Name = parser.nextText();
				}
				else if (tagName.equalsIgnoreCase("groundspeak:placed_by"))
				{
					cache.PlacedBy = parser.nextText();
				}
				else if (tagName.equalsIgnoreCase("groundspeak:owner"))
				{
					cache.Owner = parser.nextText();
				}
				else if (tagName.equalsIgnoreCase("groundspeak:type"))
				{
					cache.Type = CacheTypes.parseString(parser.nextText());
				}
				else if (tagName.equalsIgnoreCase("groundspeak:container"))
				{
					cache.Size = CacheSizes.parseString(parser.nextText());
				}
				else if (tagName.equalsIgnoreCase("groundspeak:difficulty"))
				{
					cache.Difficulty = Float.parseFloat(parser.nextText());
				}
				else if (tagName.equalsIgnoreCase("groundspeak:terrain"))
				{
					cache.Terrain = Float.parseFloat(parser.nextText());
				}
				else if (tagName.equalsIgnoreCase("groundspeak:attributes"))
				{
					parseWptCacheAttributesElement(parser, cache);
				}
				else if (tagName
						.equalsIgnoreCase("groundspeak:short_description"))
				{
					// TODO im nicht HTML-Fall die Zeilenumbrüche ersetzen
					cache.shortDescription = parser.nextText();
				}
				else if (tagName
						.equalsIgnoreCase("groundspeak:long_description"))
				{
					// TODO im nicht HTML-Fall die Zeilenumbrüche ersetzen
					cache.longDescription = parser.nextText();
				}
				else if (tagName.equalsIgnoreCase("groundspeak:encoded_hints"))
				{
					cache.hint = parser.nextText();
				}
				else if (tagName.equalsIgnoreCase("groundspeak:logs"))
				{
					parseWptCacheLogsElement(parser, cache);
				}
				else
				{
					skipUntilEndTag(parser, tagName);
				}
				break;
			case XmlPullParser.END_TAG:
				if (tagName.equalsIgnoreCase("groundspeak:cache"))
				{
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}
	}

	private void parseWptCacheAttributesElement(KXmlParser parser, Cache cache)
			throws Exception
	{
		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done)
		{
			String tagName = parser.getName();
			switch (eventType)
			{
			case XmlPullParser.START_TAG:
				if (tagName.equalsIgnoreCase("groundspeak:attribute"))
				{
					int attrGcComId = -1;
					int attrGcComVal = -1;
					if (parser.getAttributeCount() == 2)
					{
						if (parser.getAttributeName(0).equalsIgnoreCase("id"))
						{
							attrGcComId = Integer.parseInt(parser
									.getAttributeValue(0));
						}
						if (parser.getAttributeName(1).equalsIgnoreCase("inc"))
						{
							attrGcComVal = Integer.parseInt(parser
									.getAttributeValue(1));
						}
					}
					if (attrGcComId > 0 && attrGcComVal != -1)
					{
						if (attrGcComVal > 0)
						{
							cache.addAttributePositive(Attributes
									.getAttributeEnumByGcComId(attrGcComId));
						}
						else
						{
							cache.addAttributeNegative(Attributes
									.getAttributeEnumByGcComId(attrGcComId));
						}
					}
				}
				else
				{
					skipUntilEndTag(parser, tagName);
				}
				break;
			case XmlPullParser.END_TAG:
				if (tagName.equalsIgnoreCase("groundspeak:attributes"))
				{
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}
	}

	private void parseWptCacheLogsElement(KXmlParser parser, Cache cache)
			throws Exception
	{
		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done)
		{
			String tagName = parser.getName();
			switch (eventType)
			{
			case XmlPullParser.START_TAG:
				if (tagName.equalsIgnoreCase("groundspeak:log"))
				{
					LogEntry log = parseWptCacheLogsLogElement(parser, cache);
					if (log != null)
					{
						mImportHandler.handleLog(log);
					}
				}
				else
				{
					skipUntilEndTag(parser, tagName);
				}
				break;
			case XmlPullParser.END_TAG:
				if (tagName.equalsIgnoreCase("groundspeak:logs"))
				{
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}
	}

	private LogEntry parseWptCacheLogsLogElement(KXmlParser parser, Cache cache)
			throws Exception
	{
		LogEntry log = new LogEntry();
		log.CacheId = cache.Id;
		String attrValue = getAttributeValueFromParser(parser, "id");
		if (attrValue != null)
		{
			log.Id = Long.parseLong(attrValue);
		}
		boolean done = false;
		int eventType = parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT && !done)
		{
			String tagName = parser.getName();
			switch (eventType)
			{
			case XmlPullParser.START_TAG:
				if (tagName.equalsIgnoreCase("groundspeak:date"))
				{
					log.Timestamp = parseDate(parser.nextText());
				}
				else if (tagName.equalsIgnoreCase("groundspeak:finder"))
				{
					log.Finder = parser.nextText();
				}
				else if (tagName.equalsIgnoreCase("groundspeak:text"))
				{
					log.Comment = parser.nextText();
				}
				else if (tagName.equalsIgnoreCase("groundspeak:type"))
				{
					log.Type = LogTypes.parseString(parser.nextText());
				}
				else
				{
					skipUntilEndTag(parser, tagName);
				}
				break;
			case XmlPullParser.END_TAG:
				if (tagName.equalsIgnoreCase("groundspeak:log"))
				{
					mImportHandler.handleLog(log);
					done = true;
				}
				break;
			}
			eventType = parser.next();
		}

		return log;
	}

	private String getAttributeValueFromParser(KXmlParser parser,
			String attrName)
	{
		String attrValue = null;
		for (int i = 0; i < parser.getAttributeCount(); ++i)
		{
			if (parser.getAttributeName(i).equalsIgnoreCase(attrName))
			{
				attrValue = parser.getAttributeValue(i);
			}
		}
		return attrValue;
	}

	private void skipUntilEndTag(KXmlParser parser, String tagName)
			throws Exception
	{
		while (true)
		{
			if (parser.next() == XmlPullParser.END_TAG)
			{
				if (parser.getName().equalsIgnoreCase(tagName))
				{
					break;
				}
			}
		}
	}

	private void parseWptCacheAttributes(KXmlParser parser, Cache cache)
	{
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; ++i)
		{
			if (parser.getAttributeName(i).equalsIgnoreCase("id"))
			{
				cache.GcId = parser.getAttributeValue(i);
			}
			else if (parser.getAttributeName(i).equalsIgnoreCase("available"))
			{
				if (parser.getAttributeValue(i).equalsIgnoreCase("True"))
				{
					cache.Available = true;
				}
				else
				{
					cache.Available = false;
				}
			}
			else if (parser.getAttributeName(i).equalsIgnoreCase("archived"))
			{
				if (parser.getAttributeValue(i).equalsIgnoreCase("True"))
				{
					cache.Archived = true;
				}
				else
				{
					cache.Archived = false;
				}
			}
		}
	}

	private static Date parseDate(String text) throws Exception
	{
		Date date = parseDateWithFormat(datePattern1, text);
		if (date != null)
		{
			return date;
		}
		else
		{
			date = parseDateWithFormat(datePattern2, text);
			if (date != null)
			{
				return date;
			}
			else
			{
				date = parseDateWithFormat(datePattern3, text);
				if (date != null)
				{
					return date;
				}
				else
				{
					throw new XmlPullParserException("Illegal date format");
				}
			}
		}
	}

	private static Date parseDateWithFormat(SimpleDateFormat df, String text)
			throws Exception
	{
		// TODO hier müsste mal über die Zeitzone nachgedacht werden -
		// irgendwas ist an den Daten, die von GC.com kommen, komisch
		Date date = null;
		try
		{
			date = df.parse(text);
		}
		catch (ParseException e)
		{
		}
		return date;
	}

	private void parseWptAttributes(KXmlParser parser, Cache cache)
	{
		Double lat = null;
		Double lon = null;
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; ++i)
		{
			if (parser.getAttributeName(i).equalsIgnoreCase("lat"))
			{
				lat = new Double(parser.getAttributeValue(i));
			}
			else if (parser.getAttributeName(i).equalsIgnoreCase("lon"))
			{
				lon = new Double(parser.getAttributeValue(i));
			}
		}
		if (lat != null && lon != null)
		{
			cache.Pos = new Coordinate(lat.doubleValue(), lon.doubleValue());
		}
	}

	private void parseWptAdditionalWaypointsAttributes(KXmlParser parser,
			Waypoint waypoint)
	{
		Double lat = null;
		Double lon = null;
		int attributeCount = parser.getAttributeCount();
		for (int i = 0; i < attributeCount; ++i)
		{
			if (parser.getAttributeName(i).equalsIgnoreCase("lat"))
			{
				lat = new Double(parser.getAttributeValue(i));
			}
			else if (parser.getAttributeName(i).equalsIgnoreCase("lon"))
			{
				lon = new Double(parser.getAttributeValue(i));
			}
		}
		if (lat != null && lon != null)
		{
			waypoint.Pos = new Coordinate(lat.doubleValue(), lon.doubleValue());
		}
		else
		{
			waypoint.Pos = new Coordinate();
		}
	}

}
