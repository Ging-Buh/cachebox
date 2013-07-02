package CB_Core.GL_UI.Main;

import java.util.ArrayList;

public class CB_ButtonList
{

	public ArrayList<CB_Button> Buttons;

	public CB_ButtonList(ArrayList<CB_Button> Buttons)
	{
		this.Buttons = Buttons;
	}

	public CB_ButtonList()
	{

	}

	public void addButton(CB_Button Button)
	{
		if (Buttons == null) Buttons = new ArrayList<CB_Button>();
		Buttons.add(Button);
	}

}
