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
 * GL_FontFamily => DEFAULT, MONOSPACE, SANS_SERIF, SERIF;<br>
 * GL_FontStyle => BOLD, BOLD_ITALIC, ITALIC, NORMAL;
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
