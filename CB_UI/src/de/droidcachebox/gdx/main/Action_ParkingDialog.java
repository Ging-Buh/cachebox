package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.database.CacheTypes;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.dialogs.ParkingDialog;

public class Action_ParkingDialog extends AbstractAction {

    private static Action_ParkingDialog that;

    private Action_ParkingDialog() {
        super("MyParking", MenuID.AID_SHOW_PARKING_DIALOG);
    }

    public static Action_ParkingDialog getInstance() {
        if (that == null) that = new Action_ParkingDialog();
        return that;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite("big" + CacheTypes.MyParking.name());
    }

    @Override
    public void Execute() {
        ParkingDialog d = new ParkingDialog();
        d.show();
    }
}
