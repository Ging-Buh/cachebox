package CB_Core.GL_UI;

import CB_Core.Log.Logger;
import CB_Core.Math.GL_UISizes;

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

		fontAB15 = new BitmapFont(Gdx.files.internal("fonts/15.fnt"), Gdx.files.internal("fonts/15_00.png"), false);
		fontAB16 = new BitmapFont(Gdx.files.internal("fonts/16.fnt"), Gdx.files.internal("fonts/16_00.png"), false);
		fontAB17 = new BitmapFont(Gdx.files.internal("fonts/17.fnt"), Gdx.files.internal("fonts/17_00.png"), false);
		fontAB18 = new BitmapFont(Gdx.files.internal("fonts/18.fnt"), Gdx.files.internal("fonts/18_00.png"), false);
		fontAB19 = new BitmapFont(Gdx.files.internal("fonts/19.fnt"), Gdx.files.internal("fonts/19_00.png"), false);
		fontAB20 = new BitmapFont(Gdx.files.internal("fonts/20.fnt"), Gdx.files.internal("fonts/20_00.png"), false);
		fontAB21 = new BitmapFont(Gdx.files.internal("fonts/21.fnt"), Gdx.files.internal("fonts/21_00.png"), false);
		fontAB22 = new BitmapFont(Gdx.files.internal("fonts/22.fnt"), Gdx.files.internal("fonts/22_00.png"), false);
		fontAB23 = new BitmapFont(Gdx.files.internal("fonts/23.fnt"), Gdx.files.internal("fonts/23_00.png"), false);

		fontAB15_out = new BitmapFont(Gdx.files.internal("fonts/15_out.fnt"), Gdx.files.internal("fonts/15_out_00.png"), false);
		fontAB16_out = new BitmapFont(Gdx.files.internal("fonts/16_out.fnt"), Gdx.files.internal("fonts/16_out_00.png"), false);
		fontAB17_out = new BitmapFont(Gdx.files.internal("fonts/17_out.fnt"), Gdx.files.internal("fonts/17_out_00.png"), false);

	}

	public static void dispose()
	{
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

	public static BitmapFont get16()
	{
		if (GL_UISizes.FontFaktor == 1)
		{
			return fontAB16;
		}
		else if (GL_UISizes.FontFaktor > 1)
		{
			return fontAB17;
		}
		return fontAB15;
	}

	public static BitmapFont get17()
	{
		if (GL_UISizes.FontFaktor == 1)
		{
			return fontAB17;
		}
		else if (GL_UISizes.FontFaktor > 1)
		{
			return fontAB18;
		}
		return fontAB16;
	}

	public static BitmapFont get18()
	{
		if (GL_UISizes.FontFaktor == 1)
		{
			return fontAB18;
		}
		else if (GL_UISizes.FontFaktor > 1)
		{
			return fontAB19;
		}
		return fontAB17;
	}

	public static BitmapFont get19()
	{
		if (GL_UISizes.FontFaktor == 1)
		{
			return fontAB19;
		}
		else if (GL_UISizes.FontFaktor > 1)
		{
			return fontAB20;
		}
		return fontAB18;
	}

	public static BitmapFont get20()
	{
		if (GL_UISizes.FontFaktor == 1)
		{
			return fontAB20;
		}
		else if (GL_UISizes.FontFaktor > 1)
		{
			return fontAB21;
		}
		return fontAB19;
	}

	public static BitmapFont get21()
	{
		if (GL_UISizes.FontFaktor == 1)
		{
			return fontAB21;
		}
		else if (GL_UISizes.FontFaktor > 1)
		{
			return fontAB22;
		}
		return fontAB20;
	}

	public static BitmapFont get22()
	{
		if (GL_UISizes.FontFaktor == 1)
		{
			return fontAB22;
		}
		else if (GL_UISizes.FontFaktor > 1)
		{
			return fontAB23;
		}
		return fontAB21;
	}

	public static BitmapFont get16_Out()
	{
		if (GL_UISizes.FontFaktor == 1)
		{
			return fontAB16_out;
		}
		else if (GL_UISizes.FontFaktor > 1)
		{
			return fontAB17_out;
		}
		return fontAB15_out;
	}

}
