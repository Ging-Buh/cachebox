package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Views.CreditsView;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.GL_UI.Views.TestView;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MainView extends GL_View_Base
{

	/**
	 * beinhaltet die ID der aktuell angezeigten GL_View
	 */
	private static int mGL_ViewID = -1;

	public static final int TEST_VIEW = 16;
	public static final int CREDITS_VIEW = 17;
	public static final int MAP_VIEW = 18;

	private TestView testView; // ID = 16
	private CreditsView creditView; // ID = 17
	private MapView mapView; // ID = 18

	private static boolean viewChanged = false;

	/**
	 * Setzt die GL_View mit der übergebenen ID als anzuzeigende View Es wird nur diese View angezeigt!
	 * 
	 * @param ID
	 */
	public static void setGLViewID(int ID)
	{
		mGL_ViewID = ID;
		viewChanged = true;

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
		// Chk if ViewID changed
		if (viewChanged)
		{
			Logger.LogCat("Chk if ViewID changed" + mGL_ViewID);
			this.removeChilds();
			switch (mGL_ViewID)
			{
			case TEST_VIEW:
				if (testView == null)
				{
					testView = new TestView(this); // create Testview in voller Göße
					testView.setClickable(true);
				}
				this.addChild(testView);
				break;
			case CREDITS_VIEW:
				if (creditView == null)
				{
					creditView = new CreditsView(this); // create CreditsView in voller Göße
					creditView.setClickable(true);
				}
				this.addChild(creditView);
				break;
			case MAP_VIEW:
				if (mapView == null)
				{
					mapView = new MapView(this); // create MapView in voller Göße
					mapView.setClickable(true);
				}
				this.addChild(mapView);
				break;
			}

			viewChanged = false;
		}

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