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
package de.droidcachebox.core;

import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.database.LogsTableDAO;
import de.droidcachebox.dataclasses.Attribute;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.CacheList;
import de.droidcachebox.dataclasses.LogEntry;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.log.Log;

/**
 * GPX writer, which the given cache code from the current DB after each other, with all the details, loads, <br>
 * and then writes to a GPX file.<br>
 * <br>
 * The Code base is from c:geo https://github.com/cgeo/cgeo/blob/master/main/src/cgeo/geocaching/export/GpxSerializer.java<br>
 * commit cadf1bb896976c5dc04cfd4b1615ad694fec3415
 *
 * @author Longri
 */
public final class GpxSerializer {
    private static final String sClass = "GpxSerializer";
    private static final SimpleDateFormat dateFormatZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    private static final String PREFIX_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String PREFIX_GPX = "http://www.topografix.com/GPX/1/0";
    private static final String PREFIX_GROUNDSPEAK = "http://www.groundspeak.com/cache/1/0/1";
    private static final String PREFIX_CACHEBOX = "cachebox-extension";

    /**
     * During the export, only this number of Caches is fully loaded into memory.
     */
    private static final int CACHES_PER_BATCH = 500;
    private final XmlSerializer gpx = new KXmlSerializer();
    /**
     * counter for exported caches, used for progress reporting
     */
    private int countExported;
    private ProgressListener progressListener;
    private boolean cancel = false;

    private static String getState(final Cache cache) {
        return getLocationPart(cache, 0);
    }

    private static String getLocationPart(final Cache cache, int partIndex) {
        final String location = cache.getCountry();
        if (location.contains(", ")) {
            final String[] parts = location.split(",");
            if (parts.length == 2) {
                return parts[partIndex].trim();
            }
        }
        return "";
    }

    private static String getCountry(final Cache cache) {
        String country = getLocationPart(cache, 1);
        if (country.length() > 0)
            return country;
        return cache.getCountry();
    }

    /**
     * Insert an attribute-less tag with enclosed text in a XML serializer output.
     *
     * @param serializer an XML serializer
     * @param prefix     an XML prefix, see {@link XmlSerializer#startTag(String, String)}
     * @param tag        an XML tag
     * @param text       some text to insert, or <tt>null</tt> to omit completely this tag
     */
    private static void simpleText(final XmlSerializer serializer, final String prefix, final String tag, final String text) throws IOException {
        if (text != null) {
            serializer.startTag(prefix, tag);
            serializer.text(validateChar(text));
            serializer.endTag(prefix, tag);
        }
    }

