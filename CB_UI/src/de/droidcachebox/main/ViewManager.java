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
package de.droidcachebox.main;

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
import de.droidcachebox.gdx.main.*;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.CompassView;
import de.droidcachebox.invalidateTextureEventList;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.main.menuBtn1.ParkingDialog;
import de.droidcachebox.main.menuBtn1.ShowCacheList;
import de.droidcachebox.main.menuBtn1.ShowTrackableList;
import de.droidcachebox.main.menuBtn2.*;
import de.droidcachebox.main.menuBtn3.MapDownload;
import de.droidcachebox.main.menuBtn3.ShowCompass;
import de.droidcachebox.main.menuBtn3.ShowMap;
import de.droidcachebox.main.menuBtn3.ShowTrackList;
import de.droidcachebox.main.menuBtn4.ShowDrafts;
import de.droidcachebox.main.menuBtn4.ShowSolver1;
import de.droidcachebox.main.menuBtn4.ShowSolver2;
import de.droidcachebox.main.menuBtn5.*;
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

    public static PlatformActivity actionTakePicture, actionRecordVideo, actionRecordVoice, actionShare;

    private GestureButton mainBtn1; // default: show CacheList
    private GestureButton mainBtn2; // default: show CacheDecription on Phone ( and Waypoints on Tablet )
    private GestureButton mainBtn3; // default: show map on phone ( and show Compass on Tablet )
    private GestureButton mainBtn4; // default: show ToolsMenu or Drafts or Drafts Context menu (depends on config)
    private GestureButton mainBtn5; // default: show About View

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
            ShowMap.getInstance().normalMapView.setNewSettings(INITIAL_WP_LIST);
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
            mainBtn1 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "CacheList");
            mainBtn2 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Cache");
            mainBtn3 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Nav");
            mainBtn4 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Tool");
            mainBtn5 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Misc");
        } else {
            mainBtn1 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "CacheList", Sprites.CacheList);
            mainBtn2 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Cache", Sprites.Cache);
            mainBtn3 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Nav", Sprites.Nav);
            mainBtn4 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Tool", Sprites.Tool);
            mainBtn5 = new GestureButton(MainBtnSize, de.droidcachebox.Config.rememberLastAction.getValue(), "Misc", Sprites.Misc);
        }

        CB_ButtonBar mainButtonBar = new CB_ButtonBar();
        mainButtonBar.addButton(mainBtn1);
        mainButtonBar.addButton(mainBtn2);
        mainButtonBar.addButton(mainBtn3);
        mainButtonBar.addButton(mainBtn4);
        mainButtonBar.addButton(mainBtn5);
        leftTab.setButtonList(mainButtonBar);
        addChild(leftTab);

        // Actions den Buttons zuweisen
        mainBtn1.addAction(ShowCacheList.getInstance(), true, GestureDirection.Up);
        mainBtn1.addAction(ParkingDialog.getInstance(), false, GestureDirection.Down);
        mainBtn1.addAction(ShowTrackableList.getInstance(), false, GestureDirection.Right);
        actionShare = new PlatformActivity("Share", MenuID.AID_Share, ViewConst.Share, Sprites.getSprite(IconName.share.name()));
        mainBtn1.addAction(actionShare, false, GestureDirection.Left);

        mainBtn2.addAction(ShowDescription.getInstance(), true, GestureDirection.Up);
        mainBtn2.addAction(ShowWaypoint.getInstance(), false, GestureDirection.Right);
        mainBtn2.addAction(HintDialog.getInstance(), false);
        mainBtn2.addAction(ShowSpoiler.getInstance(), false);
        mainBtn2.addAction(ShowLogs.getInstance(), false, GestureDirection.Down);
        mainBtn2.addAction(ShowNotes.getInstance(), false, GestureDirection.Left);
        // mainBtn2.addAction(ShowTrackableList.getInstance(),false);
        // mainBtn2.addAction(ShowSolver1.getInstance(),false);
        // mainBtn2.addAction(ShowSolver2.getInstance(),false);
        mainBtn2.addAction(StartExternalDescription.getInstance(), false);

        mainBtn3.addAction(ShowMap.getInstance(), true, GestureDirection.Up);
        mainBtn3.addAction(ShowCompass.getInstance(), false, GestureDirection.Right);
        PlatformActivity actionNavigateTo = new PlatformActivity("NavigateTo", MenuID.AID_NAVIGATE_TO, ViewConst.NAVIGATE_TO, Sprites.getSprite(IconName.navigate.name()));
        mainBtn3.addAction(actionNavigateTo, false, GestureDirection.Down);
        mainBtn3.addAction(ShowTrackList.getInstance(), false, GestureDirection.Left);
        mainBtn3.addAction(MapDownload.getInstance(), false);

        mainBtn4.addAction(ShowDrafts.getInstance(), de.droidcachebox.Config.ShowDraftsAsDefaultView.getValue(), GestureDirection.Up);
        mainBtn4.addAction(ShowSolver1.getInstance(), false, GestureDirection.Left);
        mainBtn4.addAction(ShowSolver2.getInstance(), false, GestureDirection.Right);
        actionTakePicture = new PlatformActivity("TakePhoto", MenuID.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO, Sprites.getSprite(IconName.log10icon.name()));
        mainBtn4.addAction(actionTakePicture, false, GestureDirection.Down);
        actionRecordVideo = new PlatformActivity("RecVideo", MenuID.AID_VIDEO_REC, ViewConst.VIDEO_REC, Sprites.getSprite(IconName.videoIcon.name()));
        mainBtn4.addAction(actionRecordVideo, false);
        actionRecordVoice = new PlatformActivity("VoiceRec", MenuID.AID_VOICE_REC, ViewConst.VOICE_REC, Sprites.getSprite(IconName.voiceRecIcon.name()));
        mainBtn4.addAction(actionRecordVoice, false);
        mainBtn4.addAction(ParkingDialog.getInstance(), false);

        mainBtn5.addAction(ShowCredits.getInstance(), false);
        mainBtn5.addAction(Settings.getInstance(), false, GestureDirection.Left);
        mainBtn5.addAction(SwitchDayNight.getInstance(), false);
        mainBtn5.addAction(HelpOnline.getInstance(), false);
        mainBtn5.addAction(ContactOwner.getInstance(), false);
        // mainBtn5.addAction(TranslateDescription.getInstance(), false);
        mainBtn5.addAction(SwitchTorch.getInstance(), false);
        mainBtn5.addAction(ShowAbout.getInstance(), true, GestureDirection.Up);
        mainBtn5.addAction(ShowQuit.getInstance(), false, GestureDirection.Down);

        ShowAbout.getInstance().Execute();
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
            ShowMap.getInstance().normalMapView.invalidateTexture();
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
        } catch (Exception ex) {
            Log.err(log, "reloadSprites", ex);
        }
        GL.that.restartRendering();
    }

    public void mToolsButtonOnLeftTabPerformClick() {
        mainBtn4.performClick();
    }

    public void filterSetChanged() {
        // change the icon
        isFiltered = FilterInstances.isLastFilterSet();
        mainBtn1.isFiltered(isFiltered);

        if (!de.droidcachebox.Config.useDescriptiveCB_Buttons.getValue()) {
            if (isFiltered) {
                mainBtn1.setButtonSprites(Sprites.CacheListFilter);
            } else {
                mainBtn1.setButtonSprites(Sprites.CacheList);
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
        ShowCacheList.getInstance().setNameExtension(Name);
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
                distance = de.droidcachebox.GlobalCore.getSelectedWaypoint().getDistance();
            }

            if (de.droidcachebox.Config.switchViewApproach.getValue() && !de.droidcachebox.GlobalCore.switchToCompassCompleted && (distance < de.droidcachebox.Config.SoundApproachDistance.getValue())) {
                if (CompassView.getInstance().isVisible())
                    return;// don't show if showing compass
                if (ShowMap.getInstance().normalMapView.isVisible() && ShowMap.getInstance().normalMapView.isCarMode())
                    return; // don't show on visible map at carMode
                ShowCompass.getInstance().Execute();
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
