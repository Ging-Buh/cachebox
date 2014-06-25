package org.mapsforge.map.layer.renderer;

import junit.framework.TestCase;

import org.junit.Test;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;

import CB_UI_Base.Global;

public class LatLonToPixel extends TestCase
{
	private double tileLatLon_0_x, tileLatLon_0_y, tileLatLon_1_x, tileLatLon_1_y;
	private double divLon, divLat;

	private final int tileSize = 256;

	Tile tile = new Tile(35207, 21477, (byte) 16);
	private LatLong latLong = new LatLong(52.57133375654519, 13.400573730475);

	private final int TEST_COUNT = 10000000;

	@Test
	public void test_LatLonToPixel()
	{

		tileLatLon_0_x = MercatorProjection.tileXToLongitude(tile.tileX, tile.zoomLevel);
		tileLatLon_0_y = MercatorProjection.tileYToLatitude(tile.tileY, tile.zoomLevel);
		tileLatLon_1_x = MercatorProjection.tileXToLongitude(tile.tileX + 1, tile.zoomLevel);
		tileLatLon_1_y = MercatorProjection.tileYToLatitude(tile.tileY + 1, tile.zoomLevel);

		divLon = (tileLatLon_0_x - tileLatLon_1_x) / tileSize;
		divLat = (tileLatLon_0_y - tileLatLon_1_y) / tileSize;

		long begin = System.currentTimeMillis();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			Cachebox();
		}

		System.out.print("Cachebox tileX:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		begin = System.currentTimeMillis();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			Mapsforge();
		}

		System.out.print("Mapsforge tileX:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		float a = Mapsforge();
		float b = Cachebox();

		assertEquals(a, b);

		// #############################################################
		begin = System.currentTimeMillis();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			CacheboxY();
		}

		System.out.print("Cachebox tileY:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		begin = System.currentTimeMillis();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			MapsforgeY();
		}

		System.out.print("Mapsforge tileY:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		a = MapsforgeY();
		b = CacheboxY();

		// assertEquals(a, b);

		// #############################################################
		begin = System.currentTimeMillis();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			LatLonInitFast();
		}

		System.out.print("Cachebox tileY:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		begin = System.currentTimeMillis();

		for (int i = 0; i < TEST_COUNT; i++)
		{
			LatLonInit();
		}

		System.out.print("Mapsforge tileY:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		LatLong c = LatLonInit();
		fastLatLong d = LatLonInitFast();

		assertEquals(d, c); // must equals check fastLatLong.equals(LatLon) only fastLatLon has a comparator for this

	}

	private float Mapsforge()
	{
		float pixelX = (float) (MercatorProjection.longitudeToPixelX(latLong.getLongitude(), tile.zoomLevel, tileSize) - (tile.tileX * tileSize));
		return pixelX;
	}

	private float Cachebox()
	{
		// double pixelX = Descriptor.LongitudeToTileX(zoomLevel, latLong.longitude, tileSize) - (tileX * tileSize);
		float pixelX = (float) ((tileLatLon_0_x - latLong.getLongitude()) / divLon);
		return pixelX;
	}

	private float MapsforgeY()
	{

		float pixelY = (float) (MercatorProjection.latitudeToPixelY(latLong.getLatitude(), tile.zoomLevel, tileSize) - (tile.tileY * tileSize));
		return pixelY;
	}

	private float CacheboxY()
	{

		// double pixelY = Descriptor.LatitudeToTileY(zoomLevel, latLong.latitude, tileSize) - (tileY * tileSize);
		float pixelY = (float) ((tileLatLon_0_y - latLong.getLatitude()) / divLat);
		return pixelY;
	}

	LatLong LatLonInit()
	{
		return new LatLong(52.57133375654519, 13.400573730475);
	}

	fastLatLong LatLonInitFast()
	{
		return new fastLatLong(52.57133375654519, 13.400573730475);
	}

	private class fastLatLong implements Comparable<fastLatLong>
	{
		/**
		 * The latitude coordinate of this LatLong in degrees.
		 */
		public final double latitude;

		/**
		 * The longitude coordinate of this LatLong in degrees.
		 */
		public final double longitude;

		/**
		 * @param latitude
		 *            the latitude coordinate in degrees.
		 * @param longitude
		 *            the longitude coordinate in degrees.
		 * @throws IllegalArgumentException
		 *             if a coordinate is invalid.
		 */
		public fastLatLong(double latitude, double longitude)
		{

			this.latitude = latitude;
			this.longitude = longitude;
		}

		@Override
		public int compareTo(fastLatLong latLong)
		{
			if (this.longitude > latLong.longitude)
			{
				return 1;
			}
			else if (this.longitude < latLong.longitude)
			{
				return -1;
			}
			else if (this.latitude > latLong.latitude)
			{
				return 1;
			}
			else if (this.latitude < latLong.latitude)
			{
				return -1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			else if (!(obj instanceof LatLong))
			{
				return false;
			}
			LatLong other = (LatLong) obj;
			if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.getLatitude()))
			{
				return false;
			}
			else if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.getLongitude()))
			{
				return false;
			}
			return true;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(this.latitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.longitude);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

	}

}
