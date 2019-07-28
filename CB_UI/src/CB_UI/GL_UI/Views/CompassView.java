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
package CB_UI.GL_UI.Views;

import CB_Core.CacheListChangedEventList;
import CB_Core.CacheListChangedEventListener;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Controls.CacheInfo;
import CB_UI.GL_UI.Controls.SatBarChart;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.SunMoonCalculator;
import CB_UI.GL_UI.Views.MapView.MapMode;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI_Base.Events.invalidateTextureEvent;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.CB_Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.CB_Label.VAlignment;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;
import CB_Utils.Log.Log;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.IChanged;
import CB_Utils.Util.UnitFormatter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.Calendar;
import java.util.TimeZone;

public class CompassView extends CB_View_Base implements SelectedCacheEvent, PositionChangedEvent, invalidateTextureEvent, CacheListChangedEventListener {
    private static final String log = "CompassView";
    private static CompassView that;
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
    private SatBarChart chart;
    private CB_Label lblDistance, lbl_Name, lblGcCode, lblCoords, lblDesc, lblAlt, lblAccuracy, lblOwnCoords, lblBearing;
    private CacheInfo cacheInfo;
    private Cache aktCache;
    private Waypoint aktWaypoint;
    private float margin;
    private float descHeight;
    private float lblHeight;
    private boolean initDone, showMap, showName, showIcon, showAtt, showGcCode, showCoords, showWpDesc, showSatInfos, showSunMoon, showAnyContent, showTargetDirection, showSDT, showLastFound;
    private boolean lastUsedCompass = Locator.UseMagneticCompass();
    private MapView mCompassMapView;
    private IChanged settingChangedListener = () -> {
        readSettings();
        createControls();
    };

    private CompassView() {
        super(ViewManager.leftTab.getContentRec(), "CompassView");
        margin = GL_UISizes.margin;
        aktCache = GlobalCore.getSelectedCache();
        aktWaypoint = GlobalCore.getSelectedWaypoint();
    }

    public static CompassView getInstance() {
        if (that == null) that = new CompassView();
        return that;
    }

