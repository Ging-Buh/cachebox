package de.Map;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.map.awt.AwtGraphicFactory;

import CB_Locator.Map.BoundingBox;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.PackBase;
import CB_Utils.Util.FileIO;

public class DesktopManager extends ManagerBase
{

	public DesktopManager()
	{
		super();
	}

	@Override
	public GraphicFactory getGraphicFactory()
	{
		return AwtGraphicFactory.INSTANCE;
	}

	@Override
	public byte[] LoadLocalPixmap(Layer layer, Descriptor desc, int ThreadIndex)
	{

		if (layer.isMapsForge)
		{
			return getMapsforgePixMap(layer, desc, ThreadIndex);
		}

		try
		{
			// Schauen, ob Tile im Cache liegt
			String cachedTileFilename = layer.GetLocalFilename(desc);

			long cachedTileAge = 0;

			if (FileIO.FileExists(cachedTileFilename))
			{
				File info = new File(cachedTileFilename);
				cachedTileAge = info.lastModified();
			}

			// Kachel im Pack suchen
			for (int i = 0; i < mapPacks.size(); i++)
			{
				PackBase mapPack = mapPacks.get(i);
				if ((mapPack.Layer.Name.equalsIgnoreCase(layer.Name)) && (mapPack.MaxAge >= cachedTileAge))
				{
					BoundingBox bbox = mapPacks.get(i).Contains(desc);

					if (bbox != null) return mapPacks.get(i).LoadFromBoundingBoxByteArray(bbox, desc);
				}
			}
			// Kein Map Pack am Start!
			// Falls Kachel im Cache liegt, diese von dort laden!
			if (cachedTileAge != 0)
			{
				File myImageFile = new File(cachedTileFilename);
				BufferedImage img = ImageIO.read(myImageFile);
				ByteArrayOutputStream bas = new ByteArrayOutputStream();
				ImageIO.write(img, "png", bas);
				byte[] data = bas.toByteArray();
				return data;
			}
		}
		catch (Exception exc)
		{
			/*
			 * #if DEBUG Global.AddLog("Manager.LoadLocalBitmap: " + exc.ToString()); #endif
			 */
		}
		return null;
	}

	@Override
	protected ImageData getImagePixel(byte[] img)
	{
		InputStream in = new ByteArrayInputStream(img);
		BufferedImage bImage;
		try
		{
			bImage = ImageIO.read(in);
		}
		catch (IOException e)
		{
			return null;
		}

		ImageData imgData = new ImageData();
		imgData.width = bImage.getWidth();
		imgData.height = bImage.getHeight();

		BufferedImage intimg = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

		ColorConvertOp op = new ColorConvertOp(null);
		op.filter(bImage, intimg);

		Raster ras = ((BufferedImage) intimg).getData();
		DataBufferInt db = (DataBufferInt) ras.getDataBuffer();
		imgData.PixelColorArray = db.getData();

		return imgData;

	}

	@Override
	protected byte[] getImageFromData(ImageData imgData)
	{

		BufferedImage dstImage = new BufferedImage(imgData.width, imgData.height, BufferedImage.TYPE_INT_RGB);

		dstImage.getRaster().setDataElements(0, 0, imgData.width, imgData.height, imgData.PixelColorArray);
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		try
		{
			ImageIO.write(dstImage, "png", bas);
		}
		catch (IOException e)
		{
			return null;
		}
		return bas.toByteArray();
	}

	@Override
	public PackBase CreatePack(String file) throws IOException
	{
		return new DesktopPack(this, file);
	}

}
