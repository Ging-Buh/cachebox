package CB_UI.GL_UI.Main.Actions;

import CB_UI.CB_UI_Settings;
import CB_UI.Config;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.Views.CompassView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.OptionMenu;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_Utils.Settings.SettingBool;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowCompassView extends CB_Action_ShowView {
    private static CB_Action_ShowCompassView that;

    private CB_Action_ShowCompassView() {
        super("Compass", MenuID.AID_SHOW_COMPASS);
    }

    public static CB_Action_ShowCompassView getInstance() {
        if (that == null) that = new CB_Action_ShowCompassView();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(CompassView.getInstance());
    }

    @Override
    public CB_View_Base getView() {
        return CompassView.getInstance();
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
        icm.addMenuItem("view", null, this::showOptionMenu);
        return icm;
    }

    private void showOptionMenu() {
        OptionMenu menuCompassElements = new OptionMenu("CompassViewElementsMenuTitle");
        menuCompassElements.addCheckableMenuItem("CompassShowMap", CB_UI_Settings.CompassShowMap.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowMap));
        menuCompassElements.addCheckableMenuItem("CompassShowWP_Name", CB_UI_Settings.CompassShowWP_Name.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowWP_Name));
        menuCompassElements.addCheckableMenuItem("CompassShowWP_Icon", CB_UI_Settings.CompassShowWP_Icon.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowWP_Icon));
        menuCompassElements.addCheckableMenuItem("CompassShowAttributes", CB_UI_Settings.CompassShowAttributes.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowAttributes));
        menuCompassElements.addCheckableMenuItem("CompassShowGcCode", CB_UI_Settings.CompassShowGcCode.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowGcCode));
        menuCompassElements.addCheckableMenuItem("CompassShowCoords", CB_UI_Settings.CompassShowCoords.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowCoords));
        menuCompassElements.addCheckableMenuItem("CompassShowWpDesc", CB_UI_Settings.CompassShowWpDesc.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowWpDesc));
        menuCompassElements.addCheckableMenuItem("CompassShowSatInfos", CB_UI_Settings.CompassShowSatInfos.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowSatInfos));
        menuCompassElements.addCheckableMenuItem("CompassShowSunMoon", CB_UI_Settings.CompassShowSunMoon.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowSunMoon));
        menuCompassElements.addCheckableMenuItem("CompassShowTargetDirection", CB_UI_Settings.CompassShowTargetDirection.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowTargetDirection));
        menuCompassElements.addCheckableMenuItem("CompassShowSDT", CB_UI_Settings.CompassShowSDT.getValue(),
                () -> toggleSetting(CB_UI_Settings.CompassShowSDT));
        menuCompassElements.addCheckableMenuItem("CompassShowLastFound", CB_UI_Settings.CompassShowLastFound.getValue(),
                () -> toggleSetting( CB_UI_Settings.CompassShowLastFound));
        menuCompassElements.Show();
    }

    private boolean toggleSetting(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Config.AcceptChanges();
        return true;
    }

}
