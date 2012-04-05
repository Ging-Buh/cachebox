package CB_Core.GL_UI.Main;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.Actions.CB_Action;
import CB_Core.GL_UI.Main.Actions.CB_ActionCommand;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowAbout;
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
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowSettings;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowSolverView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowSpoilerView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowTrackListView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowTrackableListView;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowWaypointView;
import CB_Core.GL_UI.Main.Actions.CB_Action_Show_CacheList_ContextMenu;
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
import CB_Core.GL_UI.Views.SpoilerView;
import CB_Core.GL_UI.Views.TrackListView;
import CB_Core.GL_UI.Views.TrackableListView;
import CB_Core.GL_UI.Views.WaypointView;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UiSizes;

public class TabMainView extends MainViewBase
{
	private CB_Action actionTest;
	private CB_Action actionTest2;
	private CB_Action_ShowHint actionShowHint;
	private CB_Action_ShowMap actionShowMap;
	private CB_Action_ShowCacheList actionShowCacheList;
	private CB_Action_Show_CacheList_ContextMenu actionShowCacheListContextMenu;
	private CB_Action_ShowAbout actionShowAboutView;
	private CB_Action_ShowCompassView actionShowCompassView;
	private CB_Action_ShowCreditsView actionShowCreditsView;
	private CB_Action_ShowDescriptionView actionShowDescriptionView;
	private CB_Action_ShowFieldNotesView actionShowFieldNotesView;
	private CB_Action_ShowJokerView actionShowJokerView;
	private CB_Action_ShowLogView actionShowLogView;
	private CB_Action_ShowNotesView actionShowNotesView;
	private CB_Action_ShowSolverView actionShowSolverView;
	private CB_Action_ShowSpoilerView actionShowSpoilerView;
	private CB_Action_ShowTrackableListView actionShowTrackableListView;
	private CB_Action_ShowTrackListView actionShowTrackListView;
	private CB_Action_ShowWaypointView actionShowWaypointView;
	private CB_Action_ShowSettings actionShowSettings;

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

	public TabMainView(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);

