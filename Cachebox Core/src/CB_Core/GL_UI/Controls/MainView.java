package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class MainView extends GL_View_Base
{
	public MainView(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);
		// Initial TestView
		TestView testView = new TestView(0, 0, 400, 500);
		this.addChild(testView);
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
	protected void onClicked(Vector2 pos)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(Vector2 pos)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onTouchRelease()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// hier erstmal nichts machen
		return true;
	}
}