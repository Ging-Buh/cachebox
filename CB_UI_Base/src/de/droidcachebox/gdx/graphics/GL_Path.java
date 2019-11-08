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

import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import de.droidcachebox.gdx.graphics.mapsforge.ext_Matrix;
import de.droidcachebox.gdx.graphics.mapsforge.ext_Path;
import de.droidcachebox.gdx.math.RectF;
import de.droidcachebox.utils.CB_List;
import org.mapsforge.core.graphics.FillRule;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Longri
 */
public class GL_Path implements ext_Path, Disposable {
    private final static float MIN_SEGMENTH_LENGTH = 10;

    public float[] items;
    private float[] last;

    public int size;
    private float[] PathSectionLength;
    private CB_List<Integer> pathBegins = new CB_List<>();
    private int aktBeginn;
    private boolean isDisposed;

    private boolean isMoveTo;

    GL_Path(GL_Path path) {
        last = new float[2];
        size = path.size;
        last[0] = path.last[0];
        last[1] = path.last[1];

        items = new float[path.items.length];
        System.arraycopy(path.items, 0, items, 0, items.length);

        for (int i = 0, n = path.pathBegins.size(); i < n; i++) {
            pathBegins.add(path.pathBegins.get(i));
        }

        PathSectionLength = new float[path.PathSectionLength.length];
        System.arraycopy(path.PathSectionLength, 0, PathSectionLength, 0, PathSectionLength.length);
        aktBeginn = path.aktBeginn;
        isDisposed = false;
        isMoveTo = path.isMoveTo;
    }

    /**
     * Find the point on the line p0,p1 [x,y] a given fraction from p0. Fraction of 0.0 whould give back p0, 1.0 give back p1, 0.5 returns
     * midpoint of line p0,p1 and so on. F raction can be >1 and it can be negative to return any point on the line specified by p0,p1.
     *
     * @param p0x y             First coordinate of line [x,y].
     * @param p1x y             Second coordinate of line [x,y].
     * @param distance Point we are looking for coordinates of
     * @return p Coordinate of point we are looking for
     */
    private static float[] computePointOnLine(float p0x, float p0y, float p1x, float p1y, float distance) {
        float[] p = new float[2];
        float vectorX = p1x - p0x;
        float vectorY = p1y - p0y;
        float factor = (float) (distance / Math.sqrt((vectorX * vectorX) + (vectorY * vectorY)));
        vectorX *= factor;
        vectorY *= factor;
        p[0] = (p0x + vectorX);
        p[1] = (p0y + vectorY);
        return p;
    }

    private void setLast(float x, float y) {
        last[0] = x;
        last[1] = y;
    }

    /**
     * Add a line from the last point to the specified point (x,y).
     */
    @Override
    public void lineTo(float x, float y) {
        if (!isMoveTo) {
            if (last[0] == x && last[1] == y) {
                return;
            }
        }

        isMoveTo = false;

        if (size + 2 > items.length)
            resize(size + (Math.max(2, size >> 1)));
        items[size++] = x;
        items[size++] = y;
        setLast(x, y);
    }

    private void resize(int newSize) {
        this.items = Arrays.copyOf(this.items, newSize);
    }

    @Override
    public void moveTo(float x, float y) {
        aktBeginn = size;
        pathBegins.add(size);
        isMoveTo = true;
        lineTo(x, y);
    }

    @Override
    public void clear() {
        size = 0;
        aktBeginn = 0;
        pathBegins.clear();
    }

    public ArrayList<float[]> getVertices() {

        ArrayList<float[]> tmp = new ArrayList<>();

        if (pathBegins.size() > 1) {
            // Multi path
            for (int i = 0; i < pathBegins.size(); i++) {
                int pathBegin = pathBegins.get(i);

                int pathLength;
                if (i + 1 == pathBegins.size()) {
                    pathLength = size - pathBegin;
                } else {
                    int pathEnd = pathBegins.get(i + 1);
                    pathLength = pathEnd - pathBegin;
                }

                float[] array = new float[pathLength];
                System.arraycopy(items, pathBegin, array, 0, pathLength);
                tmp.add(array);
            }
        } else {
            float[] array = new float[size];
            System.arraycopy(items, 0, array, 0, size);
            tmp.add(array);
        }

        return tmp;
    }

