package de.droidcachebox.maps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Xml;
import de.droidcachebox.Config;
import de.droidcachebox.Main;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.utils.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Date;

import static de.droidcachebox.utils.MathUtils.DEG_RAD;

public class BRouter implements Router {
    private final static String sKlasse = "BRouter";
    private Activity mainActivity;
    private final Intent intent;

    private BRouterServiceConnection brouter;

    public BRouter(Main main) {
        mainActivity = main;
        brouter = null;
        intent = new Intent();
        intent.setClassName("btools.routingapp", "btools.routingapp.BRouterService");
    }

    public boolean open() {
        if (brouter == null) brouter = new BRouterServiceConnection();
        if (brouter.isConnected()) return true;
        if (mainActivity.bindService(intent, brouter, Context.BIND_AUTO_CREATE)) {
            Log.info(sKlasse, "Bind service successful!");
            return true;
        } else {
            Log.err(sKlasse, "Connect BRouter failed");
            brouter = null;
            return false;
        }
    }

    public void close() {
        if (brouter != null) {
            mainActivity.unbindService(brouter);
            brouter = null;
        }
    }

    public Track getTrack(final Coordinate start, final Coordinate dest) {
        final Bundle params = new Bundle();
        params.putString("trackFormat", "gpx");
        params.putDoubleArray("lats", new double[]{start.getLatitude(), dest.getLatitude()});
        params.putDoubleArray("lons", new double[]{start.getLongitude(), dest.getLongitude()});
        String routeProfile;
        switch (Config.routeProfile.getValue()) {
            case 0: routeProfile = "foot"; break;
            case 1: routeProfile = "bicycle"; break;
            default: routeProfile = "motorcar";
        }
        params.putString("v", routeProfile);
        Track track = new Track("", null);
        try {
            Xml.parse(brouter.getTrackFromParams(params), new DefaultHandler() {
                @Override
                public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                    if (qName.equalsIgnoreCase("trkpt")) {
                        final String lat = atts.getValue("lat");
                        if (lat != null) {
                            final String lon = atts.getValue("lon");
                            if (lon != null) {
                                track.trackPoints.add( new TrackPoint(Double.parseDouble(lon), Double.parseDouble(lat), 0, 0, new Date()));
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            Log.err("Brouter", "getTrack", ex);
        }
        // calc tracklength
        double sourceLatitude = start.getLatitude();
        double sourceLongitude = start.getLongitude();
        for (int i = 0; i < track.trackPoints.size(); i++) {
            TrackPoint t = track.trackPoints.get(i);
            track.trackLength = track.trackLength + calcDistance(sourceLatitude, sourceLongitude, t.y, t.x);
            sourceLatitude = t.y;
            sourceLongitude = t.x;
        }
        return track;
    }

    private double calcDistance(double sourceLat, double sourceLon, double targetLat, double targetLon) {
        double lat1 = sourceLat * DEG_RAD;
        double lon1 = sourceLon * DEG_RAD;
        double lat2 = targetLat * DEG_RAD;
        double lon2 = targetLon * DEG_RAD;
        return (6378137 * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos((lon2 - lon1))));
    }

    /**
     * Return a valid track (with at least two points, including the start and destination).
     * In some cases (e.g., destination is too close or too far, path could not be found),
     * a straight line will be returned.
     *
     * @param start the starting point
     * @param destination the destination point
     * @return a track with at least two points including the start and destination points
     */
    /*
    @NonNull
    public static Geopoint[] getTrack(final Geopoint start, final Geopoint destination) {
        if (brouter == null || Settings.getRoutingMode() == RoutingMode.STRAIGHT) {
            return defaultTrack(start, destination);
        }

        // avoid updating to frequently
        final long timeNow = System.currentTimeMillis();
        if ((timeNow - timeLastUpdate) < 1000 * UPDATE_MIN_DELAY_SECONDS) {
            return ensureTrack(lastRoutingPoints, start, destination);
        }

        // Disable routing for huge distances
        final int maxThresholdKm = Settings.getBrouterThreshold();
        final float targetDistance = start.distanceTo(destination);
        if (targetDistance > maxThresholdKm) {
            return defaultTrack(start, destination);
        }

        // disable routing when near the target
        if (targetDistance < MIN_ROUTING_DISTANCE_KILOMETERS) {
            return defaultTrack(start, destination);
        }

        // Use cached route if current position has not changed more than 5m and we had a route
        // TODO: Maybe adjust this to current zoomlevel
        if (lastDirectionUpdatePoint != null && destination == lastDestination && start.distanceTo(lastDirectionUpdatePoint) < UPDATE_MIN_DISTANCE_KILOMETERS && lastRoutingPoints != null) {
            return lastRoutingPoints;
        }

        // now really calculate a new route
        lastDestination = destination;
        lastRoutingPoints = calculateRouting(start, destination);
        lastDirectionUpdatePoint = start;
        timeLastUpdate = timeNow;
        return ensureTrack(lastRoutingPoints, start, destination);
    }

    @NonNull
    private static Geopoint[] ensureTrack(@Nullable final Geopoint[] routingPoints, final Geopoint start, final Geopoint destination) {
        return routingPoints != null ? routingPoints : defaultTrack(start, destination);
    }

    @NonNull
    private static Geopoint[] defaultTrack(final Geopoint start, final Geopoint destination) {
        return new Geopoint[] { start, destination };
    }

    @Nullable
    private static Geopoint[] parseGpxTrack(@NonNull final String gpx, final Geopoint destination) {
        try {
            final LinkedList<Geopoint> result = new LinkedList<>();
            Xml.parse(gpx, new DefaultHandler() {
                @Override
                public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                    if (qName.equalsIgnoreCase("trkpt")) {
                        final String lat = atts.getValue("lat");
                        if (lat != null) {
                            final String lon = atts.getValue("lon");
                            if (lon != null) {
                                result.add(new Geopoint(lat, lon));
                            }
                        }
                    }
                }
            });

            // artificial straight line from track to target
            result.add(destination);

            return result.toArray(new Geopoint[result.size()]);

        } catch (final SAXException e) {
            Log.w("cannot parse brouter output of length " + gpx.length(), e);
        }
        return null;
    }

    public static void invalidateRouting() {
        lastDirectionUpdatePoint = null;
        timeLastUpdate = 0;
    }

     */


}
