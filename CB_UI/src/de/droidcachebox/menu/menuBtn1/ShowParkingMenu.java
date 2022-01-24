package de.droidcachebox.menu.menuBtn1;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.menu.menuBtn1.executes.ParkingMenu;

public class ShowParkingMenu extends AbstractAction {

    public ShowParkingMenu() {
        super("MyParking");
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
