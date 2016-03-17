/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.GL_UI;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import CB_UI_Base.Global;
import CB_UI_Base.GL_UI.Skin.SkinBase;
import CB_UI_Base.GL_UI.Skin.SkinSettings;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;

/**
 * Enthält die benutzten und geladenen GDX-Fonts
 * 
 * @author Longri
 */
public class Fonts {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Fonts.class);
	public static final String DEFAULT_CHARACTER = getCyrilCharSet();

	static String getCyrilCharSet() {
		int CharSize = 0x04ff - 0x0400;
		char[] cyril = new char[CharSize + 1];
		for (int i = 0x0400; i < 0x04ff + 1; i++) {
			cyril[i - 0x0400] = (char) i;
		}
		return FreeTypeFontGenerator.DEFAULT_CHARS + String.copyValueOf(cyril) + "—–" +"ŐőŰű√";
	}

	private static BitmapFont compass;
	private static BitmapFont big;
	private static BitmapFont normal;
	private static BitmapFont small;
	private static BitmapFont normalBubble;
	private static BitmapFont smallBubble;

	private static SkinSettings cfg;

	/**
	 * Lädt die verwendeten Bitmap Fonts und berechnet die entsprechenden Größen
	 */
	public static void loadFonts(SkinBase skin) {

		cfg = skin.getSettings();
		COLOR.loadColors(skin);
		FreeTypeFontGenerator generator = null;

		// get the first found ttf-font

		FileHandle font = null;

		if (cfg.SkinFolder.isDirectory()) {
			FileHandle[] ttfFonts = cfg.SkinFolder.list();
			for (FileHandle file : ttfFonts) {
				if (file.extension().equalsIgnoreCase("ttf")) {
					font = file;
					break;
				}
			}
		}

		if (font == null || !font.exists()) {
			// no skin font found, use default font
			font = Global.getInternalFileHandle("skins/default/DroidSans-Bold.ttf");
		}

		Log.debug(log, "Generate scaled Fonts from " + font);
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

	public static void dispose() {
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

	public static BitmapFont getCompass() {
		return compass;
	}

	public static BitmapFont getBig() {
		return big;
	}

	public static BitmapFont getNormal() {
		return normal;
	}

	public static BitmapFont getSmall() {
		return small;
	}

	public static BitmapFont getBubbleNormal() {
		return normalBubble;
	}

	public static BitmapFont getBubbleSmall() {
		return smallBubble;
	}

	private static BitmapFontCache measureNormalCache;
	private static BitmapFontCache measureSmallCache;
	private static BitmapFontCache measureBigCache;

	//

	public static GlyphLayout Measure(String txt) {
		if (txt == null || txt.equals(""))
			txt = "text";
		if (measureNormalCache == null)
			measureNormalCache = new BitmapFontCache(Fonts.getNormal());
		GlyphLayout bounds = measureNormalCache.setText(txt, 0, 0);
		bounds.height = bounds.height - measureNormalCache.getFont().getDescent();
		return bounds;
	}

	public static GlyphLayout MeasureSmall(String txt) {

		if (txt == null || txt.equals(""))
			txt = "text";

		if (measureSmallCache == null)
			measureSmallCache = new BitmapFontCache(Fonts.getSmall());
		GlyphLayout bounds = measureSmallCache.setText(txt, 0, 0);
		bounds.height = bounds.height - measureSmallCache.getFont().getDescent();
		return bounds;
	}

	public static GlyphLayout MeasureBig(String txt) {
		if (txt == null || txt.equals(""))
			txt = "text";
		if (measureBigCache == null)
			measureBigCache = new BitmapFontCache(Fonts.getBig());
		GlyphLayout bounds = measureBigCache.setText(txt, 0, 0);
		bounds.height = bounds.height - measureBigCache.getFont().getDescent();
		return bounds;
	}

	public static GlyphLayout MeasureWrapped(String txt, float width) {
		if (txt == null || txt.equals(""))
			txt = "text";
		if (measureNormalCache == null)
			measureNormalCache = new BitmapFontCache(Fonts.getNormal());
		GlyphLayout bounds = measureNormalCache.setText(txt, 0, 0, width, 0, true);//measureNormalCache.setWrappedText(txt, 0, 0, width);
		bounds.height = bounds.height - measureNormalCache.getFont().getDescent();
		return bounds;
	}

	private static BitmapFont loadFontFromFile(FreeTypeFontGenerator generator, int scale) {
		String fs = Global.fs;
		String fontPath = "";
		// fonts-Verzeichnis "lokal" im cachebox/skins/small oder ..normal oder christmas

		if (cfg.SkinFolder.type() == FileType.Absolute) {
			String FolderPath = cfg.SkinFolder.path();
			String path = FolderPath.replace("/", fs) + fs + "fnts";
			if (FileIO.DirectoryExists(path)) {
				// fonts-Verzeichnis "lokal" im cachebox/skins/small oder ..normal oder christmas
				fontPath = path + fs + String.valueOf(scale) + ".fnt";
			} else {
				// fonts-Verzeichnis "global" im cachebox/skins
				path = FolderPath.replace("/", fs) + fs + ".." + fs + "fnts";
				fontPath = path + fs + String.valueOf(scale) + ".fnt";
			}

		}

		// Wenn der font nicht vorberechnet ist, dann wird er generiert
		if (FileIO.FileExists(fontPath)) {
			Log.debug(log, "load font for scale " + scale + " from " + fontPath);
			// automatic load of png does not work on Android, so
			// return new BitmapFont(Gdx.files.absolute(fontPath),false);
			Texture tex = new Texture(Gdx.files.absolute(fontPath.replace(".fnt", ".png")));
			tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			TextureRegion region = new TextureRegion(tex);
			BitmapFont ret = new BitmapFont(Gdx.files.absolute(fontPath), region, false);
			return ret;
		} else {
			Log.debug(log, "generate font for scale " + scale);
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = scale;
			parameter.characters = DEFAULT_CHARACTER;
			BitmapFont ret = generator.generateFont(parameter);
			TextureRegion region = ret.getRegion();
			Texture tex = region.getTexture();
			tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			return ret;
		}
	}

	public static void setNightMode(boolean value) {
		cfg.Nightmode = value;
	}

}
