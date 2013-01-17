package CB_Core.Import;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import CB_Core.GlobalCore;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.Coordinate;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.XMLParserException;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

public class GPXFileImporter
{

	private static SimpleDateFormat datePattern1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
	private static SimpleDateFormat datePattern2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static SimpleDateFormat datePattern3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private File mGpxFile;
	private String mDisplayFilename;
	private IImportHandler mImportHandler;
	private ImporterProgress mip;
	private Integer currentwpt = 0;
	private Integer countwpt = 0;
	private Integer errors = 0;

	private Cache cache = new Cache();
	private Waypoint waypoint = new Waypoint();
	private LogEntry log = new LogEntry();
	private Category category = new Category();
	private GpxFilename gpxFilename = null;

	private String gpxName = "";
	private String gpxAuthor = "";

	public GPXFileImporter(File gpxFileName)
	{
		super();
		mGpxFile = gpxFileName;
		mip = null;
		mDisplayFilename = gpxFileName.getName();
	}

	public GPXFileImporter(File file, ImporterProgress ip)
	{
		super();
		mGpxFile = file;
		mip = ip;
		mDisplayFilename = file.getName();
	}

	public void doImport(IImportHandler importHandler, Integer countwpt) throws Exception
	{
		// http://www.thebuzzmedia.com/software/simple-java-xml-parser-sjxp/

		currentwpt = 0;
		this.countwpt = countwpt;

		mImportHandler = importHandler;

		category = mImportHandler.getCategory(mGpxFile.getAbsolutePath());
		if (category == null) return;

		gpxFilename = mImportHandler.NewGpxFilename(category, mGpxFile.getAbsolutePath());
		if (gpxFilename == null) return;

		Map<String, String> values = new HashMap<String, String>();

		System.setProperty("sjxp.namespaces", "false");

		List<IRule<Map<String, String>>> ruleList = new ArrayList<IRule<Map<String, String>>>();

		ruleList = createGPXRules(ruleList);
		ruleList = createWPTRules(ruleList);
		ruleList = createGroundspeakRules(ruleList);
		ruleList = createGSAKRules(ruleList);

		@SuppressWarnings("unchecked")
		XMLParser<Map<String, String>> parserCache = new XMLParser<Map<String, String>>(ruleList.toArray(new IRule[0]));

		parserCache.parse(new FileInputStream(mGpxFile), values);

		mImportHandler = null;
	}

