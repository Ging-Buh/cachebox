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
package de.droidcachebox.gdx;

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

import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;

/**
 * Enthält die benutzten und geladenen GDX-Fonts
 *
 * @author Longri
 */
public class Fonts {
    public static final String DEFAULT_CHARACTERS = getCyrilCharSet();
    private static final String sClass = "Fonts";
    private static BitmapFont compass;
    private static BitmapFont big;
    private static BitmapFont normal;
    private static BitmapFont small;
    private static BitmapFont normalBubble;
    private static BitmapFont smallBubble;
    private static BitmapFontCache measureNormalCache;
    private static BitmapFontCache measureSmallCache;
    private static BitmapFontCache measureBigCache;

    private static String getCyrilCharSet() {
        int numberOfCharacters = 0x04ff - 0x0400 + 1;
        char[] cyril = new char[numberOfCharacters];
        for (int i = 0x0400; i <= 0x04ff; i++) {
            cyril[i - 0x0400] = (char) i;
        }
        numberOfCharacters = 0x1F5FF - 0x1F300 + 1;
        char[] smileys = new char[numberOfCharacters];
        for (int i = 0x1F300; i <= 0x1F5FF; i++) {
            smileys[i - 0x1F300] = (char) i;
        }
        return FreeTypeFontGenerator.DEFAULT_CHARS + String.copyValueOf(cyril) + String.copyValueOf(smileys) + "—–•ŐőŰű√€†„”“’‘☺čěřšťůž…";
    }

    /**
     * Lädt die verwendeten Bitmap Fonts und berechnet die entsprechenden Größen
     */
    public static void loadFonts() {

        COLOR.loadColors();

        FileHandle fontFileHandle = null;

        if (CB_Skin.getInstance().getSkinFolder().isDirectory()) {
            FileHandle[] ttfFonts = CB_Skin.getInstance().getSkinFolder().list();
            for (FileHandle file : ttfFonts) {
                if (file.extension().equalsIgnoreCase("ttf")) {
                    // get the first found ttf-font
                    fontFileHandle = file;
                    break;
                }
            }
        }

        if (fontFileHandle == null || !fontFileHandle.exists()) {
            // no skin font found, use default font
            fontFileHandle = FileFactory.getInternalFileHandle("skins/default/DroidSans-Bold.ttf");
        }

        Log.debug(sClass, "Generate scaled Fonts from " + fontFileHandle);
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFileHandle);
        double density = UiSizes.getInstance().getScale();
        compass = loadFontFromFile(generator, (int) (CB_Skin.getInstance().getSizeBiggest() * density));
        big = loadFontFromFile(generator, (int) (CB_Skin.getInstance().getSizeBig() * density));
        normal = loadFontFromFile(generator, (int) (CB_Skin.getInstance().getSizeNormal() * density));
        small = loadFontFromFile(generator, (int) (CB_Skin.getInstance().getSizeSmall() * density));
        normalBubble = loadFontFromFile(generator, (int) (CB_Skin.getInstance().getSizeNormalBubble() * density));
        smallBubble = loadFontFromFile(generator, (int) (CB_Skin.getInstance().getSizeSmallBubble() * density));
        generator.dispose();
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

    //

    public static GlyphLayout measure(String txt) {
        if (txt == null || txt.equals(""))
            txt = "Text";
        if (measureNormalCache == null)
            measureNormalCache = new BitmapFontCache(Fonts.getNormal());
        GlyphLayout bounds;
        try {
            bounds = measureNormalCache.setText(txt, 0, 0);
        } catch (Exception ex) {
            bounds = measureNormalCache.setText("Text", 0, 0);
        }
        bounds.height = bounds.height - measureNormalCache.getFont().getDescent();
        return bounds;
    }

    public static GlyphLayout measureForSmallFont(String txt) {

        if (txt == null || txt.equals(""))
            txt = "text";
        if (measureSmallCache == null)
            measureSmallCache = new BitmapFontCache(Fonts.getSmall());
        GlyphLayout bounds;
        try {
            bounds = measureSmallCache.setText(txt, 0, 0);
        } catch (Exception ex) {
            bounds = new GlyphLayout();
        }
        bounds.height = bounds.height - measureSmallCache.getFont().getDescent();
        return bounds;
    }

    public static GlyphLayout measureForBigFont(String txt) {
        if (txt == null || txt.equals(""))
            txt = "Text";
        if (measureBigCache == null)
            measureBigCache = new BitmapFontCache(Fonts.getBig());
        GlyphLayout bounds;
        try {
            bounds = measureBigCache.setText(txt, 0, 0);
        } catch (Exception e) {
            bounds = measureBigCache.setText("Text", 0, 0);
        }
        bounds.height = bounds.height - measureBigCache.getFont().getDescent();
        return bounds;
    }

    public static GlyphLayout measureWrapped(String txt, float width) {
        if (txt == null || txt.equals(""))
            txt = "Text";
        if (measureNormalCache == null)
            measureNormalCache = new BitmapFontCache(Fonts.getNormal());
        GlyphLayout bounds;
        try {
            bounds = measureNormalCache.setText(txt, 0, 0, width, 0, true);//measureNormalCache.setWrappedText(txt, 0, 0, width);
        } catch (Exception e) {
            bounds = measureNormalCache.setText("Text", 0, 0, width, 0, true);//measureNormalCache.setWrappedText(txt, 0, 0, width);
        }
        bounds.height = bounds.height - measureNormalCache.getFont().getDescent();
        return bounds;
    }

    private static BitmapFont loadFontFromFile(FreeTypeFontGenerator generator, int scale) {
        String fs = System.getProperty("file.separator");
        String fontPath = "";
        // fonts-Verzeichnis "lokal" im cachebox/skins/small oder ..normal oder christmas

        if (CB_Skin.getInstance().getSkinFolder().type() == FileType.Absolute) {
            String FolderPath = CB_Skin.getInstance().getSkinFolder().path();
            String path = FolderPath.replace("/", fs) + fs + "fnts";
            if (!FileIO.directoryExists(path)) {
                // fonts-Verzeichnis "global" im cachebox/skins
                path = FolderPath.replace("/", fs) + fs + ".." + fs + "fnts";
            }
            // fonts-Verzeichnis "lokal" im cachebox/skins/small oder ..normal oder christmas
            fontPath = path + fs + scale + ".fnt";

        }

        // Wenn der font nicht vorberechnet ist, dann wird er generiert
        if (FileIO.fileExists(fontPath)) {
            Log.debug(sClass, "load font for scale " + scale + " from " + fontPath);
            // automatic load of png does not work on Android, so
            // return new BitmapFont(Gdx.files.absolute(fontPath),false);
            Texture tex = new Texture(Gdx.files.absolute(fontPath.replace(".fnt", ".png")));
            tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            TextureRegion region = new TextureRegion(tex);
            return new BitmapFont(Gdx.files.absolute(fontPath), region, false);
        } else {
            Log.debug(sClass, "generate font for scale " + scale);
            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size = scale;
            parameter.characters = DEFAULT_CHARACTERS;
            BitmapFont ret = generator.generateFont(parameter);
            TextureRegion region = ret.getRegion();
            Texture tex = region.getTexture();
            tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            return ret;
        }
    }

    public static void setNightMode(boolean value) {
        CB_Skin.getInstance().setNightMode(value);
    }

}
