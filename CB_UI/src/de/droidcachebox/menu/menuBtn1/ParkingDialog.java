package de.droidcachebox.menu.menuBtn1;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.gdx.Sprites;

public class ParkingDialog extends AbstractAction {

    private static ParkingDialog that;

    private ParkingDialog() {
        super("MyParking");
    }

    public static ParkingDialog getInstance() {
        if (that == null) that = new ParkingDialog();
        return that;
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
        de.droidcachebox.gdx.controls.dialogs.ParkingDialog d = new de.droidcachebox.gdx.controls.dialogs.ParkingDialog();
        d.show();
    }
}
