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
package de.droidcachebox.gdx.main;

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
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.views.TrackListView;
import de.droidcachebox.gdx.views.TrackListViewItem;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.log.Log;

import java.util.Date;

public class Abstract_ShowTrackListAction extends AbstractShowAction {
    private static final String log = "Abstract_ShowTrackListAction";
    private static Abstract_ShowTrackListAction that;
    private Color TrackColor;

    private Abstract_ShowTrackListAction() {
        super("Tracks", MenuID.AID_SHOW_TRACKLIST);
    }

    public static Abstract_ShowTrackListAction getInstance() {
        if (that == null) that = new Abstract_ShowTrackListAction();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(TrackListView.getInstance());
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
        cm.addMenuItem("load", null, () -> {
            PlatformUIBase.getFile(CB_UI_Settings.TrackFolder.getValue(), "*.gpx", Translation.get("LoadTrack"), Translation.get("load"), Path -> {
                if (Path != null) {
                    TrackColor = RouteOverlay.getNextColor();
                    RouteOverlay.MultiLoadRoute(Path, TrackColor);
                    Log.debug(log, "Load Track :" + Path);
                    TrackListView.getInstance().notifyDataSetChanged();
                }
            });
        });
        cm.addMenuItem("generate", null, () -> showMenuCreate());
        // rename, save, delete darf nicht mit dem aktuellen Track gemacht werden....
        TrackListViewItem selectedTrackItem = TrackListView.getInstance().getSelectedItem();
        if (selectedTrackItem != null && !selectedTrackItem.getRoute().IsActualTrack) {
            cm.addMenuItem("rename", null, () -> {
                StringInputBox.Show(WrapType.SINGLELINE, selectedTrackItem.getRoute().Name, Translation.get("RenameTrack"), selectedTrackItem.getRoute().Name, (which, data) -> {
                    String text = StringInputBox.editText.getText();
                    switch (which) {
                        case 1: // ok Clicket
                            selectedTrackItem.getRoute().Name = text;
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
                        public void returnFile(String Path) {
                            if (Path != null) {
                                RouteOverlay.SaveRoute(Path, selectedTrackItem.getRoute());
                                Log.debug(log, "Load Track :" + Path);
                                TrackListView.getInstance().notifyDataSetChanged();
                            }
                        }
                    }));
            cm.addMenuItem("delete", null, () -> {
                TrackListViewItem mTrackItem = TrackListView.getInstance().getSelectedItem();

                if (mTrackItem == null) {
                    MessageBox.show(Translation.get("NoTrackSelected"), null, MessageBoxButtons.OK, MessageBoxIcon.Warning, null);
                } else if (mTrackItem.getRoute().IsActualTrack) {
                    MessageBox.show(Translation.get("IsActualTrack"), null, MessageBoxButtons.OK, MessageBoxIcon.Warning, null);
                } else {
                    RouteOverlay.remove(mTrackItem.getRoute());
                    TrackListView.getInstance().notifyDataSetChanged();
                }
            });
        }
        return cm;
    }

    private void showMenuCreate() {
        Menu cm2 = new Menu("TrackListViewCreateTrackTitle");
        cm2.addMenuItem("Point2Point", null, this::GenTrackP2P);
        cm2.addMenuItem("Projection", null, this::GenTrackProjection);
        cm2.addMenuItem("Circle", null, this::GenTrackCircle);
        cm2.show();
    }

    private void GenTrackP2P() {
        Coordinate coord = GlobalCore.getSelectedCoord();

        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.activityRec(), Translation.get("fromPoint"), coord, (targetCoord, startCoord, Bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            TrackColor = RouteOverlay.getNextColor();
            Track route = new Track(null, TrackColor);

            route.Name = "Point 2 Point Route";
            route.Points.add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
            route.Points.add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

            MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(), dist);
            route.TrackLength = dist[0];

            route.ShowRoute = true;
            RouteOverlay.add(route);
            TrackListView.getInstance().notifyDataSetChanged();
        }, Type.p2p, null);
        pC.show();

    }

    private void GenTrackProjection() {
        Coordinate coord = GlobalCore.getSelectedCoord();
        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.activityRec(), Translation.get("Projection"), coord, (targetCoord, startCoord, Bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            TrackColor = RouteOverlay.getNextColor();
            Track route = new Track(null, TrackColor);
            route.Name = "Projected Route";

            route.Points.add(new TrackPoint(targetCoord.getLongitude(), targetCoord.getLatitude(), 0, 0, new Date()));
            route.Points.add(new TrackPoint(startCoord.getLongitude(), startCoord.getLatitude(), 0, 0, new Date()));

            MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, targetCoord.getLatitude(), targetCoord.getLongitude(), startCoord.getLatitude(), startCoord.getLongitude(), dist);
            route.TrackLength = dist[0];

            route.ShowRoute = true;
            RouteOverlay.add(route);
            TrackListView.getInstance().notifyDataSetChanged();
        }, Type.projetion, null);

        pC.show();

    }

    private void GenTrackCircle() {
        Coordinate coord = GlobalCore.getSelectedCoord();
        if (coord == null)
            coord = Locator.getInstance().getMyPosition();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.activityRec(), Translation.get("centerPoint"), coord, (targetCoord, startCoord, Bearing, distance) -> {

            if (targetCoord == null || startCoord == null)
                return;

            float[] dist = new float[4];
            TrackColor = RouteOverlay.getNextColor();
            Track route = new Track(null, TrackColor);
            route.Name = "Circle Route";

            route.ShowRoute = true;
            RouteOverlay.add(route);

            Coordinate Projektion;
            Coordinate LastCoord = new CoordinateGPS(0, 0);

            // Achtung der Kreis darf nicht mehr als 50 Punkte haben, sonst gibt es Probleme mit dem Reduktionsalgorythmus
            for (int i = 0; i <= 360; i += 10) {
                Projektion = CoordinateGPS.Project(startCoord.getLatitude(), startCoord.getLongitude(), i, distance);
                route.Points.add(new TrackPoint(Projektion.getLongitude(), Projektion.getLatitude(), 0, 0, new Date()));
                if (!LastCoord.isValid()) {
                    LastCoord = Projektion;
                    LastCoord.setValid(true);
                } else {
                    MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, Projektion.getLatitude(), Projektion.getLongitude(), LastCoord.getLatitude(), LastCoord.getLongitude(), dist);
                    route.TrackLength += dist[0];
                    LastCoord = Projektion;
                    LastCoord.setValid(true);
                }
            }
            TrackListView.getInstance().notifyDataSetChanged();
        }, Type.circle, null);
        pC.show();
    }

}