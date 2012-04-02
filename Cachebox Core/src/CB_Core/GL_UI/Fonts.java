package CB_Core.GL_UI;

import CB_Core.Config;
import CB_Core.Log.Logger;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Enthält die benutzten und geladenen GDX-Fonts Benutzte Fonts sind in der Größe von 16 22 wählbar. Der FontFaktor bestimmt aber ob der
 * gewählte font, ein Größerer oder kleinerer font zurück gegeben wird. Bsb GetFont16() gibt bei einem FontFaktor = 1 den Font 16 zurück.
 * bei einem FontFaktor >1 den Font 17 und bei einem Fontfaktor <1 den Font 15. Damit wird der Font nicht mehr Scalliert und ist immer Klar
 * zu erkennen.
 * 
 * @author Longri
 */
public class Fonts
{

	private static int FONT_SIZE_BIG = 18;
	private static int FONT_SIZE_NORMAL = 15;
	private static int FONT_SIZE_SMALL = 13;

	private static BitmapFont big;
	private static BitmapFont normal;
	private static BitmapFont small;
	private static BitmapFont normalBubble;
	private static BitmapFont smallBubble;

	private static BitmapFont fontAB15_out;
	private static BitmapFont fontAB16_out;
	private static BitmapFont fontAB17_out;

	/**
	 * Lädt die verwendeten Bitmap Fonts und berechnet die entsprechenden Größen
	 */
	public static void LoadCalcFonts()
	{
		Logger.LogCat("Fonts => Load");
		String fontPath = Config.settings.SkinFolder.getValue() + "/day/fonts/";
		double density = UiSizes.getScale();

		big = loadScaledFont(fontPath, FONT_SIZE_BIG, density);
		normal = loadScaledFont(fontPath, FONT_SIZE_NORMAL, density);
		small = loadScaledFont(fontPath, FONT_SIZE_SMALL, density);
		normalBubble = loadScaledFont(fontPath, (int) (FONT_SIZE_NORMAL * 0.7), density);
		smallBubble = loadScaledFont(fontPath, (int) (FONT_SIZE_SMALL * 0.7), density);

		fontAB15_out = new BitmapFont(Gdx.files.absolute(fontPath + "15_out.fnt"), Gdx.files.absolute(fontPath + "15_out.png"), false);
		fontAB16_out = new BitmapFont(Gdx.files.absolute(fontPath + "16_out.fnt"), Gdx.files.absolute(fontPath + "16_out.png"), false);
		fontAB17_out = new BitmapFont(Gdx.files.absolute(fontPath + "17_out.fnt"), Gdx.files.absolute(fontPath + "17_out.png"), false);

	}

	private static BitmapFont loadScaledFont(String fontPath, int Size, double density)
	{

		int scaled = (int) (Size * density);

		if (scaled < 6) scaled = 6;
		if (scaled > 44) scaled = 44;

		BitmapFont ret = new BitmapFont(Gdx.files.absolute(fontPath + scaled + ".fnt"), Gdx.files.absolute(fontPath + scaled + ".png"),
				false);
		return ret;
	}

	public static void dispose()
	{
		big.dispose();
		normal.dispose();
		small.dispose();

		fontAB15_out.dispose();
		fontAB16_out.dispose();
		fontAB17_out.dispose();

		big = null;
		normal = null;
		small = null;

		fontAB15_out = null;
		fontAB16_out = null;
		fontAB17_out = null;
	}

	public static BitmapFont getBig()
	{
		return big;
	}

	public static BitmapFont getNormal()
	{
		return normal;
	}

	public static BitmapFont getSmall()
	{
		return small;
	}

	public static BitmapFont getBubbleNormal()
	{
		return normalBubble;
	}

	public static BitmapFont getBubbleSmall()
	{
		return smallBubble;
	}

	public static BitmapFont get16_Out()
	{
		return fontAB16_out;
	}

	public static BitmapFont get15_Out()
	{
		return fontAB15_out;
	}

}
