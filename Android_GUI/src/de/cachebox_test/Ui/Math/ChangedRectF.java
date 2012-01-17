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

package de.cachebox_test.Ui.Math;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

import de.cachebox_test.Ui.SizeChangedEvent;

/**
 * Eine Structur für RectF mit besonderen Methoden Speziel für die Handhabung in der Verwendung der Berechneten Grössen und Positionen
 * einzelner UI Elemente in Cachebox
 * 
 * @author Longri
 */
public class ChangedRectF
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

	private float width;
	private float height;

	// Constructors

	/**
	 * Constructor der alle Member mit 0 initialisiert!
	 */
	public ChangedRectF()
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
	public ChangedRectF(float X, float Y, float Width, float Height)
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

	public boolean contains(float x, float y)
	{
		return width > 0 && height > 0 // check for empty first
				&& x >= this.Pos.x && x < this.crossPos.x && y >= this.Pos.y && y < this.crossPos.y;
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
		for (SizeChangedEvent event : list)
		{
			event.sizeChanged();
		}

	}

	public boolean equals(ChangedRectF rec)
	{
		if (this.Pos.x != rec.Pos.x || this.Pos.y != rec.Pos.y) return false;
		if (this.width != rec.width || this.height != this.height) return false;
		return true;
	}

	public ChangedRectF copy()
	{
		return new ChangedRectF(this.Pos.x, this.Pos.y, width, height);
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

}
