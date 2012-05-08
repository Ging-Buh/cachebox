package CB_Core.GL_UI;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.Log.Logger;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Enthält die benutzten und geladenen GDX-Fonts
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

		double density = UiSizes.getScale();

		big = loadScaledFont(FONT_SIZE_BIG, density);
		normal = loadScaledFont(FONT_SIZE_NORMAL, density);
		small = loadScaledFont(FONT_SIZE_SMALL, density);
		normalBubble = loadScaledFont((int) (FONT_SIZE_NORMAL * 0.7), density);
		smallBubble = loadScaledFont((int) (FONT_SIZE_SMALL * 0.7), density);

		fontAB15_out = new BitmapFont(Gdx.files.absolute(defaultFontPath + "15_out.fnt"),
				Gdx.files.absolute(defaultFontPath + "15_out.png"), false);
		fontAB16_out = new BitmapFont(Gdx.files.absolute(defaultFontPath + "16_out.fnt"),
				Gdx.files.absolute(defaultFontPath + "16_out.png"), false);
		fontAB17_out = new BitmapFont(Gdx.files.absolute(defaultFontPath + "17_out.fnt"),
				Gdx.files.absolute(defaultFontPath + "17_out.png"), false);

	}

	static String defaultFontPath;
	static String defaultFontPathNight;

	static String costumFontPath;
	static String costumFontPathNight;

	private static BitmapFont loadScaledFont(int Size, double density)
	{

		String fontPath = null;

		String path = Config.settings.SkinFolder.getValue();

		costumFontPath = path + "/day/fonts/";
		costumFontPathNight = path + "/night/fonts/";

		String defaultPath = path;
		int pos = defaultPath.lastIndexOf("/");
		defaultPath = defaultPath.substring(0, pos) + "/default";

		defaultFontPath = defaultPath + "/day/fonts/";
		defaultFontPathNight = defaultPath + "/night/fonts/";

		if (Config.settings.nightMode.getValue())
		{
			if (FileIO.FileExists(costumFontPathNight)) fontPath = costumFontPathNight;
			if (fontPath == null) if (FileIO.FileExists(defaultFontPathNight)) fontPath = defaultFontPathNight;
		}
		else
		{
			if (FileIO.FileExists(costumFontPath)) fontPath = costumFontPath;
		}

		if (fontPath == null)
		{
			fontPath = defaultFontPath;
			;
		}

		int scaled = (int) (Size * density);

		if (scaled < 6) scaled = 6;
		if (scaled > 44) scaled = 44;

		// BitmapFont ret = new BitmapFont(Gdx.files.absolute(fontPath + scaled + ".fnt"), Gdx.files.absolute(fontPath + scaled + ".png"),
		// false);

		Texture tex = new Texture(Gdx.files.absolute(fontPath + scaled + ".png"));
		// tex.setFilter(TextureFilter.Nearest, TextureFilter.Linear);
		tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion region = new TextureRegion(tex);
		BitmapFont ret = new BitmapFont(Gdx.files.absolute(fontPath + scaled + ".fnt"), region, false);

		return ret;
	}

	public static void dispose()
	{
		big.dispose();
		normal.dispose();
		small.dispose();
		normalBubble.dispose();
		smallBubble.dispose();

		fontAB15_out.dispose();
		fontAB16_out.dispose();
		fontAB17_out.dispose();

		big = null;
		normal = null;
		small = null;
		normalBubble = null;
		smallBubble = null;

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
