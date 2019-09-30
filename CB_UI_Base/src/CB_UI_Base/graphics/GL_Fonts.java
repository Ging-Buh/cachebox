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
package CB_UI_Base.graphics;

import CB_UI_Base.AbstractGlobal;
import CB_UI_Base.GL_UI.Fonts;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import java.util.HashMap;

/**
 * GL_FontFamily => DEFAULT(DroidSans), MONOSPACE, SANS_SERIF, SERIF;<br>
 * GL_FontStyle => BOLD, BOLD_ITALIC, ITALIC, NORMAL;<br>
 * <br>
 * ANDROID Familyset:<br>
 *
 * <pre>
 * {@code
 * <family>
 *         <nameset>
 *             <name>sans-serif</name>
 *             <name>arial</name>
 *             <name>helvetica</name>
 *             <name>tahoma</name>
 *             <name>verdana</name>
 *         </nameset>
 *         <fileset>
 *             <file>Roboto-Regular.ttf</file>
 *             <file>Roboto-Bold.ttf</file>
 *             <file>Roboto-Italic.ttf</file>
 *             <file>Roboto-BoldItalic.ttf</file>
 *         </fileset>
 *     </family>
 *
 *     <family>
 *         <nameset>
 *             <name>sans-serif-light</name>
 *         </nameset>
 *         <fileset>
 *             <file>Roboto-Light.ttf</file>
 *             <file>Roboto-LightItalic.ttf</file>
 *         </fileset>
 *     </family>
 *
 *
 *     <family>
 *         <nameset>
 *             <name>serif</name>
 *             <name>times</name>
 *             <name>times new roman</name>
 *             <name>palatino</name>
 *             <name>georgia</name>
 *             <name>baskerville</name>
 *             <name>goudy</name>
 *             <name>fantasy</name>
 *             <name>cursive</name>
 *             <name>ITC Stone Serif</name>
 *         </nameset>
 *         <fileset>
 *             <file>DroidSerif-Regular.ttf</file>
 *             <file>DroidSerif-Bold.ttf</file>
 *             <file>DroidSerif-Italic.ttf</file>
 *             <file>DroidSerif-BoldItalic.ttf</file>
 *         </fileset>
 *     </family>
 *
 *     <family>
 *         <nameset>
 *             <name>Droid Sans</name>
 *         </nameset>
 *         <fileset>
 *             <file>DroidSans.ttf</file>
 *             <file>DroidSans-Bold.ttf</file>
 *         </fileset>
 *     </family>
 *
 *     <family>
 *         <nameset>
 *             <name>monospace</name>
 *             <name>courier</name>
 *             <name>courier new</name>
 *             <name>monaco</name>
 *         </nameset>
 *         <fileset>
 *             <file>DroidSansMono.ttf</file>
 *         </fileset>
 *     </family>
 * }
 * </pre>
 *
 * @author Longri
 */
class GL_Fonts {
    private final HashMap<Integer, BitmapFont> DroidSansMono = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> DroidSans_Bold = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> DroidSans = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> DroidSans_BoldItalic = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> DroidSans_Italic = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> Roboto_Regular = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> Roboto_Italic = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> Roboto_Bold = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> Roboto_BoldItalic = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> DroidSerif_Bold = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> DroidSerif_BoldItalic = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> DroidSerif_Italic = new HashMap<Integer, BitmapFont>();
    private final HashMap<Integer, BitmapFont> DroidSerif_Regular = new HashMap<Integer, BitmapFont>();

    private final boolean markUp;

    public GL_Fonts(boolean markUp) {
        this.markUp = markUp;
    }

    public BitmapFont get(GL_FontFamily fontFamily, GL_FontStyle fontStyle, float textSize) {
        int Size = ((int) textSize);
        if (textSize <= 0)
            textSize = 3;

        if (fontFamily == null || fontStyle == null)
            return get_Default(GL_FontStyle.NORMAL, Size);

        switch (fontFamily) {
            case MONOSPACE:
                return get_Monospace(fontStyle, Size);
            case SANS_SERIF:
                return get_SANS_SERIF(fontStyle, Size);
            case SERIF:
                return get_SERIF(fontStyle, Size);
            default:
                break;

        }
        return get_Default(fontStyle, Size);

    }

    private BitmapFont get_SANS_SERIF(GL_FontStyle fontStyle, int textSize) {
        switch (fontStyle) {
            case BOLD:
                return get_Roboto_Bold(textSize);// Roboto-Bold.ttf
            case BOLD_ITALIC:
                return get_Roboto_BoldItalic(textSize);// Roboto-BoldItalic.ttf
            case ITALIC:
                return get_Roboto_Italic(textSize);// Roboto-Italic.ttf
            case NORMAL:
                return get_Roboto_Regular(textSize);// Roboto-Regular.ttf
        }
        return null;
    }

    private BitmapFont get_SERIF(GL_FontStyle fontStyle, int textSize) {

        switch (fontStyle) {
            case BOLD:
                return get_DroidSerif_Bold(textSize);// DroidSerif-Bold.ttf
            case BOLD_ITALIC:
                return get_DroidSerif_BoldItalic(textSize);// DroidSerif-BoldItalic.ttf
            case ITALIC:
                return get_DroidSerif_Italic(textSize);// DroidSerif-Italic.ttf
            case NORMAL:
                return get_DroidSerif_Regular(textSize);// DroidSerif-Regular.ttf
        }
        return null;
    }

