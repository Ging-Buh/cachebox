package CB_Core;

import junit.framework.TestCase;

import org.junit.Test;

import CB_Utils.Util.HSV_Color;

public class HSV_Test extends TestCase
{

	public void testHSV2Int()
	{
		HSV_Color color = new HSV_Color(127, 127, 0, 0);

		int integer = color.toInt();

		HSV_Color sColor = new HSV_Color(integer);

		System.out.print(sColor.a);

		int a = 0;
		int r = 0;
		int g = 0;
		int b = 0;

		for (; a <= 255; a += 2)
		{
			for (; r <= 255; r += 2)
			{
				for (; g <= 255; g += 2)
				{
					for (; b <= 255; b += 2)
					{
						HSV_Color testcolor = new HSV_Color(a, r, g, b);
						int intColor = testcolor.toInt();
						HSV_Color compareColor = new HSV_Color(intColor);

						assertEquals(testcolor, compareColor);
					}
					b = 0;
				}
				g = 0;
			}
			r = 0;
		}

	}

	@Test
	public void testHSV_String_Constructor()
	{
		HSV_Color c = new HSV_Color("bfbfbfff");
		HSV_Color c2 = new HSV_Color(0.7490196f, 0.7490196f, 0.7490196f, 1f);
		assertEquals(c2, c);
	}

}
