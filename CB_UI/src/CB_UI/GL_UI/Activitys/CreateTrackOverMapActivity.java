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
package CB_UI.GL_UI.Activitys;

import CB_Core.CacheTypes;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.MapTileLoader;
import CB_Locator.Map.Track;
import CB_Locator.Map.TrackPoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.MapView.MapMode;
import CB_UI.GL_UI.Views.MapViewCacheList;
import CB_UI.GL_UI.Views.MapViewCacheList.WaypointRenderInfo;
import CB_UI.GlobalCore;
import CB_UI.RouteOverlay;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.CB_Button;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.MoveableList;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import java.util.Date;

import static CB_UI_Base.GL_UI.Sprites.*;

/**
 * A Activity for create a Track over the Map.<br>
 * Set TrackPoints over MapCenter!
 *
 * @author Longri
 */
public class CreateTrackOverMapActivity extends ActivityBase {
    private final MoveableList<WaypointRenderInfo> tmplist = new MoveableList<MapViewCacheList.WaypointRenderInfo>();
    public MapView mTrackMapView;
    private CB_Label lblName;
    private EditTextField editName;
    private CB_Button btnOk, btnAdd, btnCancel;
    private CB_List<Waypoint> waypoints;
    private Waypoint selectedWP;
    private OnClickListener onOkClick = (v, x, y, pointer, button) -> false;
    private OnClickListener onAddClick = (v, x, y, pointer, button) -> {
        final Coordinate coord = mTrackMapView.center;
        if ((coord == null) || (!coord.isValid()))
            return false;
        GL.that.RunOnGL(() -> {
            //Waypoint newWP = new Waypoint(String.valueOf(System.currentTimeMillis()), CacheTypes.MultiStage, "", coord.getLatitude(), coord.getLongitude(), -1, "", Translation.Get("wyptDefTitle"));
            Waypoint newWP = new Waypoint(String.valueOf(System.currentTimeMillis()), CacheTypes.MultiStage, "", coord.getLatitude(), coord.getLongitude(), -1, "", String.valueOf(System.currentTimeMillis()));
            addWP(newWP);
        });
        return true;
    };
    private OnClickListener onCancelClik = (v, x, y, pointer, button) -> {
        GL.that.RunOnGL(() -> finish());
        return true;
    };

    public CreateTrackOverMapActivity(String Name) {
        super(Name);
        createControls();
        createNewTrack();
    }

    private void createControls() {
        float btWidth = innerWidth / 3;

        btnOk = new CB_Button(new CB_RectF(leftBorder, this.getBottomHeight(), btWidth, UI_Size_Base.ui_size_base.getButtonHeight()), onOkClick);
        btnAdd = new CB_Button(new CB_RectF(btnOk.getMaxX(), this.getBottomHeight(), btWidth, UI_Size_Base.ui_size_base.getButtonHeight()), onAddClick);
        btnCancel = new CB_Button(new CB_RectF(btnAdd.getMaxX(), this.getBottomHeight(), btWidth, UI_Size_Base.ui_size_base.getButtonHeight()), onCancelClik);

        // translations
        btnOk.setText(Translation.get("ok".hashCode()));
        btnAdd.setText(Translation.get("addWP"));
        btnCancel.setText(Translation.get("cancel".hashCode()));

        this.addChild(btnOk);
        this.addChild(btnAdd);
        this.addChild(btnCancel);

        lblName = new CB_Label(Translation.get("Name"));
        editName = new EditTextField(this, "*" + Translation.get("Name"));
        lblName.setRec(new CB_RectF(leftBorder, this.getHeight() - (lblName.getHeight() + margin), lblName.getWidth(), lblName.getHeight()));
        editName.setRec(new CB_RectF(lblName.getMaxX() + margin, lblName.getY(), innerWidth - (margin + lblName.getWidth()), lblName.getHeight()));
        this.addChild(lblName);
        this.addChild(editName);

        CB_RectF mapRec = new CB_RectF(leftBorder, btnOk.getMaxY() + margin, innerWidth, innerHeight - (btnOk.getHalfHeight() + editName.getHeight() + (4 * margin) + topBorder));

        mTrackMapView = new MapView(mapRec, MapMode.Track);
        this.addChild(mTrackMapView);

        mTrackMapView.setOnLongClickListener((v, x, y, pointer, button) -> {
            // chk if any TrackPoint clicked

            double minDist = Double.MAX_VALUE;
            WaypointRenderInfo minWpi = null;
            Vector2 clickedAt = new Vector2(x, y);

            for (int i = 0, n = tmplist.size(); i < n; i++) {
                WaypointRenderInfo wpi = tmplist.get(i);
                Vector2 screen = mTrackMapView.worldToScreen(new Vector2(Math.round(wpi.MapX), Math.round(wpi.MapY)));
                if (clickedAt != null) {
                    double aktDist = Math.sqrt(Math.pow(screen.x - clickedAt.x, 2) + Math.pow(screen.y - clickedAt.y, 2));
                    if (aktDist < minDist) {
                        minDist = aktDist;
                        minWpi = wpi;
                    }
                }
            }

            if (minDist < 40) {
                selectedWP = minWpi.Waypoint;

                // Show PopUpMenu
                //					CB_RectF rec = new CB_RectF(x - 1, y - 1, 2, 2);
                //					CB_RectF mapWorld = mapView.getWorldRec();
                //					rec = rec.ScaleCenter(150);
                //					PopUpMenu menu = new PopUpMenu(rec, "popUpMenu");
                //					menu.setPos(mapWorld.getX() + x - menu.getHalfWidth(), mapView.getY() + y - menu.getHalfHeight());
                //					menu.showNotCloseAutomaticly();
            } else {
                selectedWP = null;
            }

            return true;
        });

    }

