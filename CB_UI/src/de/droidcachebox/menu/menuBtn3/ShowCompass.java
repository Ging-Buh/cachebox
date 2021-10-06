package de.droidcachebox.menu.menuBtn3;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.CB_UI_Settings;
import de.droidcachebox.Config;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.OptionMenu;
import de.droidcachebox.gdx.views.CompassView;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.settings.SettingBool;

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
                () -> toggleSetting(CB_UI_Settings.CompassShowLastFound));
        menuCompassElements.show();
    }

    private boolean toggleSetting(SettingBool setting) {
        setting.setValue(!setting.getValue());
        Config.acceptChanges();
        return true;
    }

}
