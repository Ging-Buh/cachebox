package de.droidcachebox.gdx.views;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.RouteOverlay;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.activities.ProjectionCoordinate;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils;

import java.util.Date;

public class TrackCreation {
    private static TrackCreation trackCreation;

    private TrackCreation() {
        Menu cm2 = new Menu("TrackListViewCreateTrackTitle");
        cm2.addMenuItem("Point2Point", null, trackCreation::genTrackP2P);
        cm2.addMenuItem("Projection", null, trackCreation::genTrackProjection);
        cm2.addMenuItem("Circle", null, trackCreation::genTrackCircle);
        cm2.show();
    }

    public static void getInstance() {
        if (trackCreation == null) {
            trackCreation = new TrackCreation();
        }
    }

    private void genTrackP2P() {
        Coordinate coord = GlobalCore.getSelectedCoordinate();

        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.activityRec(), Translation.get("fromPoint"), coord, (targetCoord, startCoord, Bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            Track route = new Track(null, RouteOverlay.getInstance().getNextColor());

            route.setName("Point 2 Point Route");
            route.trackPoints.add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
            route.trackPoints.add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

            MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(), dist);
            route.trackLength = dist[0];

            route.isVisible = true;
            RouteOverlay.getInstance().addTrack(route);
            TrackListView.getInstance().notifyDataSetChanged();
        }, ProjectionCoordinate.Type.p2p, null);
        pC.show();

    }

    private void genTrackProjection() {
        Coordinate coord = GlobalCore.getSelectedCoordinate();
        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.activityRec(), Translation.get("Projection"), coord, (targetCoord, startCoord, Bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            Track route = new Track(null, RouteOverlay.getInstance().getNextColor());
            route.setName("Projected Route");

            route.trackPoints.add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
            route.trackPoints.add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

            MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(), dist);
            route.trackLength = dist[0];

            route.isVisible = true;
            RouteOverlay.getInstance().addTrack(route);
            TrackListView.getInstance().notifyDataSetChanged();
        }, ProjectionCoordinate.Type.projetion, null);

        pC.show();

    }

    private void genTrackCircle() {
        Coordinate coord = GlobalCore.getSelectedCoordinate();
        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.activityRec(), Translation.get("centerPoint"), coord, (targetCoord, startCoord, Bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            Track track = new Track(null, RouteOverlay.getInstance().getNextColor());
            track.setName("Circle Route");

            track.isVisible = true;
            RouteOverlay.getInstance().addTrack(track);

            Coordinate Projektion;
            Coordinate LastCoord = new CoordinateGPS(0, 0);

            // Achtung der Kreis darf nicht mehr als 50 Punkte haben, sonst gibt es Probleme mit dem Reduktionsalgorythmus
            for (int i = 0; i <= 360; i += 10) {
                Projektion = CoordinateGPS.Project(startCoord.getLatitude(), startCoord.getLongitude(), i, distance);
                track.trackPoints.add(new TrackPoint(Projektion.getLongitude(), Projektion.getLatitude(), 0, 0, new Date()));
                if (LastCoord.isValid()) {
                    MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, Projektion.getLatitude(), Projektion.getLongitude(), LastCoord.getLatitude(), LastCoord.getLongitude(), dist);
                    track.trackLength = track.trackLength + dist[0];
                }
                LastCoord = Projektion; // !! LastCoord = new Coordinate(Projektion);
                LastCoord.setValid(true);
            }
            TrackListView.getInstance().notifyDataSetChanged();
        }, ProjectionCoordinate.Type.circle, null);
        pC.show();
    }

}
