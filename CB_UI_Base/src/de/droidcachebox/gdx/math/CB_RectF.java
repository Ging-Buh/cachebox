/*
 * Copyright (C) 2011-2022 team-cachebox.de
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

import com.badlogic.gdx.math.Vector2;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Eine Struktur für RectF mit besonderen Methoden speziell für die Handhabung in der Verwendung der berechneten Grössen und Positionen einzelner UI Elemente in Cachebox
 *
 * @author Longri
 */
public class CB_RectF {
    private CopyOnWriteArrayList<SizeChangedEvent> sizeChangedEvents;
    /**
     * Holds all values <br>
     * <br>
     * [0] = Pos.x <br>
     * [1] = Pos.y <br>
     * [2] = width <br>
     * [3] = height <br>
     * [4] = halfWidth <br>
     * [5] = halfHeight <br>
     * [6] = crossPos.x <br>
     * [7] = crossPos.x <br>
     * [8] = centerPos.x <br>
     * [9] = centerPos.x <br>
     */
    private float[] member;

    public CB_RectF() {
        member = new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    }

    public CB_RectF(SizeF size) {
        member = new float[10];
        member[0] = 0f;
        member[1] = 0f;
        member[2] = size.getWidth();
        member[3] = size.getHeight();
        calcCrossCorner();
    }

    public CB_RectF(float x, float y, float width, float height) {
        member = new float[10];
        member[0] = x;
        member[1] = y;
        member[2] = width;
        member[3] = height;
        calcCrossCorner();
    }

    public CB_RectF(float x, float y, float sideLength) {
        member = new float[10];
        member[0] = x;
        member[1] = y;
        member[2] = sideLength;
        member[3] = sideLength;
        calcCrossCorner();
    }

    public CB_RectF(CB_RectF rec) {
        member = new float[10];
        if (rec != null && rec.member != null) {
            System.arraycopy(rec.member, 0, member, 0, 10);
        }
    }

    public static CB_RectF scaleCenter(CB_RectF rectangle, float ScaleFactor) {
        float newWidth = rectangle.getWidth() * ScaleFactor;
        float newHeight = rectangle.getHeight() * ScaleFactor;
        float newX = rectangle.member[0] + ((rectangle.getWidth() - newWidth) / 2);
        float newY = rectangle.member[1] + ((rectangle.getHeight() - newHeight) / 2);
        return new CB_RectF(newX, newY, newWidth, newHeight);
    }

    /**
     * Berechnet die rechte obere Ecke
     */
    private void calcCrossCorner() {
        if (member == null)
            throw new IllegalStateException("Is Disposed");
        member[4] = member[2] / 2;
        member[5] = member[3] / 2;

        member[6] = member[0] + member[2];
        member[7] = member[1] + member[3];
        member[8] = member[0] + member[4];
        member[9] = member[1] + member[5];
    }

    public float getHalfWidth() {
        return member[4];
    }

    public float getHalfHeight() {
        return member[5];
    }

    public boolean setSize(SizeF size) {
        return setSize(size.getWidth(), size.getHeight());
    }

