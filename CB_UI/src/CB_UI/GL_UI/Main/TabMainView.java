package CB_UI.GL_UI.Main;

import java.io.File;

import CB_Core.CoreSettingsForward;
import CB_Core.FilterProperties;
import CB_Core.Api.API_ErrorEventHandler;
import CB_Core.Api.API_ErrorEventHandlerList;
import CB_Core.DB.Database;
import CB_Core.Types.CacheLite;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Locator.Map.ManagerBase;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.TrackRecorder;
import CB_UI.GL_UI.Activitys.FilterSettings.PresetListViewItem;
import CB_UI.GL_UI.Controls.Slider;
import CB_UI.GL_UI.Main.Actions.CB_Action_GenerateRoute;
import CB_UI.GL_UI.Main.Actions.CB_Action_QuickFieldNote;
import CB_UI.GL_UI.Main.Actions.CB_Action_RecTrack;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowAbout;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowActivity;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowCacheList;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowCompassView;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowCreditsView;
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
import CB_UI.GL_UI.Views.NotesView;
import CB_UI.GL_UI.Views.SolverView;
import CB_UI.GL_UI.Views.SolverView2;
import CB_UI.GL_UI.Views.SpoilerView;
import CB_UI.GL_UI.Views.TrackListView;
import CB_UI.GL_UI.Views.TrackableListView;
import CB_UI.GL_UI.Views.WaypointView;
import CB_UI.GL_UI.Views.TestViews.TestView;
import CB_UI.Map.RouteOverlay;
import CB_UI_Base.Energy;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
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
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Log.Logger;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.SyncronizeHelper;
import CB_Utils.Util.UnitFormatter;
import CB_Utils.Util.iChanged;

import com.badlogic.gdx.graphics.g2d.Batch;

public class TabMainView extends MainViewBase implements PositionChangedEvent
{
	public static TabMainView that;
	private static boolean TrackRecIsRegisted = false;
	public static CB_Action_ShowCompassView actionShowCompassView;
	public static CB_TabView LeftTab;
	public static CB_TabView RightTab;

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

	public static CB_Action_GenerateRoute actionGenerateRoute = new CB_Action_GenerateRoute();
	public static CB_Action_QuickFieldNote actionQuickFieldNote = new CB_Action_QuickFieldNote();
	public static CB_Action_Show_Parking_Dialog actionParking = new CB_Action_Show_Parking_Dialog();
	public static CB_Action_Show_Delete_Dialog actionDelCaches = new CB_Action_Show_Delete_Dialog();

	public static CB_Action_RecTrack actionRecTrack;
	private CB_Action_ShowActivity actionRecVoice;
	private CB_Action_ShowActivity actionRecPicture;
	private CB_Action_ShowActivity actionRecVideo;

	private CB_Action_switch_DayNight actionDayNight;
	// private CB_Action_ShowActivity actionScreenLock;

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

	public TabMainView(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		if (!TrackRecIsRegisted) PositionChangedEventList.Add(this);
		TrackRecIsRegisted = true;
		that = (TabMainView) (mainView = this);

	}

	@Override
	protected void Initial()
	{
		GlobalCore.receiver = new CB_UI.GlobalLocationReceiver();

		initialSettingsChangedListner();
		ini();
		isInitial = true;

		// Settings changed handling for forward to core

		iChanged settingChangedHandler = new iChanged()
		{
			@Override
			public void isChanged()
			{
				CoreSettingsForward.VersionString = GlobalCore.getVersionString();
				CoreSettingsForward.DisplayOff = Energy.DisplayOff();
			}
		};

		// first fill
		settingChangedHandler.isChanged();

		// add changed handler
		Energy.addChangedEventListner(settingChangedHandler);

		// change CBS settings Type from Develop to normal with TestVersion
		if (GlobalCore.isTestVersion())
		{
			Config.CBS_IP.changeSettingsModus(SettingModus.Normal);
		}

	}

	private boolean isInitial = false;

