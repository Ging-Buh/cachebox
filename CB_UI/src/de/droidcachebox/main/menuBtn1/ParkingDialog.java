package de.droidcachebox.main.menuBtn1;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.main.AbstractAction;

public class ParkingDialog extends AbstractAction {

    private static ParkingDialog that;

    private ParkingDialog() {
        super("MyParking", MenuID.AID_SHOW_PARKING_DIALOG);
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
