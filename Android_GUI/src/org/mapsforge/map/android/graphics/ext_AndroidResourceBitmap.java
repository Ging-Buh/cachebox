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
package org.mapsforge.map.android.graphics;

import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.extendedInterfaces.ext_Bitmap;
import android.graphics.Bitmap;
import com.badlogic.gdx.graphics.Texture;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Longri
 */
public class ext_AndroidResourceBitmap extends AndroidResourceBitmap implements ext_Bitmap {

    protected BitmapDrawable GL_image;

    public ext_AndroidResourceBitmap(Bitmap resourceBitmap) {
        super(resourceBitmap);
        GL_image = null;
    }

    ext_AndroidResourceBitmap(InputStream inputStream, int HashCode, float scaleFactor) throws IOException {
        super(inputStream, HashCode);
        GL_image = null;
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
