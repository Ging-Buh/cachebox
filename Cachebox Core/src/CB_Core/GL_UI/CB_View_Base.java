package CB_Core.GL_UI;

import CB_Core.GL_UI.interfaces.ViewOptionsMenu;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CB_View_Base extends GL_View_Base implements ViewOptionsMenu
{

	// # Constructors

	public CB_View_Base(String Name)
	{
		super(Name);
	}

	/**
	 * Constructor fuer ein neues GL_View_Base mit Angabe der linken unteren Ecke und der Hoehe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public CB_View_Base(float X, float Y, float Width, float Height, CharSequence Name)
	{
		super(X, Y, Width, Height, Name);
	}

	public CB_View_Base(float X, float Y, float Width, float Height, GL_View_Base Parent, CharSequence Name)
	{
		super(X, Y, Width, Height, Parent, Name);
	}

	public CB_View_Base(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
	}

	public CB_View_Base(CB_RectF rec, GL_View_Base Parent, CharSequence Name)
	{
		super(rec, Parent, Name);
	}

	public CB_View_Base(SizeF size, CharSequence Name)
	{
		super(size, Name);
	}

	@Override
	public int GetMenuId()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void OnShow()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void OnHide()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void OnFree()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int GetContextMenuId()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
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
