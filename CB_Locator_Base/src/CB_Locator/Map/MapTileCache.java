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

import CB_Utils.Log.Log;

public class MapTileCache {
    private final static String sKlasse = "MapTileCache";

    private final long[] EMPTY_HashList;
    private final TileGL[] EMPTY_TileList;
    private final short[] EMPTY_IndexList;

    private final long[] hashList;
    private final TileGL[] tileList;
    private final short[] indexList;
    private final short capacity;
    private final short[] tilesToDraw;
    private short numberOfLoadedTiles = 0;
    private short tilesToDrawCounter = 0;

    MapTileCache(short capacity) {
        hashList = new long[capacity];
        tileList = new TileGL[capacity];
        indexList = new short[capacity];
        tilesToDraw = new short[capacity];

        EMPTY_HashList = new long[capacity];
        EMPTY_TileList = new TileGL[capacity];
        EMPTY_IndexList = new short[capacity];

        this.capacity = capacity;

        // Initial Empty Lists
        for (short i = 0; i < this.capacity; i++) {
            EMPTY_HashList[i] = 0;
            EMPTY_TileList[i] = null;
            EMPTY_IndexList[i] = i;
        }
        clearIndexList();
    }

    long[] getHashList() {
        return hashList;
    }

    private void clearIndexList() {
        System.arraycopy(EMPTY_IndexList, 0, indexList, 0, capacity);
    }

    public void add(long hash, TileGL tile) {

        if (tile == null)
            return;
        short freeIndex = addIndex();
        // Destroy the holden Tile on this now FreeIndex
        if (tileList[freeIndex] != null) {
            tileList[freeIndex].dispose();
        }
        hashList[freeIndex] = hash;
        tileList[freeIndex] = tile;
        indexList[0] = freeIndex;
        numberOfLoadedTiles++;
        if (numberOfLoadedTiles > capacity)
            numberOfLoadedTiles = capacity;
    }

    private short addIndex() {
        short ret = indexList[capacity - 1];
        // Move IndexList
        System.arraycopy(indexList, 0, indexList, 1, capacity - 1);
        return ret;
    }

    public boolean containsKey(long hash) {
        boolean cont = false;
        for (int i = 0; i < (int) capacity; i++) {
            if (hashList[i] == hash) {
                cont = true;
                break;
            }
        }
        return cont;
    }

    public TileGL get(long hash) {
        int hashIndex = getIndex(hash);
        return tileList[hashIndex];
    }

    private short getIndex(long hash) {
        short hashIndex = -1;
        for (short i = 0, n = (short) hashList.length; i < n; i++) {
            if (hashList[i] == hash) {
                hashIndex = i;
                break;
            }
        }
        return hashIndex;
    }

    /**
     * Sort this Map with TileGL.Age
     */
    void sortByAge() {

        boolean inSort;

        try {
            do {
                inSort = false;

                for (int i = 0, n = capacity - 1; i < n; i++) {

                    short index1 = indexList[i];
                    short index2 = indexList[i + 1];

                    // null check
                    if (tileList[index1] == null && tileList[index2] == null)
                        continue;
                    if (tileList[index1] != null && tileList[index2] == null)
                        continue;
                    if (tileList[index1] == null && tileList[index2] != null) {
                        // swap
                        indexList[i] = index2;
                        indexList[i + 1] = index1;
                        inSort = true;
                        break; // sort changed, begin new
                    }

                    if (tileList[index1].age == tileList[index2].age)
                        continue;
                    if (tileList[index1].age < tileList[index2].age)
                        continue;

                    // swap
                    indexList[i] = index2;
                    indexList[i + 1] = index1;
                    inSort = true;
                    break; // sort changed, begin new
                }
            } while (inSort);
        } catch (Exception ex) {
            Log.err(sKlasse, "sort", ex);
        }

    }

    public void clear() {
        Log.info("mapTileCache", "is cleared");
        clearIndexList();
        resetTilesToDrawCounter();
        System.arraycopy(EMPTY_HashList, 0, hashList, 0, capacity);
        for (int i = 0, n = tileList.length; i < n; i++) {
            if (tileList[i] != null) {
                tileList[i].dispose();
                tileList[i] = null;
            }
        }
        System.arraycopy(EMPTY_TileList, 0, tileList, 0, capacity);
        numberOfLoadedTiles = 0;
    }

    void increaseAge() {
        synchronized (tileList) {
            for (short i = 0, n = (short) tileList.length; i < n; i++) {
                if (tileList[i] != null)
                    tileList[i].age++;
            }
        }
    }

    /*
    public TileGL get(int i) {
        return tileList[indexList[i]];
    }
     */

    int markTileToDraw(long hashCode) {
        try {
            short index = getIndex(hashCode);
            if (index == -1)
                return index;
            if (!tileList[index].canDraw())
                return index;
            tileList[index].age = 0;
            tilesToDraw[tilesToDrawCounter++] = index;
            return index;
        } catch (Exception e) {
            return -2;
        }
    }

    void resetTilesToDrawCounter() {
        tilesToDrawCounter = 0;
    }

    TileGL getDrawingTile(int index) {
        try {
            int drawingIndex = tilesToDraw[index];
            return tileList[drawingIndex];
        } catch (Exception e) {
            return null;
        }
    }

    int getTilesToDrawCounter() {
        return tilesToDrawCounter;
    }

    int getCapacity() {
        return capacity;
    }

    /*
    int getNumberOfLoadedTiles() {
        return numberOfLoadedTiles;
    }
     */
}
