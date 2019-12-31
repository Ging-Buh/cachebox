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
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.MapView;
import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.locator.map.PolylineReduction;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;

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
 * further the GlobalCore.aktuelleRoute is addToTracksToDraw for rendering. (if tracking has been switched on)
 */
public class RouteOverlay {
    private final static String log = "RouteOverlay";
    private static RouteOverlay routeOverlay;
    private boolean aTrackChanged;
    private int thisZoom;
    private ArrayList<Track> tracks;
    private Track routingTrack; // for identifying the track! has been originally from openRouteService implementation. now from BRouter
    // for rendering
    private ArrayList<DrawTrack> tracksToDraw;
    private GlyphLayout glyphLayout;

    RouteOverlay() {
        tracks = new ArrayList<>();
        aTrackChanged = false;
        thisZoom = -1;
        tracksToDraw = new ArrayList<>();
    }

    public static RouteOverlay getInstance() {
        if (routeOverlay == null) routeOverlay = new RouteOverlay();
        return routeOverlay;
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

        int mapZoom = mapView.getAktZoom();
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

            if (GlobalCore.aktuelleRoute != null && GlobalCore.aktuelleRoute.isVisible()) {
                addToTracksToDraw(tolerance, GlobalCore.aktuelleRoute, mapZoom);
            }

        }

        if (tracksToDraw != null && tracksToDraw.size() > 0) {
            for (DrawTrack drawTrack : tracksToDraw) {
                Sprite arrow = drawTrack.arrow;
                Sprite point = drawTrack.point;
                float overlap = drawTrack.overlap;
                arrow.setColor(drawTrack.mColor);
                point.setColor(drawTrack.mColor);
                float scale = UiSizes.getInstance().getScale();

                for (int ii = 0; ii < drawTrack.trackPoints.size() - 1; ii++) {

                    double mapX1 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, drawTrack.trackPoints.get(ii).x);
                    double mapY1 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, drawTrack.trackPoints.get(ii).y);

                    double mapX2 = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, drawTrack.trackPoints.get(ii + 1).x);
                    double mapY2 = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, drawTrack.trackPoints.get(ii + 1).y);

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
            Fonts.getSmall().setColor(COLOR.getFontColor());
            if (glyphLayout == null)
                glyphLayout = new GlyphLayout(Fonts.getSmall(), text);
            else
                glyphLayout.setText(Fonts.getSmall(), text);
            float halfWidth = glyphLayout.width / 2;
            Fonts.getSmall().draw(batch, glyphLayout, position.x - halfWidth, position.y - 2 * glyphLayout.height);
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

    public static class DrawTrack {
        private final Color mColor;
        protected ArrayList<TrackPoint> trackPoints;
        double tracklength;
        Sprite arrow;
        Sprite point;
        float overlap;

        /*
        public DrawTrack(Color color) {
            mColor = color;
            trackPoints = new ArrayList<>();
            arrow = Sprites.Arrows.get(5); // 5 = track-line
            point = Sprites.Arrows.get(10); // 10 = track-point
            overlap = 0.9f;
            tracklength = 0;
        }
         */

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
