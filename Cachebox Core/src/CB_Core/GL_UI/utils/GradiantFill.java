package CB_Core.GL_UI.utils;

import com.badlogic.gdx.graphics.Color;

public class GradiantFill
{
	public Color Color1;
	public Color Color2;
	public direction Direction;

	public enum direction
	{
		left2reight, right2left, top2bottom, bottom2top
	}

	public GradiantFill(Color color1, Color color2, direction d)
	{
		Color1 = color1;
		Color2 = color2;
		Direction = d;
	}
}
