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

import CB_Utils.Lists.CB_List;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author ging-buh
 * @author Longri
 */
public abstract class TileGL implements Disposable {
    private final int DEFAULT_TILE_SIZE = 256;
    public Descriptor Descriptor = null;
    public TileState State;
    // zum speichern beliebiger Zusatzinfos
    public Object data;
    // / <summary>
    // / Frames seit dem letzten Zugriff auf die Textur
    // / </summary>
    public long Age = 0;
    protected boolean isDisposed = false;

    public abstract boolean isDisposed();

    public abstract boolean canDraw();

    @Override
    public abstract String toString();

    public abstract long getWidth();

    public abstract long getHeight();

    public float getScaleFactor() {
        return getWidth() / DEFAULT_TILE_SIZE;
    }

    public abstract void draw(Batch batch, float f, float y, float tILESIZE, float tILESIZE2, CB_List<TileGL_RotateDrawables> rotateList);

    public enum TileState {
        Scheduled, Present, LowResolution, Disposed
    }

}