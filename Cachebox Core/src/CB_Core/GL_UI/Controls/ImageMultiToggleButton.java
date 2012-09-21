package CB_Core.GL_UI.Controls;

import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ImageMultiToggleButton extends MultiToggleButton
{
	private Image image;

	public ImageMultiToggleButton(CB_RectF rec, String name)
	{
		super(rec, name);
		image = new Image(this.ScaleCenter(0.4f), "");
		image.setY(this.height - image.getHeight() - image.getHalfHeight());
		this.addChild(image);
	}

	public void setImage(Drawable drawable)
	{
		image.setDrawable(drawable);
	}

	public void setImageRotation(Float angle)
	{
		mAngle = angle;
		image.setRec(this.ScaleCenter(0.4f * mScale));
		image.setY(this.height - image.getHeight());
		image.setRotate(angle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight() - image.getHalfHeight());
	}

	float mScale = 1f;
	float mAngle = 0;

	public void setImageScale(float scale)
	{
		mScale = scale;
		image.setRec(this.ScaleCenter(0.4f * mScale));
		image.setY(this.height - image.getHeight());
		image.setRotate(mAngle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight() - image.getHalfHeight());
	}

	@Override
	public void resize(float width, float height)
	{
		image.setRec(this.ScaleCenter(0.4f * mScale));
		image.setY(this.height - image.getHeight());
		image.setRotate(mAngle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight() - image.getHalfHeight());
	}
}
