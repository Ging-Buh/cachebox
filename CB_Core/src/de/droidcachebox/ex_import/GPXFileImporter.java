/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.ex_import;

import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.XMLParserException;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.droidcachebox.core.CoreData;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.LogsTableDAO;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.dataclasses.Attribute;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Categories;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.dataclasses.GeoCacheSize;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.GpxFilename;
import de.droidcachebox.dataclasses.LogEntry;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.settings.AllSettings;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.TestCancel;
import de.droidcachebox.utils.log.Log;

/**
 * imports one gpx-file (given as AbstractFile)
 * calls progress for each imported <wpt> </wpt>
 */
public class GPXFileImporter {
    private final static String sClass = "GPXFileImporter";
    private final static SimpleDateFormat DATE_PATTERN_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S", Locale.US);

    private final static SimpleDateFormat DATE_PATTERN_3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    private final static SimpleDateFormat DATE_PATTERN_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    public static int CacheCount = 0;
    public static int LogCount = 0;

    static {
        DATE_PATTERN_1.setTimeZone(TimeZone.getTimeZone("EST"));
        DATE_PATTERN_2.setTimeZone(TimeZone.getTimeZone("EST"));
        DATE_PATTERN_3.setTimeZone(TimeZone.getTimeZone("EST"));
    }

    private final AbstractFile mGpxAbstractFile;
    private final String mDisplayFilename;
    private final ImportProgress importProgress;
    private final Waypoint waypoint = new Waypoint(true);
    private final LogEntry log = new LogEntry();
    private final String br = System.getProperty("line.separator");
    private final CacheDAO cacheDAO = CacheDAO.getInstance();
    public Categories categories;
    private Integer currentWpt = 0;
    private Integer countWpt = 0;
    private Integer errors = 0;
    private GpxFilename gpxFilename = null;
    private String gpxName = "";
    private String gpxAuthor = "";
    private TestCancel testCancel;

    GPXFileImporter(AbstractFile abstractFile, ImportProgress _importProgress) {
        super();
        mGpxAbstractFile = abstractFile;
        importProgress = _importProgress;
        mDisplayFilename = abstractFile.getName();
    }

    public void doImport(Integer countwpt, TestCancel testCancel) throws Exception {
        // http://www.thebuzzmedia.com/software/simple-java-xml-parser-sjxp/
        this.testCancel = testCancel;

        if (testCancel != null && testCancel.checkCanceled()) {
            throw new Exception(TestCancel.canceled);
        }

        currentWpt = 0;
        this.countWpt = countwpt;

        Category category = getCategory(mGpxAbstractFile.getAbsolutePath());
        if (category == null)
            return;

        gpxFilename = NewGpxFilename(category, mGpxAbstractFile.getAbsolutePath());
        if (gpxFilename == null)
            return;

        Log.info(sClass, "gpx import from " + gpxFilename.GpxFileName);
        Map<String, String> values = new HashMap<>();

        System.setProperty("sjxp.namespaces", "false");

        List<IRule<Map<String, String>>> ruleList = new ArrayList<>();

        createGPXRules(ruleList);
        createWPTRules(ruleList);
        createGroundspeakRules(ruleList);
        createGSAKRules(ruleList);
        createGSAKRulesWithOutExtensions(ruleList);
        createTerraRules(ruleList);
        createCacheboxRules(ruleList);

        XMLParser<Map<String, String>> parserCache = new XMLParser<>(ruleList.toArray(new IRule[0]));

        try {
            if (testCancel != null && testCancel.checkCanceled())
                throw new Exception(TestCancel.canceled);
            FileInputStream fis = mGpxAbstractFile.getFileInputStream();
            parserCache.parse(fis, values);
            fis.close();
        } catch (Exception e) {
            if (testCancel != null && testCancel.checkCanceled())
                throw new Exception(TestCancel.canceled);
            Log.err(sClass, gpxFilename.GpxFileName + ": " + e.getLocalizedMessage());
        }
    }

    private void handleCache(Cache cache) throws Exception {

        if (testCancel != null && testCancel.checkCanceled()) {
            throw new Exception(TestCancel.canceled);
        }

        if (cacheDAO.cacheExists(cache.generatedId)) {
            cacheDAO.updateDatabase(cache);
        } else {
            cacheDAO.writeToDatabase(cache);
        }

        if (cache.getWayPoints().size() > 0) {
            for (int i = 0; i < cache.getWayPoints().size(); i++) {
                handleWayPoint(cache.getWayPoints().get(i));
            }
        }

        // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
        cache.setLongDescription("");
    }

