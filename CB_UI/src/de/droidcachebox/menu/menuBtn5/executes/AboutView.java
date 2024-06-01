/*
 * Copyright (C) 2014-2015 team-cachebox.de
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
package de.droidcachebox.menu.menuBtn5.executes;

import static de.droidcachebox.Platform.callUrl;
import static de.droidcachebox.Platform.hideForDialog;
import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_LEFT_POSITIVE;
import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_RIGHT_NEGATIVE;
import static de.droidcachebox.settings.Config_Core.br;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.SatBarChart;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.CBLocation.ProviderType;
import de.droidcachebox.locator.GPS;
import de.droidcachebox.locator.GpsStateChangeEvent;
import de.droidcachebox.locator.GpsStateChangeEventList;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn5.ShowAbout;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.UnitFormatter;

public class AboutView extends CB_View_Base implements CacheSelectionChangedListeners.CacheSelectionChangedListener, GpsStateChangeEvent, PositionChangedEvent {
    private static final String sClass = "AboutView";
    private CB_Label descTextView;
    private CB_Label cachesFoundLabel;
    private CB_Label waypointLabel;
    private CB_Label coordinateLabel;
    private CB_Label gpsLabel;
    private CB_Label accuracyLabel;
    private CB_Label currentLabel;
    private Image logoCBImage;
    private SatBarChart satBarChart;
    private int result = -1;
    private float margin;

    public AboutView() {
        super(ViewManager.leftTab.getContentRec(), sClass);
        registerSkinChangedEvent();
        if (GroundspeakAPI.isAccessTokenExpired()) {
            GL.that.postAsync(() -> {
                GroundspeakAPI.refreshAccessToken();
                refreshText();
            });
        }
        createControls();
    }

    @Override
    public void onShow() {

        // add Event Handler
        CacheSelectionChangedListeners.getInstance().addListener(this);
        GpsStateChangeEventList.add(this);
        PositionChangedListeners.addListener(this);

        positionChanged();

        if (!isRenderInitDone)
            renderInit();

        if (satBarChart != null)
            satBarChart.onShow();
        refreshText();


        if (Settings.newInstall.getValue()) {
            Settings.newInstall.setValue(false);
            String langId = Settings.Sel_LanguagePath.getValue().substring(Settings.languagePath.getValue().length()).substring(1, 3);
            String Welcome = Translation.that.getTextFile("welcome", langId) + Translation.that.getTextFile("changelog", langId);
            ButtonDialog bd = new ButtonDialog(Welcome, Translation.get("welcome"), MsgBoxButton.OK, MsgBoxIcon.Information);
            bd.setButtonClickHandler((btnNumber, data) -> true);
            bd.show();
        }

        hideForDialog();
    }

    @Override
    public void onHide() {
        // remove Event Handler
        CacheSelectionChangedListeners.getInstance().remove(this);
        GpsStateChangeEventList.remove(this);
        PositionChangedListeners.removeListener(this);

        if (satBarChart != null)
            satBarChart.onHide();
        ShowAbout.getInstance().viewIsHiding();
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);

        if (getBackground() == null)
            renderInit();
    }

    private void createControls() {
        removeChildren();

        if (isDisposed)
            return;

        setBackground(Sprites.aboutback);
        float ref = UiSizes.getInstance().getWindowHeight() / 13f;
        margin = UiSizes.getInstance().getMargin();
        CB_RectF CB_LogoRec = new CB_RectF(getHalfWidth() - (ref * 2.5f), getHeight() - ((ref * 5) / 4.11f) - ref - margin - margin, ref * 5, (ref * 5) / 4.11f);
        //Log.debug(log, "CB_Logo" + CB_LogoRec.toString());
        logoCBImage = new Image(CB_LogoRec, "CB_Logo", false);
        logoCBImage.setDrawable(new SpriteDrawable(Sprites.getSpriteDrawable("cachebox-logo")));
        addChild(logoCBImage);

        String VersionString = GlobalCore.getInstance().getVersionString() + br + br + GlobalCore.aboutMsg;

        GlyphLayout layout = new GlyphLayout();
        layout.setText(Fonts.getSmall(), VersionString);

        descTextView = new CB_Label(name + " descTextView", 0, logoCBImage.getY() - margin - margin - margin - layout.height, getWidth(), layout.height + margin);
        descTextView.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);

        descTextView.setWrappedText(VersionString);
        addChild(descTextView);

        HSV_Color cachesFoundLabelColor = COLOR.getLinkFontColor();
        if (GroundspeakAPI.isAccessTokenExpired()) {
            cachesFoundLabelColor = COLOR.getFontColor();
        }
        cachesFoundLabel = new CB_Label("", Fonts.getNormal(), cachesFoundLabelColor, WrapType.SINGLELINE).setHAlignment(HAlignment.CENTER);
        cachesFoundLabel.setWidth(getWidth());

        cachesFoundLabel.setClickHandler((view, x, y, pointer, button) -> {
            ButtonDialog msgBox = new ButtonDialog(Translation.get("LoadFinds"), Translation.get("AdjustFinds"), MsgBoxButton.YesNo, MsgBoxIcon.GC_Live);
            msgBox.setButtonClickHandler(
                    (which, data) -> {
                        switch (which) {
                            case BTN_LEFT_POSITIVE:
                                msgBox.close();
                                GL.that.postAsync(() -> {
                                    AtomicBoolean isCanceled = new AtomicBoolean(false);
                                    new CancelWaitDialog(Translation.get("LoadFinds"), new DownloadAnimation(), new RunAndReady() {
                                        @Override
                                        public void ready() {
                                            if (result > -1) {
                                                String Text = Translation.get("FoundsSetTo", String.valueOf(result));
                                                new ButtonDialog(Text, Translation.get("AdjustFinds"), MsgBoxButton.OK, MsgBoxIcon.GC_Live).show();
                                                Settings.foundOffset.setValue(result);
                                                Settings.getInstance().acceptChanges();
                                                AboutView.this.refreshText();
                                            }
                                        }

                                        @Override
                                        public void run() {
                                            result = GroundspeakAPI.forceFetchMyUserInfos().findCount;
                                        }

                                        @Override
                                        public void setIsCanceled() {
                                            isCanceled.set(true);
                                        }

                                    }).show();
                                });
                                break;
                            case BTN_RIGHT_NEGATIVE:
                                msgBox.close();
                                GL.that.runOnGL(() -> {
                                    NumericInputBox numericInputBox = new NumericInputBox(Translation.get("TelMeFounds"), Translation.get("AdjustFinds"));
                                    numericInputBox.initIntInput(Settings.foundOffset.getValue(),
                                            new NumericInputBox.IReturnValueListener() {
                                                @Override
                                                public void returnValue(int value) {
                                                    Settings.foundOffset.setValue(value);
                                                    Settings.getInstance().acceptChanges();
                                                    AboutView.this.refreshText();
                                                }

                                                @Override
                                                public void cancelClicked() {
                                                }
                                            });
                                    numericInputBox.show();
                                });

                                break;
                        }
                        return true;
                    });
            msgBox.show();
            return true;
        });

        addChild(cachesFoundLabel);
        createTable();
        refreshText();
    }

    private void createTable() {
        float leftMaxWidth = 0;
        CB_RectF lblRec = new CB_RectF(0, 0, 0, UiSizes.getInstance().getButtonHeight() / 2.5f);

        CB_Label lblGPS = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblGPS.setText(Translation.get("gps")).getTextWidth());

        CB_Label lblAccuracy = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblAccuracy.setText(Translation.get("accuracy")).getTextWidth());

        CB_Label lblWP = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblWP.setText(Translation.get("waypoint")).getTextWidth());

        CB_Label lblCoordinate = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblCoordinate.setText(Translation.get("coordinate")).getTextWidth());

        CB_Label lblCurrent = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblCurrent.setText(Translation.get("current")).getTextWidth());

        // set all lbl to the same max width + margin
        leftMaxWidth += margin;
        lblGPS.setWidth(leftMaxWidth);
        lblAccuracy.setWidth(leftMaxWidth);
        lblWP.setWidth(leftMaxWidth);
        lblCoordinate.setWidth(leftMaxWidth);
        lblCurrent.setWidth(leftMaxWidth);

        // set lbl position on Screen
        lblCurrent.setPos(margin, margin);
        lblCoordinate.setPos(margin, lblCurrent.getMaxY());
        lblWP.setPos(margin, lblCoordinate.getMaxY());
        lblAccuracy.setPos(margin, lblWP.getMaxY());
        lblGPS.setPos(margin, lblAccuracy.getMaxY());

        // add to Screen
        addChild(lblGPS);
        addChild(lblAccuracy);
        addChild(lblWP);
        addChild(lblCoordinate);
        addChild(lblCurrent);

        // ##############################
        // create Value Label
        lblRec.setX(lblGPS.getMaxX() + margin);
        lblRec.setWidth(getWidth() - margin - lblGPS.getMaxX());

        gpsLabel = new CB_Label(lblRec);
        accuracyLabel = new CB_Label(lblRec);
        waypointLabel = new CB_Label("-", Fonts.getNormal(), COLOR.getLinkFontColor(), WrapType.SINGLELINE);
        waypointLabel.setRec(lblRec);
        coordinateLabel = new CB_Label(lblRec);
        currentLabel = new CB_Label(lblRec);

        // set Y Pos
        gpsLabel.setY(lblGPS.getY());
        accuracyLabel.setY(lblAccuracy.getY());
        waypointLabel.setY(lblWP.getY());
        coordinateLabel.setY(lblCoordinate.getY());
        currentLabel.setY(lblCurrent.getY());

        // set LinkColor

        waypointLabel.setClickHandler((v, x, y, pointer, button) -> {
            if (GlobalCore.getSelectedCache() == null)
                return true;
            callUrl(GlobalCore.getSelectedCache().getUrl());
            return true;
        });

        // add to Screen
        addChild(gpsLabel);
        addChild(accuracyLabel);
        addChild(waypointLabel);
        addChild(coordinateLabel);
        addChild(currentLabel);

        // create Sat Chart
        float l = margin * 2;
        satBarChart = new SatBarChart(new CB_RectF(l, gpsLabel.getMaxY() + l, getWidth() - l - l, cachesFoundLabel.getY() - gpsLabel.getMaxY()), "Sat Chart");
        satBarChart.setDrawWithAlpha(true);
        addChild(satBarChart);
        setYPositions();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        setYPositions();
    }

    @Override
    protected void skinIsChanged() {
        createControls();
        setYPositions();
    }

    private void setYPositions() {
        if (logoCBImage != null) {
            logoCBImage.setY(getHeight() - (margin * 2) - logoCBImage.getHeight());
            if (descTextView != null) {
                descTextView.setY(logoCBImage.getY() - margin - margin - margin - descTextView.getHeight());
                if (cachesFoundLabel != null) {
                    cachesFoundLabel.setY(descTextView.getY() - cachesFoundLabel.getHeight() + margin);
                    if (satBarChart != null)
                        satBarChart.setHeight(cachesFoundLabel.getY() - gpsLabel.getMaxY());
                }
            }
        }
    }

    private void refreshText() {
        if (waypointLabel == null || cachesFoundLabel == null || coordinateLabel == null)
            return;
        try {
            HSV_Color cachesFoundLabelColor = COLOR.getLinkFontColor();
            if (GroundspeakAPI.isAccessTokenExpired()) {
                cachesFoundLabelColor = COLOR.getFontColor();
            }
            cachesFoundLabel.setTextColor(cachesFoundLabelColor);
            cachesFoundLabel.setText(Translation.get("caches_found") + " " + Settings.foundOffset.getValue());

            Cache selectedCache = GlobalCore.getSelectedCache();
            Waypoint selectedWaypoint = GlobalCore.getSelectedWayPoint();

            if (selectedCache != null) {
                try {
                    if (selectedWaypoint != null) {
                        waypointLabel.setText(selectedWaypoint.getWaypointCode());
                        coordinateLabel.setText(UnitFormatter.FormatLatitudeDM(selectedWaypoint.getLatitude()) + " " + UnitFormatter.FormatLongitudeDM(selectedWaypoint.getLongitude()));
                    } else {
                        waypointLabel.setText(selectedCache.getGeoCacheCode());
                        coordinateLabel.setText(UnitFormatter.FormatLatitudeDM(selectedCache.getCoordinate().getLatitude()) + " " + UnitFormatter.FormatLongitudeDM(selectedCache.getCoordinate().getLongitude()));
                    }
                } catch (Exception e) {
                    coordinateLabel.setText(" - - - ");
                }
            }
            GL.that.renderOnce();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void gpsStateChanged() {
        if (Locator.getInstance().getMyPosition().hasAccuracy()) {
            int radius = Locator.getInstance().getMyPosition().getAccuracy();

            if (accuracyLabel != null)
                accuracyLabel.setText("+/- " + UnitFormatter.distanceString(radius) + " (" + Locator.getInstance().getProvider().toString() + ")");
        } else {
            if (accuracyLabel != null)
                accuracyLabel.setText("?");
        }
        if (Locator.getInstance().getProvider() == ProviderType.GPS || Locator.getInstance().getProvider() == ProviderType.Network) {
            if (currentLabel != null)
                currentLabel.setText(UnitFormatter.FormatLatitudeDM(Locator.getInstance().getLatitude()) + " " + UnitFormatter.FormatLongitudeDM(Locator.getInstance().getLongitude()));
            if (gpsLabel != null)
                gpsLabel.setText(GPS.getSatAndFix() + "   " + Translation.get("alt") + " " + Locator.getInstance().getAltStringWithCorection());
        } else {
            if (gpsLabel != null)
                gpsLabel.setText(Translation.get("not_detected"));
        }
    }

    @Override
    public void handleCacheSelectionChanged(Cache cache, Waypoint selectedWaypoint) {
        GL.that.runOnGL(this::refreshText);
    }

    @Override
    public void positionChanged() {
        gpsStateChanged();
    }

    @Override
    public void orientationChanged() {
    }

    @Override
    public String getReceiverName() {
        return "AboutView";
    }

    @Override
    public Priority getPriority() {
        return Priority.Low;
    }

    @Override
    public void speedChanged() {
    }
}
