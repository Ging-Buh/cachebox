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

import com.badlogic.gdx.math.MathUtils;

/**
 * @author Longri
 */
public class CircleDrawable extends PolygonDrawable {

    final static float MIN_SEGMENTH_LENGTH = 10;
    final static int MIN_SEGMENTH_COUNT = 18;
    final float radius;
    final float x;
    final float y;
    int segmente;

    public CircleDrawable(float x, float y, float radius, GL_Paint paint, float width, float height) {
        super(paint, width, height);
        this.radius = radius;
        this.x = x;
        this.y = y;
        createTriangles();
    }

    private void createTriangles() {
        // calculate segment count
        double alpha = (360 * MIN_SEGMENTH_LENGTH) / (MathUtils.PI2 * radius);
        segmente = Math.max(MIN_SEGMENTH_COUNT, (int) (360 / alpha));

        // calculate theta step
        double thetaStep = (MathUtils.PI2 / segmente);

        if (paint.getGL_Style() == GL_Paint.GL_Style.FILL) {
            // initialize arrays
            vertices = new float[(segmente + 1) * 2];
            triangles = new short[(segmente) * 3];

            int index = 0;

            // first point is the center point
            vertices[index++] = x;
            vertices[index++] = y;

            int triangleIndex = 0;
            int verticeIdex = 1;
            boolean beginnTriangles = false;
            for (double i = 0; index < (segmente + 1) * 2; i += thetaStep) {
                vertices[index++] = (float) (x + radius * Math.cos(i));
                vertices[index++] = (float) (y + radius * Math.sin(i));

                if (!beginnTriangles) {
                    if (index % 6 == 0)
                        beginnTriangles = true;
                }

                if (beginnTriangles) {
                    triangles[triangleIndex++] = 0;
                    triangles[triangleIndex++] = (short) verticeIdex++;
                    triangles[triangleIndex++] = (short) verticeIdex;
                }

            }

            // last Triangle
            triangles[triangleIndex++] = 0;
            triangles[triangleIndex++] = (short) verticeIdex++;
            triangles[triangleIndex++] = (short) 1;

        } else {

            vertices = new float[(segmente) * 4];
            triangles = new short[(segmente) * 6];

            float halfStrokeWidth = (paint.strokeWidth) / 2;

            float radius1 = radius - halfStrokeWidth;
            float radius2 = radius + halfStrokeWidth;

            int index = 0;
            int triangleIndex = 0;
            int verticeIdex = 0;
            boolean beginnTriangles = false;
            for (float i = 0; index < (segmente * 4); i += thetaStep) {
                vertices[index++] = x + radius1 * MathUtils.cos(i);
                vertices[index++] = y + radius1 * MathUtils.sin(i);
                vertices[index++] = x + radius2 * MathUtils.cos(i);
                vertices[index++] = y + radius2 * MathUtils.sin(i);

                if (!beginnTriangles) {
                    if (index % 8 == 0)
                        beginnTriangles = true;
                }

                if (beginnTriangles) {
                    triangles[triangleIndex++] = (short) verticeIdex++;
                    triangles[triangleIndex++] = (short) verticeIdex++;
                    triangles[triangleIndex++] = (short) verticeIdex--;

                    triangles[triangleIndex++] = (short) verticeIdex++;
                    triangles[triangleIndex++] = (short) verticeIdex++;
                    triangles[triangleIndex++] = (short) verticeIdex--;
                }

            }

            // last two Triangles
            triangles[triangleIndex++] = (short) verticeIdex++;
            triangles[triangleIndex++] = (short) verticeIdex;
            triangles[triangleIndex++] = (short) 0;

            triangles[triangleIndex++] = (short) 0;
            triangles[triangleIndex++] = (short) 1;
            triangles[triangleIndex++] = (short) verticeIdex;
        }
    }

    /**
     * Returns TRUE if the given coordinate inside this circle
     *
     * @param x
     * @param y
     * @return
     */
    public boolean contains(float x, float y) {

        float dx = Math.abs(x - this.x);
        float dy = Math.abs(y - this.y);

        if (dx > radius)
            return false;
        if (dy > radius)
            return false;
        if (dx + dy <= radius)
            return true;

        if ((dx * dx) + (dy * dy) <= (radius * radius))
            return true;
        else
            return false;

    }
}
