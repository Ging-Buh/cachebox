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

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author Longri
 */
public interface ext_Matrix extends Disposable {

    /**
     * Set the matrix to identity
     */
    void reset();

    void rotate(float theta);

    void rotate(float theta, float pivotX, float pivotY);

    void scale(float scaleX, float scaleY);

    void scale(float scaleX, float scaleY, float pivotX, float pivotY);

    void translate(float translateX, float translateY);

    void set(ext_Matrix matrix);

    void postConcat(ext_Matrix matrix);

    void preTranslate(float x, float y);

    void preScale(float x, float y);

    void preScale(float sx, float sy, float px, float py);

    void postRotate(float angle);

    void postScale(float rx, float ry);

    void postTranslate(float cx, float cy);

    void mapPoints(float[] src);

    Matrix4 getMatrix4();

    void preRotate(float angle);

    void setValues(float[] fs);

    void preSkew(float f, float tan);

    void preRotate(Float angle, Float cx, Float cy);

    void preConcat(ext_Matrix matrix);

    boolean invert();

    @Override
    String toString();

    boolean isDefault();

}