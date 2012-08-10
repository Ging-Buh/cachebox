package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class chkBox extends Button
{

	private Drawable drawableDisabledChk;
	private boolean isChk = false;
	private OnCheckedChangeListener changeListner;

	public chkBox(String name)
	{
		super(new CB_RectF(UiSizes.getChkBoxSize()), name);
		this.isClickable = true;
	}

	public chkBox(CB_RectF rec, String name)
	{
		super(rec, name);
		this.isClickable = true;
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (drawableNormal == null || drawablePressed == null || drawableDisabledChk == null || drawableDisabled == null)
		{
			Initial();
			GL_Listener.glListener.renderOnce(this.getName() + " render");
		}

		if (!isChk && !isDisabled)
		{
			if (drawableNormal != null)
			{
				drawableNormal.draw(batch, 0, 0, width, height);
			}
		}
		else if (isChk && isDisabled)
		{
			if (drawableDisabledChk != null)
			{
				drawableDisabledChk.draw(batch, 0, 0, width, height);
			}
		}
		else if (isChk)
		{
			if (drawablePressed != null)
			{
				drawablePressed.draw(batch, 0, 0, width, height);
			}
		}
		else
		{
			if (drawableDisabled != null)
			{
				drawableDisabled.draw(batch, 0, 0, width, height);
			}
		}

	}

	@Override
	protected void Initial()
	{

		// die einzelnen Hintergründe werden hier anders benutzt
		// drawableNormal= unchecked
		// drawablePressed= checked
		// drawableDisabled= unchecked Disabled
		// drawableDisabledChk = checked Disabled

		if (drawableNormal == null)
		{
			drawableNormal = SpriteCache.chkOff;
		}
		if (drawablePressed == null)
		{
			drawablePressed = SpriteCache.chkOn;
		}
		if (drawableDisabled == null)
		{
			drawableDisabled = SpriteCache.chkOffDisabled;
		}

		if (drawableDisabledChk == null)
		{
			drawableDisabledChk = SpriteCache.chkOnDisabled;
		}
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		if (!isDisabled)
		{

			GL_Listener.glListener.renderOnce(this.getName() + " touchDown");
		}
		return dragableButton ? false : true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{

		GL_Listener.glListener.renderOnce(this.getName() + " Dragged");
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{

		GL_Listener.glListener.renderOnce(this.getName() + " touchUp");
		return dragableButton ? false : true;
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		if (!isDisabled)
		{
			isChk = !isChk;
			if (changeListner != null) changeListner.onCheckedChanged(this, isChk);
		}
		return true;
	}

	public void setChecked(boolean b)
	{
		isChk = b;
		if (changeListner != null) changeListner.onCheckedChanged(this, isChk);
	}

	public void setEnabled(boolean b)
	{
		isDisabled = !b;
	}

	public boolean isChecked()
	{
		return isChk;
	}

	/**
	 * Interface definition for a callback to be invoked when the checked state of a compound button changed.
	 */
	public static interface OnCheckedChangeListener
	{
		/**
		 * Called when the checked state of a compound button has changed.
		 * 
		 * @param buttonView
		 *            The compound button view whose state has changed.
		 * @param isChecked
		 *            The new checked state of buttonView.
		 */
		void onCheckedChanged(chkBox view, boolean isChecked);
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listner)
	{
		changeListner = listner;
	}

}
