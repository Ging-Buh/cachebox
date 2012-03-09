package CB_Core.GL_UI;

import CB_Core.Config;
import CB_Core.Log.Logger;

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
	private static BitmapFont fontAB11;
	private static BitmapFont fontAB12;
	private static BitmapFont fontAB13;
	private static BitmapFont fontAB14;
	private static BitmapFont fontAB15;
	private static BitmapFont fontAB16;
	private static BitmapFont fontAB17;
	private static BitmapFont fontAB18;
	private static BitmapFont fontAB19;
	private static BitmapFont fontAB20;
	private static BitmapFont fontAB21;
	private static BitmapFont fontAB22;
	private static BitmapFont fontAB23;

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

		fontAB11 = new BitmapFont(Gdx.files.absolute(fontPath + "11.fnt"), Gdx.files.absolute(fontPath + "11.png"), false);
		fontAB12 = new BitmapFont(Gdx.files.absolute(fontPath + "12.fnt"), Gdx.files.absolute(fontPath + "12.png"), false);
		fontAB13 = new BitmapFont(Gdx.files.absolute(fontPath + "13.fnt"), Gdx.files.absolute(fontPath + "13.png"), false);
		fontAB14 = new BitmapFont(Gdx.files.absolute(fontPath + "14.fnt"), Gdx.files.absolute(fontPath + "14.png"), false);
		fontAB15 = new BitmapFont(Gdx.files.absolute(fontPath + "15.fnt"), Gdx.files.absolute(fontPath + "15.png"), false);
		fontAB16 = new BitmapFont(Gdx.files.absolute(fontPath + "16.fnt"), Gdx.files.absolute(fontPath + "16.png"), false);
		fontAB17 = new BitmapFont(Gdx.files.absolute(fontPath + "17.fnt"), Gdx.files.absolute(fontPath + "17.png"), false);
		fontAB18 = new BitmapFont(Gdx.files.absolute(fontPath + "18.fnt"), Gdx.files.absolute(fontPath + "18.png"), false);
		fontAB19 = new BitmapFont(Gdx.files.absolute(fontPath + "19.fnt"), Gdx.files.absolute(fontPath + "19.png"), false);
		fontAB20 = new BitmapFont(Gdx.files.absolute(fontPath + "20.fnt"), Gdx.files.absolute(fontPath + "20.png"), false);
		fontAB21 = new BitmapFont(Gdx.files.absolute(fontPath + "21.fnt"), Gdx.files.absolute(fontPath + "21.png"), false);
		fontAB22 = new BitmapFont(Gdx.files.absolute(fontPath + "22.fnt"), Gdx.files.absolute(fontPath + "22.png"), false);
		fontAB23 = new BitmapFont(Gdx.files.absolute(fontPath + "23.fnt"), Gdx.files.absolute(fontPath + "23.png"), false);

		fontAB15_out = new BitmapFont(Gdx.files.absolute(fontPath + "15_out.fnt"), Gdx.files.absolute(fontPath + "15_out.png"), false);
		fontAB16_out = new BitmapFont(Gdx.files.absolute(fontPath + "16_out.fnt"), Gdx.files.absolute(fontPath + "16_out.png"), false);
		fontAB17_out = new BitmapFont(Gdx.files.absolute(fontPath + "17_out.fnt"), Gdx.files.absolute(fontPath + "17_out.png"), false);

	}

	public static void dispose()
	{
		fontAB11.dispose();
		fontAB12.dispose();
		fontAB13.dispose();
		fontAB14.dispose();
		fontAB15.dispose();
		fontAB16.dispose();
		fontAB17.dispose();
		fontAB18.dispose();
		fontAB19.dispose();
		fontAB20.dispose();
		fontAB21.dispose();
		fontAB22.dispose();
		fontAB23.dispose();

		fontAB15_out.dispose();
		fontAB16_out.dispose();
		fontAB17_out.dispose();

		fontAB11 = null;
		fontAB12 = null;
		fontAB13 = null;
		fontAB14 = null;
		fontAB15 = null;
		fontAB16 = null;
		fontAB17 = null;
		fontAB18 = null;
		fontAB19 = null;
		fontAB20 = null;
		fontAB21 = null;
		fontAB22 = null;
		fontAB23 = null;

		fontAB15_out = null;
		fontAB16_out = null;
		fontAB17_out = null;
	}

	public static BitmapFont get11()
	{
		return fontAB11;
	}

	public static BitmapFont get12()
	{
		return fontAB12;
	}

	public static BitmapFont get13()
	{
		return fontAB13;
	}

	public static BitmapFont get14()
	{
		return fontAB14;
	}

	public static BitmapFont get15()
	{
		return fontAB15;
	}

	public static BitmapFont get16()
	{
		return fontAB16;
	}

	public static BitmapFont get17()
	{
		return fontAB17;
	}

	public static BitmapFont get18()
	{
		return fontAB18;
	}

	public static BitmapFont get19()
	{
		return fontAB19;
	}

	public static BitmapFont get20()
	{
		return fontAB20;
	}

	public static BitmapFont get21()
	{
		return fontAB21;
	}

	public static BitmapFont get22()
	{
		return fontAB22;
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
