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

import CB_UI_Base.GL_UI.GL_Listener.GL;
import com.badlogic.gdx.utils.Array;

class MapTiles {

    private final MapTileCache tiles;
    private final MapTileCache overlayTiles;

    Layer currentLayer = null;
    Layer currentOverlayLayer = null;

    MapTiles(int capacity) {
        tiles = new MapTileCache((short) capacity);
        overlayTiles = new MapTileCache((short) capacity);
    }

    int getCapacity() {
        return tiles.getCapacity();
    }

    void loadTile(final Descriptor descriptor) {
        // get in separate thread, cause the awake by interrupt closes the stream of mapsforge
        // java.nio.channels.ClosedByInterruptException
        // conclusion: todo better
        GL.that.postAsync(() -> {
            TileGL tile = currentLayer.getTileGL(descriptor);
            if (tile == null) {
                if (currentLayer.cacheTileToFile(descriptor)) {
                    addTile(descriptor, currentLayer.getTileGL(descriptor));
                }
            } else {
                addTile(descriptor, tile);
            }
        });
    }

    private void addTile(Descriptor descriptor, TileGL tile) {
        if (tile != null) {
            synchronized (tiles) {
                if (!tiles.containsKey(descriptor.getHashCode())) {
                    tiles.add(descriptor.getHashCode(), tile);
                    if (tile.canDraw())
                        GL.that.renderOnce();
                }
            }
            GL.that.renderOnce();
        }
    }

    void loadOverlayTile(final Descriptor descriptor) {
        // overlay is never mapsforge
        TileGL tile = currentOverlayLayer.getTileGL(descriptor);
        if (tile == null) {
            GL.that.postAsync(() -> {
                // download in separate thread
                if (currentOverlayLayer.cacheTileToFile(descriptor)) {
                    addOverlayTile(descriptor, currentOverlayLayer.getTileGL(descriptor));
                }
            });
        } else {
            addOverlayTile(descriptor, tile);
        }
    }

    private void addOverlayTile(Descriptor descriptor, TileGL tile) {
        if (tile != null) {
            synchronized (overlayTiles) {
                if (!overlayTiles.containsKey(descriptor.getHashCode())) {
                    overlayTiles.add(descriptor.getHashCode(), tile);
                    // Redraw Map after a new Tile was loaded or generated
                    if (tile.canDraw())
                        GL.that.renderOnce();
                }
            }
            GL.that.renderOnce();
        }
    }

    void clearTiles() {
        synchronized (tiles) {
            tiles.clear();
        }
    }

    void clearOverlayTiles() {
        if (currentOverlayLayer != null) {
            synchronized (overlayTiles) {
                overlayTiles.clear();
            }
        }
    }

    int markTileToDraw(long hash) {
        synchronized (tiles) {
            return tiles.markTileToDraw(hash);
        }
    }

    int getTilesToDrawCounter() {
        synchronized (tiles) {
            return tiles.getTilesToDrawCounter();
        }
    }

    TileGL getDrawingTile(int i) {
        synchronized (tiles) {
            return tiles.getDrawingTile(i);
        }
    }

    void resetTilesToDrawCounter() {
        tiles.resetTilesToDrawCounter();
    }

    Array<Long> getTilesHashCopy() {
        Array<Long> tilesHashCopy = new Array<>();
        synchronized (tiles) {
            for (long hash : tiles.getHashList()) {
                if (hash != 0) tilesHashCopy.add(hash);
            }
        }
        return tilesHashCopy;
    }

    int markOverlayTileToDraw(long hash) {
        synchronized (overlayTiles) {
            return overlayTiles.markTileToDraw(hash);
        }
    }

    int getOverlayTilesToDrawCounter() {
        synchronized (overlayTiles) {
            return overlayTiles.getTilesToDrawCounter();
        }
    }

    TileGL getOverlayDrawingTile(int i) {
        synchronized (overlayTiles) {
            return overlayTiles.getDrawingTile(i);
        }
    }

    void resetOverlayTilesToDrawCounter() {
        overlayTiles.resetTilesToDrawCounter();
    }

    Array<Long> getOverlayTilesHashCopy() {
        Array<Long> overlayTilesHashCopy = new Array<>();
        if (currentOverlayLayer != null) {
            synchronized (overlayTiles) {
                for (long hash : overlayTiles.getHashList()) {
                    if (hash != 0) overlayTilesHashCopy.add(hash);
                }
            }
        }
        return overlayTilesHashCopy;
    }

    void increaseAge() {
        synchronized (tiles) {
            tiles.increaseAge();
        }
        if (currentOverlayLayer != null) {
            synchronized (overlayTiles) {
                overlayTiles.increaseAge();
            }
        }
    }

    void sortByAge() {
        synchronized (tiles) {
            tiles.sortByAge();
        }
        if (currentOverlayLayer != null) {
            synchronized (overlayTiles) {
                overlayTiles.sortByAge();
            }
        }
    }


    /*
    public int getNumberOfLoadedTiles() {
        return tiles.getNumberOfLoadedTiles();
    }
     */


}