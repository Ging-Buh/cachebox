/*
 * Copyright (C) 2011-2020 team-cachebox.de
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

/**
 * Eine Structur f�r RectF mit besonderen Methoden speziell f�r die Handhabung in der Verwendung der berechneten Gr�ssen und Positionen einzelner UI Elemente
 *
 * @author Longri
 */
public class CB_Rect {
    // Member

    /**
     * rechte obere Ecke des Rechtecks
     */
    private final intPos crossPos = new intPos(0, 0);
    private final ArrayList<SizeChangedEvent> list = new ArrayList<SizeChangedEvent>();
    /**
     * Linke untere Ecke des Rechtecks
     */
    private intPos Pos = new intPos(0, 0);
    private int width;

    // Constructors
    private int height;

    /**
     * Constructor der alle Member mit 0 initialisiert!
     */
    public CB_Rect() {
        this.Pos.x = 0;
        this.Pos.y = 0;
        this.height = 0;
        this.width = 0;
    }

    /**
     * Constructor f�r ein neues RectF mit Angabe der linken unteren Ecke und der H�he und Breite
     *
     * @param X
     * @param Y
     * @param Width
     * @param Height
     */
    public CB_Rect(int X, int Y, int Width, int Height) {
        this.Pos.x = X;
        this.Pos.y = Y;
        this.width = Width;
        this.height = Height;
        calcCrossCorner();
    }

    public CB_Rect(intPos leftTop, intPos rightBotom) {
        this.Pos = leftTop;
        this.width = rightBotom.x - leftTop.x;
        this.height = rightBotom.y - leftTop.y;
        calcCrossCorner();
    }

    public CB_Rect(CB_Rect rChkBounds) {
        this.Pos.x = rChkBounds.getX();
        this.Pos.y = rChkBounds.getY();
        this.width = rChkBounds.getWidth();
        this.height = rChkBounds.getHeight();
        calcCrossCorner();
    }

    /**
     * Calculates the next highest power of two for a given integer.
     *
     * @param n the number
     * @return a power of two equal to or higher than n
     */
    public static int getNextHighestPO2(int n) {
        n -= 1;
        n = n | (n >> 1);
        n = n | (n >> 2);
        n = n | (n >> 4);
        n = n | (n >> 8);
        n = n | (n >> 16);
        n = n | (n >> 32);
        return n + 1;
    }

    public static CB_Rect ScaleCenter(CB_Rect rectangle, double ScaleFactor) {
        int newWidth = (int) (rectangle.getWidth() * ScaleFactor);
        int newHeight = (int) (rectangle.getHeight() * ScaleFactor);
        int newX = rectangle.Pos.x + ((rectangle.getWidth() - newWidth) / 2);
        int newY = rectangle.Pos.y + ((rectangle.getHeight() - newHeight) / 2);
        return new CB_Rect(newX, newY, newWidth, newHeight);

    }

    public boolean setSize(Size size) {
        return setSize(size.width, size.height);
    }

    /**
     * Setzt die Werte f�r Height und Width. Wenn sich einer der Werte ge�ndert hat, wird ein True zur�ck gegeben, ansonsten False.
     *
     * @param Width
     * @param Height
     * @return
     */
    public boolean setSize(int Width, int Height) {
        if (this.width == Width && this.height == Height)
            return false;
        this.width = Width;
        this.height = Height;
        calcCrossCorner();
        CallSizeChanged();
        return true;
    }

    public CB_Rect offset(int offX, int offY) {
        int newX = this.Pos.x + offX;
        int newY = this.Pos.y + offY;

        if (this.Pos.x == newX && this.Pos.y == newY)
            return this.copy();
        this.Pos.x = newX;
        this.Pos.y = newY;
        calcCrossCorner();
        return this.copy();
    }

    public int getX() {
        return this.Pos.x;
    }

    public void setX(int i) {
        if (this.Pos.x == i)
            return;
        this.Pos.x = i;
        calcCrossCorner();
    }

    public int getY() {
        return this.Pos.y;
    }

    public void setY(int i) {
        if (this.Pos.y == i)
            return;
        this.Pos.y = i;
        calcCrossCorner();
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int Width) {
        if (this.width == Width)
            return;
        this.width = Width;
        calcCrossCorner();
        CallSizeChanged();
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int Height) {
        if (this.height == Height)
            return;
        this.height = Height;
        calcCrossCorner();
        CallSizeChanged();
    }

    public intPos getPos() {
        return this.Pos;
    }

    public void setPos(intPos Pos) {
        if (this.Pos.x == Pos.x && this.Pos.y == Pos.y)
            return;
        this.Pos.x = Pos.x;
        this.Pos.y = Pos.y;
        calcCrossCorner();
    }

    /**
     * Gibt die Position der rechten oberen Ecke zur�ck
     *
     * @return intPos
     */
    public intPos getCrossPos() {
        return this.crossPos;
    }

    /**
     * Berechnet die rechte obere Ecke
     */
    private void calcCrossCorner() {
        this.crossPos.x = this.Pos.x + this.width;
        this.crossPos.y = this.Pos.y + this.height;
    }

    public boolean contains(int x, int y) {
        return width > 0 && height > 0 // check for empty first
                && x >= this.Pos.x && x < this.crossPos.x && y >= this.Pos.y && y < this.crossPos.y;
    }

    public void Add(SizeChangedEvent event) {
        list.add(event);
    }

    public void Remove(SizeChangedEvent event) {
        list.remove(event);
    }

    public void CallSizeChanged() {
        for (SizeChangedEvent event : list) {
            event.sizeChanged();
        }

    }

    public boolean equals(CB_Rect rec) {
        if (this.Pos.x != rec.Pos.x || this.Pos.y != rec.Pos.y)
            return false;
        if (this.width != rec.width || this.height != rec.height)
            return false;
        return true;
    }

    public CB_Rect copy() {
        return new CB_Rect(this.Pos.x, this.Pos.y, width, height);
    }

    /**
     * Setzt Height und Width auf die n�chst gr��ere Potenz von 2
     */
    public void setPO2() {
        int PO2width = getNextHighestPO2(this.width);
        int PO2height = getNextHighestPO2(this.height);

        setSize(PO2width, PO2height);
    }

    public int getLeft() {
        return this.Pos.x;
    }

    public int getBottom() {
        return this.Pos.y;
    }

    public int getTop() {
        return this.crossPos.y;
    }

    public int getRight() {
        return this.crossPos.x;
    }

    public CB_Rect ScaleCenter(double ScaleFactor) {
        return ScaleCenter(this, ScaleFactor);
    }

    public CB_RectF asFloat() {
        return new CB_RectF(Pos.x, Pos.y, width, height);
    }

    public int getMaxX() {
        return this.crossPos.x;
    }

    public int getMaxY() {
        return this.crossPos.y;
    }

}
