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

import CB_Locator.LocatorBasePlatFormMethods;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.extendedInterfaces.ext_Bitmap;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import org.mapsforge.core.graphics.TileBitmap;

import java.io.ByteArrayOutputStream;

public class TileGL_Bmp extends TileGL {
    private static final String log = "TileGL_Bmp";
    private final Format format;
    private byte[] bytes;
    private Texture texture;
    private TileBitmap bitmap;
    private boolean inCreation = false;

    TileGL_Bmp(Descriptor desc, TileBitmap bitmap, TileState state, Format format) {
        descriptor = desc;
        this.texture = null;
        this.format = format;
        this.bitmap = bitmap;
        if (bitmap instanceof ext_Bitmap) {
            bytes = getByteArray();
            this.bitmap = null;
        } else {
            // todo check (is quicker) for removing else part (getTexture special for Android without creating a bytearray)
            /* */
            bytes = getByteArray();
            this.bitmap = null;
            /* */
        }
        State = state;
        createTexture();
    }

    TileGL_Bmp(Descriptor desc, byte[] bytes, TileState state, Format format) {
        descriptor = desc;
        this.texture = null;
        this.format = format;
        this.bytes = bytes;
        this.bitmap = null;
        State = state;
        createTexture();
    }

    @Override
    public boolean canDraw() {
        if (texture != null)
            return true;
        if (inCreation)
            return false;
        Log.err(log, "Impossible: not in Creation and texture is null! " + this);
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
                texture = LocatorBasePlatFormMethods.getTexture(bitmap);
                bitmap = null;
                // Log.info(log, "created: " + this);
            }
        } else {
            // create Texture on next GlThread
            GL.that.RunOnGL(() -> {
                if (bitmap == null)
                    getTexture();
                else {
                    texture = LocatorBasePlatFormMethods.getTexture(bitmap);
                    bitmap = null;
                    // Log.info(log, "created: " + this);
                }
                GL.that.renderOnce();
            });
        }
    }

    private void getTexture() {
        try {
            Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
            texture = new Texture(pixmap, format, CB_UI_Base_Settings.useMipMap.getValue());
            pixmap.dispose();
            Log.info(log, "Final step: " + this);
        } catch (Exception ex) {
            Log.err(log, "[TileGL] can't create Pixmap or Texture: ", ex);
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
            if (bitmap instanceof ext_Bitmap) ((ext_Bitmap) bitmap).recycle();
            return byteArray;
        } catch (Exception ex) {
            Log.err(log, "convert bitmap to byteArray", ex);
            return null;
        }
    }

    @Override
    public String toString() {
        return "[Age: " + age + " " + State.toString() + ", " + descriptor.toString();
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
        // Log.info(log, "dispose: " + this);

        if (GL.that.isGlThread()) {
            try {
                if (texture != null)
                    texture.dispose();
            } catch (java.lang.NullPointerException e) {
                e.printStackTrace();
            }
            texture = null;
        } else {
            GL.that.RunOnGL(() -> {
                try {
                    if (texture != null)
                        texture.dispose();
                } catch (NullPointerException e) {
                    e.printStackTrace();
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

}
