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

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.Batch;

import CB_Core.CacheListChangedEventList;
import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Api.API_ErrorEventHandler;
import CB_Core.Api.API_ErrorEventHandlerList;
import CB_Core.Api.API_ErrorEventHandlerList.API_ERROR;
import CB_Core.DAO.CacheListDAO;
import CB_Core.Types.Cache;
import CB_Locator.LocatorSettings;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.AppRater;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.RouteOverlay;
import CB_UI.TrackRecorder;
import CB_UI.GL_UI.Controls.Slider;
import CB_UI.GL_UI.Main.Actions.CB_Action_GenerateRoute;
import CB_UI.GL_UI.Main.Actions.CB_Action_Help;
import CB_UI.GL_UI.Main.Actions.CB_Action_QuickFieldNote;
import CB_UI.GL_UI.Main.Actions.CB_Action_RecTrack;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowAbout;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowActivity;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowCacheList;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowCompassView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowCreditsView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowDescExt;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowDescriptionView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowFieldNotesView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowFilterSettings;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowHint;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowImportMenu;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowJokerView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowLogView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowNotesView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowSolverView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowSolverView2;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowSpoilerView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowTestView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowTrackListView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowTrackableListView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowWaypointView;
import CB_UI.GL_UI.Main.Actions.CB_Action_Show_Delete_Dialog;
import CB_UI.GL_UI.Main.Actions.CB_Action_Show_Parking_Dialog;
import CB_UI.GL_UI.Main.Actions.CB_Action_Show_SelectDB_Dialog;
import CB_UI.GL_UI.Main.Actions.CB_Action_Show_Settings;
import CB_UI.GL_UI.Main.Actions.CB_Action_switch_DayNight;
import CB_UI.GL_UI.Views.AboutView;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI.GL_UI.Views.CompassView;
import CB_UI.GL_UI.Views.CreditsView;
import CB_UI.GL_UI.Views.DescriptionView;
import CB_UI.GL_UI.Views.FieldNotesView;
import CB_UI.GL_UI.Views.JokerView;
import CB_UI.GL_UI.Views.LogView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.MapView.MapMode;
import CB_UI.GL_UI.Views.NotesView;
import CB_UI.GL_UI.Views.SolverView;
import CB_UI.GL_UI.Views.SolverView2;
import CB_UI.GL_UI.Views.SpoilerView;
import CB_UI.GL_UI.Views.TrackListView;
import CB_UI.GL_UI.Views.TrackableListView;
import CB_UI.GL_UI.Views.WaypointView;
import CB_UI.GL_UI.Views.TestViews.TestView;
import CB_UI_Base.Energy;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.CB_ActionButton;
import CB_UI_Base.GL_UI.Main.CB_ActionButton.GestureDirection;
import CB_UI_Base.GL_UI.Main.CB_Button;
import CB_UI_Base.GL_UI.Main.CB_ButtonList;
import CB_UI_Base.GL_UI.Main.CB_TabView;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.graphics.GL_RenderType;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Log.Log;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.IChanged;
import CB_Utils.Util.UnitFormatter;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

/**
 * the TabMainView has one tab (leftTab) on the phone<br>
 * and two tabs (leftTab , rightTab) on the tablet.<br>
 * Each tab has buttons (5/3) at the bottom for selecting the different actions to do.<br>
 * @author ging-buh
 * @author Longri
 */
public class TabMainView extends MainViewBase implements PositionChangedEvent {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(TabMainView.class);
	public static TabMainView that;

	public static CB_TabView leftTab;
	public static CB_TabView rightTab;

	CB_Button mCacheListButtonOnLeftTab; // default: show CacheList
	CB_Button mDescriptionButtonOnLeftTab; // default: show CacheDecription on Phone and Waypoints on Tablet
	CB_Button mMapButtonOnLeftTab; // default: show map on phone and show Compass on Tablet
	CB_Button mToolsButtonOnLeftTab; // default: show ToolsMenu or Fieldnotes (depends on config)
	CB_Button mAboutButtonOnLeftTab; // default: show About View

