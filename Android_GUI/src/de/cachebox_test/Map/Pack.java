package de.cachebox_test.Map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import CB_Core.Map.BoundingBox;
import CB_Core.Map.Descriptor;
import CB_Core.Map.ManagerBase;
import CB_Core.Map.PackBase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Pack extends PackBase
{

	public Pack(CB_Core.Map.Layer layer)
	{
		super(layer);
	}

	public Pack(ManagerBase manager, String file) throws IOException
	{
		super(manager, file);
	}

	/*
	 * public int CompareTo(object obj) { Pack cmp = obj as Pack; if (this.MaxAge < cmp.MaxAge) return -1;
	 * 
	 * if (this.MaxAge > cmp.MaxAge) return 1;
	 * 
	 * return 0; }
	 */
	// / <summary>
	// /
	// / </summary>
	// / <param name="bbox">Bounding Box</param>
	// / <param name="desc">Descriptor</param>
	// / <returns>Bitmap der Kachel</returns>
	public Bitmap LoadFromBoundingBox(BoundingBox bbox, Descriptor desc)
	{
		try
		{
			byte[] buffer = LoadFromBoundingBoxByteArray(bbox, desc);
			if (buffer == null) return null;

			Bitmap result = BitmapFactory.decodeByteArray(buffer, 0, (int) buffer.length);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			result.compress(Bitmap.CompressFormat.JPEG, 80, baos);
			Bitmap bitj = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
			result.recycle();
			baos.close();
			return bitj;
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
		}

		return null;

	}

}
