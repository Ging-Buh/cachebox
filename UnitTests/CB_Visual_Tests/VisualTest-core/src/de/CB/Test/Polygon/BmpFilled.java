package de.CB.Test.Polygon;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;
import org.mapsforge.core.graphics.ResourceBitmap;



import CB_UI_Base.graphics.PolygonDrawable;
import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_Utils.Plattform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;

public class BmpFilled extends PolygonTestBase
{
	BitmapDrawable StreamBMP;

	String FilledBmpPath = "assets/freizeitkarte/patterns/1-watt.png";

	public BmpFilled()
	{
		super(" Polygone Test Bitmap filled" + br + "Mapsforge", "Cachebox");

		if (Plattform.used == Plattform.Android)
		{
			FilledBmpPath = "storage/extSdCard/freizeitkarte/themes/freizeitkarte/patterns/1-watt.png";
		}
		else
		{
			FilledBmpPath = "assets/themes/freizeitkarte/patterns/1-watt.png";
		}
	}

	@Override
	public void work()
	{

		// ################# GDX

		if (Plattform.used == Plattform.Android)
		{
			InputStream stream;
			try
			{
				stream = new FileInputStream(FilledBmpPath);
				StreamBMP = new BitmapDrawable(stream, FilledBmpPath.hashCode(), 1f);
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else
		{
			InputStream stream;
			FileHandle fh = Gdx.files.internal(FilledBmpPath);
			stream = fh.read();
			// stream = new FileInputStream(FilledBmpPath);
			StreamBMP = new BitmapDrawable(stream, FilledBmpPath.hashCode(), 1f);
		}

		paint.setBitmapShader(StreamBMP);
		polygon = new PolygonDrawable(vertices, ECT.computeTriangles(vertices).toArray(), paint, 256, 256);

		// ################# Mapsforge

		GraphicFactory factory = Mapsforge_Factory;
		bmp = factory.createBitmap(256, 256);
		Canvas canvas = factory.createCanvas();
		canvas.setBitmap(bmp);

		Path path = factory.createPath();

		// Flip Y (256-y)
		path.moveTo(vertices[0], 256 - vertices[1]);

		for (int i = 2; i < vertices.length - 1; i += 2)
		{
			path.lineTo(vertices[i], 256 - vertices[i + 1]);
		}

		Paint MF_Paint = factory.createPaint();

		try
		{

			InputStream MFstream = null;

			if (Plattform.used == Plattform.Android)
			{

				try
				{
					MFstream = new FileInputStream(FilledBmpPath);

				}
				catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			else
			{

				FileHandle fh = Gdx.files.internal(FilledBmpPath);
				MFstream = fh.read();

			}

			// InputStream MFstream = new FileInputStream(FilledBmpPath);
			ResourceBitmap bmpShader = factory.createResourceBitmap(MFstream, FilledBmpPath.hashCode());
			MF_Paint.setBitmapShader(bmpShader);
			// MF_Paint.setStyle(Style.STROKE);

			canvas.drawPath(path, MF_Paint);

			tex = Bmp2Texture(bmp);

		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void render(Batch batch)
	{
		super.render(batch);

		if (StreamBMP != null)
		{

			float w = StreamBMP.getWidth();
			float h = StreamBMP.getHeight();
			// StreamBMP.draw(batch, 200, 600, w, h);
			//
			// StreamBMP.draw(batch, 300, 600, w * 2, h * 2);
			isReady = true;
		}
	}
}