    /**
     * Setzt die Werte für Height und Width. Wenn sich einer der Werte geändert hat, wird ein True zurück gegeben, ansonsten False.
     */
    public boolean setSize(float width, float height) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! false; // isDisposed!
        if (member[2] == width && member[3] == height)
            return false;
        member[2] = width;
        member[3] = height;
        calcCrossCorner();
        sizeChanged();
        return true;
    }

    public boolean setSize(float sideLength) {
        return setSize(sideLength, sideLength);
    }

    public boolean setSize(CB_RectF rec) {
        setSize(rec.member[2], rec.member[3]);
        return true;
    }

    public CB_RectF offset(Vector2 Offset) {
        return offset(Offset.x, Offset.y);
    }

    public CB_RectF offset(float offX, float offY) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! null; // isDisposed!
        member[0] += offX;
        member[1] += offY;
        calcCrossCorner();
        return this;
    }

    public float getX() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[0];
    }

    public void setX(float i) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!; // isDisposed!
        if (member[0] == i)
            return;
        member[0] = i;
        calcCrossCorner();
        sizeChanged();
    }

    public float getY() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[1];
    }

    public void setY(float i) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!; // isDisposed!
        if (member[1] == i)
            return;
        member[1] = i;
        calcCrossCorner();
        sizeChanged();
    }

    public float getWidth() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[2];
    }

    public void setWidth(float Width) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!;
        if (member[2] == Width)
            return;
        member[2] = Width;
        calcCrossCorner();
        sizeChanged();
    }

    public float getHeight() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[3];
    }

    public void setHeight(float Height) {
        if (member[3] == Height)
            return;
        member[3] = Height;
        calcCrossCorner();
        sizeChanged();
    }

    public boolean contains(Vector2 ret) {
        if (ret == null)
            return false;
        return contains(ret.x, ret.y);
    }

    public boolean contains(float x, float y) {
        if (member == null)
            return false;

        try {
            // runde
            float rX = Math.round(x);
            float rY = Math.round(y);
            float rTX = Math.round(member[0]);
            float rTY = Math.round(member[1]);
            float rTCX = Math.round(member[6]);
            float rTCY = Math.round(member[7]);

            return member[2] > 0 && member[3] > 0 // check for empty first
                    && rX >= rTX && rX <= rTCX && rY >= rTY && rY <= rTCY;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * liefert True, wenn das übergebene Rechteck kommplett in diese rechteck Passt.
     */
    public boolean contains(CB_RectF rec) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! false; // isDisposed!
        if (rec == null)
            return false;
        boolean ret = contains(rec.member[0], rec.member[1]);
        ret &= contains(rec.member[6], rec.member[7]);

        return ret;
    }

    public void addListener(SizeChangedEvent listener) {
        if (sizeChangedEvents == null) sizeChangedEvents = new CopyOnWriteArrayList<>();
        sizeChangedEvents.add(listener);
    }

    private void sizeChanged() {
        resize(member[2], member[3]);
        if (sizeChangedEvents != null)
            for (SizeChangedEvent listener : sizeChangedEvents) {
                listener.sizeChanged();
            }
    }

    public void resize(float width, float height) {
    }

    // /**
    // * Setzt Height und Width auf die nächst größere Potenz von 2
    // */
    // public void setPO2()
    // {
    // int PO2width = getNextHighestPO2((int) member[2]);
    // int PO2height = getNextHighestPO2((int) member[3]);
    //
    // setSize(PO2width, PO2height);
    // }

    // /**
    // * Calculates the next highest power of two for a given integer.
    // *
    // * @param n
    // * the number
    // * @return a power of two equal to or higher than n
    // */
    // public static int getNextHighestPO2(int n)
    // {
    // n -= 1;
    // n = n | (n >> 1);
    // n = n | (n >> 2);
    // n = n | (n >> 4);
    // n = n | (n >> 8);
    // n = n | (n >> 16);
    // n = n | (n >> 32);
    // return n + 1;
    // }

    public boolean equals(CB_RectF rec) {
        if (member == null || rec.member == null)
            return false; // any is disposed!

        // Compare only x,y,width and height
        if (member[0] != rec.member[0])
            return false;
        if (member[1] != rec.member[1])
            return false;
        if (member[2] != rec.member[2])
            return false;
        return member[3] == rec.member[3];
    }

    public CB_RectF scaleCenter(float ScaleFactor) {
        return scaleCenter(this, ScaleFactor);
    }

    /**
     * Gibt den ersten Schnittpunkt des Rechtecks zwichen den Punkten P1 und P2 zurück! <img src="doc-files/rec-intersection.png"
     * width=537 height=307>
     *
     * @param P1 = start Punkt der Linie
     * @param P2 = End Punkt der Line
     * @return Punkt (b) da dieser als erster Schnittpunkt gefunden wird.
     */
    public Vector2 getIntersection(Vector2 P1, Vector2 P2) {
        return getIntersection(P1, P2, 1);
    }

    /**
     * Gibt den ersten Schnittpunkt des Rechtecks zwichen den Punkten P1 und P2 zurück! </br> Wobei die als int übergebene Nummer der
     * Gerade des Rechtecks als erstes überprüft wird. </br> <img src="doc-files/rec-intersection.png" width=537 height=307>
     *
     * @param P1    = start Punkt der Linie
     * @param P2    = End Punkt der Line
     * @param first line of rectangle to be used
     * @return Punkt (b) wenn first=1 </br> Punkt (a) wenn first=2,3 oder 4 </br>
     */
    public Vector2 getIntersection(Vector2 P1, Vector2 P2, int first) {
        Vector2 ret = new Vector2();
        int i = first;
        for (int j = 1; j <= 4; j++) {
            switch (i) {
                case 1:
                    if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, new Vector2(member[0], member[1]), new Vector2(member[6], member[1]), ret)) {
                        if (contains(ret))
                            return ret; // 1 unten
                    }
                    break;
                case 2:
                    if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, new Vector2(member[0], member[1]), new Vector2(member[0], member[7]), ret)) {
                        if (contains(ret))
                            return ret; // 2 links
                    }
                    break;
                case 3:
                    if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, new Vector2(member[6], member[7]), new Vector2(member[6], member[1]), ret)) {
                        if (contains(ret))
                            return ret; // 3 rechts
                    }
                    break;
                case 4:
                    if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, new Vector2(member[6], member[7]), new Vector2(member[0], member[7]), ret)) {
                        if (contains(ret))
                            return ret; // 4 oben
                    }
                    break;
            }
            i++;
            if (i > 4) i = 1;
        }
        return null;
    }

    /**
     * Returns the smallest X coordinate of the framing rectangle of the <code>CB_RectF</code> in <code>double</code> precision.
     *
     * @return the smallest x coordinate of the framing rectangle of the <code>CB_RectF</code>.
     */
    private float getMinX() {
        return getX();
    }

    /**
     * Returns the smallest Y coordinate of the framing rectangle of the <code>CB_RectF</code> in <code>double</code> precision.
     *
     * @return the smallest y coordinate of the framing rectangle of the <code>CB_RectF</code>.
     */
    private float getMinY() {
        return getY();
    }

    /**
     * Returns the lowest X position for the next object (without collision)
     *
     * @return the highest x coordinate of the framing rectangle of the <code>CB_RectF</code>.
     */
    public float getMaxX() {
        return getX() + getWidth();
    }

    /**
     * Returns the largest Y coordinate of the framing rectangle of the <code>CB_RectF</code> in <code>double</code> precision.
     *
     * @return the largest y coordinate of the framing rectangle of the <code>CB_RectF</code>.
     */
    public float getMaxY() {
        return getY() + getHeight();
    }

    public CB_RectF createIntersection(CB_RectF rec) {
        float x1 = Math.max(getMinX(), rec.getMinX());
        float y1 = Math.max(getMinY(), rec.getMinY());
        float x2 = Math.min(getMaxX(), rec.getMaxX());
        float y2 = Math.min(getMaxY(), rec.getMaxY());
        return new CB_RectF(x1, y1, x2 - x1, y2 - y1);
    }

    public SizeF getSize() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! new SizeF(); // isDisposed!
        return new SizeF(member[2], member[3]);
    }

    public void setRec(CB_RectF rec) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!; // isDisposed!
        if (rec == null)
            return;
        // chk of changes
        if (equals(rec))
            return;
        System.arraycopy(rec.member, 0, member, 0, 10);
        sizeChanged();
    }

    @Override
    public String toString() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! "disposed Rec"; // isDisposed!
        return "rec X,Y/Width,Height = " + getX() + "," + getY() + "/" + member[2] + "," + member[3];
    }

    public void setPos(float x, float y) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!; // isDisposed!
        // chk of changes
        if (member[0] == x && member[1] == y)
            return;

        member[0] = x;
        member[1] = y;
        calcCrossCorner();
        sizeChanged();
    }

    public float getCenterPosX() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[8];
    }

    public float getCenterPosY() {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed! 0; // isDisposed!
        return member[9];
    }

    public void set(float x, float y, float width, float height) {
        if (member == null)
            throw new IllegalStateException("Is Disposed"); // isDisposed!
        // chk of changes
        if (member[0] == x && member[1] == y && member[2] == width && member[3] == height)
            return;
        member[0] = x;
        member[1] = y;
        member[2] = width;
        member[3] = height;
        calcCrossCorner();
        sizeChanged();
    }

    public void dispose() {
        if (sizeChangedEvents != null)
            sizeChangedEvents.clear();
        member = null;
    }
}
