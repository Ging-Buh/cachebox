package CB_Core.GL_UI;

import java.io.File;
import java.io.FilenameFilter;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
	private static Color day_fontColorHighLight;
	private static Color day_fontColorLink;

	private static Color night_fontColor;
	private static Color night_fontColorDisable;
	private static Color night_fontColorHighLight;
	private static Color night_fontColorLink;

	private static int FONT_SIZE_COMPASS_DISTANCE = 27;
	private static int FONT_SIZE_BIG = 18;
	private static int FONT_SIZE_NORMAL = 15;
	private static int FONT_SIZE_SMALL = 13;

	private static BitmapFont compass;
	private static BitmapFont big;
	private static BitmapFont normal;
	private static BitmapFont small;
	private static BitmapFont normalBubble;
	private static BitmapFont smallBubble;

	// private static BitmapFont night_fontAB17_out;

	/**
	 * Lädt die verwendeten Bitmap Fonts und berechnet die entsprechenden Größen
	 */
	public static void loadFonts()
	{

		Logger.DEBUG("Fonts.loadFonts()");

		double density = UiSizes.getScale();

		String path = Config.settings.SkinFolder.getValue();

		String day_skinPath = path + "/day/skin.json";
		Skin day_skin = new Skin(Gdx.files.absolute(day_skinPath));

		String night_skinPath = path + "/night/skin.json";
		Skin night_skin = new Skin(Gdx.files.absolute(night_skinPath));

		day_fontColor = day_skin.getColor("font-color");
		day_fontColorDisable = day_skin.getColor("font-color-disable");
		day_fontColorHighLight = day_skin.getColor("font-color-highlight");
		day_fontColorLink = day_skin.getColor("font-color-link");

		night_fontColor = night_skin.getColor("font-color");
		night_fontColorDisable = night_skin.getColor("font-color-disable");
		night_fontColorHighLight = night_skin.getColor("font-color-highlight");
		night_fontColorLink = night_skin.getColor("font-color-link");

		// get the first found ttf-font
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
		Logger.DEBUG("from " + ttfPath);

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.absolute(ttfPath));
		compass = loadFontFromFile(generator, (int) (FONT_SIZE_COMPASS_DISTANCE * density));
		big = loadFontFromFile(generator, (int) (FONT_SIZE_BIG * density));
		normal = loadFontFromFile(generator, (int) (FONT_SIZE_NORMAL * density));
		small = loadFontFromFile(generator, (int) (FONT_SIZE_SMALL * density));
		normalBubble = loadFontFromFile(generator, (int) (FONT_SIZE_NORMAL * density * 0.8));
		smallBubble = loadFontFromFile(generator, (int) (FONT_SIZE_NORMAL * density * 0.8));
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

	public static Color getHighLightFontColor()
	{
		return Config.settings.nightMode.getValue() ? night_fontColorHighLight : day_fontColorHighLight;
	}

	public static Color getLinkFontColor()
	{
		return Config.settings.nightMode.getValue() ? night_fontColorLink : day_fontColorLink;
	}

	public static void dispose()
	{
		compass.dispose();
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

	public static BitmapFont getCompass()
	{
		return compass;
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

	private static BitmapFontCache measureNormalCache;
	private static BitmapFontCache measureSmallCache;
	private static BitmapFontCache measureBigCache;

	//

	public static TextBounds Measure(String txt)
	{
		if (txt == null || txt.equals("")) txt = "text";
		if (measureNormalCache == null) measureNormalCache = new BitmapFontCache(Fonts.getNormal());
		TextBounds bounds = measureNormalCache.setText(txt, 0, 0);

		return bounds;
	}

	public static TextBounds MeasureSmall(String txt)
	{
		if (measureSmallCache == null) measureSmallCache = new BitmapFontCache(Fonts.getSmall());
		TextBounds bounds = measureSmallCache.setText(txt, 0, 0);

		return bounds;
	}

	public static TextBounds MeasureBig(String txt)
	{
		if (measureBigCache == null) measureBigCache = new BitmapFontCache(Fonts.getBig());
		TextBounds bounds = measureBigCache.setText(txt, 0, 0);

		return bounds;
	}

	public static TextBounds MeasureWrapped(String txt, float width)
	{
		if (measureNormalCache == null) measureNormalCache = new BitmapFontCache(Fonts.getNormal());
		TextBounds bounds = measureNormalCache.setWrappedText(txt, 0, 0, width);

		return bounds;
	}

	private static BitmapFont loadFontFromFile(FreeTypeFontGenerator generator, int scale)
	{
		String fs = GlobalCore.fs;
		String path = Config.settings.SkinFolder.getValue() + fs + "fonts";
		String fontPath = null;
		for (int i = 0; i < 46; i++)
		{
			if ((scale - i > 0) && FileIO.FileExists(path + fs + String.valueOf(scale - i) + ".fnt"))
			{
				fontPath = path + fs + String.valueOf(Math.abs(scale - i)) + ".fnt";
				break;
			}
			else if (FileIO.FileExists(path + fs + String.valueOf(scale + i) + ".fnt"))
			{
				fontPath = path + fs + String.valueOf(scale + i) + ".fnt";
				break;
			}
		}
		if (fontPath == null)
		{
			Logger.DEBUG("load font for scale " + scale);
			return generator.generateFont(scale);
		}
		else
		{
			Logger.DEBUG("load font for scale " + scale + " from " + fontPath);
			// automatic load of png does not work on Android, so
			// return new BitmapFont(Gdx.files.absolute(fontPath),false);
			Texture tex = new Texture(Gdx.files.absolute(fontPath.replace(".fnt", ".png")));
			tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			TextureRegion region = new TextureRegion(tex);
			return new BitmapFont(Gdx.files.absolute(fontPath), region, false);
		}
	}
}
