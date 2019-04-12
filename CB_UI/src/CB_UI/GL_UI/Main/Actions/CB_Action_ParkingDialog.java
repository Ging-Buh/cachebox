package CB_UI.GL_UI.Main.Actions;

import CB_Core.CacheTypes;
import CB_UI.GL_UI.Controls.Dialogs.ParkingDialog;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ParkingDialog extends CB_Action {

    private static CB_Action_ParkingDialog that;
    private CB_Action_ParkingDialog() {
        super("MyParking", MenuID.AID_SHOW_PARKING_DIALOG);
    }

    public static CB_Action_ParkingDialog getInstance() {
        if (that == null) that = new CB_Action_ParkingDialog();
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
        d.Show();
    }
}
