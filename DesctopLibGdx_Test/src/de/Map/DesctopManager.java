package de.Map;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import CB_Core.FileIO;
import CB_Core.Map.BoundingBox;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;
import CB_Core.Map.PackBase;

public class DesctopManager extends ManagerBase
{

	public DesctopManager()
	{
		super();
	}

	@Override
	public byte[] LoadLocalPixmap(Layer layer, Descriptor desc)
	{
		// Mapsforge 3.0
		if (layer.isMapsForge)
		{
			return null;
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

	// / <summary>
	// / L�d die Kachel mit dem �bergebenen Descriptor
	// / </summary>
	// / <param name="layer"></param>
	// / <param name="tile"></param>
	// / <returns></returns>
	public boolean CacheTile(Layer layer, Descriptor tile)
	{
		if (tile == null) return false;

		// Gibts die Kachel schon in einem Mappack? Dann kann sie �bersprungen
		// werden!
		for (PackBase pack : mapPacks)
			if (pack.Layer == layer) if (pack.Contains(tile) != null) return true;

		String filename = layer.GetLocalFilename(tile);
		String path = layer.GetLocalPath(tile);
		String url = layer.GetUrl(tile);

		// Falls Kachel schon geladen wurde, kann sie �bersprungen werden
		synchronized (this)
		{
			if (FileIO.FileExists(filename)) return true;
		}

		// Kachel laden
		// set the connection timeout value to 15 seconds (15000 milliseconds)
		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONECTION_TIME_OUT);

		HttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpResponse response = null;

		try
		{
			response = httpclient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();

				String responseString = out.toString();

				// Verzeichnis anlegen
				synchronized (this)
				{
					if (!FileIO.createDirectory(path)) return false;
				}
				// Datei schreiben
				synchronized (this)
				{
					FileOutputStream stream = new FileOutputStream(filename, false);

					out.writeTo(stream);
					stream.close();
				}

				NumTilesLoaded++;
				// Global.TransferredBytes += result.Length;

				// ..more logic
			}
			else
			{
				// Closes the connection.
				response.getEntity().getContent().close();
				// throw new IOException(statusLine.getReasonPhrase());
				return false;
			}

		}
		catch (Exception ex)
		{
			return false;
		}
		/*
		 * finally { if (stream != null) { stream.Close(); stream = null; } if (responseStream != null) { responseStream.Close();
		 * responseStream = null; } if (webResponse != null) { webResponse.Close(); webResponse = null; } if (webRequest != null) {
		 * webRequest.Abort(); webRequest = null; } GC.Collect(); }
		 */
		return true;
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

}
