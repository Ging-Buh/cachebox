package de.cachebox_test.Views;

import java.io.File;
import java.util.ArrayList;

import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.cachebox_test.R;
import de.cachebox_test.Components.TouchImageView;
import de.cachebox_test.Events.ViewOptionsMenu;

public class SpoilerView extends FrameLayout implements ViewOptionsMenu, AdapterView.OnItemSelectedListener
{

	RelativeLayout spoilerLayout;
	Context context;
	Gallery g;
	Cache aktCache;
	TextView spoilerFilename;
	TouchImageView spoilerImage;
	ArrayList<Bitmap> lBitmaps;
	loadProcessor processor;

	public SpoilerView(Context context, LayoutInflater inflater)
	{
		super(context);
		this.context = context;
		lBitmaps = new ArrayList<Bitmap>();

		aktCache = null;
		RelativeLayout spoilerLayout = (RelativeLayout) inflater.inflate(R.layout.spoilerview, null, false);
		this.addView(spoilerLayout);

		g = (Gallery) findViewById(R.id.spoilerGallery);
		g.setSpacing(0);
		spoilerFilename = (TextView) findViewById(R.id.spoilerFilename);
		spoilerImage = new TouchImageView(context);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.BELOW, R.id.spoilerFilename);
		params.addRule(RelativeLayout.ALIGN_BOTTOM);
		spoilerImage.setLayoutParams(params);
		spoilerLayout.addView(spoilerImage);

		g.setAdapter(new ImageAdapter(context));
		g.setOnItemSelectedListener(this);

		spoilerFilename.setTextColor(Color.BLACK);
	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{
	}

	Bitmap bmp;
	String nextBitmap = "";

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
	{
		// mSwitcher.setImageResource(mImageIds[position]);
		if (aktCache == null) return;
		String filename = GlobalCore.SelectedCache().SpoilerRessources().get(position);
		filename = filename.substring(0, filename.lastIndexOf("."));
		filename = FileIO.GetFileNameWithoutExtension(filename);
		if (filename.indexOf(aktCache.GcCode) == 0) filename = filename.substring(aktCache.GcCode.length());
		if (filename.indexOf(" - ") == 0) filename = filename.substring(3);
		spoilerFilename.setText(filename);
		String file = GlobalCore.SelectedCache().SpoilerRessources().get(position);

		// das Laden sollte noch in einen Thread ausgelagert werden
		nextBitmap = file;
		if (processor == null)
		{
			processor = new loadProcessor();
			processor.execute(file);
		}
	}

	public class ImageAdapter extends BaseAdapter
	{
		public ImageAdapter(Context c)
		{
			mContext = c;
		}

		public int getCount()
		{
			if (aktCache == null) return 0;
			if (aktCache.SpoilerExists()) return aktCache.SpoilerRessources().size();
			else
				return 0;
			// return mThumbIds.length;
		}

		public Object getItem(int position)
		{
			return position;
		}

		public long getItemId(int position)
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			ImageView i = new ImageView(mContext);

			// String file =
			// GlobalCore.SelectedCache().SpoilerRessources().get(position);
			Bitmap bit = null;
			bit = lBitmaps.get(position);
			if (bit == null) return null;
			i.setImageBitmap(bit);
			// i.setImageResource(mThumbIds[position]);
			i.setAdjustViewBounds(true);
			i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			// i.setBackgroundResource(R.drawable.picture_frame);
			return i;
		}

		private Context mContext;

	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
	{

		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		if (scaleWidth > scaleHeight) scaleWidth = scaleHeight;
		else
			scaleHeight = scaleWidth;
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(String f)
	{
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(f, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 100;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true)
		{
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeFile(f, o2);
	}

	@Override
	public void OnShow()
	{
		aktCache = GlobalCore.SelectedCache();
		lBitmaps.clear();
		for (String filename : aktCache.SpoilerRessources())
		{
			try
			{
				lBitmaps.add(decodeFile(filename));
			}
			catch (Exception exc)
			{
				Logger.Error("SpoilerView.onShow()", "AddBitmap", exc);
			}
		}
		g.setAdapter(new ImageAdapter(context));
	}

	@Override
	public void OnHide()
	{
	}

	@Override
	public void OnFree()
	{
	}

	@Override
	public int GetMenuId()
	{
		return 0;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0)
	{
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
	}

	@Override
	public int GetContextMenuId()
	{
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu)
	{
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item)
	{
		return false;
	}

	private class loadProcessor extends AsyncTask<String, Integer, Integer>
	{
		Bitmap bmp = null;

		@Override
		protected Integer doInBackground(String... params)
		{
			if (!(params == null || params[0].equals("")))
			{
				File testingFile = new File(params[0]);
				if (testingFile.exists())
				{
					System.gc();
					bmp = BitmapFactory.decodeFile(params[0]);
					if (params[0].equalsIgnoreCase(nextBitmap)) nextBitmap = "";
				}
			}

			return null;
		}

		protected void onPostExecute(Integer result)
		{
			if (bmp != null) spoilerImage.setImage(bmp, spoilerImage.getWidth(), spoilerImage.getHeight());
			processor = null;
			System.gc();

			if (!(nextBitmap.equals("")))
			{
				processor = new loadProcessor();
				processor.execute(nextBitmap);
			}
		}

	}

}
