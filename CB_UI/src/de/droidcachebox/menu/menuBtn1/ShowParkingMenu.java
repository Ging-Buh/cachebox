package de.droidcachebox.menu.menuBtn1;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.menu.menuBtn1.executes.ParkingMenu;

public class ShowParkingMenu extends AbstractAction {

    private static ShowParkingMenu showParkingMenu;

    private ShowParkingMenu() {
        super("MyParking");
    }

    public static ShowParkingMenu getInstance() {
        if (showParkingMenu == null) showParkingMenu = new ShowParkingMenu();
        return showParkingMenu;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite("big" + GeoCacheType.MyParking.name());
    }

    @Override
    public void execute() {
        new ParkingMenu().show();
    }
}
