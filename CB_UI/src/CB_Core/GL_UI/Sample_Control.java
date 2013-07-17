package CB_Core.GL_UI;

import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * zeigt die Methoden die �berschrieben werden m�ssen und die, welche �berschrieben werden k�nnen. Wobei die Basis Methoden von CB_RecF
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
	}

	@Override
	public void render(SpriteBatch batch)
	{
	}

	@Override
	public void onResized(CB_RectF rec)
	{
	}

	// can override

	@Override
	public void onStop()
	{
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		return false;
	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		return false;
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{
	}

	@Override
	protected void SkinIsChanged()
	{
	}

}