    private void handleLog(LogEntry log) throws Exception {
        if (testCancel != null && testCancel.checkCanceled()) {
            throw new Exception(TestCancel.canceled);
        }
        LogsTableDAO.getInstance().WriteLogEntry(log);
    }

    private void handleWayPoint(Waypoint wayPoint) throws Exception {
        if (testCancel != null && testCancel.checkCanceled()) {
            throw new Exception(TestCancel.canceled);
        }
        WaypointDAO.getInstance().writeImportToDatabase(wayPoint);
    }

    private Category getCategory(String fileName) throws Exception {
        if (testCancel != null && testCancel.checkCanceled()) {
            throw new Exception(TestCancel.canceled);
        }
        return CoreData.categories.getCategory(fileName);
    }

    private GpxFilename NewGpxFilename(Category category, String fileName) throws Exception {
        if (testCancel != null && testCancel.checkCanceled()) {
            throw new Exception(TestCancel.canceled);
        }
        return category.addGpxFilename(fileName);
    }

    private void createGPXRules(List<IRule<Map<String, String>>> ruleList) {

        // Basic GPX Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                gpxName = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/author") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                gpxAuthor = text;
            }
        });

    }

    private Date parseDate(String text) {
        Date date = parseDateWithFormat(DATE_PATTERN_1, text);
        if (date != null) {
            return date;
        } else {
            date = parseDateWithFormat(DATE_PATTERN_2, text);
            if (date != null) {
                return date;
            } else {
                date = parseDateWithFormat(DATE_PATTERN_3, text);
                if (date != null) {
                    return date;
                } else {
                    throw new XMLParserException("Illegal date format");
                }
            }
        }
    }

    private Date parseDateWithFormat(SimpleDateFormat df, String text) {
        Date date = null;
        try {
            date = df.parse(text);
        } catch (ParseException ignored) {
        }
        return date;
    }

    private void createCacheboxRules(List<IRule<Map<String, String>>> ruleList) {
        // Cachebox Extension

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cachebox-extension/Parent") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_Parent", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cachebox-extension/note") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cachebox-extension_note", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cachebox-extension/solver") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cachebox-extension_solver", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cachebox-extension/clue") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cachebox-extension_clue", text);
            }
        });

    }

    private void createWPTRules(List<IRule<Map<String, String>>> ruleList) {

        // Basic wpt Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    values.clear();
                } else {
                    String wpt_type = values.get("wpt_type");
                    if (wpt_type == null) {
                        // perhaps a lab-cache
                        wpt_type = values.get("cache_type");
                    }
                    if (wpt_type.startsWith("Geocache")) {
                        try {
                            createCache(values);
                            CacheCount++;
                        } catch (Exception e) {
                            if (testCancel != null && testCancel.checkCanceled())
                                parser.stop();
                            else {
                                errors++;
                                Log.err(sClass, "CreateCache", e);
                            }
                        }
                    } else if (wpt_type.startsWith("Waypoint|")) {
                        try {
                            createWaypoint(values);
                        } catch (Exception e) {
                            if (testCancel != null && testCancel.checkCanceled())
                                parser.stop();
                            else {
                                errors++;
                                Log.err(sClass, "CreateWaypoint", e);
                            }
                        }
                    }
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt", "lat", "lon") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("wpt_attribute_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                if (text.startsWith("TerraCache|Classic Cache")) {
                    values.put("wpt_type", "Geocache|Traditional Cache"); // nötig um GPX von TerraCaching.com einzulesen
                } else {
                    values.put("wpt_type", text);
                }
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_name", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/desc") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_desc", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/cmt") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_cmt", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/time") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_time", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/url") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_url", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/link") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("wpt_link", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/sym") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                if (text.startsWith("Default")) {
                    values.put("wpt_sym", "Geocache"); // nötig um GPX von Navicache.com einzulesen
                } else {
                    values.put("wpt_sym", text);
                }
            }
        });

    }

    private void createGroundspeakRules(List<IRule<Map<String, String>>> ruleList) {

        // groundspeak:cache Rules for GPX from GC.com

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache", "id", "available", "archived") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_name", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:placed_by") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_placed_by", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:owner") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_owner", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:container") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_container", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:difficulty") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_difficulty", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:terrain") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_terrain", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:country") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_country", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:state") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_state", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/groundspeak:cache/groundspeak:attributes/groundspeak:attribute") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_attributes_count"))
                        values.put("cache_attributes_count", String.valueOf((Integer.parseInt(values.get("cache_attributes_count")) + 1)));
                    else
                        values.put("cache_attributes_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:attributes/groundspeak:attribute", "id", "inc") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + values.get("cache_attributes_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:short_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_short_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:short_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_short_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:long_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_long_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:long_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_long_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:encoded_hints") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_encoded_hints", text);
            }
        });

        // Log Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_logs_count"))
                        values.put("cache_logs_count", String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
                    else
                        values.put("cache_logs_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log", "id") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
            }
        });

    }

    private void createGSAKRules(List<IRule<Map<String, String>>> ruleList) {

        // GSAK Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/gsak:wptExtension/gsak:Parent") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_Parent", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/gsak:wptExtension/gsak:LatBeforeCorrect") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_corrected_coordinates", "True");
                values.put("cache_gsak_corrected_coordinates_before_lat", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/gsak:wptExtension/gsak:LonBeforeCorrect") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_corrected_coordinates_before_lon", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/gsak:wptExtension/gsak:FavPoints") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_favpoints", text);
            }
        });

        // Cache Rules for GPX from Groundspeak

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache", "id", "available", "archived") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_name", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:placed_by") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_placed_by", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:owner") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_owner", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:container") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_container", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:difficulty") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_difficulty", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:terrain") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_terrain", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:country") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_country", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:state") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_state", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_attributes_count"))
                        values.put("cache_attributes_count", String.valueOf((Integer.parseInt(values.get("cache_attributes_count")) + 1)));
                    else
                        values.put("cache_attributes_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute", "id", "inc") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + values.get("cache_attributes_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_short_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_short_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_long_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_long_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:encoded_hints") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_encoded_hints", text);
            }
        });

        // Log Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_logs_count"))
                        values.put("cache_logs_count", String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
                    else
                        values.put("cache_logs_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log", "id") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
            }
        });

    }

    private void createGSAKRulesWithOutExtensions(List<IRule<Map<String, String>>> ruleList) {

        // GSAK Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/gsak:wptExtension/gsak:Parent") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_Parent", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/gsak:wptExtension/gsak:FavPoints") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_favpoints", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/gsak:wptExtension/gsak:LatBeforeCorrect") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_corrected_coordinates", "True");
                values.put("cache_gsak_corrected_coordinates_before_lat", text);
            }

        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/gsak:wptExtension/gsak:LonBeforeCorrect") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_gsak_corrected_coordinates_before_lon", text);
            }

        });

        // Cache Rules for GPX from Groundspeak

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/groundspeak:cache", "id", "available", "archived") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_name", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:placed_by") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_placed_by", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:owner") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_owner", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:container") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_container", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:difficulty") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_difficulty", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:terrain") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_terrain", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:country") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_country", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/groundspeak:cache/groundspeak:state") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_state", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_attributes_count"))
                        values.put("cache_attributes_count", String.valueOf((Integer.parseInt(values.get("cache_attributes_count")) + 1)));
                    else
                        values.put("cache_attributes_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:attributes/groundspeak:attribute", "id", "inc") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_attribute_" + values.get("cache_attributes_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_short_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:short_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_short_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_long_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:long_description", "html") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_long_description_html", value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:encoded_hints") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_encoded_hints", text);
            }
        });

        // Log Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_logs_count"))
                        values.put("cache_logs_count", String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
                    else
                        values.put("cache_logs_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log", "id") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:date") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:finder") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/groundspeak:cache/groundspeak:logs/groundspeak:log/groundspeak:text") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
            }
        });

    }

    private void createTerraRules(List<IRule<Map<String, String>>> ruleList) {

        // Terra Rules

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_name", text);
            }
        });

        // Cache Rules for GPX from Terracaching.com

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:owner") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_owner", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:description") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_long_description", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:hint") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_encoded_hints", text);
            }
        });

        // Log Rules
        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    if (values.containsKey("cache_logs_count"))
                        values.put("cache_logs_count", String.valueOf((Integer.parseInt(values.get("cache_logs_count")) + 1)));
                    else
                        values.put("cache_logs_count", "1");
                }

            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.ATTRIBUTE, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log", "id") {
            @Override
            public void handleParsedAttribute(XMLParser<Map<String, String>> parser, int index, String value, Map<String, String> values) {

                values.put("cache_log_" + values.get("cache_logs_count") + "_" + this.getAttributeNames()[index], value);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log/terra:date") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_date", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log/terra:user") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_finder", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log/terra:type") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                if (text.startsWith("find")) // Umcodieren auf groundspeak style
                {
                    text = "Found it"; // Evtl. ist noch eine Umcodierung für "nicht gefunden" nötig
                }

                values.put("cache_log_" + values.get("cache_logs_count") + "_type", text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/gpx/wpt/extensions/terra:terracache/terra:logs/terra:log/terra:entry") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                values.put("cache_log_" + values.get("cache_logs_count") + "_text", text);
            }
        });

    }

    private void createCache(Map<String, String> values) throws Exception {
        // create new Cache Object for each imported cache to avoid that informations of one cache are copied into anohter cache.
        Cache cache = new Cache(true);
        // if (cache.detail == null) cache.detail = new CacheDetail();

        if (gpxAuthor.toLowerCase().contains("gctour")) {
            cache.setTourName(gpxName);
        }

        if (values.containsKey("wpt_attribute_lat") && values.containsKey("wpt_attribute_lon")) {
            cache.setCoordinate(new CoordinateGPS(Double.parseDouble(values.get("wpt_attribute_lat")), Double.parseDouble(values.get("wpt_attribute_lon"))));
        }

        if (values.containsKey("wpt_name")) {
            cache.setGeoCacheCode(values.get("wpt_name"));
            cache.generatedId = Cache.generateCacheId(cache.getGeoCacheCode());
        }

        if (values.containsKey("wpt_time")) {
            cache.setDateHidden(parseDate(values.get("wpt_time")));
        }

        if (values.containsKey("cache_attribute_id")) {
            cache.setGeoCacheId(values.get("cache_attribute_id"));
        }

        if (values.containsKey("wpt_url")) {
            cache.setUrl(values.get("wpt_url"));
        }

        // Ein evtl. in der Datenbank vorhandenen "Favorit" nicht überschreiben
        // Boolean fav = LoadBooleanValueFromDB("select favorit from Caches where GcCode = \"" + cache.GcCode + "\"");

        // Read from IndexDBList
        boolean fav = CacheInfoList.CacheIsFavoriteInDB(cache.getGeoCacheCode());

        cache.setFavorite(fav);

        if (values.containsKey("wpt_sym")) {
            // Ein evtl. in der Datenbank vorhandenen "Found" nicht überschreiben
            // Boolean Found = LoadBooleanValueFromDB("select found from Caches where GcCode = \"" + cache.GcCode + "\"");

            // Read from IndexDBList
            boolean Found = CacheInfoList.CacheIsFoundInDB(cache.getGeoCacheCode());

            if (!Found) {
                cache.setFound(values.get("wpt_sym").equalsIgnoreCase("Geocache Found"));
            } else {
                cache.setFound(true);
            }
        }

        if (values.containsKey("cache_attribute_available")) {
            cache.setAvailable(values.get("cache_attribute_available").equalsIgnoreCase("True"));
        } else {
            cache.setAvailable(true);
        }

        if (values.containsKey("cache_attribute_archived")) {
            cache.setArchived(values.get("cache_attribute_archived").equalsIgnoreCase("True"));
        } else {
            cache.setArchived(false);
        }

        if (values.containsKey("cache_name")) {
            cache.setGeoCacheName(values.get("cache_name"));
        } else if (values.containsKey("wpt_desc")) // kein Name gefunden? Dann versuche den WP-Namen
        {
            cache.setGeoCacheName(values.get("wpt_desc"));
        }

        if (values.containsKey("cache_placed_by")) {
            cache.setPlacedBy(values.get("cache_placed_by"));
        }

        if (values.containsKey("cache_owner")) {
            cache.setOwner(values.get("cache_owner"));
        }

        if (values.containsKey("cache_type")) {
            cache.setGeoCacheType(GeoCacheType.parseString(values.get("cache_type")));
            if (cache.getGeoCacheCode().indexOf("MZ") == 0)
                cache.setGeoCacheType(GeoCacheType.Munzee);
        } else {
            cache.setGeoCacheType(GeoCacheType.Undefined);
        }

        if (values.containsKey("cache_container")) {
            cache.geoCacheSize = GeoCacheSize.parseString(values.get("cache_container"));
        } else {
            cache.geoCacheSize = GeoCacheSize.other;
        }

        if (values.containsKey("cache_difficulty")) {
            try {
                cache.setDifficulty(Float.parseFloat(values.get("cache_difficulty")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cache.getDifficulty() < 0)
                cache.setDifficulty(0f);
        }

        if (values.containsKey("cache_terrain")) {
            try {
                cache.setTerrain(Float.parseFloat(values.get("cache_terrain")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cache.getTerrain() < 0)
                cache.setTerrain(0f);
        }

        if (values.containsKey("cache_country")) {
            cache.setCountry(values.get("cache_country"));
        }

        if (values.containsKey("cache_state")) {
            cache.setState(values.get("cache_state"));
        }

        if (values.containsKey("cache_gsak_favpoints")) {
            try {
                cache.favPoints = Integer.parseInt(values.get("cache_gsak_favpoints"));
            } catch (Exception ignored) {
            }
        }

        if (values.containsKey("cache_attributes_count")) {
            try {
                int count = Integer.parseInt(values.get("cache_attributes_count"));

                for (int i = 1; i <= count; i++) {
                    int attrGcComId;
                    int attrGcComVal;

                    attrGcComId = Integer.parseInt(values.get("cache_attribute_" + i + "_id"));
                    try {
                        attrGcComVal = Integer.parseInt(values.get("cache_attribute_" + i + "_inc"));
                    } catch (Exception ex) {
                        // if there is no given value "inc" in attribute definition this should be = 1 (gccapp gpx files)
                        attrGcComVal = 1;
                    }

                    if (attrGcComId > 0 && attrGcComVal != -1) {
                        if (attrGcComVal > 0) {
                            cache.addAttributePositive(Attribute.getAttributeEnumByGcComId(attrGcComId));
                        } else {
                            cache.addAttributeNegative(Attribute.getAttributeEnumByGcComId(attrGcComId));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (values.containsKey("cache_short_description")) {
            cache.setShortDescription(values.get("cache_short_description").trim());

            if (values.containsKey("cache_short_description_html") && values.get("cache_short_description_html").equalsIgnoreCase("False")) {
                cache.setShortDescription(cache.getShortDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
            }
        }

        if (values.containsKey("cache_long_description")) {
            cache.setLongDescription(values.get("cache_long_description").trim());

            if (values.containsKey("cache_long_description_html") && values.get("cache_long_description_html").equalsIgnoreCase("False")) {
                cache.setLongDescription(cache.getLongDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
            }
        }

        if (values.containsKey("cache_encoded_hints")) {
            cache.setHint(values.get("cache_encoded_hints"));
        }

        if (values.containsKey("cache_logs_count")) {
            try {
                int count = Integer.parseInt(values.get("cache_logs_count"));

                for (int i = 1; i <= count; i++) {
                    log.cacheId = cache.generatedId;
                    String attrValue = values.get("cache_log_" + i + "_id");
                    if (attrValue != null) {
                        try {
                            log.logId = Long.parseLong(attrValue);
                            // GSAK Special improvisiert
                            if (log.logId < 0)
                                log.logId = log.cacheId + log.logId;
                        } catch (Exception ex) {
                            // Cache ID konnte nicht als Zahl interpretiert werden -> in eine eindeutige Zahl wandeln
                            log.logId = Cache.generateCacheId(attrValue);
                        }
                    }

                    if (values.containsKey("cache_log_" + i + "_date")) {
                        log.logDate = parseDate(values.get("cache_log_" + i + "_date"));
                    }

                    if (values.containsKey("cache_log_" + i + "_finder")) {
                        log.finder = values.get("cache_log_" + i + "_finder");
                    }

                    if (values.containsKey("cache_log_" + i + "_text")) {
                        log.logText = values.get("cache_log_" + i + "_text");
                    }

                    if (values.containsKey("cache_log_" + i + "_type")) {
                        log.logType = LogType.parseString(values.get("cache_log_" + i + "_type"));
                    }

                    LogCount++;
                    handleLog(log);

                    log.clear();
                }
            } catch (Exception e) {
                if (testCancel != null && testCancel.checkCanceled()) {
                    throw new Exception(TestCancel.canceled);
                }
                else Log.err(sClass, "", e);
            }

        }

        if (values.containsKey("cache_gsak_corrected_coordinates")) { // Handle GSAK Corrected Coordinates
            if (AllSettings.UseCorrectedFinal.getValue()) {
                if (values.get("cache_gsak_corrected_coordinates").equalsIgnoreCase("True")) {
                    double lat = Double.parseDouble(values.get("cache_gsak_corrected_coordinates_before_lat"));
                    double lon = Double.parseDouble(values.get("cache_gsak_corrected_coordinates_before_lon"));

                    Coordinate coorectedCoord = cache.getCoordinate();

                    // set Original Coords
                    cache.setCoordinate(new Coordinate(lat, lon));

                    // create final WP with Corrected Coords
                    String newGcCode = WaypointDAO.getInstance().createFreeGcCode(cache.getGeoCacheCode());

                    // Check if "Final GSAK Corrected" exist
                    CB_List<Waypoint> wplist = WaypointDAO.getInstance().getWaypointsFromCacheID(cache.generatedId, false);

                    for (int i = 0; i < wplist.size(); i++) {
                        Waypoint wp = wplist.get(i);
                        if (wp.isCorrectedFinal()) {
                            newGcCode = wp.getWaypointCode();
                            break;
                        }
                    }

                    Waypoint finalGsakCorrected = new Waypoint(newGcCode, GeoCacheType.Final, "", coorectedCoord.getLatitude(), coorectedCoord.getLongitude(), cache.generatedId, "", "Final GSAK Corrected");

                    cache.getWayPoints().add(finalGsakCorrected);

                    // the coordinates of the Cache are not changed. we have a Final with valid coordinates
                }
            } else {
                cache.setHasCorrectedCoordinates(true);
            }
        }

        cache.setGPXFilename_ID(gpxFilename.Id);

        currentWpt++;

        StringBuilder info = new StringBuilder();

        info.append(mDisplayFilename);
        info.append(br);
        info.append("Cache: ");
        info.append(currentWpt);
        info.append("/");
        info.append(countWpt);

        if (errors > 0) {
            info.append(br);
            info.append("Errors: ");
            info.append(errors);
        }

        importProgress.incrementStep("ImportGPX", info.toString());

        // Merge mit cache info
        if (CacheInfoList.existCache(cache.getGeoCacheCode())) {
            CacheInfoList.mergeCacheInfo(cache);
        } else {
            // Neue CacheInfo erstellen und zur Liste Hinzufügen
            CacheInfoList.putNewInfo(cache);
        }

        // write to Database
        handleCache(cache);

        /*
        Write Note and Solver!
        Attention: the cache must already exist in the db, else these values are overwritten
        so the setSolver and setNote have to be executed after handleCache
         */
        if (values.containsKey("cachebox-extension_solver")) {
            CacheDAO.getInstance().setSolver(cache, values.get("cachebox-extension_solver"));
        }
        if (values.containsKey("cachebox-extension_note")) {
            CacheDAO.getInstance().setNote(cache, values.get("cachebox-extension_note"));
        }

    }

    private void createWaypoint(Map<String, String> values) throws Exception {
        if (values.containsKey("wpt_attribute_lat") && values.containsKey("wpt_attribute_lon")) {
            waypoint.setCoordinate(new CoordinateGPS(Double.parseDouble(values.get("wpt_attribute_lat")), Double.parseDouble(values.get("wpt_attribute_lon"))));
        } else {
            waypoint.setCoordinate(new CoordinateGPS(0, 0));
        }

        if (values.containsKey("wpt_name")) {
            waypoint.setWaypointCode(values.get("wpt_name"));
            waypoint.setTitle(waypoint.getWaypointCode());
            // TODO Hack to get parent Cache

            if (values.containsKey("cache_gsak_Parent")) {
                String parent = values.get("cache_gsak_Parent");
                waypoint.geoCacheId = Cache.generateCacheId(parent);
            } else {
                waypoint.geoCacheId = Cache.generateCacheId("GC" + waypoint.getWaypointCode().substring(2));
            }

        }

        if (values.containsKey("wpt_desc")) {
            waypoint.setTitle(values.get("wpt_desc"));
        }

        if (values.containsKey("wpt_type")) {
            String typeString = values.get("wpt_type");
            if (typeString.contains("Waypoint|Flag")) {
                typeString = values.get("wpt_desc");
            }

            waypoint.parseTypeString(typeString);
        }

        if (values.containsKey("wpt_cmt")) {
            waypoint.setDescription(values.get("wpt_cmt"));
        }

        if (values.containsKey("cachebox-extension_clue")) {
            waypoint.setClue(values.get("cachebox-extension_clue"));
        }

        currentWpt++;
        importProgress.incrementStep("ImportGPX", mDisplayFilename + "\nWaypoint: " + currentWpt + "/" + countWpt + "\n" + waypoint.getWaypointCode() + " - " + waypoint.getDescription());

        handleWayPoint(waypoint);

        waypoint.clear();

    }
}
