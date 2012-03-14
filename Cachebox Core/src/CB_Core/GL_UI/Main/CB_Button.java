package CB_Core.GL_UI.Main;

import java.util.ArrayList;

import CB_Core.GL_UI.Controls.Button;
import CB_Core.Math.CB_RectF;

public class CB_Button extends Button
{

	ArrayList<CB_ActionButton> mButtonActions;

	public CB_Button(CB_RectF rec, String Name, ArrayList<CB_ActionButton> ButtonActions)
	{
		super(rec, Name);
		mButtonActions = ButtonActions;
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

}
