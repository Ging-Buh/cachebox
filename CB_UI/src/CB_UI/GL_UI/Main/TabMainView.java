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
import CB_UI.GL_UI.Views.*;
import CB_UI.GL_UI.Views.MapView.MapMode;
import CB_UI.GL_UI.Views.TestViews.TestView;
import CB_UI_Base.Energy;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Main.*;
import CB_UI_Base.GL_UI.Main.CB_ActionButton.GestureDirection;
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
import CB_Utils.Util.IChanged;
import CB_Utils.Util.UnitFormatter;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.Timer;
import java.util.TimerTask;

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

    public static CB_Action_ShowCacheList actionShowCacheList;
    public static CB_Action_ShowTrackableListView actionShowTrackableListView;
    public static CB_Action_ShowTrackListView actionShowTrackListView;

    public static CB_Action_ShowDescriptionView actionShowDescriptionView;

    public static CB_Action_ShowView actionShowMap;
    public static CB_Action_ShowView actionShowCompassView;
    public static CB_Action_ShowView actionNavigateTo1;

    public static CB_Action_ShowView actionShowFieldNotesView;
    public static CB_Action_ShowView actionRecVoice;
    public static CB_Action_ShowView actionRecPicture;
    public static CB_Action_ShowView actionRecVideo;
    public static CB_Action_ShowView actionShowSolverView;
    public static CB_Action_ShowView actionShowSolverView2;

    public static CB_Action_ShowAbout actionShowAboutView;
    public static CB_Action_ShowView actionShowCreditsView;

    public static CB_Action_ShowWaypointView actionShowWaypointView;
    public static CB_Action_ShowView actionShowLogView;
    public static CB_Action_ShowView actionShowSpoilerView;
    public static CB_Action_ShowView actionShowNotesView;

    public static CB_Action_ShowView actionTestView;

    public static CB_Action actionShowImportMenu;
    public static CB_Action_ShowFilterSettings actionShowFilter;
    public static CB_Action actionShowSettings;
    public static CB_Action actionShowSelectDbDialog;
    public static CB_Action actionShowDescExt;
    // public static CB_Action actionGenerateRoute;
    public static CB_Action actionQuickFieldNote;
    public static CB_Action actionShowSearch;
    public static CB_Action action_add_WP;
    public static CB_Action actionShowParking;
    public static CB_Action actionShwDeleteCaches;
    public static CB_Action action_SwitchAutoResort;
    public static CB_Action_ShowHint actionShowHint;
    public static CB_Action action_DayNight;
    public static CB_Action_RecTrack actionRecTrack;
    public static CB_Action actionDayNight;
    public static CB_Action_GetFriends actionGetFriends;
    public static CB_Action_switch_Torch action_Torch;
    public static CB_Action actionHelp;

    public static MapView mapView = null;
    public static CacheListView cacheListView = null;
    public static AboutView aboutView = null;
    public static CompassView compassView = null;
    public static CreditsView creditsView = null;
    public static DescriptionView descriptionView = null;
    public static FieldNotesView fieldNotesView = null;
    public static NotesView notesView = null;
    public static SolverView solverView = null;
    public static SpoilerView spoilerView = null;
    public static TrackListView trackListView = null;
    public static WaypointView waypointView = null;
    public static TestView testView = null;
    public static SolverView2 solverView2 = null;
    private static boolean TrackRecIsRegisted = false;
    private final API_ErrorEventHandler handler = new API_ErrorEventHandler() {

        @Override
        public void InvalidAPI_Key() {

            Timer t = new Timer();
            TimerTask tt = new TimerTask() {

                @Override
                public void run() {
                    String Msg = Translation.Get("apiKeyInvalid") + GlobalCore.br + GlobalCore.br;
                    Msg += Translation.Get("wantApi");

                    GL_MsgBox.Show(Msg, Translation.Get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, new OnMsgBoxClickListener() {

                        @Override
                        public boolean onClick(int which, Object data) {
                            if (which == GL_MsgBox.BUTTON_POSITIVE)
                                PlatformConnector.callGetApiKey();
                            return true;
                        }
                    });
                }
            };
            t.schedule(tt, 1500);
        }

        @Override
        public void ExpiredAPI_Key() {
            Timer t = new Timer();
            TimerTask tt = new TimerTask() {

                @Override
                public void run() {
                    String Msg = Translation.Get("apiKeyExpired") + GlobalCore.br + GlobalCore.br;
                    Msg += Translation.Get("wantApi");

                    GL_MsgBox.Show(Msg, Translation.Get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, new OnMsgBoxClickListener() {

                        @Override
                        public boolean onClick(int which, Object data) {
                            if (which == GL_MsgBox.BUTTON_POSITIVE)
                                PlatformConnector.callGetApiKey();
                            return true;
                        }
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
                    String Msg = Translation.Get("apiKeyNeeded") + GlobalCore.br + GlobalCore.br;
                    Msg += Translation.Get("wantApi");

                    GL_MsgBox.Show(Msg, Translation.Get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, new OnMsgBoxClickListener() {

                        @Override
                        public boolean onClick(int which, Object data) {
                            if (which == GL_MsgBox.BUTTON_POSITIVE)
                                PlatformConnector.callGetApiKey();
                            return true;
                        }
                    }, Config.RememberAsk_Get_API_Key);
                }
            };
            t.schedule(tt, 1500);
        }
    };
    private CB_Button mCacheListButtonOnLeftTab; // default: show CacheList
    private CB_Button mDescriptionButtonOnLeftTab; // default: show CacheDecription on Phone and Waypoints on Tablet
    private CB_Button mMapButtonOnLeftTab; // default: show map on phone and show Compass on Tablet
    private CB_Button mToolsButtonOnLeftTab; // default: show ToolsMenu or Fieldnotes (depends on config)
    private CB_Button mAboutButtonOnLeftTab; // default: show About View

    private boolean isInitial = false;
    private boolean isFiltered = false;

    public TabMainView(CB_RectF rec) {
        super(rec);
        if (!TrackRecIsRegisted)
            PositionChangedEventList.Add(this);
        TrackRecIsRegisted = true;
        that = this;

        Timer releaseTimer = new Timer();
        TimerTask releaseTask = new TimerTask() {
            @Override
            public void run() {
                releaseNonvisibleViews();
            }
        };
        releaseTimer.scheduleAtFixedRate(releaseTask, 5000, 5000);
    }

    public static void reloadCacheList() {
        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
        synchronized (Database.Data.Query) {
            CacheListDAO cacheListDAO = new CacheListDAO();
            cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
            cacheListDAO = null;
        }
        CacheListChangedEventList.Call();
    }

    /**
     * release all non visible Views
     */
    private void releaseNonvisibleViews() {
        if (cacheListView != null && !cacheListView.isVisible()) {
            //Log.debug(log, "Release CachelistView");
            cacheListView.dispose();
            cacheListView = null;
        }

        if (aboutView != null && !aboutView.isVisible()) {
            //Log.debug(log, "Release aboutView");
            aboutView.dispose();
            aboutView = null;
        }

        if (compassView != null && !compassView.isVisible()) {
            //Log.debug(log, "Release compassView");
            compassView.dispose();
            compassView = null;
        }

        if (fieldNotesView != null && !fieldNotesView.isVisible()) {
            //Log.debug(log, "Release fieldNotesView");
            fieldNotesView.dispose();
            fieldNotesView = null;
        }

        if (LogView.that != null && !LogView.that.isVisible()) {
            boolean doRelease = true;
            if (LogView.that.getCache() != null)
                if (GlobalCore.isSetSelectedCache())
                    if (LogView.that.getCache().equals(GlobalCore.getSelectedCache()))
                        doRelease = false;
            if (doRelease) {
                //Log.debug(log, "Release logView");
                LogView.that.dispose();
                LogView.that = null;
            }
        }

        if (waypointView != null && !waypointView.isVisible()) {
            //Log.debug(log, "Release waypointView");
            waypointView.dispose();
            waypointView = null;
        }

        if (solverView2 != null && !solverView2.isVisible()) {
            //Log.debug(log, "Release solverView2");
            solverView2.dispose();
            solverView2 = null;
        }
    }

    @Override
    protected void Initial() {
        Log.debug(log, "Start TabMainView-Initial");

        GlobalCore.receiver = new CB_UI.GlobalLocationReceiver();

        UnitFormatter.setUseImperialUnits(Config.ImperialUnits.getValue());
        Config.ImperialUnits.addSettingChangedListener(new IChanged() {
            @Override
            public void handleChange() {
                UnitFormatter.setUseImperialUnits(Config.ImperialUnits.getValue());
            }
        });

        Config.ShowAllWaypoints.addSettingChangedListener(new IChanged() {
            @Override
            public void handleChange() {
                reloadCacheList();
                // must reload MapViewCacheList: do this over MapView.INITIAL_WP_LIST
                if (MapView.that != null)
                    MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);
            }
        });

        CoreSettingsForward.VersionString = GlobalCore.getInstance().getVersionString();
        CoreSettingsForward.DisplayOff = Energy.DisplayOff();
        Energy.addChangedEventListener(new IChanged() {
            @Override
            public void handleChange() {
                CoreSettingsForward.VersionString = GlobalCore.getInstance().getVersionString();
                CoreSettingsForward.DisplayOff = Energy.DisplayOff();
            }
        });

        API_ErrorEventHandlerList.addHandler(handler);

        actionShowAboutView = new CB_Action_ShowAbout();

        // CacheList alternatives + context
        actionShowCacheList = new CB_Action_ShowCacheList();
        action_SwitchAutoResort = new CB_Action_switch_Autoresort();
        actionShowFilter = new CB_Action_ShowFilterSettings();
        actionShowSelectDbDialog = new CB_Action_Show_SelectDB_Dialog();
        actionShowSearch = new CB_Action_Show_Search();
        actionShowImportMenu = new CB_Action_ShowImportMenu();
        actionShowParking = new CB_Action_Show_Parking_Dialog();
        actionShwDeleteCaches = new CB_Action_Show_Delete_Dialog();
        actionShowTrackableListView = new CB_Action_ShowTrackableListView();
        actionShowTrackListView = new CB_Action_ShowTrackListView();

        // CacheDescription alternatives + context
        actionShowDescriptionView = new CB_Action_ShowDescriptionView();
        actionShowWaypointView = new CB_Action_ShowWaypointView();
        actionShowLogView = new CB_Action_ShowLogView();
        actionShowSpoilerView = new CB_Action_ShowSpoilerView();
        actionShowHint = new CB_Action_ShowHint();
        actionShowNotesView = new CB_Action_ShowNotesView();
        actionShowDescExt = new CB_Action_ShowDescExt();
        action_add_WP = new CB_Action_add_WP();

        // map alternatives + context
        actionShowMap = new CB_Action_ShowMap();
        actionShowCompassView = new CB_Action_ShowCompassView();
        actionNavigateTo1 = new CB_Action_ShowActivity("NavigateTo", MenuID.AID_NAVIGATE_TO, ViewConst.NAVIGATE_TO, Sprites.getSprite(IconName.navigate.name()));
        if (GlobalCore.isTestVersion())
            actionTestView = new CB_Action_ShowTestView();

        // fieldnotesalternatives + context
        actionShowFieldNotesView = new CB_Action_ShowFieldNotesView();
        actionRecTrack = new CB_Action_RecTrack();
        actionRecVoice = new CB_Action_ShowActivity("VoiceRec", MenuID.AID_VOICE_REC, ViewConst.VOICE_REC, Sprites.getSprite(IconName.voiceRecIcon.name()));
        actionRecPicture = new CB_Action_ShowActivity("TakePhoto", MenuID.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO, Sprites.getSprite(IconName.log10icon.name()));
        actionRecVideo = new CB_Action_ShowActivity("RecVideo", MenuID.AID_VIDEO_REC, ViewConst.VIDEO_REC, Sprites.getSprite(IconName.videoIcon.name()));
        actionShowSolverView = new CB_Action_ShowSolverView();
        actionShowSolverView2 = new CB_Action_ShowSolverView2();

        // context of about
        actionShowCreditsView = new CB_Action_ShowCreditsView();
        actionShowSettings = new CB_Action_Show_Settings();
        actionDayNight = new CB_Action_switch_DayNight();
        actionHelp = new CB_Action_Help();
        action_DayNight = new CB_Action_switch_DayNight();


        // others QuickButtons
        actionQuickFieldNote = new CB_Action_QuickFieldNote();
        // actionGenerateRoute = new CB_Action_GenerateRoute();
        // actionScreenLock = new CB_Action_ShowActivity("screenlock", MenuID.AID_LOCK, ViewConst.LOCK, SpriteCache.Icons.get(14));

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
            synchronized (Database.Data.Query) {
                for (int i = 0, n = Database.Data.Query.size(); i < n; i++) {
                    Cache c = Database.Data.Query.get(i);
                    if (c.getGcCode().equalsIgnoreCase(sGc)) {
                        Log.debug(log, "TabMainView: Set selectedCache to " + c.getGcCode() + " from lastSaved.");
                        GlobalCore.setSelectedCache(c); // !! sets GlobalCore.setAutoResort to false
                        break;
                    }
                }
            }
        }

        GlobalCore.setAutoResort(Config.StartWithAutoSelect.getValue());

        // create MapView Instanz
        CB_TabView mapTap = leftTab;
        TabMainView.mapView = new MapView(mapTap.getContentRec(), MapMode.Normal, "MapView");
        MapView.that.SetZoom(Config.lastZoomLevel.getValue());

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

        CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);
        if (Config.useDescriptiveCB_Buttons.getValue()) {
            mCacheListButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "CacheList");
            mDescriptionButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "Cache");
            mMapButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "Nav");
            mToolsButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "Tool");
            mAboutButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "Misc");
        } else {
            mCacheListButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "CacheList", Sprites.CacheList);
            mDescriptionButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "Cache", Sprites.Cache);
            mMapButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "Nav", Sprites.Nav);
            mToolsButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "Tool", Sprites.Tool);
            mAboutButtonOnLeftTab = new CB_Button(btnRec, Config.rememberLastAction.getValue(), "Misc", Sprites.Misc);
        }

        CB_ButtonList btnList = new CB_ButtonList();
        btnList.addButton(mCacheListButtonOnLeftTab);
        btnList.addButton(mDescriptionButtonOnLeftTab);
        btnList.addButton(mMapButtonOnLeftTab);
        btnList.addButton(mToolsButtonOnLeftTab);
        btnList.addButton(mAboutButtonOnLeftTab);
        leftTab.setButtonList(btnList);
        addChild(leftTab);

        actionShowCacheList.setTab(this, leftTab);
        // actionShowTrackableListView.setTab(this, leftTab);
        actionShowTrackListView.setTab(this, leftTab);
        actionShowDescriptionView.setTab(this, leftTab);
        actionShowWaypointView.setTab(this, leftTab);
        actionShowLogView.setTab(this, leftTab);
        actionShowNotesView.setTab(this, leftTab);
        actionShowSpoilerView.setTab(this, leftTab);
        actionShowMap.setTab(this, leftTab);
        actionShowCompassView.setTab(this, leftTab);
        actionNavigateTo1.setTab(this, leftTab);
        if (GlobalCore.isTestVersion())
            actionTestView.setTab(this, leftTab);
        actionShowFieldNotesView.setTab(this, leftTab);
        actionRecPicture.setTab(this, leftTab);
        actionRecVideo.setTab(this, leftTab);
        actionRecVoice.setTab(this, leftTab);
        actionShowSolverView.setTab(this, leftTab);
        actionShowSolverView2.setTab(this, leftTab);
        actionShowAboutView.setTab(this, leftTab);
        actionShowCreditsView.setTab(this, leftTab);

        // Actions den Buttons zuweisen
        mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowCacheList, true, GestureDirection.Up));
        mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowParking, false));
        mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowTrackableListView, false, GestureDirection.Right));
        //mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowTrackListView, false, GestureDirection.Down));

        mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowDescriptionView, true, GestureDirection.Up));
        mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowWaypointView, false, GestureDirection.Right));
        mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowHint, false));
        mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSpoilerView, false));
        mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowLogView, false, GestureDirection.Down));
        mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowNotesView, false));
        mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowTrackableListView, false));
        mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowDescExt, false));

        mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionShowMap, true, GestureDirection.Up));
        mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionShowCompassView, false, GestureDirection.Right));
        mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionNavigateTo1, false, GestureDirection.Down));
        mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionShowTrackListView, false, GestureDirection.Left));
        // mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionGenerateRoute, false, GestureDirection.Left));
        if (GlobalCore.isTestVersion())
            mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionTestView, false));

        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowFieldNotesView, Config.ShowFieldnotesAsDefaultView.getValue()));
        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowTrackableListView, false));
        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));
        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView2, false, GestureDirection.Right));
        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecPicture, false, GestureDirection.Down));
        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVoice, false));
        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVideo, false));
        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowParking, false));
        // mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecTrack, false));

        mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionShowAboutView, true, GestureDirection.Up));
        mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionShowCreditsView, false));
        mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSettings, false, GestureDirection.Left));
        mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionDayNight, false));
        mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionHelp, false));
        mAboutButtonOnLeftTab.addAction((new CB_ActionButton(actionGetFriends = new CB_Action_GetFriends(), false)));
        mAboutButtonOnLeftTab.addAction((new CB_ActionButton(action_Torch = new CB_Action_switch_Torch(), false)));
        mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionClose, false, GestureDirection.Down));

        actionShowAboutView.Execute();
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
            File sddir = FileFactory.createFile(trackPath);
            sddir.mkdirs();
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
            mapView.invalidateTexture();
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

    public boolean isFiltered() {
        return isFiltered;
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
        String Name = "";

        synchronized (Database.Data.Query) {
            int filterCount = Database.Data.Query.size();

            if (Database.Data.Query.GetCacheByGcCode("CBPark") != null)
                --filterCount;

            int DBCount = Database.Data.getCacheCountInDB();
            String strFilterCount = "";
            if (filterCount != DBCount) {
                strFilterCount = String.valueOf(filterCount) + "/";
            }

            Name = "  (" + strFilterCount + String.valueOf(DBCount) + ")";
        }
        actionShowCacheList.setNameExtension(Name);
    }

    public void showCacheList() {
        actionShowCacheList.Execute();
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
                if (compassView != null && compassView.isVisible())
                    return;// don't show if showing compass
                if (mapView != null && mapView.isVisible() && mapView.isCarMode())
                    return; // don't show on visible map at carMode
                actionShowCompassView.Execute();
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
