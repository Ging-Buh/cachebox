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
package de.droidcachebox.gdx.activities.notUsed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.RouteOverlay;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.ParentInfo;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.MapView;
import de.droidcachebox.gdx.views.MapView.MapMode;
import de.droidcachebox.gdx.views.MapViewCacheList.WayPointRenderInfo;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.locator.map.TrackPoint;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.MoveableList;

import java.util.Date;

import static de.droidcachebox.gdx.Sprites.*;

/**
 * A Activity for create a Track over the Map.<br>
 * Set TrackPoints over MapCenter!
 *
 * @author Longri
 */
public class CreateTrackOverMapActivity extends ActivityBase {
    private final MoveableList<WayPointRenderInfo> wayPointRenderInfos = new MoveableList<>();
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
            Waypoint newWP = new Waypoint(String.valueOf(System.currentTimeMillis()), GeoCacheType.MultiStage, "", coord.getLatitude(), coord.getLongitude(), -1, "", String.valueOf(System.currentTimeMillis()));
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

        btnOk = new CB_Button(new CB_RectF(leftBorder, this.getBottomHeight(), btWidth, UiSizes.getInstance().getButtonHeight()), onOkClick);
        btnAdd = new CB_Button(new CB_RectF(btnOk.getMaxX(), this.getBottomHeight(), btWidth, UiSizes.getInstance().getButtonHeight()), onAddClick);
        btnCancel = new CB_Button(new CB_RectF(btnAdd.getMaxX(), this.getBottomHeight(), btWidth, UiSizes.getInstance().getButtonHeight()), onCancelClik);

        // translations
        btnOk.setText(Translation.get("ok"));
        btnAdd.setText(Translation.get("addWP"));
        btnCancel.setText(Translation.get("cancel"));

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
            WayPointRenderInfo minWpi = null;
            Vector2 clickedAt = new Vector2(x, y);

            for (int i = 0, n = wayPointRenderInfos.size(); i < n; i++) {
                WayPointRenderInfo wpi = wayPointRenderInfos.get(i);
                Vector2 screen = mTrackMapView.worldToScreen(new Vector2(Math.round(wpi.mapX), Math.round(wpi.mapY)));
                if (clickedAt != null) {
                    double aktDist = Math.sqrt(Math.pow(screen.x - clickedAt.x, 2) + Math.pow(screen.y - clickedAt.y, 2));
                    if (aktDist < minDist) {
                        minDist = aktDist;
                        minWpi = wpi;
                    }
                }
            }

            if (minDist < 40) {
                selectedWP = minWpi.waypoint;

                // Show PopUpMenu
                //					CB_RectF rec = new CB_RectF(x - 1, y - 1, 2, 2);
                //					CB_RectF mapWorld = mapView.getWorldRec();
                //					rec = rec.scaleCenter(150);
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
            GlobalCore.aktuelleRoute.trackPoints.add(convertToTrackPoint(waypoints.get(0)));
            GlobalCore.aktuelleRoute.trackPoints.add(convertToTrackPoint(waypoints.get(1)));
        }

        if (waypoints.size() > 2) {
            GlobalCore.aktuelleRoute.trackPoints.add(convertToTrackPoint(wp));
        }

        if (waypoints.size() > 1) {
            RouteOverlay.getInstance().trackListChanged();
        }
    }

    private void createNewTrack() {
        GlobalCore.aktuelleRoute = new Track(Translation.get("actualTrack"), Color.BLUE);
        GlobalCore.aktuelleRoute.isVisible = true;
        GlobalCore.aktuelleRoute.isActualTrack = true;
        GlobalCore.aktuelleRouteCount = 0;
        GlobalCore.aktuelleRoute.trackLength = 0;
        GlobalCore.aktuelleRoute.altitudeDifference = 0;
    }

    private TrackPoint convertToTrackPoint(Waypoint wp) {
        return new TrackPoint(wp.getLongitude(), wp.getLatitude(), 0, 0, new Date());
    }

    @Override
    public void renderChilds(final Batch batch, ParentInfo parentInfo) {
        super.renderChilds(batch, parentInfo);

        // render WPs
        if (waypoints == null)
            return;
        wayPointRenderInfos.clear();

        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);

            double MapX = 256.0 * Descriptor.LongitudeToTileX(MapView.MAX_MAP_ZOOM, wp.getLongitude());
            double MapY = -256.0 * Descriptor.LatitudeToTileY(MapView.MAX_MAP_ZOOM, wp.getLatitude());
            if (true)// isVisible(MapX, MapY)
            {
                WayPointRenderInfo wpi = new WayPointRenderInfo();
                wpi.mapX = (float) MapX;
                wpi.mapY = (float) MapY;
                wpi.icon = getSprite("mapTrailhead");
                wpi.cache = null;
                wpi.waypoint = wp;
                wpi.underlayIcon = null;

                wpi.selected = false;
                if (selectedWP != null) {
                    if (selectedWP.getGcCode().equals(wp.getGcCode())) {
                        wpi.selected = true;
                        wpi.underlayIcon = getMapOverlay(IconName.shaddowrectselected);
                    }
                }
                wayPointRenderInfos.add(wpi);
            }
        }

        batch.setProjectionMatrix(mTrackMapView.myParentInfo.Matrix());

        Gdx.gl.glScissor((int) mTrackMapView.thisWorldRec.getX(), (int) mTrackMapView.thisWorldRec.getY(), (int) mTrackMapView.thisWorldRec.getWidth() + 1, (int) mTrackMapView.thisWorldRec.getHeight() + 1);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        SizeF drawingSize = new SizeF(40, 40);
        for (WayPointRenderInfo wayPointRenderInfo : wayPointRenderInfos) {
            mTrackMapView.renderWPI(batch, drawingSize, drawingSize, wayPointRenderInfo);
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
