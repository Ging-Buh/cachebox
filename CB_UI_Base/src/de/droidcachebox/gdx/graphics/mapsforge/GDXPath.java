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

import de.droidcachebox.gdx.math.RectF;

/**
 * @author Longri
 */
public interface GDXPath extends org.mapsforge.core.graphics.Path {
    @Override
    void lineTo(float x, float y);

    @Override
    void moveTo(float x, float y);

    void close();

    void cubicTo(float x1, float y1, float x2, float y2, float x, float y);

    void transform(GDXMatrix currentMatrix, GDXPath transformedPath);

    void computeBounds(RectF pathBounds, boolean b);

    void quadTo(float x1, float y1, float x2, float y2);

    void addPath(GDXPath path, GDXMatrix combinedPathMatrix);

    FillType getFillType();

    void setFillType(FillType clipRuleFromState);

    void addPath(GDXPath spanPath);

    void transform(GDXMatrix transform);

    enum FillType {
        WINDING, EVEN_ODD

    }
}
