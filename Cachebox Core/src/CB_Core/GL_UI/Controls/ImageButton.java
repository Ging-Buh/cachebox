package CB_Core.GL_UI.Controls;

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

	@Override
	public void setImage(Drawable drawable)
	{
		image.setDrawable(drawable);
	}

	public void setDrawable(Drawable drawable)
	{
		image.setDrawable(drawable);
	}

	public void setImageRotation(Float angle)
	{
		image.setRotate(angle);
		image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
	}

	public void setImageScale(float scale)
	{
		image.setScale(scale);
	}

	@Override
	public void resize(float width, float height)
	{
		image.setRec(this.ScaleCenter(0.8f));
	}
}