    @Override
    public void setFillRule(FillRule fillRule) {

    }

    /**
     * Close the current contour. If the current point is not equal to the first point of the contour, a line segment is automatically
     * added.
     */
    @Override
    public void close() {
        if (isClosed())
            return;
        lineTo(items[aktBeginn], items[aktBeginn + 1]);
    }

    public boolean isClosed() {
        return (items[aktBeginn] == last[0] && items[aktBeginn + 1] == last[1]);
    }

    /**
     * Add a cubic bezier from the last point, approaching control points (x1,y1) and (x2,y2), and ending at (x3,y3). If no moveTo() call
     * has been made for this contour, the first point is automatically set to (0,0).
     *
     * @param x1 The x-coordinate of the 1st control point on a cubic curve
     * @param y1 The y-coordinate of the 1st control point on a cubic curve
     * @param x2 The x-coordinate of the 2nd control point on a cubic curve
     * @param y2 The y-coordinate of the 2nd control point on a cubic curve
     * @param x3 The x-coordinate of the end point on a cubic curve
     * @param y3 The y-coordinate of the end point on a cubic curve
     */
    @Override
    public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {

        // calculate distance
        float distance = calcDistance(last[0], last[1], x1, y1);
        distance += calcDistance(x1, y1, x2, y2);
        distance += calcDistance(x2, y2, x3, y3);
        distance = Math.min(0.2f, 1 / (distance / MIN_SEGMENTH_LENGTH));

        if (items.length == 0) {
            throw new IllegalStateException("Missing initial moveTo()");
        }

        Vector2 vec0 = new Vector2(last[0], last[1]);
        Vector2 vec1 = new Vector2(x1, y1);
        Vector2 vec2 = new Vector2(x2, y2);
        Vector2 vec3 = new Vector2(x3, y3);

        for (float location = distance; !(location > 1); location += distance) {
            Vector2 out = new Vector2();
            Vector2 tmp = new Vector2();
            Bezier.cubic(out, location, vec0, vec1, vec2, vec3, tmp);
            lineTo(out.x, out.y);
        }
        lineTo(x3, y3);
    }