    /**
     * Android throws a InvalidCharacterException so we check this before write
     *
     * @return valid text as String
     */
    private static String validateChar(String text) {
        char[] validChars = new char[text.length()];
        int validCount = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean valid = (c >= 0x20 && c <= 0xd7ff) || (c >= 0xe000 && c <= 0xfffd);
            if (valid) {
                validChars[validCount++] = c;
            } else {
                if (c == 10) // LineBreak
                {
                    validChars[validCount++] = c;
                }
            }
        }
        return new String(validChars, 0, validCount);
    }

    /**
     * Insert pairs of attribute-less tags and enclosed texts in a XML serializer output
     *
     * @param serializer an XML serializer
     * @param prefix     an XML prefix, see {@link XmlSerializer#startTag(String, String)} shared by all tags
     * @param tagAndText an XML tag, the corresponding text, another XML tag, the corresponding text. <tt>null</tt> texts will be omitted along
     *                   with their respective tag.
     */
    private static void multipleTexts(final XmlSerializer serializer, final String prefix, final String... tagAndText) throws IOException {
        for (int i = 0; i < tagAndText.length; i += 2) {
            simpleText(serializer, prefix, tagAndText[i], tagAndText[i + 1]);
        }
    }

    /**
     * Quick and naive check for possible rich HTML content in a string.
     *
     * @param str A string containing HTML code.
     * @return <tt>true</tt> if <tt>str</tt> contains HTML code that needs to go through a HTML renderer before being displayed,
     * <tt>false</tt> if it can be displayed as-is without any loss
     */
    private static boolean containsHtml(final String str) {
        if (str == null)
            return false;
        return str.indexOf('<') != -1 || str.indexOf('&') != -1;
    }

    // private void writeTravelBugs(final Cache cache) throws IOException
    // {
    // List<Trackable> inventory = cache.getInventory();
    // if (CollectionUtils.isEmpty(inventory))
    // {
    // return;
    // }
    // gpx.startTag(PREFIX_GROUNDSPEAK, "travelbugs");
    //
    // for (final Trackable trackable : inventory)
    // {
    // gpx.startTag(PREFIX_GROUNDSPEAK, "travelbug");
    //
    // // in most cases the geocode will be empty (only the guid is known). those travel bugs cannot be imported again!
    // gpx.attribute("", "ref", trackable.getGcCode());
    // simpleText(gpx, PREFIX_GROUNDSPEAK, "name", trackable.getName());
    //
    // gpx.endTag(PREFIX_GROUNDSPEAK, "travelbug");
    // }
    //
    // gpx.endTag(PREFIX_GROUNDSPEAK, "travelbugs");
    // }

    /**
     * Cancel the Export
     */
    public void cancel() {
        cancel = true;
    }

    public void writeGPX(List<String> allGeocodesIn, Writer writer, final ProgressListener progressListener) throws IOException {
        cancel = false;

        // create a copy of the geocode list, as we need to modify it, but it might be immutable
        final ArrayList<String> allGeocodes = new ArrayList<>(allGeocodesIn);

        this.progressListener = progressListener;
        gpx.setOutput(writer);
        gpx.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        final String UTF_8 = StandardCharsets.UTF_8.name();
        gpx.startDocument(UTF_8, true);
        gpx.setPrefix("xsi", PREFIX_XSI);
        gpx.setPrefix("", PREFIX_GPX);
        gpx.setPrefix("groundspeak", PREFIX_GROUNDSPEAK);

        gpx.startTag(PREFIX_GPX, "gpx");
        gpx.attribute("", "version", "1.0");
        gpx.attribute("", "creator", "Cachebox - http://www.team-cachebox.de/");
        gpx.attribute(PREFIX_XSI, "schemaLocation", PREFIX_GPX + " http://www.topografix.com/GPX/1/0/gpx.xsd " + PREFIX_GROUNDSPEAK + " http://www.groundspeak.com/cache/1/0/1/cache.xsd ");

        simpleText(gpx, PREFIX_GPX, "desc", "Geocache file generated by Cachebox (HasChildren)");

        // Split the overall set of geocodes into small chunks. That is a compromise between memory efficiency (because
        // we don't load all caches fully into memory) and speed (because we don't query each cache separately).
        while (!allGeocodes.isEmpty()) {
            try {
                final ArrayList<String> batch = new ArrayList<>(allGeocodes.subList(0, Math.min(CACHES_PER_BATCH, allGeocodes.size())));
                exportBatch(gpx, batch);
                allGeocodes.removeAll(batch);
                batch.clear();
                if (cancel)
                    break;
            }
            catch (Exception ex) {
                Log.err(sClass, ex);
                break;
            }
        }

        gpx.endTag(PREFIX_GPX, "gpx");
        gpx.endDocument();
    }

    private void exportBatch(final XmlSerializer gpx, ArrayList<String> geocodesOfBatch) throws IOException {

        progressListener.publishProgress(countExported, Translation.get("readCacheDetails", String.valueOf(geocodesOfBatch.size())));

        CacheList cacheList = CacheListDAO.getInstance().readCacheList(geocodesOfBatch, true, true, true);
        for (int i = 0; i < cacheList.size(); i++) {
            if (cancel)
                break;
            Cache cache = cacheList.get(i);

            try {
                if (cache == null) {
                    continue;
                }
                final Coordinate coords = cache.getCoordinate();
                if (coords == null) {
                    // Export would be invalid without coordinates.
                    continue;
                }
                gpx.startTag(PREFIX_GPX, "wpt");
                gpx.attribute("", "lat", Double.toString(coords.getLatitude()));
                gpx.attribute("", "lon", Double.toString(coords.getLongitude()));

                final Date hiddenDate = cache.getDateHidden();
                if (hiddenDate != null) {
                    simpleText(gpx, PREFIX_GPX, "time", dateFormatZ.format(hiddenDate));
                }

                String additinalIfFound = cache.isFound() ? "|Found" : "";
                String note = CacheDAO.getInstance().getNote(cache);
                if (note == null)
                    note = "";
                String solver = CacheDAO.getInstance().getSolver(cache);
                if (solver == null)
                    solver = "";

                multipleTexts(gpx, PREFIX_GPX, //
                        "name", cache.getGeoCacheCode(), //
                        "desc", cache.getGeoCacheName(), //
                        "url", cache.getUrl(), //
                        "urlname", cache.getGeoCacheName(), //
                        "sym", cache.isFound() ? "Geocache Found" : "Geocache", //
                        "type", "Geocache|" + cache.getGeoCacheType().toString() + additinalIfFound//

                );

                gpx.startTag(PREFIX_GROUNDSPEAK, "cache");
                gpx.attribute("", "id", cache.getGeoCacheId());
                gpx.attribute("", "available", cache.isAvailable() ? "True" : "False");
                gpx.attribute("", "archived", cache.isArchived() ? "True" : "False");
                gpx.attribute("", "xmlns:groundspeak", PREFIX_GROUNDSPEAK);

                String difficulty;
                String terrain;

                if (cache.getDifficulty() % 1 == 0) {
                    difficulty = Integer.toString((int) cache.getDifficulty());
                } else {
                    difficulty = Float.toString(cache.getDifficulty());
                }

                if (cache.getTerrain() % 1 == 0) {
                    terrain = Integer.toString((int) cache.getTerrain());
                } else {
                    terrain = Float.toString(cache.getTerrain());
                }

                multipleTexts(gpx, PREFIX_GROUNDSPEAK, //
                        "name", cache.getGeoCacheName(), //
                        "placed_by", cache.getPlacedBy(), //
                        "owner", cache.getOwner(), //
                        "type", cache.getGeoCacheType().toString(), //
                        "container", cache.geoCacheSize.toString(), //
                        "difficulty", difficulty, //
                        "terrain", terrain, //
                        "country", getCountry(cache), //
                        "state", getState(cache), //
                        "encoded_hints", cache.getHint());

                writeAttributes(cache);

                // Shortdescription is not in DB. It is combined with LongDescription and saved into ROW Description
                // Expand DB with ROW shortDescription
                String shortDesc = null;
                try {
                    shortDesc = cache.getShortDescription();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (shortDesc != null && shortDesc.length() > 0) {
                    gpx.startTag(PREFIX_GROUNDSPEAK, "short_description");
                    gpx.attribute("", "html", containsHtml(cache.getShortDescription()) ? "True" : "False");
                    gpx.text(validateChar(cache.getShortDescription()));
                    gpx.endTag(PREFIX_GROUNDSPEAK, "short_description");
                }

                String longDesc = cache.getLongDescription();
                if (longDesc != null && longDesc.length() > 0) {
                    gpx.startTag(PREFIX_GROUNDSPEAK, "long_description");
                    gpx.attribute("", "html", containsHtml(cache.getLongDescription()) ? "True" : "False");
                    gpx.text(validateChar(cache.getLongDescription()));
                    gpx.endTag(PREFIX_GROUNDSPEAK, "long_description");
                }
                writeLogs(cache);
                // writeTravelBugs(cache);

                gpx.endTag(PREFIX_GROUNDSPEAK, "cache");

                gpx.startTag(PREFIX_GPX, PREFIX_CACHEBOX);
                multipleTexts(gpx, PREFIX_GPX, //
                        "note", note, //
                        "solver", solver//
                );
                gpx.endTag(PREFIX_GPX, PREFIX_CACHEBOX);

                gpx.endTag(PREFIX_GPX, "wpt");

                try {
                    writeWaypoints(cache);
                }
                catch (Exception sex) {
                    Log.err(sClass, "write waypoints for " + cache.getGeoCacheCode(), sex);
                }

                countExported++;
                if (progressListener != null) {
                    progressListener.publishProgress(countExported, Translation.get("writeCache", cache.getGeoCacheCode()));
                }
            }
            catch (Exception ex) {
                Log.err(sClass, "write waypoints for " + cache.getGeoCacheCode(), ex);
            }
        }

        cacheList.dispose();
    }

    private void writeWaypoints(final Cache cache) throws IOException {
        final CB_List<Waypoint> waypoints = cache.getWayPoints();
        final List<Waypoint> ownWaypoints = new ArrayList<>(waypoints.size());
        final List<Waypoint> originWaypoints = new ArrayList<>(waypoints.size());

        for (int i = 0; i < cache.getWayPoints().size(); i++) {
            Waypoint wp = cache.getWayPoints().get(i);

            if (wp.isUserWaypoint) {
                ownWaypoints.add(wp);
            } else {
                originWaypoints.add(wp);
            }
        }
        for (final Waypoint wp : originWaypoints) {
            writeCacheWaypoint(cache, wp);
        }
        // Prefixes must be unique. There use numeric strings as prefixes in OWN waypoints where they are missing
        for (final Waypoint wp : ownWaypoints) {
            writeCacheWaypoint(cache, wp);
        }
    }

    /**
     * Writes one waypoint entry for cache waypoint.
     */
    private void writeCacheWaypoint(Cache cache, final Waypoint wp) throws IOException {
        final Coordinate coords = wp.getCoordinate();
        if (coords != null) {
            gpx.startTag(PREFIX_GPX, "wpt");
            gpx.attribute("", "lat", Double.toString(coords.getLatitude()));
            gpx.attribute("", "lon", Double.toString(coords.getLongitude()));
            multipleTexts(gpx, PREFIX_GPX, //
                    "name", wp.getWaypointCode(), //
                    "cmt", wp.getDescription(), //
                    "desc", wp.getTitle(), //
                    "sym", wp.waypointType.toString(), //
                    "type", "Waypoint|" + wp.waypointType.toString()); //

            gpx.startTag(PREFIX_GPX, PREFIX_CACHEBOX);

            multipleTexts(gpx, PREFIX_GPX, //
                    "clue", wp.getClue(), //
                    "Parent", cache.getGeoCacheCode());
            gpx.endTag(PREFIX_GPX, PREFIX_CACHEBOX);

            gpx.endTag(PREFIX_GPX, "wpt");
        }
    }

    private void writeLogs(final Cache cache) throws IOException {
        CB_List<LogEntry> logEntries = LogsTableDAO.getInstance().getLogs(cache);
        if (logEntries.isEmpty()) {
            return;
        }
        gpx.startTag(PREFIX_GROUNDSPEAK, "logs");
        for (LogEntry log : logEntries) {
            gpx.startTag(PREFIX_GROUNDSPEAK, "log");
            gpx.attribute("", "id", Integer.toString((int) log.logId));

            multipleTexts(gpx, PREFIX_GROUNDSPEAK, "date", dateFormatZ.format(log.logDate), "type", log.logType.toString());

            gpx.startTag(PREFIX_GROUNDSPEAK, "finder");
            gpx.attribute("", "id", "");
            gpx.text(validateChar(log.finder));
            gpx.endTag(PREFIX_GROUNDSPEAK, "finder");

            gpx.startTag(PREFIX_GROUNDSPEAK, "text");
            gpx.attribute("", "encoded", "False");
            try {
                gpx.text(validateChar(log.logText));
            } catch (final IllegalArgumentException e) {
                Log.err(sClass, "GpxSerializer.writeLogs: cannot write log " + log.logId + " for cache " + cache.getGeoCacheCode(), e);
                gpx.text(" [end of log omitted due to an invalid character]");
            }
            gpx.endTag(PREFIX_GROUNDSPEAK, "text");

            gpx.endTag(PREFIX_GROUNDSPEAK, "log");
        }
        gpx.endTag(PREFIX_GROUNDSPEAK, "logs");
    }

    private void writeAttributes(final Cache cache) throws IOException {
        if (cache.getAttributes().isEmpty()) {
            return;
        }

        gpx.startTag(PREFIX_GROUNDSPEAK, "attributes");

        for (final Attribute attribute : cache.getAttributes()) {
            final boolean enabled = cache.isAttributePositiveSet(attribute);

            gpx.startTag(PREFIX_GROUNDSPEAK, "attribute");
            gpx.attribute("", "id", Integer.toString(Attribute.GetAttributeID(attribute)));
            gpx.attribute("", "inc", enabled ? "1" : "0");
            gpx.text(validateChar(attribute.toString()));
            gpx.endTag(PREFIX_GROUNDSPEAK, "attribute");
        }

        gpx.endTag(PREFIX_GROUNDSPEAK, "attributes");
    }

    public interface ProgressListener {
        void publishProgress(int countExported, String name);
    }

}