    @Override
    public void onShow() {
        aktCache = GlobalCore.getSelectedCache();
        aktWaypoint = GlobalCore.getSelectedWaypoint();
        Initial();
        setCache();
        if (chart != null) {
            chart.onShow();
            chart.setDrawWithAlpha(false);
        }
        try {
            if (mCompassMapView != null) mCompassMapView.onShow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        PositionChanged();
        SelectedCacheEventList.Add(this);
        PositionChangedEventList.Add(this);
        CacheListChangedEventList.Add(this);
        invalidateTextureEventList.Add(this);
    }

    @Override
    public void onHide() {
        if (chart != null)
            chart.onHide();
        SelectedCacheEventList.Remove(this);
        PositionChangedEventList.Remove(this);
        if (mCompassMapView != null) {
            mCompassMapView.onHide();
        }
        CacheListChangedEventList.Remove(this);
        invalidateTextureEventList.Remove(this);
    }

    @Override
    protected void Initial() {
        if (initDone)
            return;
        readSettings();
        createControls();
        registerSettingsChangedListeners();
        initDone = true;
    }

    private void registerSettingsChangedListeners() {
        Config.CompassShowMap.addSettingChangedListener(settingChangedListener);
        Config.CompassShowWP_Name.addSettingChangedListener(settingChangedListener);
        Config.CompassShowWP_Icon.addSettingChangedListener(settingChangedListener);
        Config.CompassShowAttributes.addSettingChangedListener(settingChangedListener);
        Config.CompassShowGcCode.addSettingChangedListener(settingChangedListener);
        Config.CompassShowCoords.addSettingChangedListener(settingChangedListener);
        Config.CompassShowWpDesc.addSettingChangedListener(settingChangedListener);
        Config.CompassShowSatInfos.addSettingChangedListener(settingChangedListener);
        Config.CompassShowSunMoon.addSettingChangedListener(settingChangedListener);
        Config.CompassShowTargetDirection.addSettingChangedListener(settingChangedListener);
        Config.CompassShowSDT.addSettingChangedListener(settingChangedListener);
        Config.CompassShowLastFound.addSettingChangedListener(settingChangedListener);
    }

    private void readSettings() {
        showMap = Config.CompassShowMap.getValue();
        showName = Config.CompassShowWP_Name.getValue();
        showIcon = Config.CompassShowWP_Icon.getValue();
        showAtt = Config.CompassShowAttributes.getValue();
        showGcCode = Config.CompassShowGcCode.getValue();
        showCoords = Config.CompassShowCoords.getValue();
        showWpDesc = Config.CompassShowWpDesc.getValue();
        showSatInfos = Config.CompassShowSatInfos.getValue();
        showSunMoon = Config.CompassShowSunMoon.getValue();
        showTargetDirection = Config.CompassShowTargetDirection.getValue();
        showSDT = Config.CompassShowSDT.getValue();
        showLastFound = Config.CompassShowLastFound.getValue();

        showAnyContent = showMap || showName || showIcon || showAtt || showGcCode || showCoords || showWpDesc || showSatInfos || showSunMoon || showTargetDirection || showSDT || showLastFound;
    }

    private void setCache() {

        try {
            if (aktCache == null)
                return;
            synchronized (aktCache) {
                Log.debug(log, "new cache: " + aktCache.getGcCode() + ":" + aktCache.getName());

                if (!aktCache.isDetailLoaded()) {
                    Log.debug(log, "loading details.");
                    aktCache.loadDetail();
                }

                if (showAtt) {
                    try {
                        int attributesSize = aktCache.getAttributes().size();
                        for (int i = 0; i < 19; i++) {
                            if (i < attributesSize) {
                                try {
                                    String ImageName = aktCache.getAttributes().get(i).getImageName() + "Icon";
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
                    if (aktWaypoint == null) {
                        if (aktCache.hasCorrectedCoordiantesOrHasCorrectedFinal()) {
                            Icon.setDrawable(new SpriteDrawable(Sprites.getSprite("big" + aktCache.Type.name() + "Solved")));
                        } else {
                            Icon.setDrawable(new SpriteDrawable(Sprites.getSprite("big" + aktCache.Type.name())));
                        }
                    } else {
                        Icon.setDrawable(new SpriteDrawable(Sprites.getSprite("big" + aktWaypoint.Type.name())));
                    }
                }

                if (showName && lbl_Name != null) {
                    if (aktWaypoint == null) {
                        lbl_Name.setText(aktCache.getName());
                    } else {
                        lbl_Name.setText(aktWaypoint.getTitleForGui());
                    }
                }

                if (showGcCode && lblGcCode != null) {
                    lblGcCode.setText(aktCache.getGcCode());
                }

                if (showCoords && lblCoords != null) {
                    if (aktWaypoint == null) {
                        lblCoords.setText(aktCache.Pos.FormatCoordinate());
                    } else {
                        lblCoords.setText(aktWaypoint.Pos.FormatCoordinate());
                    }
                }

                if (showWpDesc && lblDesc != null) {
                    if (aktWaypoint != null && !aktWaypoint.getDescription().equals("")) {
                        lblDesc.setWrappedText(aktWaypoint.getDescription());
                    } else {
                        lblDesc.setText("");
                    }

                }

                if (showSDT & cacheInfo != null) {
                    cacheInfo.setCache(aktCache);
                }

            }

            Layout();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Layout() {
        Log.debug(log, "Layout");

        // Die Größe des Kompasses nach rest Platz berechnen

        float compassHeight = Math.min(leftBox.getHeight(), this.getWidth()) - margin - margin;

        if (!showMap) {
            lblDistance.setY(margin);
            compassHeight *= 0.95f;
        }

        if (showSunMoon)
            compassHeight -= Sun.getHeight();

        SizeF s = new SizeF(compassHeight, compassHeight);

        frame.setSize(s);
        scale.setSize(s);

        // calc center

        float left = leftBox.getHalfWidth() - s.halfWidth;

        frame.setX(left);
        scale.setX(left);

        float yPos = showSunMoon ? margin + Sun.getHalfHeight() : margin;

        frame.setY(yPos);
        scale.setY(yPos);
        arrow.setY(yPos);

        arrow.setSize(frame);
        arrow.setX(frame.getX());
        arrow.setRec(arrow.ScaleCenter(0.8f));
        arrow.setScale(0.7f);

        scale.setOriginCenter();
        arrow.setOriginCenter();

        if (showSunMoon)
            setMoonSunPos();
        if (showSatInfos && showCoords && !showGcCode) {
            chart.setHeight((lblHeight + margin) * 2.3f + (lblHeight + margin));
        }

        if (showAnyContent) {
            topBox.setVisible();
        } else {
            topBox.setInvisible();
        }
    }

    private void createControls() {
        this.removeChilds();

        if (distanceBack != null) {
            distanceBack.removeChilds();
            distanceBack.dispose();
        }

        if (topContentBox != null) {
            topContentBox.removeChilds();
            topContentBox.dispose();
        }

        lblHeight = Fonts.Measure("Tg").height * 1.3f;
        float attHeight = (this.getWidth() / 9) - margin;

        // Calc content height
        float contentHeight = margin + margin;
        if (showName || showIcon)
            contentHeight += (showIcon ? attHeight : lblHeight) + margin;
        if (showAtt)
            contentHeight += attHeight + margin + attHeight + margin;// two Att Lines
        if (showGcCode || showCoords)
            contentHeight += lblHeight + margin;
        if (showWpDesc) {
            if (aktWaypoint != null) {
                if (aktWaypoint.getDescription() != null && !aktWaypoint.getDescription().equals("")) {
                    descHeight = Fonts.MeasureWrapped(aktWaypoint.getDescription(), this.getWidth()).height + margin;
                    contentHeight += descHeight + margin;
                }
            }
        }
        if (showSatInfos)
            contentHeight += attHeight + attHeight + margin;
        if (showTargetDirection)
            contentHeight += lblHeight + margin;
        if (showSDT)
            contentHeight += Fonts.MeasureSmall("Tg").height * 1.3f;
        if (showLastFound)
            contentHeight += Fonts.MeasureSmall("Tg").height * 1.3f;

        float topH = Math.max((this.getWidth() * 0.7f), this.getHeight() - contentHeight - Sprites.activityBackground.getTopHeight() - Sprites.activityBackground.getBottomHeight());

        if (showMap) {
            topH = this.getHalfWidth();
        }

        topBox = new ScrollBox(new CB_RectF(0, topH, this.getWidth(), this.getHeight() - topH));

        topBox.setBackground(Sprites.activityBackground);

        topContentBox = new Box(topBox, "topContent");
        topContentBox.setWidth(topBox.getInnerWidth());

        CB_RectF attRec = new CB_RectF(0, 0, attHeight, attHeight);

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

            lblDistance = new CB_Label("Distance", margin, margin, rightBox.getWidth(), (Fonts.MeasureBig("T").height * 2.5f));
            BitmapFont font = Fonts.getCompass();
            lblDistance.setFont(font);
            lblDistance.setHAlignment(HAlignment.CENTER);

            distanceBack = new Box(lblDistance, "DistanceBack");
            distanceBack.setBackground(Sprites.InfoBack);
            rightBox.addChild(distanceBack);
            rightBox.addChild(lblDistance);
        } else {
            float h = Fonts.MeasureBig("T").height * 2.5f;
            lblDistance = new CB_Label("Distance", margin, leftBox.getHeight() - margin - h, leftBox.getWidth() - margin - margin, h);
            BitmapFont font = Fonts.getCompass();
            lblDistance.setFont(font);
            lblDistance.setHAlignment(HAlignment.LEFT);
            distanceBack = new Box(lblDistance, "DistanceBack");
            distanceBack.setBackground(Sprites.InfoBack);
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

        imageRec = (new CB_RectF(0, 0, getWidth(), getWidth())).ScaleCenter(0.6f);
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

            CB_RectF rec = showMap ? attRec.ScaleCenter(0.7f) : attRec.copy();

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
                    topContentBox.addNext(Icon, CB_View_Base.FIXED);
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

        // add GC-Code and Coord line
        float mesuredCoorWidth = Fonts.Measure("52° 27.130N / 13° 33.117E").width + margin;
        if (showGcCode || showCoords) {
            if (showCoords) {
                lblCoords = new CB_Label("");
                lblCoords.setHeight(lblHeight);
                lblCoords.setWidth(mesuredCoorWidth);
                if (showGcCode) {
                    topContentBox.addNext(lblCoords, CB_View_Base.FIXED);
                } else {
                    topContentBox.addLast(lblCoords, CB_View_Base.FIXED);
                }
            }
            if (showGcCode) {
                lblGcCode = new CB_Label("");
                lblGcCode.setHeight(lblHeight);
                topContentBox.addLast(lblGcCode);
            }
        }

        // add sat infos
        if (showSatInfos) {
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

            chart = new SatBarChart(attRec, "");
            chart.setHeight((lblHeight + margin) * 2.3f);

            float chartWidth = topContentBox.getInnerWidth() - mesuredCoorWidth - margin;
            chart.setWidth(chartWidth);
            topContentBox.addLast(chart, CB_View_Base.FIXED);

            lblOwnCoords = new CB_Label("");
            lblOwnCoords.setHeight(lblHeight);
            lblOwnCoords.setWidth(chart.getX() - margin);
            lblOwnCoords.setPos(0, lblAlt.getMaxY() + margin);
            topContentBox.addChild(lblOwnCoords);

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

            float infoHeight = Fonts.MeasureSmall("Tg").height * 1.3f;
            if (showSDT && showLastFound) {
                infoHeight *= 2.5f;
            }

            cacheInfo = new CacheInfo(new SizeF(topContentBox.getWidth(), infoHeight), "cacheInfo", aktCache);
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

        aktCache = GlobalCore.getSelectedCache();
        aktWaypoint = GlobalCore.getSelectedWaypoint();
        setCache();
    }

    private void setArrowDrawable() {
        setArrowDrawable(false);
    }

    private void setArrowDrawable(boolean forceSet) {
        boolean tmp = Locator.UseMagneticCompass();
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
    public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
        if (aktCache != cache || aktWaypoint != waypoint) {
            aktCache = cache;
            aktWaypoint = waypoint;
            setCache();
        }
    }

    @Override
    public void PositionChanged() {
        if (this.isDisposed()) {
            return;
        }
        if (aktCache == null) {
            return;
        }

        CoordinateGPS position = Locator.getMyPosition();

        if (position == null) {
            Log.info(log, "but position is null");
            return;
        }

        double heading = Locator.getHeading();

        if (lblOwnCoords != null)
            lblOwnCoords.setText(position.FormatCoordinate());

        Coordinate dest = aktWaypoint != null ? aktWaypoint.Pos : aktCache.Pos;

        float[] result = new float[4];

        try {
            MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, position.getLatitude(), position.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);
        } catch (Exception e1) {
            Log.info(log, "PositionChanged but error calculating distance and bearing");
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

            String sBearing = Translation.get("directionToTarget") + " : " + String.format("%.0f", directionToTarget) + "°";
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
            float labelWidth = lblDistance.setText(UnitFormatter.DistanceString(distance)).getTextWidth() + (6 * margin);
            if (showMap) {
                if (distanceBack != null && !distanceBack.isDisposed()) {
                    distanceBack.setWidth(labelWidth);
                    distanceBack.setX(rightBox.getHalfWidth() - distanceBack.getHalfWidth());
                }
            }
        } else {
            Log.info(log, "PositionChanged but lblDistance is null or disposed");
        }

        if (lblAccuracy != null && !lblAccuracy.isDisposed()) {
            lblAccuracy.setText("  +/- " + UnitFormatter.DistanceString(position.getAccuracy()));
        }

        if (showSatInfos && lblAlt != null && !lblAlt.isDisposed()) {
            lblAlt.setText(Translation.get("alt") + Locator.getAltString());
        }

        if (showSunMoon) {
            if (Moon != null && Sun != null) {
                setMoonSunPos();
            }
        }

        GL.that.renderOnce();

    }

    @Override
    public void OrientationChanged() {
        if (this.isDisposed())
            return;
        if (aktCache == null)
            return;

        if (Locator.Valid()) {
            Coordinate position = Locator.getMyPosition();
            double heading = Locator.getHeading();

            Coordinate dest = aktWaypoint != null ? aktWaypoint.Pos : aktCache.Pos;

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

        // chk instanzes
        if (Sun == null || Moon == null)
            return;

        if (Locator.Valid()) {
            CoordinateGPS latLon = Locator.getMyPosition();
            double heading = Locator.getHeading();
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
    public void SpeedChanged() {
    }

    @Override
    public void invalidateTexture() {
        createControls();
    }

    @Override
    public void CacheListChangedEvent() {
        if (aktCache != GlobalCore.getSelectedCache() || aktWaypoint != GlobalCore.getSelectedWaypoint()) {
            aktCache = GlobalCore.getSelectedCache();
            aktWaypoint = GlobalCore.getSelectedWaypoint();
            setCache();
        }
    }

    @Override
    public void dispose() {
        // release all Member
        if (imageRec != null)
            imageRec.dispose();
        imageRec = null;
        if (frame != null)
            frame.dispose();
        frame = null;
        if (scale != null)
            scale.dispose();
        scale = null;
        if (arrow != null)
            arrow.dispose();
        arrow = null;
        if (Icon != null)
            Icon.dispose();
        Icon = null;
        if (Sun != null)
            Sun.dispose();
        Sun = null;
        if (Moon != null)
            Moon.dispose();
        Moon = null;
        if (topContentBox != null)
            topContentBox.dispose();
        topContentBox = null;
        if (leftBox != null)
            leftBox.dispose();
        leftBox = null;
        if (rightBox != null)
            rightBox.dispose();
        rightBox = null;
        if (rightBoxMask != null)
            rightBoxMask.dispose();
        rightBoxMask = null;
        if (distanceBack != null)
            distanceBack.dispose();
        distanceBack = null;
        if (topBox != null)
            topBox.dispose();
        topBox = null;
        if (mCompassMapView != null) {
            mCompassMapView.dispose();
            mCompassMapView = null;
        }
        if (chart != null)
            chart.dispose();
        chart = null;
        if (lblDistance != null)
            lblDistance.dispose();
        lblDistance = null;
        if (lbl_Name != null)
            lbl_Name.dispose();
        lbl_Name = null;
        if (lblGcCode != null)
            lblGcCode.dispose();
        lblGcCode = null;
        if (lblCoords != null)
            lblCoords.dispose();
        lblCoords = null;
        if (lblDesc != null)
            lblDesc.dispose();
        lblDesc = null;
        if (lblAlt != null)
            lblAlt.dispose();
        lblAlt = null;
        if (lblAccuracy != null)
            lblAccuracy.dispose();
        lblAccuracy = null;
        if (lblOwnCoords != null)
            lblOwnCoords.dispose();
        lblOwnCoords = null;
        if (lblBearing != null)
            lblBearing.dispose();
        lblBearing = null;
        if (cacheInfo != null)
            cacheInfo.dispose();
        cacheInfo = null;

        aktCache = null;
        aktWaypoint = null;

        if (att != null) {
            for (Image img : att) {
                if (img != null)
                    img.dispose();
                img = null;
            }
            att = null;
        }

        // release all EventHandler
        Config.CompassShowMap.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowWP_Name.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowWP_Icon.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowAttributes.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowGcCode.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowCoords.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowWpDesc.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowSatInfos.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowSunMoon.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowTargetDirection.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowSDT.removeSettingChangedListener(settingChangedListener);
        Config.CompassShowLastFound.removeSettingChangedListener(settingChangedListener);

        settingChangedListener = null;
        SelectedCacheEventList.Remove(this);
        CacheListChangedEventList.Remove(this);
        PositionChangedEventList.Remove(this);
        invalidateTextureEventList.Remove(this);

        super.dispose();
        that = null;
    }

}
