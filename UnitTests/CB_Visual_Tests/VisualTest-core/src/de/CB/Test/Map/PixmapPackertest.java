package de.CB.Test.Map;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;





import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_Utils.Plattform;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.ObjectSet;

import de.CB.Test.Tag;
import de.CB.TestBase.Actions.TestCaseBase;

public class PixmapPackertest extends TestCaseBase
{
	private final CB_List<BitmapDrawable> BmpList = new CB_List<BitmapDrawable>();

	private static final float SCALE = 3;

	public PixmapPackertest()
	{
		super("PixmapPacker", "");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void work()
	{
		try
		{
			InputStream stream = getPngStream("museum");
			BmpList.add(new BitmapDrawable(stream, "museum".hashCode(), SCALE));

		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			Gdx.app.error(Tag.TAG, "", e);
		}

		try
		{
			InputStream stream = getPngStream("flughafen");
			BmpList.add(new BitmapDrawable(stream, "flughafen".hashCode(), SCALE));

		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			Gdx.app.error(Tag.TAG, "", e);
		}

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					InputStream stream = getPngStream("polizei");
					BmpList.add(new BitmapDrawable(stream, "polizei".hashCode(), SCALE));

				}
				catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					Gdx.app.error(Tag.TAG, "", e);
				}

				try
				{
					InputStream stream = getPngStream("polizei");
					BmpList.add(new BitmapDrawable(stream, "polizei".hashCode(), SCALE));

				}
				catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					Gdx.app.error(Tag.TAG, "", e);
				}
			}
		});
		thread.start();
	}

	private InputStream getPngStream(String Name) throws FileNotFoundException
	{
		String path;
		if (Plattform.used == Plattform.Android)
		{
			path = "storage/extSdCard/freizeitkarte/themes/freizeitkarte/symbols/" + Name + ".png";
		}
		else
		{
			path = "assets/themes/freizeitkarte/symbols/" + Name + ".png";
		}
		return new FileInputStream(path);
	}

	int count = 0;

	@Override
	public void draw(Batch batch)
	{
		float x = 0;

		for (int i = 0, n = BmpList.size(); i < n; i++)
		{
			BitmapDrawable bmp = BmpList.get(i);
			bmp.draw(batch, x, 0, bmp.getWidth(), bmp.getHeight());

			Texture tex = bmp.getTexture();
			if (tex != null) batch.draw(tex, x, 50, bmp.getWidth(), bmp.getHeight());

			x += 2 + bmp.getWidth();
		}

		if (count++ < 10)
		{
			try
			{
				InputStream stream = getPngStream("leuchtturm");
				BmpList.add(new BitmapDrawable(stream, "leuchtturm".hashCode(), SCALE));

			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				Gdx.app.error(Tag.TAG, "", e);
			}
		}
		else if (count < 12)
		{
			Timer timer = new Timer();
			TimerTask task = new TimerTask()
			{
				@Override
				public void run()
				{
					try
					{
						InputStream stream = getPngStream("geldautomat");
						BmpList.add(new BitmapDrawable(stream, "geldautomat".hashCode(), SCALE));

					}
					catch (FileNotFoundException e)
					{
						// TODO Auto-generated catch block
						Gdx.app.error(Tag.TAG, "", e);
					}
				}
			};
			timer.schedule(task, 1600);
			Thread thread = new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					try
					{
						InputStream stream = getPngStream("hotel-restaurant");
						BmpList.add(new BitmapDrawable(stream, "hotel-restaurant".hashCode(), SCALE));

					}
					catch (FileNotFoundException e)
					{
						// TODO Auto-generated catch block
						Gdx.app.error(Tag.TAG, "", e);
					}

				}
			});
			thread.start();
		}
		else
		{
			System.out.print("");
		}

		if (BitmapDrawable.Atlas != null)
		{
			ObjectSet<Texture> texSet = BitmapDrawable.Atlas.getTextures();
			for (Texture tex : texSet)
			{
				batch.draw(tex, 50, 50, 500, 500);
			}
		}

	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

}
