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

import com.badlogic.gdx.graphics.g2d.Batch;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.TrackRecorder;
import de.droidcachebox.core.API_ErrorEventHandler;
import de.droidcachebox.core.API_ErrorEventHandlerList;
import de.droidcachebox.core.API_ErrorEventHandlerList.API_ERROR;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.database.Database;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.Slider;
import de.droidcachebox.gdx.controls.dialogs.Toast;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.CB_ActionButton.GestureDirection;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.CompassView;
import de.droidcachebox.invalidateTextureEventList;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

import java.util.Timer;
import java.util.TimerTask;

import static de.droidcachebox.gdx.math.GL_UISizes.MainBtnSize;
import static de.droidcachebox.locator.map.MapViewBase.INITIAL_WP_LIST;

/**
 * the ViewManager has one tab (leftTab) on the phone<br>
 * tablet is no longer implemented! two tabs (leftTab , rightTab) on the tablet.<br>
 * Each tab has buttons (5/3) at the bottom for selecting the different actions to do.<br>
 *
 * @author ging-buh
 * @author Longri
 */
public class ViewManager extends MainViewBase implements PositionChangedEvent {
    private static final String log = "ViewManager";
    public static ViewManager that;
    public static CB_TabView leftTab; // the only one (has been left aand right for Tablet)

    public static Action_PlatformActivity actionTakePicture, actionRecordVideo, actionRecordVoice, actionShare;

    private GestureButton db_button; // default: show CacheList
    private GestureButton cache_button; // default: show CacheDecription on Phone ( and Waypoints on Tablet )
    private GestureButton navButton; // default: show map on phone ( and show Compass on Tablet )
    private GestureButton tool_button; // default: show ToolsMenu or Drafts or Drafts Context menu (depends on config)
    private GestureButton misc_button; // default: show About View

    private boolean isInitial = false;
    private boolean isFiltered = false;

    public ViewManager(CB_RectF rec) {
        super(rec);
        PositionChangedListeners.addListener(this);
        that = this;
    }

    public static void reloadCacheList() {
        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(de.droidcachebox.Config.GcLogin.getValue());
        synchronized (Database.Data.cacheList) {
            CacheListDAO cacheListDAO = new CacheListDAO();
            cacheListDAO.ReadCacheList(Database.Data.cacheList, sqlWhere, false, de.droidcachebox.Config.ShowAllWaypoints.getValue());
        }
        CacheListChangedListeners.getInstance().cacheListChanged();
    }


