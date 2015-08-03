package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class chkBox extends Button
{

	protected Drawable drawableDisabledChk;
	protected boolean isChk = false;
	protected OnCheckedChangeListener changeListner;

	public chkBox(String name)
	{
		super(new CB_RectF(UI_Size_Base.that.getChkBoxSize()), name);
		this.setClickable(true);
	}

	public chkBox(CB_RectF rec, String name)
	{
		super(rec, name);
		this.setClickable(true);
	}

	@Override
	protected void render(Batch batch)
	{
		if (drawableNormal == null || drawablePressed == null || drawableDisabledChk == null || drawableDisabled == null)
		{
			Initial();
			GL.that.renderOnce();
		}

		if (!isChk && !isDisabled)
		{
			if (drawableNormal != null)
			{
				drawableNormal.draw(batch, 0, 0, getWidth(), getHeight());
			}
		}
		else if (isChk && isDisabled)
		{
			if (drawableDisabledChk != null)
			{
				drawableDisabledChk.draw(batch, 0, 0, getWidth(), getHeight());
			}
		}
		else if (isChk)
		{
			if (drawablePressed != null)
			{
				drawablePressed.draw(batch, 0, 0, getWidth(), getHeight());
			}
		}
		else
		{
			if (drawableDisabled != null)
			{
				drawableDisabled.draw(batch, 0, 0, getWidth(), getHeight());
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
			drawableNormal = SpriteCacheBase.chkOff;
		}
		if (drawablePressed == null)
		{
			drawablePressed = SpriteCacheBase.chkOn;
		}
		if (drawableDisabled == null)
		{
			drawableDisabled = SpriteCacheBase.chkOffDisabled;
		}

		if (drawableDisabledChk == null)
		{
			drawableDisabledChk = SpriteCacheBase.chkOnDisabled;
		}
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		if (!isDisabled)
		{

			GL.that.renderOnce();
		}
		return dragableButton ? false : true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{

		GL.that.renderOnce();
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{

		GL.that.renderOnce();
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
		if (mOnClickListener != null) mOnClickListener.onClick(this, x, y, pointer, button);
		return true;
	}

	public void setChecked(boolean b)
	{
		isChk = b;
		if (changeListner != null) changeListner.onCheckedChanged(this, isChk);
	}

	@Override
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
