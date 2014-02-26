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

import java.util.HashMap;

import CB_UI_Base.Global;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

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
 *     <family>
 *         <nameset>
 *             <name>sans-serif-thin</name>
 *         </nameset>
 *         <fileset>
 *             <file>Roboto-Thin.ttf</file>
 *             <file>Roboto-ThinItalic.ttf</file>
 *         </fileset>
 *     </family>
 * 
 *     <family>
 *         <nameset>
 *             <name>sans-serif-condensed</name>
 *         </nameset>
 *         <fileset>
 *             <file>RobotoCondensed-Regular.ttf</file>
 *             <file>RobotoCondensed-Bold.ttf</file>
 *             <file>RobotoCondensed-Italic.ttf</file>
 *             <file>RobotoCondensed-BoldItalic.ttf</file>
 *         </fileset>
 *     </family>
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
public class GL_Fonts
{
	private final static HashMap<Integer, BitmapFont> Fonts_Default_Normal = new HashMap<Integer, BitmapFont>();

	public static BitmapFont get(GL_FontFamily fontFamily, GL_FontStyle fontStyle, float textSize)
	{
		int Size = ((int) textSize);

		if (textSize <= 0) textSize = 3;

		if (fontFamily == null || fontStyle == null) return get_Default_Normal(Size);

		try
		{
			return get_Default_Normal(Size);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static BitmapFont get_Default_Normal(int textSize)
	{
		if (Fonts_Default_Normal.containsKey(textSize)) return Fonts_Default_Normal.get(textSize);

		// create font
		// FileHandle fh = Global.getInternalFileHandle("data/fonts/DroidSans-Bold.ttf");
		FileHandle fh = Global.getInternalFileHandle("data/fonts/micross.ttf");
		// FileHandle fh = Global.getInternalFileHandle("skins/default/DroidSans-Bold.ttf");
		BitmapFont f = generateFont(fh, textSize);
		Fonts_Default_Normal.put(textSize, f);
		return f;
	}

	private static BitmapFont generateFont(FileHandle file, int textSize)
	{
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(file);

		BitmapFont ret = generator.generateFont(textSize);

		TextureRegion region = ret.getRegion();
		Texture tex = region.getTexture();
		tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		generator.dispose();
		return ret;
	}

}
