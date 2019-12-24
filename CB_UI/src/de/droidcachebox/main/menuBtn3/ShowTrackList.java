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
package de.droidcachebox.main.menuBtn3;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.*;
import de.droidcachebox.PlatformUIBase.IgetFileReturnListener;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.ProjectionCoordinate;
import de.droidcachebox.gdx.activities.ProjectionCoordinate.Type;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.gdx.views.TrackListView;
import de.droidcachebox.gdx.views.TrackListViewItem;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.main.AbstractShowAction;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.log.Log;

import java.util.Date;

public class ShowTrackList extends AbstractShowAction {
    private static final String log = "ShowTrackList";
    private static ShowTrackList that;
    private Color TrackColor;

    private ShowTrackList() {
        super("Tracks", MenuID.AID_SHOW_TRACKLIST);
    }

    public static ShowTrackList getInstance() {
        if (that == null) that = new ShowTrackList();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(TrackListView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.trackListIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return TrackListView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("TrackListViewContextMenuTitle");
        cm.addMenuItem("load", null, this::loadTrackList);
        cm.addMenuItem("generate", null, this::showMenuCreate);

        // rename, save, delete darf nicht mit dem aktuellen Track gemacht werden....
        TrackListViewItem selectedTrackItem = TrackListView.getInstance().getSelectedItem();
        if (selectedTrackItem != null && !selectedTrackItem.getTrack().isActualTrack) {
            cm.addMenuItem("rename", null, () -> {
                StringInputBox.Show(WrapType.SINGLELINE, selectedTrackItem.getTrack().getName(), Translation.get("RenameTrack"), selectedTrackItem.getTrack().getName(), (which, data) -> {
                    String text = StringInputBox.editText.getText();
                    switch (which) {
                        case 1: // ok Clicket
                            selectedTrackItem.getTrack().setName(text);
                            TrackListView.getInstance().notifyDataSetChanged();
                            break;
                        case 2: // cancel clicket
                            break;
                        case 3:
                            break;
                    }

                    return true;
                });
                TrackListView.getInstance().notifyDataSetChanged();
            });
            cm.addMenuItem("save", null, () -> PlatformUIBase.getFile(CB_UI_Settings.TrackFolder.getValue(),
                    "*.gpx",
                    Translation.get("SaveTrack"),
                    Translation.get("save"),
                    new IgetFileReturnListener() {
                        TrackListViewItem selectedTrackItem = TrackListView.getInstance().getSelectedItem();
                        @Override
                        public void returnFile(String path) {
                            if (path != null) {
                                RouteOverlay.saveRoute(path, selectedTrackItem.getTrack());
                                Log.debug(log, "Load Track :" + path);
                                TrackListView.getInstance().notifyDataSetChanged();
                            }
                        }
                    }));
            cm.addMenuItem("delete", null, () -> {
                TrackListViewItem trackListViewItem = TrackListView.getInstance().getSelectedItem();

                if (trackListViewItem == null) {
                    MessageBox.show(Translation.get("NoTrackSelected"), null, MessageBoxButton.OK, MessageBoxIcon.Warning, null);
                } else if (trackListViewItem.getTrack().isActualTrack) {
                    MessageBox.show(Translation.get("IsActualTrack"), null, MessageBoxButton.OK, MessageBoxIcon.Warning, null);
                } else {
                    RouteOverlay.remove(trackListViewItem.getTrack());
                    TrackListView.getInstance().notifyDataSetChanged();
                }
            });
        }
        return cm;
    }

    private void showMenuCreate() {
        Menu cm2 = new Menu("TrackListViewCreateTrackTitle");
        cm2.addMenuItem("Point2Point", null, this::genTrackP2P);
        cm2.addMenuItem("Projection", null, this::genTrackProjection);
        cm2.addMenuItem("Circle", null, this::genTrackCircle);
        cm2.show();
    }

    private void genTrackP2P() {
        Coordinate coord = GlobalCore.getSelectedCoordinate();

        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.activityRec(), Translation.get("fromPoint"), coord, (targetCoord, startCoord, Bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            TrackColor = RouteOverlay.getNextColor();
            Track route = new Track(null, TrackColor);

            route.setName("Point 2 Point Route");
            route.trackPoints.add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
            route.trackPoints.add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

            MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(), dist);
            route.trackLength = dist[0];

            route.isVisible = true;
            RouteOverlay.add(route);
            TrackListView.getInstance().notifyDataSetChanged();
        }, Type.p2p, null);
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
            TrackColor = RouteOverlay.getNextColor();
            Track route = new Track(null, TrackColor);
            route.setName("Projected Route");

            route.trackPoints.add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
            route.trackPoints.add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

            MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(), dist);
            route.trackLength = dist[0];

            route.isVisible = true;
            RouteOverlay.add(route);
            TrackListView.getInstance().notifyDataSetChanged();
        }, Type.projetion, null);

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
            TrackColor = RouteOverlay.getNextColor();
            Track track = new Track(null, TrackColor);
            track.setName("Circle Route");

            track.isVisible = true;
            RouteOverlay.add(track);

            Coordinate Projektion;
            Coordinate LastCoord = new CoordinateGPS(0, 0);

            // Achtung der Kreis darf nicht mehr als 50 Punkte haben, sonst gibt es Probleme mit dem Reduktionsalgorythmus
            for (int i = 0; i <= 360; i += 10) {
                Projektion = CoordinateGPS.Project(startCoord.getLatitude(), startCoord.getLongitude(), i, distance);
                track.trackPoints.add(new TrackPoint(Projektion.getLongitude(), Projektion.getLatitude(), 0, 0, new Date()));
                if (LastCoord.isValid()) {
                    MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, Projektion.getLatitude(), Projektion.getLongitude(), LastCoord.getLatitude(), LastCoord.getLongitude(), dist);
                    track.trackLength = track.trackLength + dist[0];
                }
                LastCoord = Projektion; // !! LastCoord = new Coordinate(Projektion);
                LastCoord.setValid(true);
            }
            TrackListView.getInstance().notifyDataSetChanged();
        }, Type.circle, null);
        pC.show();
    }

    private void loadTrackList() {
        PlatformUIBase.getFile(CB_UI_Settings.TrackFolder.getValue(), "*.gpx", Translation.get("LoadTrack"), Translation.get("load"), Path -> {
            if (Path != null) {
                TrackColor = RouteOverlay.getNextColor();
                RouteOverlay.multiLoadRoute(Path, TrackColor);
                Log.debug(log, "Load Track :" + Path);
                TrackListView.getInstance().notifyDataSetChanged();
            }
        });
    }
}