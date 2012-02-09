package CB_Core.GL_UI;

import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * zeigt die Methoden die Überschrieben werden müssen und die, welche Überschrieben werden können. Wobei die Basis Methoden von CB_RecF
 * weggelassen wurden.
 * 
 * @author Longri
 */
public class Sample_Control extends GL_View_Base
{

	// must overrides

	public Sample_Control(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);
		// TODO Auto-generated constructor stub
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

	// can override

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchMoved(int x, int y)
	{
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
		return false;
	}

	@Override
	public void onStop()
	{

	}

	@Override
	public boolean touchDown(int x, int y, int pointer)
	{
		return false;
	}

	@Override
	public boolean tap(int x, int y, int count)
	{
		return false;
	}

	@Override
	public boolean longPress(int x, int y)
	{
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY)
	{
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
