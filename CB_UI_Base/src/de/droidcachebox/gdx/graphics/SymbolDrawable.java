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

import com.badlogic.gdx.graphics.g2d.Batch;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Longri
 */
public class SymbolDrawable implements IRotateDrawable {

    private final float DEFAULT_WIDTH;
    private final float DEFAULT_HEIGHT;
    private final boolean alignCenter;
    private final float pointX;
    private final float pointY;
    private final float postRotate;
    private final GL_Matrix matrix = new GL_Matrix();
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private BitmapDrawable BITMAP;

    /**
     * Constructor for symbols are rotated back on drawing
     *
     * @param bmp
     * @param pointX
     * @param pointY
     * @param defaultWidth
     * @param defaultHeight
     * @param scale
     * @param alignCenter
     */
    public SymbolDrawable(BitmapDrawable bmp, float pointX, float pointY, float defaultWidth, float defaultHeight, boolean alignCenter) {
        BITMAP = bmp;
        DEFAULT_WIDTH = defaultWidth;
        DEFAULT_HEIGHT = defaultHeight;
        this.alignCenter = alignCenter;
        this.pointX = pointX;
        this.pointY = pointY;
        this.postRotate = 0;
    }

    public SymbolDrawable(BitmapDrawable bmp, float pointX, float pointY, int defaultWidth, int defaultHeight, boolean alignCenter, float theta) {
        BITMAP = bmp;
        DEFAULT_WIDTH = defaultWidth;
        DEFAULT_HEIGHT = defaultHeight;
        this.alignCenter = alignCenter;
        this.pointX = pointX;
        this.pointY = pointY;
        this.postRotate = theta;
    }

    @Override
    public boolean draw(Batch batch, float x, float y, float width, float height, float rotate) {
        if (isDisposed.get())
            return true;
        if (BITMAP == null)
            return true;

        float scaleWidth = width / DEFAULT_WIDTH;
        float scaleHeight = height / DEFAULT_HEIGHT;
        boolean scaled = scaleWidth != 1 || scaleHeight != 1;
        rotate += this.postRotate;

        float offsetX = 0;// (tex.getWidth() * scaleWidth) / 2;
        float offsetY = BITMAP.getHeight();

        matrix.reset();

        float pivotX = (BITMAP.getWidth()) / 2;
        float pivotY = (BITMAP.getHeight()) / 2;

        if (alignCenter) {
            matrix.setTranslate(-pivotX, pivotY);
            matrix.setTranslate(-offsetX, -offsetY);
            if (scaled)
                matrix.setScale(1 / scaleWidth, 1 / scaleHeight);
            // matrix.rotate(rotate);
            if (scaled)
                matrix.setScale(scaleWidth, scaleHeight);
            matrix.setTranslate(pointX + pivotX, pointY - pivotY);
            matrix.setTranslate(-pivotX, -pivotY);
        } else {
            matrix.setTranslate(-pivotX, pivotY);
            matrix.setTranslate(-offsetX, -offsetY);
            if (scaled)
                matrix.setScale(1 / scaleWidth, 1 / scaleHeight);
            if (scaled)
                matrix.setScale(scaleWidth, scaleHeight);
            matrix.setTranslate(pointX + pivotX, pointY - pivotY);
        }

        if (scaled)
            matrix.setScale(scaleWidth, scaleHeight);

        float[] pos = new float[]{this.pointX, this.pointY};

        matrix.mapPoints(pos);

        BITMAP.draw(batch, pos[0] + x, pos[1] + y, pivotX, pivotY, BITMAP.getWidth(), BITMAP.getHeight(), scaleWidth, scaleHeight, rotate);
        return false;
    }

    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void dispose() {
        synchronized (isDisposed) {
            if (isDisposed.get())
                return;
            if (BITMAP != null)
                BITMAP.dispose();
            BITMAP = null;
            isDisposed.set(true);
        }
    }

}
