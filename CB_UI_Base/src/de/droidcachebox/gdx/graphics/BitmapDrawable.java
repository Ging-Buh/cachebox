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
package de.droidcachebox.gdx.graphics;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.IRenderFBO;
import de.droidcachebox.gdx.graphics.mapsforge.GDXBitmap;
import de.droidcachebox.utils.CB_List;

/**
 * @author Longri
 */
public class BitmapDrawable implements GDXBitmap, Disposable {
    // static HashMap<String, Texture> TextureList = new HashMap<String, Texture>();
    public static TextureAtlas Atlas;
    static CB_List<String> HashStringList = new CB_List<String>();
    static PixmapPacker Packer = new PixmapPacker(2048, 2048, Format.RGBA8888, 2, true);

    private final float scaleFactor;
    private final boolean isDisposed = false;
    private byte[] buffer;
    private String AtlasHashString;
    private Sprite sprite;
    private Texture tex;

    public BitmapDrawable(InputStream stream, int HashCode, float scaleFactor) {

        AtlasHashString = String.valueOf(HashCode);
        this.scaleFactor = scaleFactor;

        if (HashStringList.contains(AtlasHashString))
            return;
        HashStringList.add(AtlasHashString);
        try {
            int length = stream.available();
            if (length == 0)
                length = 512;
            buffer = new byte[length];
            int position = 0;

            while (true) {
                int count = stream.read(buffer, position, buffer.length - position);
                if (count == -1)
                    break;
                position += count;
                if (position == buffer.length) {
                    int b = stream.read();
                    if (b == -1)
                        break;
                    // Grow buffer.
                    byte[] newBuffer = new byte[buffer.length * 2];
                    System.arraycopy(buffer, 0, newBuffer, 0, position);
                    buffer = newBuffer;
                    buffer[position++] = (byte) b;
                }
            }

            if (position < buffer.length) {
                // Shrink buffer.
                byte[] newBuffer = new byte[position];
                System.arraycopy(buffer, 0, newBuffer, 0, position);
                buffer = newBuffer;
            }

        } catch (IOException ex) {
            throw new GdxRuntimeException("Error reading file: " + this, ex);
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException ignored) {
            }
        }

        if (GL.that.isGlThread()) {
            createData();
        } else {
            GL.that.runOnGL((IRenderFBO) () -> createData());
        }
    }

    public BitmapDrawable(byte[] bytes, int HashCode, float scaleFactor) {
        AtlasHashString = String.valueOf(HashCode);
        this.scaleFactor = scaleFactor;

        if (HashStringList.contains(AtlasHashString)) {
            return;
        }
        HashStringList.add(AtlasHashString);
        buffer = bytes;

        if (GL.that.isGlThread()) {
            createData();
        } else {
            GL.that.runOnGL((IRenderFBO) () -> createData());
        }
    }

    public static boolean AtlasContains(int hashCode) {
        return HashStringList.contains(String.valueOf(hashCode));
    }

    private void createData() {
        Pixmap pix;
        try {
            pix = new Pixmap(buffer, 0, buffer.length);
        } catch (Exception e) {
            // Can't create
            e.printStackTrace();
            return;
        }

        // scale?
        if (this.scaleFactor != 1) {
            int w = (int) (pix.getWidth() * this.scaleFactor);
            int h = (int) (pix.getHeight() * this.scaleFactor);
            Pixmap tmpPixmap = new Pixmap(w, h, pix.getFormat());
            tmpPixmap.setFilter(Pixmap.Filter.NearestNeighbour);
            tmpPixmap.drawPixmap(pix, 0, 0, pix.getWidth(), pix.getHeight(), 0, 0, w, h);
            pix.dispose();
            pix = tmpPixmap;
        }

        try {
            Packer.pack(AtlasHashString, pix);
        } catch (Exception e) {

            e.printStackTrace();
        }

        if (Atlas == null) {
            Atlas = Packer.generateTextureAtlas(TextureFilter.Linear, TextureFilter.Linear, false);
        } else {
            Packer.updateTextureAtlas(Atlas, TextureFilter.Linear, TextureFilter.Linear, false);
        }

        pix.dispose();
        buffer = null;
    }

    public void draw(Batch batch, float x, float y, float width, float height) {
        if (Atlas == null)
            return;
        if (sprite == null) {
            createSprite();
        }
        if (sprite != null)
            batch.draw(sprite, x, y, width, height);
    }

    private void createSprite() {
        sprite = Atlas.createSprite(AtlasHashString);
    }

    public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        if (Atlas == null)
            return;
        if (sprite == null)
            createSprite();
        if (sprite != null)
            batch.draw(sprite, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    @Override
    public void compress(OutputStream outputStream) throws IOException {

    }

    @Override
    public void decrementRefCount() {

    }

    @Override
    public int getHeight() {
        if (Atlas == null)
            return 0;
        if (sprite == null)
            createSprite();
        if (sprite == null)
            return 0;
        return (int) sprite.getHeight();
    }

    @Override
    public int getWidth() {
        if (Atlas == null)
            return 0;
        if (sprite == null)
            createSprite();
        if (sprite == null)
            return 0;
        return (int) sprite.getWidth();
    }

    @Override
    public void incrementRefCount() {

    }

    @Override
    public void scaleTo(int width, int height) {

    }

    @Override
    public void setBackgroundColor(int color) {

    }

    @Override
    public void recycle() {

    }

    @Override
    public void getPixels(int[] maskBuf, int i, int w, int j, int y, int w2, int k) {

    }

    @Override
    public void setPixels(int[] maskedContentBuf, int i, int w, int j, int y, int w2, int k) {

    }

    @Override
    public Texture getTexture() {
        if (isDisposed)
            return null;
        return tex;
    }

    @Override
    public void dispose() {
        // Dont Dispose Texture, is Hold in a Static List
        tex = null;
        sprite = null;
        AtlasHashString = null;

    }

    @Override
    public boolean isDestroyed() {
        // TODO Auto-generated method stub
        return false;
    }

}
