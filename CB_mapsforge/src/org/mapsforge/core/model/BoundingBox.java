/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
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
package org.mapsforge.core.model;

import java.io.Serializable;

import org.mapsforge.core.util.LatLongUtils;

/**
 * A BoundingBox represents an immutable set of two latitude and two longitude coordinates.
 */
public class BoundingBox implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final double C=1e6;
	
	/**
	 * Creates a new BoundingBox from a comma-separated string of coordinates in the order minLat, minLon, maxLat,
	 * maxLon. All coordinate values must be in degrees.
	 * 
	 * @param boundingBoxString
	 *            the string that describes the BoundingBox.
	 * @return a new BoundingBox with the given coordinates.
	 * @throws IllegalArgumentException
	 *             if the string cannot be parsed or describes an invalid BoundingBox.
	 */
	public static BoundingBox fromString(String boundingBoxString) {
		double[] coordinates = LatLongUtils.parseCoordinateString(boundingBoxString, 4);
		return new BoundingBox(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
	}

	/**
	 * The maximum latitude coordinate of this BoundingBox in degrees.
	 */
	private final int maxLatitude;

	/**
	 * The maximum longitude coordinate of this BoundingBox in degrees.
	 */
	private final int maxLongitude;

	/**
	 * The minimum latitude coordinate of this BoundingBox in degrees.
	 */
	private final int minLatitude;

	/**
	 * The minimum longitude coordinate of this BoundingBox in degrees.
	 */
	private final int minLongitude;

	/**
	 * @param minLatitude
	 *            the minimum latitude coordinate in degrees.
	 * @param minLongitude
	 *            the minimum longitude coordinate in degrees.
	 * @param maxLatitude
	 *            the maximum latitude coordinate in degrees.
	 * @param maxLongitude
	 *            the maximum longitude coordinate in degrees.
	 * @throws IllegalArgumentException
	 *             if a coordinate is invalid.
	 */
	public BoundingBox(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
		LatLongUtils.validateLatitude(minLatitude);
		LatLongUtils.validateLongitude(minLongitude);
		LatLongUtils.validateLatitude(maxLatitude);
		LatLongUtils.validateLongitude(maxLongitude);

		if (minLatitude > maxLatitude) {
			throw new IllegalArgumentException("invalid latitude range: " + minLatitude + ' ' + maxLatitude);
		} else if (minLongitude > maxLongitude) {
			throw new IllegalArgumentException("invalid longitude range: " + minLongitude + ' ' + maxLongitude);
		}

		this.minLatitude = (int) (minLatitude*C);
		this.minLongitude = (int) (minLongitude*C);
		this.maxLatitude = (int) (maxLatitude*C);
		this.maxLongitude = (int) (maxLongitude*C);
	}
	
	/**
	 * @param minLatitude
	 *            the minimum latitude coordinate in degrees.
	 * @param minLongitude
	 *            the minimum longitude coordinate in degrees.
	 * @param maxLatitude
	 *            the maximum latitude coordinate in degrees.
	 * @param maxLongitude
	 *            the maximum longitude coordinate in degrees.
	 * @throws IllegalArgumentException
	 *             if a coordinate is invalid.
	 */
	public BoundingBox(int minLatitude, int minLongitude, int maxLatitude, int maxLongitude) {
	

		if (minLatitude > maxLatitude) {
			throw new IllegalArgumentException("invalid latitude range: " + minLatitude + ' ' + maxLatitude);
		} else if (minLongitude > maxLongitude) {
			throw new IllegalArgumentException("invalid longitude range: " + minLongitude + ' ' + maxLongitude);
		}

		this.minLatitude = minLatitude;
		this.minLongitude = minLongitude;
		this.maxLatitude = maxLatitude;
		this.maxLongitude =maxLongitude;
	}


	/**
	 * @param latLong
	 *            the LatLong whose coordinates should be checked.
	 * @return true if this BoundingBox contains the given LatLong, false otherwise.
	 */
	public boolean contains(LatLong latLong) {
		return this.minLatitude <= latLong.getIntLatitude() && this.maxLatitude >= latLong.getIntLatitude()
				&& this.minLongitude <= latLong.getIntLongitude() && this.maxLongitude >= latLong.getIntLongitude();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof BoundingBox)) {
			return false;
		}
		BoundingBox other = (BoundingBox) obj;
		if (Double.doubleToLongBits(this.getMaxLatitude()) != Double.doubleToLongBits(other.getMaxLatitude())) {
			return false;
		} else if (Double.doubleToLongBits(this.getMaxLongitude()) != Double.doubleToLongBits(other.getMaxLongitude())) {
			return false;
		} else if (Double.doubleToLongBits(this.getMinLatitude()) != Double.doubleToLongBits(other.getMinLatitude())) {
			return false;
		} else if (Double.doubleToLongBits(this.getMinLongitude()) != Double.doubleToLongBits(other.getMinLongitude())) {
			return false;
		}
		return true;
	}

	/**
	 * @return a new LatLong at the horizontal and vertical center of this BoundingBox.
	 */
	public LatLong getCenterPoint() {
		double latitudeOffset = (this.getMaxLatitude() - this.getMinLatitude()) / 2;
		double longitudeOffset = (this.getMaxLongitude() - this.getMinLongitude()) / 2;
		return new LatLong(this.getMinLatitude() + latitudeOffset, this.getMinLongitude() + longitudeOffset);
	}

	/**
	 * @return the latitude span of this BoundingBox in degrees.
	 */
	public double getLatitudeSpan() {
		return this.getMaxLatitude() - this.getMinLatitude();
	}

	/**
	 * @return the longitude span of this BoundingBox in degrees.
	 */
	public double getLongitudeSpan() {
		return this.getMaxLongitude() - this.getMinLongitude();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.getMaxLatitude());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.getMaxLongitude());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.getMinLatitude());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.getMinLongitude());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * @param boundingBox
	 *            the BoundingBox which should be checked for intersection with this BoundingBox.
	 * @return true if this BoundingBox intersects with the given BoundingBox, false otherwise.
	 */
	public boolean intersects(BoundingBox boundingBox) {
		if (this == boundingBox) {
			return true;
		}

		return this.getMaxLatitude() >= boundingBox.getMinLatitude() && this.getMaxLongitude() >= boundingBox.getMinLongitude()
				&& this.getMinLatitude() <= boundingBox.getMaxLatitude() && this.getMinLongitude() <= boundingBox.getMaxLongitude();
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("minLatitude=");
		stringBuilder.append(this.getMinLatitude());
		stringBuilder.append(", minLongitude=");
		stringBuilder.append(this.getMinLongitude());
		stringBuilder.append(", maxLatitude=");
		stringBuilder.append(this.getMaxLatitude());
		stringBuilder.append(", maxLongitude=");
		stringBuilder.append(this.getMaxLongitude());
		return stringBuilder.toString();
	}

	public double getMaxLatitude() {
		return maxLatitude/C;
	}

	public double getMaxLongitude() {
		return maxLongitude/C;
	}

	public double getMinLatitude() {
		return minLatitude/C;
	}

	public double getMinLongitude() {
		return minLongitude/C;
	}
}