    @Override
    protected void initialize() {
        Log.debug(log, "Start ViewManager-Initial");

        de.droidcachebox.GlobalCore.receiver = new de.droidcachebox.GlobalLocationReceiver();

        UnitFormatter.setUseImperialUnits(de.droidcachebox.Config.ImperialUnits.getValue());
        de.droidcachebox.Config.ImperialUnits.addSettingChangedListener(() -> UnitFormatter.setUseImperialUnits(de.droidcachebox.Config.ImperialUnits.getValue()));

        de.droidcachebox.Config.ShowAllWaypoints.addSettingChangedListener(() -> {
            reloadCacheList();
            // must reload MapViewCacheList: do this over MapView.INITIAL_WP_LIST
            Abstract_ShowMap.getInstance().normalMapView.setNewSettings(INITIAL_WP_LIST);
        });

        API_ErrorEventHandlerList.addHandler(new API_ErrorEventHandler() {
            @Override
            public void InvalidAPI_Key() {
                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        String Msg = Translation.get("apiKeyInvalid") + de.droidcachebox.GlobalCore.br + de.droidcachebox.GlobalCore.br;
                        Msg += Translation.get("wantApi");

                        MessageBox.show(Msg, Translation.get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, (which, data) -> {
                            if (which == MessageBox.BUTTON_POSITIVE)
                                PlatformUIBase.getApiKey();
                            return true;
                        });
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
                        String Msg = Translation.get("apiKeyExpired") + de.droidcachebox.GlobalCore.br + de.droidcachebox.GlobalCore.br;
                        Msg += Translation.get("wantApi");

                        MessageBox.show(Msg, Translation.get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, (which, data) -> {
                            if (which == MessageBox.BUTTON_POSITIVE)
                                PlatformUIBase.getApiKey();
                            return true;
                        });
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
                        String Msg = Translation.get("apiKeyNeeded") + de.droidcachebox.GlobalCore.br + de.droidcachebox.GlobalCore.br;
                        Msg += Translation.get("wantApi");

                        MessageBox.show(Msg, Translation.get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, (which, data) -> {
                            if (which == MessageBox.BUTTON_POSITIVE)
                                PlatformUIBase.getApiKey();
                            return true;
                        }, de.droidcachebox.Config.RememberAsk_Get_API_Key);
                    }
                };
                t.schedule(tt, 1500);
            }
        });

        addPhoneTab();

        // add Slider as last
        Slider slider = new Slider(this, "Slider");
        this.addChild(slider);

        Log.debug(log, "Ende ViewManager-Initial");

        autoLoadTrack();

        if (de.droidcachebox.Config.TrackRecorderStartup.getValue() && PlatformUIBase.isGPSon()) {
            de.droidcachebox.TrackRecorder.StartRecording();
        }
        de.droidcachebox.Config.TrackDistance.addSettingChangedListener(() -> de.droidcachebox.TrackRecorder.distanceForNextTrackpoint = de.droidcachebox.Config.TrackDistance.getValue());

        // set last selected Cache
        String sGc = de.droidcachebox.Config.LastSelectedCache.getValue();
        if (sGc != null && sGc.length() > 0) {
            synchronized (Database.Data.cacheList) {
                for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                    Cache c = Database.Data.cacheList.get(i);
                    if (c.getGcCode().equalsIgnoreCase(sGc)) {
                        Log.debug(log, "ViewManager: Set selectedCache to " + c.getGcCode() + " from lastSaved.");
                        de.droidcachebox.GlobalCore.setSelectedCache(c); // !! sets GlobalCore.setAutoResort to false
                        break;
                    }
                }
            }
        }

        de.droidcachebox.GlobalCore.setAutoResort(de.droidcachebox.Config.StartWithAutoSelect.getValue());
        filterSetChanged();
        GL.that.removeRenderView(this);

        de.droidcachebox.AppRater.app_launched();

        if (de.droidcachebox.Config.AccessToken.getValue().equals(""))
            API_ErrorEventHandlerList.handleApiKeyError(API_ERROR.NO);

        isInitial = true;

        PlatformUIBase.handleExternalRequest();

    }

    private void addPhoneTab() {
        // nur ein Tab  mit fünf Buttons

        CB_RectF rec = new CB_RectF(0, 0, GL_UISizes.UI_Left.getWidth(), getHeight() - UiSizes.getInstance().getInfoSliderHeight());
        leftTab = new CB_TabView(rec, "leftTab");

        if (de.droidcachebox.Config.useDescriptiveCB_Buttons.getValue()) {
            db_button = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "CacheList");
            cache_button = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Cache");
            navButton = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Nav");
            tool_button = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Tool");
            misc_button = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Misc");
        } else {
            db_button = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "CacheList", Sprites.CacheList);
            cache_button = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Cache", Sprites.Cache);
            navButton = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Nav", Sprites.Nav);
            tool_button = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Tool", Sprites.Tool);
            misc_button = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Misc", Sprites.Misc);
        }

        CB_ButtonBar mainButtonBar = new CB_ButtonBar();
        mainButtonBar.addButton(db_button);
        mainButtonBar.addButton(cache_button);
        mainButtonBar.addButton(navButton);
        mainButtonBar.addButton(tool_button);
        mainButtonBar.addButton(misc_button);
        leftTab.setButtonList(mainButtonBar);
        addChild(leftTab);

        // Actions den Buttons zuweisen
        db_button.addAction(Abstract_ShowCacheList.getInstance(), true, GestureDirection.Up);
        db_button.addAction(Action_ParkingDialog.getInstance(), false, GestureDirection.Down);
        db_button.addAction(Abstract_ShowTrackableListAction.getInstance(), false, GestureDirection.Right);
        actionShare = new Action_PlatformActivity("Share", MenuID.AID_Share, ViewConst.Share, Sprites.getSprite(IconName.share.name()));
        db_button.addAction(actionShare, false, GestureDirection.Left);

        cache_button.addAction(Abstract_ShowDescriptionAction.getInstance(), true, GestureDirection.Up);
        cache_button.addAction(Abstract_ShowWaypointAction.getInstance(), false, GestureDirection.Right);
        cache_button.addAction(Action_HintDialog.getInstance(), false);
        cache_button.addAction(Abstract_ShowSpoilerAction.getInstance(), false);
        cache_button.addAction(Abstract_ShowLogAction.getInstance(), false, GestureDirection.Down);
        cache_button.addAction(Abstract_ShowNotesAction.getInstance(), false, GestureDirection.Left);
        cache_button.addAction(Action_StartExternalDescription.getInstance(), false);

        navButton.addAction(Abstract_ShowMap.getInstance(), true, GestureDirection.Up);
        navButton.addAction(Abstract_ShowCompassAction.getInstance(), false, GestureDirection.Right);
        Action_PlatformActivity actionNavigateTo = new Action_PlatformActivity("NavigateTo", MenuID.AID_NAVIGATE_TO, ViewConst.NAVIGATE_TO, Sprites.getSprite(IconName.navigate.name()));
        navButton.addAction(actionNavigateTo, false, GestureDirection.Down);
        navButton.addAction(Abstract_ShowTrackListAction.getInstance(), false, GestureDirection.Left);
        navButton.addAction(Action_MapDownload.getInstance(), false);

        tool_button.addAction(Abstract_ShowDraftsAction.getInstance(), de.droidcachebox.Config.ShowDraftsAsDefaultView.getValue(), GestureDirection.Up);
        tool_button.addAction(Abstract_ShowSolverAction.getInstance(), false, GestureDirection.Left);
        tool_button.addAction(Abstract_ShowSolverAction2.getInstance(), false, GestureDirection.Right);
        actionTakePicture = new Action_PlatformActivity("TakePhoto", MenuID.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO, Sprites.getSprite(IconName.log10icon.name()));
        tool_button.addAction(actionTakePicture, false, GestureDirection.Down);
        actionRecordVideo = new Action_PlatformActivity("RecVideo", MenuID.AID_VIDEO_REC, ViewConst.VIDEO_REC, Sprites.getSprite(IconName.videoIcon.name()));
        tool_button.addAction(actionRecordVideo, false);
        actionRecordVoice = new Action_PlatformActivity("VoiceRec", MenuID.AID_VOICE_REC, ViewConst.VOICE_REC, Sprites.getSprite(IconName.voiceRecIcon.name()));
        tool_button.addAction(actionRecordVoice, false);
        tool_button.addAction(Action_ParkingDialog.getInstance(), false);

        misc_button.addAction(Abstract_ShowCreditsAction.getInstance(), false);
        misc_button.addAction(Action_SettingsActivity.getInstance(), false, GestureDirection.Left);
        misc_button.addAction(Action_switch_DayNight.getInstance(), false);
        misc_button.addAction(Action_Help.getInstance(), false);
        misc_button.addAction(Action_ContactOwner.getInstance(), false);
        misc_button.addAction(Action_switch_Torch.getInstance(), false);
        misc_button.addAction(Abstract_ShowAbout.getInstance(), true, GestureDirection.Up);
        misc_button.addAction(Action_ShowQuit.getInstance(), false, GestureDirection.Down);

        Abstract_ShowAbout.getInstance().Execute();
    }

    private void autoLoadTrack() {
        String trackPath = de.droidcachebox.Config.TrackFolder.getValue() + "/Autoload";
        if (FileIO.createDirectory(trackPath)) {
            File dir = FileFactory.createFile(trackPath);
            String[] files = dir.list();
            if (!(files == null)) {
                if (files.length > 0) {
                    for (String file : files) {
                        de.droidcachebox.RouteOverlay.LoadTrack(trackPath, file);
                    }
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
            initialize();

        try {
            GL.that.stopRendering();
            if (switchDayNight)
                de.droidcachebox.Config.changeDayNight();
            GL.that.onStop();
            Sprites.loadSprites(true);
            Abstract_ShowMap.getInstance().normalMapView.invalidateTexture();
            GL.that.onStart();
            CallSkinChanged();

            this.removeChilds();

            GestureButton.refreshContextMenuSprite();
            addPhoneTab();

            // add Slider as last
            Slider slider = new Slider(this, "Slider");
            this.addChild(slider);
            slider.selectedCacheChanged(de.droidcachebox.GlobalCore.getSelectedCache(), de.droidcachebox.GlobalCore.getSelectedWaypoint());

            String state = de.droidcachebox.Config.nightMode.getValue() ? "Night" : "Day";

            GL.that.Toast("Switch to " + state, Toast.LENGTH_SHORT);

            PlatformUIBase.dayNightSwitched();

            synchronized (childs) {
                for (int i = 0, n = childs.size(); i < n; i++) {
                    GL_View_Base view = childs.get(i);
                    if (view instanceof CB_TabView) {
                        ((CB_TabView) view).skinIsChanged();
                    }
                }
            }
            invalidateTextureEventList.Call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        GL.that.restartRendering();
    }

    public void mToolsButtonOnLeftTabPerformClick() {
        tool_button.performClick();
    }

    public void filterSetChanged() {
        // change the icon
        isFiltered = FilterInstances.isLastFilterSet();
        db_button.isFiltered(isFiltered);

        if (!de.droidcachebox.Config.useDescriptiveCB_Buttons.getValue()) {
            if (isFiltered) {
                db_button.setButtonSprites(Sprites.CacheListFilter);
            } else {
                db_button.setButtonSprites(Sprites.CacheList);
            }
        }

        // ##################################
        // Set new list size at context menu
        // ##################################
        String Name;

        synchronized (Database.Data.cacheList) {
            int filterCount = Database.Data.cacheList.size();

            if (Database.Data.cacheList.getCacheByGcCodeFromCacheList("CBPark") != null)
                --filterCount;

            int DBCount = Database.Data.getCacheCountInDB();
            String strFilterCount = "";
            if (filterCount != DBCount) {
                strFilterCount = filterCount + "/";
            }

            Name = "  (" + strFilterCount + DBCount + ")";
        }
        Abstract_ShowCacheList.getInstance().setNameExtension(Name);
    }

    @Override
    public void renderChilds(final Batch batch, ParentInfo parentInfo) {
        if (childs == null)
            return;
        super.renderChilds(batch, parentInfo);
    }

    @Override
    public void positionChanged() {
        try {
            TrackRecorder.recordPosition();
        } catch (Exception e) {
            Log.err(log, "Core.MainViewBase.PositionChanged()", "TrackRecorder.recordPosition()", e);
            e.printStackTrace();
        }

        if (de.droidcachebox.GlobalCore.isSetSelectedCache()) {
            float distance = de.droidcachebox.GlobalCore.getSelectedCache().Distance(CalculationType.FAST, false);
            if (de.droidcachebox.GlobalCore.getSelectedWaypoint() != null) {
                distance = de.droidcachebox.GlobalCore.getSelectedWaypoint().Distance();
            }

            if (de.droidcachebox.Config.switchViewApproach.getValue() && !de.droidcachebox.GlobalCore.switchToCompassCompleted && (distance < de.droidcachebox.Config.SoundApproachDistance.getValue())) {
                if (CompassView.getInstance().isVisible())
                    return;// don't show if showing compass
                if (Abstract_ShowMap.getInstance().normalMapView.isVisible() && Abstract_ShowMap.getInstance().normalMapView.isCarMode())
                    return; // don't show on visible map at carMode
                Abstract_ShowCompassAction.getInstance().Execute();
                de.droidcachebox.GlobalCore.switchToCompassCompleted = true;
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
