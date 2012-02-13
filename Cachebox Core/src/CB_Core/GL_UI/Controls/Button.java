package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Button extends GL_View_Base
{

	protected static NinePatch ninePatch;
	protected static NinePatch ninePatchPressed;
	protected static NinePatch ninePatchDisabled;

	private boolean isPressed = false;
	private boolean isDisabled = false;

	public Button(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);

	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (ninePatch == null)
		{
			ninePatch = new NinePatch(SpriteCache.uiAtlas.findRegion("day_btn_default_normal"), 8, 8, 8, 8);
			ninePatchPressed = new NinePatch(SpriteCache.uiAtlas.findRegion("day_btn_default_normal_pressed"), 8, 8, 8, 8);
			ninePatchDisabled = new NinePatch(SpriteCache.uiAtlas.findRegion("day_btn_default_normal_disabled"), 8, 8, 8, 8);
		}

		if (!isPressed && !isDisabled)
		{
			if (ninePatch != null)
			{
				ninePatch.draw(batch, 0, 0, width, height);
			}
		}
		else if (isPressed)
		{
			if (ninePatchPressed != null)
			{
				ninePatchPressed.draw(batch, 0, 0, width, height);
			}
		}
		else
		{
			if (ninePatchDisabled != null)
			{
				ninePatchDisabled.draw(batch, 0, 0, width, height);
			}
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
		if (!isDisabled)
		{
			isPressed = true;
		}
		return true;
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

		isPressed = false;
		return true;
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

	public void enable()
	{
		isDisabled = false;
	}

	public void disable()
	{
		isDisabled = true;
	}

	public boolean isDisabled()
	{
		return isDisabled;
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		// wenn Button disabled ein Behandelt zurück schicken,
		// damit keine weiteren Abfragen durchgereicht werden.
		// Auch wenn dieser Button ein OnClickLÖistner hat.
		if (isDisabled)
		{
			return true;
		}

		else
			return super.click(x, y, pointer, button);
	}
}
