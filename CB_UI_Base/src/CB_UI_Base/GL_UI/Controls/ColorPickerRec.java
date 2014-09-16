package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.utils.ColorDrawable;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Util.HSV_Color;

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
		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{
				setBackground(new ColorDrawable(mColor));
			}
		});

	}
}
