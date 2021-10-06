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
package de.droidcachebox.gdx.views;

import static de.droidcachebox.PlatformUIBase.callUrl;
import static de.droidcachebox.PlatformUIBase.hideForDialog;
import static de.droidcachebox.utils.Config_Core.br;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.SatBarChart;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox;
import de.droidcachebox.gdx.controls.dialogs.NumericInputBox.IReturnValueListener;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
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
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

public class AboutView extends CB_View_Base implements CacheSelectionChangedListeners.CacheSelectionChangedListener, GpsStateChangeEvent, PositionChangedEvent {
    private static final String sKlasse = "AboutView";
    private static AboutView aboutView;
    private CB_Label descTextView, CachesFoundLabel, WaypointLabel, CoordLabel, lblGPS, Gps, lblAccuracy, Accuracy, lblWP, lblCoord, lblCurrent, Current;
    private Image CB_Logo;
    private float margin;
    private CancelWaitDialog pd;
    private SatBarChart chart;
    private int result = -1;
    private boolean mustShowNewInstallInfo;

    private AboutView() {
        super(ViewManager.leftTab.getContentRec(), sKlasse);
        registerSkinChangedEvent();
        createControls();
        mustShowNewInstallInfo = true;
        Log.debug(sKlasse, " created.");
    }

    public static AboutView getInstance() {
        if (aboutView == null) aboutView = new AboutView();
        return aboutView;
    }

    @Override
    public void onShow() {

        // add Event Handler
        CacheSelectionChangedListeners.getInstance().addListener(this);
        GpsStateChangeEventList.Add(this);
        PositionChangedListeners.addListener(this);

        positionChanged();

        if (!isInitialized)
            initialize();

        if (chart != null)
            chart.onShow();
        refreshText();


        if (Config.newInstall.getValue() && mustShowNewInstallInfo) {
            mustShowNewInstallInfo = false;
            String langId = Config.Sel_LanguagePath.getValue().substring(Config.languagePath.getValue().length()).substring(1,3);
            String Welcome = Translation.that.getTextFile("welcome", langId) + Translation.that.getTextFile("changelog", langId);
            MsgBox.show(Welcome, Translation.get("welcome"), MsgBoxButton.OK, MsgBoxIcon.Information, (btnNumber, data) -> true);
        }

        hideForDialog();
    }

    @Override
    public void onHide() {
        // remove Event Handler
        CacheSelectionChangedListeners.getInstance().remove(this);
        GpsStateChangeEventList.Remove(this);
        PositionChangedListeners.removeListener(this);

        if (chart != null)
            chart.onHide();
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);