	CB_Button mDescriptionButtonOnRightTab; // default: show CacheDecription
	CB_Button mMapButtonOnRightTab; // default: show map
	CB_Button mToolsButtonOnRightTab; // default: show SolverView

	private static boolean TrackRecIsRegisted = false;
	public static CB_Action_ShowCompassView actionShowCompassView;

	private CB_Action_ShowTestView actionTestView;
	private CB_Action_ShowHint actionShowHint;
	public static CB_Action_ShowMap actionShowMap;
	public static CB_Action_ShowCacheList actionShowCacheList = new CB_Action_ShowCacheList();

	private CB_Action_ShowAbout actionShowAboutView;
	private CB_Action_ShowCreditsView actionShowCreditsView;
	public static CB_Action_ShowImportMenu actionShowImportMenu = new CB_Action_ShowImportMenu();
	public static CB_Action_ShowDescriptionView actionShowDescriptionView;
	public static CB_Action_ShowFieldNotesView actionShowFieldNotesView;
	public static CB_Action_ShowJokerView actionShowJokerView;
	public static CB_Action_ShowLogView actionShowLogView;
	public static CB_Action_ShowNotesView actionShowNotesView;
	public static CB_Action_ShowSolverView actionShowSolverView;
	public static CB_Action_ShowSolverView2 actionShowSolverView2;
	public static CB_Action_ShowSpoilerView actionShowSpoilerView;
	public static CB_Action_ShowFilterSettings actionShowFilter = new CB_Action_ShowFilterSettings();
	public static CB_Action_ShowTrackableListView actionShowTrackableListView;
	public static CB_Action_ShowTrackListView actionShowTrackListView;
	public static CB_Action_ShowWaypointView actionShowWaypointView;
	public static CB_Action_Show_Settings actionShowSettings;
	private CB_Action_ShowActivity actionNavigateTo1;
	private CB_Action_ShowActivity actionNavigateTo2;
	public static CB_Action_Show_SelectDB_Dialog actionShowSelectDbDialog = new CB_Action_Show_SelectDB_Dialog();
	public static CB_Action_ShowDescExt actionShowDescExt = new CB_Action_ShowDescExt();

	public static CB_Action_GenerateRoute actionGenerateRoute = new CB_Action_GenerateRoute();
	public static CB_Action_QuickFieldNote actionQuickFieldNote = new CB_Action_QuickFieldNote();
	public static CB_Action_Show_Parking_Dialog actionParking = new CB_Action_Show_Parking_Dialog();
	public static CB_Action_Show_Delete_Dialog actionDelCaches = new CB_Action_Show_Delete_Dialog();

	public static CB_Action_RecTrack actionRecTrack;
	private CB_Action_ShowActivity actionRecVoice;
	private CB_Action_ShowActivity actionRecPicture;
	private CB_Action_ShowActivity actionRecVideo;

	private CB_Action_switch_DayNight actionDayNight;
	private CB_Action_Help actionHelp;

	public static MapView mapView = null;
	public static CacheListView cacheListView = null;
	public static AboutView aboutView = null;
	public static CompassView compassView = null;
	public static CreditsView creditsView = null;
	public static DescriptionView descriptionView = null;
	public static FieldNotesView fieldNotesView = null;
	public static JokerView jokerView = null;
	public static LogView logView = null;
	public static NotesView notesView = null;
	public static SolverView solverView = null;
	public static SpoilerView spoilerView = null;
	public static TrackableListView trackableListView = null;
	public static TrackListView trackListView = null;
	public static WaypointView waypointView = null;
	public static TestView testView = null;
	public static SolverView2 solverView2 = null;

