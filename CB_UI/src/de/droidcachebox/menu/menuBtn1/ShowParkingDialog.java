package de.droidcachebox.menu.menuBtn1;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.menu.menuBtn1.executes.ParkingDialog;

public class ShowParkingDialog extends AbstractAction {

    private static ShowParkingDialog showParkingDialog;

    private ShowParkingDialog() {
        super("MyParking");
    }

    public static ShowParkingDialog getInstance() {
        if (showParkingDialog == null) showParkingDialog = new ShowParkingDialog();
        return showParkingDialog;
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
        new ParkingDialog().show();
    }
}
