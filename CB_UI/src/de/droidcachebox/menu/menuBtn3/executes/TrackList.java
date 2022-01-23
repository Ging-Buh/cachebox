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
package de.droidcachebox.menu.menuBtn3.executes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.DrawUtils;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.locator.map.PolylineReduction;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.menu.menuBtn3.ShowTracks;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

/**
 * mainly holds the list of tracks in tracks with getNumberOfTracks
 * and its accessor methods getTrack, addTrack, removeTrack .
 * <p>
 * further holds a track called routingTrack, calculated by some routing code (at the moment the BRouter Code for Android)
 * with accessors existsRoutingTrack, setRoutingTrack and removeRoutingTrack
 * <p>
 * if a track is changed, aTrackChanged is set to true until rendered once again on one called map !!! only once
 * <p>
 * a track is selected for rendering, if its isVisible is set (in addToTracksToDraw with reduced number of trackpoints)
 * further the GlobalCore.currentRoute is addToTracksToDraw for rendering. (if tracking has been switched on)
 */
public class TrackList {
    private final static String log = "TrackList";
    private static TrackList trackList;
    private final ArrayList<Track> tracks;
    // for rendering
    private final ArrayList<DrawTrack> tracksToDraw;
    private boolean aTrackChanged;
    private int thisZoom;
    private Track routingTrack; // for identifying the track! has been originally from openRouteService implementation. now from BRouter
    private GlyphLayout glyphLayout;
    public Track currentRoute = null;


    private TrackList() {
        tracks = new ArrayList<>();
        aTrackChanged = false;
        thisZoom = -1;
        tracksToDraw = new ArrayList<>();
    }

    public static TrackList getInstance() {
        if (trackList == null) trackList = new TrackList();
        return trackList;
    }

    public int getNumberOfTracks() {
        return tracks.size();
    }

    public Track getTrack(int position) {
        return tracks.get(position);
    }

    public void addTrack(Track track) {
        // Dont use this for internal RoutingTrack!! Use setRoutingTrack(Track track)
        tracks.add(track);
        trackListChanged();
    }

    public void removeTrack(Track track) {
        if (track == routingTrack) {
            routingTrack = null;
        }
        tracks.remove(track);
        trackListChanged();
    }

    public void trackListChanged() {
        aTrackChanged = true;
        GL.that.renderOnce();
    }

    // =================================================================================================================

    public boolean existsRoutingTrack() {
        return routingTrack != null;
    }

    public void setRoutingTrack(Track track) {
        if (routingTrack == null) {
            track.setColor(new Color(0.85f, 0.1f, 0.2f, 1f));
        } else {
            // erst alten routingTrack löschen
            tracks.remove(routingTrack);
            track.setColor(routingTrack.getColor());
        }
        tracks.add(0, track);
        routingTrack = track;
        trackListChanged();
    }

    public void removeRoutingTrack() {
        tracks.remove(routingTrack);
        routingTrack = null;
        trackListChanged();
    }

    // =================================================================================================================

