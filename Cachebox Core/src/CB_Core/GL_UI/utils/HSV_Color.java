package CB_Core.GL_UI.utils;

import java.math.BigDecimal;

import com.badlogic.gdx.graphics.Color;

/**
 * Erweitert die LibGdx Color um die HSV Werte
 * 
 * @author Longri
 */
public class HSV_Color extends Color
{
	private float h;
	private float s;
	private float v;

	public HSV_Color(Color color)
	{
		super(color);
	}

	@Override
	public void clamp()
	{
		super.clamp();
		calculateHSV();
	}

	private void calculateHSV()
	{
		float num4;
		float num3 = 0.0f;
		float num2 = Math.min(Math.min(r, g), b);
		float num5 = Math.max(Math.max(r, g), b);
		float num = (num5 - num2);

		if (num5 == 0)
		{
			num4 = 0;
		}
		else
		{
			num4 = (num / num5);
		}

		if (num4 == 0)
		{
			num3 = 0;
		}
		else
		{
			if (r == num5)
			{
				num3 = ((g - b)) / num;
			}
			else if (g == num5)
			{
				num3 = (2 + (b - r)) / num;
			}
			else if (b == num5)
			{
				num3 = (4 + (r - g)) / num;
			}
			num3 = (num3 * 60);
			if (num3 < 0)
			{
				num3 = (num3 + 360);
			}
		}

		this.h = num3;
		this.s = num4;
		this.v = (num5 / 255);

	}

	private void convertHsvToRgb(float h, float s, float v)
	{
		float num = 0f;
		float num2 = 0f;
		float num3 = 0f;

		if (s == 0)
		{
			num = v;
			num2 = v;
			num3 = v;
		}
		else
		{
			if (h == 360)
			{
				h = 0;
			}
			else
			{
				h = (h / 60);
			}

			BigDecimal bd = new BigDecimal(h);
			bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);

			int num4 = bd.intValue();
			float num5 = (h - num4);
			float num6 = (v * (1 - s));
			float num7 = (v * (1 - (s * num5)));
			float num8 = (v * (1 - (s * (1 - num5))));

			switch (num4)
			{
			case 0:
				num = v;
				num2 = num8;
				num3 = num6;
				break;
			case 1:
				num = num7;
				num2 = v;
				num3 = num6;
				break;
			case 2:
				num = num6;
				num2 = v;
				num3 = num8;
				break;
			case 3:
				num = num6;
				num2 = num7;
				num3 = v;
				break;
			case 4:
				num = num8;
				num2 = num6;
				num3 = v;
				break;
			}

			num = v;
			num2 = num6;
			num3 = num7;
		}

		r = num;
		g = num2;
		b = num3;

		// Color.FromArgb(&HFF, CByte((num * 255)), CByte((num2 * 255)), CByte((num3 * 255)))

	}

	public void setHue(float hue)
	{
		convertHsvToRgb(hue, s, v);
	}

	public float getHue()
	{
		return h;
	}
}
