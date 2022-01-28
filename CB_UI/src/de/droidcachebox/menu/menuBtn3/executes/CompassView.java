/*
 * Copyright (C) 2015 team-cachebox.de
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
package de.droidcachebox.menu.menuBtn3.executes;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.InvalidateTextureListeners;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.CB_Label.VAlignment;
import de.droidcachebox.gdx.controls.CacheInfo;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.SatBarChart;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.menu.Action;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.ShowCompass;
import de.droidcachebox.menu.menuBtn3.executes.MapView.MapMode;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

public class CompassView extends CB_View_Base implements CacheSelectionChangedListeners.CacheSelectionChangedListener, PositionChangedEvent, InvalidateTextureListeners.InvalidateTextureListener, CacheListChangedListeners.CacheListChangedListener {
    private static final String sClass = "CompassView";
    private CB_RectF imageRec;
    private Image frame;
    private Image scale;
    private Image arrow;
    private Image[] att;
    private Image Icon;
    private Image Sun;
    private Image Moon;
    private Box topContentBox, leftBox, rightBox, rightBoxMask, distanceBack;
    private ScrollBox topBox;
    private SatBarChart satBarChart;
    private CB_Label lblDistance, lbl_Name, lblGcCode, lblCoordinate, lblDesc, lblAlt, lblAccuracy, lblOwnCoordinate, lblBearing;
    private CacheInfo cacheInfo;
    private Cache currentGeoCache;
    private Waypoint currentWaypoint;
    private float margin;
    private float descHeight;
    private float lblHeight;
    private boolean initDone, showMap, showName, showIcon, showAtt, showGcCode, showCoordinate, showWpDesc, showSatInformation, showSunMoon, showAnyContent, showTargetDirection, showSDT, showLastFound;
    private boolean lastUsedCompass = Locator.getInstance().UseMagneticCompass();
    private MapView mCompassMapView;
    private IChanged settingChangedListener = () -> {
        readSettings();
        createControls();
    };

    public CompassView() {
        super(ViewManager.leftTab.getContentRec(), "CompassView");
        margin = GL_UISizes.margin;
        currentGeoCache = GlobalCore.getSelectedCache();
        currentWaypoint = GlobalCore.getSelectedWayPoint();
    }

    @Override
    public void onShow() {
        currentGeoCache = GlobalCore.getSelectedCache();
        currentWaypoint = GlobalCore.getSelectedWayPoint();
        renderInit();
        setCache();
        if (satBarChart != null) {
            satBarChart.onShow();
            satBarChart.setDrawWithAlpha(false);
        }
        try {
            if (mCompassMapView != null) mCompassMapView.onShow();
        } catch (Exception ex) {
            Log.err(sClass, ex);
        }
        positionChanged();
        CacheSelectionChangedListeners.getInstance().addListener(this);
        PositionChangedListeners.addListener(this);
        CacheListChangedListeners.getInstance().addListener(this);
        InvalidateTextureListeners.getInstance().addListener(this);
    }

    @Override
    public void onHide() {
        if (satBarChart != null)
            satBarChart.onHide();
        if (mCompassMapView != null) {
            mCompassMapView.onHide();
        }
        Settings.CompassShowMap.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowWP_Name.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowWP_Icon.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowAttributes.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowGcCode.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowCoords.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowWpDesc.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowSatInfos.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowSunMoon.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowTargetDirection.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowSDT.removeSettingChangedListener(settingChangedListener);
        Settings.CompassShowLastFound.removeSettingChangedListener(settingChangedListener);
        CacheSelectionChangedListeners.getInstance().remove(this);
        CacheListChangedListeners.getInstance().removeListener(this);
        PositionChangedListeners.removeListener(this);
        InvalidateTextureListeners.getInstance().removeListener(this);

        ((ShowCompass) Action.ShowCompass.action).viewIsHiding();
    }

    @Override
    protected void renderInit() {
        if (initDone)
            return;
        readSettings();
        createControls();
        registerSettingsChangedListeners();
        initDone = true;
    }

    private void registerSettingsChangedListeners() {
        Settings.CompassShowMap.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowWP_Name.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowWP_Icon.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowAttributes.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowGcCode.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowCoords.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowWpDesc.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowSatInfos.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowSunMoon.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowTargetDirection.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowSDT.addSettingChangedListener(settingChangedListener);
        Settings.CompassShowLastFound.addSettingChangedListener(settingChangedListener);
    }

    private void readSettings() {
        showMap = Settings.CompassShowMap.getValue();
        showName = Settings.CompassShowWP_Name.getValue();
        showIcon = Settings.CompassShowWP_Icon.getValue();
        showAtt = Settings.CompassShowAttributes.getValue();
        showGcCode = Settings.CompassShowGcCode.getValue();
        showCoordinate = Settings.CompassShowCoords.getValue();
        showWpDesc = Settings.CompassShowWpDesc.getValue();
        showSatInformation = Settings.CompassShowSatInfos.getValue();
        showSunMoon = Settings.CompassShowSunMoon.getValue();
        showTargetDirection = Settings.CompassShowTargetDirection.getValue();
        showSDT = Settings.CompassShowSDT.getValue();
        showLastFound = Settings.CompassShowLastFound.getValue();

        showAnyContent = showMap || showName || showIcon || showAtt || showGcCode || showCoordinate || showWpDesc || showSatInformation || showSunMoon || showTargetDirection || showSDT || showLastFound;
    }

    private void setCache() {

        try {
            synchronized (currentGeoCache) {
                if (currentGeoCache == null)
                    return;
                Log.debug(sClass, "new cache: " + currentGeoCache.getGeoCacheCode() + ":" + currentGeoCache.getGeoCacheName());

                if (currentGeoCache.mustLoadDetail()) {
                    Log.debug(sClass, "loading details.");
                    new CachesDAO().loadDetail(currentGeoCache);
                }

                if (showAtt) {
                    try {
                        int attributesSize = currentGeoCache.getAttributes().size();
                        for (int i = 0; i < 19; i++) {
                            if (i < attributesSize) {
                                try {
                                    String ImageName = currentGeoCache.getAttributes().get(i).getImageName() + "Icon";
                                    ImageName = ImageName.replace("_", "-");
                                    att[i].setDrawable(new SpriteDrawable(Sprites.getSprite(ImageName)));
                                } catch (Exception e) {
                                    att[i].setDrawable(null);
                                }
                            } else {
                                att[i].setDrawable(null);
                            }
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                if (showIcon && Icon != null) {
                    if (currentWaypoint == null) {
                        if (currentGeoCache.hasCorrectedCoordinatesOrHasCorrectedFinal()) {
                            Icon.setDrawable(new SpriteDrawable(Sprites.getSprite("big" + currentGeoCache.getGeoCacheType().name() + "Solved")));
                        } else {
                            Icon.setDrawable(new SpriteDrawable(Sprites.getSprite("big" + currentGeoCache.getGeoCacheType().name())));
                        }
                    } else {
                        Icon.setDrawable(new SpriteDrawable(Sprites.getSprite("big" + currentWaypoint.waypointType.name())));
                    }
                }

                if (showName && lbl_Name != null) {
                    if (currentWaypoint == null) {
                        lbl_Name.setText(currentGeoCache.getGeoCacheName());
                    } else {
                        lbl_Name.setText(currentWaypoint.getTitleForGui());
                    }
                }

                if (showGcCode && lblGcCode != null) {
                    lblGcCode.setText(currentGeoCache.getGeoCacheCode());
                }

                if (showCoordinate && lblCoordinate != null) {
                    if (currentWaypoint == null) {
                        lblCoordinate.setText(currentGeoCache.getCoordinate().formatCoordinate());
                    } else {
                        lblCoordinate.setText(currentWaypoint.getCoordinate().formatCoordinate());
                    }
                }

                if (showWpDesc && lblDesc != null) {
                    if (currentWaypoint != null && !currentWaypoint.getDescription().equals("")) {
                        lblDesc.setWrappedText(currentWaypoint.getDescription());
                    } else {
                        lblDesc.setText("");
                    }

                }

                if (showSDT & cacheInfo != null) {
                    cacheInfo.setCache(currentGeoCache);
                }

            }

            Layout();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Layout() {
        Log.debug(sClass, "layout");

        // calculate the size of compass (depends on still available place)

        float compassHeight = Math.min(leftBox.getHeight(), this.getWidth()) - margin - margin;

        if (!showMap) {
            lblDistance.setY(margin);
            compassHeight *= 0.95f;
        }

        if (showSunMoon)
            compassHeight -= Sun.getHeight();

        SizeF s = new SizeF(compassHeight);

        frame.setSize(s);
        scale.setSize(s);

        // calc center

        float left = leftBox.getHalfWidth() - s.getHalfWidth();

        frame.setX(left);
        scale.setX(left);

        float yPos = showSunMoon ? margin + Sun.getHalfHeight() : margin;

        frame.setY(yPos);
        scale.setY(yPos);
        arrow.setY(yPos);

        arrow.setSize(frame);
        arrow.setX(frame.getX());
        arrow.setRec(arrow.scaleCenter(0.8f));
        arrow.setScale(0.7f);

        scale.setOriginCenter();
        arrow.setOriginCenter();

        if (showSunMoon)
            setMoonSunPos();
        if (showSatInformation && showCoordinate && !showGcCode) {
            satBarChart.setHeight((lblHeight + margin) * 2.3f + (lblHeight + margin));
        }

        if (showAnyContent) {
            topBox.setVisible();
        } else {
            topBox.setInvisible();
        }
    }

    private void createControls() {
        this.removeChildren();

        if (distanceBack != null) {
            distanceBack.removeChildren();
            distanceBack.dispose();
        }

        if (topContentBox != null) {
            topContentBox.removeChildren();
            topContentBox.dispose();
        }

        lblHeight = Fonts.measure("Tg").height * 1.3f;
        float attHeight = (this.getWidth() / 9) - margin;

        // Calc content height
        float contentHeight = margin + margin;
        if (showName || showIcon)
            contentHeight += (showIcon ? attHeight : lblHeight) + margin;
        if (showAtt)
            contentHeight += attHeight + margin + attHeight + margin;// two Att Lines
        if (showGcCode || showCoordinate)
            contentHeight += lblHeight + margin;
        if (showWpDesc) {
            if (currentWaypoint != null) {
                if (currentWaypoint.getDescription() != null && !currentWaypoint.getDescription().equals("")) {
                    descHeight = Fonts.measureWrapped(currentWaypoint.getDescription(), this.getWidth()).height + margin;
                    contentHeight += descHeight + margin;
                }
            }
        }
        if (showSatInformation)
            contentHeight += attHeight + attHeight + margin;
        if (showTargetDirection)
            contentHeight += lblHeight + margin;
        if (showSDT)
            contentHeight += Fonts.measureForSmallFont("Tg").height * 1.3f;
        if (showLastFound)
            contentHeight += Fonts.measureForSmallFont("Tg").height * 1.3f;

        float topH = Math.max((this.getWidth() * 0.7f), this.getHeight() - contentHeight - Sprites.activityBackground.getTopHeight() - Sprites.activityBackground.getBottomHeight());

        if (showMap) {
            topH = this.getHalfWidth();
        }

        topBox = new ScrollBox(new CB_RectF(0, topH, this.getWidth(), this.getHeight() - topH));

        topBox.setBackground(Sprites.activityBackground);

        topContentBox = new Box(topBox, "topContent");
        topContentBox.setWidth(topBox.getInnerWidth());

        CB_RectF attRec = new CB_RectF(0, 0, attHeight);

        topContentBox.setHeight(contentHeight);
        topContentBox.setZeroPos();

        leftBox = new Box(new CB_RectF(0, 0, showMap ? this.getHalfWidth() : this.getWidth(), this.getHeight() - topBox.getHeight()), "left");
        leftBox.setBackground(Sprites.activityBackground);

        if (showMap) {
            rightBox = new Box(new CB_RectF(this.getHalfWidth(), 0, this.getHalfWidth(), this.getHalfWidth()), "right");
            rightBoxMask = new Box(new CB_RectF(this.getHalfWidth(), 0, this.getHalfWidth(), this.getHalfWidth()), "rightMask");
            rightBox.setBackground(Sprites.activityBackground);
            rightBoxMask.setBackground(Sprites.activityBorderMask);
            this.addChild(rightBox);
            this.addChild(rightBoxMask);

            if (mCompassMapView == null) mCompassMapView = new MapView(rightBox, MapMode.Compass);
            mCompassMapView.setZeroPos();
            rightBox.addChild(mCompassMapView);

            lblDistance = new CB_Label("lblDistance", margin, margin, rightBox.getWidth(), (Fonts.measureForBigFont("T").height * 2.5f));
            BitmapFont font = Fonts.getCompass();
            lblDistance.setFont(font);
            lblDistance.setHAlignment(HAlignment.CENTER);

            distanceBack = new Box(lblDistance, "DistanceBack");
            distanceBack.setBackground(Sprites.infoBack);
            rightBox.addChild(distanceBack);
            rightBox.addChild(lblDistance);
        } else {
            float h = Fonts.measureForBigFont("T").height * 2.5f;
            lblDistance = new CB_Label("lblDistance", margin, leftBox.getHeight() - margin - h, leftBox.getWidth() - margin - margin, h);
            BitmapFont font = Fonts.getCompass();
            lblDistance.setFont(font);
            lblDistance.setHAlignment(HAlignment.LEFT);
            distanceBack = new Box(lblDistance, "DistanceBack");
            distanceBack.setBackground(Sprites.infoBack);
            leftBox.addChild(distanceBack);
            lblDistance.setZeroPos();
            lblDistance.setX(margin);
            lblDistance.setVAlignment(VAlignment.BOTTOM);

            lblAccuracy = new CB_Label(lblDistance);
            lblAccuracy.setHAlignment(HAlignment.RIGHT);
            lblAccuracy.setZeroPos();
            lblAccuracy.setVAlignment(VAlignment.CENTER);

            distanceBack.addChild(lblDistance);
            distanceBack.addChild(lblAccuracy);
        }

        this.addChild(topBox);
        this.addChild(leftBox);

        margin = GL_UISizes.margin;

        topContentBox.setMargins(margin, margin);
        topContentBox.initRow();

        imageRec = (new CB_RectF(0, 0, getWidth(), getWidth())).scaleCenter(0.6f);
        this.setBackground(Sprites.ListBack);

        frame = new Image(imageRec, "frame", false);
        frame.setDrawable(Sprites.Compass.get(0));
        this.addChild(frame);

        scale = new Image(imageRec, "scale", false);
        scale.setDrawable(Sprites.Compass.get(1));
        this.addChild(scale);

        arrow = new Image(imageRec, "arrow", false);
        setArrowDrawable(true);
        this.addChild(arrow);

        if (showSunMoon) {

            CB_RectF rec = showMap ? attRec.scaleCenter(0.7f) : new CB_RectF(attRec);

            Sun = new Image(rec, "sun", false);
            Sun.setDrawable(Sprites.Compass.get(5));
            Sun.setInvisible();
            this.addChild(Sun);

            Moon = new Image(rec, "moon", false);
            Moon.setDrawable(Sprites.Compass.get(6));
            Moon.setInvisible();
            this.addChild(Moon);
        }

        // add WP Name and Icon Line
        if (showIcon || showName) {
            if (showIcon) {
                Icon = new Image(attRec, "Compass-CacheIcon", false);
                if (showName) {
                    topContentBox.addNext(Icon, FIXED);
                } else {
                    topContentBox.addLast(Icon);
                }
            }
            if (showName) {
                lbl_Name = new CB_Label("");
                lbl_Name.setHeight(lblHeight);
                topContentBox.addLast(lbl_Name);
            }
        }

        // add WP description line
        if (showWpDesc) {
            lblDesc = new CB_Label("");
            lblDesc.setHeight(descHeight);
            topContentBox.addLast(lblDesc);
        }

        // add GC-Code and Coordinate line
        float measuredCoordinateWidth = Fonts.measure("52° 27.130N / 13° 33.117E").width + margin;
        if (showGcCode || showCoordinate) {
            if (showCoordinate) {
                lblCoordinate = new CB_Label("");
                lblCoordinate.setHeight(lblHeight);
                lblCoordinate.setWidth(measuredCoordinateWidth);
                if (showGcCode) {
                    topContentBox.addNext(lblCoordinate, FIXED);
                } else {
                    topContentBox.addLast(lblCoordinate, FIXED);
                }
            }
            if (showGcCode) {
                lblGcCode = new CB_Label("");
                lblGcCode.setHeight(lblHeight);
                topContentBox.addLast(lblGcCode);
            }
        }

        // add sat information
        if (showSatInformation) {
            if (showMap)//
            {
                lblAlt = new CB_Label("");
                lblAlt.setHeight(lblHeight);
                topContentBox.addNext(lblAlt, 0.9f);

                lblAccuracy = new CB_Label("");
                lblAccuracy.setHeight(lblHeight);
                topContentBox.addNext(lblAccuracy, 0.7f);

            } else {
                lblAlt = new CB_Label("");
                lblAlt.setHeight(lblHeight);
                topContentBox.addNext(lblAlt);
            }

            satBarChart = new SatBarChart(attRec, "");
            satBarChart.setHeight((lblHeight + margin) * 2.3f);

            float chartWidth = topContentBox.getInnerWidth() - measuredCoordinateWidth - margin;
            satBarChart.setWidth(chartWidth);
            topContentBox.addLast(satBarChart, FIXED);

            lblOwnCoordinate = new CB_Label("");
            lblOwnCoordinate.setHeight(lblHeight);
            lblOwnCoordinate.setWidth(satBarChart.getX() - margin);
            lblOwnCoordinate.setPos(0, lblAlt.getMaxY() + margin);
            topContentBox.addChild(lblOwnCoordinate);

        }

        // add Target direction
        if (showTargetDirection) {
            lblBearing = new CB_Label("");
            lblBearing.setHeight(lblHeight);

            topContentBox.addLast(lblBearing);
        }

        // add cacheInfo line or LastFound
        if (showSDT || showLastFound) {

            int viewMode = 0;
            if (showSDT)
                viewMode += CacheInfo.SHOW_S_D_T;
            if (showLastFound)
                viewMode += CacheInfo.SHOW_LAST_FOUND;

            float infoHeight = Fonts.measureForSmallFont("Tg").height * 1.3f;
            if (showSDT && showLastFound) {
                infoHeight *= 2.5f;
            }

            cacheInfo = new CacheInfo(new SizeF(topContentBox.getWidth(), infoHeight), "cacheInfo", currentGeoCache);
            cacheInfo.setViewMode(viewMode);
            topContentBox.addLast(cacheInfo);
        }

        // add Attribute
        if (showAtt) {
            att = new Image[20];

            int attLineBreak = (int) (topContentBox.getInnerWidth() / (attHeight + margin)) - 2;
            for (int i = 0; i < 20; i++) {
                att[i] = new Image(attRec, "", false);
                if ((i < attLineBreak - 1) || (i >= attLineBreak && i < 19)) {
                    topContentBox.addNext(att[i], FIXED);
                }
                if (i == attLineBreak - 1 || i == 19) {
                    topContentBox.addLast(att[i], FIXED);
                }
            }
        }

        topBox.addChild(topContentBox);
        topBox.setVirtualHeight(topContentBox.getHeight());
        topBox.scrollTo(0);

        currentGeoCache = GlobalCore.getSelectedCache();
        currentWaypoint = GlobalCore.getSelectedWayPoint();
        setCache();
    }

    private void setArrowDrawable() {
        setArrowDrawable(false);
    }

    private void setArrowDrawable(boolean forceSet) {
        boolean tmp = Locator.getInstance().UseMagneticCompass();
        if (!forceSet && tmp == lastUsedCompass)
            return;// no change required
        lastUsedCompass = tmp;

        int arrowId;
        if (lastUsedCompass) {
            arrowId = 0;
        } else {
            arrowId = 2;
        }
        Sprite arrowSprite = new Sprite(Sprites.Arrows.get(arrowId));
        arrowSprite.setRotation(0);// reset rotation
        arrowSprite.setOrigin(0, 0);
        arrow.setDrawable(new SpriteDrawable(arrowSprite));
    }

    @Override
    public void onResized(CB_RectF rec) {
        createControls();
    }

    @Override
    public void handleCacheChanged(Cache cache, Waypoint waypoint) {
        if (currentGeoCache != cache || currentWaypoint != waypoint) {
            currentGeoCache = cache;
            currentWaypoint = waypoint;
            setCache();
        }
    }

    @Override
    public void positionChanged() {
        if (this.isDisposed) {
            return;
        }
        if (currentGeoCache == null) {
            return;
        }

        CoordinateGPS position = Locator.getInstance().getMyPosition();

        if (position == null) {
            Log.info(sClass, "but position is null");
            return;
        }

        double heading = Locator.getInstance().getHeading();

        if (lblOwnCoordinate != null)
            lblOwnCoordinate.setText(position.formatCoordinate());

        Coordinate dest = currentWaypoint != null ? currentWaypoint.getCoordinate() : currentGeoCache.getCoordinate();

        float[] result = new float[4];

        try {
            MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, position.getLatitude(), position.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);
        } catch (Exception e1) {
            Log.info(sClass, "PositionChanged but error calculating distance and bearing");
            return;
        }

        float distance = result[0];
        float bearing = result[1];

        if (lblBearing != null) {
            double directionToTarget;
            if (bearing < 0)
                directionToTarget = 360 + bearing;
            else
                directionToTarget = bearing;

            String sBearing = Translation.get("directionToTarget") + " : " + String.format(Locale.US, "%.0f", directionToTarget) + "°";
            lblBearing.setText(sBearing);
        }

        double relativeBearing = bearing - heading;

        if (arrow != null) {
            setArrowDrawable();
            arrow.setRotate((float) -relativeBearing);
        }
        if (scale != null)
            scale.setRotate((float) heading);
        if (lblDistance != null && !lblDistance.isDisposed()) {
            float labelWidth = lblDistance.setText(UnitFormatter.distanceString(distance)).getTextWidth() + (6 * margin);
            if (showMap) {
                if (distanceBack != null && !distanceBack.isDisposed()) {
                    distanceBack.setWidth(labelWidth);
                    distanceBack.setX(rightBox.getHalfWidth() - distanceBack.getHalfWidth());
                }
            }
        } else {
            Log.info(sClass, "PositionChanged but lblDistance is null or disposed");
        }

        if (lblAccuracy != null && !lblAccuracy.isDisposed()) {
            lblAccuracy.setText("  +/- " + UnitFormatter.distanceString(position.getAccuracy()));
        }

        if (showSatInformation && lblAlt != null && !lblAlt.isDisposed()) {
            lblAlt.setText(Translation.get("alt") + Locator.getInstance().getAltString());
        }

        if (showSunMoon) {
            if (Moon != null && Sun != null) {
                setMoonSunPos();
            }
        }

        GL.that.renderOnce();

    }

    @Override
    public void orientationChanged() {
        if (this.isDisposed)
            return;
        if (currentGeoCache == null)
            return;

        if (Locator.getInstance().isValid()) {
            Coordinate position = Locator.getInstance().getMyPosition();
            double heading = Locator.getInstance().getHeading();

            Coordinate dest = currentWaypoint != null ? currentWaypoint.getCoordinate() : currentGeoCache.getCoordinate();

            float[] result = new float[2];

            MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, position.getLatitude(), position.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);

            float bearing = result[1];

            double relativeBearing = bearing - heading;
            setArrowDrawable();
            arrow.setRotate((float) -relativeBearing);
            scale.setRotate((float) heading);

            if (showSunMoon)
                setMoonSunPos();

            GL.that.renderOnce();
        }
    }

    @Override
    public String getReceiverName() {
        return "CompassView";
    }

    private void setMoonSunPos() {

        if (Sun == null || Moon == null)
            return;

        if (Locator.getInstance().isValid()) {
            CoordinateGPS latLon = Locator.getInstance().getMyPosition();
            double heading = Locator.getInstance().getHeading();
            float centerX = frame.getCenterPosX();
            float centerY = frame.getCenterPosY();
            float radius = frame.getHalfWidth() + Sun.getHalfHeight() + (Sun.getHalfHeight() / 4);
            float iconSize = Sun.getWidth();
            try {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UT"));
                SunMoonCalculator sunMoonCalculator = new SunMoonCalculator(calendar, latLon.getLongitude(), latLon.getLatitude());
                sunMoonCalculator.calcSunAndMoon();
                if (sunMoonCalculator.moonEl >= 0) {
                    int x = (int) (centerX + (radius - iconSize / 2) * Math.sin((sunMoonCalculator.moonAz * MathUtils.RAD_DEG - heading) * MathUtils.DEG_RAD));
                    int y = (int) (centerY + (radius - iconSize / 2) * Math.cos((sunMoonCalculator.moonAz * MathUtils.RAD_DEG - heading) * MathUtils.DEG_RAD));
                    Moon.setPos(x - iconSize / 2, y - iconSize / 2);
                    Moon.setVisible();
                } else {
                    Moon.setInvisible();
                }

                if (sunMoonCalculator.sunEl >= 0) {
                    int x = (int) (centerX + (radius - iconSize / 2) * Math.sin((sunMoonCalculator.sunAz * MathUtils.RAD_DEG - heading) * MathUtils.DEG_RAD));
                    int y = (int) (centerY + (radius - iconSize / 2) * Math.cos((sunMoonCalculator.sunAz * MathUtils.RAD_DEG - heading) * MathUtils.DEG_RAD));
                    Sun.setPos(x - iconSize / 2, y - iconSize / 2);
                    Sun.setVisible();
                } else {
                    Sun.setInvisible();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Sun.setInvisible();
                Moon.setInvisible();
            }
        }
    }

    @Override
    public Priority getPriority() {
        return Priority.High;
    }

    @Override
    public void speedChanged() {
    }

    @Override
    public void handleInvalidateTexture() {
        createControls();
    }

    @Override
    public void cacheListChanged() {
        if (currentGeoCache != GlobalCore.getSelectedCache() || currentWaypoint != GlobalCore.getSelectedWayPoint()) {
            currentGeoCache = GlobalCore.getSelectedCache();
            currentWaypoint = GlobalCore.getSelectedWayPoint();
            setCache();
        }
    }

}
