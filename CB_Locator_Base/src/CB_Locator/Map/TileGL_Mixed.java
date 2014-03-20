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
package CB_Locator.Map;

import CB_UI_Base.graphics.Images.MatrixDrawable;
import CB_UI_Base.graphics.Images.SortedRotateList;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Extends TileGL_Bmp with holding a List of Drawable for Symbols and Textes
 * 
 * @author Longri
 */
public class TileGL_Mixed extends TileGL_Bmp
{
	SortedRotateList rotateList;
	TileGL_RotateDrawables rotateDrawable;

	public TileGL_Mixed(CB_Locator.Map.Descriptor desc, byte[] bytes, TileState state, Format format)
	{
		super(desc, bytes, state, format);

	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height, CB_List<TileGL_RotateDrawables> returnDrawableList)
	{
		super.draw(batch, x, y, width, height, returnDrawableList);

		if (returnDrawableList != null)
		{
			if (rotateList != null)
			{
				if (rotateDrawable == null)
				{
					rotateDrawable = new TileGL_RotateDrawables(x, y, width, height, this, rotateList);
				}

				rotateDrawable.set(x, y, width, height);
				returnDrawableList.add(rotateDrawable);
			}
		}

	}

	public void add(SortedRotateList rotateList)
	{
		this.rotateList = rotateList;
	}

	@Override
	public void dispose()
	{
		super.dispose();

		if (rotateList != null)
		{
			for (MatrixDrawable drw : rotateList)
			{
				drw.dispose();
			}
		}
		rotateList = null;
		rotateDrawable = null;
	}

}