	private List<IRule<Map<String, String>>> createGPXRules(List<IRule<Map<String, String>>> ruleList) throws Exception
	{

		// Basic GPX Rules

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/name")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				gpxName = text;
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/author")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				gpxAuthor = text;
			}
		});

		return ruleList;
	}

	public static int CacheCount = 0;
	public static int LogCount = 0;

	private List<IRule<Map<String, String>>> createWPTRules(List<IRule<Map<String, String>>> ruleList) throws Exception
	{

		// Basic wpt Rules

		ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt")
		{
			@Override
			public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values)
			{

				if (isStartTag)
				{
					values.clear();
				}
				else
				{
					if (values.get("wpt_type").startsWith("Geocache|"))
					{
						try
						{
							createCache(values);
							CacheCount++;
						}
						catch (Exception e)
						{
							errors++;
							Logger.Error("GPXFileImporter", "CreateCache", e);
						}
					}
					else if (values.get("wpt_type").startsWith("Waypoint|"))
					{
						try
						{
							createWaypoint(values);
						}
						catch (Exception e)
						{
							errors++;
							Logger.Error("GPXFileImporter", "CreateWaypoint", e);
						}

					}
				}

			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt", "lat", "lon")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("wpt_attribute_" + this.getAttributeNames()[index], value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/type")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("wpt_type", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/name")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("wpt_name", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/desc")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("wpt_desc", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cmt")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("wpt_cmt", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/time")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("wpt_time", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/url")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("wpt_url", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/link")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("wpt_link", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/sym")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("wpt_sym", text);
			}
		});

		return ruleList;
	}

	private List<IRule<Map<String, String>>> createGroundspeakRules(List<IRule<Map<String, String>>> ruleList) throws Exception
	{

		// groundspeak:cache Rules for GPX from GC.com

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache", "id", "available", "archived")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_attribute_" + this.getAttributeNames()[index], value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:name")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_name", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:placed_by")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_placed_by", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:owner")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_owner", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:type")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_type", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:container")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_container", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:difficulty")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_difficulty", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:terrain")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_terrain", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:country")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_country", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:state")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_state", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG,
				"/gpx/wpt/groundspeak:cache/groundspeak:attributes/groundspeak:attribute")
		{
			@Override
			public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values)
			{

				if (isStartTag)
				{
					if (values.containsKey("cache_attributes_count")) values.put("cache_attributes_count",
							String.valueOf((Integer.parseInt(values.get("cache_attributes_count")) + 1)));
					else
						values.put("cache_attributes_count", "1");
				}

			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE,
				"/gpx/wpt/groundspeak:cache/groundspeak:attributes/groundspeak:attribute", "id", "inc")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_attribute_" + values.get("cache_attributes_count") + "_" + this.getAttributeNames()[index], value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:short_description")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_short_description", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:short_description",
				"html")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_short_description_html", value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:long_description")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_long_description", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:long_description", "html")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_long_description_html", value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:encoded_hints")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_encoded_hints", text);
			}
		});

		// Log Rules

		ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log")
		{
			@Override
			public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values)
			{

				if (isStartTag)
				{
					if (values.containsKey("cache_logs_count")) values.put("cache_logs_count",
							String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
					else
						values.put("cache_logs_count", "1");
				}

			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log",
				"id")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
			}
		});

		return ruleList;
	}

	private List<IRule<Map<String, String>>> createGSAKRules(List<IRule<Map<String, String>>> ruleList) throws Exception
	{

		// GSAK Rules

		ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/gsak:wptExtension/gsak:LatBeforeCorrect")
		{
			@Override
			public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values)
			{

				if (isStartTag)
				{
					values.put("cache_gsak_corrected_coordinates", "True");
				}

			}
		});

		// Cache Rules for GPX from GSAK

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache", "id", "available",
				"archived")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_attribute_" + this.getAttributeNames()[index], value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:name")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_name", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:placed_by")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_placed_by", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:owner")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_owner", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:type")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_type", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:container")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_container", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:difficulty")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_difficulty", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:terrain")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_terrain", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:country")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_country", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:state")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_state", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute")
		{
			@Override
			public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values)
			{

				if (isStartTag)
				{
					if (values.containsKey("cache_attributes_count")) values.put("cache_attributes_count",
							String.valueOf((Integer.parseInt(values.get("cache_attributes_count")) + 1)));
					else
						values.put("cache_attributes_count", "1");
				}

			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute", "id", "inc")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_attribute_" + values.get("cache_attributes_count") + "_" + this.getAttributeNames()[index], value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_short_description", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description", "html")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_short_description_html", value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_long_description", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description", "html")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_long_description_html", value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:encoded_hints")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_encoded_hints", text);
			}
		});

		// Log Rules

		ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log")
		{
			@Override
			public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values)
			{

				if (isStartTag)
				{
					if (values.containsKey("cache_logs_count")) values.put("cache_logs_count",
							String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
					else
						values.put("cache_logs_count", "1");
				}

			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log", "id")
		{
			@Override
			public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values)
			{

				values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
			}
		});

		ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER,
				"/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text")
		{
			@Override
			public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values)
			{
				values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
			}
		});

		return ruleList;
	}

	private void createCache(Map<String, String> values) throws Exception
	{

		if (gpxAuthor.toLowerCase().contains("gctour"))
		{
			cache.TourName = gpxName;
		}

		if (values.containsKey("wpt_attribute_lat") && values.containsKey("wpt_attribute_lon"))
		{
			cache.Pos = new Coordinate(new Double(values.get("wpt_attribute_lat")).doubleValue(), new Double(
					values.get("wpt_attribute_lon")).doubleValue());
		}

		if (values.containsKey("wpt_name"))
		{
			cache.GcCode = values.get("wpt_name");
			cache.Id = Cache.GenerateCacheId(cache.GcCode);
		}

		if (values.containsKey("wpt_time"))
		{
			cache.DateHidden = parseDate(values.get("wpt_time"));
		}

		if (values.containsKey("wpt_url"))
		{
			cache.Url = values.get("wpt_url");
		}

		if (values.containsKey("wpt_sym"))
		{
			cache.Found = values.get("wpt_sym").equalsIgnoreCase("Geocache Found");
		}

		if (values.containsKey("cache_attribute_id"))
		{

			cache.GcId = values.get("cache_attribute_id");
		}

		if (values.containsKey("cache_attribute_available"))
		{
			if (values.get("cache_attribute_available").equalsIgnoreCase("True"))
			{
				cache.Available = true;
			}
			else
			{
				cache.Available = false;
			}
		}

		if (values.containsKey("cache_attribute_archived"))
		{
			if (values.get("cache_attribute_archived").equalsIgnoreCase("True"))
			{
				cache.Archived = true;
			}
			else
			{
				cache.Archived = false;
			}
		}

		if (values.containsKey("cache_name"))
		{
			cache.Name = values.get("cache_name");
		}

		if (values.containsKey("cache_placed_by"))
		{
			cache.PlacedBy = values.get("cache_placed_by");
		}

		if (values.containsKey("cache_owner"))
		{
			cache.Owner = values.get("cache_owner");
		}

		if (values.containsKey("cache_type"))
		{
			cache.Type = CacheTypes.parseString(values.get("cache_type"));
		}

		if (values.containsKey("cache_container"))
		{
			cache.Size = CacheSizes.parseString(values.get("cache_container"));
		}

		if (values.containsKey("cache_difficulty"))
		{
			cache.Difficulty = Float.parseFloat(values.get("cache_difficulty"));
		}

		if (values.containsKey("cache_terrain"))
		{
			cache.Terrain = Float.parseFloat(values.get("cache_terrain"));
		}

		if (values.containsKey("cache_country"))
		{
			cache.Country = values.get("cache_country");
		}

		if (values.containsKey("cache_state"))
		{
			cache.State = values.get("cache_state");
		}

		if (values.containsKey("cache_attributes_count"))
		{
			int count = Integer.parseInt(values.get("cache_attributes_count"));

			for (int i = 1; i <= count; i++)
			{
				int attrGcComId = -1;
				int attrGcComVal = -1;

				attrGcComId = Integer.parseInt(values.get("cache_attribute_" + String.valueOf(i) + "_id"));
				try
				{
					attrGcComVal = Integer.parseInt(values.get("cache_attribute_" + String.valueOf(i) + "_inc"));
				}
				catch (Exception ex)
				{
					// if there is no given value "inc" in attribute definition this should be = 1 (gccapp gpx files)
					attrGcComVal = 1;
				}

				if (attrGcComId > 0 && attrGcComVal != -1)
				{
					if (attrGcComVal > 0)
					{
						cache.addAttributePositive(Attributes.getAttributeEnumByGcComId(attrGcComId));
					}
					else
					{
						cache.addAttributeNegative(Attributes.getAttributeEnumByGcComId(attrGcComId));
					}
				}
			}

		}

		if (values.containsKey("cache_short_description"))
		{
			cache.shortDescription = values.get("cache_short_description").trim();

			if (values.containsKey("cache_short_description_html") && values.get("cache_short_description_html").equalsIgnoreCase("False"))
			{
				cache.shortDescription = cache.shortDescription.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
			}
		}

		if (values.containsKey("cache_long_description"))
		{
			cache.longDescription = values.get("cache_long_description").trim();

			if (values.containsKey("cache_long_description_html") && values.get("cache_long_description_html").equalsIgnoreCase("False"))
			{
				cache.longDescription = cache.longDescription.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
			}
		}

		if (values.containsKey("cache_encoded_hints"))
		{
			cache.hint = values.get("cache_encoded_hints");
		}

		if (values.containsKey("cache_logs_count"))
		{
			int count = Integer.parseInt(values.get("cache_logs_count"));

			for (int i = 1; i <= count; i++)
			{
				log.CacheId = cache.Id;
				String attrValue = values.get("cache_log_" + String.valueOf(i) + "_id");
				if (attrValue != null)
				{
					try
					{
						log.Id = Long.parseLong(attrValue);
					}
					catch (Exception ex)
					{
						// Cache ID konnte nicht als Zahl interpretiert werden -> in eine eindeutige Zahl wandeln
						log.Id = Cache.GenerateCacheId(attrValue);
					}
				}

				if (values.containsKey("cache_log_" + String.valueOf(i) + "_date"))
				{
					log.Timestamp = parseDate(values.get("cache_log_" + String.valueOf(i) + "_date"));
				}

				if (values.containsKey("cache_log_" + String.valueOf(i) + "_finder"))
				{
					log.Finder = values.get("cache_log_" + String.valueOf(i) + "_finder");
				}

				if (values.containsKey("cache_log_" + String.valueOf(i) + "_text"))
				{
					log.Comment = values.get("cache_log_" + String.valueOf(i) + "_text");
				}

				if (values.containsKey("cache_log_" + String.valueOf(i) + "_type"))
				{
					log.Type = LogTypes.parseString(values.get("cache_log_" + String.valueOf(i) + "_type"));
				}

				if (log != null)
				{
					LogCount++;
					mImportHandler.handleLog(log);
				}

				log.clear();
			}

		}

		if (values.containsKey("cache_gsak_corrected_coordinates"))
		{
			if (values.get("cache_gsak_corrected_coordinates").equalsIgnoreCase("True"))
			{
				cache.setCorrectedCoordinates(true);
			}
		}

		cache.GPXFilename_ID = gpxFilename.Id;

		currentwpt++;

		StringBuilder info = new StringBuilder();

		info.append(mDisplayFilename);
		info.append(GlobalCore.br);
		info.append("Cache: ");
		info.append(currentwpt);
		info.append("/");
		info.append(countwpt);

		if (errors > 0)
		{
			info.append(GlobalCore.br);
			info.append("Errors: ");
			info.append(errors);
		}

		if (mip != null) mip.ProgressInkrement("ImportGPX", info.toString(), false);

		mImportHandler.handleCache(cache);

		// Merge mit cache info
		if (CacheInfoList.ExistCache(cache.GcCode))
		{
			CacheInfoList.mergeCacheInfo(cache);
		}
		else
		{
			// Neue CacheInfo erstellen und zur Liste Hinzufügen
			CacheInfoList.putNewInfo(cache);
		}

		cache.clear();

	}

	private void createWaypoint(Map<String, String> values) throws Exception
	{
		if (values.containsKey("wpt_attribute_lat") && values.containsKey("wpt_attribute_lon"))
		{
			waypoint.Pos = new Coordinate(new Double(values.get("wpt_attribute_lat")).doubleValue(), new Double(
					values.get("wpt_attribute_lon")).doubleValue());
		}
		else
		{
			waypoint.Pos = new Coordinate();
		}

		if (values.containsKey("wpt_name"))
		{
			waypoint.GcCode = values.get("wpt_name");
			waypoint.Title = waypoint.GcCode;
			// TODO Hack to get parent Cache
			waypoint.CacheId = Cache.GenerateCacheId("GC" + waypoint.GcCode.substring(2, waypoint.GcCode.length()));
		}

		if (values.containsKey("wpt_desc"))
		{
			waypoint.Description = values.get("wpt_desc");
		}

		if (values.containsKey("wpt_type"))
		{
			waypoint.parseTypeString(values.get("wpt_type"));
		}

		if (values.containsKey("wpt_cmt"))
		{
			waypoint.Clue = values.get("wpt_cmt");
		}

		currentwpt++;
		if (mip != null) mip.ProgressInkrement("ImportGPX", mDisplayFilename + "\nWaypoint: " + currentwpt + "/" + countwpt + "\n"
				+ waypoint.GcCode + " - " + waypoint.Description, false);

		mImportHandler.handleWaypoint(waypoint);

		waypoint.clear();

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
					throw new XMLParserException("Illegal date format");
				}
			}
		}
	}

	private static Date parseDateWithFormat(SimpleDateFormat df, String text) throws Exception
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

}
