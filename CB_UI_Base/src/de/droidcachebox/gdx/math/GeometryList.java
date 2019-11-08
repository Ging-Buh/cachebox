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
package de.droidcachebox.gdx.math;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A List of IGeometry objects.<br>
 * <br>
 * All vertices and triangle indices of all objects combined to one list!
 *
 * @author Longri
 */
public class GeometryList implements IGeometry {
    public boolean isDirty = true;
    protected AtomicBoolean isDisposed = new AtomicBoolean(false);
    private ArrayList<IGeometry> list = new ArrayList<IGeometry>();
    private float[] vertices;
    private short[] triangles;

    public void add(IGeometry geometry) {
        list.add(geometry);
        isDirty = true;
    }

    public void Compute() {
        // calculate array length
        int verticesCount = 0;
        int triangleCount = 0;
        for (IGeometry element : list) {
            verticesCount += element.getVertices().length;
            triangleCount += element.getTriangles().length;
        }

        vertices = new float[verticesCount];
        triangles = new short[triangleCount];

        int verticesIndex = 0;
        short triangleIndex = 0;
        short lastTriangleIndex = 0;
        for (IGeometry element : list) {
            int verticeCount = 0;
            for (float value : element.getVertices()) {
                vertices[verticesIndex++] = value;
                verticeCount++;
            }

            for (short value : element.getTriangles()) {
                triangles[triangleIndex++] = (short) (lastTriangleIndex + value);
            }
            lastTriangleIndex += (verticeCount / 2);
        }
        isDirty = false;
    }

    @Override
    public float[] getVertices() {
        if (isDirty)
            Compute();
        return vertices;
    }

    @Override
    public short[] getTriangles() {
        if (isDirty)
            Compute();
        return triangles;
    }

    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void dispose() {
        synchronized (isDisposed) {
            if (isDisposed.get())
                return;
            vertices = null;
            triangles = null;
            if (list != null) {
                for (IGeometry ge : list) {
                    ge.dispose();
                }
                list.clear();
            }
            list = null;
            isDisposed.set(true);
        }
    }
}
