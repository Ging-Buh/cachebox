package org.mapsforge.map.layer.renderer;

import junit.framework.TestCase;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.MercatorProjection;

import CB_Locator.Map.Descriptor;
import CB_UI_Base.Global;

public class LatLonToPixel extends TestCase
{
	public void test_LatLonToPixel()
	{
		long begin = System.currentTimeMillis();

		for (int i = 0; i < 1000000; i++)
		{
			Cachebox();
		}

		System.out.print("Cachebox tileX:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		begin = System.currentTimeMillis();

		for (int i = 0; i < 1000000; i++)
		{
			Mapsforge();
		}

		System.out.print("Mapsforge tileX:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		double a = Mapsforge();
		double b = Cachebox();

		assertEquals(a, b);

		// #############################################################
		begin = System.currentTimeMillis();

		for (int i = 0; i < 1000000; i++)
		{
			CacheboxY();
		}

		System.out.print("Cachebox tileY:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		begin = System.currentTimeMillis();

		for (int i = 0; i < 1000000; i++)
		{
			MapsforgeY();
		}

		System.out.print("Mapsforge tileY:" + (System.currentTimeMillis() - begin) + "ms" + Global.br);

		a = MapsforgeY();
		b = CacheboxY();

		assertEquals(a, b);

	}

	private final int tileSize = 256;
	private final byte zoomLevel = 14;

	private long tileX = 1000;
	private long tileY = 2000;

	private LatLong latLong = new LatLong(53.0, 13.0);

	private double Mapsforge()
	{
		double pixelX = MercatorProjection.longitudeToPixelX(latLong.longitude, zoomLevel, tileSize) - (tileX * tileSize);
		return pixelX;
	}

	private double Cachebox()
	{
		double pixelX = Descriptor.LongitudeToTileX(zoomLevel, latLong.longitude, tileSize) - (tileX * tileSize);
		return pixelX;
	}

	private double MapsforgeY()
	{

		double pixelY = MercatorProjection.latitudeToPixelY(latLong.latitude, zoomLevel, tileSize) - (tileY * tileSize);
		return pixelY;
	}

	private double CacheboxY()
	{

		double pixelY = Descriptor.LatitudeToTileY(zoomLevel, latLong.latitude, tileSize) - (tileY * tileSize);

		return pixelY;
	}

}
