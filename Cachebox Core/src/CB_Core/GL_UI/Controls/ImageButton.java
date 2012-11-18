package CB_Core.GL_UI.Controls;

import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ImageButton extends Button
{
	private Image image;

	public ImageButton(String name)
	{
		super(name);
		image = new Image(this.ScaleCenter(0.8f), "");
		this.addChild(image);
	}

	public ImageButton(CB_RectF rec, String name)
	{
		super(rec, name);
		image = new Image(this.ScaleCenter(0.8f), "");
		this.addChild(image);
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);
		if (isDisabled)
		{
			image.setColor(new Color(1f, 1f, 1f, 0.5f));
		}
		else
		{
			image.setColor(null);
		}
	}

	public void setImage(Drawable drawable)
	{
		image.setDrawable(drawable);
	}

	public void setImageRotation(Float angle)
	{
		mAngle = angle;
		image.setRec(this.ScaleCenter(0.8f * mScale));
		image.setRotate(angle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
	}

	float mScale = 1f;
	float mAngle = 0;

	public void setImageScale(float scale)
	{
		mScale = scale;
		image.setRec(this.ScaleCenter(0.8f * mScale));
		image.setRotate(mAngle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
	}

	@Override
	public void resize(float width, float height)
	{
		image.setRec(this.ScaleCenter(0.8f * mScale));
		image.setRotate(mAngle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
	}

	@Override
	public void dispose()
	{
		image.dispose();
		super.dispose();
	}
}
