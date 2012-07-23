package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ProgressBar extends CB_View_Base
{
	int progress = 0;
	float progressDrawWidth = 0;
	NinePatch progressNinePatch;
	Label label;

	public ProgressBar(CB_RectF rec, String Name)
	{
		super(rec, Name);

		label = new Label(this, "ProgressLabel");
		label.setHAlignment(HAlignment.CENTER);
		label.setVAlignment(VAlignment.CENTER);

		this.addChild(label);

	}

	@Override
	protected void Initial()
	{
		if (nineBackground == null)
		{
			setBackground(new NinePatch(SpriteCache.ToggleBtn.get(0), 16, 16, 16, 16));
		}

		if (progressNinePatch == null)
		{
			progressNinePatch = new NinePatch(SpriteCache.Progress, 15, 15, 15, 15);
		}

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
	}

	public void setProgress(int value, String msg)
	{
		setProgress(value);
		label.setText(msg);
	}

	public void setProgressNinePatch(NinePatch ninePatch)
	{
		progressNinePatch = ninePatch;
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (progressNinePatch == null) Initial();

		if (progressNinePatch != null)
		{
			float patch = progressNinePatch.getLeftWidth() + progressNinePatch.getRightWidth();
			if (progressDrawWidth >= patch)
			{
				progressNinePatch.draw(batch, 0, 0, progressDrawWidth, height);
			}
		}
		super.render(batch);
	}

}
