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
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class MainView extends GL_View_Base
{

	private TestView testView; // ID = 16
	private CreditsView creditView; // ID = 17
	private MapView mapView; // ID = 18

	private GL_View_Base leftFrame;
	private GL_View_Base rightFrame;
	public static MainView mainView = null;

	/**
	 * Setzt die GL_View mit der übergebenen ID als anzuzeigende View Es wird nur diese View angezeigt!
	 * 
	 * @param ID
	 */
	public void setGLViewID(ViewID ID)
	{

		if (leftFrame == null || rightFrame == null)
		{
			leftFrame = new Box(GL_UISizes.UI_Left, "LeftBox");
			rightFrame = new Box(GL_UISizes.UI_Right, "RightBox");

			leftFrame.setClickable(true);

			this.removeChilds();

			this.addChild(leftFrame);

			if (GlobalCore.isTab)
			{
				this.addChild(rightFrame);
				rightFrame.setClickable(true);
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

	public MainView(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);

		Me = this;
		mainView = this;

		Logger.LogCat("Construct MainView " + X + "/" + Y + "/" + "/" + Width + "/" + Height);

	}

	@Override
	public void render(SpriteBatch batch)
	{

	}

	private GL_View_Base getView(ViewID ID)
	{
		Vector2 iniPos = new Vector2(0, 0);

		if (ID.getID() == ViewID.TEST_VIEW)
		{
			testView = new TestView(GL_UISizes.UI_Right, "TestView");
			testView.setClickable(true);
			testView.setPos(iniPos);
			return testView;
		}

		if (ID.getID() == ViewID.CREDITS_VIEW)
		{
			creditView = new CreditsView(GL_UISizes.UI_Right, "CreditView");
			creditView.setClickable(true);
			creditView.setPos(iniPos);
			return creditView;
		}

		if (ID.getID() == ViewID.GL_MAP_VIEW)
		{
			mapView = new MapView(GL_UISizes.UI_Right, "MapView");
			mapView.setClickable(true);
			mapView.setPos(iniPos);
			return mapView;
		}
		return null;
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		requestLayout();
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

	@Override
	public void onParentRezised(CB_RectF rec)
	{
		requestLayout();

	}

	public void requestLayout()
	{
		// muss die Größe für leftFrame und rightFrame neu berechnen

		Logger.LogCat("MainView onParentRezised()");

		GL_UISizes.UI_Left = new CB_RectF(0, GL_UISizes.BottomButtonHeight, UiSizes.RefWidth, this.height - GL_UISizes.BottomButtonHeight
				- GL_UISizes.TopButtonHeight);
		GL_UISizes.UI_Right = GL_UISizes.UI_Left.copy();
		if (GlobalCore.isTab)
		{
			GL_UISizes.UI_Right.setX(GL_UISizes.UI_Left.getWidth() + 1);
			GL_UISizes.UI_Right.setWidth(this.width - UiSizes.RefWidth);
		}

		if (leftFrame != null && rightFrame != null)
		{
			leftFrame.setRec(GL_UISizes.UI_Left);
			rightFrame.setRec(GL_UISizes.UI_Right);
		}
	}
}