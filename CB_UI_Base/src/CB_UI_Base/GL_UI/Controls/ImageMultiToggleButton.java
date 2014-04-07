package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ImageMultiToggleButton extends MultiToggleButton
{
	private final Image image;

	public ImageMultiToggleButton(CB_RectF rec, String name)
	{
		super(rec, name);
		image = new Image(this.ScaleCenter(0.4f), "");
		image.setY(this.getHeight() - image.getHeight() - image.getHalfHeight());
		this.addChild(image);
	}

	public void setImage(Drawable drawable)
	{
		image.setDrawable(drawable);
	}

	public void setImageRotation(Float angle)
	{
		mAngle = angle;
		CB_RectF imgRec = this.copy();
		imgRec.setPos(0, 0);
		image.setRec(imgRec.ScaleCenter(0.4f * mScale));
		image.setY(this.getHeight() - image.getHeight() - image.getHalfHeight());
		image.setRotate(angle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight() - image.getHalfHeight());
	}

	float mScale = 1f;
	float mAngle = 0;

	public void setImageScale(float scale)
	{
		mScale = scale;
		CB_RectF imgRec = this.copy();
		imgRec.setPos(0, 0);
		image.setRec(imgRec.ScaleCenter(0.4f * mScale));
		image.setY(this.getHeight() - image.getHeight() - image.getHalfHeight());
		image.setRotate(mAngle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight() - image.getHalfHeight());
	}

	@Override
	public void resize(float width, float height)
	{
		super.resize(width, height);

		CB_RectF imgRec = this.copy();
		imgRec.setPos(0, 0);
		image.setRec(imgRec.ScaleCenter(0.4f * mScale));
		image.setY(this.getHeight() - image.getHeight() - image.getHalfHeight());
		image.setRotate(mAngle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight() - image.getHalfHeight());
	}

	@Override
	public void render(Batch batch)
	{
		super.render(batch);
	}

	@Override
	public void renderChilds(final Batch batch, ParentInfo parentInfo)
	{
		super.renderChilds(batch, parentInfo);
	}
}
