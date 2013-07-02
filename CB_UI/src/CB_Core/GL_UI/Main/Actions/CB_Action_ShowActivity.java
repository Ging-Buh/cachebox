package CB_Core.GL_UI.Main.Actions;

import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.ViewID;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowActivity extends CB_Action_ShowView
{
	private ViewID viewConst;
	private Sprite mIcon;

	public CB_Action_ShowActivity(String Name, int ID, ViewID ViewConst, Sprite icon)
	{
		super(Name, ID);
		viewConst = ViewConst;
		mIcon = icon;
	}

	@Override
	public void Execute()
	{
		platformConector.showView(viewConst, 0, 0, 0, 0);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return mIcon;
	}

	@Override
	public CB_View_Base getView()
	{
		return null;
	}
}
