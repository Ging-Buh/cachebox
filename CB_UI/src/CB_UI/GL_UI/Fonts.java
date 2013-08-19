package CB_UI.GL_UI;

import CB_UI.GlobalCore;
import CB_UI.GL_UI.Skin.SkinBase;
import CB_UI.GL_UI.Skin.SkinSettings;
import CB_UI.Math.UiSizes;
import CB_Utils.Log.Logger;
import CB_Utils.Util.FileIO;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

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

	private static SkinSettings cfg;

	// private static BitmapFont night_fontAB17_out;

	/**
	 * Lädt die verwendeten Bitmap Fonts und berechnet die entsprechenden Größen
	 */
	public static void loadFonts(SkinBase skin)
	{
		cfg = skin.getSettings();

		day_fontColor = SkinBase.getDaySkin().getColor("font-color");
		day_fontColorDisable = SkinBase.getDaySkin().getColor("font-color-disable");
		day_fontColorHighLight = SkinBase.getDaySkin().getColor("font-color-highlight");
		day_fontColorLink = SkinBase.getDaySkin().getColor("font-color-link");
		day_darknesColor = SkinBase.getDaySkin().getColor("darknes");
		day_crossColor = SkinBase.getDaySkin().getColor("cross");

		night_fontColor = SkinBase.getNightSkin().getColor("font-color");
		night_fontColorDisable = SkinBase.getNightSkin().getColor("font-color-disable");
		night_fontColorHighLight = SkinBase.getNightSkin().getColor("font-color-highlight");
		night_fontColorLink = SkinBase.getNightSkin().getColor("font-color-link");
		night_darknesColor = SkinBase.getNightSkin().getColor("darknes");
		night_crossColor = SkinBase.getNightSkin().getColor("cross");

		FreeTypeFontGenerator generator = null;

		// get the first found ttf-font

		FileHandle font = null;

		if (cfg.SkinFolder.isDirectory())
		{
			FileHandle[] ttfFonts = cfg.SkinFolder.list();
			for (FileHandle file : ttfFonts)
			{
				if (file.extension().equalsIgnoreCase("ttf"))
				{
					font = file;
					break;
				}
			}
		}

		if (font == null || !font.exists())
		{
			// no skin font found, use default font
			font = GlobalCore.getInternalFileHandle("skins/default/DroidSans-Bold.ttf");
		}

		Logger.DEBUG("Generate scaled Fonts from " + font);
		generator = new FreeTypeFontGenerator(font);

		double density = UiSizes.that.getScale();

		compass = loadFontFromFile(generator, (int) (cfg.SizeBiggest * density));
		big = loadFontFromFile(generator, (int) (cfg.SizeBig * density));
		normal = loadFontFromFile(generator, (int) (cfg.SizeNormal * density));
		small = loadFontFromFile(generator, (int) (cfg.SizeSmall * density));
		normalBubble = loadFontFromFile(generator, (int) (cfg.SizeNormalbubble * density));
		smallBubble = loadFontFromFile(generator, (int) (cfg.SizeSmallBubble * density));
		generator.dispose();
	}

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
		bounds.height = bounds.height - measureNormalCache.getFont().getDescent();
		return bounds;
	}

	public static TextBounds MeasureSmall(String txt)
	{
		if (measureSmallCache == null) measureSmallCache = new BitmapFontCache(Fonts.getSmall());
		TextBounds bounds = measureSmallCache.setText(txt, 0, 0);
		bounds.height = bounds.height - measureSmallCache.getFont().getDescent();
		return bounds;
	}

	public static TextBounds MeasureBig(String txt)
	{
		if (measureBigCache == null) measureBigCache = new BitmapFontCache(Fonts.getBig());
		TextBounds bounds = measureBigCache.setText(txt, 0, 0);
		bounds.height = bounds.height - measureBigCache.getFont().getDescent();
		return bounds;
	}

	public static TextBounds MeasureWrapped(String txt, float width)
	{
		if (measureNormalCache == null) measureNormalCache = new BitmapFontCache(Fonts.getNormal());
		TextBounds bounds = measureNormalCache.setWrappedText(txt, 0, 0, width);
		bounds.height = bounds.height - measureNormalCache.getFont().getDescent();
		return bounds;
	}

	private static BitmapFont loadFontFromFile(FreeTypeFontGenerator generator, int scale)
	{
		String fs = GlobalCore.fs;
		String fontPath = "";
		// fonts-Verzeichnis "lokal" im cachebox/skins/small oder ..normal oder christmas

		if (cfg.SkinFolder.type() == FileType.Absolute)
		{
			String FolderPath = cfg.SkinFolder.path();
			String path = FolderPath.replace("/", fs) + fs + "fnts";
			if (FileIO.DirectoryExists(path))
			{
				// fonts-Verzeichnis "lokal" im cachebox/skins/small oder ..normal oder christmas
				fontPath = path + fs + String.valueOf(scale) + ".fnt";
			}
			else
			{
				// fonts-Verzeichnis "global" im cachebox/skins
				path = FolderPath.replace("/", fs) + fs + ".." + fs + "fnts";
				fontPath = path + fs + String.valueOf(scale) + ".fnt";
			}

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
			BitmapFont ret = new BitmapFont(Gdx.files.absolute(fontPath), region, false);
			return ret;
		}
		else
		{
			Logger.DEBUG("generate font for scale " + scale);
			BitmapFont ret = generator.generateFont(scale);
			TextureRegion region = ret.getRegion();
			Texture tex = region.getTexture();
			tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			return ret;
		}
	}

	public static void setNightMode(boolean value)
	{
		cfg.Nightmode = value;
	}

}