	private void initialSettingsChangedListner()
	{
		Config.WriteLoggerDebugMode.addChangedEventListner(new iChanged()
		{
			@Override
			public void isChanged()
			{
				Logger.setDebug(Config.WriteLoggerDebugMode.getValue());
			}
		});

		Config.ImperialUnits.addChangedEventListner(new iChanged()
		{

			@Override
			public void isChanged()
			{
				UnitFormatter.setUseImperialUnits(Config.ImperialUnits.getValue());
			}
		});

		// Set settings first
		UnitFormatter.setUseImperialUnits(Config.ImperialUnits.getValue());
		Logger.setDebug(Config.WriteLoggerDebugMode.getValue());
	}

	private void ini()
	{

		API_ErrorEventHandlerList.addHandler(handler);

		Logger.LogCat("Start TabMainView-Initial");
		Logger.DEBUG("Start TabMainView-Initial");

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
		if (GlobalCore.isTestVersion()) actionTestView = new CB_Action_ShowTestView();
		actionShowSettings = new CB_Action_Show_Settings();

		actionNavigateTo1 = actionNavigateTo2 = new CB_Action_ShowActivity("NavigateTo", MenuID.AID_NAVIGATE_TO, ViewConst.NAVIGATE_TO,
				SpriteCacheBase.Icons.get(IconName.navigate_46.ordinal()));

		actionRecTrack = new CB_Action_RecTrack();
		actionRecVoice = new CB_Action_ShowActivity("VoiceRec", MenuID.AID_VOICE_REC, ViewConst.VOICE_REC,
				SpriteCacheBase.Icons.get(IconName.voiceRec_11.ordinal()));
		actionRecPicture = new CB_Action_ShowActivity("TakePhoto", MenuID.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO,
				SpriteCacheBase.Icons.get(IconName.log10_47.ordinal()));
		actionRecVideo = new CB_Action_ShowActivity("RecVideo", MenuID.AID_VIDEO_REC, ViewConst.VIDEO_REC,
				SpriteCacheBase.Icons.get(IconName.video_10.ordinal()));

		actionDayNight = new CB_Action_switch_DayNight();
		// actionScreenLock = new CB_Action_ShowActivity("screenlock", MenuID.AID_LOCK, ViewConst.LOCK, SpriteCache.Icons.get(14));

		if (GlobalCore.isTab) addTabletTabs();
		else
			addPhoneTab();

		// add Slider as last
		Slider slider = new Slider(this, "Slider");
		this.addChild(slider);

		Logger.LogCat("Ende TabMainView-Initial");

		autoLoadTrack();

		if (Config.TrackRecorderStartup.getValue() && platformConector.isGPSon())
		{
			TrackRecorder.StartRecording();
		}

		// set last selected Cache
		String sGc = Config.LastSelectedCache.getValue();
		if (sGc != null && !sGc.equals(""))
		{
			synchronized (Database.Data.Query)
			{
				for (int i = 0, n = Database.Data.Query.size(); i < n; i++)
				{
					CacheLite c = Database.Data.Query.get(i);
					if (c.getGcCode().equalsIgnoreCase(sGc))
					{
						Logger.DEBUG("TabMainView: Set selectedCache to " + c.getGcCode() + " from lastSaved.");
						GlobalCore.setSelectedCache(c); // !! sets GlobalCore.setAutoResort to false
						break;
					}
				}
			}
		}

		GlobalCore.setAutoResort(Config.StartWithAutoSelect.getValue());

		// create MapView Instanz
		CB_TabView mapTap = GlobalCore.isTab ? RightTab : LeftTab;
		TabMainView.mapView = new MapView(mapTap.getContentRec(), false, "MapView");

		platformConector.FirstShow();
		filterSetChanged();
		GL.that.removeRenderView(this);
	}

	private void addPhoneTab()
	{
		// nur ein Tab

		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.getHeight() - UiSizes.that.getInfoSliderHeight());
		rec.setPos(0, 0);

		LeftTab = new CB_TabView(rec, "Phone Tab");

