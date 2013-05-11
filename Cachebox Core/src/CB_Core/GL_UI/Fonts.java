package CB_Core.GL_UI;

import java.io.File;
import java.io.FilenameFilter;

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
	private static Color day_darknesColor;
	private static Color day_crossColor;

	private static Color night_fontColor;
	private static Color night_fontColorDisable;
	private static Color night_fontColorHighLight;
	private static Color night_fontColorLink;
	private static Color night_darknesColor;
	private static Color night_crossColor;

	private static BitmapFont compass;
	private static BitmapFont big;
	private static BitmapFont normal;
	private static BitmapFont small;
	private static BitmapFont normalBubble;
	private static BitmapFont smallBubble;

	private static Settings cfg;

	// private static BitmapFont night_fontAB17_out;

	public enum FileType
	{
		absolute, internal;
	}

	public class Settings
	{
		public FileType fileType = FileType.absolute;

		public String SkinFolder;
		public String DefaultSkinFolder;
		public boolean Nightmode = false;
		public int SizeBiggest = 27;
		public int SizeBig = 18;
		public int SizeNormal = 15;
		public int SizeNormalbubble = 14;
		public int SizeSmall = 13;
		public int SizeSmallBubble = 11;
		public String InternalFont = null;
	}

	/**
	 * Lädt die verwendeten Bitmap Fonts und berechnet die entsprechenden Größen
	 */
	public static void loadFonts(Settings config)
	{
		cfg = config;
		Logger.DEBUG("Fonts.loadFonts()");

		// double density = UiSizes.that.getScale();

		String day_skinPath = cfg.SkinFolder + "/day/skin.json";
		Skin day_skin = (cfg.fileType == FileType.absolute) ? new Skin(Gdx.files.absolute(day_skinPath)) : new Skin(
				Gdx.files.internal(day_skinPath));

		String night_skinPath = cfg.SkinFolder + "/night/skin.json";
		Skin night_skin = (cfg.fileType == FileType.absolute) ? new Skin(Gdx.files.absolute(night_skinPath)) : new Skin(
				Gdx.files.internal(night_skinPath));

		day_fontColor = day_skin.getColor("font-color");
		day_fontColorDisable = day_skin.getColor("font-color-disable");
		day_fontColorHighLight = day_skin.getColor("font-color-highlight");
		day_fontColorLink = day_skin.getColor("font-color-link");
		day_darknesColor = day_skin.getColor("darknes");
		day_crossColor = day_skin.getColor("cross");

		night_fontColor = night_skin.getColor("font-color");
		night_fontColorDisable = night_skin.getColor("font-color-disable");
		night_fontColorHighLight = night_skin.getColor("font-color-highlight");
		night_fontColorLink = night_skin.getColor("font-color-link");
		night_darknesColor = night_skin.getColor("darknes");
		night_crossColor = night_skin.getColor("cross");

		FreeTypeFontGenerator generator = null;

		if (cfg.InternalFont != null)
		{
			generator = new FreeTypeFontGenerator(Gdx.files.internal(cfg.InternalFont));
		}
		else
		{
			// get the first found ttf-font
			File skinDir = new File(cfg.SkinFolder);

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

				File defaultSkinDir = new File(cfg.DefaultSkinFolder);
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
			String ttfPath = cfg.DefaultSkinFolder + "/" + FontName;
			Logger.DEBUG("from " + ttfPath);

			generator = new FreeTypeFontGenerator(Gdx.files.absolute(ttfPath));
		}

		double density = UiSizes.that.getScale();

		compass = loadFontFromFile(generator, (int) (cfg.SizeBiggest * density));
		big = loadFontFromFile(generator, (int) (cfg.SizeBig * density));
		normal = loadFontFromFile(generator, (int) (cfg.SizeNormal * density));
		small = loadFontFromFile(generator, (int) (cfg.SizeSmall * density));
		normalBubble = loadFontFromFile(generator, (int) (cfg.SizeNormalbubble * density));
		smallBubble = loadFontFromFile(generator, (int) (cfg.SizeSmallBubble * density));
		generator.dispose();
	}

	static String defaultFontPath;
	static String defaultFontPathNight;

	static String CustomFontPath;
	static String CustomFontPathNight;

	public static Color getFontColor()
	{
		return cfg.Nightmode ? night_fontColor : day_fontColor;
	}

	public static Color getDisableFontColor()
	{
		return cfg.Nightmode ? night_fontColorDisable : day_fontColorDisable;
	}

	public static Color getHighLightFontColor()
	{
		return cfg.Nightmode ? night_fontColorHighLight : day_fontColorHighLight;
	}

	public static Color getLinkFontColor()
	{
		return cfg.Nightmode ? night_fontColorLink : day_fontColorLink;
	}

	public static Color getDarknesColor()
	{
		return cfg.Nightmode ? night_darknesColor : day_darknesColor;
	}

	public static Color getCrossColor()
	{
		return cfg.Nightmode ? night_crossColor : day_crossColor;
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
		String fontPath = "";
		// fonts-Verzeichnis "lokal" im cachebox/skins/small oder ..normal oder christmas
		String path = cfg.SkinFolder.replace("/", fs) + fs + "fonts";
		if (FileIO.DirectoryExists(path))
		{
			// fonts-Verzeichnis "lokal" im cachebox/skins/small oder ..normal oder christmas
			fontPath = path + fs + String.valueOf(scale) + ".fnt";
		}
		else
		{
			// fonts-Verzeichnis "global" im cachebox/skins
			path = cfg.SkinFolder.replace("/", fs) + fs + ".." + fs + "fonts";
			fontPath = path + fs + String.valueOf(scale) + ".fnt";
		}

		// Wenn der font nicht vorberechnet ist, dann wird er generiert
		if (FileIO.FileExists(fontPath))
		{
			Logger.DEBUG("load font for scale " + scale + " from " + fontPath);
			// automatic load of png does not work on Android, so
			// return new BitmapFont(Gdx.files.absolute(fontPath),false);
			Texture tex = new Texture(Gdx.files.absolute(fontPath.replace(".fnt", ".png")));
			tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			TextureRegion region = new TextureRegion(tex);
			return new BitmapFont(Gdx.files.absolute(fontPath), region, false);
		}
		else
		{
			Logger.DEBUG("generate font for scale " + scale);
			return generator.generateFont(scale);
		}
	}

	public static void setNightMode(boolean value)
	{
		cfg.Nightmode = value;
	}

}
