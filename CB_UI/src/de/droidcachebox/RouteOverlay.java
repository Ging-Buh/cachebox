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
package de.droidcachebox;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.MapView;
import de.droidcachebox.gdx.views.TrackListView;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.locator.map.PolylineReduction;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RouteOverlay {
    private final static String log = "RouteOverlay";
    private static RouteOverlay routeOverlay;
    private static Color[] colors = new Color[13];
    public boolean mRoutesChanged = false;
    public int aktCalcedZoomLevel = -1;
    private Track routingTrack; // for identifying the track! has been originally from openRouteService implementation. now from BRouter
    private ArrayList<Track> tracks;
    private ArrayList<Route> routes;
    private GlyphLayout glyphLayout;

    RouteOverlay() {
        tracks = new ArrayList<>();
        colors[0] = Color.RED;
        colors[1] = Color.YELLOW;
        colors[2] = Color.BLACK;
        colors[3] = Color.LIGHT_GRAY;
        colors[4] = Color.GREEN;
        colors[5] = Color.BLUE;
        colors[6] = Color.CYAN;
        colors[7] = Color.GRAY;
        colors[8] = Color.MAGENTA;
        colors[9] = Color.ORANGE;
        colors[10] = Color.DARK_GRAY;
        colors[11] = Color.PINK;
        colors[12] = Color.WHITE;
    }

    public static RouteOverlay getInstance() {
        if (routeOverlay == null) routeOverlay = new RouteOverlay();
        return routeOverlay;
    }

    public Color getNextColor() {
        return colors[(tracks.size()) % colors.length];
    }

    public void trackListChanged() {
        mRoutesChanged = true;
        GL.that.renderOnce();
    }

    public Track readFromGpxFile(String file, Color color) {
        // !!! it is possible that a gpx file contains more than 1 <trk> segments
        float[] dist = new float[4];
        double distance = 0;
        double altitudeDifference = 0;
        double deltaAltitude;
        CoordinateGPS fromPosition = new CoordinateGPS(0, 0);
        BufferedReader reader;

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            Track track = new Track(null, color);

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
                                track = new Track(null, color);
                                track.setFileName(file);
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(FileIO.getFileName(file));
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
                                track = new Track(null, color);
                                track.setFileName(file);
                                distance = 0;
                                altitudeDifference = 0;
                                anzSegments++;
                                if (gpxName == null)
                                    track.setName(FileIO.getFileName(file));
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
                                if (isSeg | isRte)
                                    track.setName(tmpLine.substring(name_start, name_end));
                                else
                                    gpxName = tmpLine.substring(name_start, name_end);
                            }

                            readName = false;
                            continue;
                        }

                        if (line.contains("</trkseg>")) // End of the Track Segment detected?
                        {
                            if (track.trackPoints.size() < 2)
                                track.setName("no Route segment found");
                            track.isVisible = true;
                            track.trackLength = distance;
                            track.altitudeDifference = altitudeDifference;
                            tracks.add(track);
                            trackListChanged();
                            isSeg = false;
                            break;
                        }

                        if (line.contains("</rte>")) // End of the Route detected?
                        {
                            if (track.trackPoints.size() < 2)
                                track.setName("no Route segment found");
                            track.isVisible = true;
                            track.trackLength = distance;
                            track.altitudeDifference = altitudeDifference;
                            tracks.add(track);
                            trackListChanged();
                            isRte = false;
                            break;
                        }

                        if ((line.contains("<trkpt")) | (line.contains("<rtept"))) {
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
                            color = new HSV_Color(couStr);
                            track.setColor(color);
                        }

                        if ((line.contains("</trkpt>")) | (line.contains("</rtept>")) | ((line.contains("/>")) & isTrkptOrRtept)) {
                            // trkpt abgeschlossen, jetzt kann der Trackpunkt erzeugt werden
                            isTrkptOrRtept = false;
                            if (lastAcceptedCoordinate != null) {
                                track.trackPoints.add(new TrackPoint(lastAcceptedCoordinate.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getElevation(), lastAcceptedDirection, lastAcceptedTime));

                                // Calculate the length of a Track
                                if (!fromPosition.isValid()) {
                                    fromPosition = new CoordinateGPS(lastAcceptedCoordinate);
                                    fromPosition.setElevation(lastAcceptedCoordinate.getElevation());
                                    fromPosition.setValid(true);
                                } else {
                                    MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, fromPosition.getLatitude(), fromPosition.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getLongitude(), dist);
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
            return track;
        } catch (IOException ex) {
            Log.err(log, "readFromGpxFile", ex);
            return null;
        }
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

    public void renderRoute(Batch batch, MapView mapView) {

        int Zoom = mapView.getAktZoom();
        float yVersatz = mapView.ySpeedVersatz;

        if (aktCalcedZoomLevel != Zoom || mRoutesChanged) {// Zoom or Routes changed => calculate new Sprite Points

            // Log.debug(log, "Zoom Changed => Calc Track Points");

            mRoutesChanged = false;
            aktCalcedZoomLevel = Zoom;
            if (routes == null)
                routes = new ArrayList<>();
            else
                routes.clear();

            double tolerance = 0.01 * Math.exp(-1 * (Zoom - 11));

            // Log.info(log, "Number of Routes to show: " + Routes.size());
            for (Track track : tracks) {
                if (track != null && track.isVisible) {
                    addToDrawRoutes(tolerance, track, Zoom);
                }
            }

            if (GlobalCore.aktuelleRoute != null && GlobalCore.aktuelleRoute.isVisible) {
                addToDrawRoutes(tolerance, GlobalCore.aktuelleRoute, Zoom);
            }

        }

        // DrawedLineCount = 0;

        if (routes != null && routes.size() > 0) {
            for (Route route : routes) {

                Sprite arrow = route.arrow;
                Sprite point = route.point;
                float overlap = route.overlap;
                arrow.setColor(route.mColor);
                point.setColor(route.mColor);
                float scale = UiSizes.getInstance().getScale();

                for (int ii = 0; ii < route.trackPoints.size() - 1; ii++) {

                    double mapX1 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, route.trackPoints.get(ii).x);
                    double mapY1 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, route.trackPoints.get(ii).y);

                    double mapX2 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, route.trackPoints.get(ii + 1).x);
                    double mapY2 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, route.trackPoints.get(ii + 1).y);

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
                        if (ii == route.trackPoints.size() - 2) {
                            try {
                                drawText(batch, UnitFormatter.distanceString((float) route.tracklength), screen2);
                            } catch (Exception ex) {
                                Log.err(log, "for loop: " + route.tracklength, ex);
                            }
                        }
                    }
                }

            }
        }
    }

    private void drawText(Batch batch, String text, Vector2 position) {
        try {
            Fonts.getSmall().setColor(COLOR.getFontColor());
            if (glyphLayout == null)
                glyphLayout = new GlyphLayout(Fonts.getSmall(), text);
            else
                glyphLayout.setText(Fonts.getSmall(), text);
            float halfWidth = glyphLayout.width / 2;
            Fonts.getSmall().draw(batch, glyphLayout, position.x - halfWidth, position.y);
        } catch (Exception ex) {
            Log.err(log, "drawText", ex);
        }
    }

    private void addToDrawRoutes(double tolerance, Track track, int zoom) {

        synchronized (track.trackPoints) {

            ArrayList<TrackPoint> reducedPoints;

            // reduce no points for zoom >= 18
            if (zoom >= 18) {
                reducedPoints = track.trackPoints;
            } else {
                reducedPoints = PolylineReduction.polylineReduction(track.trackPoints, tolerance);
                // Log.info(log, "Track: " + track.FileName + " has " + track.Points.size() + ". reduced to " + reducedPoints.size() + " at Zoom = " + zoom);
                if (reducedPoints.size() == 2) {
                    reducedPoints = track.trackPoints;
                }
            }

            // AllTrackPoints = track.Points.size();
            // ReduceTrackPoints = reducedPoints.size();

            Route route = new Route(track.getColor(), track == routingTrack);
            route.trackPoints = reducedPoints;
            route.tracklength = track.trackLength;

            routes.add(route);

        }

    }

    public void loadTrack(String trackPath, String file) {
        String absolutPath;
        if (file.equals("")) {
            absolutPath = trackPath;
        } else {
            absolutPath = trackPath + "/" + file;
        }
        readFromGpxFile(absolutPath, getNextColor());
    }

    public void remove(Track track) {
        if (track == routingTrack) {
            routingTrack = null;
        }
        tracks.remove(track);
        trackListChanged();
    }

    /**
     * Dont use this for internal RoutingTrack!! Use setRoutingTrack(Track route)
     *
     * @param track ?
     */
    public void addTrack(Track track) {
        tracks.add(track);
        trackListChanged();
    }

    public void setRoutingTrack(Track track) {
        if (routingTrack == null) {
            track.setColor(new Color(0.85f, 0.1f, 0.2f, 1f));
        } else {
            // erst die alte route löschen
            tracks.remove(routingTrack);
            track.setColor(routingTrack.getColor());
        }
        tracks.add(0, track);
        routingTrack = track;
        trackListChanged();
    }

    public boolean existsRoutingTrack() {
        return routingTrack != null;
    }

    public void removeRoutingTrack() {
        tracks.remove(routingTrack);
        routingTrack = null;
        trackListChanged();
    }

    public int getNumberOfTracks() {
        return tracks.size();
    }

    public Track getTrack(int position) {
        return tracks.get(position);
    }

    public void loadTrackList() {
        PlatformUIBase.getFile(CB_UI_Settings.TrackFolder.getValue(), "*.gpx", Translation.get("LoadTrack"), Translation.get("load"), path -> {
            if (path != null) {
                readFromGpxFile(path, getNextColor());
                Log.debug(log, "Load Track :" + path);
                TrackListView.getInstance().notifyDataSetChanged();
            }
        });
    }

    public static class Route {
        private final Color mColor;
        protected ArrayList<TrackPoint> trackPoints;
        double tracklength;
        Sprite arrow;
        Sprite point;
        float overlap;

        public Route(Color color) {
            mColor = color;
            trackPoints = new ArrayList<>();
            arrow = Sprites.Arrows.get(5); // 5 = track-line
            point = Sprites.Arrows.get(10); // 10 = track-point
            overlap = 0.9f;
            tracklength = 0;
        }

        public Route(Color color, boolean isInternalRoutingTrack) {
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
