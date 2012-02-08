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
 */

package CB_Core.Math;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Types.MoveableList;

import com.badlogic.gdx.math.Vector2;

/**
 * Eine Structur für RectF mit besonderen Methoden Speziel für die Handhabung in der Verwendung der Berechneten Grössen und Positionen
 * einzelner UI Elemente in Cachebox
 * 
 * @author Longri
 */
public class CB_RectF
{
	// Member

	/**
	 * Linke untere Ecke des Rechtecks
	 */
	private Vector2 Pos = new Vector2(0, 0);

	/**
	 * rechte obere Ecke des Rechtecks
	 */
	private Vector2 crossPos = new Vector2(0, 0);

	protected float width;
	protected float height;

	// Constructors

	/**
	 * Constructor der alle Member mit 0 initialisiert!
	 */
	public CB_RectF()
	{
		this.Pos.x = 0F;
		this.Pos.y = 0F;
		this.height = 0F;
		this.width = 0F;
	}

	/**
	 * Constructor für ein neues RectF mit Angabe der linken unteren Ecke und der Höhe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public CB_RectF(float X, float Y, float Width, float Height)
	{
		this.Pos.x = X;
		this.Pos.y = Y;
		this.width = Width;
		this.height = Height;
		calcCrossCorner();
	}

	public void setWidth(float Width)
	{
		if (this.width == Width) return;
		this.width = Width;
		calcCrossCorner();
		CallSizeChanged();
	}

	public void setHeight(float Height)
	{
		if (this.height == Height) return;
		this.height = Height;
		calcCrossCorner();
		CallSizeChanged();
	}

	public boolean setSize(SizeF Size)
	{
		return setSize(Size.width, Size.height);
	}

	/**
	 * Setzt die Werte für Height und Width. Wenn sich einer der Werte geändert hat, wird ein True zurück gegeben, ansonsten False.
	 * 
	 * @param Width
	 * @param Height
	 * @return
	 */
	public boolean setSize(float Width, float Height)
	{
		if (this.width == Width && this.height == Height) return false;
		this.width = Width;
		this.height = Height;
		calcCrossCorner();
		CallSizeChanged();
		return true;
	}

	public void setPos(Vector2 Pos)
	{
		if (this.Pos.x == Pos.x && this.Pos.y == Pos.y) return;
		this.Pos.x = Pos.x;
		this.Pos.y = Pos.y;
		calcCrossCorner();
	}

	public void offset(float offX, float offY)
	{
		float newX = this.Pos.x + offX;
		float newY = this.Pos.y + offY;

		if (this.Pos.x == newX && this.Pos.y == newY) return;
		this.Pos.x = newX;
		this.Pos.y = newY;
		calcCrossCorner();
	}

	public float getX()
	{
		return this.Pos.x;
	}

	public float getY()
	{
		return this.Pos.y;
	}

	public float getWidth()
	{
		return this.width;
	}

	public float getHeight()
	{
		return this.height;
	}

	public Vector2 getPos()
	{
		return this.Pos;
	}

	/**
	 * Gibt die Position der rechten oberen Ecke zurück
	 * 
	 * @return Vector2
	 */
	public Vector2 getCrossPos()
	{
		return this.crossPos;
	}

	/**
	 * Berechnet die rechte obere Ecke
	 */
	private void calcCrossCorner()
	{
		this.crossPos.x = this.Pos.x + this.width;
		this.crossPos.y = this.Pos.y + this.height;
	}

	private boolean contains(Vector2 ret)
	{
		return contains(ret.x, ret.y);
	}

	public boolean contains(float x, float y)
	{
		return width > 0 && height > 0 // check for empty first
				&& x >= this.Pos.x && x <= this.crossPos.x && y >= this.Pos.y && y <= this.crossPos.y;
	}

	private ArrayList<SizeChangedEvent> list = new ArrayList<SizeChangedEvent>();

	public void Add(SizeChangedEvent event)
	{
		list.add(event);
	}

	public void Remove(SizeChangedEvent event)
	{
		list.remove(event);
	}

	public void CallSizeChanged()
	{
		resize(this.width, this.height);

		for (SizeChangedEvent event : list)
		{
			event.sizeChanged();
		}

	}

	public void resize(float width, float height)
	{
	}

	public boolean equals(CB_RectF rec)
	{
		if (this.Pos.x != rec.Pos.x || this.Pos.y != rec.Pos.y) return false;
		if (this.width != rec.width || this.height != this.height) return false;
		return true;
	}

	public CB_RectF copy()
	{
		return new CB_RectF(this.Pos.x, this.Pos.y, width, height);
	}

