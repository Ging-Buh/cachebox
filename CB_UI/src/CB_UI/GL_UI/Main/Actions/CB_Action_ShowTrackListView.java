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
package CB_UI.GL_UI.Main.Actions;

import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Locator;
import CB_Locator.Map.Track;
import CB_Locator.Map.TrackPoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.CB_UI_Settings;
import CB_UI.GL_UI.Activitys.ProjectionCoordinate;
import CB_UI.GL_UI.Activitys.ProjectionCoordinate.Type;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.Views.TrackListView;
import CB_UI.GL_UI.Views.TrackListViewItem;
import CB_UI.GlobalCore;
import CB_UI.RouteOverlay;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.IgetFileReturnListener;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Dialogs.StringInputBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_Utils.Log.Log;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Date;

public class CB_Action_ShowTrackListView extends CB_Action_ShowView {
    private static final String log = "CB_Action_ShowTrackListView";
    private static CB_Action_ShowTrackListView that;
    private Color TrackColor;

    private CB_Action_ShowTrackListView() {
        super("Tracks", MenuID.AID_SHOW_TRACKLIST);
    }

    public static CB_Action_ShowTrackListView getInstance() {
        if (that == null) that = new CB_Action_ShowTrackListView();
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
            PlatformConnector.getFile(CB_UI_Settings.TrackFolder.getValue(), "*.gpx", Translation.get("LoadTrack"), Translation.get("load"), Path -> {
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
            cm.addMenuItem("save", null, () -> PlatformConnector.getFile(CB_UI_Settings.TrackFolder.getValue(),
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
        cm2.addMenuItem( "Point2Point",null, this::GenTrackP2P);
        cm2.addMenuItem("Projection",null, this::GenTrackProjection);
        cm2.addMenuItem("Circle",null, this::GenTrackCircle);
        cm2.show();
    }

    private void GenTrackP2P() {
        Coordinate coord = GlobalCore.getSelectedCoord();

        if (coord == null)
            coord = Locator.getCoordinate();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), Translation.get("fromPoint"), coord, (targetCoord, startCoord, Bearing, distance) -> {

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
            coord = Locator.getCoordinate();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), Translation.get("Projection"), coord, (targetCoord, startCoord, Bearing, distance) -> {

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
            coord = Locator.getCoordinate();

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), Translation.get("centerPoint"), coord, (targetCoord, startCoord, Bearing, distance) -> {

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