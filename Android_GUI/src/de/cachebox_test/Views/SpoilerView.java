package de.cachebox_test.Views;

import java.util.ArrayList;

import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.iStartPictureApp;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Events.ViewOptionsMenu;

@SuppressWarnings("deprecation")
public class SpoilerView extends FrameLayout implements ViewOptionsMenu, AdapterView.OnItemSelectedListener {
	RelativeLayout spoilerLayout;
	Context context;
	Gallery g;
	Cache aktCache;
	TextView spoilerFilename;
	WebView spoilerImage;
	ArrayList<Bitmap> lBitmaps;
	String fileName;

	public SpoilerView(Context context, LayoutInflater inflater) {
		super(context);
		this.context = context;
		lBitmaps = new ArrayList<Bitmap>();

		aktCache = null;
		spoilerLayout = (RelativeLayout) inflater.inflate(R.layout.spoilerview, null, false);
		this.addView(spoilerLayout);

		g = (Gallery) findViewById(R.id.spoilerGallery);
		g.setSpacing(0);
		spoilerFilename = (TextView) findViewById(R.id.spoilerFilename);
		spoilerImage = new WebView(context);
		spoilerImage.getSettings().setBuiltInZoomControls(true);
		spoilerImage.getSettings().setUseWideViewPort(true);
		spoilerImage.getSettings().setLoadWithOverviewMode(true);
		spoilerImage.setBackgroundColor(Global.getColor(R.attr.EmptyBackground));
		spoilerImage.setFocusable(false);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.BELOW, R.id.spoilerFilename);
		params.addRule(RelativeLayout.ALIGN_BOTTOM);
		spoilerImage.setLayoutParams(params);
		spoilerLayout.addView(spoilerImage);

		g.setAdapter(new ImageAdapter(context));
		g.setOnItemSelectedListener(this);

		spoilerFilename.setTextColor(Color.BLACK);

		PlatformConnector.setStartPictureApp(new iStartPictureApp() {
			@Override
			public void Start(String file) {
				Uri uriToImage = Uri.fromFile(new java.io.File(file));
				Intent shareIntent = new Intent(Intent.ACTION_VIEW);
				shareIntent.setDataAndType(uriToImage, "image/*");
				main.mainActivity.startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.app_name)));
			}
		});
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
	}

	Bitmap bmp;
	String nextBitmap = "";

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
		if (aktCache == null)
			return;
		String filename = GlobalCore.getSelectedCache().getSpoilerRessources().get(position).Name;

		spoilerFilename.setText(filename);
		String file = GlobalCore.getSelectedCache().getSpoilerRessources().get(position).LocalPath;

		String html = "<html><body><div style=\"width: 100%;\"><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"file://" + file + "\"></img></div></body></html>";
		spoilerImage.loadDataWithBaseURL("fake://not/needed", html, "text/html", "utf-8", "");

		fileName = file;
	}

	public class ImageAdapter extends BaseAdapter {
		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			if (aktCache == null)
				return 0;
			if (aktCache.hasSpoiler()) {
				return lBitmaps.size();
			} else
				return 0;
			// return mThumbIds.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);

			// String file =
			// GlobalCore.getSelectedCache().SpoilerRessources().get(position);
			Bitmap bit = null;
			bit = lBitmaps.get(position);
			if (bit == null) {
				return null;
			}
			i.setImageBitmap(bit);
			// i.setImageResource(mThumbIds[position]);
			i.setAdjustViewBounds(true);
			i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			// i.setBackgroundResource(R.drawable.picture_frame);
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

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(String f) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		Bitmap ori = BitmapFactory.decodeFile(f, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 100;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		Bitmap scaledBmp = BitmapFactory.decodeFile(f, o2);
		return scaledBmp == null ? ori : scaledBmp;
	}

	@Override
	public void OnShow() {
		// clear old selection
		spoilerFilename.setText(Translation.Get("NoSpoiler"));
		spoilerImage.loadDataWithBaseURL("fake://not/needed", "", "text/html", "utf-8", "");

		aktCache = GlobalCore.getSelectedCache();
		lBitmaps.clear();

		if (aktCache == null) {
			g.setAdapter(new ImageAdapter(context));
			return;
		}

		for (int i = 0, n = aktCache.getSpoilerRessources().size(); i < n; i++) {
			ImageEntry image = aktCache.getSpoilerRessources().get(i);
			try {
				Bitmap tmp = decodeFile(image.LocalPath);
				if (tmp != null)
					lBitmaps.add(tmp);
			} catch (Exception exc) {
			}
		}

		g.setAdapter(new ImageAdapter(context));
	}

	@Override
	public void OnHide() {
	}

	@Override
	public void OnFree() {
	}

	@Override
	public int GetMenuId() {
		return 0;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
	}

	@Override
	public int GetContextMenuId() {
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		return false;
	}

}
