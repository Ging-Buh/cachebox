package de.droidcachebox.Map;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.LoggerFactory;

import CB_Locator.Map.BoundingBox;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.PackBase;
import CB_Utils.Log.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AndroidPack extends PackBase {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(AndroidPack.class);

	public AndroidPack(CB_Locator.Map.Layer layer) {
		super(layer);
	}

	public AndroidPack(String file) throws IOException {
		super(file);
	}

	// LoadFromBoundingBoxByteArray
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
	public Bitmap LoadFromBoundingBox(BoundingBox bbox, Descriptor desc) {
		Log.debug(log, "LoadFromBoundingBox");
		try {
			byte[] buffer = LoadFromBoundingBoxByteArray(bbox, desc);
			if (buffer == null)
				return null;

			Bitmap result = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			result.compress(Bitmap.CompressFormat.JPEG, 80, baos);
			Bitmap bitj = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
			result.recycle();
			baos.close();
			return bitj;
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return null;

	}

	@Override
	public byte[] LoadFromBoundingBoxByteArray(BoundingBox bbox, Descriptor desc) {
		try {
			if (bbox.Zoom != desc.getZoom())
				return null;

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

			if (length == 0) {
				reader.close();
				return null;
			}

			stream.skip(tileOffset - offset - 16);
			byte[] buffer = new byte[length];
			stream.read(buffer, 0, length);

			reader.close();

			// check for support / conversion
			byte[] signature = new byte[] { (byte) 137, (byte) 80, (byte) 78, (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10 };
			if (Arrays.equals(signature, get(buffer, 0, 8))) {
				// es ist ein png
				byte BitDepth = buffer[24];
				// byte ColourType = buffer[25];
				// byte CompressionMethod = buffer[26];
				// BitDepth not supported by pixmap
				switch (BitDepth) {
				case 4:
					// Log.debug(log, "[PackBase] unsupported png in Pack " + this.Filename + " tile: " + desc);
					Bitmap result = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
					ByteArrayOutputStream bas = new ByteArrayOutputStream();
					result.compress(Bitmap.CompressFormat.JPEG, 80, bas);
					result.recycle();
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
		} catch (Exception exc) {
			/*
			 * #if DEBUG Global.AddLog("Pack.LoadFromBoundingBox: Out of memory!" + exc.ToString()); Global.AddMemoryLog(); #endif
			 */
		}

		return null;

	}

}
