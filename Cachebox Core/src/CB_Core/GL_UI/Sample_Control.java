package CB_Core.GL_UI;

import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * zeigt die Methoden die Überschrieben werden müssen und die, welche Überschrieben werden können. Wobei die Basis Methoden von CB_RecF
 * weggelassen wurden.
 * 
 * @author Longri
 */
public class Sample_Control extends GL_View_Base
{

	// must overrides

	public Sample_Control(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
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

	// can override

	@Override
	public void onStop()
	{

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
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
		// TODO Auto-generated method stub

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void renderWithoutScissor(SpriteBatch batch)
	{
		// TODO Auto-generated method stub

	}
}
