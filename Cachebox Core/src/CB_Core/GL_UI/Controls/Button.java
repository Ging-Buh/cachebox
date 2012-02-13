package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Button extends GL_View_Base
{

	NinePatch ninePatch;

	public Button(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);

	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (ninePatch == null)
		{
			ninePatch = new NinePatch(new Texture(Gdx.files.internal("9patch/day_btn_default_normal.9.png")), 8, 8, 8, 8);
		}

		if (ninePatch != null)
		{
			ninePatch.draw(batch, 0, 0, width, height);
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
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