    private BitmapFont get_Default(GL_FontStyle fontStyle, int textSize) {
        switch (fontStyle) {
            case BOLD:
                return get_DroidSans_Bold(textSize);
            case BOLD_ITALIC:
                return get_DroidSans_BoldItalic(textSize);
            case ITALIC:
                return get_DroidSans_Italic(textSize);
            case NORMAL:
                return get_DroidSans(textSize);
        }
        return null;
    }

    private BitmapFont get_Monospace(GL_FontStyle fontStyle, int textSize) {
        // MonoSpace has no Style
        return get_DroidSansMono(textSize);
    }

    /**
     * DroidSerif-Bold.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_DroidSerif_Bold(int textSize) {
        if (DroidSerif_Bold.containsKey(textSize))
            return DroidSerif_Bold.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/DroidSerif-Bold.ttf");
        BitmapFont f = generateFont(fh, textSize);
        DroidSerif_Bold.put(textSize, f);
        return f;
    }

    /**
     * DroidSerif-BoldItalic.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_DroidSerif_BoldItalic(int textSize) {
        if (DroidSerif_BoldItalic.containsKey(textSize))
            return DroidSerif_BoldItalic.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/DroidSerif-BoldItalic.ttf");
        BitmapFont f = generateFont(fh, textSize);
        DroidSerif_BoldItalic.put(textSize, f);
        return f;
    }

    /**
     * DroidSerif-Italic.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_DroidSerif_Italic(int textSize) {
        if (DroidSerif_Italic.containsKey(textSize))
            return DroidSerif_Italic.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/DroidSerif-Italic.ttf");
        BitmapFont f = generateFont(fh, textSize);
        DroidSerif_Italic.put(textSize, f);
        return f;
    }

    /**
     * DroidSerif-Regular.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_DroidSerif_Regular(int textSize) {
        if (DroidSerif_Regular.containsKey(textSize))
            return DroidSerif_Regular.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/DroidSerif-Regular.ttf");
        BitmapFont f = generateFont(fh, textSize);
        DroidSerif_Regular.put(textSize, f);
        return f;
    }

    /**
     * Roboto-Regular.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_Roboto_Regular(int textSize) {
        if (Roboto_Regular.containsKey(textSize))
            return Roboto_Regular.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/Roboto-Regular.ttf");
        BitmapFont f = generateFont(fh, textSize);
        Roboto_Regular.put(textSize, f);
        return f;
    }

    /**
     * Roboto-Bold.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_Roboto_Bold(int textSize) {
        if (Roboto_Bold.containsKey(textSize))
            return Roboto_Bold.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/Roboto-Bold.ttf");
        BitmapFont f = generateFont(fh, textSize);
        Roboto_Bold.put(textSize, f);
        return f;
    }

    /**
     * Roboto-Italic.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_Roboto_Italic(int textSize) {
        if (Roboto_Italic.containsKey(textSize))
            return Roboto_Italic.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/Roboto-Italic.ttf");
        BitmapFont f = generateFont(fh, textSize);
        Roboto_Italic.put(textSize, f);
        return f;
    }

    /**
     * Roboto-BoldItalic.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_Roboto_BoldItalic(int textSize) {
        if (Roboto_BoldItalic.containsKey(textSize))
            return Roboto_BoldItalic.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/Roboto-BoldItalic.ttf");
        BitmapFont f = generateFont(fh, textSize);
        Roboto_BoldItalic.put(textSize, f);
        return f;
    }

    /**
     * DroidSans.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_DroidSans(int textSize) {
        if (DroidSans.containsKey(textSize))
            return DroidSans.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/DroidSans.ttf");
        BitmapFont f = generateFont(fh, textSize);
        DroidSans.put(textSize, f);
        return f;
    }

    /**
     * DroidSans-BoldItalic.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_DroidSans_BoldItalic(int textSize) {
        if (DroidSans_BoldItalic.containsKey(textSize))
            return DroidSans_BoldItalic.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/DroidSans-BoldItalic.ttf");
        BitmapFont f = generateFont(fh, textSize);
        DroidSans_BoldItalic.put(textSize, f);
        return f;
    }

    /**
     * DroidSans-BoldItalic.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_DroidSans_Italic(int textSize) {
        if (DroidSans_Italic.containsKey(textSize))
            return DroidSans_Italic.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/DroidSans-Italic.ttf");
        BitmapFont f = generateFont(fh, textSize);
        DroidSans_Italic.put(textSize, f);
        return f;
    }

    /**
     * DroidSans-Bold.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_DroidSans_Bold(int textSize) {
        if (DroidSans_Bold.containsKey(textSize))
            return DroidSans_Bold.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/DroidSans-Bold.ttf");
        BitmapFont f = generateFont(fh, textSize);
        DroidSans_Bold.put(textSize, f);
        return f;
    }

    /**
     * DroidSansMono.ttf
     *
     * @param textSize
     * @return
     */
    private BitmapFont get_DroidSansMono(int textSize) {
        if (DroidSansMono.containsKey(textSize))
            return DroidSansMono.get(textSize);
        FileHandle fh = AbstractGlobal.getInternalFileHandle("data/fonts/DroidSansMono.ttf");
        BitmapFont f = generateFont(fh, textSize);
        DroidSansMono.put(textSize, f);
        return f;
    }

    private BitmapFont generateFont(FileHandle file, int textSize) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(file);

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = textSize;
        parameter.characters = Fonts.DEFAULT_CHARACTER;
        BitmapFont ret = generator.generateFont(parameter);

        TextureRegion region = ret.getRegion();
        Texture tex = region.getTexture();
        tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        generator.dispose();

        return ret;
    }

}
