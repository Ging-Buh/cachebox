/*
 * Copyright (C) 2011-2012 team-cachebox.de
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
 *
 */

package de.droidcachebox.gdx.math;

/**
 * Die Size Structur enth�lt die Member width und height
 *
 * @author Longri
 */
public class SizeF {
    private float width;
    private float height;

    /**
     * Constructor
     *
     * @param width  as float
     * @param height as float
     */
    public SizeF(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public SizeF() {
        this.width = 0f;
        this.height = 0f;
    }

    public SizeF(SizeF size) {
        width = size.width;
        height = size.height;
    }

    public SizeF(float side) {
        this(side, side);
    }

    public void setSize(float width, float height) {
        if (this.width == width && this.height == height)
            return;
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getHalfWidth() {
        return width / 2f;
    }

    public float getHalfHeight() {
        return height / 2f;
    }

    /*
    public float getWidth48() {
        return width / 4.8f;
    }
     */

    public float getHeight48() {
        return height / 4.8f;
    }

    public void scale(float f) {
        width = width * f;
        height = height * f;
    }

    public CB_RectF getBounds() {
        return new CB_RectF(0, 0, width, height);
    }

    public CB_RectF getBounds(int x, int y) {
        return new CB_RectF(x, y, width + x, height + y);
    }

    public CB_RectF getBounds(int x, int y, int k, int l) {
        return new CB_RectF(x, y, width + x + k, height + y + l);
    }

}
