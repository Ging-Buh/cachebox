package CB_Core.GL_UI.Main.Actions;

import CB_Core.Config;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.CompassView;
import CB_Core.Settings.SettingBool;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowCompassView extends CB_Action_ShowView
{
	public final int MI_TEST1 = 1;
	public final int MI_TEST2 = 2;

	public CB_Action_ShowCompassView()
	{
		super("Compass", MenuID.AID_SHOW_COMPASS);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.compassView == null) && (tabMainView != null) && (tab != null)) TabMainView.compassView = new CompassView(
				tab.getContentRec(), "CompassView");

		if ((TabMainView.compassView != null) && (tab != null)) tab.ShowView(TabMainView.compassView);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.compassView;
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(6);
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		Menu icm = new Menu("menu_compassView");
		icm.addItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_COMPASS_SHOW_MAP, "CompassShowMap");
		mi.setCheckable(true);
		mi.setChecked(Config.settings.CompassShowMap.getValue());

		mi = icm.addItem(MenuID.MI_COMPASS_SHOW_NAME, "CompassShowWP_Name");
		mi.setCheckable(true);
		mi.setChecked(Config.settings.CompassShowWP_Name.getValue());

		mi = icm.addItem(MenuID.MI_COMPASS_SHOW_ICON, "CompassShowWP_Icon");
		mi.setCheckable(true);
		mi.setChecked(Config.settings.CompassShowWP_Icon.getValue());

		mi = icm.addItem(MenuID.MI_COMPASS_SHOW_ATTRIBUTES, "CompassShowAttributes");
		mi.setCheckable(true);
		mi.setChecked(Config.settings.CompassShowAttributes.getValue());

		mi = icm.addItem(MenuID.MI_COMPASS_SHOW_GC_CODE, "CompassShowGcCode");
		mi.setCheckable(true);
		mi.setChecked(Config.settings.CompassShowGcCode.getValue());

		mi = icm.addItem(MenuID.MI_COMPASS_SHOW_COORDS, "CompassShowCoords");
		mi.setCheckable(true);
		mi.setChecked(Config.settings.CompassShowCoords.getValue());

		mi = icm.addItem(MenuID.MI_COMPASS_SHOW_WP_DESC, "CompassShowWpDesc");
		mi.setCheckable(true);
		mi.setChecked(Config.settings.CompassShowWpDesc.getValue());

		mi = icm.addItem(MenuID.MI_COMPASS_SHOW_SAT_INFO, "CompassShowSatInfos");
		mi.setCheckable(true);
		mi.setChecked(Config.settings.CompassShowSatInfos.getValue());

		mi = icm.addItem(MenuID.MI_COMPASS_SHOW_SUN_MOON, "CompassShowSunMon");
		mi.setCheckable(true);
		mi.setChecked(Config.settings.CompassShowSunMoon.getValue());

		return icm;
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{

			switch (((MenuItem) v).getMenuItemId())
			{
			case MenuID.MI_COMPASS_SHOW_MAP:
				toggleSetting(Config.settings.CompassShowMap);
				return true;

			case MenuID.MI_COMPASS_SHOW_NAME:
				toggleSetting(Config.settings.CompassShowWP_Name);
				return true;

			case MenuID.MI_COMPASS_SHOW_ICON:
				toggleSetting(Config.settings.CompassShowWP_Icon);
				return true;

			case MenuID.MI_COMPASS_SHOW_ATTRIBUTES:
				toggleSetting(Config.settings.CompassShowAttributes);
				return true;

			case MenuID.MI_COMPASS_SHOW_GC_CODE:
				toggleSetting(Config.settings.CompassShowGcCode);
				return true;

			case MenuID.MI_COMPASS_SHOW_COORDS:
				toggleSetting(Config.settings.CompassShowCoords);
				return true;

			case MenuID.MI_COMPASS_SHOW_WP_DESC:
				toggleSetting(Config.settings.CompassShowWpDesc);
				return true;

			case MenuID.MI_COMPASS_SHOW_SAT_INFO:
				toggleSetting(Config.settings.CompassShowSatInfos);
				return true;

			case MenuID.MI_COMPASS_SHOW_SUN_MOON:
				toggleSetting(Config.settings.CompassShowSunMoon);
				return true;
			}
			return true;
		}
	};

	private void toggleSetting(SettingBool setting)
	{
		setting.setValue(!setting.getValue());
		Config.AcceptChanges();
		if (CompassView.that != null && CompassView.that.isVisible()) CompassView.that.onShow();
	}

}
