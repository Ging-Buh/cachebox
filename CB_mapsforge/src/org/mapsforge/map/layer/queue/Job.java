/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2014 Ludwig M Brinckmann
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.layer.queue;

import org.mapsforge.core.model.Tile;

public class Job {
	public final boolean hasAlpha;
	public final Tile tile;
	public final int tileSize;

	protected Job(Tile tile, int tileSize, boolean hasAlpha) {
		if (tile == null) {
			throw new IllegalArgumentException("tile must not be null");
		}

		this.tile = tile;
		this.tileSize = tileSize;
		this.hasAlpha = hasAlpha;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof Job)) {
			return false;
		}
		Job other = (Job) obj;
		if (this.tileSize != other.tileSize) {
			return false;
		}
		if (this.hasAlpha != other.hasAlpha) {
			return false;
		}
		return this.tile.equals(other.tile);
	}

	@Override
	public int hashCode() {
		return 31 * this.tile.hashCode() + this.tileSize;
	}
}
