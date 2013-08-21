package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.SpriteCacheBase;
import CB_UI.GL_UI.Controls.Dialogs.ParkingDialog;
import CB_UI.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Show_Parking_Dialog extends CB_ActionCommand
{

	Color TrackColor;

	public CB_Action_Show_Parking_Dialog()
	{
		super("MyParking", MenuID.AID_SHOW_PARKING_DIALOG);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.BigIcons.get(20);
	}

	@Override
	public void Execute()
	{
		ParkingDialog d = new ParkingDialog();
		d.Show();
	}
}
