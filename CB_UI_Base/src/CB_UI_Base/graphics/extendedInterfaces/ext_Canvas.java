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
package CB_UI_Base.graphics.extendedInterfaces;

import CB_UI_Base.graphics.fromAndroid.RectF;
import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;

/**
 * @author Longri
 */
public interface ext_Canvas extends org.mapsforge.core.graphics.Canvas {

    void drawText(String text, float x, float y, Paint paint);

    void drawRect(RectF rect, ext_Paint strokePaint);

    void drawRoundRect(RectF rect, float rx, float ry, ext_Paint strokePaint);

    void drawOval(RectF rect, ext_Paint fillPaint);

    void scale(float sx, float sy);

    void setMatrix(ext_Matrix matrix);

    ext_Matrix getMatrix();

    void setMatrix(Matrix matrix);

    void save();

    void restore();

    void concat(ext_Matrix matrix);

    void drawTextOnPath(String text, ext_Path path, float x, float y, ext_Paint fillPaint);

    void clipRect(float left, float top, float right, float bottom);

    void clipPath(ext_Path path);

    void translate(float stepX, float stepY);

    void saveMatrix();// canvas.save(Canvas.MATRIX_SAVE_FLAG);

}
