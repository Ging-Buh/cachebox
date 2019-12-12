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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.gdx.DrawUtils;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.MapView;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.locator.map.PolylineReduction;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.log.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RouteOverlay {
    private static final String log = "RouteOverlay";
    public static boolean mRoutesChanged = false;
    public static int aktCalcedZoomLevel = -1;
    private static Track internalRoutingTrack; // for identifying the track! has been originally from openRouteService implementation. now from BRouter
    private static ArrayList<Track> tracks = new ArrayList<>();
    private static Color[] colors = new Color[13];
    private static ArrayList<Route> routes;

    public static Color getNextColor() {
        Color ret = colors[(tracks.size()) % colors.length];
        if (ret == null)
            initialColorField();
        return colors[(tracks.size()) % colors.length];
    }

    private static void initialColorField() {
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

    // Debug
    // public static int AllTrackPoints = 0;
    // public static int ReduceTrackPoints = 0;
    // public static int DrawedLineCount = 0;

    public static void routesChanged() {
        mRoutesChanged = true;
        GL.that.renderOnce();
    }

    // Read track from gpx file. !!! it is possible that a gpx file contains more than 1 <trk> segments
    public static Track multiLoadRoute(String file, Color color) {
        float[] dist = new float[4];
        double Distance = 0;
        double AltitudeDifference = 0;
        double DeltaAltitude;
        CoordinateGPS FromPosition = new CoordinateGPS(0, 0);
        BufferedReader reader;

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            Track track = new Track(null, color);

            String line;
            String tmpLine;
            String GPXName = null;
            boolean isSeg = false;
            boolean isTrk = false;
            boolean isRte = false;
            boolean IStrkptORrtept = false;
            boolean ReadName = false;
            int AnzTracks = 0;

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
                                track.fileName = file;
                                Distance = 0;
                                AltitudeDifference = 0;
                                AnzTracks++;
                                if (GPXName == null)
                                    track.name = FileIO.getFileName(file);
                                else {
                                    if (AnzTracks <= 1)
                                        track.name = GPXName;
                                    else
                                        track.name = GPXName + AnzTracks;
                                }
                                continue;
                            }
                        }

                        if (!isRte) // Begin of the Route detected?
                        {
                            if (line.contains("<rte>")) {
                                isRte = true;
                                track = new Track(null, color);
                                track.fileName = file;
                                Distance = 0;
                                AltitudeDifference = 0;
                                AnzTracks++;
                                if (GPXName == null)
                                    track.name = FileIO.getFileName(file);
                                else {
                                    if (AnzTracks <= 1)
                                        track.name = GPXName;
                                    else
                                        track.name = GPXName + AnzTracks;
                                }
                                continue;
                            }
                        }

                        if ((line.contains("<name>")) & !IStrkptORrtept) // found <name>?
                        {
                            ReadName = true;
                            continue;
                        }

                        if (ReadName & !IStrkptORrtept) {
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
                                    track.name = tmpLine.substring(name_start, name_end);
                                else
                                    GPXName = tmpLine.substring(name_start, name_end);
                            }

                            ReadName = false;
                            continue;
                        }

                        if (line.contains("</trkseg>")) // End of the Track Segment detected?
                        {
                            if (track.trackPoints.size() < 2)
                                track.name = "no Route segment found";
                            track.showRoute = true;
                            track.trackLength = Distance;
                            track.altitudeDifference = AltitudeDifference;
                            add(track);
                            isSeg = false;
                            break;
                        }

                        if (line.contains("</rte>")) // End of the Route detected?
                        {
                            if (track.trackPoints.size() < 2)
                                track.name = "no Route segment found";
                            track.showRoute = true;
                            track.trackLength = Distance;
                            track.altitudeDifference = AltitudeDifference;
                            add(track);
                            isRte = false;
                            break;
                        }

                        if ((line.contains("<trkpt")) | (line.contains("<rtept"))) {
                            IStrkptORrtept = true;
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

                        if ((line.contains("</ele>")) & IStrkptORrtept) {
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

                        if ((line.contains("</trkpt>")) | (line.contains("</rtept>")) | ((line.contains("/>")) & IStrkptORrtept)) {
                            // trkpt abgeschlossen, jetzt kann der Trackpunkt erzeugt werden
                            IStrkptORrtept = false;
                            if (lastAcceptedCoordinate != null) {
                                track.trackPoints.add(new TrackPoint(lastAcceptedCoordinate.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getElevation(), lastAcceptedDirection, lastAcceptedTime));

                                // Calculate the length of a Track
                                if (!FromPosition.isValid()) {
                                    FromPosition = new CoordinateGPS(lastAcceptedCoordinate);
                                    FromPosition.setElevation(lastAcceptedCoordinate.getElevation());
                                    FromPosition.setValid(true);
                                } else {
                                    MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, FromPosition.getLatitude(), FromPosition.getLongitude(), lastAcceptedCoordinate.getLatitude(), lastAcceptedCoordinate.getLongitude(), dist);
                                    Distance += dist[0];
                                    DeltaAltitude = Math.abs(FromPosition.getElevation() - lastAcceptedCoordinate.getElevation());
                                    FromPosition = new CoordinateGPS(lastAcceptedCoordinate);

                                    if (DeltaAltitude >= 25.0) // nur aufaddieren wenn Höhenunterschied größer 10 Meter
                                    {
                                        FromPosition.setElevation(lastAcceptedCoordinate.getElevation());
                                        AltitudeDifference = AltitudeDifference + DeltaAltitude;
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
            Log.err(log, "multiLoadRoute", ex);
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
    private static Date parseDate(String dateString) {
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

    public static void renderRoute(Batch batch, MapView mapView) {

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
                if (track != null && track.showRoute) {
                    addToDrawRoutes(tolerance, track, Zoom);
                }
            }

            if (GlobalCore.AktuelleRoute != null && GlobalCore.AktuelleRoute.showRoute) {
                addToDrawRoutes(tolerance, GlobalCore.AktuelleRoute, Zoom);
            }

        }

        // DrawedLineCount = 0;

        if (routes != null && routes.size() > 0) {
            for (Route route : routes) {

                Sprite ArrowSprite = route.ArrowSprite;
                Sprite PointSprite = route.PointSprite;
                float overlap = route.overlap;
                ArrowSprite.setColor(route.mColor);
                PointSprite.setColor(route.mColor);
                float scale = UiSizes.getInstance().getScale();

                for (int ii = 0; ii < route.trackPoints.size() - 1; ii++) {

                    double MapX1 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, route.trackPoints.get(ii).X);
                    double MapY1 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, route.trackPoints.get(ii).Y);

                    double MapX2 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, route.trackPoints.get(ii + 1).X);
                    double MapY2 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, route.trackPoints.get(ii + 1).Y);

                    Vector2 screen1 = mapView.worldToScreen(new Vector2((float) MapX1, (float) MapY1));
                    Vector2 screen2 = mapView.worldToScreen(new Vector2((float) MapX2, (float) MapY2));

                    screen1.y -= yVersatz;
                    screen2.y -= yVersatz;

                    CB_RectF chkRec = new CB_RectF(mapView);
                    chkRec.setPos(0, 0);

                    // chk if line on Screen
                    if (chkRec.contains(screen1.x, screen1.y) || chkRec.contains(screen2.x, screen2.y)) {
                        DrawUtils.drawSpriteLine(batch, ArrowSprite, PointSprite, overlap * scale, screen1.x, screen1.y, screen2.x, screen2.y);
                        // DrawedLineCount++;
                    } else {// chk if intersection
                        if (chkRec.getIntersection(screen1, screen2, 2) != null) {
                            DrawUtils.drawSpriteLine(batch, ArrowSprite, PointSprite, overlap * scale, screen1.x, screen1.y, screen2.x, screen2.y);
                            // DrawedLineCount++;
                        }

                        // the line is not on the screen
                    }

                }

            }
        }
    }

    private static void addToDrawRoutes(double tolerance, Track track, int zoom) {

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

            Route route = new Route(track.color, track == internalRoutingTrack);
            route.trackPoints = reducedPoints;

            routes.add(route);

        }

    }

    public static void saveRoute(String Path, Track track) {
        FileWriter writer = null;
        File gpxfile = FileFactory.createFile(Path);
        try {
            writer = gpxfile.getFileWriter();
            try {
                writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.append(
                        "<gpx version=\"1.0\" creator=\"cachebox track recorder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");

                Date now = new Date();
                SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                String sDate = datFormat.format(now);
                datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                sDate += "T" + datFormat.format(now) + "Z";
                writer.append("<time>").append(sDate).append("</time>\n");

                writer.append("<bounds minlat=\"-90\" minlon=\"-180\" maxlat=\"90\" maxlon=\"180\"/>\n");

                writer.append("<trk>\n");
                writer.append("<name>").append(track.name).append("</name>\n");
                writer.append("<extensions>\n<gpxx:TrackExtension>\n");
                writer.append("<gpxx:ColorRGB>").append(track.color.toString()).append("</gpxx:ColorRGB>\n");
                writer.append("</gpxx:TrackExtension>\n</extensions>\n");
                writer.append("<trkseg>\n");
                writer.flush();
            } catch (IOException e) {
                Log.err(log, "SaveTrack", e);
            }
        } catch (IOException e1) {
            Log.err(log, "SaveTrack", e1);
        }

        if (writer != null) {
            try {
                for (int i = 0; i < track.trackPoints.size(); i++) {
                    writer.append("<trkpt lat=\"").append(String.valueOf(track.trackPoints.get(i).Y)).append("\" lon=\"").append(String.valueOf(track.trackPoints.get(i).X)).append("\">\n");

                    writer.append("   <ele>").append(String.valueOf(track.trackPoints.get(i).Elevation)).append("</ele>\n");
                    SimpleDateFormat datFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    String sDate = datFormat.format(track.trackPoints.get(i).TimeStamp);
                    datFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    sDate += "T" + datFormat.format(track.trackPoints.get(i).TimeStamp) + "Z";
                    writer.append("   <time>").append(sDate).append("</time>\n");
                    writer.append("</trkpt>\n");
                }
                writer.append("</trkseg>\n");
                writer.append("</trk>\n");
                writer.append("</gpx>\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                Log.err(log, "SaveTrack", e);
            }
        }
    }

    public static void loadTrack(String trackPath, String file) {

        String absolutPath;
        if (file.equals("")) {
            absolutPath = trackPath;
        } else {
            absolutPath = trackPath + "/" + file;
        }
        multiLoadRoute(absolutPath, getNextColor());
    }

    public static void remove(Track track) {
        if (track == internalRoutingTrack) {
            internalRoutingTrack = null;
        }
        tracks.remove(track);
        routesChanged();
    }

    /**
     * Dont use this for InternalRoutingTrack!! Use addInternalRoutingTrack(Track route)
     *
     * @param track ?
     */
    public static void add(Track track) {
        tracks.add(track);
        routesChanged();
    }

    public static void addInternalRoutingTrack(Track track) {
        if (internalRoutingTrack == null) {
            track.setColor(new Color(0.85f, 0.1f, 0.2f, 1f));
        } else {
            // erst die alte route löschen
            tracks.remove(internalRoutingTrack);
            track.setColor(internalRoutingTrack.getColor());
        }
        tracks.add(0, track);
        internalRoutingTrack = track;

        routesChanged();
    }

    public static int getRouteCount() {
        return tracks.size();
    }

    public static Track getRoute(int position) {
        return tracks.get(position);
    }

    public static class Route {
        private final Color mColor;
        protected ArrayList<TrackPoint> trackPoints;
        Sprite ArrowSprite;
        Sprite PointSprite;
        float overlap;
        private boolean isInternalRoutingTrack = false;

        public Route(Color color) {
            mColor = color;
            trackPoints = new ArrayList<>();
            ArrowSprite = Sprites.Arrows.get(5); // 5 = track-line
            PointSprite = Sprites.Arrows.get(10); // 10 = track-point
            overlap = 0.9f;
        }

        public Route(Color color, boolean isInternalRoutingTrack) {
            this.isInternalRoutingTrack = isInternalRoutingTrack;
            if (isInternalRoutingTrack) {
                ArrowSprite = new Sprite(Sprites.Arrows.get(5));
                PointSprite = new Sprite(Sprites.Arrows.get(10));
                ArrowSprite.scale(1.6f);
                PointSprite.scale(0.2f);
                overlap = 1.9f;
            } else {
                ArrowSprite = Sprites.Arrows.get(5);
                PointSprite = Sprites.Arrows.get(10);
                overlap = 0.9f;
            }
            mColor = color;
            trackPoints = new ArrayList<>();
        }

        public boolean isInternalRoutingTrack() {
            return isInternalRoutingTrack;
        }

    }

}
