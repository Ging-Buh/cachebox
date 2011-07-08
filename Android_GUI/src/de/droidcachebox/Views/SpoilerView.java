package de.droidcachebox.Views;

import java.util.ArrayList;

import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import de.droidcachebox.R;
import de.droidcachebox.Components.CacheDraw;
import de.droidcachebox.Components.TouchImageView;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;

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

public class SpoilerView extends FrameLayout  implements ViewOptionsMenu, SelectedCacheEvent, AdapterView.OnItemSelectedListener{

	RelativeLayout spoilerLayout;
	Context context;
	Gallery g;
	Cache aktCache;
	TextView spoilerFilename;
	TouchImageView spoilerImage;
	ArrayList<Bitmap> lBitmaps;
	loadProcessor processor;
	
	public SpoilerView(Context context, LayoutInflater inflater) {
		super(context);
		this.context = context;
		lBitmaps = new ArrayList<Bitmap>();
		
		aktCache = null;

		SelectedCacheEventList.Add(this);
		
		RelativeLayout spoilerLayout = (RelativeLayout)inflater.inflate(R.layout.spoilerview, null, false);
		this.addView(spoilerLayout);
	
        g = (Gallery) findViewById(R.id.spoilerGallery);
        g.setSpacing(0);
        spoilerFilename = (TextView) findViewById(R.id.spoilerFilename);
        spoilerImage = new TouchImageView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW, R.id.spoilerFilename);
        params.addRule(RelativeLayout.ALIGN_BOTTOM);
        spoilerImage.setLayoutParams(params);
        spoilerLayout.addView(spoilerImage);
		
        g.setAdapter(new ImageAdapter(context));
        g.setOnItemSelectedListener(this);

        spoilerFilename.setTextColor(Color.BLACK);
    }


	@Override
	public boolean ItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}
	Bitmap bmp;
	String nextBitmap = "";
	
	@Override
	public void onItemSelected(AdapterView parent, View v, int position, long id) {
    //    mSwitcher.setImageResource(mImageIds[position]);
		if(aktCache == null)
			return;
		String filename = GlobalCore.SelectedCache().SpoilerRessources().get(position);
		filename = filename.substring(0, filename.lastIndexOf("."));
		filename = FileIO.GetFileNameWithoutExtension(filename);
		if (filename.indexOf(aktCache.GcCode) == 0)
			filename = filename.substring(aktCache.GcCode.length());
		if (filename.indexOf(" - ") == 0)
			filename = filename.substring(3);
		spoilerFilename.setText(filename);
        String file = GlobalCore.SelectedCache().SpoilerRessources().get(position);
		
		// das Laden sollte noch in einen Thread ausgelagert werden
        nextBitmap = file;
        if (processor == null)
        {
        	processor = new loadProcessor();
        	processor.execute(file);
        }
//        bmp = BitmapFactory.decodeFile(file);
//        spoilerImage.setImage(bmp, spoilerImage.getWidth(), spoilerImage.getHeight());
    }

    public class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
        	if (aktCache == null) return 0;
        	if (aktCache.SpoilerExists())
        		return aktCache.SpoilerRessources().size();
        	else
        		return 0;
//            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);

            String file = GlobalCore.SelectedCache().SpoilerRessources().get(position);
            Bitmap bit = null;
       		bit = lBitmaps.get(position);
            if (bit == null)
            	return null;
            i.setImageBitmap(bit);
//            i.setImageResource(mThumbIds[position]);
            i.setAdjustViewBounds(true);
            i.setLayoutParams(new Gallery.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//            i.setBackgroundResource(R.drawable.picture_frame);
            return i;
        }

        private Context mContext;

    }
    
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
    
    	int width = bm.getWidth();
       	int height = bm.getHeight();
    	float scaleWidth = ((float) newWidth) / width;
    	float scaleHeight = ((float) newHeight) / height;
    	if (scaleWidth > scaleHeight)
    		scaleWidth = scaleHeight;
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
    
	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnShow() {
		// TODO Auto-generated method stub
		Logger.DEBUG("sv1");
		aktCache = GlobalCore.SelectedCache();
		lBitmaps.clear();
		CacheDraw.ReloadSpoilerRessources(aktCache);
		Logger.DEBUG("sv2");
		for (String filename : aktCache.SpoilerRessources())
		{
			try
			{
				Logger.DEBUG("sv3");
				Bitmap bmp = BitmapFactory.decodeFile(filename);
				Logger.DEBUG("sv4");

				lBitmaps.add(getResizedBitmap(bmp, 200, 100));
				Logger.DEBUG("sv5");
				bmp.recycle();
				Logger.DEBUG("sv6");
			} catch (Exception exc)
			{
				Logger.Error("SpoilerView.onShow()", "AddBitmap", exc);
			}
		}
		Logger.DEBUG("sv7");
        g.setAdapter(new ImageAdapter(context));		
		Logger.DEBUG("sv8");
	}


	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
	}

	@Override
	public void OnFree() {
		
	}


	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	
    private class loadProcessor extends AsyncTask<String, Integer, Integer> {
    	Bitmap bmp = null;
		@Override
		protected Integer doInBackground(String... params) {
	        bmp = BitmapFactory.decodeFile(params[0]);
	        if (params[0].equalsIgnoreCase(nextBitmap))
	        	nextBitmap = "";
			return null;
		}
		
		protected void onPostExecute(Integer result) {
			if (bmp != null)				
				spoilerImage.setImage(bmp, spoilerImage.getWidth(), spoilerImage.getHeight());			
			processor = null;
			if (!(nextBitmap.equals("")))
			{
	        	processor = new loadProcessor();
	        	processor.execute(nextBitmap);				
			}
		}    	

    }

}
