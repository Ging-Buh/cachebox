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

import static de.droidcachebox.settings.AllSettings.useMipMap;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

import org.mapsforge.core.graphics.TileBitmap;

import java.io.ByteArrayOutputStream;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.graphics.mapsforge.GDXBitmap;
import de.droidcachebox.locator.LocatorMethods;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.log.Log;

public class TileGL_Bmp extends TileGL {
    private static final String sClass = "TileGL_Bmp";
    private final Format format;
    private byte[] bytes;
    private Texture texture;
    private TileBitmap bitmap;
    private boolean inCreation = false;

    TileGL_Bmp(Descriptor desc, TileBitmap bitmap, TileState state, Format format) {
        setDescriptor(desc);
        texture = null;
        this.format = format;
        this.bitmap = bitmap;
        if (bitmap instanceof GDXBitmap) {
            bytes = getByteArray();
            this.bitmap = null;
        } else {
            // todo check (is quicker) for removing else part (getTexture special for Android without creating a bytearray)
            /* */
            bytes = getByteArray();
            this.bitmap = null;
            /* */
        }
        this.state = state;
        createTexture();
    }

    TileGL_Bmp(Descriptor desc, byte[] bytes, TileState state, Format format) {
        setDescriptor(desc);
        texture = null;
        this.format = format;
        this.bytes = bytes;
        this.bitmap = null;
        this.state = state;
        createTexture();
    }

    @Override
    public boolean canDraw() {
        if (texture != null)
            return true;
        if (inCreation)
            return false;
        Log.err(sClass, "Impossible: not in Creation and texture is null! " + this);
        return false;
    }

    private void createTexture() {
        if (inCreation)
            return;
        inCreation = true;
        if (isDisposed)
            return;
        if (texture != null)
            return;
        if (bitmap == null && bytes == null)
            return;

        if (GL.that.isGlThread()) {
            if (bitmap == null)
                getTexture();
            else {
                texture = LocatorMethods.getTexture(bitmap);
                bitmap = null;
                // Log.debug(log, "created: " + this);
            }
        } else {
            // create Texture on next GlThread
            GL.that.runOnGL(() -> {
                if (bitmap == null)
                    getTexture();
                else {
                    texture = LocatorMethods.getTexture(bitmap);
                    bitmap = null;
                    // Log.debug(log, "created: " + this);
                }
                GL.that.renderOnce();
            });
        }
    }

    private void getTexture() {
        try {
            Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
            texture = new Texture(pixmap, format, useMipMap.getValue());
            pixmap.dispose();
            // Log.debug(log, "Final step: " + this);
        } catch (Exception ex) {
            Log.err(sClass, "[TileGL] can't create Pixmap or Texture: ", ex);
        }
        bitmap = null;
        bytes = null;
        inCreation = false;
    }

    private byte[] getByteArray() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(baos);
            byte[] byteArray = baos.toByteArray(); // takes long
            baos.close();
            if (bitmap instanceof GDXBitmap) ((GDXBitmap) bitmap).recycle();
            return byteArray;
        } catch (Exception ex) {
            Log.err(sClass, "convert bitmap to byteArray", ex);
            return null;
        }
    }

    @Override
    public String toString() {
        return "[Age: " + getAge() + " " + getState().toString() + ", " + getDescriptor().toString();
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height, CB_List<TileGL_RotateDrawables> rotateList) {
        if (texture != null)
            batch.draw(texture, x, y, width, height);
    }

    @Override
    public long getWidth() {
        if (texture != null)
            return texture.getWidth();
        return 0;
    }

    @Override
    public long getHeight() {
        if (texture != null)
            return texture.getHeight();
        return 0;
    }

    @Override
    public void dispose() {
        if (isDisposed)
            return;

        if (GL.that.isGlThread()) {
            try {
                if (texture != null)
                    texture.dispose();
            } catch (java.lang.NullPointerException ignored) {
            }
            texture = null;
        } else {
            GL.that.runOnGL(() -> {
                try {
                    if (texture != null)
                        texture.dispose();
                } catch (NullPointerException ignored) {
                }
                texture = null;
            });
        }
        isDisposed = true;
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
