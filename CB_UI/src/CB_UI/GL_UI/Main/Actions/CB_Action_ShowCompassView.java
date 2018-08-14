package CB_UI.GL_UI.Main.Actions;

import CB_UI.CB_UI_Settings;
import CB_UI.Config;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.CompassView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Menu.OptionMenu;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_Utils.Settings.SettingBool;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowCompassView extends CB_Action_ShowView {
    public final int MI_TEST1 = 1;
    public final int MI_TEST2 = 2;
    private final OnClickListener onItemClickListener = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

            switch (((MenuItem) v).getMenuItemId()) {
                case MenuID.MI_COMPASS_SHOW:
                    showOptionMenu();
                    return true;
                case MenuID.MI_COMPASS_SHOW_MAP:
                    toggleSetting(CB_UI_Settings.CompassShowMap);
                    return true;

                case MenuID.MI_COMPASS_SHOW_NAME:
                    toggleSetting(CB_UI_Settings.CompassShowWP_Name);
                    return true;

                case MenuID.MI_COMPASS_SHOW_ICON:
                    toggleSetting(CB_UI_Settings.CompassShowWP_Icon);
                    return true;

                case MenuID.MI_COMPASS_SHOW_ATTRIBUTES:
                    toggleSetting(CB_UI_Settings.CompassShowAttributes);
                    return true;

                case MenuID.MI_COMPASS_SHOW_GC_CODE:
                    toggleSetting(CB_UI_Settings.CompassShowGcCode);
                    return true;

                case MenuID.MI_COMPASS_SHOW_COORDS:
                    toggleSetting(CB_UI_Settings.CompassShowCoords);
                    return true;

                case MenuID.MI_COMPASS_SHOW_WP_DESC:
                    toggleSetting(CB_UI_Settings.CompassShowWpDesc);
                    return true;

                case MenuID.MI_COMPASS_SHOW_SAT_INFO:
                    toggleSetting(CB_UI_Settings.CompassShowSatInfos);
                    return true;

                case MenuID.MI_COMPASS_SHOW_SUN_MOON:
                    toggleSetting(CB_UI_Settings.CompassShowSunMoon);
                    return true;

                case MenuID.MI_COMPASS_SHOW_TARGET_DIRECTION:
                    toggleSetting(CB_UI_Settings.CompassShowTargetDirection);
                    return true;
                case MenuID.MI_COMPASS_SHOW_S_D_T:
                    toggleSetting(CB_UI_Settings.CompassShowSDT);
                    return true;
                case MenuID.MI_COMPASS_SHOW_LAST_FOUND:
                    toggleSetting(CB_UI_Settings.CompassShowLastFound);
                    return true;
            }
            return false;
        }
    };

    public CB_Action_ShowCompassView() {
        super("Compass", MenuID.AID_SHOW_COMPASS);
    }

    @Override
    public void Execute() {
        if ((TabMainView.compassView == null) && (tabMainView != null) && (tab != null))
            TabMainView.compassView = new CompassView(tab.getContentRec(), "CompassView");

        if ((TabMainView.compassView != null) && (tab != null))
            tab.ShowView(TabMainView.compassView);
    }

    @Override
    public CB_View_Base getView() {
        return TabMainView.compassView;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.compass.name());
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu icm = new Menu("menu_compassView");
        icm.addOnClickListener(onItemClickListener);

        icm.addItem(MenuID.MI_COMPASS_SHOW, "view");

        return icm;
    }

    private void showOptionMenu() {
        OptionMenu icm = new OptionMenu("menu_compassView");
        icm.addOnClickListener(onItemClickListener);
        MenuItem mi;

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_MAP, "CompassShowMap");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowMap.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_NAME, "CompassShowWP_Name");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowWP_Name.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_ICON, "CompassShowWP_Icon");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowWP_Icon.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_ATTRIBUTES, "CompassShowAttributes");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowAttributes.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_GC_CODE, "CompassShowGcCode");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowGcCode.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_COORDS, "CompassShowCoords");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowCoords.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_WP_DESC, "CompassShowWpDesc");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowWpDesc.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_SAT_INFO, "CompassShowSatInfos");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowSatInfos.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_SUN_MOON, "CompassShowSunMoon");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowSunMoon.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_TARGET_DIRECTION, "CompassShowTargetDirection");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowTargetDirection.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_S_D_T, "CompassShowSDT");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowSDT.getValue());

        mi = icm.addItem(MenuID.MI_COMPASS_SHOW_LAST_FOUND, "CompassShowLastFound");
        mi.setCheckable(true);
        mi.setChecked(CB_UI_Settings.CompassShowLastFound.getValue());

        icm.Show();

    }

    private void toggleSetting(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Config.AcceptChanges();
    }

}
