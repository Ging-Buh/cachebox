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
package CB_UI.GL_UI.Main;

import CB_Core.Api.API_ErrorEventHandler;
import CB_Core.Api.API_ErrorEventHandlerList;
import CB_Core.Api.API_ErrorEventHandlerList.API_ERROR;
import CB_Core.CacheListChangedEventList;
import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheListDAO;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.*;
import CB_UI.GL_UI.Controls.Slider;
import CB_UI.GL_UI.Main.Actions.*;
import CB_UI.GL_UI.Views.CompassView;
import CB_UI_Base.Energy;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowQuit;
import CB_UI_Base.GL_UI.Main.CB_ActionButton.GestureDirection;
import CB_UI_Base.GL_UI.Main.CB_Button;
import CB_UI_Base.GL_UI.Main.CB_ButtonList;
import CB_UI_Base.GL_UI.Main.CB_TabView;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.Log;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.UnitFormatter;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.Timer;
import java.util.TimerTask;

import static CB_Locator.Map.MapViewBase.INITIAL_WP_LIST;
import static CB_UI_Base.Math.GL_UISizes.MainBtnSize;

/**
 * the TabMainView has one tab (leftTab) on the phone<br>
 * tablet is no longer implemented! two tabs (leftTab , rightTab) on the tablet.<br>
 * Each tab has buttons (5/3) at the bottom for selecting the different actions to do.<br>
 *
 * @author ging-buh
 * @author Longri
 */
public class TabMainView extends MainViewBase implements PositionChangedEvent {
    private static final String log = "TabMainView";
    public static TabMainView that;
    public static CB_TabView leftTab; // the only one (has been left aand right for Tablet)

    public static CB_Action_PlatformActivity actionTakePicture;
    public static CB_Action_PlatformActivity actionRecordVideo;
    public static CB_Action_PlatformActivity actionRecordVoice;

    private CB_Button mCacheListButtonOnLeftTab; // default: show CacheList
    private CB_Button mDescriptionButtonOnLeftTab; // default: show CacheDecription on Phone ( and Waypoints on Tablet )
    private CB_Button mMapButtonOnLeftTab; // default: show map on phone ( and show Compass on Tablet )
    private CB_Button mToolsButtonOnLeftTab; // default: show ToolsMenu or Fieldnotes or Fieldnotes Context menu (depends on config)
    private CB_Button mAboutButtonOnLeftTab; // default: show About View

    private boolean isInitial = false;
    private boolean isFiltered = false;

    public TabMainView(CB_RectF rec) {
        super(rec);
        PositionChangedEventList.Add(this);
        that = this;
    }

    public static void reloadCacheList() {
        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
        synchronized (Database.Data.cacheList) {
            CacheListDAO cacheListDAO = new CacheListDAO();
            cacheListDAO.ReadCacheList(Database.Data.cacheList, sqlWhere, false, Config.ShowAllWaypoints.getValue());
        }
        CacheListChangedEventList.Call();
    }


