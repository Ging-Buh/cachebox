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
public class MapTileCache
{

	private final long[] EMPTY_HashList;
	private final TileGL[] EMPTY_TileList;
	private final short[] EMPTY_IndexList;

	private final long[] HashList;
	private final TileGL[] TileList;
	private final short[] IndexList;
	private final short Capacity;
	private short Size = 0;

	private final short[] DrawingIndexList;
	private short TilesToDrawSize = 0;

	public MapTileCache(short capacity)
	{
		this.HashList = new long[capacity];
		this.TileList = new TileGL[capacity];
		this.IndexList = new short[capacity];
		this.DrawingIndexList = new short[capacity];

		this.EMPTY_HashList = new long[capacity];
		this.EMPTY_TileList = new TileGL[capacity];
		this.EMPTY_IndexList = new short[capacity];

		this.Capacity = capacity;

		// Initial Empty Lists
		for (short i = 0, n = Capacity; i < n; i++)
		{
			this.EMPTY_HashList[i] = 0;
			this.EMPTY_TileList[i] = null;
			this.EMPTY_IndexList[i] = i;
		}
		clearIndexList();
	}

	private void clearIndexList()
	{
		System.arraycopy(this.EMPTY_IndexList, 0, this.IndexList, 0, Capacity);
	}

	public void add(Long Hash, TileGL tile)
	{

		if (tile == null) return;
		short freeIndex = addIndex();
		// Destroy the holden Tile on this now FreeIndex
		if (this.TileList[freeIndex] != null)
		{
			this.TileList[freeIndex].dispose();
		}
		this.HashList[freeIndex] = Hash;
		this.TileList[freeIndex] = tile;
		this.IndexList[0] = freeIndex;
		this.Size++;
		if (this.Size > this.Capacity) Size = (this.Capacity);

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
		for (int i = 0, n = this.Capacity; i < n; i++)
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
		int HashIndex = getIndex(Hash);
		return this.TileList[HashIndex];
	}

	private short getIndex(Long Hash)
	{
		short HashIndex = -1;
		for (short i = 0, n = (short) this.HashList.length; i < n; i++)
		{
			if (this.HashList[i] == Hash)
			{
				HashIndex = i;
				break;
			}
		}
		return HashIndex;
	}

	/**
	 * Sort this Map with TileGL.Age
	 */
	public void sort()
	{

		boolean inSort = true;

		try
		{
			do
			{
				inSort = false;

				for (int i = 0, n = this.Capacity - 1; i < n; i++)
				{

					short index1 = IndexList[i];
					short index2 = IndexList[i + 1];

					// null check
					if (TileList[index1] == null && TileList[index2] == null) continue;
					if (TileList[index1] != null && TileList[index2] == null) continue;
					if (TileList[index1] == null && TileList[index2] != null)
					{
						// swap
						IndexList[i] = index2;
						IndexList[i + 1] = index1;
						inSort = true;
						break; // sort changed, begin new
					}

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
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public int size()
	{
		return this.Size;
	}

	public void clear()
	{
		// System.out.print("LOADED_TILES clear() " + Logger.getCallerName(1) + Global.br);
		clearIndexList();
		clearDrawingList();
		System.arraycopy(this.EMPTY_HashList, 0, this.HashList, 0, Capacity - 1);
		for (int i = 0, n = this.TileList.length; i < n; i++)
		{
			if (this.TileList[i] != null)
			{
				this.TileList[i].dispose();
				this.TileList[i] = null;
			}
		}
		System.arraycopy(this.EMPTY_TileList, 0, this.TileList, 0, Capacity - 1);
		Size = 0;
	}

	public void increaseLoadedTilesAge()
	{
		for (short i = 0, n = (short) this.TileList.length; i < n; i++)
		{
			if (this.TileList[i] != null) this.TileList[i].Age++;
		}
	}

	public TileGL get(int i)
	{
		return this.TileList[this.IndexList[i]];
	}

	// #############################################################################
	// Inherited drawing list!

	public boolean markToDraw(Long HashCode)
	{
		try
		{
			short index = getIndex(HashCode);
			if (index == -1) return false;
			if (!this.TileList[index].canDraw()) return false;
			this.TileList[index].Age = 0;
			this.DrawingIndexList[this.TilesToDrawSize++] = index;
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public void clearDrawingList()
	{
		// System.out.print("LOADED_TILES clearDrawingList() " + Logger.getCallerName(1) + Global.br);
		this.TilesToDrawSize = 0;
	}

	public TileGL getDrawingTile(int index)
	{
		try
		{
			int drawingIndex = this.DrawingIndexList[index];
			return this.TileList[drawingIndex];
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public int DrawingSize()
	{
		return this.TilesToDrawSize;
	}

	public int getCapacity()
	{
		return Capacity;
	}

}
