package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_Core.CacheTypes;
import CB_UI.GL_UI.Controls.Dialogs.ParkingDialog;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_Show_Parking_Dialog extends CB_Action {

    Color TrackColor;

    public CB_Action_Show_Parking_Dialog() {
	super("MyParking", MenuID.AID_SHOW_PARKING_DIALOG);
    }

    @Override
    public boolean getEnabled() {
	return true;
    }

    @Override
    public Sprite getIcon() {
	return SpriteCacheBase.getThemedSprite("big" + CacheTypes.MyParking.name());
    }

    @Override
    public void Execute() {
	ParkingDialog d = new ParkingDialog();
	d.Show();
    }
}
