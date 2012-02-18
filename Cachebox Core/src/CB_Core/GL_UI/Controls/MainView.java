package CB_Core.GL_UI.Controls;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.Views.CreditsView;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.GL_UI.Views.TestView;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainView extends GL_View_Base
{

	private TestView testView; // ID = 16
	private CreditsView creditView; // ID = 17
	private MapView mapView; // ID = 18

	private GL_View_Base leftFrame;
	private GL_View_Base rightFrame;

	/**
	 * Setzt die GL_View mit der übergebenen ID als anzuzeigende View Es wird nur diese View angezeigt!
	 * 
	 * @param ID
	 */
	public void setGLViewID(ViewID ID)
	{

		if (leftFrame == null || rightFrame == null)
		{
			leftFrame = new Box(GL_UISizes.UI_Left);
			rightFrame = new Box(GL_UISizes.UI_Right);

			this.removeChilds();

			this.addChild(leftFrame);

			if (GlobalCore.isTab)
			{
				this.addChild(rightFrame);
			}
		}

		if (ID.getPos() == ViewID.UI_Pos.Left)
		{
			leftFrame.removeChilds();
			leftFrame.addChild(getView(ID));
		}
		else
		{
			rightFrame.removeChilds();
			rightFrame.addChild(getView(ID));
		}

		Logger.LogCat("SetGlViewID" + ID);
	}

	public MainView(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);

		Me = this;

		Logger.LogCat("Construct MainView " + X + "/" + Y + "/" + "/" + Width + "/" + Height);

	}

	@Override
	public void render(SpriteBatch batch)
	{

	}

	private GL_View_Base getView(ViewID ID)
	{
		if (ID.getID() == ViewID.TEST_VIEW) return testView = new TestView(GL_UISizes.UI_Right);
		if (ID.getID() == ViewID.CREDITS_VIEW) return creditView = new CreditsView(GL_UISizes.UI_Right);
		if (ID.getID() == ViewID.GL_MAP_VIEW) return mapView = new MapView(GL_UISizes.UI_Right);
		return null;
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// hier erstmal nichts machen
		return true;
	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}
}