	public TabMainView(float X, float Y, float Width, float Height, String Name) {
		super(X, Y, Width, Height, Name);
		if (!TrackRecIsRegisted)
			PositionChangedEventList.Add(this);
		TrackRecIsRegisted = true;
		that = (TabMainView) (mainView = this);

		Timer releaseTimer = new Timer();
		TimerTask releaseTask = new TimerTask() {
			@Override
			public void run() {
				releaseNonvisibleViews();
			}
		};
		releaseTimer.scheduleAtFixedRate(releaseTask, 5000, 5000);
		
		
		
	
			LocatorSettings.MapsforgeRenderType.setEnumValue(GL_RenderType.Mapsforge);
			// Set setting to invisible
			LocatorSettings.MapsforgeRenderType.changeSettingsModus(SettingModus.Never);
			Config.settings.WriteToDB();
			Log.debug(log, "disable MixedDatabaseRenderer for Android Version ");


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

		if (logView != null && !logView.isVisible()) {
			boolean doRelease = true;
			if (logView.getCache() != null)
				if (GlobalCore.isSetSelectedCache())
					if (logView.getCache().equals(GlobalCore.getSelectedCache()))
						doRelease = false;
			if (doRelease) {
				//Log.debug(log, "Release logView");
				logView.dispose();
				logView = null;
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
		GlobalCore.receiver = new CB_UI.GlobalLocationReceiver();

		initialSettingsChangedListener();
		ini();
		isInitial = true;

		// Settings changed handling for forward to core

		IChanged settingChangedHandler = new IChanged() {
			@Override
			public void isChanged() {
				CoreSettingsForward.VersionString = GlobalCore.getVersionString();
				CoreSettingsForward.DisplayOff = Energy.DisplayOff();
			}
		};

		// first fill
		settingChangedHandler.isChanged();

		// add changed handler
		Energy.addChangedEventListener(settingChangedHandler);

		// change CBS settings Type from Develop to normal with TestVersion
		if (GlobalCore.isTestVersion()) {
			Config.CBS_IP.changeSettingsModus(SettingModus.Normal);
		}

	}

	private boolean isInitial = false;

	private void initialSettingsChangedListener() {
		Config.ImperialUnits.addChangedEventListener(new IChanged() {
			@Override
			public void isChanged() {
				UnitFormatter.setUseImperialUnits(Config.ImperialUnits.getValue());
			}
		});
		addSettingChangedListener(Config.ShowAllWaypoints);
		// Set settings first
		UnitFormatter.setUseImperialUnits(Config.ImperialUnits.getValue());
	}

	private void addSettingChangedListener(SettingBase<?> setting) {
		setting.addChangedEventListener(new IChanged() {
			@Override
			public void isChanged() {
				reloadCacheList();
				// must reload MapViewCacheList
				// do this over Initial WPI-List
				if (MapView.that != null)
					MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);
			}
		});
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

	private void ini() {

		API_ErrorEventHandlerList.addHandler(handler);

		Log.debug(log, "Start TabMainView-Initial");

		actionShowMap = new CB_Action_ShowMap();
		actionShowHint = new CB_Action_ShowHint();

		actionShowAboutView = new CB_Action_ShowAbout();
		actionShowCompassView = new CB_Action_ShowCompassView();
		actionShowCreditsView = new CB_Action_ShowCreditsView();
		actionShowDescriptionView = new CB_Action_ShowDescriptionView();
		actionShowFieldNotesView = new CB_Action_ShowFieldNotesView();
		actionShowJokerView = new CB_Action_ShowJokerView();
		actionShowLogView = new CB_Action_ShowLogView();
		actionShowNotesView = new CB_Action_ShowNotesView();
		actionShowSolverView = new CB_Action_ShowSolverView();
		actionShowSolverView2 = new CB_Action_ShowSolverView2();
		actionShowSpoilerView = new CB_Action_ShowSpoilerView();
		actionShowTrackableListView = new CB_Action_ShowTrackableListView();
		actionShowTrackListView = new CB_Action_ShowTrackListView();
		actionShowWaypointView = new CB_Action_ShowWaypointView();
		if (GlobalCore.isTestVersion())
			actionTestView = new CB_Action_ShowTestView();
		actionShowSettings = new CB_Action_Show_Settings();

		actionNavigateTo1 = actionNavigateTo2 = new CB_Action_ShowActivity("NavigateTo", MenuID.AID_NAVIGATE_TO, ViewConst.NAVIGATE_TO, Sprites.getSprite(IconName.navigate.name()));

		actionRecTrack = new CB_Action_RecTrack();
		actionRecVoice = new CB_Action_ShowActivity("VoiceRec", MenuID.AID_VOICE_REC, ViewConst.VOICE_REC, Sprites.getSprite(IconName.voiceRecIcon.name()));
		actionRecPicture = new CB_Action_ShowActivity("TakePhoto", MenuID.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO, Sprites.getSprite(IconName.log10icon.name()));
		actionRecVideo = new CB_Action_ShowActivity("RecVideo", MenuID.AID_VIDEO_REC, ViewConst.VIDEO_REC, Sprites.getSprite(IconName.videoIcon.name()));

		actionDayNight = new CB_Action_switch_DayNight();
		actionHelp = new CB_Action_Help();
		// actionScreenLock = new CB_Action_ShowActivity("screenlock", MenuID.AID_LOCK, ViewConst.LOCK, SpriteCache.Icons.get(14));

		if (GlobalCore.isTab)
			addTabletTabs();
		else
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
		if (sGc != null && !sGc.equals("")) {
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
		CB_TabView mapTap = GlobalCore.isTab ? rightTab : leftTab;
		TabMainView.mapView = new MapView(mapTap.getContentRec(), MapMode.Normal, "MapView");
		MapView.that.SetZoom(Config.lastZoomLevel.getValue());

		PlatformConnector.FirstShow();
		filterSetChanged();
		GL.that.removeRenderView(this);

		AppRater.app_launched();

		if (Config.GcAPI.getValue().equals(""))
			API_ErrorEventHandlerList.callInvalidApiKey(API_ERROR.NO);

	}

	private void addPhoneTab() {
		// nur ein Tab

		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.getHeight() - UiSizes.that.getInfoSliderHeight());
		rec.setPos(0, 0);

		leftTab = new CB_TabView(rec, "leftTab");

		mCacheListButtonOnLeftTab = new CB_Button(btnRec, "Button1", Sprites.CacheList);
		mDescriptionButtonOnLeftTab = new CB_Button(btnRec, "Button2", Sprites.Cache);
		mMapButtonOnLeftTab = new CB_Button(btnRec, "Button3", Sprites.Nav);
		mToolsButtonOnLeftTab = new CB_Button(btnRec, "Button4", Sprites.Tool);
		mAboutButtonOnLeftTab = new CB_Button(btnRec, "Button5", Sprites.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(mCacheListButtonOnLeftTab);
		btnList.addButton(mDescriptionButtonOnLeftTab);
		btnList.addButton(mMapButtonOnLeftTab);
		btnList.addButton(mToolsButtonOnLeftTab);
		btnList.addButton(mAboutButtonOnLeftTab);
		leftTab.setButtonList(btnList);

		this.addChild(leftTab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, leftTab);
		actionShowCacheList.setTab(this, leftTab);

		actionShowAboutView.setTab(this, leftTab);
		actionShowCompassView.setTab(this, leftTab);
		actionShowCreditsView.setTab(this, leftTab);
		actionShowDescriptionView.setTab(this, leftTab);
		actionShowFieldNotesView.setTab(this, leftTab);
		actionShowJokerView.setTab(this, leftTab);
		actionShowLogView.setTab(this, leftTab);
		actionShowNotesView.setTab(this, leftTab);
		actionShowSolverView.setTab(this, leftTab);
		actionShowSolverView2.setTab(this, leftTab);
		actionShowSpoilerView.setTab(this, leftTab);
		actionShowTrackableListView.setTab(this, leftTab);
		actionShowTrackListView.setTab(this, leftTab);
		actionShowWaypointView.setTab(this, leftTab);
		actionNavigateTo1.setTab(this, leftTab);

		actionRecVoice.setTab(this, leftTab);
		actionRecPicture.setTab(this, leftTab);
		actionRecVideo.setTab(this, leftTab);
		if (GlobalCore.isTestVersion())
			actionTestView.setTab(this, leftTab);

		// Actions den Buttons zuweisen

		mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowCacheList, true, GestureDirection.Up));
		mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowTrackableListView, false, GestureDirection.Right));
		mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowTrackListView, false, GestureDirection.Down));

		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowDescriptionView, true, GestureDirection.Up));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowWaypointView, false, GestureDirection.Right));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowLogView, false, GestureDirection.Down));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowHint, false));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowDescExt, false));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSpoilerView, false));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowNotesView, false));

		mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionShowMap, true, GestureDirection.Up));
		mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionShowCompassView, false, GestureDirection.Right));
		mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionNavigateTo1, false, GestureDirection.Down));
		mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionGenerateRoute, false, GestureDirection.Left));
		if (GlobalCore.isTestVersion())
			mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionTestView, false));

		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionQuickFieldNote, false, GestureDirection.Up));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowFieldNotesView, Config.ShowFieldnotesAsDefaultView.getValue()));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecTrack, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVoice, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecPicture, false, GestureDirection.Down));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVideo, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionParking, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView2, false, GestureDirection.Right));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowJokerView, false));

		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionShowAboutView, true, GestureDirection.Up));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionShowCreditsView, false));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSettings, false, GestureDirection.Left));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionDayNight, false));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionHelp, false));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionClose, false, GestureDirection.Down));

		actionShowAboutView.Execute();
	}

	private void addTabletTabs() {
		addLeftForTabletsTab();
		addRightForTabletsTab();
	}

	private void addLeftForTabletsTab() {
		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.getHeight() - UiSizes.that.getInfoSliderHeight());
		rec.setPos(0, 0);

		leftTab = new CB_TabView(rec, "leftTab");

		mCacheListButtonOnLeftTab = new CB_Button(btnRec, "Button1", Sprites.CacheList);
		mDescriptionButtonOnLeftTab = new CB_Button(btnRec, "Button2", Sprites.Cache);
		mMapButtonOnLeftTab = new CB_Button(btnRec, "Button3", Sprites.Nav);
		mToolsButtonOnLeftTab = new CB_Button(btnRec, "Button4", Sprites.Tool);
		mAboutButtonOnLeftTab = new CB_Button(btnRec, "Button5", Sprites.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(mCacheListButtonOnLeftTab);
		btnList.addButton(mDescriptionButtonOnLeftTab);
		btnList.addButton(mMapButtonOnLeftTab);
		btnList.addButton(mToolsButtonOnLeftTab);
		btnList.addButton(mAboutButtonOnLeftTab);
		leftTab.setButtonList(btnList);

		this.addChild(leftTab);

		// Tab den entsprechneden Actions zuweisen
		actionShowCacheList.setTab(this, leftTab);
		actionShowWaypointView.setTab(this, leftTab);
		actionShowAboutView.setTab(this, leftTab);
		actionShowCreditsView.setTab(this, leftTab);
		actionShowTrackableListView.setTab(this, leftTab);
		actionShowTrackListView.setTab(this, leftTab);
		actionShowCompassView.setTab(this, leftTab);
		actionShowLogView.setTab(this, leftTab);
		actionShowFieldNotesView.setTab(this, leftTab);
		actionShowJokerView.setTab(this, leftTab);
		actionShowNotesView.setTab(this, leftTab);
		actionNavigateTo1.setTab(this, leftTab);

		actionRecVoice.setTab(this, leftTab);
		actionRecPicture.setTab(this, leftTab);
		actionRecVideo.setTab(this, leftTab);

		// Actions den Buttons zuweisen
		mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowCacheList, true));
		mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowTrackableListView, false));
		mCacheListButtonOnLeftTab.addAction(new CB_ActionButton(actionShowTrackListView, false));

		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowWaypointView, true, GestureDirection.Right));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowLogView, false, GestureDirection.Down));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowHint, false));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowDescExt, false));
		mDescriptionButtonOnLeftTab.addAction(new CB_ActionButton(actionShowNotesView, false));

		mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionShowCompassView, true, GestureDirection.Right));
		mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionNavigateTo1, false, GestureDirection.Down));
		mMapButtonOnLeftTab.addAction(new CB_ActionButton(actionGenerateRoute, false, GestureDirection.Left));

		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionQuickFieldNote, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowFieldNotesView, Config.ShowFieldnotesAsDefaultView.getValue()));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecTrack, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVoice, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecPicture, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVideo, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionParking, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView2, false));
		mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowJokerView, false));

		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionShowAboutView, true, GestureDirection.Up));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionShowCreditsView, false));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSettings, false, GestureDirection.Left));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionDayNight, false));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionHelp, false));
		mAboutButtonOnLeftTab.addAction(new CB_ActionButton(actionClose, false));

		actionShowAboutView.Execute();

		// // Rate Timer
		// Timer raTi = new Timer();
		// TimerTask raTa = new TimerTask()
		// {
		// @Override
		// public void run()
		// {
		// AppRater.app_launched(main.this);
		// }
		// };
		//
		// raTi.schedule(raTa, 15000);

	}

	private void addRightForTabletsTab() {

		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Right.getWidth());
		rec.setX(GL_UISizes.UI_Left.getWidth());
		rec.setY(0);

		rec.setHeight(this.getHeight() - UiSizes.that.getInfoSliderHeight());

		rightTab = new CB_TabView(rec, "rightTab");

		mDescriptionButtonOnRightTab = new CB_Button(btnRec, "Button2", Sprites.Cache);
		mMapButtonOnRightTab = new CB_Button(btnRec, "Button3", Sprites.Nav);
		mToolsButtonOnRightTab = new CB_Button(btnRec, "Button4", Sprites.Tool);

		CB_ButtonList btnList = new CB_ButtonList();

		btnList.addButton(mDescriptionButtonOnRightTab);
		btnList.addButton(mMapButtonOnRightTab);
		btnList.addButton(mToolsButtonOnRightTab);

		rightTab.setButtonList(btnList);

		this.addChild(rightTab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, rightTab);
		actionShowSolverView.setTab(this, rightTab);
		actionShowSolverView2.setTab(this, rightTab);
		actionShowDescriptionView.setTab(this, rightTab);
		actionNavigateTo2.setTab(this, rightTab);
		if (GlobalCore.isTestVersion())
			actionTestView.setTab(this, rightTab);
		actionShowSpoilerView.setTab(this, rightTab);

		// Actions den Buttons zuweisen
		mDescriptionButtonOnRightTab.addAction(new CB_ActionButton(actionShowDescriptionView, true));
		mDescriptionButtonOnRightTab.addAction(new CB_ActionButton(actionShowSpoilerView, false));

		mMapButtonOnRightTab.addAction(new CB_ActionButton(actionShowMap, true, GestureDirection.Up));
		mMapButtonOnRightTab.addAction(new CB_ActionButton(actionNavigateTo2, false, GestureDirection.Down));
		if (GlobalCore.isTestVersion())
			mMapButtonOnRightTab.addAction(new CB_ActionButton(actionTestView, false));

		mToolsButtonOnRightTab.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));

		actionShowMap.Execute();
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

		// chk if initial
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

			CB_Button.reloadMenuSprite();
			if (GlobalCore.isTab)
				addTabletTabs();
			else
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

	private boolean isFiltered = false;

	public boolean isFiltered() {
		return isFiltered;
	}

	public void mToolsButtonOnLeftTabPerformClick() {
		mToolsButtonOnLeftTab.performClick();
	}

	public void filterSetChanged() {
		// change the icon
		isFiltered = FilterInstances.isLastFilterSet();
		if (isFiltered) {
			mCacheListButtonOnLeftTab.setButtonSprites(Sprites.CacheListFilter);
		} else {
			mCacheListButtonOnLeftTab.setButtonSprites(Sprites.CacheList);
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
		actionShowCacheList.setNameExtention(Name);
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
								PlatformConnector.callGetApiKeyt();
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
								PlatformConnector.callGetApiKeyt();
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
								PlatformConnector.callGetApiKeyt();
							return true;
						}
					}, Config.RememberAsk_Get_API_Key);
				}
			};
			t.schedule(tt, 1500);
		}
	};

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
