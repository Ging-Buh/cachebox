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
package CB_UI_Base.graphics.Geometry;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Line defined as the set of start and end point points.
 *
 * @author Longri
 */
public class Line implements IGeometry {
    /**
     * points[0] =Start- X <br>
     * points[1] =Start- Y <br>
     * <br>
     * points[2] =End- X <br>
     * points[3] =End- Y <br>
     */
    public float[] points = new float[4];
    protected AtomicBoolean isDisposed = new AtomicBoolean(false);

    /**
     * Constructor
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public Line(float x1, float y1, float x2, float y2) {
        points[0] = x1;
        points[1] = y1;
        points[2] = x2;
        points[3] = y2;
    }

    public Line(float[] coords, int offset) {
        for (int i = 0; i < 4; i++)
            points[i] = coords[i + offset];
    }

    /**
     * Returns two Lines are splitted on value
     *
     * @param value
     * @return
     */
    public SplittResult splitt(float value) {
        SplittResult ret = new SplittResult();

        if (this.length() <= value) {
            ret.splittLine1 = this;
            ret.splittLine2 = null;
            ret.rest = value - this.length();

            return ret;
        }

        // calculate a point on the line x1-y1 to x2-y2 that is distance from x2-y2
        float vx = points[2] - points[0]; // x vector
        float vy = points[3] - points[1]; // y vector

        float mag = (float) Math.sqrt(vx * vx + vy * vy); // length

        vx /= mag;
        vy /= mag;

        // calculate the new vector, which is x2y2 + vxvy * (mag + distance).

        float px = (points[2] - vx * (mag - value));
        float py = (points[3] - vy * (mag - value));

        ret.splittLine1 = new Line(points[0], points[1], px, py);
        ret.splittLine2 = new Line(px, py, points[2], points[3]);
        ret.rest = -1;
        return ret;
    }

    /**
     * Returns True, if the beginn of this Line on given X,Y
     *
     * @param X
     * @param Y
     * @return
     */
    public boolean lineBeginn(float X, float Y) {
        if (points[0] == X && points[1] == Y)
            return true;
        return false;
    }

    /**
     * returns the length of this Line
     *
     * @return
     */
    public float length() {
        return (float) Math.sqrt((points[2] - points[0]) * (points[2] - points[0]) + (points[3] - points[1]) * (points[3] - points[1]));
    }

    @Override
    public float[] getVertices() {
        return points;
    }

    @Override
    public short[] getTriangles() {
        return null; // a line has no triangles
    }

    @Override
    public String toString() {
        return "[l " + points[0] + "," + points[1] + "-" + points[2] + "," + points[3] + "]";
    }

    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void dispose() {
        synchronized (isDisposed) {
            if (isDisposed.get())
                return;
            points = null;
            isDisposed.set(true);
        }
    }

    public float getAngle() {
        float ret = MathUtils.atan2((points[2] - points[0]), (points[3] - points[1])) * MathUtils.radiansToDegrees;
        ret = 90 - ret;
        return ret;
    }

    /**
     * Holds the result of splitting a Line
     *
     * @author Longri
     */
    public class SplittResult implements Disposable {
        public Line splittLine1, splittLine2;
        public float rest;
        protected AtomicBoolean isDisposed = new AtomicBoolean(false);

        public boolean isDisposed() {
            return isDisposed.get();
        }

        @Override
        public void dispose() {
            synchronized (isDisposed) {
                if (isDisposed.get())
                    return;
                if (splittLine1 != null)
                    splittLine1.dispose();
                splittLine1 = null;
                if (splittLine2 != null)
                    splittLine2.dispose();
                splittLine2 = null;
                isDisposed.set(true);
            }
        }
    }
}
