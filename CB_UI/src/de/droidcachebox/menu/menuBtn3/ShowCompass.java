package de.droidcachebox.menu.menuBtn3;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.OptionMenu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.executes.CompassView;
import de.droidcachebox.settings.SettingBool;
import de.droidcachebox.settings.Settings;

public class ShowCompass extends AbstractShowAction {
    private static ShowCompass that;

    private ShowCompass() {
        super("Compass");
    }

    public static ShowCompass getInstance() {
        if (that == null) that = new ShowCompass();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(CompassView.getInstance());
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
        Menu icm = new Menu("CompassViewContextMenuTitle");
        icm.addMenuItem("view", null, this::showOptionMenu);
        return icm;
    }

    private void showOptionMenu() {
        OptionMenu menuCompassElements = new OptionMenu("CompassViewElementsMenuTitle");
        menuCompassElements.addCheckableMenuItem("CompassShowMap", Settings.CompassShowMap.getValue(),
                () -> toggleSetting(Settings.CompassShowMap));
        menuCompassElements.addCheckableMenuItem("CompassShowWP_Name", Settings.CompassShowWP_Name.getValue(),
                () -> toggleSetting(Settings.CompassShowWP_Name));
        menuCompassElements.addCheckableMenuItem("CompassShowWP_Icon", Settings.CompassShowWP_Icon.getValue(),
                () -> toggleSetting(Settings.CompassShowWP_Icon));
        menuCompassElements.addCheckableMenuItem("CompassShowAttributes", Settings.CompassShowAttributes.getValue(),
                () -> toggleSetting(Settings.CompassShowAttributes));
        menuCompassElements.addCheckableMenuItem("CompassShowGcCode", Settings.CompassShowGcCode.getValue(),
                () -> toggleSetting(Settings.CompassShowGcCode));
        menuCompassElements.addCheckableMenuItem("CompassShowCoords", Settings.CompassShowCoords.getValue(),
                () -> toggleSetting(Settings.CompassShowCoords));
        menuCompassElements.addCheckableMenuItem("CompassShowWpDesc", Settings.CompassShowWpDesc.getValue(),
                () -> toggleSetting(Settings.CompassShowWpDesc));
        menuCompassElements.addCheckableMenuItem("CompassShowSatInfos", Settings.CompassShowSatInfos.getValue(),
                () -> toggleSetting(Settings.CompassShowSatInfos));
        menuCompassElements.addCheckableMenuItem("CompassShowSunMoon", Settings.CompassShowSunMoon.getValue(),
                () -> toggleSetting(Settings.CompassShowSunMoon));
        menuCompassElements.addCheckableMenuItem("CompassShowTargetDirection", Settings.CompassShowTargetDirection.getValue(),
                () -> toggleSetting(Settings.CompassShowTargetDirection));
        menuCompassElements.addCheckableMenuItem("CompassShowSDT", Settings.CompassShowSDT.getValue(),
                () -> toggleSetting(Settings.CompassShowSDT));
        menuCompassElements.addCheckableMenuItem("CompassShowLastFound", Settings.CompassShowLastFound.getValue(),
                () -> toggleSetting(Settings.CompassShowLastFound));
        menuCompassElements.show();
    }

    private void toggleSetting(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Settings.getInstance().acceptChanges();
    }

}
