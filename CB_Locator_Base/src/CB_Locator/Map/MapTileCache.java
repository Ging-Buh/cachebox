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
        HashList = new long[capacity];
        tileList = new TileGL[capacity];
        IndexList = new short[capacity];
        DrawingIndexList = new short[capacity];

        EMPTY_HashList = new long[capacity];
        EMPTY_TileList = new TileGL[capacity];
        EMPTY_IndexList = new short[capacity];

        Capacity = capacity;

        // Initial Empty Lists
        for (short i = 0; i < Capacity; i++) {
            EMPTY_HashList[i] = 0;
            EMPTY_TileList[i] = null;
            EMPTY_IndexList[i] = i;
        }
        clearIndexList();
    }

    private void clearIndexList() {
        System.arraycopy(EMPTY_IndexList, 0, IndexList, 0, Capacity);
    }

    public void add(Long Hash, TileGL tile) {

        if (tile == null)
            return;
        short freeIndex = addIndex();
        // Destroy the holden Tile on this now FreeIndex
        if (tileList[freeIndex] != null) {
            tileList[freeIndex].dispose();
        }
        HashList[freeIndex] = Hash;
        tileList[freeIndex] = tile;
        IndexList[0] = freeIndex;
        Size++;
        if (Size > Capacity)
            Size = (Capacity);

    }

    private short addIndex() {
        short ret = IndexList[Capacity - 1];
        // Move IndexList
        System.arraycopy(IndexList, 0, IndexList, 1, Capacity - 1);
        return ret;
    }

    public boolean containsKey(Long Hash) {
        boolean cont = false;
        for (int i = 0; i < (int) Capacity; i++) {
            if (HashList[i] == Hash) {
                cont = true;
                break;
            }
        }
        return cont;
    }

    public TileGL get(Long Hash) {
        int HashIndex = getIndex(Hash);
        return tileList[HashIndex];
    }

    private short getIndex(Long Hash) {
        short HashIndex = -1;
        for (short i = 0, n = (short) HashList.length; i < n; i++) {
            if (HashList[i] == Hash) {
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

                for (int i = 0, n = Capacity - 1; i < n; i++) {

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
        return Size;
    }

    public void clear() {
        // System.out.print("LOADED_TILES clear() " + Logger.getCallerName(1) + Global.br);
        clearIndexList();
        clearDrawingList();
        System.arraycopy(EMPTY_HashList, 0, HashList, 0, Capacity - 1);
        for (int i = 0, n = tileList.length; i < n; i++) {
            if (tileList[i] != null) {
                tileList[i].dispose();
                tileList[i] = null;
            }
        }
        System.arraycopy(EMPTY_TileList, 0, tileList, 0, Capacity - 1);
        Size = 0;
    }

    public void increaseLoadedTilesAge() {
        for (short i = 0, n = (short) tileList.length; i < n; i++) {
            if (tileList[i] != null)
                tileList[i].age++;
        }
    }

    public TileGL get(int i) {
        return tileList[IndexList[i]];
    }

    // #############################################################################
    // Inherited drawing list!

    boolean markToDraw(Long hashCode) {
        try {
            short index = getIndex(hashCode);
            if (index == -1)
                return false;
            if (!tileList[index].canDraw())
                return false;
            tileList[index].age = 0;
            DrawingIndexList[tilesToDrawSize++] = index;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    void clearDrawingList() {
        // System.out.print("LOADED_TILES clearDrawingList() " + Logger.getCallerName(1) + Global.br);
        tilesToDrawSize = 0;
    }

    TileGL getDrawingTile(int index) {
        try {
            int drawingIndex = DrawingIndexList[index];
            return tileList[drawingIndex];
        } catch (Exception e) {
            return null;
        }
    }

    int DrawingSize() {
        return tilesToDrawSize;
    }

    int getCapacity() {
        return Capacity;
    }

}