    public void renderTracks(Batch batch, MapView mapView) {

        int mapZoom = mapView.getCurrentZoom();
        float yVersatz = mapView.ySpeedVersatz;

        if (thisZoom != mapZoom || aTrackChanged) {
            aTrackChanged = false;
            thisZoom = mapZoom;
            tracksToDraw.clear();
            double tolerance = 0.01 * Math.exp(-1 * (mapZoom - 11));

            for (Track track : tracks) {
                if (track != null && track.isVisible()) {
                    addToTracksToDraw(tolerance, track, mapZoom);
                }
            }

            if (currentRoute != null && currentRoute.isVisible()) {
                addToTracksToDraw(tolerance, currentRoute, mapZoom);
            }

        }

        if (tracksToDraw.size() > 0) {
            for (DrawTrack drawTrack : tracksToDraw) {
                Sprite arrow = drawTrack.arrow;
                Sprite point = drawTrack.point;
                float overlap = drawTrack.overlap;
                arrow.setColor(drawTrack.mColor);
                point.setColor(drawTrack.mColor);
                float scale = UiSizes.getInstance().getScale();

                for (int ii = 0; ii < drawTrack.trackPoints.size() - 1; ii++) {

                    double mapX1 = 256.0 * Descriptor.longitudeToTileX(MapView.MAX_MAP_ZOOM, drawTrack.trackPoints.get(ii).x);
                    double mapY1 = -256.0 * Descriptor.latitudeToTileY(MapView.MAX_MAP_ZOOM, drawTrack.trackPoints.get(ii).y);

                    double mapX2 = 256.0 * Descriptor.longitudeToTileX(MapView.MAX_MAP_ZOOM, drawTrack.trackPoints.get(ii + 1).x);
                    double mapY2 = -256.0 * Descriptor.latitudeToTileY(MapView.MAX_MAP_ZOOM, drawTrack.trackPoints.get(ii + 1).y);

                    Vector2 screen1 = mapView.worldToScreen(new Vector2((float) mapX1, (float) mapY1));
                    Vector2 screen2 = mapView.worldToScreen(new Vector2((float) mapX2, (float) mapY2));

                    screen1.y = screen1.y - yVersatz;
                    screen2.y = screen2.y - yVersatz;

                    CB_RectF chkRec = new CB_RectF(mapView);
                    chkRec.setPos(0, 0);

                    // chk if line on Screen
                    if (chkRec.contains(screen1.x, screen1.y) || chkRec.contains(screen2.x, screen2.y)) {
                        DrawUtils.drawSpriteLine(batch, arrow, point, overlap * scale, screen1.x, screen1.y, screen2.x, screen2.y);
                        // DrawedLineCount++;
                    } else {// chk if intersection
                        if (chkRec.getIntersection(screen1, screen2, 2) != null) {
                            DrawUtils.drawSpriteLine(batch, arrow, point, overlap * scale, screen1.x, screen1.y, screen2.x, screen2.y);
                            // DrawedLineCount++;
                        }
                        // the line is not on the screen
                    }

                    if (chkRec.contains(screen2.x, screen2.y)) {
                        if (ii == drawTrack.trackPoints.size() - 2) {
                            try {
                                drawText(batch, UnitFormatter.distanceString((float) drawTrack.tracklength), screen2);
                            } catch (Exception ex) {
                                Log.err(log, "for loop: " + drawTrack.tracklength, ex);
                            }
                        }
                    }
                }

            }
        }
    }

    private void drawText(Batch batch, String text, Vector2 position) {
        try {
            BitmapFont smallFont = Fonts.getSmall();
            Color fontColor = smallFont.getColor();
            if (fontColor != null) {
                Color skinFontColor = COLOR.getFontColor();
                if (skinFontColor != null) {
                    // if (!skinFontColor.equals(fontColor))
                    smallFont.setColor(skinFontColor); // modifies fontColor
                } else {
                    Log.err(log, "skinFontColor should never be null");
                }
                if (glyphLayout == null)
                    try {
                        glyphLayout = new GlyphLayout(smallFont, text);
                    } catch (Exception e) {
                        glyphLayout = new GlyphLayout(smallFont, "Error");
                    }
                else {
                    // reuse? of glyphLayout sometimes gives NPE in com.badlogic.gdx.graphics.g2d.GlyphLayout$GlyphRun.color
                    glyphLayout.setText(smallFont, text);
                }
                float halfWidth = glyphLayout.width / 2;
                smallFont.draw(batch, glyphLayout, position.x - halfWidth, position.y - 2 * glyphLayout.height);
            } else {
                Log.err(log, "fontColor should never be null");
            }
        } catch (Exception ex) {
            Log.err(log, "drawText", ex);
        }
    }