		CacheListButton = new CB_Button(btnRec, "Button1", SpriteCacheBase.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCacheBase.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCacheBase.Nav);
		ToolsButton = new CB_Button(btnRec, "Button4", SpriteCacheBase.Tool);
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCacheBase.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(CacheListButton);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(ToolsButton);
		btnList.addButton(btn5);

		LeftTab.addButtonList(btnList);

		this.addChild(LeftTab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, LeftTab);
		actionShowCacheList.setTab(this, LeftTab);

		actionShowAboutView.setTab(this, LeftTab);
		actionShowCompassView.setTab(this, LeftTab);
		actionShowCreditsView.setTab(this, LeftTab);
		actionShowDescriptionView.setTab(this, LeftTab);
		actionShowFieldNotesView.setTab(this, LeftTab);
		actionShowJokerView.setTab(this, LeftTab);
		actionShowLogView.setTab(this, LeftTab);
		actionShowNotesView.setTab(this, LeftTab);
		actionShowSolverView.setTab(this, LeftTab);
		actionShowSolverView2.setTab(this, LeftTab);
		actionShowSpoilerView.setTab(this, LeftTab);
		actionShowTrackableListView.setTab(this, LeftTab);
		actionShowTrackListView.setTab(this, LeftTab);
		actionShowWaypointView.setTab(this, LeftTab);
		actionNavigateTo1.setTab(this, LeftTab);

		actionRecVoice.setTab(this, LeftTab);
		actionRecPicture.setTab(this, LeftTab);
		actionRecVideo.setTab(this, LeftTab);
		if (GlobalCore.isTestVersion()) actionTestView.setTab(this, LeftTab);

		// actionScreenLock.setTab(this, Tab);

		// Actions den Buttons zuweisen

		CacheListButton.addAction(new CB_ActionButton(actionShowCacheList, true, GestureDirection.Up));
		CacheListButton.addAction(new CB_ActionButton(actionShowTrackableListView, false, GestureDirection.Right));
		CacheListButton.addAction(new CB_ActionButton(actionShowTrackListView, false, GestureDirection.Down));
		// CacheListButton.addAction(new CB_ActionButton(actionShowExportView, false));

		btn2.addAction(new CB_ActionButton(actionShowDescriptionView, true, GestureDirection.Up));
		btn2.addAction(new CB_ActionButton(actionShowWaypointView, false, GestureDirection.Right));
		btn2.addAction(new CB_ActionButton(actionShowLogView, false, GestureDirection.Down));
		btn2.addAction(new CB_ActionButton(actionShowHint, false));
		btn2.addAction(new CB_ActionButton(actionShowSpoilerView, false));
		btn2.addAction(new CB_ActionButton(actionShowNotesView, false));
		// btn2.addAction(new CB_ActionButton(actionDelCaches, false));

		btn3.addAction(new CB_ActionButton(actionShowMap, true, GestureDirection.Up));
		btn3.addAction(new CB_ActionButton(actionShowCompassView, false, GestureDirection.Right));
		btn3.addAction(new CB_ActionButton(actionNavigateTo1, false, GestureDirection.Down));
		btn3.addAction(new CB_ActionButton(actionGenerateRoute, false, GestureDirection.Left));
		if (GlobalCore.isTestVersion()) btn3.addAction(new CB_ActionButton(actionTestView, false));

