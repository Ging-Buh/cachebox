package CB_Core.GL_UI.Main;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ViewID;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.MainButtonBar;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.Views.AboutView;
import CB_Core.GL_UI.Views.CreditsView;
import CB_Core.GL_UI.Views.MapControlTest;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.GL_UI.Views.TestView;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class MainView extends MainViewBase
{

	private AboutView aboutView; // ID=11

	private TestView testView; // ID = 16
	private CreditsView creditView; // ID = 17
	private MapView mapView; // ID = 18
	private MapControlTest mapControlTest;// ID=19

	private MainButtonBar mainBtnBar;

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

			addMainBtnBar();

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

		GL_Listener.glListener.renderOnce("MainView SetGlViewID");

		Logger.LogCat("SetGlViewID" + ID);
	}

	private void addMainBtnBar()
	{
		if (mainBtnBar == null)
		{
			int btnWidth = GlobalCore.isTab ? UiSizes.RefWidth * 2 : UiSizes.RefWidth;
			mainBtnBar = new MainButtonBar(new CB_RectF(0, 0, btnWidth, GL_UISizes.BottomButtonHeight), "mainButtonBar");
		}
		this.addChild(mainBtnBar);
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

		// TODO nur aktive views, die nicht mehr benötigt werden löschen
		testView = null;
		creditView = null;
		mapView = null;
		mapControlTest = null;
		aboutView = null;

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

		if (ID.getID() == ViewID.MAP_CONTROL_TEST_VIEW)
		{
			mapControlTest = new MapControlTest(GL_UISizes.UI_Right, "MapControlTestView");
			mapControlTest.setClickable(true);
			mapControlTest.setPos(iniPos);
			return mapControlTest;
		}

		if (ID.getID() == ViewID.ABOUT_VIEW)
		{
			aboutView = new AboutView(GL_UISizes.UI_Left, "AboutView");
			aboutView.setClickable(true);
			aboutView.setPos(iniPos);
			return aboutView;
		}

		return null;
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		requestLayout();
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

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return true;
	}

}