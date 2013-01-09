package CB_Core.GL_UI.Main;

import java.io.File;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.TrackRecorder;
import CB_Core.DB.Database;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Activitys.FilterSettings.PresetListViewItem;
import CB_Core.GL_UI.Controls.Slider;
import CB_Core.GL_UI.Controls.Dialogs.Toast;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Main.CB_ActionButton.GestureDirection;
import CB_Core.GL_UI.Main.Actions.CB_Action_GenerateRoute;
import CB_Core.GL_UI.Main.Actions.CB_Action_QuickFieldNote;
import CB_Core.GL_UI.Main.Actions.CB_Action_RecTrack;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowAbout;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowActivity;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowCacheList;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowCompassView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowCreditsView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowDescriptionView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowFieldNotesView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowHint;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowJokerView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowLogView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowNotesView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowQuit;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowSolverView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowSolverView2;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowSpoilerView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowTestView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowTrackListView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowWaypointView;
import CB_Core.GL_UI.Main.Actions.CB_Action_Show_Delete_Dialog;
import CB_Core.GL_UI.Main.Actions.CB_Action_Show_Parking_Dialog;
import CB_Core.GL_UI.Main.Actions.CB_Action_Show_SelectDB_Dialog;
import CB_Core.GL_UI.Main.Actions.CB_Action_Show_Settings;
import CB_Core.GL_UI.Main.Actions.CB_Action_switch_DayNight;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Views.AboutView;
import CB_Core.GL_UI.Views.CacheListView;
import CB_Core.GL_UI.Views.CompassView;
import CB_Core.GL_UI.Views.CreditsView;
import CB_Core.GL_UI.Views.DescriptionView;
import CB_Core.GL_UI.Views.FieldNotesView;
import CB_Core.GL_UI.Views.JokerView;
import CB_Core.GL_UI.Views.LogView;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.GL_UI.Views.NotesView;
import CB_Core.GL_UI.Views.SolverView;
import CB_Core.GL_UI.Views.SolverView2;
import CB_Core.GL_UI.Views.SpoilerView;
import CB_Core.GL_UI.Views.TrackListView;
import CB_Core.GL_UI.Views.TrackableListView;
import CB_Core.GL_UI.Views.WaypointView;
import CB_Core.GL_UI.Views.TestViews.TestView;
import CB_Core.Log.Logger;
import CB_Core.Map.ManagerBase;
import CB_Core.Map.RouteOverlay;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TabMainView extends MainViewBase
{
	public static TabMainView that;

	private CB_Action_ShowTestView actionTestView;
	private CB_Action_ShowHint actionShowHint;
	public static CB_Action_ShowMap actionShowMap;
	public static CB_Action_ShowCacheList actionShowCacheList;

	private CB_Action_ShowAbout actionShowAboutView;

	private CB_Action_ShowCreditsView actionShowCreditsView;
	public static CB_Action_ShowDescriptionView actionShowDescriptionView;
	public static CB_Action_ShowFieldNotesView actionShowFieldNotesView;
	public static CB_Action_ShowJokerView actionShowJokerView;
	public static CB_Action_ShowLogView actionShowLogView;
	private CB_Action_ShowNotesView actionShowNotesView;
	public static CB_Action_ShowSolverView actionShowSolverView;
	private CB_Action_ShowSolverView2 actionShowSolverView2;
	public static CB_Action_ShowSpoilerView actionShowSpoilerView;
	// TODO activate TB List on 0.6.x => private CB_Action_ShowTrackableListView actionShowTrackableListView;
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

	public static CB_Action_ShowQuit actionClose = new CB_Action_ShowQuit();
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

		that = (TabMainView) (mainView = this);
		GL.that.addRenderView(this, GL.FRAME_RATE_IDLE);
	}

	@Override
	protected void Initial()
	{
		// Wird inerhalb des ersten Render Vorgangs aufgerufen.

		// eine Initialisierung der actions kommt hier zu spät, daher als Aufruf aus dem Constructor verschoben!

		// Config.settings.quickButtonShow.setValue(false);
		// Config.AcceptChanges();

		ini();
		isInitial = true;
	}

	private boolean isInitial = false;

	private void ini()
	{
		Logger.LogCat("Start TabMainView-Initial");

		actionShowMap = new CB_Action_ShowMap();
		actionShowHint = new CB_Action_ShowHint();
		actionShowCacheList = new CB_Action_ShowCacheList();

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
		// TODO activate TB List on 0.6.x => actionShowTrackableListView = new CB_Action_ShowTrackableListView();
		actionShowTrackListView = new CB_Action_ShowTrackListView();
		actionShowWaypointView = new CB_Action_ShowWaypointView();
		if (GlobalCore.isTestVersion()) actionTestView = new CB_Action_ShowTestView();
		actionShowSettings = new CB_Action_Show_Settings();

		actionNavigateTo1 = actionNavigateTo2 = new CB_Action_ShowActivity("NavigateTo", MenuID.AID_NAVIGATE_TO, ViewConst.NAVIGATE_TO,
				SpriteCache.Icons.get(46));

		actionRecTrack = new CB_Action_RecTrack();
		actionRecVoice = new CB_Action_ShowActivity("VoiceRec", MenuID.AID_VOICE_REC, ViewConst.VOICE_REC, SpriteCache.Icons.get(11));
		actionRecPicture = new CB_Action_ShowActivity("TakePhoto", MenuID.AID_TAKE_PHOTO, ViewConst.TAKE_PHOTO, SpriteCache.Icons.get(47));
		actionRecVideo = new CB_Action_ShowActivity("RecVideo", MenuID.AID_VIDEO_REC, ViewConst.VIDEO_REC, SpriteCache.Icons.get(10));

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

		if (Config.settings.TrackRecorderStartup.getValue() && platformConector.isGPSon())
		{
			TrackRecorder.StartRecording();
		}

		// set last selected Cache
		String sGc = Config.settings.LastSelectedCache.getValue();
		if (sGc != null && !sGc.equals(""))
		{
			synchronized (Database.Data.Query)
			{
				for (Cache c : Database.Data.Query)
				{
					if (c.GcCode.equalsIgnoreCase(sGc))
					{
						GlobalCore.setSelectedCache(c);
						break;
					}
				}
			}
		}

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

		rec.setHeight(this.height - UiSizes.getInfoSliderHeight());
		rec.setPos(0, 0);

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		CacheListButton = new CB_Button(btnRec, "Button1", SpriteCache.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav);
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool);
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCache.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(CacheListButton);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);
		btnList.addButton(btn5);

		Tab.addButtonList(btnList);

		this.addChild(Tab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, Tab);
		actionShowCacheList.setTab(this, Tab);

		actionShowAboutView.setTab(this, Tab);
		actionShowCompassView.setTab(this, Tab);
		actionShowCreditsView.setTab(this, Tab);
		actionShowDescriptionView.setTab(this, Tab);
		actionShowFieldNotesView.setTab(this, Tab);
		actionShowJokerView.setTab(this, Tab);
		actionShowLogView.setTab(this, Tab);
		actionShowNotesView.setTab(this, Tab);
		actionShowSolverView.setTab(this, Tab);
		actionShowSolverView2.setTab(this, Tab);
		actionShowSpoilerView.setTab(this, Tab);
		// TODO activate TB List on 0.6.x => actionShowTrackableListView.setTab(this, Tab);
		actionShowTrackListView.setTab(this, Tab);
		actionShowWaypointView.setTab(this, Tab);
		actionNavigateTo1.setTab(this, Tab);

		actionRecVoice.setTab(this, Tab);
		actionRecPicture.setTab(this, Tab);
		actionRecVideo.setTab(this, Tab);
		if (GlobalCore.isTestVersion()) actionTestView.setTab(this, Tab);

		// actionScreenLock.setTab(this, Tab);

		// Actions den Buttons zuweisen

		CacheListButton.addAction(new CB_ActionButton(actionShowCacheList, true, GestureDirection.Up));
		// TODO activate TB List on 0.6.x => CacheListButton.addAction(new CB_ActionButton(actionShowTrackableListView, false,
		// GestureDirection.Right));
		CacheListButton.addAction(new CB_ActionButton(actionShowTrackListView, false, GestureDirection.Down));

		btn2.addAction(new CB_ActionButton(actionShowDescriptionView, true, GestureDirection.Up));
		btn2.addAction(new CB_ActionButton(actionShowWaypointView, false, GestureDirection.Right));
		btn2.addAction(new CB_ActionButton(actionShowLogView, false, GestureDirection.Down));
		btn2.addAction(new CB_ActionButton(actionShowHint, false));
		btn2.addAction(new CB_ActionButton(actionShowSpoilerView, false));
		btn2.addAction(new CB_ActionButton(actionShowNotesView, false));

		btn3.addAction(new CB_ActionButton(actionShowMap, true, GestureDirection.Up));
		btn3.addAction(new CB_ActionButton(actionShowCompassView, false, GestureDirection.Right));
		btn3.addAction(new CB_ActionButton(actionNavigateTo1, false, GestureDirection.Down));
		btn3.addAction(new CB_ActionButton(actionGenerateRoute, false, GestureDirection.Left));
		if (GlobalCore.isTestVersion()) btn3.addAction(new CB_ActionButton(actionTestView, false));

		btn4.addAction(new CB_ActionButton(actionQuickFieldNote, false));
		btn4.addAction(new CB_ActionButton(actionShowFieldNotesView, false));
		btn4.addAction(new CB_ActionButton(actionRecTrack, false));
		btn4.addAction(new CB_ActionButton(actionRecVoice, false));
		btn4.addAction(new CB_ActionButton(actionRecPicture, false));
		btn4.addAction(new CB_ActionButton(actionRecVideo, false));
		btn4.addAction(new CB_ActionButton(actionDelCaches, false));
		btn4.addAction(new CB_ActionButton(actionParking, false));
		btn4.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));
		btn4.addAction(new CB_ActionButton(actionShowSolverView2, false));
		btn4.addAction(new CB_ActionButton(actionShowJokerView, false));

		btn5.addAction(new CB_ActionButton(actionShowAboutView, true, GestureDirection.Up));
		btn5.addAction(new CB_ActionButton(actionShowCreditsView, false));
		btn5.addAction(new CB_ActionButton(actionShowSettings, false, GestureDirection.Left));
		btn5.addAction(new CB_ActionButton(actionDayNight, false));
		// btn5.addAction(new CB_ActionButton(actionScreenLock, false));
		btn5.addAction(new CB_ActionButton(actionClose, false, GestureDirection.Down));

		btn5.performClick();// actionShowAboutView.Execute();
	}

	private void addTabletTabs()
	{
		addLeftForTabletsTab();
		addRightForTabletsTab();
	}

	CB_Button CacheListButton;

	private void addLeftForTabletsTab()
	{
		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.height - UiSizes.getInfoSliderHeight());
		rec.setPos(0, 0);

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		CacheListButton = new CB_Button(btnRec, "Button1", SpriteCache.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav);
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool);
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCache.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(CacheListButton);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);
		btnList.addButton(btn5);

		Tab.addButtonList(btnList);

		this.addChild(Tab);
		// Tab.ShowView(new AboutView(this, "AboutView"));

		// Tab den entsprechneden Actions zuweisen
		actionShowCacheList.setTab(this, Tab);
		actionShowWaypointView.setTab(this, Tab);
		actionShowAboutView.setTab(this, Tab);
		actionShowCreditsView.setTab(this, Tab);
		// TODO activate TB List on 0.6.x =>actionShowTrackableListView.setTab(this, Tab);
		actionShowTrackListView.setTab(this, Tab);
		actionShowCompassView.setTab(this, Tab);
		actionShowLogView.setTab(this, Tab);
		actionShowFieldNotesView.setTab(this, Tab);
		actionShowJokerView.setTab(this, Tab);
		actionShowNotesView.setTab(this, Tab);
		actionNavigateTo1.setTab(this, Tab);

		actionRecVoice.setTab(this, Tab);
		actionRecPicture.setTab(this, Tab);
		actionRecVideo.setTab(this, Tab);

		// actionScreenLock.setTab(this, Tab);

		// Actions den Buttons zuweisen
		CacheListButton.addAction(new CB_ActionButton(actionShowCacheList, true));
		// TODO activate TB List on 0.6.x =>CacheListButton.addAction(new CB_ActionButton(actionShowTrackableListView, false));
		CacheListButton.addAction(new CB_ActionButton(actionShowTrackListView, false));

		btn2.addAction(new CB_ActionButton(actionShowWaypointView, true, GestureDirection.Right));
		btn2.addAction(new CB_ActionButton(actionShowLogView, false, GestureDirection.Down));
		btn2.addAction(new CB_ActionButton(actionShowHint, false));
		btn2.addAction(new CB_ActionButton(actionShowNotesView, false));

		btn3.addAction(new CB_ActionButton(actionShowCompassView, true, GestureDirection.Right));
		btn3.addAction(new CB_ActionButton(actionNavigateTo1, false, GestureDirection.Down));
		btn3.addAction(new CB_ActionButton(actionGenerateRoute, false, GestureDirection.Left));

		btn4.addAction(new CB_ActionButton(actionQuickFieldNote, false));
		btn4.addAction(new CB_ActionButton(actionShowFieldNotesView, false));
		btn4.addAction(new CB_ActionButton(actionRecTrack, false));
		btn4.addAction(new CB_ActionButton(actionRecVoice, false));
		btn4.addAction(new CB_ActionButton(actionRecPicture, false));
		btn4.addAction(new CB_ActionButton(actionRecVideo, false));
		btn4.addAction(new CB_ActionButton(actionDelCaches, false));
		btn4.addAction(new CB_ActionButton(actionParking, false));
		btn4.addAction(new CB_ActionButton(actionShowSolverView2, false));
		btn4.addAction(new CB_ActionButton(actionShowJokerView, false));

		btn5.addAction(new CB_ActionButton(actionShowAboutView, true, GestureDirection.Up));
		btn5.addAction(new CB_ActionButton(actionShowCreditsView, false));
		btn5.addAction(new CB_ActionButton(actionShowSettings, false, GestureDirection.Left));
		btn5.addAction(new CB_ActionButton(actionDayNight, false));
		// btn5.addAction(new CB_ActionButton(actionScreenLock, false));
		btn5.addAction(new CB_ActionButton(actionClose, false));

		btn5.performClick();// actionShowAboutView.Execute();
	}

	private void addRightForTabletsTab()
	{

		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Right.getWidth());
		rec.setX(GL_UISizes.UI_Left.getWidth());
		rec.setY(0);

		rec.setHeight(this.height - UiSizes.getInfoSliderHeight());

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav);
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool);

		CB_ButtonList btnList = new CB_ButtonList();

		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);

		Tab.addButtonList(btnList);

		this.addChild(Tab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, Tab);
		actionShowSolverView.setTab(this, Tab);
		actionShowSolverView2.setTab(this, Tab);
		actionShowDescriptionView.setTab(this, Tab);
		actionNavigateTo2.setTab(this, Tab);
		if (GlobalCore.isTestVersion()) actionTestView.setTab(this, Tab);
		actionShowSpoilerView.setTab(this, Tab);

		// Actions den Buttons zuweisen
		btn2.addAction(new CB_ActionButton(actionShowDescriptionView, true));
		btn2.addAction(new CB_ActionButton(actionShowSpoilerView, false));

		btn3.addAction(new CB_ActionButton(actionShowMap, true, GestureDirection.Up));
		btn3.addAction(new CB_ActionButton(actionNavigateTo2, false, GestureDirection.Down));
		if (GlobalCore.isTestVersion()) btn3.addAction(new CB_ActionButton(actionTestView, false));

		btn4.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));

		btn3.performClick();// actionShowMap.Execute();
	}

	private void autoLoadTrack()
	{
		String trackPath = Config.settings.TrackFolder.getValue() + "/Autoload";
		if (FileIO.DirectoryExists(trackPath))
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
			for (GL_View_Base view : this.childs)
			{
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

		GL.that.StopRender();
		if (switchDayNight) Config.changeDayNight();
		ManagerBase.RenderThemeChanged = true;
		GL.that.onStop();

		SpriteCache.LoadSprites(true);
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

		String state = Config.settings.nightMode.getValue() ? "Night" : "Day";

		GL.that.Toast("Switch to " + state, Toast.LENGTH_SHORT);

		platformConector.DayNightSwitched();

		synchronized (childs)
		{
			for (GL_View_Base view : this.childs)
			{
				if (view instanceof CB_TabView)
				{
					((CB_TabView) view).SkinIsChanged();
				}
			}
		}
		GL.that.RestartRender();
	}

	public void filterSetChanged()
	{
		if ((GlobalCore.LastFilter == null) || (GlobalCore.LastFilter.ToString().equals(""))
				|| (PresetListViewItem.chkPresetFilter(FilterProperties.presets[0], GlobalCore.LastFilter.ToString()))
				&& !GlobalCore.LastFilter.isExtendsFilter())
		{
			CacheListButton.setButtonSprites(SpriteCache.CacheList);
		}
		else
		{
			CacheListButton.setButtonSprites(SpriteCache.CacheListFilter);
		}

		// ##################################
		// Set new list size at context menu
		// ##################################
		String Name = "";
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
		actionShowCacheList.setNameExtention(Name);
	}

	public void showCacheList()
	{
		actionShowCacheList.Execute();
	}

	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{
		if (childs == null) return;
		super.renderChilds(batch, parentInfo);
	}
}