    private float calcDistance(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Transform the points in this path by matrix, and write the answer into dst. If dst is null, then the the original path is modified.
     *
     * @param currentMatrix The matrix to apply to the path
     * @param transformedPath    The transformed path is written here. If dst is null, then the the original path is modified
     */
    @Override
    public void transform(ext_Matrix currentMatrix, ext_Path transformedPath) {

    }

    @Override
    public void computeBounds(RectF pathBounds, boolean b) {

    }

    /**
     * Add a quadratic bezier from the last point, approaching control point (x1,y1), and ending at (x2,y2). If no moveTo() call has been
     * made for this contour, the first point is automatically set to (0,0).
     *
     * @param x1 The x-coordinate of the control point on a quadratic curve
     * @param y1 The y-coordinate of the control point on a quadratic curve
     * @param x2 The x-coordinate of the end point on a quadratic curve
     * @param y2 The y-coordinate of the end point on a quadratic curve
     */
    @Override
    public void quadTo(float x1, float y1, float x2, float y2) {

        // calculate distance
        float distance = calcDistance(last[0], last[1], x1, y1);
        distance += calcDistance(x1, y1, x2, y2);
        distance = Math.min(0.25f, 1 / (distance / MIN_SEGMENTH_LENGTH));

        if (items.length == 0) {
            throw new IllegalStateException("Missing initial moveTo()");
        }

        Vector2 vec0 = new Vector2(last[0], last[1]);
        Vector2 vec1 = new Vector2(x1, y1);
        Vector2 vec2 = new Vector2(x2, y2);

        for (float location = 0; location < 1; location += distance) {
            Vector2 out = new Vector2();
            Vector2 tmp = new Vector2();
            Bezier.quadratic(out, location, vec0, vec1, vec2, tmp);
            lineTo(out.x, out.y);
        }
        lineTo(x2, y2);
    }

    @Override
    public void addPath(ext_Path path, ext_Matrix combinedPathMatrix) {

    }

    @Override
    public FillType getFillType() {

        return null;
    }

    @Override
    public void setFillType(FillType clipRuleFromState) {

    }

    @Override
    public void addPath(ext_Path spanPath) {

    }

    @Override
    public void transform(ext_Matrix transform) {

    }

    private void calcSectionLength() {

        int arrayLength = ((size - 2) / 2);

        PathSectionLength = new float[arrayLength];
        if (arrayLength == 0)
            return;

        int index = 0;

        float length = PathSectionLength[index] = 0;

        for (int i = 0; i < size - 2; i += 2) {

            float x1 = items[i];
            float y1 = items[i + 1];

            float x2 = items[i + 2];
            float y2 = items[i + 3];

            float segmentLength = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

            length = PathSectionLength[index] = length + segmentLength;

            index++;
        }
    }

    /**
     * Returns an Float array with the Position of a point on the Path after given distance!<br>
     * or NULL, if the Path.length closer the given distance
     *
     * @param distance ?
     * @return float[3] <br>
     * [0]=x-value <br>
     * [1]=y-value <br>
     * [2]= angle of the Line segment from the point <br>
     */
    public float[] getPointOnPathAfter(float distance) {
        if (size < 4)
            return null; // no Path
        if (distance < 0)
            return null; // not on Path

        if (distance == 0) { // first Point of Path
            float[] ret = new float[3];
            ret[0] = items[0];
            ret[1] = items[1];

            // calc angle
            ret[2] = getAngle(0, 1);
            return ret;
        } else {
            if (distance > getLength()) {
                return null;
            }

            if (PathSectionLength == null)
                calcSectionLength();

            int ind = 0;
            do {
                if (ind > PathSectionLength.length - 1) {
                    // Path to close
                    return null;
                }
                if (PathSectionLength[ind] >= distance) {
                    // calc point on path with restDis on path line[ind]

                    float movedLength = ind == 0 ? 0 : PathSectionLength[ind - 1];

                    float dis = distance - movedLength;
                    float[] res;

                    int lineBeginn = (ind) * 2;

                    if (dis > 0 && distance > 0) {
                        res = computePointOnLine(items[lineBeginn], items[lineBeginn + 1], items[lineBeginn + 2], items[lineBeginn + 3], dis);
                    } else {
                        res = new float[]{items[ind], items[ind + 1]};
                    }

                    float[] ret = new float[3];

                    ret[0] = res[0];
                    ret[1] = res[1];

                    // calc angle
                    ret[2] = getAngle(ind, ind < size ? ind + 1 : ind - 1);

                    return ret;
                }
                ind++;
            } while (true);
        }
    }

    private float getAngle(int index1, int index2) {
        float ret = 0;

        try {
            if (index1 >= items.length || index2 >= items.length)
                return 0;

            ret = MathUtils.atan2((items[index2 * 2] - items[index1 * 2]), (items[index2 * 2 + 1] - items[index1 * 2 + 1])) * MathUtils.radiansToDegrees;

            ret = 90 - ret;
        } catch (Exception ignored) {
        }

        return ret;
    }

    public float getLength() {
        if (PathSectionLength == null)
            calcSectionLength();
        if (PathSectionLength.length < 1) {
            return 0;
        }
        return PathSectionLength[PathSectionLength.length - 1];

    }

    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if (isDisposed)
            return;
        items = null;
        pathBegins = null;
        last = null;
        PathSectionLength = null;
        isDisposed = true;
    }

    @Override
    public boolean isEmpty() {
        return items.length == 0;
    }

}
