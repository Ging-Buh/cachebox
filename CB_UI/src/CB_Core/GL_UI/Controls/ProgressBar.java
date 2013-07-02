package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ProgressBar extends CB_View_Base
{
	private int progress = 0;
	private float progressDrawWidth = 0;
	private Drawable progressFill;
	private Label label;
	private String msg = "";

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
			setBackground(SpriteCache.ProgressBack);
		}

		if (progressFill == null)
		{
			progressFill = SpriteCache.ProgressFill;
		}

		GL.that.renderOnce("InitialProgressBar reday");
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public void setProgress(int value)
	{
		progress = value;
		if (progress > 100) progress = 100;
		progressDrawWidth = (width / 100) * progress;
		GL.that.renderOnce("ProgressBar state changed");
	}

	public void setProgress(int value, final String Msg)
	{
		msg = Msg;

		setProgress(value);

		this.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				label.setText(msg);
			}
		});

		GL.that.renderOnce("ProgressBar state changed");
	}

	public void setProgressFill(Drawable drawable)
	{
		progressFill = drawable;
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (progressFill == null) Initial();

		if (progressFill != null)
		{
			float patch = progressFill.getLeftWidth() + progressFill.getRightWidth();
			if (progressDrawWidth >= patch)
			{
				progressFill.draw(batch, 0, 0, progressDrawWidth, height);
			}
		}
		super.render(batch);
	}

	public void setText(String message)
	{
		msg = message;
		label.setText(msg);
	}

}
