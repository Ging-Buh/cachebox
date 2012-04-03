package CB_Core.GL_UI.Main;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.Actions.CB_Action;
import CB_Core.GL_UI.Main.Actions.CB_ActionCommand;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowCacheList;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowHint;
import CB_Core.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_Core.GL_UI.Views.AboutView;
import CB_Core.GL_UI.Views.CacheListView;
import CB_Core.GL_UI.Views.MapView;
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

	public static MapView mapView = null;
	public static CacheListView cacheListView = null;
	public static AboutView aboutView = null;

	public TabMainView(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);

		mainView = this;

	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub
		actionTest = new CB_ActionCommand("Test", CB_Action.AID_TEST1);
		actionTest2 = new CB_ActionCommand("Test2", CB_Action.AID_TEST1);

		actionShowMap = new CB_Action_ShowMap();
		actionShowHint = new CB_Action_ShowHint();
		actionShowCacheList = new CB_Action_ShowCacheList();

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
		// Actions den Buttons zuweisen
		btn1.addAction(new CB_ActionButton(actionShowCacheList, true));
		btn1.addAction(new CB_ActionButton(actionShowHint, false));

		btn3.addAction(new CB_ActionButton(actionShowMap, true));

		Tab.ShowView(new AboutView(this, "AboutView"));
		// Tab.ShowView(new CacheListView(this, "CacheListView"));
		// Tab.ShowView(new MapView(this, "MapView"));

	}

	private void addTabletTabs()
	{
		addLeftForTabletsTab();
		addRightForTabletsTab();
		actionShowCacheList.Execute();
		actionShowMap.Execute();
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

		// Actions den Buttons zuweisen
		btn1.addAction(new CB_ActionButton(actionShowCacheList, true));
		btn1.addAction(new CB_ActionButton(actionShowHint, false));
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

		// Tab den entsprechneden Actions zuweisen
		actionShowMap.setTab(this, Tab);
		// Actions den Buttons zuweisen
		btn3.addAction(new CB_ActionButton(actionShowMap, true));
	}

}
