package de.CB.TestBase.Map;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import CB_Locator.Map.BoundingBox;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.PackBase;

public class DesktopPack extends PackBase
{

	public DesktopPack(CB_Locator.Map.Layer layer)
	{
		super(layer);
	}

	public DesktopPack(ManagerBase manager, String file) throws IOException
	{
		super(manager, file);
	}

	// // unpack all files to cache
	// // extractImages();
	// private void extractImages()
	// {
	// for (BoundingBox bbox : BoundingBoxes)
	// {
	// int z = bbox.Zoom;
	// for (int x = bbox.MinX; x <= bbox.MaxX; x++)
	// {
	// for (int y = bbox.MinY; y <= bbox.MaxY; y++)
	// {
	// Descriptor desc = new Descriptor(x, y, z);
	// byte[] b = LoadFromBoundingBoxByteArray(bbox, desc);
	// String fname = Layer.GetLocalFilename(desc);
	// File ff = new File(fname);
	// if (!ff.getParentFile().exists())
	// {
	// ff.getParentFile().mkdirs();
	// }
	// try
	// {
	// FileOutputStream fos = new FileOutputStream(ff.getAbsoluteFile());
	// fos.write(b);
	// fos.close();
	// }
	// catch (Exception e)
	// {
	// // TODO: handle exception
	// }
	// }
	// }
	//
	// }
	//
	// }

	@Override
	public byte[] LoadFromBoundingBoxByteArray(BoundingBox bbox, Descriptor desc)
	{
		try
		{
			if (bbox.Zoom != desc.getZoom()) return null;

			int index = (desc.getY() - bbox.MinY) * bbox.Stride + (desc.getX() - bbox.MinX) - 1;
			long offset = bbox.OffsetToIndex + index * 8;

			FileInputStream stream = new FileInputStream(Filename);
			/* Stream stream = new FileStream(Filename, FileMode.Open, FileAccess.Read); */
			// stream.Seek(offset, SeekOrigin.Begin);
			stream.skip(offset);

			// BinaryReader reader = new BinaryReader(stream);
			DataInputStream reader = new DataInputStream(stream);

			long tileOffset = Long.reverseBytes(reader.readLong());
			long nextOffset = Long.reverseBytes(reader.readLong());
			int length = (int) (nextOffset - tileOffset);

			if (length == 0)
			{
				reader.close();
				return null;
			}

			stream.skip(tileOffset - offset - 16);
			byte[] buffer = new byte[length];
			stream.read(buffer, 0, length);

			reader.close();

			// check for support / conversion
			byte[] signature = new byte[]
				{ (byte) 137, (byte) 80, (byte) 78, (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10 };
			if (Arrays.equals(signature, get(buffer, 0, 8)))
			{
				// es ist ein png
				byte BitDepth = buffer[24];
				// byte ColourType = buffer[25];
				// byte CompressionMethod = buffer[26];
				// BitDepth not supported by pixmap
				switch (BitDepth)
				{
				case 4:
					// Logger.DEBUG("[PackBase] unsupported png in Pack " + this.Filename + " tile: " + desc);
					InputStream in = new ByteArrayInputStream(buffer);
					BufferedImage img = ImageIO.read(in);
					ByteArrayOutputStream bas = new ByteArrayOutputStream();
					ImageIO.write(img, "jpg", bas);
					byte[] data = bas.toByteArray();
					bas.close();
					return data;
					// break;
				case 8:
					// supported
					break;
				default:
					// perhaps supported
					break;
				}
			}
			return buffer;
		}
		catch (Exception exc)
		{
			/*
			 * #if DEBUG Global.AddLog("Pack.LoadFromBoundingBox: Out of memory!" + exc.ToString()); Global.AddMemoryLog(); #endif
			 */
		}

		return null;

	}

}
