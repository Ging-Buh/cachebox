package de.droidcachebox.menu.menuBtn3.executes;

import static de.droidcachebox.menu.Action.ShowTracks;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Date;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.activities.ProjectionCoordinate;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.menu.menuBtn3.ShowTracks;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils;

public class TrackCreation extends AbstractShowAction {

    public TrackCreation() {
        super("");
    }

    @Override
    public CB_View_Base getView() {
        // don't return a view.
        // show menu direct.
        GL.that.runOnGL(getContextMenu()::show);
        return null;
    }

    @Override
    public void execute() {
        getContextMenu().show();
    }

    @Override
    public Sprite getIcon() {
        return null;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm2 = new Menu("TrackListViewCreateTrackTitle");
        cm2.addMenuItem("Point2Point", null, this::genTrackP2P);
        cm2.addMenuItem("Projection", null, this::genTrackProjection);
        cm2.addMenuItem("Circle", null, this::genTrackCircle);
        return cm2;
    }

    @Override
    public void viewIsHiding() {

    }

    private void genTrackP2P() {
        Coordinate coord = GlobalCore.getSelectedCoordinate();

        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate("projection", coord, (targetCoord, startCoord, bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            Track track = new Track("Point 2 Point Route");
            track.getTrackPoints().add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
            track.getTrackPoints().add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

            MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(), dist);
            track.setTrackLength(dist[0]);

            track.setVisible(true);
            TrackList.getInstance().addTrack(track);
            ((ShowTracks) ShowTracks.action).notifyDataSetChanged();
        }, ProjectionCoordinate.ProjectionType.point2point, Translation.get("fromPoint"));
        pC.show();

    }

    private void genTrackProjection() {
        Coordinate coord = GlobalCore.getSelectedCoordinate();
        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate(Translation.get("Projection"), coord, (targetCoord, startCoord, bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            Track track = new Track("Projected Route");

            track.getTrackPoints().add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
            track.getTrackPoints().add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

            MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(), dist);
            track.setTrackLength(dist[0]);

            track.setVisible(true);
            TrackList.getInstance().addTrack(track);
            ((ShowTracks) ShowTracks.action).notifyDataSetChanged();
        }, ProjectionCoordinate.ProjectionType.projection, Translation.get("fromPoint"));

        pC.show();

    }

    private void genTrackCircle() {
        Coordinate coord = GlobalCore.getSelectedCoordinate();
        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate(Translation.get("centerPoint"), coord, (targetCoord, startCoord, bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            Track track = new Track("Circle Route");

            track.setVisible(true);
            TrackList.getInstance().addTrack(track);

            Coordinate Projektion;
            Coordinate LastCoord = new CoordinateGPS(0, 0);

            // Achtung der Kreis darf nicht mehr als 50 Punkte haben, sonst gibt es Probleme mit dem Reduktionsalgorythmus
            for (int i = 0; i <= 360; i += 10) {
                Projektion = CoordinateGPS.project(startCoord.getLatitude(), startCoord.getLongitude(), i, distance);
                track.getTrackPoints().add(new TrackPoint(Projektion.getLongitude(), Projektion.getLatitude(), 0, 0, new Date()));
                if (LastCoord.isValid()) {
                    MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.ACCURATE, Projektion.getLatitude(), Projektion.getLongitude(), LastCoord.getLatitude(), LastCoord.getLongitude(), dist);
                    track.setTrackLength(track.getTrackLength() + dist[0]);
                }
                LastCoord = Projektion; // !! LastCoord = new Coordinate(Projektion);
                LastCoord.setValid(true);
            }
            ((ShowTracks) ShowTracks.action).notifyDataSetChanged();
        }, ProjectionCoordinate.ProjectionType.circle, Translation.get("centerPoint"));
        pC.show();
    }
}
