package CB_Core;

import junit.framework.TestCase;
import CB_UI_Base.GL_UI.utils.HSV_Color;

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

}
