package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.utils.ColorDrawable;
import CB_Core.GL_UI.utils.HSV_Color;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;

public class ColorPickerRec extends CB_View_Base
{

	private HSV_Color mColor = new HSV_Color(Color.YELLOW);

	public ColorPickerRec(CB_RectF rec, String Name)
	{
		super(rec, Name);
		colorChanged();
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public void setHue(float hue)
	{
		mColor.setHue(hue);
		colorChanged();
	}

	public void setColor(HSV_Color color)
	{
		mColor = color;
	}

	public void setColor(Color color)
	{
		mColor = new HSV_Color(color);
	}

	private void colorChanged()
	{
		this.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				setBackground(new ColorDrawable(mColor));
			}
		});

	}
}