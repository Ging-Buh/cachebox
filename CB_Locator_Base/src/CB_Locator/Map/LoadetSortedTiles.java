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

import java.util.Iterator;

import CB_Utils.Lists.CB_List;
import CB_Utils.Util.MoveableList;

/**
 * @author Longri
 */
public class LoadetSortedTiles implements Iterable<TileGL>
{

	private final MoveableList<Long> descList = new MoveableList<Long>();
	private final MoveableList<TileGL> tileList = new MoveableList<TileGL>();

	public LoadetSortedTiles()
	{

	}

	public void add(Long Hash, TileGL tile)
	{
		synchronized (descList)
		{
			descList.add(Hash);
			tileList.add(tile);
		}
	}

	public void remove(Long Hash)
	{
		synchronized (descList)
		{
			int index = descList.indexOf(Hash);
			descList.remove(index);
			TileGL t = tileList.remove(index);
			t.dispose();
		}
	}

	public void remove(TileGL tile)
	{
		synchronized (descList)
		{
			int index = tileList.indexOf(tile);
			descList.remove(index);
			TileGL t = tileList.remove(index);
			t.dispose();
		}
	}

	public boolean containsKey(Long Hash)
	{
		int index = descList.indexOf(Hash);
		if (index < 0 || index >= tileList.size()) return false;
		return true;
	}

	public TileGL get(Long Hash)
	{
		synchronized (descList)
		{
			int index = descList.indexOf(Hash);
			if (index < 0 || index >= tileList.size()) return null;
			return tileList.get(index);
		}
	}

	/**
	 * Sort this Map with TileGL.Age
	 */
	public void sort()
	{
		synchronized (descList)
		{
			boolean inSort = true;

			do
			{
				inSort = false;

				for (int i = 0; i < tileList.size() - 1; i++)
				{
					if (tileList.get(i).Age == tileList.get(i + 1).Age) continue;
					if (tileList.get(i).Age < tileList.get(i + 1).Age) continue;

					// swap
					tileList.MoveItem(i);
					descList.MoveItem(i);
					inSort = true;
					break; // sort changed, begin new
				}
			}
			while (inSort);
		}
	}

	/**
	 * All items with index > given size will remove and destroy!
	 * 
	 * @param size
	 */
	public void removeAndDestroy(int size)
	{
		synchronized (descList)
		{
			if (size >= descList.size()) return; // nothing to remove

			Object[] delList = tileList.get(size, descList.size());

			for (int i = 0; i < delList.length; i++)
			{
				TileGL tile = (TileGL) delList[i];
				tile.dispose();
				tile = null;
			}
			delList = null;
			tileList.truncate(size);
			descList.truncate(size);
		}

	}

	public int size()
	{
		synchronized (descList)
		{
			return descList.size();
		}
	}

	public TileGL[] getValues()
	{
		synchronized (descList)
		{
			Object[] arr = tileList.get(0, tileList.size());

			// must cast
			TileGL[] ret = new TileGL[arr.length];
			for (int i = 0; i < arr.length; i++)
			{
				ret[i] = (TileGL) arr[i];
			}

			return ret;
		}
	}

	public void clear()
	{
		synchronized (descList)
		{
			removeAndDestroy(descList.size());
		}
	}

	public void removeDestroyedTiles()
	{
		Object[] arr = tileList.get(0, tileList.size());
		CB_List<TileGL> destroyedList = new CB_List<TileGL>();
		for (int i = 0; i < arr.length; i++)
		{
			if (((TileGL) arr[i]).isDisposed()) destroyedList.add((TileGL) arr[i]);
		}

		for (TileGL tile : destroyedList)
		{
			int index = tileList.indexOf(tile);
			tileList.remove(index);
			descList.remove(index);
		}

	}

	@Override
	public Iterator<TileGL> iterator()
	{
		return tileList.iterator();
	}

	public Iterator<TileGL> reverse()
	{
		return tileList.reverseIterator();
	}

	/**
	 * Reduces the size of the array to the specified size. If the array is already smaller than the specified size, no action is taken.
	 */
	public void truncate(int size)
	{
		tileList.truncate(size);
		descList.truncate(size);
	}

	public CB_List<Long> allKeysAreNot(CB_List<Long> neadedTiles)
	{
		CB_List<Long> ret = new CB_List<Long>();

		for (long desc : descList)
		{
			if (!neadedTiles.contains(desc))
			{
				ret.add(desc);
			}
		}

		return ret;
	}
}
