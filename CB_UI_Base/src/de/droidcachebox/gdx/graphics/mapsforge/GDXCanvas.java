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
package de.droidcachebox.gdx.graphics.mapsforge;

import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;

/**
 * @author Longri
 */
public interface GDXCanvas extends org.mapsforge.core.graphics.Canvas {

    void drawText(String text, float x, float y, Paint paint);

    void scale(float sx, float sy);

    GDXMatrix getMatrix();

    void setMatrix(GDXMatrix matrix);

    void setMatrix(Matrix matrix);

    void save();

    void restore();

    void concat(GDXMatrix matrix);

    void drawTextOnPath(String text, GDXPath path, float x, float y, GDXPaint fillPaint);

    void clipRect(float left, float top, float right, float bottom);

    void clipPath(GDXPath path);

    void translate(float stepX, float stepY);

    void saveMatrix();// canvas.save(Canvas.MATRIX_SAVE_FLAG);

}
