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
package de.droidcachebox.locator.map;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import de.droidcachebox.utils.CB_List;

public abstract class TileGL implements Disposable {
    protected boolean isDisposed = false;
    private Descriptor descriptor;
    protected TileState state;
    // zum speichern beliebiger Zusatzinfos
    private Object data;
    // / <summary>
    // / Frames seit dem letzten Zugriff auf die Textur
    // / </summary>
    private long age;

    protected TileGL() {
        age = 0;
        descriptor = null;
    }

    public abstract boolean isDisposed();

    public abstract boolean canDraw();

    public abstract long getWidth();

    public abstract long getHeight();

    public float getScaleFactor() {
        return ((float) getWidth()) / 256;
    }

    public abstract void draw(Batch batch, float f, float y, float tILESIZE, float tILESIZE2, CB_List<TileGL_RotateDrawables> rotateList);

    public Descriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    public TileState getState() {
        return state;
    }

    public void setState(TileState state) {
        this.state = state;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public enum TileState {
        Scheduled, Present, LowResolution, Disposed
    }

}