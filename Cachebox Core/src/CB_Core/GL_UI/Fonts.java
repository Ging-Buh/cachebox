package CB_Core.GL_UI;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.Log.Logger;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
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

	private static BitmapFont night_big;
	private static BitmapFont night_normal;
	private static BitmapFont night_small;
	private static BitmapFont night_normalBubble;
	private static BitmapFont night_smallBubble;

	private static BitmapFont night_fontAB15_out;
	private static BitmapFont night_fontAB16_out;

	// private static BitmapFont night_fontAB17_out;

	/**
	 * Lädt die verwendeten Bitmap Fonts und berechnet die entsprechenden Größen
	 */
	public static void LoadCalcFonts()
	{

		Logger.DEBUG("Fonts.LoadCalcFonts()");

		double density = UiSizes.getScale();

		big = loadScaledFont(FONT_SIZE_BIG, density, false);
		normal = loadScaledFont(FONT_SIZE_NORMAL, density, false);
		small = loadScaledFont(FONT_SIZE_SMALL, density, false);
		normalBubble = loadScaledFont((int) (FONT_SIZE_NORMAL * 0.7), density, false);
		smallBubble = loadScaledFont((int) (FONT_SIZE_SMALL * 0.7), density, false);

		fontAB15_out = loadScaledFont("15_out", false);
		fontAB16_out = loadScaledFont("16_out", false);
		fontAB17_out = loadScaledFont("17_out", false);

		night_big = loadScaledFont(FONT_SIZE_BIG, density, true);
		night_normal = loadScaledFont(FONT_SIZE_NORMAL, density, true);
		night_small = loadScaledFont(FONT_SIZE_SMALL, density, true);
		night_normalBubble = loadScaledFont((int) (FONT_SIZE_NORMAL * 0.7), density, true);
		night_smallBubble = loadScaledFont((int) (FONT_SIZE_SMALL * 0.7), density, true);

		night_fontAB15_out = loadScaledFont("15_out", true);
		night_fontAB16_out = loadScaledFont("16_out", true);
		// night_fontAB17_out = loadScaledFont("17_out", true);
	}

	static String defaultFontPath;
	static String defaultFontPathNight;

	static String CustomFontPath;
	static String CustomFontPathNight;

	private static BitmapFont loadScaledFont(int Size, double density, boolean night)
	{
		int scaled = (int) (Size * density);

		if (scaled < 6) scaled = 6;
		if (scaled > 44) scaled = 44;

		String strScaled = String.valueOf(scaled);

		return loadScaledFont(strScaled, night);
	}

	private static BitmapFont loadScaledFont(String strScaled, boolean night)
	{

		String fontPath = null;

		String path = Config.settings.SkinFolder.getValue();

		CustomFontPath = path + "/day/fonts/" + strScaled + ".png";
		CustomFontPathNight = path + "/night/fonts/" + strScaled + ".png";

		String defaultPath = path;
		int pos = defaultPath.lastIndexOf("/");
		defaultPath = defaultPath.substring(0, pos) + "/default";

		defaultFontPath = defaultPath + "/day/fonts/" + strScaled + ".png";
		defaultFontPathNight = defaultPath + "/night/fonts/" + strScaled + ".png";

		if (night)
		{
			if (FileIO.FileExists(CustomFontPathNight)) fontPath = CustomFontPathNight;
			if (fontPath == null) if (FileIO.FileExists(defaultFontPathNight)) fontPath = defaultFontPathNight;
		}
		else
		{
			if (FileIO.FileExists(CustomFontPath)) fontPath = CustomFontPath;
		}

		if (fontPath == null)
		{
			fontPath = defaultFontPath;
			;
		}

		Texture tex = new Texture(Gdx.files.absolute(fontPath));
		tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		TextureRegion region = new TextureRegion(tex);

		fontPath = fontPath.replace(".png", ".fnt");

		BitmapFont ret = new BitmapFont(Gdx.files.absolute(fontPath), region, false);

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
		return Config.settings.nightMode.getValue() ? night_big : big;
	}

	public static BitmapFont getNormal()
	{
		return Config.settings.nightMode.getValue() ? night_normal : normal;
	}

	public static BitmapFont getSmall()
	{
		return Config.settings.nightMode.getValue() ? night_small : small;
	}

	public static BitmapFont getBubbleNormal()
	{
		return Config.settings.nightMode.getValue() ? night_normalBubble : normalBubble;
	}

	public static BitmapFont getBubbleSmall()
	{
		return Config.settings.nightMode.getValue() ? night_smallBubble : smallBubble;
	}

	public static BitmapFont get16_Out()
	{
		return Config.settings.nightMode.getValue() ? night_fontAB16_out : fontAB16_out;
	}

	public static BitmapFont get15_Out()
	{
		return Config.settings.nightMode.getValue() ? night_fontAB15_out : fontAB15_out;
	}

	private static BitmapFontCache mesureNormalCache;
	private static BitmapFontCache mesureSmallCache;
	private static BitmapFontCache mesureBigCache;

	public static TextBounds Mesure(String txt)
	{
		if (txt == null || txt.equals("")) txt = "text";
		if (mesureNormalCache == null) mesureNormalCache = new BitmapFontCache(Fonts.getNormal());
		TextBounds bounds = mesureNormalCache.setText(txt, 0, 0);

		return bounds;
	}

	public static TextBounds MesureSmall(String txt)
	{
		if (mesureSmallCache == null) mesureSmallCache = new BitmapFontCache(Fonts.getSmall());
		TextBounds bounds = mesureSmallCache.setText(txt, 0, 0);

		return bounds;
	}

	public static TextBounds MesureBig(String txt)
	{
		if (mesureBigCache == null) mesureBigCache = new BitmapFontCache(Fonts.getBig());
		TextBounds bounds = mesureBigCache.setText(txt, 0, 0);

		return bounds;
	}

	public static TextBounds MesureWrapped(String txt, float width)
	{
		if (mesureNormalCache == null) mesureNormalCache = new BitmapFontCache(Fonts.getNormal());
		TextBounds bounds = mesureNormalCache.setWrappedText(txt, 0, 0, width);

		return bounds;
	}
}