		ToolsButton.addAction(new CB_ActionButton(actionQuickFieldNote, false, GestureDirection.Up));
		ToolsButton.addAction(new CB_ActionButton(actionShowFieldNotesView, Config.ShowFieldnotesAsDefaultView.getValue()));
		ToolsButton.addAction(new CB_ActionButton(actionRecTrack, false));
		ToolsButton.addAction(new CB_ActionButton(actionRecVoice, false));
		ToolsButton.addAction(new CB_ActionButton(actionRecPicture, false, GestureDirection.Down));
		ToolsButton.addAction(new CB_ActionButton(actionRecVideo, false));
		ToolsButton.addAction(new CB_ActionButton(actionParking, false));
		ToolsButton.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));
		ToolsButton.addAction(new CB_ActionButton(actionShowSolverView2, false, GestureDirection.Right));
		ToolsButton.addAction(new CB_ActionButton(actionShowJokerView, false));

		btn5.addAction(new CB_ActionButton(actionShowAboutView, true, GestureDirection.Up));
		btn5.addAction(new CB_ActionButton(actionShowCreditsView, false));
		btn5.addAction(new CB_ActionButton(actionShowSettings, false, GestureDirection.Left));
		btn5.addAction(new CB_ActionButton(actionDayNight, false));
		// btn5.addAction(new CB_ActionButton(actionScreenLock, false));
		btn5.addAction(new CB_ActionButton(actionClose, false, GestureDirection.Down));

		actionShowAboutView.Execute();
	}

	private void addTabletTabs()
	{
		addLeftForTabletsTab();
		addRightForTabletsTab();
	}

	public CB_Button CacheListButton;
	public CB_Button ToolsButton;

	private void addLeftForTabletsTab()
	{
		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.getHeight() - UiSizes.that.getInfoSliderHeight());
		rec.setPos(0, 0);

		LeftTab = new CB_TabView(rec, "Phone Tab");

		CacheListButton = new CB_Button(btnRec, "Button1", SpriteCacheBase.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCacheBase.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCacheBase.Nav);
		ToolsButton = new CB_Button(btnRec, "Button4", SpriteCacheBase.Tool);
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCacheBase.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(CacheListButton);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(ToolsButton);
		btnList.addButton(btn5);

		LeftTab.addButtonList(btnList);

		this.addChild(LeftTab);
		// Tab.ShowView(new AboutView(this, "AboutView"));

		// Tab den entsprechneden Actions zuweisen
		actionShowCacheList.setTab(this, LeftTab);
		actionShowWaypointView.setTab(this, LeftTab);
		actionShowAboutView.setTab(this, LeftTab);
		actionShowCreditsView.setTab(this, LeftTab);
		actionShowTrackableListView.setTab(this, LeftTab);
		actionShowTrackListView.setTab(this, LeftTab);
		actionShowCompassView.setTab(this, LeftTab);
		actionShowLogView.setTab(this, LeftTab);
		actionShowFieldNotesView.setTab(this, LeftTab);
		actionShowJokerView.setTab(this, LeftTab);
		actionShowNotesView.setTab(this, LeftTab);
		actionNavigateTo1.setTab(this, LeftTab);

		actionRecVoice.setTab(this, LeftTab);
		actionRecPicture.setTab(this, LeftTab);
		actionRecVideo.setTab(this, LeftTab);

		// actionScreenLock.setTab(this, Tab);

		// Actions den Buttons zuweisen
		CacheListButton.addAction(new CB_ActionButton(actionShowCacheList, true));
		CacheListButton.addAction(new CB_ActionButton(actionShowTrackableListView, false));
		CacheListButton.addAction(new CB_ActionButton(actionShowTrackListView, false));

		btn2.addAction(new CB_ActionButton(actionShowWaypointView, true, GestureDirection.Right));
		btn2.addAction(new CB_ActionButton(actionShowLogView, false, GestureDirection.Down));
		btn2.addAction(new CB_ActionButton(actionShowHint, false));
		btn2.addAction(new CB_ActionButton(actionShowNotesView, false));
		// btn2.addAction(new CB_ActionButton(actionDelCaches, false));

		btn3.addAction(new CB_ActionButton(actionShowCompassView, true, GestureDirection.Right));
		btn3.addAction(new CB_ActionButton(actionNavigateTo1, false, GestureDirection.Down));
		btn3.addAction(new CB_ActionButton(actionGenerateRoute, false, GestureDirection.Left));

		ToolsButton.addAction(new CB_ActionButton(actionQuickFieldNote, false));
		ToolsButton.addAction(new CB_ActionButton(actionShowFieldNotesView, Config.ShowFieldnotesAsDefaultView.getValue()));
		ToolsButton.addAction(new CB_ActionButton(actionRecTrack, false));
		ToolsButton.addAction(new CB_ActionButton(actionRecVoice, false));
		ToolsButton.addAction(new CB_ActionButton(actionRecPicture, false));
		ToolsButton.addAction(new CB_ActionButton(actionRecVideo, false));
		ToolsButton.addAction(new CB_ActionButton(actionParking, false));
		ToolsButton.addAction(new CB_ActionButton(actionShowSolverView2, false));
		ToolsButton.addAction(new CB_ActionButton(actionShowJokerView, false));

		btn5.addAction(new CB_ActionButton(actionShowAboutView, true, GestureDirection.Up));
		btn5.addAction(new CB_ActionButton(actionShowCreditsView, false));
		btn5.addAction(new CB_ActionButton(actionShowSettings, false, GestureDirection.Left));
		btn5.addAction(new CB_ActionButton(actionDayNight, false));
		// btn5.addAction(new CB_ActionButton(actionScreenLock, false));
		btn5.addAction(new CB_ActionButton(actionClose, false));

		actionShowAboutView.Execute();
	}

	private void addRightForTabletsTab()
	{

		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Right.getWidth());
		rec.setX(GL_UISizes.UI_Left.getWidth());
		rec.setY(0);

		rec.setHeight(this.getHeight() - UiSizes.that.getInfoSliderHeight());

		RightTab = new CB_TabView(rec, "Phone Tab");

		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCacheBase.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCacheBase.Nav);
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCacheBase.Tool);

		CB_ButtonList btnList = new CB_ButtonList();

		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);

		RightTab.addButtonList(btnList);

		this.addChild(RightTab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, RightTab);
		actionShowSolverView.setTab(this, RightTab);
		actionShowSolverView2.setTab(this, RightTab);
		actionShowDescriptionView.setTab(this, RightTab);
		actionNavigateTo2.setTab(this, RightTab);
		if (GlobalCore.isTestVersion()) actionTestView.setTab(this, RightTab);
		actionShowSpoilerView.setTab(this, RightTab);

		// Actions den Buttons zuweisen
		btn2.addAction(new CB_ActionButton(actionShowDescriptionView, true));
		btn2.addAction(new CB_ActionButton(actionShowSpoilerView, false));

		btn3.addAction(new CB_ActionButton(actionShowMap, true, GestureDirection.Up));
		btn3.addAction(new CB_ActionButton(actionNavigateTo2, false, GestureDirection.Down));
		if (GlobalCore.isTestVersion()) btn3.addAction(new CB_ActionButton(actionTestView, false));

		btn4.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));

		actionShowMap.Execute();
	}

	private void autoLoadTrack()
	{
		String trackPath = Config.TrackFolder.getValue() + "/Autoload";
		if (FileIO.createDirectory(trackPath))
		{
			File dir = new File(trackPath);
			String[] files = dir.list();
			if (!(files == null))
			{
				if (files.length > 0)
				{
					for (String file : files)
					{
						RouteOverlay.LoadTrack(trackPath, file);

					}
				}
			}
		}
		else
		{
			File sddir = new File(trackPath);
			sddir.mkdirs();
		}
	}

	public void setContentMaxY(float y)
	{
		// Logger.LogCat("TabMainView SetContent maxY" + y);
		synchronized (childs)
		{
			for (int i = 0, n = childs.size(); i < n; i++)
			{
				GL_View_Base view = childs.get(i);
				if (view instanceof CB_TabView)
				{
					view.setHeight(y);
				}
			}
		}
	}

	public void switchDayNight()
	{
		reloadSprites(true);
	}

	public void reloadSprites(boolean switchDayNight)
	{

		// chk if initial
		if (!isInitial) Initial();

		try
		{
			GL.that.StopRender();
			if (switchDayNight) Config.changeDayNight();
			ManagerBase.RenderThemeChanged = true;
			GL.that.onStop();

			SpriteCacheBase.LoadSprites(true);
			GL.that.onStart();
			CallSkinChanged();

			this.removeChilds();

			CB_Button.reloadMenuSprite();
			if (GlobalCore.isTab) addTabletTabs();
			else
				addPhoneTab();

			// add Slider as last
			Slider slider = new Slider(this, "Slider");
			this.addChild(slider);

			String state = Config.nightMode.getValue() ? "Night" : "Day";

			GL.that.Toast("Switch to " + state, Toast.LENGTH_SHORT);

			platformConector.DayNightSwitched();

			synchronized (childs)
			{
				for (int i = 0, n = childs.size(); i < n; i++)
				{
					GL_View_Base view = childs.get(i);
					if (view instanceof CB_TabView)
					{
						((CB_TabView) view).SkinIsChanged();
					}
				}
			}
			invalidateTextureEventList.Call();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		GL.that.RestartRender();
	}

	private boolean isFilterd = false;

	public boolean isFilterd()
	{
		return isFilterd;
	}

	public void filterSetChanged()
	{
		if ((GlobalCore.LastFilter == null) || (GlobalCore.LastFilter.toString().equals(""))
				|| (PresetListViewItem.chkPresetFilter(FilterProperties.presets[0], GlobalCore.LastFilter))
				&& !GlobalCore.LastFilter.isExtendsFilter())
		{
			CacheListButton.setButtonSprites(SpriteCacheBase.CacheList);
			isFilterd = false;
		}
		else
		{
			CacheListButton.setButtonSprites(SpriteCacheBase.CacheListFilter);
			isFilterd = true;
		}

		// ##################################
		// Set new list size at context menu
		// ##################################
		String Name = "";

		SyncronizeHelper.sync("TabmainView 496");
		synchronized (Database.Data.Query)
		{
			int filterCount = Database.Data.Query.size();

			if (Database.Data.Query.GetCacheByGcCode("CBPark") != null) --filterCount;

			int DBCount = Database.Data.getCacheCountInDB();
			String Filtert = "";
			if (filterCount != DBCount)
			{
				Filtert = String.valueOf(filterCount) + "/";
			}

			Name = "  (" + Filtert + String.valueOf(DBCount) + ")";
		}
		SyncronizeHelper.endSync("TabmainView 496");
		actionShowCacheList.setNameExtention(Name);
	}

	public void showCacheList()
	{
		actionShowCacheList.Execute();
	}

	public void renderChilds(final Batch batch, ParentInfo parentInfo)
	{
		if (childs == null) return;
		super.renderChilds(batch, parentInfo);
	}

	private API_ErrorEventHandler handler = new API_ErrorEventHandler()
	{

		@Override
		public void InvalidAPI_Key()
		{
			String Msg = Translation.Get("apiKeyNeeded") + GlobalCore.br + GlobalCore.br;
			Msg += Translation.Get("wantApi");

			GL_MsgBox.Show(Msg, Translation.Get("errorAPI"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, new OnMsgBoxClickListener()
			{

				@Override
				public boolean onClick(int which, Object data)
				{
					if (which == GL_MsgBox.BUTTON_POSITIVE) platformConector.callGetApiKeyt();
					return true;
				}
			}, Config.RememberAsk_Get_API_Key);
		}
	};

	@Override
	public void PositionChanged()
	{
		try
		{
			TrackRecorder.recordPosition();
		}
		catch (Exception e)
		{
			Logger.Error("Core.MainViewBase.PositionChanged()", "TrackRecorder.recordPosition()", e);
			e.printStackTrace();
		}

		if (GlobalCore.getSelectedCache() != null)
		{
			float distance = GlobalCore.getSelectedCache().Distance(CalculationType.FAST, false);
			if (GlobalCore.getSelectedWaypoint() != null)
			{
				distance = GlobalCore.getSelectedWaypoint().Distance();
			}

			if (Config.switchViewApproach.getValue() && !GlobalCore.switchToCompassCompleted
					&& (distance < Config.SoundApproachDistance.getValue()))
			{
				actionShowCompassView.Execute();
				GlobalCore.switchToCompassCompleted = true;
			}
		}
	}

	@Override
	public String getReceiverName()
	{
		return "Core.MainViewBase";
	}

	@Override
	public Priority getPriority()
	{
		return Priority.High;
	}

	@Override
	public void OrientationChanged()
	{
	}

	@Override
	public void SpeedChanged()
	{
	}

	public boolean isInitial()
	{
		return isInitial;
	}

}
