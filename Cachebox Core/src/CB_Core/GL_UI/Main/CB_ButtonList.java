package CB_Core.GL_UI.Main;

import java.util.ArrayList;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.Math.CB_RectF;

public class CB_ButtonList extends CB_View_Base
{

	private ArrayList<CB_Button> mButtons;

	public CB_ButtonList(CB_RectF rec, CharSequence Name, ArrayList<CB_Button> Buttons)
	{
		super(rec, Name);
		mButtons = Buttons;
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

}
