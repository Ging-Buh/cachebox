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

import de.droidcachebox.gdx.graphics.mapsforge.ext_Matrix;

/**
 * @author Longri
 */
public class MatrixDrawable {
    /**
     * Can draw on real GL drawing
     */
    public final boolean reaelDraw;
    /**
     * @uml.property name="drawable"
     * @uml.associationEnd
     */
    public final IRotateDrawable drawable;
    /**
     * @uml.property name="matrix"
     * @uml.associationEnd
     */
    public ext_Matrix matrix;

    public MatrixDrawable(IRotateDrawable drw, ext_Matrix mat, boolean realDraw) {
        this.drawable = drw;

        if (!mat.isDefault()) {
            this.matrix = new GL_Matrix();
            this.matrix.set(mat);
        } else {
            this.matrix = null;
        }

        this.reaelDraw = realDraw;

    }

    public void dispose() {
        if (this.matrix != null)
            this.matrix.dispose();
        // TODO chek if we cann dispose and not hold on Cache like BmpBuffer at GL_GraphicFactory // this.drawable.dispose();
    }
}
