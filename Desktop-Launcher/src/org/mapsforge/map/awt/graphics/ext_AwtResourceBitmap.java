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
package org.mapsforge.map.awt.graphics;

import CB_Locator.LocatorSettings;
import CB_UI_Base.graphics.GL_RenderType;
import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.extendedInterfaces.ext_Bitmap;
import com.badlogic.gdx.graphics.Texture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Longri
 */
public class ext_AwtResourceBitmap extends AwtResourceBitmap implements ext_Bitmap {
    protected BitmapDrawable GL_image;

    public ext_AwtResourceBitmap(InputStream stream, int HashCode, float scaleFactor) throws IOException {
        super(stream);

        if (scaleFactor != 1) {
            int w = (int) (this.getWidth() * scaleFactor);
            int h = (int) (this.getHeight() * scaleFactor);
            this.scaleTo(w, h);
        }

        createGL_Image(HashCode, scaleFactor);
    }

    public ext_AwtResourceBitmap(BufferedImage resourceBitmap) throws IOException {
        super(resourceBitmap);
        createGL_Image(resourceBitmap.hashCode(), 1.0f);
    }

    private void createGL_Image(int HashCode, float scaleFactor) throws IOException {
        byte[] bytes = null;

        if (!BitmapDrawable.AtlasContains(HashCode)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            this.compress(baos);

            bytes = new byte[baos.toByteArray().length];
            System.arraycopy(baos.toByteArray(), 0, bytes, 0, baos.toByteArray().length);

        }

        GL_RenderType RENDERING_TYPE = LocatorSettings.MapsforgeRenderType.getEnumValue();

        // Don't create GL_Image with renderType Mapsforge! GL_Images are not needed!
        if (RENDERING_TYPE == GL_RenderType.Mapsforge) {
            GL_image = null;
            return;
        }

        GL_image = new BitmapDrawable(bytes, HashCode, scaleFactor);
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
    public BitmapDrawable getGlBmpHandle() {
        return GL_image;
    }

    @Override
    public Texture getTexture() {
        if (GL_image == null)
            return null;
        return GL_image.getTexture();
    }

}
