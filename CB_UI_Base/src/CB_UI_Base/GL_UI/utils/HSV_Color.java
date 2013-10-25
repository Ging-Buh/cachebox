package CB_UI_Base.GL_UI.utils;

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
		clamp();
	}

	/**
	 * Constructor for color as Hex String WITHOUT #
	 * 
	 * @param hex
	 */
	public HSV_Color(String hex)
	{
		if (hex.length() != 6 || hex.length() != 8) throw new IllegalArgumentException("wrong argument: " + hex);

		int values = hex.length() / 2;

		int[] ret = new int[values];
		for (int i = 0; i < values; i++)
		{
			ret[i] = hexToInt(hex.charAt(i * 2), hex.charAt(i * 2 + 1));
		}

		if (values == 4)
		{
			a = ret[0] / 255f;
			r = ret[1] / 255f;
			g = ret[2] / 255f;
			b = ret[3] / 255f;
		}
		else
		{
			a = 1f;
			r = ret[0] / 255f;
			g = ret[1] / 255f;
			b = ret[2] / 255f;
		}

		clamp();

	}

	/**
	 * Constructor
	 * 
	 * @param a
	 *            Alpha 0-255
	 * @param r
	 *            Red 0-255
	 * @param g
	 *            Green 0-255
	 * @param b
	 *            Blue 0-255
	 */
	public HSV_Color(int a, int r, int g, int b)
	{
		super(r / 255f, g / 255f, b / 255f, a / 255f);
		clamp();
	}

	public HSV_Color(int color)
	{
		a = ((color & 0xff000000) >>> 24) / 255f;
		r = ((color & 0xff0000) >>> 16) / 255f;
		g = ((color & 0xff00) >>> 8) / 255f;
		b = (color & 0xff) / 255f;
		clamp();
	}

	private int hexToInt(char c1, char c2)
	{
		String s = String.valueOf(c1) + String.valueOf(c2);
		int z = Integer.parseInt(s, 16);

		return z;
	}

	@Override
	public Color clamp()
	{
		Color ret = super.clamp();

		calculateHSV();
		return ret;

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

	public int toInt()
	{
		return (((int) (a * 255f)) & 0xff) << 24 | (((int) (r * 255f)) & 0xff) << 16 | (((int) (g * 255f)) & 0xff) << 8
				| (((int) (b * 255f)) & 0xff);
	}
}
