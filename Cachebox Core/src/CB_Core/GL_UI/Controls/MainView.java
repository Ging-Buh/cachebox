package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class MainView extends GL_View_Base
{
	private Image image;

	public MainView(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);

		Logger.LogCat("Construct MainView " + X + "/" + Y + "/" + "/" + Width + "/" + Height);

		// Initial TestView
		TestView testView = new TestView(100, 200, 200, 200);
		this.addChild(testView);

		// Initial TestView
		ZoomButtons btnZoom = new ZoomButtons(20, 20, 200, 75);
		this.addChild(btnZoom);

		// initial Image
		image = new Image(200, 400, 128, 128);
		image.setImage("data/cb_test.png");

		this.addChild(image);
	}

	@Override
	public void render(SpriteBatch batch)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		image.setPos(new Vector2(x - image.getWidth() / 2, y - image.getHeight() / 2));
		// hier erstmal nichts machen
		return true;
	}

	@Override
	public boolean onClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
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