		mainView = this;

	}

	@Override
	protected void Initial()
	{
		// Wird inerhalb des ersten Render Vorgangs aufgerufen.

		// eine Initialisierung der actions kommt hier zu spät, daher als Aufruf aus dem Constructor verschoben!

		ini();

	}

	private void ini()
	{
		actionTest = new CB_ActionCommand("Test", CB_Action.AID_TEST1);
		actionTest2 = new CB_ActionCommand("Test2", CB_Action.AID_TEST1);

		actionShowMap = new CB_Action_ShowMap();
		actionShowHint = new CB_Action_ShowHint();
		actionShowCacheList = new CB_Action_ShowCacheList();
		actionShowCacheListContextMenu = new CB_Action_Show_CacheList_ContextMenu();
		actionShowAboutView = new CB_Action_ShowAbout();
		actionShowCompassView = new CB_Action_ShowCompassView();
		actionShowCreditsView = new CB_Action_ShowCreditsView();
		actionShowDescriptionView = new CB_Action_ShowDescriptionView();
		actionShowFieldNotesView = new CB_Action_ShowFieldNotesView();
		actionShowJokerView = new CB_Action_ShowJokerView();
		actionShowLogView = new CB_Action_ShowLogView();
		actionShowNotesView = new CB_Action_ShowNotesView();
		actionShowSolverView = new CB_Action_ShowSolverView();
		actionShowSpoilerView = new CB_Action_ShowSpoilerView();
		actionShowTrackableListView = new CB_Action_ShowTrackableListView();
		actionShowTrackListView = new CB_Action_ShowTrackListView();
		actionShowWaypointView = new CB_Action_ShowWaypointView();
		actionShowSettings = new CB_Action_ShowSettings();

		if (GlobalCore.isTab) addTabletTabs();
		else
			addPhoneTab();
	}

	private void addPhoneTab()
	{
		// nur ein Tab

		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.getHeight() - UiSizes.getInfoSliderHeight());
		rec.setPos(0, 0);

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		CB_Button btn1 = new CB_Button(btnRec, "Button1", SpriteCache.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav);
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool);
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCache.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(btn1);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);
		btnList.addButton(btn5);

		Tab.addButtonList(btnList);

		this.addChild(Tab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, Tab);
		actionShowCacheList.setTab(this, Tab);
		actionShowCacheListContextMenu.setTab(this, Tab);

		actionShowAboutView.setTab(this, Tab);
		actionShowCompassView.setTab(this, Tab);
		actionShowCreditsView.setTab(this, Tab);
		actionShowDescriptionView.setTab(this, Tab);
		actionShowFieldNotesView.setTab(this, Tab);
		actionShowJokerView.setTab(this, Tab);
		actionShowLogView.setTab(this, Tab);
		actionShowNotesView.setTab(this, Tab);
		actionShowSolverView.setTab(this, Tab);
		actionShowSpoilerView.setTab(this, Tab);
		actionShowTrackableListView.setTab(this, Tab);
		actionShowTrackListView.setTab(this, Tab);
		actionShowWaypointView.setTab(this, Tab);

		// Actions den Buttons zuweisen

		btn1.addAction(new CB_ActionButton(actionShowCacheList, true));
		btn1.addAction(new CB_ActionButton(actionShowTrackableListView, false));
		btn1.addAction(new CB_ActionButton(actionShowTrackListView, false));
		btn1.addAction(new CB_ActionButton(actionShowCacheListContextMenu, false));

		btn2.addAction(new CB_ActionButton(actionShowDescriptionView, true));
		btn2.addAction(new CB_ActionButton(actionShowFieldNotesView, false));
		btn2.addAction(new CB_ActionButton(actionShowJokerView, false));
		btn2.addAction(new CB_ActionButton(actionShowLogView, false));
		btn2.addAction(new CB_ActionButton(actionShowNotesView, false));
		btn2.addAction(new CB_ActionButton(actionShowSolverView, false));
		btn2.addAction(new CB_ActionButton(actionShowSpoilerView, false));
		btn2.addAction(new CB_ActionButton(actionShowWaypointView, false));
		btn2.addAction(new CB_ActionButton(actionShowHint, false));

		btn3.addAction(new CB_ActionButton(actionShowMap, true));
		btn3.addAction(new CB_ActionButton(actionShowCompassView, false));

		btn5.addAction(new CB_ActionButton(actionShowAboutView, true));
		btn5.addAction(new CB_ActionButton(actionShowCreditsView, false));
		btn5.addAction(new CB_ActionButton(actionShowSettings, false));

		btn5.performClick();// actionShowAboutView.Execute();
	}

	private void addTabletTabs()
	{
		addLeftForTabletsTab();
		addRightForTabletsTab();
	}

	private void addLeftForTabletsTab()
	{
		// mit fünf Buttons
		CB_RectF btnRec = new CB_RectF(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight);

		CB_RectF rec = this.copy();
		rec.setWidth(GL_UISizes.UI_Left.getWidth());

		rec.setHeight(this.getHeight() - UiSizes.getInfoSliderHeight());
		rec.setPos(0, 0);

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		CB_Button btn1 = new CB_Button(btnRec, "Button1", SpriteCache.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav);
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool);
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCache.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(btn1);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);
		btnList.addButton(btn5);

		Tab.addButtonList(btnList);

		this.addChild(Tab);
		// Tab.ShowView(new AboutView(this, "AboutView"));

		// Tab den entsprechneden Actions zuweisen
		actionShowCacheList.setTab(this, Tab);
		actionShowCacheListContextMenu.setTab(this, Tab);
		actionShowAboutView.setTab(this, Tab);
		actionShowCreditsView.setTab(this, Tab);
		actionShowTrackableListView.setTab(this, Tab);
		actionShowTrackListView.setTab(this, Tab);
		actionShowSettings.setTab(this, Tab);
		actionShowCompassView.setTab(this, Tab);

		// Actions den Buttons zuweisen
		btn1.addAction(new CB_ActionButton(actionShowCacheList, true));
		btn1.addAction(new CB_ActionButton(actionShowTrackableListView, false));
		btn1.addAction(new CB_ActionButton(actionShowTrackListView, false));
		btn1.addAction(new CB_ActionButton(actionShowCacheListContextMenu, false));

		btn3.addAction(new CB_ActionButton(actionShowCompassView, true));

		btn5.addAction(new CB_ActionButton(actionShowAboutView, true));
		btn5.addAction(new CB_ActionButton(actionShowCreditsView, false));
		btn5.addAction(new CB_ActionButton(actionShowSettings, false));

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

		rec.setHeight(this.getHeight() - UiSizes.getInfoSliderHeight());

		CB_TabView Tab = new CB_TabView(rec, "Phone Tab");

		CB_Button btn1 = new CB_Button(btnRec, "Button1", SpriteCache.CacheList);
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache);
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav);
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool);
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCache.Misc);

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(btn1);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);
		btnList.addButton(btn5);

		Tab.addButtonList(btnList);

		this.addChild(Tab);

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, Tab);

		actionShowDescriptionView.setTab(this, Tab);
		actionShowFieldNotesView.setTab(this, Tab);
		actionShowJokerView.setTab(this, Tab);
		actionShowLogView.setTab(this, Tab);
		actionShowNotesView.setTab(this, Tab);
		actionShowSolverView.setTab(this, Tab);
		actionShowSpoilerView.setTab(this, Tab);
		actionShowWaypointView.setTab(this, Tab);
		// Actions den Buttons zuweisen
		btn2.addAction(new CB_ActionButton(actionShowDescriptionView, true));
		btn2.addAction(new CB_ActionButton(actionShowFieldNotesView, false));
		btn2.addAction(new CB_ActionButton(actionShowJokerView, false));
		btn2.addAction(new CB_ActionButton(actionShowLogView, false));
		btn2.addAction(new CB_ActionButton(actionShowNotesView, false));
		btn2.addAction(new CB_ActionButton(actionShowSolverView, false));
		btn2.addAction(new CB_ActionButton(actionShowSpoilerView, false));
		btn2.addAction(new CB_ActionButton(actionShowWaypointView, false));
		btn2.addAction(new CB_ActionButton(actionShowHint, false));

		btn3.addAction(new CB_ActionButton(actionShowMap, true));

		btn3.performClick();// actionShowMap.Execute();
	}

}
