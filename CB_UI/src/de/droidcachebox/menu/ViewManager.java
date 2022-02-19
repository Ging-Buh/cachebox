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
package de.droidcachebox.menu;

import static de.droidcachebox.gdx.math.GL_UISizes.mainButtonSize;
import static de.droidcachebox.locator.map.MapViewBase.INITIAL_WP_LIST;
import static de.droidcachebox.menu.Action.RecordVideo;
import static de.droidcachebox.menu.Action.RecordVoice;
import static de.droidcachebox.menu.Action.ShowCompass;
import static de.droidcachebox.menu.Action.ShowDescription;
import static de.droidcachebox.menu.Action.ShowDrafts;
import static de.droidcachebox.menu.Action.ShowGeoCaches;
import static de.droidcachebox.menu.Action.ShowHint;
import static de.droidcachebox.menu.Action.ShowLogs;
import static de.droidcachebox.menu.Action.ShowMap;
import static de.droidcachebox.menu.Action.ShowNotes;
import static de.droidcachebox.menu.Action.ShowParkingMenu;
import static de.droidcachebox.menu.Action.ShowSolver1;
import static de.droidcachebox.menu.Action.ShowSolver2;
import static de.droidcachebox.menu.Action.ShowSpoiler;
import static de.droidcachebox.menu.Action.ShowTrackableList;
import static de.droidcachebox.menu.Action.ShowTracks;
import static de.droidcachebox.menu.Action.ShowWayPoints;
import static de.droidcachebox.menu.Action.SwitchDayNight;
import static de.droidcachebox.menu.Action.SwitchTorch;
import static de.droidcachebox.menu.Action.TakePicture;
import static de.droidcachebox.settings.Config_Core.br;

import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.AppRater;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.GlobalLocationReceiver;
import de.droidcachebox.InvalidateTextureListeners;
import de.droidcachebox.Platform;
import de.droidcachebox.PlatformAction;
import de.droidcachebox.core.API_ErrorEventHandler;
import de.droidcachebox.core.API_ErrorEventHandlerList;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.ParentInfo;
import de.droidcachebox.gdx.Slider;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.ViewConst;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.main.CB_ActionButton.GestureDirection;
import de.droidcachebox.gdx.main.CB_TabView;
import de.droidcachebox.gdx.main.GestureButton;
import de.droidcachebox.gdx.main.MainViewBase;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.menu.menuBtn1.ShowGeoCaches;
import de.droidcachebox.menu.menuBtn2.StartExternalDescription;
import de.droidcachebox.menu.menuBtn3.MapDownloadMenu;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.menu.menuBtn3.executes.TrackList;
import de.droidcachebox.menu.menuBtn3.executes.TrackRecorder;
import de.droidcachebox.menu.menuBtn5.ShowAbout;
import de.droidcachebox.menu.menuBtn5.ShowCredits;
import de.droidcachebox.menu.menuBtn5.ShowHelp;
import de.droidcachebox.menu.menuBtn5.ShowQuit;
import de.droidcachebox.menu.menuBtn5.ShowSettings;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

/**
 * the ViewManager has one tab (leftTab) on the phone<br>
 * tablet is no longer implemented! two tabs (leftTab , rightTab) on the tablet.<br>
 * Each tab has buttons (5/3) at the bottom for selecting the different actions to do.<br>
 *
 * @author ging-buh
 * @author Longri
 */
public class ViewManager extends MainViewBase implements PositionChangedEvent {
    private static final String sClass = "ViewManager";
    public static ViewManager that;
    public static CB_TabView leftTab; // the only one (has been left and right for Tablet)

    static PlatformAction actionShare;

    private GestureButton mainBtn1; // default: show CacheList
    private GestureButton mainBtn2; // default: show CacheDescription on Phone ( and Waypoints on Tablet )
    private GestureButton mainBtn3; // default: show map on phone ( and show Compass on Tablet )
    private GestureButton mainBtn4; // default: show ToolsMenu or Drafts or Drafts Context menu (depends on config)
    private GestureButton mainBtn5; // default: show About View

    private boolean isInitial = false;

    public ViewManager(CB_RectF rec) {
        super(rec);
        PositionChangedListeners.addListener(this);
        that = this;
    }

