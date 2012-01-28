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
 *
 */

package CB_Core.Math;

/**
 * Die Size Structur enth�lt die Member width und height
 * 
 * @author Longri
 */
public class SizeF
{
	public float width;
	public float height;

	public float halfWidth;
	public float halfHeight;

	public float Width4_8;
	public float Height4_8;

	/**
	 * Constructor
	 * 
	 * @param width
	 * @param height
	 */
	public SizeF(float Width, float Height)
	{
		this.width = Width;
		this.height = Height;
		this.halfWidth = Width / 2;
		this.halfHeight = Height / 2;
		this.Width4_8 = (float) (Width / 4.8);
		this.Height4_8 = (float) (Height / 4.8);
	}

	public SizeF()
	{
		this.width = 0f;
		this.height = 0f;
	}

	/**
	 * Setzt die Werte f�r Height und Width. Wenn sich einer der Werte ge�ndert hat, wird ein True zur�ck gegeben, ansonsten False.
	 * 
	 * @param Width
	 * @param Height
	 * @return
	 */
	public boolean setSize(float Width, float Height)
	{
		if (this.width == Width && this.height == Height) return false;
		this.width = Width;
		this.height = Height;
		this.halfWidth = Width / 2;
		this.halfHeight = Height / 2;
		this.Width4_8 = (float) (Width / 4.8);
		this.Height4_8 = (float) (Height / 4.8);
		return true;
	}

	public CB_RectF getBounds()
	{
		return getBounds(0, 0);
	}

	public CB_RectF getBounds(int x, int y)
	{
		return new CB_RectF(x, y, width + x, height + y);
	}

	public CB_RectF getBounds(int x, int y, int k, int l)
	{
		return new CB_RectF(x, y, width + x + k, height + y + l);
	}

}