        if (getBackground() == null)
            initialize();
    }

    private void createControls() {
        removeChilds();

        if (isDisposed())
            return;

        setBackground(Sprites.aboutback);
        float ref = UiSizes.getInstance().getWindowHeight() / 13f;
        margin = UiSizes.getInstance().getMargin();
        CB_RectF CB_LogoRec = new CB_RectF(getHalfWidth() - (ref * 2.5f), getHeight() - ((ref * 5) / 4.11f) - ref - margin - margin, ref * 5, (ref * 5) / 4.11f);
        //Log.debug(log, "CB_Logo" + CB_LogoRec.toString());
        CB_Logo = new Image(CB_LogoRec, "CB_Logo", false);
        CB_Logo.setDrawable(new SpriteDrawable(Sprites.getSpriteDrawable("cachebox-logo")));
        addChild(CB_Logo);

        String VersionString = GlobalCore.getInstance().getVersionString() + br + br + GlobalCore.aboutMsg;

        GlyphLayout layout = new GlyphLayout();
        layout.setText(Fonts.getSmall(), VersionString);

        descTextView = new CB_Label(name + " descTextView", 0, CB_Logo.getY() - margin - margin - margin - layout.height, getWidth(), layout.height + margin);
        descTextView.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);

        descTextView.setWrappedText(VersionString);
        addChild(descTextView);

        CachesFoundLabel = new CB_Label("", Fonts.getNormal(), COLOR.getLinkFontColor(), WrapType.SINGLELINE).setHAlignment(HAlignment.CENTER);
        CachesFoundLabel.setWidth(getWidth());

        CachesFoundLabel.setClickHandler(new OnClickListener() {
            MsgBox msgBox;

            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                msgBox = MsgBox.show(Translation.get("LoadFounds"), Translation.get("AdjustFinds"), MsgBoxButton.YesNo, MsgBoxIcon.GC_Live,
                        (which, data) -> {
                            switch (which) {
                                case 1:
                                    msgBox.close();
                                    pd = CancelWaitDialog.ShowWait(Translation.get("LoadFounds"), DownloadAnimation.GetINSTANCE(), null, new ICancelRunnable() {
                                        @Override
                                        public void run() {
                                            result = GroundspeakAPI.fetchMyUserInfos().findCount;
                                            pd.close();
                                            if (result > -1) {
                                                String Text = Translation.get("FoundsSetTo", String.valueOf(result));
                                                MsgBox.show(Text, Translation.get("LoadFinds!"), MsgBoxButton.OK, MsgBoxIcon.GC_Live, null);
                                                Config.FoundOffset.setValue(result);
                                                Config.acceptChanges();
                                                AboutView.this.refreshText();
                                            }
                                        }

                                        @Override
                                        public boolean doCancel() {
                                            return false;
                                        }
                                    });
                                    break;
                                case 3:
                                    msgBox.close();
                                    GL.that.RunOnGL(() -> NumericInputBox.Show(Translation.get("TelMeFounds"), Translation.get("AdjustFinds"), Config.FoundOffset.getValue(), new IReturnValueListener() {
                                        @Override
                                        public void returnValue(int value) {
                                            Config.FoundOffset.setValue(value);
                                            Config.acceptChanges();
                                            AboutView.this.refreshText();
                                        }

                                        @Override
                                        public void cancelClicked() {
                                        }
                                    }));
                                    break;
                            }
                            return true;
                        });
                return true;
            }
        });
        addChild(CachesFoundLabel);
        createTable();
        refreshText();
    }

    private void createTable() {
        float leftMaxWidth = 0;
        CB_RectF lblRec = new CB_RectF(0, 0, 0, UiSizes.getInstance().getButtonHeight() / 2.5f);

        lblGPS = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblGPS.setText(Translation.get("gps")).getTextWidth());

        lblAccuracy = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblAccuracy.setText(Translation.get("accuracy")).getTextWidth());

        lblWP = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblWP.setText(Translation.get("waypoint")).getTextWidth());

        lblCoord = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblCoord.setText(Translation.get("coordinate")).getTextWidth());

        lblCurrent = new CB_Label(lblRec);
        leftMaxWidth = Math.max(leftMaxWidth, lblCurrent.setText(Translation.get("current")).getTextWidth());

        // set all lbl to the same max width + margin
        leftMaxWidth += margin;
        lblGPS.setWidth(leftMaxWidth);
        lblAccuracy.setWidth(leftMaxWidth);
        lblWP.setWidth(leftMaxWidth);
        lblCoord.setWidth(leftMaxWidth);
        lblCurrent.setWidth(leftMaxWidth);

        // set lbl position on Screen
        lblCurrent.setPos(margin, margin);
        lblCoord.setPos(margin, lblCurrent.getMaxY());
        lblWP.setPos(margin, lblCoord.getMaxY());
        lblAccuracy.setPos(margin, lblWP.getMaxY());
        lblGPS.setPos(margin, lblAccuracy.getMaxY());

        // add to Screen
        addChild(lblGPS);
        addChild(lblAccuracy);
        addChild(lblWP);
        addChild(lblCoord);
        addChild(lblCurrent);

        // ##############################
        // create Value Label
        lblRec.setX(lblGPS.getMaxX() + margin);
        lblRec.setWidth(getWidth() - margin - lblGPS.getMaxX());

        Gps = new CB_Label(lblRec);
        Accuracy = new CB_Label(lblRec);
        WaypointLabel = new CB_Label("-", Fonts.getNormal(), COLOR.getLinkFontColor(), WrapType.SINGLELINE);
        WaypointLabel.setRec(lblRec);
        CoordLabel = new CB_Label(lblRec);
        Current = new CB_Label(lblRec);

        // set Y Pos
        Gps.setY(lblGPS.getY());
        Accuracy.setY(lblAccuracy.getY());
        WaypointLabel.setY(lblWP.getY());
        CoordLabel.setY(lblCoord.getY());
        Current.setY(lblCurrent.getY());

        // set LinkColor

        WaypointLabel.setClickHandler((v, x, y, pointer, button) -> {
            if (GlobalCore.getSelectedCache() == null)
                return true;
            callUrl(GlobalCore.getSelectedCache().getUrl());
            return true;
        });

        // add to Screen
        addChild(Gps);
        addChild(Accuracy);
        addChild(WaypointLabel);
        addChild(CoordLabel);
        addChild(Current);

        // create Sat Chart
        float l = margin * 2;
        chart = new SatBarChart(new CB_RectF(l, Gps.getMaxY() + l, getWidth() - l - l, CachesFoundLabel.getY() - Gps.getMaxY()), "Sat Chart");
        chart.setDrawWithAlpha(true);
        addChild(chart);
        setYpositions();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        setYpositions();
    }

    @Override
    protected void skinIsChanged() {
        createControls();
        setYpositions();
    }

    private void setYpositions() {
        if (CB_Logo != null) {
            CB_Logo.setY(getHeight() - (margin * 2) - CB_Logo.getHeight());
            if (descTextView != null) {
                descTextView.setY(CB_Logo.getY() - margin - margin - margin - descTextView.getHeight());
                if (CachesFoundLabel != null) {
                    CachesFoundLabel.setY(descTextView.getY() - CachesFoundLabel.getHeight() + margin);
                    if (chart != null)
                        chart.setHeight(CachesFoundLabel.getY() - Gps.getMaxY());
                }
            }
        }
    }

    private void refreshText() {
        if (WaypointLabel == null || CachesFoundLabel == null || CoordLabel == null)
            return;
        try {
            CachesFoundLabel.setText(Translation.get("caches_found") + " " + Config.FoundOffset.getValue());

            Cache selectedCache = GlobalCore.getSelectedCache();
            Waypoint selectedWaypoint = GlobalCore.getSelectedWayPoint();

            if (selectedCache != null) {
                try {
                    if (selectedWaypoint != null) {
                        WaypointLabel.setText(selectedWaypoint.getWaypointCode());
                        CoordLabel.setText(UnitFormatter.FormatLatitudeDM(selectedWaypoint.getLatitude()) + " " + UnitFormatter.FormatLongitudeDM(selectedWaypoint.getLongitude()));
                    } else {
                        WaypointLabel.setText(selectedCache.getGeoCacheCode());
                        CoordLabel.setText(UnitFormatter.FormatLatitudeDM(selectedCache.getCoordinate().getLatitude()) + " " + UnitFormatter.FormatLongitudeDM(selectedCache.getCoordinate().getLongitude()));
                    }
                } catch (Exception e) {
                    CoordLabel.setText(" - - - ");
                }
            }
            GL.that.renderOnce();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void GpsStateChanged() {
        if (Locator.getInstance().getMyPosition().hasAccuracy()) {
            int radius = Locator.getInstance().getMyPosition().getAccuracy();

            if (Accuracy != null)
                Accuracy.setText("+/- " + UnitFormatter.distanceString(radius) + " (" + Locator.getInstance().getProvider().toString() + ")");
        } else {
            if (Accuracy != null)
                Accuracy.setText("?");
        }
        if (Locator.getInstance().getProvider() == ProviderType.GPS || Locator.getInstance().getProvider() == ProviderType.Network) {
            if (Current != null)
                Current.setText(UnitFormatter.FormatLatitudeDM(Locator.getInstance().getLatitude()) + " " + UnitFormatter.FormatLongitudeDM(Locator.getInstance().getLongitude()));
            if (Gps != null)
                Gps.setText(GPS.getSatAndFix() + "   " + Translation.get("alt") + " " + Locator.getInstance().getAltStringWithCorection());
        } else {
            if (Gps != null)
                Gps.setText(Translation.get("not_detected"));
        }
    }

    @Override
    public void handleCacheChanged(Cache cache, Waypoint waypoint) {
        GL.that.RunOnGL(this::refreshText);
    }

    @Override
    public void positionChanged() {
        GpsStateChanged();
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

    @Override
    public void dispose() {
        aboutView = null;

        if (descTextView != null)
            descTextView.dispose();
        descTextView = null;
        if (CachesFoundLabel != null)
            CachesFoundLabel.dispose();
        CachesFoundLabel = null;
        if (WaypointLabel != null)
            WaypointLabel.dispose();
        WaypointLabel = null;
        if (CoordLabel != null)
            CoordLabel.dispose();
        CoordLabel = null;
        if (lblGPS != null)
            lblGPS.dispose();
        lblGPS = null;
        if (Gps != null)
            Gps.dispose();
        Gps = null;
        if (lblAccuracy != null)
            lblAccuracy.dispose();
        lblAccuracy = null;
        if (Accuracy != null)
            Accuracy.dispose();
        Accuracy = null;
        if (lblWP != null)
            lblWP.dispose();
        lblWP = null;
        if (lblCoord != null)
            lblCoord.dispose();
        lblCoord = null;
        if (lblCurrent != null)
            lblCurrent.dispose();
        lblCurrent = null;
        if (Current != null)
            Current.dispose();
        Current = null;
        if (CB_Logo != null)
            CB_Logo.dispose();
        CB_Logo = null;
        if (chart != null)
            chart.dispose();
        chart = null;
        if (pd != null)
            pd.dispose();
        pd = null;

        CacheSelectionChangedListeners.getInstance().remove(this);
        GpsStateChangeEventList.Remove(this);
        PositionChangedListeners.removeListener(this);

        super.dispose();
    }
}
