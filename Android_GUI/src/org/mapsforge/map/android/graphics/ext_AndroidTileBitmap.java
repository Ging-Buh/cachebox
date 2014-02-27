package org.mapsforge.map.android.graphics;

import CB_Utils.Lists.CB_List;
import android.graphics.Bitmap.Config;

public class ext_AndroidTileBitmap extends ext_AndroidBitmap
{
	static CB_List<android.graphics.Bitmap> ReusebleList = new CB_List<android.graphics.Bitmap>();

	private static android.graphics.Bitmap getReusable(int tileSize)
	{
		synchronized (ReusebleList)
		{
			for (android.graphics.Bitmap reuse : ReusebleList)
			{
				if (reuse != null && reuse.getWidth() == tileSize && reuse.getHeight() == tileSize)
				{
					ReusebleList.remove(reuse);
					return reuse;
				}
			}
		}
		return null;
	}

	public ext_AndroidTileBitmap(int tileSize)
	{
		android.graphics.Bitmap reuse = getReusable(tileSize);
		if (reuse != null)
		{
			this.bitmap = reuse;
		}
		else
		{
			this.bitmap = super.createAndroidBitmap(tileSize, tileSize, Config.ARGB_8888);
			;
		}
	}

	static int DEBUG_MAX_REUSABLE_COUNT = 0;

	@Override
	public void recycle()
	{

		// chk if reuseList full or cann put BufferdImage to ReuseList?
		synchronized (ReusebleList)
		{

			if (ReusebleList.size() < 15)
			{
				ReusebleList.add(this.bitmap);
			}
			else
			{
				this.bitmap.recycle();
			}

			DEBUG_MAX_REUSABLE_COUNT = Math.max(DEBUG_MAX_REUSABLE_COUNT, ReusebleList.size());
		}
		instCount++;
		this.bitmap = null;
	}
}
