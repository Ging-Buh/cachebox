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
package org.mapsforge.map.awt;

import java.awt.image.BufferedImage;

import CB_Utils.Lists.CB_List;

/**
 * @author Longri
 */
public class ext_AwtTileBitmap extends ext_AwtBitmap
{

	static CB_List<BufferedImage> ReusebleList = new CB_List<BufferedImage>();

	private static BufferedImage getReusable(int tileSize)
	{
		synchronized (ReusebleList)
		{
			for (int i = 0, n = ReusebleList.size(); i < n; i++)
			{
				BufferedImage reuse = ReusebleList.get(i);
				if (reuse != null && reuse.getWidth() == tileSize && reuse.getHeight() == tileSize)
				{
					ReusebleList.remove(reuse);
					return reuse;
				}
			}
		} 
		return null;
	}

	public ext_AwtTileBitmap(int tileSize)
	{
		BufferedImage reuse = getReusable(tileSize);
		if (reuse != null)
		{
			this.bufferedImage = reuse;
		}
		else
		{
			this.bufferedImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
		}
	}

	@Override
	public void recycle()
	{

		// chk if reuseList full or cann put BufferdImage to ReuseList?
		synchronized (ReusebleList)
		{
			if (ReusebleList.size() < 15)
			{
				ReusebleList.add(bufferedImage);
			}
		}
		instCount++;
		this.bufferedImage = null;
	}

	public BufferedImage getBufferedImage() {
		return this.bufferedImage;
	}

}