    @Override
    protected void Initial() {
        Log.debug(log, "Start TabMainView-Initial");

        GlobalCore.receiver = new CB_UI.GlobalLocationReceiver();

        UnitFormatter.setUseImperialUnits(Config.ImperialUnits.getValue());
        Config.ImperialUnits.addSettingChangedListener(() -> UnitFormatter.setUseImperialUnits(Config.ImperialUnits.getValue()));

        Config.ShowAllWaypoints.addSettingChangedListener(() -> {
            reloadCacheList();
            // must reload MapViewCacheList: do this over MapView.INITIAL_WP_LIST
            CB_Action_ShowMap.getInstance().normalMapView.setNewSettings(INITIAL_WP_LIST);
        });

        CoreSettingsForward.VersionString = GlobalCore.getInstance().getVersionString();
        CoreSettingsForward.DisplayOff = Energy.DisplayOff();
        Energy.addChangedEventListener(() -> {
            CoreSettingsForward.VersionString = GlobalCore.getInstance().getVersionString();
            CoreSettingsForward.DisplayOff = Energy.DisplayOff();
        });

        API_ErrorEventHandlerList.addHandler(new API_ErrorEventHandler() {
            @Override
            public void InvalidAPI_Key() {
                TimerTask tt = new TimerTask() {

                    @Override
                    public void run() {
                        String Msg = Translation.get("apiKeyInvalid") + GlobalCore.br + GlobalCore.br;
                        Msg += Translation.get("wantApi");

                        MessageBox.show(Msg, Translation.get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, (which, data) -> {
                            if (which == MessageBox.BUTTON_POSITIVE)
                                PlatformConnector.callGetApiKey();
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
                        String Msg = Translation.get("apiKeyExpired") + GlobalCore.br + GlobalCore.br;
                        Msg += Translation.get("wantApi");

                        MessageBox.show(Msg, Translation.get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, (which, data) -> {
                            if (which == MessageBox.BUTTON_POSITIVE)
                                PlatformConnector.callGetApiKey();
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
                        String Msg = Translation.get("apiKeyNeeded") + GlobalCore.br + GlobalCore.br;
                        Msg += Translation.get("wantApi");

                        MessageBox.show(Msg, Translation.get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, (which, data) -> {
                            if (which == MessageBox.BUTTON_POSITIVE)
                                PlatformConnector.callGetApiKey();
                            return true;
                        }, Config.RememberAsk_Get_API_Key);
                    }
                };
                t.schedule(tt, 1500);
            }
        });

        addPhoneTab();

        // add Slider as last
        Slider slider = new Slider(this, "Slider");
        this.addChild(slider);

        Log.debug(log, "Ende TabMainView-Initial");

        autoLoadTrack();

        if (Config.TrackRecorderStartup.getValue() && PlatformConnector.isGPSon()) {
            TrackRecorder.StartRecording();
        }

        // set last selected Cache
        String sGc = Config.LastSelectedCache.getValue();
        if (sGc != null && sGc.length() > 0) {
            synchronized (Database.Data.cacheList) {
                for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                    Cache c = Database.Data.cacheList.get(i);
                    if (c.getGcCode().equalsIgnoreCase(sGc)) {
                        Log.debug(log, "TabMainView: Set selectedCache to " + c.getGcCode() + " from lastSaved.");
                        GlobalCore.setSelectedCache(c); // !! sets GlobalCore.setAutoResort to false
                        break;
                    }
                }
            }
        }

        GlobalCore.setAutoResort(Config.StartWithAutoSelect.getValue());

        PlatformConnector.FirstShow();
        filterSetChanged();
        GL.that.removeRenderView(this);

        AppRater.app_launched();

        if (Config.AccessToken.getValue().equals(""))
            API_ErrorEventHandlerList.handleApiKeyError(API_ERROR.NO);

        isInitial = true;

    }

    private void addPhoneTab() {
        // nur ein Tab  mit fünf Buttons

        CB_RectF rec = new CB_RectF(0, 0, GL_UISizes.UI_Left.getWidth(), getHeight() - UiSizes.that.getInfoSliderHeight());
        leftTab = new CB_TabView(rec, "leftTab");

        if (Config.useDescriptiveCB_Buttons.getValue()) {
            mCacheListButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "CacheList");
            mDescriptionButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "Cache");
            mMapButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "Nav");
            mToolsButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "Tool");
            mAboutButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "Misc");
        } else {
            mCacheListButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "CacheList", Sprites.CacheList);
            mDescriptionButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "Cache", Sprites.Cache);
            mMapButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "Nav", Sprites.Nav);
            mToolsButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "Tool", Sprites.Tool);
            mAboutButtonOnLeftTab = new CB_Button(MainBtnSize, Config.rememberLastAction.getValue(), "Misc", Sprites.Misc);
        }

        CB_ButtonList btnList = new CB_ButtonList();
        btnList.addButton(mCacheListButtonOnLeftTab);
        btnList.addButton(mDescriptionButtonOnLeftTab);
        btnList.addButton(mMapButtonOnLeftTab);
        btnList.addButton(mToolsButtonOnLeftTab);
        btnList.addButton(mAboutButtonOnLeftTab);
        leftTab.setButtonList(btnList);
        addChild(leftTab);

        // Actions den Buttons zuweisen
        mCacheListButtonOnLeftTab.addAction(CB_Action_ShowCacheList.getInstance(), true, GestureDirection.Up);
        mCacheListButtonOnLeftTab.addAction(CB_Action_ParkingDialog.getInstance(), false);
        mCacheListButtonOnLeftTab.addAction(CB_Action_ShowTrackableListView.getInstance(), false, GestureDirection.Right);

        mDescriptionButtonOnLeftTab.addAction(CB_Action_ShowDescriptionView.getInstance(), true, GestureDirection.Up);
        mDescriptionButtonOnLeftTab.addAction(CB_Action_ShowWaypointView.getInstance(), false, GestureDirection.Right);
        mDescriptionButtonOnLeftTab.addAction(CB_Action_HintDialog.getInstance(), false);
        mDescriptionButtonOnLeftTab.addAction(CB_Action_ShowSpoilerView.getInstance(), false);
        mDescriptionButtonOnLeftTab.addAction(CB_Action_ShowLogView.getInstance(), false, GestureDirection.Down);
        mDescriptionButtonOnLeftTab.addAction(CB_Action_ShowNotesView.getInstance(), false);
        mDescriptionButtonOnLeftTab.addAction(CB_Action_StartExternalDescription.getInstance(), false);

        mMapButtonOnLeftTab.addAction(CB_Action_ShowMap.getInstance(), true, GestureDirection.Up);
        mMapButtonOnLeftTab.addAction(CB_Action_ShowCompassView.getInstance(), false, GestureDirection.Right);
        CB_Action_PlatformActivity actionNavigateTo = new CB_Action_PlatformActivity("NavigateTo", MenuID.AID_NAVIGATE_TO, ViewConst.NAVIGATE_TO, Sprites.getSprite(IconName.navigate.name()));
        mMapButtonOnLeftTab.addAction(actionNavigateTo, false, GestureDirection.Down);
        mMapButtonOnLeftTab.addAction(CB_Action_ShowTrackListView.getInstance(), false, GestureDirection.Left);
        mMapButtonOnLeftTab.addAction(CB_Action_MapDownload.getInstance(), false);

        mToolsButtonOnLeftTab.addAction(CB_Action_ShowFieldNotesView.getInstance(), Config.ShowFieldnotesAsDefaultView.getValue(), GestureDirection.Up);
        mToolsButtonOnLeftTab.addAction(CB_Action_ShowSolverView.getInstance(), false, GestureDirection.Left);
        mToolsButtonOnLeftTab.addAction(CB_Action_ShowSolverView2.getInstance(), false, GestureDirection.Right);
        actionTakePicture = new CB_Action_PlatformActivity("TakePhoto", MenuID.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO, Sprites.getSprite(IconName.log10icon.name()));
        mToolsButtonOnLeftTab.addAction(actionTakePicture, false, GestureDirection.Down);
        actionRecordVideo = new CB_Action_PlatformActivity("RecVideo", MenuID.AID_VIDEO_REC, ViewConst.VIDEO_REC, Sprites.getSprite(IconName.videoIcon.name()));
        mToolsButtonOnLeftTab.addAction(actionRecordVideo, false);
        actionRecordVoice = new CB_Action_PlatformActivity("VoiceRec", MenuID.AID_VOICE_REC, ViewConst.VOICE_REC, Sprites.getSprite(IconName.voiceRecIcon.name()));
        mToolsButtonOnLeftTab.addAction(actionRecordVoice, false);
        mToolsButtonOnLeftTab.addAction(CB_Action_ParkingDialog.getInstance(), false);

        mAboutButtonOnLeftTab.addAction(CB_Action_ShowCreditsView.getInstance(), false);
        mAboutButtonOnLeftTab.addAction(CB_Action_SettingsActivity.getInstance(), false, GestureDirection.Left);
        mAboutButtonOnLeftTab.addAction(CB_Action_switch_DayNight.getInstance(), false);
        mAboutButtonOnLeftTab.addAction(CB_Action_Help.getInstance(), false);
        mAboutButtonOnLeftTab.addAction(CB_Action_Mail.getInstance(), false);
        mAboutButtonOnLeftTab.addAction(CB_Action_switch_Torch.getInstance(), false);
        mAboutButtonOnLeftTab.addAction(CB_Action_ShowAbout.getInstance(), true, GestureDirection.Up);
        mAboutButtonOnLeftTab.addAction(CB_Action_ShowQuit.getInstance(), false, GestureDirection.Down);

        CB_Action_ShowAbout.getInstance().Execute();
    }

    private void autoLoadTrack() {
        String trackPath = Config.TrackFolder.getValue() + "/Autoload";
        if (FileIO.createDirectory(trackPath)) {
            File dir = FileFactory.createFile(trackPath);
            String[] files = dir.list();
            if (!(files == null)) {
                if (files.length > 0) {
                    for (String file : files) {
                        RouteOverlay.LoadTrack(trackPath, file);
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
            Initial();

        try {
            GL.that.StopRender();
            if (switchDayNight)
                Config.changeDayNight();
            GL.that.onStop();
            Sprites.loadSprites(true);
            CB_Action_ShowMap.getInstance().normalMapView.invalidateTexture();
            GL.that.onStart();
            CallSkinChanged();

            this.removeChilds();

            CB_Button.refreshContextMenuSprite();
            addPhoneTab();

            // add Slider as last
            Slider slider = new Slider(this, "Slider");
            this.addChild(slider);
            slider.SelectedCacheChanged(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());

            String state = Config.nightMode.getValue() ? "Night" : "Day";

            GL.that.Toast("Switch to " + state, Toast.LENGTH_SHORT);

            PlatformConnector.DayNightSwitched();

            synchronized (childs) {
                for (int i = 0, n = childs.size(); i < n; i++) {
                    GL_View_Base view = childs.get(i);
                    if (view instanceof CB_TabView) {
                        ((CB_TabView) view).SkinIsChanged();
                    }
                }
            }
            invalidateTextureEventList.Call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        GL.that.RestartRender();
    }

    public void mToolsButtonOnLeftTabPerformClick() {
        mToolsButtonOnLeftTab.performClick();
    }

    public void filterSetChanged() {
        // change the icon
        isFiltered = FilterInstances.isLastFilterSet();
        mCacheListButtonOnLeftTab.isFiltered(isFiltered);

        if (!Config.useDescriptiveCB_Buttons.getValue()) {
            if (isFiltered) {
                mCacheListButtonOnLeftTab.setButtonSprites(Sprites.CacheListFilter);
            } else {
                mCacheListButtonOnLeftTab.setButtonSprites(Sprites.CacheList);
            }
        }

        // ##################################
        // Set new list size at context menu
        // ##################################
        String Name;

        synchronized (Database.Data.cacheList) {
            int filterCount = Database.Data.cacheList.size();

            if (Database.Data.cacheList.GetCacheByGcCode("CBPark") != null)
                --filterCount;

            int DBCount = Database.Data.getCacheCountInDB();
            String strFilterCount = "";
            if (filterCount != DBCount) {
                strFilterCount = filterCount + "/";
            }

            Name = "  (" + strFilterCount + DBCount + ")";
        }
        CB_Action_ShowCacheList.getInstance().setNameExtension(Name);
    }

    @Override
    public void renderChilds(final Batch batch, ParentInfo parentInfo) {
        if (childs == null)
            return;
        super.renderChilds(batch, parentInfo);
    }

    @Override
    public void PositionChanged() {
        try {
            TrackRecorder.recordPosition();
        } catch (Exception e) {
            Log.err(log, "Core.MainViewBase.PositionChanged()", "TrackRecorder.recordPosition()", e);
            e.printStackTrace();
        }

        if (GlobalCore.isSetSelectedCache()) {
            float distance = GlobalCore.getSelectedCache().Distance(CalculationType.FAST, false);
            if (GlobalCore.getSelectedWaypoint() != null) {
                distance = GlobalCore.getSelectedWaypoint().Distance();
            }

            if (Config.switchViewApproach.getValue() && !GlobalCore.switchToCompassCompleted && (distance < Config.SoundApproachDistance.getValue())) {
                if (CompassView.getInstance().isVisible())
                    return;// don't show if showing compass
                if (CB_Action_ShowMap.getInstance().normalMapView.isVisible() && CB_Action_ShowMap.getInstance().normalMapView.isCarMode())
                    return; // don't show on visible map at carMode
                CB_Action_ShowCompassView.getInstance().Execute();
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
    public void OrientationChanged() {
    }

    @Override
    public void SpeedChanged() {
    }

    public boolean isInitial() {
        return isInitial;
    }

}
