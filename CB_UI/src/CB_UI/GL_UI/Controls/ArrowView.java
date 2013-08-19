/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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

package CB_UI.GL_UI.Controls;

import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.SpriteCache;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Eine Test GLView , die nur ein Image anzeigen soll. Im Einfachsten Fall soll der Richtungspfeil in der Mitte des Schirms plaziert werden.
 * 
 * @author Longri
 */
public class ArrowView extends CB_View_Base
{

	int arrowX = 200;
	int arrowY = 300;

	// # Constructors
	/**
	 * Constructor für ein neues TestView mit Angabe der linken unteren Ecke und der Höhe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public ArrowView(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);

	}

	@Override
	public void render(SpriteBatch batch)
	{

		Sprite arrow = SpriteCache.Arrows.get(0);
		arrow.setRotation(0);
		arrow.setBounds(-(width / 2), 0, width, height);
		arrow.setOrigin(this.centerPos.x, this.centerPos.y);
		arrow.draw(batch);

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// Pfeil auf Touch-Positon setzen zum Test
		arrowX = x;
		arrowY = y;
		return true;
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

}
