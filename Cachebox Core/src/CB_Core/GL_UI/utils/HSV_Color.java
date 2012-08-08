package CB_Core.GL_UI.utils;

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
		float max = (r > g) ? r : g;
		max = (max > b) ? max : b;

		float min = (r < g) ? r : g;
		min = (min < b) ? min : b;

		float delta;

		v = max; // v

		delta = max - min;

		if (max != 0) s = delta / max; // s
		else
		{
			// r = g = b = 0 // s = 0, v is undefined
			s = 0;
			h = 0;
			return;
		}

		if (r == max) h = (g - b) / delta; // between yellow & magenta
		else if (g == max) h = 2 + (b - r) / delta; // between cyan & yellow
		else
			h = 4 + (r - g) / delta; // between magenta & cyan

		h *= 60; // degrees
		if (h < 0) h += 360;

	}

	public void convertHSVtoRGB()
	{
		float hue = h / 60;

		int i = (int) Math.floor(hue);
		float f = hue - i; // factorial part of h
		float p = v * (1 - s);
		float q = v * (1 - s * f);
		float t = v * (1 - s * (1 - f));

		switch (i)
		{
		case 0:
			r = v;
			g = t;
			b = p;
			break;
		case 1:
			r = q;
			g = v;
			b = p;
			break;
		case 2:
			r = p;
			g = v;
			b = t;
			break;
		case 3:
			r = p;
			g = q;
			b = v;
			break;
		case 4:
			r = t;
			g = p;
			b = v;
			break;
		default:
			r = v;
			g = p;
			b = q;
			break;
		}
	}

	public void setHue(float hue)
	{
		h = hue;
		convertHSVtoRGB();
	}

	public float getHue()
	{
		return h;
	}

	public float getSat()
	{
		return s;
	}

	public float getVal()
	{
		return v;
	}

	public void setSat(float sat)
	{
		s = sat;
		convertHSVtoRGB();
	}

	public void setVal(float val)
	{
		v = val;
		convertHSVtoRGB();
	}
}