    public static void reloadCacheList() {
        synchronized (CBDB.cacheList) {
            new CachesDAO().readCacheList(FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue()), false, false, Settings.showAllWaypoints.getValue());
        }
        CacheListChangedListeners.getInstance().fire(sClass);
    }

    @Override
    protected void renderInit() {
        Log.debug(sClass, "initialize ViewManager");

        GlobalCore.receiver = new GlobalLocationReceiver();

        UnitFormatter.setUseImperialUnits(Settings.ImperialUnits.getValue());
        Settings.ImperialUnits.addSettingChangedListener(() -> UnitFormatter.setUseImperialUnits(Settings.ImperialUnits.getValue()));

        Settings.showAllWaypoints.addSettingChangedListener(() -> {
            reloadCacheList();
            // must reload MapViewCacheList: do this over MapView.INITIAL_WP_LIST
            ((ShowMap) ShowMap.action).normalMapView.setNewSettings(INITIAL_WP_LIST);
        });

        API_ErrorEventHandlerList.addHandler(new API_ErrorEventHandler() {
            @Override
            public void InvalidAPI_Key() {
                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        String msg = Translation.get("apiKeyInvalid") + br + br;
                        msg += Translation.get("wantApi");

                        ButtonDialog bd = new ButtonDialog(msg, Translation.get("errorAPI"), MsgBoxButton.YesNo, MsgBoxIcon.GC_Live);
                        bd.setButtonClickHandler((which, data) -> {
                            if (which == ButtonDialog.BTN_LEFT_POSITIVE)
                                Platform.getApiKey();
                            return true;
                        });
                        bd.show();
                    }
                };
                new Timer().schedule(tt, 1500);
            }

            @Override
            public void ExpiredAPI_Key() {
                Timer t = new Timer();
                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        String msg = Translation.get("apiKeyExpired") + br + br;
                        msg += Translation.get("wantApi");

                        ButtonDialog bd = new ButtonDialog(msg, Translation.get("errorAPI"), MsgBoxButton.YesNo, MsgBoxIcon.GC_Live);
                        bd.setButtonClickHandler((which, data) -> {
                            if (which == ButtonDialog.BTN_LEFT_POSITIVE)
                                Platform.getApiKey();
                            return true;
                        });
                        bd.show();
                    }
                };
                t.schedule(tt, 1500);
            }

            @Override
            public void NoAPI_Key() {

                Timer t = new Timer();
                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        String msg = Translation.get("apiKeyNeeded") + br + br;
                        msg += Translation.get("wantApi");
                        new ButtonDialog(msg, Translation.get("errorAPI"), MsgBoxButton.YesNo, MsgBoxIcon.GC_Live,
                                (which, data) -> {
                                    if (which == ButtonDialog.BTN_LEFT_POSITIVE)
                                        Platform.getApiKey();
                                    return true;
                                }, Settings.RememberAsk_Get_API_Key).show();
                    }
                };
                t.schedule(tt, 1500);
            }
        });

        addPhoneTab();

        // add Slider as last
        Slider slider = new Slider(this, "Slider");
        addChild(slider);

        Log.debug(sClass, "End of ViewManager-Initialization");

        autoLoadTrack();

        if (Settings.TrackRecorderStartup.getValue()) {
            if (Platform.isGPSon()) {
                TrackRecorder.getInstance().startRecording();
            }
        }
        Settings.trackDistance.addSettingChangedListener(() -> TrackRecorder.getInstance().distanceForNextTrackpoint = Settings.trackDistance.getValue());

        // set last selected Cache
        String sGc = Settings.lastSelectedCache.getValue();
        if (sGc != null && sGc.length() > 0) {
            synchronized (CBDB.cacheList) {
                for (int i = 0, n = CBDB.cacheList.size(); i < n; i++) {
                    Cache c = CBDB.cacheList.get(i);
                    if (c.getGeoCacheCode().equalsIgnoreCase(sGc)) {
                        Log.debug(sClass, "ViewManager: Set selectedCache to " + c.getGeoCacheCode() + " from lastSaved.");
                        GlobalCore.setSelectedCache(c); // !! sets GlobalCore.setAutoResort to false
                        break;
                    }
                }
            }
        }

        GlobalCore.setAutoResort(Settings.StartWithAutoSelect.getValue());
        filterSetChanged();
        GL.that.removeRenderView(this);

        AppRater.app_launched();

        isInitial = true;

        Platform.handleExternalRequest();

    }

    private void addPhoneTab() {
        // only one Tab with 5 Buttons

        CB_RectF rec = new CB_RectF(0, 0, GL_UISizes.uiLeft.getWidth(), getHeight() - UiSizes.getInstance().getInfoSliderHeight());
        leftTab = new CB_TabView(rec, "leftTab");

        if (Settings.useDescriptiveCB_Buttons.getValue()) {
            mainBtn1 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "CacheList");
            mainBtn2 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "Cache");
            mainBtn3 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "Nav");
            mainBtn4 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "Tool");
            mainBtn5 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "Misc");
        } else {
            mainBtn1 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "CacheList", Sprites.CacheList);
            mainBtn2 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "Cache", Sprites.Cache);
            mainBtn3 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "Nav", Sprites.Nav);
            mainBtn4 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "Tool", Sprites.Tool);
            mainBtn5 = new GestureButton(mainButtonSize, Settings.rememberLastAction.getValue(), "Misc", Sprites.Misc);
        }

        leftTab.addMainButton(mainBtn1);
        leftTab.addMainButton(mainBtn2);
        leftTab.addMainButton(mainBtn3);
        leftTab.addMainButton(mainBtn4);
        leftTab.addMainButton(mainBtn5);
        leftTab.setButtonList();
        addChild(leftTab);

        mainBtn1.addAction(ShowGeoCaches.action, true, GestureDirection.Up);
        mainBtn1.addAction(ShowParkingMenu.action, false, GestureDirection.Down);
        mainBtn1.addAction(ShowTrackableList.action, false, GestureDirection.Right);
        actionShare = new PlatformAction("Share", ViewConst.Share, Sprites.getSprite(IconName.share.name()));
        mainBtn1.addAction(actionShare, false, GestureDirection.Left);

        mainBtn2.addAction(ShowDescription.action, true, GestureDirection.Up);
        mainBtn2.addAction(ShowWayPoints.action, false, GestureDirection.Right);
        mainBtn2.addAction(ShowHint.action, false);
        mainBtn2.addAction(ShowSpoiler.action, false);
        mainBtn2.addAction(ShowLogs.action, false, GestureDirection.Down);
        mainBtn2.addAction(ShowNotes.action, false, GestureDirection.Left);
        mainBtn2.addAction(StartExternalDescription.getInstance(), false);

        mainBtn3.addAction(ShowMap.action, true, GestureDirection.Up);
        mainBtn3.addAction(ShowCompass.action, false, GestureDirection.Right);
        PlatformAction actionNavigateTo = new PlatformAction("NavigateTo", ViewConst.NAVIGATE_TO, Sprites.getSprite(IconName.navigate.name()));
        mainBtn3.addAction(actionNavigateTo, false, GestureDirection.Down);
        mainBtn3.addAction(ShowTracks.action, false, GestureDirection.Left);
        mainBtn3.addAction(MapDownloadMenu.getInstance(), false);

        mainBtn4.addAction(ShowDrafts.action, Settings.ShowDraftsAsDefaultView.getValue(), GestureDirection.Up);
        mainBtn4.addAction(ShowSolver1.action, false, GestureDirection.Left);
        mainBtn4.addAction(ShowSolver2.action, false, GestureDirection.Right);
        mainBtn4.addAction(TakePicture.action, false, GestureDirection.Down);
        mainBtn4.addAction(RecordVideo.action, false);
        mainBtn4.addAction(RecordVoice.action, false);

        mainBtn5.addAction(ShowCredits.getInstance(), false, GestureDirection.Up);
        mainBtn5.addAction(ShowSettings.getInstance(), false, GestureDirection.Left);
        mainBtn5.addAction(ShowParkingMenu.action, false, GestureDirection.Right);
        mainBtn5.addAction(SwitchDayNight.action, false);
        mainBtn5.addAction(ShowHelp.getInstance(), false);
        mainBtn5.addAction(SwitchTorch.action, false);
        mainBtn5.addAction(ShowAbout.getInstance(), true);
        mainBtn5.addAction(ShowQuit.getInstance(), false, GestureDirection.Down);

        Log.debug(sClass, "start with 'about view'");
        ShowAbout.getInstance().execute(); // activated as first view
    }

    private void autoLoadTrack() {
        String trackPath = Settings.TrackFolder.getValue() + "/Autoload";
        if (FileIO.createDirectory(trackPath)) {
            AbstractFile dir = FileFactory.createFile(trackPath);
            String[] files = dir.list();
            if (files != null) {
                for (String file : files) {
                    TrackList.getInstance().loadTrack(trackPath, file);
                }
            }
        } else {
            (FileFactory.createFile(trackPath)).mkdirs();
        }
    }

    public void setContentMaxY(float y) {
        synchronized (childs) {
            for (int i = 0, n = childs.size(); i < n; i++) {
                GL_View_Base view = childs.get(i);
                if (view instanceof CB_TabView) {
                    view.setHeight(y);
                }
            }
        }
    }

    public void switchDayNight() {
        reloadSprites(true);
    }

    public void reloadSprites(boolean switchDayNight) {

        if (!isInitial)
            renderInit();

        try {
            GL.that.stopRendering();
            if (switchDayNight) {
                boolean value = Settings.nightMode.getValue();
                value = !value;
                Settings.nightMode.setValue(value);
                Settings.getInstance().acceptChanges();
            }
            GL.that.onStop();
            Sprites.loadSprites(true);

            ((ShowMap) ShowMap.action).normalMapView.handleInvalidateTexture();

            GL.that.onStart();

            fireSkinChanged();

            removeChildren();

            GestureButton.refreshContextMenuSprite();
            addPhoneTab();

            // add Slider as last
            Slider slider = new Slider(this, "Slider");
            addChild(slider);
            slider.handleCacheSelectionChanged(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWayPoint());

            String state = Settings.nightMode.getValue() ? "Night" : "Day";

            GL.that.toast("Switch to " + state);

            Platform.dayNightSwitched();

            synchronized (childs) {
                for (int i = 0, n = childs.size(); i < n; i++) {
                    GL_View_Base view = childs.get(i);
                    if (view instanceof CB_TabView) {
                        ((CB_TabView) view).skinIsChanged();
                    }
                }
            }
            InvalidateTextureListeners.getInstance().fireInvalidateTexture();
        } catch (Exception ex) {
            Log.err(sClass, "reloadSprites", ex);
        }
        GL.that.restartRendering();
    }

    public void mToolsButtonOnLeftTabPerformClick() {
        mainBtn4.performClick();
    }

    public void filterSetChanged() {
        // change the icon
        boolean isFiltered = FilterInstances.isLastFilterSet();
        mainBtn1.isFiltered(isFiltered);
        mainBtn3.isFiltered(isFiltered);

        if (!Settings.useDescriptiveCB_Buttons.getValue()) {
            if (isFiltered) {
                mainBtn1.setButtonSprites(Sprites.CacheListFilter);
            } else {
                mainBtn1.setButtonSprites(Sprites.CacheList);
            }
        }
        String name;
        synchronized (CBDB.cacheList) {
            int filterCount = CBDB.cacheList.size();

            if (CBDB.cacheList.getCacheByGcCodeFromCacheList("CBPark") != null) {
                filterCount = filterCount - 1;
            }

            int cacheCountInDB = CBDB.getInstance().getCacheCountInDB();
            String strFilterCount = "";
            if (filterCount != cacheCountInDB) {
                strFilterCount = filterCount + "/";
            }

            name = "  (" + strFilterCount + cacheCountInDB + ")";
        }
        ((ShowGeoCaches) ShowGeoCaches.action).setNameExtension(name);
    }

    @Override
    public void renderChildren(final Batch batch, ParentInfo parentInfo) {
        super.renderChildren(batch, parentInfo);
    }

    @Override
    public void positionChanged() {
        try {
            TrackRecorder.getInstance().recordPosition();
        } catch (Exception ex) {
            Log.err(sClass, "PositionChanged()", "TrackRecorder.recordPosition()", ex);
        }

        if (GlobalCore.isSetSelectedCache()) {
            float distance = GlobalCore.getSelectedCache().recalculateAndGetDistance(CalculationType.FAST, false, Locator.getInstance().getMyPosition());
            if (GlobalCore.getSelectedWayPoint() != null) {
                distance = GlobalCore.getSelectedWayPoint().recalculateAndGetDistance();
            }

            if (Settings.switchViewApproach.getValue() && !GlobalCore.switchToCompassCompleted && (distance < Settings.SoundApproachDistance.getValue())) {
                if (((de.droidcachebox.menu.menuBtn3.ShowCompass) ShowCompass.action).getView() != null)
                    return;// don't show if showing compass
                if (((ShowMap) ShowMap.action).normalMapView.isVisible() && ((ShowMap) ShowMap.action).normalMapView.isCarMode())
                    return; // don't show on visible map at carMode
                ShowCompass.action.execute();
                GlobalCore.switchToCompassCompleted = true;
            }
        }
    }

    @Override
    public String getReceiverName() {
        return "Core.MainViewBase";
    }

    @Override
    public Priority getPriority() {
        return Priority.High;
    }

    @Override
    public void orientationChanged() {
    }

    @Override
    public void speedChanged() {
    }

    public boolean isInitialized() {
        return isInitial;
    }

}
