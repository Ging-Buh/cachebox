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

import CB_UI_Base.graphics.Images.SortedRotateList;
import CB_UI_Base.graphics.Images.VectorDrawable;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * @author Longri
 */
public class TileGL_Vector extends TileGL
{
	private VectorDrawable drawable;

	public TileGL_Vector(Descriptor desc, VectorDrawable drawable, TileState state)
	{
		this.drawable = drawable;
		this.Descriptor = desc;
		this.State = state;
	}

	@Override
	public boolean canDraw()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#ToString()
	 */
	@Override
	public String toString()
	{
		return State.toString() + ", " + Descriptor.ToString();
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height, CB_List<TileGL_RotateDrawables> returnDrawableList)
	{
		if (drawable != null)
		{
			drawable.draw(batch, x, y, width, height);

			if (returnDrawableList != null)
			{
				SortedRotateList list = drawable.getRotateDrawables();
				if (list != null) returnDrawableList.add(new TileGL_RotateDrawables(x, y, width, height, list));
			}
		}
	}

	@Override
	public long getWidth()
	{
		long w = drawable != null ? drawable.getWidth() : 0;
		return w;
	}

	@Override
	public long getHeight()
	{
		long h = drawable != null ? drawable.getHeight() : 0;
		return h;
	}

	@Override
	public void dispose()
	{
		if (drawable != null) drawable.dispose();
		drawable = null;
	}

	@Override
	public boolean isDisposed()
	{
		return drawable == null;
	}

}