    private void addToTracksToDraw(double tolerance, Track track, int zoom) {

        synchronized (track.getTrackPoints()) {

            ArrayList<TrackPoint> reducedPoints;

            // do not reduce for zoom >= 18
            if (zoom >= 18) {
                reducedPoints = track.getTrackPoints();
            } else {
                reducedPoints = PolylineReduction.polylineReduction(track.getTrackPoints(), tolerance);
                // Log.info(log, "Track: " + track.FileName + " has " + track.Points.size() + ". reduced to " + reducedPoints.size() + " at Zoom = " + zoom);
                if (reducedPoints.size() == 2) {
                    reducedPoints = track.getTrackPoints();
                }
            }

            DrawTrack drawTrack = new DrawTrack(track.getColor(), track == routingTrack);
            drawTrack.trackPoints = reducedPoints;
            drawTrack.tracklength = track.getTrackLength();

            tracksToDraw.add(drawTrack);

        }

    }

    public void selectTrackFileReadAndAddToTracks() {
        new FileOrFolderPicker(Settings.TrackFolder.getValue(), "*.gpx", Translation.get("LoadTrack"), Translation.get("load"), abstractFile -> {
            if (abstractFile != null) {
                readFromGpxFile(abstractFile);
            }
        }).show();
    }

    public void loadTrack(String trackPath, String fileName) {
        // used by autoload
        String absolutPath;
        if (fileName.equals("")) {
            absolutPath = trackPath;
        } else {
            absolutPath = trackPath + "/" + fileName;
        }
        readFromGpxFile(FileFactory.createFile(absolutPath));
    }

