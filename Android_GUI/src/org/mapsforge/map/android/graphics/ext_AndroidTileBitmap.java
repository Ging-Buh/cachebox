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
package org.mapsforge.map.android.graphics;

import CB_Utils.Lists.CB_List;
import android.graphics.Bitmap.Config;

/**
 * @author Longri
 */
public class ext_AndroidTileBitmap extends ext_AndroidBitmap
{
	static CB_List<android.graphics.Bitmap> ReusebleList = new CB_List<android.graphics.Bitmap>();

	private static android.graphics.Bitmap getReusable(int tileSize)
	{
		synchronized (ReusebleList)
		{
			for (android.graphics.Bitmap reuse : ReusebleList)
			{
				if (reuse != null && reuse.getWidth() == tileSize && reuse.getHeight() == tileSize)
				{
					ReusebleList.remove(reuse);
					return reuse;
				}
			}
		}
		return null;
	}

	public ext_AndroidTileBitmap(int tileSize)
	{
		android.graphics.Bitmap reuse = getReusable(tileSize);
		if (reuse != null)
		{
			this.bitmap = reuse;
		}
		else
		{
			this.bitmap = super.createAndroidBitmap(tileSize, tileSize, Config.RGB_565);
			;
		}
	}

	static int DEBUG_MAX_REUSABLE_COUNT = 0;

	@Override
	public void recycle()
	{

		// chk if reuseList full or cann put BufferdImage to ReuseList?
		synchronized (ReusebleList)
		{

			if (ReusebleList.size() < 15)
			{
				ReusebleList.add(this.bitmap);
			}
			else
			{
				this.bitmap.recycle();
			}

			DEBUG_MAX_REUSABLE_COUNT = Math.max(DEBUG_MAX_REUSABLE_COUNT, ReusebleList.size());
		}
		instCount++;
		this.bitmap = null;
	}
}
