package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ProgressBar extends CB_View_Base
{
	private int progress = 0;
	protected float progressDrawWidth = 0;
	private Drawable progressFill, progressFillDisabled;
	private final Label label;
	private String msg = "";
	private boolean isDisabled = false;

	public ProgressBar(CB_RectF rec, String Name)
	{
		super(rec, Name);

		label = new Label(this, "ProgressLabel");
		label.setHAlignment(HAlignment.CENTER);

		this.addChild(label);

	}

	@Override
	protected void Initial()
	{
		if (drawableBackground == null)
		{
			setBackground(SpriteCacheBase.ProgressBack);
		}

		if (progressFill == null)
		{
			progressFill = SpriteCacheBase.ProgressFill;
		}

		if (progressFillDisabled == null)
		{
			progressFillDisabled = SpriteCacheBase.ProgressDisabled;
		}

		GL.that.renderOnce();
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	/**
	 * @param value
	 * @return the pos of Progress end
	 */
	public float setProgress(int value)
	{
		progress = value;
		if (progress > 100) progress = 100;
		progressDrawWidth = (getWidth() / 100) * progress;
		GL.that.renderOnce();
		return progressDrawWidth;
	}

	/**
	 * @param value
	 * @param Msg
	 * @return the pos of Progress end
	 */
	public float setProgress(int value, final String Msg)
	{
		msg = Msg;

		float ret = setProgress(value);

		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{
				label.setText(msg);
			}
		});

		return ret;
	}

	public void setProgressFill(Drawable drawable)
	{
		progressFill = drawable;
	}

	public void setProgressFillDisabled(Drawable drawable)
	{
		progressFillDisabled = drawable;
	}

	@Override
	protected void render(Batch batch)
	{
		if (progressFill == null || progressFillDisabled == null) Initial();

		if (!isDisabled)
		{
			if (progressFill != null)
			{
				float patch = progressFill.getLeftWidth() + progressFill.getRightWidth();
				if (progressDrawWidth >= patch)
				{
					progressFill.draw(batch, 0, 0, progressDrawWidth, getHeight());
				}
			}
		}
		else
		{
			if (progressFillDisabled != null)
			{
				float patch = progressFillDisabled.getLeftWidth() + progressFillDisabled.getRightWidth();
				if (progressDrawWidth >= patch)
				{
					progressFillDisabled.draw(batch, 0, 0, progressDrawWidth, getHeight());
				}
			}
		}
		super.render(batch);
	}

	public void setText(String message)
	{
		msg = message;
		label.setText(msg);
	}

	public int getProgress()
	{
		return progress;
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

}