    private void readFromGpxFile(AbstractFile abstractFile) {
        // !!! it is possible that a gpx file contains more than 1 <trk> segments
        // they are all added to the TrackList
        ArrayList<Track> tracks = new ArrayList<>();
        float[] dist = new float[4];
        double distance = 0;
        double altitudeDifference = 0;
        double deltaAltitude;
        CoordinateGPS fromPosition = new CoordinateGPS(0, 0);
        BufferedReader reader;
        HSV_Color trackColor = null;

        try {
            InputStreamReader isr = new InputStreamReader(abstractFile.getFileInputStream(), StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            Track track = new Track("");

            String line;
            String tmpLine;
            String gpxName = null;
            boolean isSeg = false;
            boolean isTrk = false;
            boolean isRte = false;
            boolean isTrkptOrRtept = false;
            boolean readName = false;
            int anzSegments = 0;

            CoordinateGPS lastAcceptedCoordinate = null;
            double lastAcceptedDirection = -1;
            Date lastAcceptedTime = null;

            StringBuilder sb = new StringBuilder();
            String rline;
            while ((rline = reader.readLine()) != null) {
                for (int i = 0; i < rline.length(); i++) {
                    char nextChar = rline.charAt(i);
                    sb.append(nextChar);

                    if (nextChar == '>') {
                        line = sb.toString().trim().toLowerCase();
                        tmpLine = sb.toString();
                        sb = new StringBuilder();

                        if (!isTrk) // Begin of the Track detected?
                        {
                            if (line.contains("<trk>")) {
                                isTrk = true;
                                continue;
                            }
                        }

                        if (!isSeg) // Begin of the Track Segment detected?
                        {
                            if (line.contains("<trkseg>")) {
                                isSeg = true;
                                track = new Track("");
                                track.setFileName(abstractFile.getAbsolutePath());
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(abstractFile.getName()); // FileIO.getFileName(file)
                                else {
                                    if (anzSegments <= 1)
                                        track.setName(gpxName);
                                    else
                                        track.setName(gpxName + anzSegments);
                                }
                                continue;
                            }
                        }

                        if (!isRte) // Begin of the Route detected?
                        {
                            if (line.contains("<rte>")) {
                                isRte = true;
                                track = new Track("");
                                track.setFileName(abstractFile.getAbsolutePath());
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(abstractFile.getName()); // FileIO.getFileName(file)
                                else {
                                    if (anzSegments <= 1)
                                        track.setName(gpxName);
                                    else
                                        track.setName(gpxName + anzSegments);
                                }
                                continue;
                            }
                        }

                        if ((line.contains("<name>")) & !isTrkptOrRtept) // found <name>?
                        {
                            readName = true;
                            continue;
                        }

                        if (readName & !isTrkptOrRtept) {
                            int cdata_start;
                            int name_start = 0;
                            int name_end;

                            name_end = line.indexOf("</name>");

                            // Name contains cdata?
                            cdata_start = line.indexOf("[cdata[");
                            if (cdata_start > -1) {
                                name_start = cdata_start + 7;
                                name_end = line.indexOf("]");
                            }

                            if (name_end > name_start) {
                                // tmpLine, damit Groß-/Kleinschreibung beachtet wird
                                if (isSeg || isRte)
                                    track.setName(tmpLine.substring(name_start, name_end));
                                else
                                    gpxName = tmpLine.substring(name_start, name_end);
                            }

                            readName = false;
                            continue;
                        }

                        if (line.contains("</trkseg>")) // End of the Track Segment detected?
                        {
                            if (track.getTrackPoints().size() < 2)
                                track.setName("no Route segment found");
                            track.setVisible(true);
                            track.setTrackLength(distance);
                            track.setAltitudeDifference(altitudeDifference);
                            tracks.add(track);
                            isSeg = false;
                            break;
                        }

                        if (line.contains("</rte>")) // End of the Route detected?
                        {
                            if (track.getTrackPoints().size() < 2)
                                track.setName("no Route segment found");
                            track.setVisible(true);
                            track.setTrackLength(distance);
                            track.setAltitudeDifference(altitudeDifference);
                            tracks.add(track);
                            isRte = false;
                            break;
                        }

                        if ((line.contains("<trkpt")) || (line.contains("<rtept"))) {
                            isTrkptOrRtept = true;
                            // Trackpoint lesen
                            int lonIdx = line.indexOf("lon=\"") + 5;
                            int latIdx = line.indexOf("lat=\"") + 5;

                            int lonEndIdx = line.indexOf("\"", lonIdx);
                            int latEndIdx = line.indexOf("\"", latIdx);

                            String latStr = line.substring(latIdx, latEndIdx);
                            String lonStr = line.substring(lonIdx, lonEndIdx);

                            double lat = Double.parseDouble(latStr);
                            double lon = Double.parseDouble(lonStr);

                            lastAcceptedCoordinate = new CoordinateGPS(lat, lon);
                        }

                        if (line.contains("</time>")) {
                            // Time lesen
                            int timIdx = line.indexOf("<time>") + 6;
                            if (timIdx == 5)
                                timIdx = 0;
                            int timEndIdx = line.indexOf("</time>", timIdx);

                            String timStr = line.substring(timIdx, timEndIdx);

                            lastAcceptedTime = parseDate(timStr);
                        }

                        if (line.contains("</course>")) {
                            // Course lesen
                            int couIdx = line.indexOf("<course>") + 8;
                            if (couIdx == 7)
                                couIdx = 0;
                            int couEndIdx = line.indexOf("</course>", couIdx);

                            String couStr = line.substring(couIdx, couEndIdx);

                            lastAcceptedDirection = Double.parseDouble(couStr);

                        }

                        if ((line.contains("</ele>")) & isTrkptOrRtept) {
                            // Elevation lesen
                            int couIdx = line.indexOf("<ele>") + 5;
                            if (couIdx == 4)
                                couIdx = 0;
                            int couEndIdx = line.indexOf("</ele>", couIdx);

                            String couStr = line.substring(couIdx, couEndIdx);

                            lastAcceptedCoordinate.setElevation(Double.parseDouble(couStr));

                        }

                        if (line.contains("</gpxx:colorrgb>")) {
                            // Color lesen
                            int couIdx = line.indexOf("<gpxx:colorrgb>") + 15;
                            if (couIdx == 14)
                                couIdx = 0;
                            int couEndIdx = line.indexOf("</gpxx:colorrgb>", couIdx);

                            String couStr = line.substring(couIdx, couEndIdx);
                            trackColor = new HSV_Color(couStr);
                            track.setColor(trackColor);
                        }

                        if ((line.contains("</trkpt>")) || (line.contains("</rtept>")) || ((line.contains("/>")) & isTrkptOrRtept)) {
                            // trkpt abgeschlossen, jetzt kann der Trackpunkt erzeugt werden
                            isTrkptOrRtept = false;
                            if (lastAcceptedCoordinate != null) {
                                track.getTrackPoints().add(new TrackPoint(lastAcceptedCoordinate.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getElevation(), lastAcceptedDirection, lastAcceptedTime));

                                // Calculate the length of a Track
                                if (!fromPosition.isValid()) {
                                    fromPosition = new CoordinateGPS(lastAcceptedCoordinate);
                                    fromPosition.setElevation(lastAcceptedCoordinate.getElevation());
                                    fromPosition.setValid(true);
                                } else {
                                    MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, fromPosition.getLatitude(), fromPosition.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getLongitude(), dist);
                                    distance = distance + dist[0];
                                    deltaAltitude = Math.abs(fromPosition.getElevation() - lastAcceptedCoordinate.getElevation());
                                    fromPosition = new CoordinateGPS(lastAcceptedCoordinate);

                                    if (deltaAltitude >= 25.0) // nur aufaddieren wenn Höhenunterschied größer 10 Meter
                                    {
                                        fromPosition.setElevation(lastAcceptedCoordinate.getElevation());
                                        altitudeDifference = altitudeDifference + deltaAltitude;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException ex) {
            Log.err(log, "readFromGpxFile", ex);
        }
        for (Track track : tracks) {
            if (trackColor != null) track.setColor(trackColor);
            TrackList.getInstance().addTrack(track);
        }
        ShowTracks.getInstance().notifyDataSetChanged();

    }

    /**
     * Going to assume date is always in the form:<br>
     * 2006-05-25T08:55:01Z<br>
     * 2006-05-25T08:56:35Z<br>
     * <br>
     * i.e.: yyyy-mm-ddThh-mm-ssZ <br>
     * code from Tommi Laukkanen http://www.substanceofcode.com
     *
     * @param dateString ?
     * @return ?
     */
    private Date parseDate(String dateString) {
        try {
            final int year = Integer.parseInt(dateString.substring(0, 4));
            final int month = Integer.parseInt(dateString.substring(5, 7));
            final int day = Integer.parseInt(dateString.substring(8, 10));

            final int hour = Integer.parseInt(dateString.substring(11, 13));
            final int minute = Integer.parseInt(dateString.substring(14, 16));
            final int second = Integer.parseInt(dateString.substring(17, 19));

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1); // Beware MONTH was counted for 0 to 11, so we have to subtract 1
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);

            return calendar.getTime();
        } catch (Exception ex) {
            Log.err(log, "Exception caught trying to parse date : ", ex);
        }
        return null;
    }

    public static class DrawTrack {
        private final Color mColor;
        protected ArrayList<TrackPoint> trackPoints;
        double tracklength;
        Sprite arrow;
        Sprite point;
        float overlap;

        public DrawTrack(Color color, boolean isInternalRoutingTrack) {
            if (isInternalRoutingTrack) {
                arrow = new Sprite(Sprites.Arrows.get(5));
                point = new Sprite(Sprites.Arrows.get(10));
                arrow.scale(1.6f);
                point.scale(0.2f);
                overlap = 1.9f;
            } else {
                arrow = Sprites.Arrows.get(5);
                point = Sprites.Arrows.get(10);
                overlap = 0.9f;
            }
            mColor = color;
            trackPoints = new ArrayList<>();
            tracklength = 0;
        }

    }

}