    private void addWP(Waypoint wp) {
        if (waypoints == null)
            waypoints = new CB_List<Waypoint>();
        waypoints.add(wp);
        if (waypoints.size() == 2) {
            // Two Points, begins with Track drawing
            Track track = new Track("generate", Color.RED);
            GlobalCore.AktuelleRoute.Points.add(convertToTrackPoint(waypoints.get(0)));
            GlobalCore.AktuelleRoute.Points.add(convertToTrackPoint(waypoints.get(1)));
        }

        if (waypoints.size() > 2) {
            GlobalCore.AktuelleRoute.Points.add(convertToTrackPoint(wp));
        }

        if (waypoints.size() > 1) {
            RouteOverlay.RoutesChanged();
        }
    }

    private void createNewTrack() {
        GlobalCore.AktuelleRoute = new Track(Translation.get("actualTrack"), Color.BLUE);
        GlobalCore.AktuelleRoute.ShowRoute = true;
        GlobalCore.AktuelleRoute.IsActualTrack = true;
        GlobalCore.aktuelleRouteCount = 0;
        GlobalCore.AktuelleRoute.TrackLength = 0;
        GlobalCore.AktuelleRoute.AltitudeDifference = 0;
    }

    private TrackPoint convertToTrackPoint(Waypoint wp) {

        TrackPoint trp = new TrackPoint(wp.Pos.getLongitude(), wp.Pos.getLatitude(), 0, 0, new Date());
        return trp;

    }

    @Override
    public void renderChilds(final Batch batch, ParentInfo parentInfo) {
        super.renderChilds(batch, parentInfo);

        // render WPs
        if (waypoints == null)
            return;
        tmplist.clear();

        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);

            double MapX = 256.0 * Descriptor.LongitudeToTileX(MapTileLoader.MAX_MAP_ZOOM, wp.Pos.getLongitude());
            double MapY = -256.0 * Descriptor.LatitudeToTileY(MapTileLoader.MAX_MAP_ZOOM, wp.Pos.getLatitude());
            if (true)// isVisible(MapX, MapY)
            {
                WaypointRenderInfo wpi = new WaypointRenderInfo();
                wpi.MapX = (float) MapX;
                wpi.MapY = (float) MapY;
                wpi.Icon = getSprite("mapTrailhead");
                wpi.Cache = null;
                wpi.Waypoint = wp;
                wpi.UnderlayIcon = null;

                wpi.Selected = false;
                if (selectedWP != null) {
                    if (selectedWP.getGcCode().equals(wp.getGcCode())) {
                        wpi.Selected = true;
                        wpi.UnderlayIcon = getMapOverlay(IconName.shaddowrectselected);
                    }
                }
                tmplist.add(wpi);
            }
        }

        batch.setProjectionMatrix(mTrackMapView.myParentInfo.Matrix());

        Gdx.gl.glScissor((int) mTrackMapView.thisWorldRec.getX(), (int) mTrackMapView.thisWorldRec.getY(), (int) mTrackMapView.thisWorldRec.getWidth() + 1, (int) mTrackMapView.thisWorldRec.getHeight() + 1);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        SizeF drawingSize = new SizeF(40, 40);

        for (int i = 0; i < tmplist.size(); i++) {

            mTrackMapView.renderWPI(batch, drawingSize, drawingSize, tmplist.get(i));

        }

    }

    @Override
    public void dispose() {
        super.dispose();
        if (mTrackMapView != null) {
            mTrackMapView.dispose();
            mTrackMapView = null;
        }
        if (btnOk != null)
            btnOk.dispose();
        btnOk = null;

        if (btnAdd != null)
            btnAdd.dispose();
        btnAdd = null;

        if (btnCancel != null)
            btnCancel.dispose();
        btnCancel = null;

        if (lblName != null)
            lblName.dispose();
        lblName = null;

        onOkClick = null;
        onAddClick = null;
        onCancelClik = null;
    }

}
