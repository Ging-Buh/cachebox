package CB_Core.GL_UI.Controls;

import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ImageButton extends Button
{
	private Image image;

	public ImageButton(String name)
	{
		super(name);
		this.setText("");
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

	private void chkImagePos()
	{
		// chk image Pos
		CB_RectF thisRec = this.copy();
		thisRec.setPos(0, 0);
		image.setRec(thisRec.ScaleCenter(0.8f * mScale));
	}

	public void setImage(Drawable drawable)
	{
		image.setDrawable(drawable);
		chkImagePos();
	}

	public void setImage(Sprite sprite)
	{
		image.setSprite(sprite);
		chkImagePos();
	}

	public void setImageRotation(Float angle)
	{
		mAngle = angle;
		chkImagePos();
		image.setRotate(angle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
	}

	float mScale = 1f;
	float mAngle = 0;

	public void setImageScale(float scale)
	{
		mScale = scale;
		chkImagePos();
		image.setRotate(mAngle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
	}

	@Override
	public void resize(float width, float height)
	{
		chkImagePos();
		image.setRotate(mAngle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
	}

	@Override
	public void dispose()
	{
		image.dispose();
		super.dispose();
	}

	public void clearImage()
	{
		image.clearImage();
	}
}