	public void setY(float i)
	{
		if (this.Pos.y == i) return;
		this.Pos.y = i;
		calcCrossCorner();
	}

	public void setX(float i)
	{
		if (this.Pos.x == i) return;
		this.Pos.x = i;
		calcCrossCorner();
	}

	/**
	 * Setzt Height und Width auf die nächst größere Potenz von 2
	 */
	public void setPO2()
	{
		int PO2width = getNextHighestPO2((int) this.width);
		int PO2height = getNextHighestPO2((int) this.height);

		setSize(PO2width, PO2height);
	}

	/**
	 * Calculates the next highest power of two for a given integer.
	 * 
	 * @param n
	 *            the number
	 * @return a power of two equal to or higher than n
	 */
	public static int getNextHighestPO2(int n)
	{
		n -= 1;
		n = n | (n >> 1);
		n = n | (n >> 2);
		n = n | (n >> 4);
		n = n | (n >> 8);
		n = n | (n >> 16);
		n = n | (n >> 32);
		return n + 1;
	}

	public float getLeft()
	{
		return this.Pos.x;
	}

	public float getTop()
	{
		return this.crossPos.y;
	}

	public float getBottom()
	{
		return this.Pos.y;
	}

	public float getRight()
	{
		return this.crossPos.x;
	}

	public CB_RectF ScaleCenter(float ScaleFactor)
	{
		return ScaleCenter(this, ScaleFactor);
	}

	public static CB_RectF ScaleCenter(CB_RectF rectangle, float ScaleFactor)
	{
		float newWidth = (int) (rectangle.getWidth() * ScaleFactor);
		float newHeight = (int) (rectangle.getHeight() * ScaleFactor);
		float newX = rectangle.Pos.x + ((rectangle.getWidth() - newWidth) / 2);
		float newY = rectangle.Pos.y + ((rectangle.getHeight() - newHeight) / 2);
		return new CB_RectF(newX, newY, newWidth, newHeight);

	}

	/**
	 * Gibt den ersten Schnittpunkt des Rechtecks zwichen den Punkten P1 und P2 zurück! <img src="doc-files/rec-intersection.png" width=537
	 * height=307>
	 * 
	 * @param P1
	 *            = start Punkt der Linie
	 * @param P2
	 *            = End Punkt der Line
	 * @return Punkt (b) da dieser als erster Schnittpunkt gefunden wird.
	 */
	public Vector2 getIntersection(Vector2 P1, Vector2 P2)
	{
		return getIntersection(P1, P2, 1);
	}

	/**
	 * Gibt den ersten Schnittpunkt des Rechtecks zwichen den Punkten P1 und P2 zurück! </br> Wobei die als int übergebene Nummer der Gerade
	 * des Rechtecks als erstes überprüft wird. </br> <img src="doc-files/rec-intersection.png" width=537 height=307>
	 * 
	 * @param P1
	 *            = start Punkt der Linie
	 * @param P2
	 *            = End Punkt der Line
	 * @param first
	 * @return Punkt (b) wenn first=1 </br> Punkt (a) wenn first=2,3 oder 4 </br>
	 */
	public Vector2 getIntersection(Vector2 P1, Vector2 P2, int first)
	{

		// Array mit Geraden Nummern füllen
		if (Geraden.size() < 4)
		{
			Geraden.add(1);
			Geraden.add(2);
			Geraden.add(3);
			Geraden.add(4);
		}

		Geraden.MoveItemFirst(Geraden.indexOf(first));

		Vector2 ret = new Vector2();

		for (Iterator<Integer> i = Geraden.iterator(); i.hasNext();)
		{
			switch (i.next())
			{
			case 1:

				if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, Pos, new Vector2(crossPos.x, Pos.y), ret))
				{
					if (contains(ret)) return ret; // 1 unten
				}
				break;

			case 2:
				if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, Pos, new Vector2(Pos.x, crossPos.y), ret))
				{
					if (contains(ret)) return ret; // 2 links
				}
				break;

			case 3:
				if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, crossPos, new Vector2(crossPos.x, Pos.y), ret))
				{
					if (contains(ret)) return ret; // 3 rechts
				}

				break;

			case 4:
				if (com.badlogic.gdx.math.Intersector.intersectSegments(P1, P2, crossPos, new Vector2(Pos.x, crossPos.y), ret))
				{
					if (contains(ret)) return ret; // 4 oben
				}
				break;
			}
		}

		return null;
	}

	private static MoveableList<Integer> Geraden = new MoveableList<Integer>();

}
