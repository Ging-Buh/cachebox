package CB_Core.GL_UI;

import java.io.File;
import java.io.FilenameFilter;

import CB_Core.Config;
import CB_Core.Log.Logger;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Enthält die benutzten und geladenen GDX-Fonts
 * 
 * @author Longri
 */
public class Fonts
{

	private static Color day_fontColor;
	private static Color day_fontColorDisable;

	private static Color night_fontColor;
	private static Color night_fontColorDisable;

	private static int FONT_SIZE_BIG = 18;
	private static int FONT_SIZE_NORMAL = 15;
	private static int FONT_SIZE_SMALL = 13;

	private static BitmapFont big;
	private static BitmapFont normal;
	private static BitmapFont small;
	private static BitmapFont normalBubble;
	private static BitmapFont smallBubble;

	// private static BitmapFont night_fontAB17_out;

	/**
	 * Lädt die verwendeten Bitmap Fonts und berechnet die entsprechenden Größen
	 */
	public static void LoadCalcFonts()
	{

		Logger.DEBUG("Fonts.LoadCalcFonts()");

		double density = UiSizes.getScale();

		String path = Config.settings.SkinFolder.getValue();

		String day_skinPath = path + "\\day\\skin.json";
		Skin day_skin = new Skin(Gdx.files.absolute(day_skinPath));
		day_fontColor = day_skin.getColor("font-color");
		day_fontColorDisable = day_skin.getColor("font-color-disable");

		String night_skinPath = path + "\\night\\skin.json";
		Skin night_skin = new Skin(Gdx.files.absolute(night_skinPath));
		night_fontColor = night_skin.getColor("font-color");
		night_fontColorDisable = night_skin.getColor("font-color-disable");

		// get the first founded ttf-font
		File skinDir = new File(path);
		String FontName = null;
		String[] ttfFonts = skinDir.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File arg0, String arg1)
			{
				if (arg1.endsWith(".ttf")) return true;
				return false;
			}
		});

		if (ttfFonts != null && ttfFonts.length > 0 && ttfFonts[0] != null) FontName = ttfFonts[0];
		if (FontName == null)
		{
			// no skin font found, use default font
			path = Config.settings.SkinFolder.getDefaultValue();
			File defaultSkinDir = new File(path);
			String[] defaultTtfFonts = defaultSkinDir.list(new FilenameFilter()
			{
				@Override
				public boolean accept(File arg0, String arg1)
				{
					if (arg1.endsWith(".ttf")) return true;
					return false;
				}
			});
			FontName = defaultTtfFonts[0];
		}

		String ttfPath = path + "/" + FontName;
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.absolute(ttfPath));

		big = generator.generateFont((int) (FONT_SIZE_BIG * density));
		normal = generator.generateFont((int) (FONT_SIZE_NORMAL * density));
		small = generator.generateFont((int) (FONT_SIZE_SMALL * density));
		normalBubble = generator.generateFont((int) (FONT_SIZE_NORMAL * density * 0.8));
		smallBubble = generator.generateFont((int) (FONT_SIZE_NORMAL * density * 0.8));

		generator.dispose();
	}

	static String defaultFontPath;
	static String defaultFontPathNight;

	static String CustomFontPath;
	static String CustomFontPathNight;

	public static Color getFontColor()
	{
		return Config.settings.nightMode.getValue() ? night_fontColor : day_fontColor;
	}

	public static Color getDisableFontColor()
	{
		return Config.settings.nightMode.getValue() ? night_fontColorDisable : day_fontColorDisable;
	}

	public static void dispose()
	{
		big.dispose();
		normal.dispose();
		small.dispose();
		normalBubble.dispose();
		smallBubble.dispose();

		big = null;
		normal = null;
		small = null;
		normalBubble = null;
		smallBubble = null;

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
