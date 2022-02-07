package de.droidcachebox.maps;

import static de.droidcachebox.utils.MathUtils.DEG_RAD;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Date;

import de.droidcachebox.Main;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.log.Log;

public class BRouter implements Router {
    private final static String sClass = "BRouter";
    private final Intent intent;
    private final Activity mainActivity;
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
            Log.debug(sClass, "Bind service successful!");
            return true;
        } else {
            Log.err(sClass, "Connect BRouter failed");
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
        switch (Settings.routeProfile.getValue()) {
            case 0:
                routeProfile = "foot";
                break;
            case 1:
                routeProfile = "bicycle";
                break;
            default:
                routeProfile = "motorcar";
        }
        params.putString("v", routeProfile);
        Track track = new Track("");
        try {
            Xml.parse(brouter.getTrackFromParams(params), new DefaultHandler() {
                @Override
                public void startElement(final String uri, final String localName, final String qName, final Attributes atts) {
                    if (qName.equalsIgnoreCase("trkpt")) {
                        final String lat = atts.getValue("lat");
                        if (lat != null) {
                            final String lon = atts.getValue("lon");
                            if (lon != null) {
                                track.getTrackPoints().add(new TrackPoint(Double.parseDouble(lon), Double.parseDouble(lat), 0, 0, new Date()));
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
        for (int i = 0; i < track.getTrackPoints().size(); i++) {
            TrackPoint t = track.getTrackPoints().get(i);
            track.setTrackLength(track.getTrackLength() + calcDistance(sourceLatitude, sourceLongitude, t.y, t.x));
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

}
