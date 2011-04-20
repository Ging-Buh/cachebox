package de.droidcachebox.Views;

import java.io.File;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.TouchImageView;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Gallery.LayoutParams;

public class SpoilerView extends FrameLayout  implements ViewOptionsMenu, SelectedCacheEvent, AdapterView.OnItemSelectedListener{

	RelativeLayout spoilerLayout;
	Context context;
	Gallery g;
	Cache aktCache;
	TextView spoilerFilename;
	TouchImageView spoilerImage;
    
	public SpoilerView(Context context, LayoutInflater inflater) {
		super(context);
		this.context = context;

		aktCache = null;

		SelectedCacheEventList.Add(this);
		
		RelativeLayout spoilerLayout = (RelativeLayout)inflater.inflate(R.layout.spoilerview, null, false);
		this.addView(spoilerLayout);
	
        Gallery g = (Gallery) findViewById(R.id.spoilerGallery);
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
	
	@Override
	public void onItemSelected(AdapterView parent, View v, int position, long id) {
    //    mSwitcher.setImageResource(mImageIds[position]);
		String filename = Global.SelectedCache().SpoilerRessources().get(position);
		filename = filename.substring(0, filename.lastIndexOf("."));
		if (filename.indexOf(aktCache.GcCode) == 0)
			filename = filename.substring(aktCache.GcCode.length());
		if (filename.indexOf(" - ") == 0)
			filename = filename.substring(3);
		spoilerFilename.setText(filename);
		
        String file = Config.GetString("SpoilerFolder") + "/" + Global.SelectedCache().GcCode.substring(0, 4) + "/" + Global.SelectedCache().SpoilerRessources().get(position);
        bmp = BitmapFactory.decodeFile(file);
        spoilerImage.setImage(bmp, spoilerImage.getWidth(), spoilerImage.getHeight());
    }

	@Override
    public void onNothingSelected(AdapterView parent) {
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

            String file = Config.GetString("SpoilerFolder") + "/" + Global.SelectedCache().GcCode.substring(0, 4) + "/" + Global.SelectedCache().SpoilerRessources().get(position);
            Bitmap bit = BitmapFactory.decodeFile(file);
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

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		// TODO Auto-generated method stub

		aktCache = cache;

        Gallery g = (Gallery) findViewById(R.id.spoilerGallery);
        g.setAdapter(new ImageAdapter(context));		
/*
        if (aktCache.SpoilerRessources().size() > 1)
			g.setVisibility(1);
		else
			g.setVisibility(0);
*/
	}

	@Override
	public void OnShow() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
