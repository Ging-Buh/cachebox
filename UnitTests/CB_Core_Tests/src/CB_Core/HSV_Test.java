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
	}

}
