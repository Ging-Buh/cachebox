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

public class MapTileCache {

    private final long[] EMPTY_HashList;
    private final TileGL[] EMPTY_TileList;
    private final short[] EMPTY_IndexList;

    private final long[] HashList;
    private final TileGL[] tileList;
    private final short[] IndexList;
    private final short Capacity;
    private final short[] DrawingIndexList;
    private short Size = 0;
    private short tilesToDrawSize = 0;

    MapTileCache(short capacity) {
        this.HashList = new long[capacity];
        this.tileList = new TileGL[capacity];
        this.IndexList = new short[capacity];
        this.DrawingIndexList = new short[capacity];

        this.EMPTY_HashList = new long[capacity];
        this.EMPTY_TileList = new TileGL[capacity];
        this.EMPTY_IndexList = new short[capacity];

        this.Capacity = capacity;

        // Initial Empty Lists
        for (short i = 0; i < Capacity; i++) {
            this.EMPTY_HashList[i] = 0;
            this.EMPTY_TileList[i] = null;
            this.EMPTY_IndexList[i] = i;
        }
        clearIndexList();
    }

    private void clearIndexList() {
        System.arraycopy(this.EMPTY_IndexList, 0, this.IndexList, 0, Capacity);
    }

    public void add(Long Hash, TileGL tile) {

        if (tile == null)
            return;
        short freeIndex = addIndex();
        // Destroy the holden Tile on this now FreeIndex
        if (this.tileList[freeIndex] != null) {
            this.tileList[freeIndex].dispose();
        }
        this.HashList[freeIndex] = Hash;
        this.tileList[freeIndex] = tile;
        this.IndexList[0] = freeIndex;
        this.Size++;
        if (this.Size > this.Capacity)
            Size = (this.Capacity);

    }

    private short addIndex() {
        short ret = this.IndexList[Capacity - 1];
        // Move IndexList
        System.arraycopy(this.IndexList, 0, this.IndexList, 1, Capacity - 1);
        return ret;
    }

    public boolean containsKey(Long Hash) {
        boolean cont = false;
        for (int i = 0; i < (int) this.Capacity; i++) {
            if (this.HashList[i] == Hash) {
                cont = true;
                break;
            }
        }
        return cont;
    }

    public TileGL get(Long Hash) {
        int HashIndex = getIndex(Hash);
        return this.tileList[HashIndex];
    }

    private short getIndex(Long Hash) {
        short HashIndex = -1;
        for (short i = 0, n = (short) this.HashList.length; i < n; i++) {
            if (this.HashList[i] == Hash) {
                HashIndex = i;
                break;
            }
        }
        return HashIndex;
    }

    /**
     * Sort this Map with TileGL.Age
     */
    public void sort() {

        boolean inSort;

        try {
            do {
                inSort = false;

                for (int i = 0, n = this.Capacity - 1; i < n; i++) {

                    short index1 = IndexList[i];
                    short index2 = IndexList[i + 1];

                    // null check
                    if (tileList[index1] == null && tileList[index2] == null)
                        continue;
                    if (tileList[index1] != null && tileList[index2] == null)
                        continue;
                    if (tileList[index1] == null && tileList[index2] != null) {
                        // swap
                        IndexList[i] = index2;
                        IndexList[i + 1] = index1;
                        inSort = true;
                        break; // sort changed, begin new
                    }

                    if (tileList[index1].age == tileList[index2].age)
                        continue;
                    if (tileList[index1].age < tileList[index2].age)
                        continue;

                    // swap
                    IndexList[i] = index2;
                    IndexList[i + 1] = index1;
                    inSort = true;
                    break; // sort changed, begin new
                }
            } while (inSort);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int size() {
        return this.Size;
    }

    public void clear() {
        // System.out.print("LOADED_TILES clear() " + Logger.getCallerName(1) + Global.br);
        clearIndexList();
        clearDrawingList();
        System.arraycopy(this.EMPTY_HashList, 0, this.HashList, 0, Capacity - 1);
        for (int i = 0, n = this.tileList.length; i < n; i++) {
            if (this.tileList[i] != null) {
                this.tileList[i].dispose();
                this.tileList[i] = null;
            }
        }
        System.arraycopy(this.EMPTY_TileList, 0, this.tileList, 0, Capacity - 1);
        Size = 0;
    }

    public void increaseLoadedTilesAge() {
        for (short i = 0, n = (short) this.tileList.length; i < n; i++) {
            if (this.tileList[i] != null)
                this.tileList[i].age++;
        }
    }

    public TileGL get(int i) {
        return this.tileList[this.IndexList[i]];
    }

    // #############################################################################
    // Inherited drawing list!

    boolean markToDraw(Long hashCode) {
        try {
            short index = getIndex(hashCode);
            if (index == -1)
                return false;
            if (!this.tileList[index].canDraw())
                return false;
            this.tileList[index].age = 0;
            this.DrawingIndexList[this.tilesToDrawSize++] = index;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    void clearDrawingList() {
        // System.out.print("LOADED_TILES clearDrawingList() " + Logger.getCallerName(1) + Global.br);
        this.tilesToDrawSize = 0;
    }

    TileGL getDrawingTile(int index) {
        try {
            int drawingIndex = this.DrawingIndexList[index];
            return this.tileList[drawingIndex];
        } catch (Exception e) {
            return null;
        }
    }

    int DrawingSize() {
        return this.tilesToDrawSize;
    }

    int getCapacity() {
        return Capacity;
    }

}
