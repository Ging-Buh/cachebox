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

/**
 * @author Longri
 */
public class LoadedSortedTiles
{

	private final long[] HashList;
	private final TileGL[] TileList;
	private final short[] IndexList;
	private final short Capacity;
	private short Size = 0;

	public LoadedSortedTiles(short capacity)
	{
		this.HashList = new long[capacity];
		this.TileList = new TileGL[capacity];
		this.IndexList = new short[capacity];
		this.Capacity = capacity;
		clearIndexList();
	}

	private void clearIndexList()
	{
		for (short i = 0, n = (short) this.IndexList.length; i < n; i++)
		{
			this.IndexList[i] = i;
		}
	}

	public void add(Long Hash, TileGL tile)
	{
		if (tile == null) return;

		short freeIndex = addIndex();
		this.HashList[freeIndex] = Hash;
		this.TileList[freeIndex] = tile;
		this.IndexList[0] = freeIndex;
		this.Size++;
		if (this.Size > this.Capacity - 1) Size = (short) (this.Capacity - 1);

	}

	private short addIndex()
	{
		short ret = this.IndexList[Capacity - 1];
		// Move IndexList
		System.arraycopy(this.IndexList, 0, this.IndexList, 1, Capacity - 1);
		return ret;
	}

	public boolean containsKey(Long Hash)
	{
		boolean cont = false;
		for (short i = 0, n = (short) (this.Capacity - 1); i < n; i++)
		{
			if (this.HashList[i] == Hash)
			{
				cont = true;
				break;
			}
		}
		return cont;

	}

	public TileGL get(Long Hash)
	{
		int HashIndex = -1;
		for (short i = 0, n = (short) this.HashList.length; i < n; i++)
		{
			if (this.HashList[i] == Hash)
			{
				HashIndex = i;
				break;
			}
		}
		return this.TileList[HashIndex];
	}

	/**
	 * Sort this Map with TileGL.Age
	 */
	public void sort()
	{

		boolean inSort = true;

		do
		{
			inSort = false;

			for (short i = 0, n = (short) (this.Capacity - 1); i < n; i++)
			{

				short index1 = IndexList[i];
				short index2 = IndexList[i + 1];

				if (TileList[index1].Age == TileList[index2].Age) continue;
				if (TileList[index1].Age < TileList[index2].Age) continue;

				// swap
				IndexList[i] = index2;
				IndexList[i + 1] = index1;
				inSort = true;
				break; // sort changed, begin new
			}
		}
		while (inSort);

	}

	public int size()
	{
		return this.Size;
	}

	public void clear()
	{
		clearIndexList();
		for (short i = 0, n = (short) this.HashList.length; i < n; i++)
		{
			this.HashList[i] = 0;
		}
		Size = 0;
	}

	public void increaseLoadedTilesAge()
	{
		for (short i = 0, n = (short) this.TileList.length; i < n; i++)
		{
			this.TileList[i].Age++;
		}
	}

	public TileGL get(int i)
	{
		return this.TileList[this.IndexList[i]];
	}

}
